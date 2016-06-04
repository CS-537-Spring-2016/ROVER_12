
package rover_febi;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.json.simple.JSONObject;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import common.Communication_trash;
import common.Coord;
import common.MapTile;
import common.ScanMap;
import enums.Science;
import enums.Terrain;

/**
 * The seed that this program is built on is a chat program example found here:
 * http://cs.lmu.edu/~ray/notes/javanetexamples/ Many thanks to the authors for
 * publishing their code examples
 */

public class ROVER_12 {

	BufferedReader in;
	PrintWriter out;
	String rovername;
	ScanMap scanMap;
	int sleepTime;
	String SERVER_ADDRESS = "localhost", line;
	static final int PORT_ADDRESS = 9537;
	static String myJSONStringBackupofMap;
	Coord currentLoc, previousLoc, rovergroupStartPosition = null,
			targetLocation = null;
	Map<Coord, MapTile> mapTileLog = new HashMap<Coord, MapTile>();
	// MapTile[][] mapTileLog = new MapTile[100][100];
	public ArrayList<Coord> pathMap = new ArrayList<Coord>();

	public ROVER_12() {
		// constructor
		System.out.println("ROVER_12 rover object constructed");
		rovername = "ROVER_12";
		SERVER_ADDRESS = "localhost";
		// this should be a safe but slow timer value
		sleepTime = 100; // in milliseconds - smaller is faster, but the server
							// will cut connection if it is too small
	}

	public ROVER_12(String serverAddress) {
		// constructor
		System.out.println("ROVER_12 rover object constructed");
		rovername = "ROVER_12";
		SERVER_ADDRESS = serverAddress;
		sleepTime = 200; // in milliseconds - smaller is faster, but the server
							// will cut connection if it is too small
	}

	/**
	 * Connects to the server then enters the processing loop.
	 */
	private void run() throws IOException, InterruptedException {

		String url = "http://23.251.155.186:3000/api";
		String corp_secret = "0FSj7Pn23t";
		Communication_trash com = new Communication_trash(url, rovername, corp_secret);

		// Make connection to SwarmServer and initialize streams
		Socket socket = null;
		try {
			socket = new Socket(SERVER_ADDRESS, PORT_ADDRESS);
			in = new BufferedReader(new InputStreamReader(
					socket.getInputStream()));
			out = new PrintWriter(socket.getOutputStream(), true);

			initSwarmServerConnection();

			// ********* Rover logic setup *********
			/**
			 * Get initial values that won't change
			 */
			// **** get equipment listing ****
			ArrayList<String> equipment = new ArrayList<String>();
			equipment = getEquipment();
			System.out.println(rovername + " equipment list results "
					+ equipment + "\n");

			// **** Request START_LOC Location from SwarmServer ****
			rovergroupStartPosition = requestStartLoc(socket);

			// **** Request TARGET_LOC Location from SwarmServer ****
			targetLocation = requestTargetLoc(socket);

			boolean stuck = false; // just means it did not change locations
									// between requests,
			// could be velocity limit or obstruction etc. group12 - anyone
			// knows what this means?
			boolean blocked = false;

			boolean[] cardinals = new boolean[4];
			cardinals[0] = false; // S: goingSouth
			cardinals[1] = true; // E: goingEast
			cardinals[2] = false; // N: goingNorth
			cardinals[3] = false; // W: goingWest

			int stepTrack = 0;

			/**
			 * #### Rover controller process loop ####
			 */
			while (true) {

				setCurrentLoc();
				previousLoc = currentLoc;

				// ***** do a SCAN ******
				if ((stepTrack++) % 4 == 0) {
					loadScanMapFromSwarmServer();
					scanMap.debugPrintMap();// debug
					debugPrintMapTileArrayText(mapTileLog, 30);
					debugPrintMapTileArray(mapTileLog);
				}

				// ***** MOVING *****
				// pull the MapTile array out of the ScanMap object
				MapTile[][] scanMapTiles = scanMap.getScanMap();

				int centerIndex = (scanMap.getEdgeSize() - 1) / 2;
				// tile S = y + 1; N = y - 1; E = x + 1; W = x - 1

				// store rover 12 path for easy return
				pathMap.add(new Coord(currentLoc.xpos, currentLoc.ypos));

				// febi-motion-logic starts***************
				roverMotionLogic(cardinals, scanMapTiles, centerIndex,
						currentLoc.xpos, currentLoc.ypos);
				setCurrentLoc();

				// test for stuckness
				// KS - below line causes a crash, must be modified
				// stuck = currentLoc.equals(previousLoc);

				// System.out.println("ROVER_12 stuck test " + stuck);
				// System.out.println("ROVER_12 blocked test " + blocked);
				// System.out.println(currentLoc);

				// this is the Rovers HeartBeat, it regulates how fast the Rover
				// cycles through the control loop
				Thread.sleep(sleepTime); // G12 - sleepTime has been reduced to
											// 100. is that alright?

				System.out
						.println("ROVER_12 ------------ bottom process control --------------");

				// This catch block closes the open socket connection to the
				// server
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (socket != null) {
				try {
					socket.close();
				} catch (IOException e) {
					System.out.println("ROVER_12 problem closing socket");
				}
			}
		}

	}// END of Rover main control loop

	private void initSwarmServerConnection() throws IOException {
		// Process all messages from server, wait until server requests
		// Rover ID
		// name - Return Rover Name to complete connection
		while (true) {
			String line = in.readLine();
			if (line.startsWith("SUBMITNAME")) {
				out.println(rovername); // This sets the name of this
										// instance
				// of a swarmBot for identifying the
				// thread to the server
				break;
			}
		}
	}

	private void roverMotionLogic(boolean[] cardinals,
			MapTile[][] scanMapTiles, int centerIndex, int currentXPos,
			int currentYPos) throws InterruptedException {
		// ************* Febi's rover motion logic **********
		// int tempRowArray;
		// int tempColumnArray;

		// logic if going in east
		if (cardinals[1]) {
			// Checks to see if there is science on current tile, if not
			// it moves East
			System.out
					.println("ROVER_12: scanMapTiles[centerIndex][centerIndex].getScience().getSciString() "
							+ scanMapTiles[centerIndex][centerIndex]
									.getScience().getSciString());
			if (scanMapTiles[centerIndex + 1][centerIndex].getScience().equals(
					"C")) {
				// move east
				cardinals = moveEast(cardinals);

			} else if (scanMapTiles[centerIndex][centerIndex + 1].getScience()
					.equals("C")) {
				// move south
				cardinals = moveSouth(cardinals);

			} else if (scanMapTiles[centerIndex][centerIndex - 1].getScience()
					.equals("C")) {
				// move north
				cardinals = moveNorth(cardinals);
			} else {
				// if next move to east is an obstacle
				if ((isTowardsEastIsObsatacle(scanMapTiles, centerIndex))
						|| (isAlreadyTraveledPathTowardsEast(currentXPos,
								currentYPos))) {
					// check whether south is obstacle
					if ((isTowardsSouthIsObsatacle(scanMapTiles, centerIndex))
							|| (isAlreadyTraveledPathTowardsSouth(currentXPos,
									currentYPos))) {
						// check whether north is obstacle
						if ((isTowardsNorthIsObsatacle(scanMapTiles,
								centerIndex))
								|| (isAlreadyTraveledPathTowardsNorth(
										currentXPos, currentYPos))) {
							// move west if no obstacle or else east
							if (isTowardsWestIsObsatacle(scanMapTiles,
									centerIndex)) {
								cardinals = moveEast(cardinals);
							} else {
								cardinals = randomPickMotion(cardinals,
										centerIndex, scanMapTiles);
								// cardinals = moveUsingPastPath(cardinals,
								// currentXPos, currentYPos);
							}

						} else {
							// move north
							cardinals = moveNorth(cardinals);
						}
					} else {
						// move south
						cardinals = moveSouth(cardinals);
					}
				}
				// when no obstacle is in next move to east
				else {
					// move east
					cardinals = moveEast(cardinals);
				}
			}
		} else if (cardinals[3]) {
			// if next move to west is an obstacle
			if ((isTowardsWestIsObsatacle(scanMapTiles, centerIndex))
					|| (isAlreadyTraveledPathTowardsWest(currentXPos,
							currentYPos))) {
				// check whether south is obstacle
				if ((isTowardsSouthIsObsatacle(scanMapTiles, centerIndex))
						|| (isAlreadyTraveledPathTowardsSouth(currentXPos,
								currentYPos))) {
					// check whether north is obstacle
					if ((isTowardsNorthIsObsatacle(scanMapTiles, centerIndex))
							|| (isAlreadyTraveledPathTowardsNorth(currentXPos,
									currentYPos))) {
						// move east if no obstacle or else move to west
						if (isTowardsEastIsObsatacle(scanMapTiles, centerIndex)) {
							cardinals = moveWest(cardinals);
						} else {
							cardinals = randomPickMotion(cardinals,
									centerIndex, scanMapTiles);

							// cardinals = moveUsingPastPath(cardinals,
							// currentXPos, currentYPos);
						}

					} else {
						// move north
						cardinals = moveNorth(cardinals);
					}
				} else {
					// move south
					cardinals = moveSouth(cardinals);
				}
			}
			// when no obstacle is in next move to west
			else {
				// move west
				cardinals = moveWest(cardinals);
			}
		} else if (cardinals[0]) {

			// check whether south is obstacle
			if ((isTowardsSouthIsObsatacle(scanMapTiles, centerIndex))
					|| (isAlreadyTraveledPathTowardsSouth(currentXPos,
							currentYPos))) {
				// if next move to west is an obstacle
				if ((isTowardsWestIsObsatacle(scanMapTiles, centerIndex))
						|| (isAlreadyTraveledPathTowardsWest(currentXPos,
								currentYPos))) {
					// check whether east is obstacle
					if ((isTowardsEastIsObsatacle(scanMapTiles, centerIndex))
							|| (isAlreadyTraveledPathTowardsEast(currentXPos,
									currentYPos))) {
						// move north if no obstacle or else move in south
						if (isTowardsNorthIsObsatacle(scanMapTiles, centerIndex)) {
							cardinals = moveSouth(cardinals);
						} else {
							cardinals = randomPickMotion(cardinals,
									centerIndex, scanMapTiles);

							// cardinals = moveUsingPastPath(cardinals,
							// currentXPos, currentYPos);
						}
					} else {
						// move east
						cardinals = moveEast(cardinals);
					}
				} else {
					// move west
					cardinals = moveWest(cardinals);
				}
			}
			// when no obstacle is in next move to south
			else {
				// move south
				cardinals = moveSouth(cardinals);
			}
		} else if (cardinals[2]) {

			// check whether north is obstacle
			if ((isTowardsNorthIsObsatacle(scanMapTiles, centerIndex))
					|| (isAlreadyTraveledPathTowardsNorth(currentXPos,
							currentYPos))) {
				// if next move to west is an obstacle
				if ((isTowardsWestIsObsatacle(scanMapTiles, centerIndex))
						|| (isAlreadyTraveledPathTowardsWest(currentXPos,
								currentYPos))) {
					// check whether east is obstacle
					if ((isTowardsEastIsObsatacle(scanMapTiles, centerIndex))
							|| (isAlreadyTraveledPathTowardsEast(currentXPos,
									currentYPos))) {
						// move south if no obstacle or else go back to
						// north098uuu
						if (isTowardsSouthIsObsatacle(scanMapTiles, centerIndex)) {
							cardinals = moveNorth(cardinals);
						} else {
							cardinals = randomPickMotion(cardinals,
									centerIndex, scanMapTiles);

							// cardinals = moveUsingPastPath(cardinals,
							// currentXPos, currentYPos);
						}
					} else {
						// move east
						cardinals = moveEast(cardinals);
					}
				} else {
					// move west
					cardinals = moveWest(cardinals);
				}
			}
			// when no obstacle is in next move to north
			else {
				// move north
				cardinals = moveNorth(cardinals);
			}
		}
	}

	private boolean[] randomPickMotion(boolean[] cardinals, int centerIndex,
			MapTile[][] scanMapTiles) {
		
		int randomNumber = randomNum(0, 3);

		if (cardinals[randomNumber] == true) {
			randomPickMotion(cardinals, centerIndex, scanMapTiles);
		} else {
			switch (randomNumber) {

			// going south
			case 0: {
				if (isTowardsSouthIsObsatacle(scanMapTiles, centerIndex)) {
					randomPickMotion(cardinals, centerIndex, scanMapTiles);
				} else {
					cardinals = moveSouth(cardinals);
				}
				break;
			}

			// going east
			case 1: {
				if (isTowardsEastIsObsatacle(scanMapTiles, centerIndex)) {
					randomPickMotion(cardinals, centerIndex, scanMapTiles);
				} else {
					cardinals = moveEast(cardinals);
				}
				break;
			}

			// going north
			case 2: {

				if (isTowardsNorthIsObsatacle(scanMapTiles, centerIndex)) {
					randomPickMotion(cardinals, centerIndex, scanMapTiles);
				} else {
					cardinals = moveNorth(cardinals);
				}
				break;
			}

			// going west

			case 3: {
				if (isTowardsSouthIsObsatacle(scanMapTiles, centerIndex)) {
					randomPickMotion(cardinals, centerIndex, scanMapTiles);
				} else {
					cardinals = moveSouth(cardinals);
				}
				break;

			}

			}

		}

		return cardinals;
	}

	public int randomNum(int min, int max) {
		Random rand = new Random();
		return rand.nextInt(max + 1) + min;
	}

	private boolean[] moveUsingPastPath(boolean[] cardinals, int currentXPos,
			int currentYPos) throws InterruptedException {
		try {
			Coord current = returnCurrentLoc(), prev = current.clone();

			for (int j = 0; (j < 10) && (j < pathMap.size()); j++) {
				for (int i = pathMap.size(); i > 0; i++) {
					while (current.equals(prev)) {
						Thread.sleep(300);
						current = returnCurrentLoc();
					}

					cardinals = assignTheMove(cardinals, pathMap.get(i),
							currentXPos, currentYPos);
					prev = current.clone();
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return cardinals;
	}

	private boolean[] assignTheMove(boolean[] cardinals, Coord eachCoord,
			int currentXPos, int currentYPos) {
		if (isPastPositonIsEast(cardinals, eachCoord, currentXPos, currentYPos)) {
			cardinals = moveEast(cardinals);

		} else if (isPastPositonIsWest(cardinals, eachCoord, currentXPos,
				currentYPos)) {
			cardinals = moveWest(cardinals);

		} else if (isPastPositonIsNorth(cardinals, eachCoord, currentXPos,
				currentYPos)) {
			cardinals = moveNorth(cardinals);

		} else if (isPastPositonIsSouth(cardinals, eachCoord, currentXPos,
				currentYPos)) {
			cardinals = moveSouth(cardinals);

		}
		return cardinals;
	}

	private boolean isPastPositonIsSouth(boolean[] cardinals, Coord eachCoord,
			int currentXPos, int currentYPos) {
		int previousXPos = eachCoord.xpos;
		int previousYPos = eachCoord.ypos;
		if ((previousXPos == currentXPos) && (previousYPos == currentYPos + 1)) {
			return true;
		}
		return false;
	}

	private boolean isPastPositonIsNorth(boolean[] cardinals, Coord eachCoord,
			int currentXPos, int currentYPos) {
		int previousXPos = eachCoord.xpos;
		int previousYPos = eachCoord.ypos;
		if ((previousXPos == currentXPos) && (previousYPos == currentYPos - 1)) {
			return true;
		}
		return false;
	}

	private boolean isPastPositonIsWest(boolean[] cardinals, Coord eachCoord,
			int currentXPos, int currentYPos) {
		int previousXPos = eachCoord.xpos;
		int previousYPos = eachCoord.ypos;
		if ((previousXPos == currentXPos - 1) && (previousYPos == currentYPos)) {
			return true;
		}
		return false;
	}

	private boolean isPastPositonIsEast(boolean[] cardinals, Coord eachCoord,
			int currentXPos, int currentYPos) {
		int previousXPos = eachCoord.xpos;
		int previousYPos = eachCoord.ypos;
		if ((previousXPos == currentXPos + 1) && (previousYPos == currentYPos)) {
			return true;
		}
		return false;
	}

	private boolean isAlreadyTraveledPathTowardsWest(int currentXPos,
			int currentYPos) {
		int nextXPosition = currentXPos - 1;
		int nextYPosition = currentYPos;
		for (Coord coord : pathMap) {
			if ((coord.xpos == nextXPosition) && (coord.ypos == nextYPosition)) {
				return true;
			}
		}
		return false;
	}

	private boolean isAlreadyTraveledPathTowardsNorth(int currentXPos,
			int currentYPos) {
		int nextXPosition = currentXPos;
		int nextYPosition = currentYPos - 1;
		for (Coord coord : pathMap) {
			if ((coord.xpos == nextXPosition) && (coord.ypos == nextYPosition)) {
				return true;
			}
		}
		return false;
	}

	private boolean isAlreadyTraveledPathTowardsSouth(int currentXPos,
			int currentYPos) {
		int nextXPosition = currentXPos;
		int nextYPosition = currentYPos + 1;
		for (Coord coord : pathMap) {
			if ((coord.xpos == nextXPosition) && (coord.ypos == nextYPosition)) {
				return true;
			}
		}
		return false;
	}

	private boolean isAlreadyTraveledPathTowardsEast(int currentXPos,
			int currentYPos) {
		int nextXPosition = currentXPos + 1;
		int nextYPosition = currentYPos;
		for (Coord coord : pathMap) {
			if ((coord.xpos == nextXPosition) && (coord.ypos == nextYPosition)) {
				return true;
			}
		}
		return false;
	}

	private boolean[] moveWest(boolean[] cardinals) {
		out.println("MOVE W");
		System.out.println("ROVER_12 request move W");
		cardinals[0] = false; // S
		cardinals[1] = false; // E
		cardinals[2] = false; // N
		cardinals[3] = true; // W
		return cardinals;
	}

	private boolean[] moveNorth(boolean[] cardinals) {
		out.println("MOVE N");
		System.out.println("ROVER_12 request move N");
		cardinals[0] = false; // S
		cardinals[1] = false; // E
		cardinals[2] = true; // N
		cardinals[3] = false; // W
		return cardinals;

	}

	private boolean[] moveSouth(boolean[] cardinals) {
		out.println("MOVE S");
		System.out.println("ROVER_12 request move S");
		cardinals[0] = true; // S
		cardinals[1] = false; // E
		cardinals[2] = false; // N
		cardinals[3] = false; // W
		return cardinals;

	}

	private boolean[] moveEast(boolean[] cardinals) {
		out.println("MOVE E");
		System.out.println("ROVER_12 request move E");
		cardinals[0] = false; // S
		cardinals[1] = true; // E
		cardinals[2] = false; // N
		cardinals[3] = false; // W
		return cardinals;
	}

	private boolean isTowardsWestIsObsatacle(MapTile[][] scanMapTiles,
			int centerIndex) {
		if (scanMapTiles[centerIndex - 1][centerIndex].getHasRover()
				|| scanMapTiles[centerIndex - 1][centerIndex].getTerrain() == Terrain.ROCK
				|| scanMapTiles[centerIndex - 1][centerIndex].getTerrain() == Terrain.NONE
				|| scanMapTiles[centerIndex - 1][centerIndex].getTerrain() == Terrain.FLUID
				|| scanMapTiles[centerIndex - 1][centerIndex].getTerrain() == Terrain.SAND) {
			return true;
		}
		return false;
	}

	private boolean isTowardsNorthIsObsatacle(MapTile[][] scanMapTiles,
			int centerIndex) {
		if (scanMapTiles[centerIndex][centerIndex - 1].getHasRover()
				|| scanMapTiles[centerIndex][centerIndex - 1].getTerrain() == Terrain.ROCK
				|| scanMapTiles[centerIndex][centerIndex - 1].getTerrain() == Terrain.NONE
				|| scanMapTiles[centerIndex][centerIndex - 1].getTerrain() == Terrain.FLUID
				|| scanMapTiles[centerIndex][centerIndex - 1].getTerrain() == Terrain.SAND) {
			return true;
		}
		return false;
	}

	private boolean isTowardsSouthIsObsatacle(MapTile[][] scanMapTiles,
			int centerIndex) {
		if (scanMapTiles[centerIndex][centerIndex + 1].getHasRover()
				|| scanMapTiles[centerIndex][centerIndex + 1].getTerrain() == Terrain.ROCK
				|| scanMapTiles[centerIndex][centerIndex + 1].getTerrain() == Terrain.NONE
				|| scanMapTiles[centerIndex][centerIndex + 1].getTerrain() == Terrain.FLUID
				|| scanMapTiles[centerIndex][centerIndex + 1].getTerrain() == Terrain.SAND) {
			return true;
		}
		return false;
	}

	private boolean isTowardsEastIsObsatacle(MapTile[][] scanMapTiles,
			int centerIndex) {
		if (scanMapTiles[centerIndex + 1][centerIndex].getHasRover()
				|| scanMapTiles[centerIndex + 1][centerIndex].getTerrain() == Terrain.ROCK
				|| scanMapTiles[centerIndex + 1][centerIndex].getTerrain() == Terrain.NONE
				|| scanMapTiles[centerIndex + 1][centerIndex].getTerrain() == Terrain.FLUID
				|| scanMapTiles[centerIndex + 1][centerIndex].getTerrain() == Terrain.SAND) {
			return true;
		}

		return false;
	}

	private void setCurrentLoc() throws IOException {

		String line;
		// **** Request Rover Location from SwarmServer ****
		out.println("LOC");
		line = in.readLine();
		if (line == null) {
			System.out.println(rovername + " check connection to server");
			line = "";
		}
		if (line.startsWith("LOC")) {
			// loc = line.substring(4);
			currentLoc = extractLocationFromString(line);

		}
	}

	private Coord returnCurrentLoc() throws IOException {
		Coord clone = new Coord(currentLoc.xpos, currentLoc.ypos);
		String line;
		// **** Request Rover Location from SwarmServer ****
		out.println("LOC");
		line = in.readLine();
		if (line == null) {
			System.out.println(rovername + " check connection to server");
			line = "";
		}
		if (line.startsWith("LOC")) {
			// loc = line.substring(4);
			clone = extractLocationFromString(line);

		}
		return clone;
	}

	// ####################### Support Methods #############################

	private void clearReadLineBuffer() throws IOException {
		while (in.ready()) {
			// System.out.println("ROVER_12 clearing readLine()");
			in.readLine();
		}
	}

	// method to retrieve a list of the rover's EQUIPMENT from the server
	private ArrayList<String> getEquipment() throws IOException {
		// System.out.println("ROVER_12 method getEquipment()");
		Gson gson = new GsonBuilder().setPrettyPrinting()
				.enableComplexMapKeySerialization().create();
		out.println("EQUIPMENT");

		String jsonEqListIn = in.readLine(); // grabs the string that was
												// returned first
		if (jsonEqListIn == null) {
			jsonEqListIn = "";
		}
		StringBuilder jsonEqList = new StringBuilder();
		// System.out.println("ROVER_12 incomming EQUIPMENT result - first readline: "
		// + jsonEqListIn);

		if (jsonEqListIn.startsWith("EQUIPMENT")) {
			while (!(jsonEqListIn = in.readLine()).equals("EQUIPMENT_END")) {
				if (jsonEqListIn == null) {
					break;
				}
				// System.out.println("ROVER_12 incomming EQUIPMENT result: " +
				// jsonEqListIn);
				jsonEqList.append(jsonEqListIn);
				jsonEqList.append("\n");
				// System.out.println("ROVER_12 doScan() bottom of while");
			}
		} else {
			// in case the server call gives unexpected results
			clearReadLineBuffer();
			return null; // server response did not start with "EQUIPMENT"
		}

		String jsonEqListString = jsonEqList.toString();
		ArrayList<String> returnList;
		returnList = gson.fromJson(jsonEqListString,
				new TypeToken<ArrayList<String>>() {
				}.getType());
		// System.out.println("ROVER_12 returnList " + returnList);

		return returnList;
	}

	// sends a SCAN request to the server and puts the result in the scanMap
	// array group12 - this raw JsonData should be used for our maptileLog?
	public void loadScanMapFromSwarmServer() throws IOException {
		// System.out.println("ROVER_12 method doScan()");
		setCurrentLoc();
		Coord scanLoc = new Coord(currentLoc.xpos, currentLoc.ypos);
		Gson gson = new GsonBuilder().setPrettyPrinting()
				.enableComplexMapKeySerialization().create();
		out.println("SCAN");

		String jsonScanMapIn = in.readLine(); // grabs the string that was
												// returned first
		if (jsonScanMapIn == null) {
			System.out.println("ROVER_12 check connection to server");
			jsonScanMapIn = "";
		}
		StringBuilder jsonScanMap = new StringBuilder();
		System.out.println("ROVER_12 incomming SCAN result - first readline: "
				+ jsonScanMapIn);

		if (jsonScanMapIn.startsWith("SCAN")) {
			while (!(jsonScanMapIn = in.readLine()).equals("SCAN_END")) {
				// System.out.println("ROVER_12 incomming SCAN result: " +
				// jsonScanMapIn);
				jsonScanMap.append(jsonScanMapIn);
				jsonScanMap.append("\n");
				// System.out.println("ROVER_12 doScan() bottom of while");
			}
		} else {
			// in case the server call gives unexpected results
			clearReadLineBuffer();
			return; // server response did not start with "SCAN"
		}
		// System.out.println("ROVER_12 finished scan while");

		String jsonScanMapString = jsonScanMap.toString();
		// debug print json object to a file
		// new MyWriter( jsonScanMapString, 0); //gives a strange result -
		// prints the \n instead of newline character in the file

		System.out.println("+++++++++++++++ jsonScanMapString +++++++++++++++");
		System.out.println(jsonScanMapString.toString());
		// try {
		// Thread.sleep(10000);
		// } catch (InterruptedException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }

		// System.out.println("ROVER_12 convert from json back to ScanMap class");
		// convert from the json string back to a ScanMap object
		scanMap = gson.fromJson(jsonScanMapString, ScanMap.class);

		myJSONStringBackupofMap = jsonScanMapString;
		loadMapTilesOntoGlobalMapLog(scanMap.getScanMap(), scanLoc);
	}

	private Coord requestStartLoc(Socket soc) throws IOException {

		// **** Request Rover Location from SwarmServer ****
		out.println("LOC");
		line = in.readLine();
		if (line == null) {
			System.out.println(rovername + " check connection to server");
			line = "";
		}
		if (line.startsWith("LOC")) {
			// loc = line.substring(4);
			currentLoc = extractLocationFromString(line);

		}
		System.out.println(rovername + " currentLoc at start: " + currentLoc);

		out.println("START_LOC " + currentLoc.xpos + " " + currentLoc.ypos);
		line = in.readLine();

		if (line == null || line == "") {
			System.out.println("ROVER_12 check connection to server");
			line = "";
		}

		//
		System.out.println();
		if (line.startsWith("START")) {
			rovergroupStartPosition = extractStartLOC(line);
		}
		return rovergroupStartPosition;
	}

	private Coord requestTargetLoc(Socket soc) throws IOException {

		// **** Request Rover Location from SwarmServer ****
		out.println("LOC");
		line = in.readLine();
		if (line == null) {
			System.out.println(rovername + " check connection to server");
			line = "";
		}
		if (line.startsWith("LOC")) {
			// loc = line.substring(4);
			currentLoc = extractLocationFromString(line);

		}
		System.out.println(rovername + " currentLoc at start: " + currentLoc);

		out.println("TARGET_LOC " + currentLoc.xpos + " " + currentLoc.ypos);
		line = in.readLine();

		if (line == null || line == "") {
			// System.out.println("ROVER_12 check connection to server");
			line = "";
		}

		if (line.startsWith("TARGET")) {
			targetLocation = extractTargetLOC(line);
		}
		return targetLocation;
	}

	public static Coord extractCurrLOC(String sStr) {
		sStr = sStr.substring(4);
		if (sStr.lastIndexOf(" ") != -1) {
			String xStr = sStr.substring(0, sStr.lastIndexOf(" "));
			// System.out.println("extracted xStr " + xStr);

			String yStr = sStr.substring(sStr.lastIndexOf(" ") + 1);
			// System.out.println("extracted yStr " + yStr);
			return new Coord(Integer.parseInt(xStr), Integer.parseInt(yStr));
		}
		return null;
	}

	public static Coord extractStartLOC(String sStr) {

		sStr = sStr.substring(10);

		if (sStr.lastIndexOf(" ") != -1) {
			String xStr = sStr.substring(0, sStr.lastIndexOf(" "));
			// System.out.println("extracted xStr " + xStr);

			String yStr = sStr.substring(sStr.lastIndexOf(" ") + 1);
			// System.out.println("extracted yStr " + yStr);
			return new Coord(Integer.parseInt(xStr), Integer.parseInt(yStr));
		}
		return null;
	}

	public static Coord extractTargetLOC(String sStr) {
		sStr = sStr.substring(11);
		if (sStr.lastIndexOf(" ") != -1) {
			String xStr = sStr.substring(0, sStr.lastIndexOf(" "));
			System.out.println("extracted xStr " + xStr);

			String yStr = sStr.substring(sStr.lastIndexOf(" ") + 1);
			System.out.println("extracted yStr " + yStr);
			return new Coord(Integer.parseInt(xStr), Integer.parseInt(yStr));
		}
		return null;
	}

	// this takes the server response string, parses out the x and x values and
	// returns a Coord object
	public static Coord extractLocationFromString(String sStr) {
		int indexOf;
		indexOf = sStr.indexOf(" ");
		sStr = sStr.substring(indexOf + 1);
		if (sStr.lastIndexOf(" ") != -1) {
			String xStr = sStr.substring(0, sStr.lastIndexOf(" "));
			// System.out.println("extracted xStr " + xStr);

			String yStr = sStr.substring(sStr.lastIndexOf(" ") + 1);
			// System.out.println("extracted yStr " + yStr);
			return new Coord(Integer.parseInt(xStr), Integer.parseInt(yStr));
		}
		return null;
	}

	public void debugPrintMapTileArray(MapTile[][] mapTileArray) {

		int edgeSize = mapTileArray.length;
		System.out.println("edge size: " + edgeSize);
		for (int k = 0; k < edgeSize + 2; k++) {
			System.out.print("--");
		}

		System.out.print("\n");

		for (int j = 0; j < edgeSize; j++) {

			System.out.print("j=" + j + "\t");

			System.out.print("| ");
			for (int i = 0; i < edgeSize; i++) {
				if (mapTileArray[i][j] == null) {
					System.out.print("n");
				}
				// check and print edge of map has first priority
				else if (mapTileArray[i][j].getTerrain().toString()
						.equals("NONE")) {
					System.out.print("XX");

					// next most important - print terrain and/or science
					// locations
					// terrain and science
				} else if (!(mapTileArray[i][j].getTerrain().toString()
						.equals("SOIL"))
						&& !(mapTileArray[i][j].getScience().toString()
								.equals("NONE"))) {
					// both terrain and science

					System.out.print(mapTileArray[i][j].getTerrain().toString()
							.substring(0, 1)
							+ mapTileArray[i][j].getScience().getSciString());
					// just terrain
				} else if (!(mapTileArray[i][j].getTerrain().toString()
						.equals("SOIL"))) {
					System.out.print(mapTileArray[i][j].getTerrain().toString()
							.substring(0, 1)
							+ " ");
					// just science
				} else if (!(mapTileArray[i][j].getScience().toString()
						.equals("NONE"))) {
					System.out.print(" "
							+ mapTileArray[i][j].getScience().getSciString());

					// if still empty check for rovers and print them
				} else if (mapTileArray[i][j].getHasRover()) {
					System.out.print("[]");

					// nothing here so print nothing
				} else {
					System.out.print("  ");
				}
			}
			System.out.print(" |\n");
		}
		for (int k = 0; k < edgeSize + 2; k++) {
			System.out.print("--");
		}
		System.out.print("\n");
	}

	public void debugPrintMapTileArrayText(Map<Coord, MapTile> globalMapCopy,
			int mapSize) {
		MapTile tile;

		for (int y = 0; y < mapSize; y++) {
			for (int x = 0; x < mapSize; x++) {
				tile = globalMapCopy.get(new Coord(x, y));
				System.out.print("x,y=" + x + "," + y + "\t" + tile + "\t/t");
			}
		}
	}

	/* **********************************
	 * The time the rover takes in getting to a point to another point
	 */

	@SuppressWarnings("unused")
	private long startWatch() {
		return System.currentTimeMillis();
	}

	@SuppressWarnings("unused")
	private long stopWatch(long start) {
		return System.currentTimeMillis() - start;
	}

	// public void debugPrintMapTileHashMap(Map<Coord,MapTile> maptiles, int
	// xStart, int yXtart, int edgeSize){
	// System.out.println("edge size: " + edgeSize);
	// for (int k = 0; k < edgeSize + 2; k++) {
	// System.out.print("--");
	// }
	//
	// System.out.print("\n");
	// for (int j = yXtart; j < edgeSize; j++) {
	// System.out.print("| ");
	// for (int i = yXtart; i < edgeSize; i++) {
	// // check and print edge of map has first priority
	// if (scanArray[i][j].getTerrain().toString().equals("NONE")) {
	// System.out.print("XX");
	//
	// // next most important - print terrain and/or science
	// // locations
	// // terrain and science
	// } else if (!(scanArray[i][j].getTerrain().toString()
	// .equals("SOIL"))
	// && !(scanArray[i][j].getScience().toString()
	// .equals("NONE"))) {
	// // both terrain and science
	//
	// System.out.print(scanArray[i][j].getTerrain().toString()
	// .substring(0, 1)
	// + scanArray[i][j].getScience().getSciString());
	// // just terrain
	// } else if (!(scanArray[i][j].getTerrain().toString()
	// .equals("SOIL"))) {
	// System.out.print(scanArray[i][j].getTerrain().toString()
	// .substring(0, 1)
	// + " ");
	// // just science
	// } else if (!(scanArray[i][j].getScience().toString()
	// .equals("NONE"))) {
	// System.out.print(" "
	// + scanArray[i][j].getScience().getSciString());
	//
	// // if still empty check for rovers and print them
	// } else if (scanArray[i][j].getHasRover()) {
	// System.out.print("[]");
	//
	// // nothing here so print nothing
	// } else {
	// System.out.print("  ");
	// }
	// }
	// System.out.print(" |\n");
	// }
	// for (int k = 0; k < edgeSize + 2; k++) {
	// System.out.print("--");
	// }
	// System.out.print("\n");
	// }

	public void debugPrintMapTileArray(Map<Coord, MapTile> globalMapCopy) {

		// FIXME
		int edgeSize = 100;
		System.out.println("edge size: " + edgeSize);
		for (int k = 0; k < edgeSize + 2; k++) {
			System.out.print("--");
		}

		System.out.print("\n");

		for (int j = 0; j < edgeSize; j++) {

			// System.out.print("j=" + j + "\t");

			System.out.print("| ");
			for (int i = 0; i < edgeSize; i++) {
				if (mapTileLog.get(new Coord(i, j)) == null) {
					System.out.print("nn");
				}
				// check and print edge of map has first priority
				else if (mapTileLog.get(new Coord(i, j)).getTerrain()
						.toString().equals("NONE")) {
					System.out.print("XX");

					// next most important - print terrain and/or science
					// locations
					// terrain and science
				} else if (!(mapTileLog.get(new Coord(i, j)).getTerrain()
						.toString().equals("SOIL"))
						&& !(mapTileLog.get(new Coord(i, j)).getScience()
								.toString().equals("NONE"))) {
					// both terrain and science

					System.out.print(mapTileLog.get(new Coord(i, j))
							.getTerrain().toString().substring(0, 1)
							+ mapTileLog.get(new Coord(i, j)).getScience()
									.getSciString());
					// just terrain
				} else if (!(mapTileLog.get(new Coord(i, j)).getTerrain()
						.toString().equals("SOIL"))) {
					System.out.print(mapTileLog.get(new Coord(i, j))
							.getTerrain().toString().substring(0, 1)
							+ " ");
					// just science
				} else if (!(mapTileLog.get(new Coord(i, j)).getScience()
						.toString().equals("NONE"))) {
					System.out.print(" "
							+ mapTileLog.get(new Coord(i, j)).getScience()
									.getSciString());

					// if still empty check for rovers and print them
				} else if (mapTileLog.get(new Coord(i, j)).getHasRover()) {
					System.out.print("[]");

					// nothing here so print nothing
				} else {
					System.out.print("  ");
				}
			}
			System.out.print(" |\n");
		}
		for (int k = 0; k < edgeSize + 2; k++) {
			System.out.print("--");
		}
		System.out.print("\n");
	}

	private void loadMapTilesOntoGlobalMapLog(MapTile[][] ptrScanMap,
			Coord scanLoc) {

		MapTile tempTile;
		Coord tempCoord;
		Terrain ter;
		Science sci;
		int elev;
		boolean hasR;
		int halfTileSize = ptrScanMap.length / 2;

		// debug - print out
		System.out.println("inside of loadMapTileIntoGlobal()[scanLoc="
				+ scanLoc + "]:" + "[currLoc=" + currentLoc);
		System.out.println("ptrScanMap Size: " + ptrScanMap.length);

		for (int y = 0; y < ptrScanMap.length; y++) {
			for (int x = 0; x < ptrScanMap.length; x++) {

				ter = ptrScanMap[x][y].getTerrain();
				sci = ptrScanMap[x][y].getScience();
				elev = ptrScanMap[x][y].getElevation();
				hasR = ptrScanMap[x][y].getHasRover();

				tempTile = new MapTile(ter, sci, elev, hasR);
				tempCoord = new Coord((scanLoc.xpos - halfTileSize) + x,
						scanLoc.ypos - halfTileSize + y);

				// debug
				System.out.println("(x,y)=(" + x + "," + y + ")|" + "(X,Y)=("
						+ (scanLoc.xpos - halfTileSize + x) + ","
						+ (scanLoc.ypos - halfTileSize + y) + ")\t" + tempCoord
						+ tempTile);
				mapTileLog.put(tempCoord, tempTile);

				System.out.println(tempCoord + " *** " + tempTile);
			}
		}
	}

	private void move(String dir) throws IOException {
		System.out.println("current location in move(): " + currentLoc);
		setCurrentLoc();
		// doScanOriginal();

		switch (dir) {

		case "E":
			if (!checkSand("E")) {
				System.out.println("request move -> E");
				moveEast();
			}
			break;
		case "W":
			if (!checkSand("W")) {
				System.out.println("request move -> W");
				moveWest();
			}
			break;
		case "N":
			if (!checkSand("N")) {
				System.out.println("request move -> N");
				moveNorth();
			}
			break;
		case "S":
			if (!checkSand("S")) {
				System.out.println("request move -> S");
				moveSouth();
			}
			break;
		default:
			break;
		}
	}

	private void moveEast() throws IOException {

		out.println("MOVE E");
		System.out.print(currentLoc + " - E -> ");
		System.out.print(currentLoc + "\n");
	}

	private void moveWest() throws IOException {
		out.println("MOVE W");
		System.out.print(currentLoc + " - W -> ");
		System.out.print(currentLoc + "\n");
	}

	private void moveNorth() throws IOException {
		out.println("MOVE N");
		System.out.print(currentLoc + " - N -> ");
		System.out.print(currentLoc + "\n");

	}

	private void moveSouth() throws IOException {
		out.println("MOVE S");
		System.out.print(currentLoc + " - S -> ");
		System.out.print(currentLoc + "\n");

	}

	public boolean checkSand(String direction) {

		int centerIndex = (scanMap.getEdgeSize() - 1) / 2;
		int x = centerIndex, y = centerIndex, scanRange = 2;

		for (int i = 1; i < scanRange; i++) {
			if (direction == "S")
				x = centerIndex + i;
			else if (direction == "N")
				x = centerIndex - i;
			else if (direction == "E")
				y = centerIndex + i;
			else
				y = centerIndex - i;

			// Checks whether there is sand in the next tile
			if (scanMap.getScanMap()[x][y].getTerrain() == Terrain.SAND)
				return true;
		}

		return false;
	}

	// a check function to prevent IndexOutOfBounds exception
	public boolean withinTheGrid(int i, int j, int arrayLength) {
		return i >= 0 && j >= 0 && i < arrayLength && j < arrayLength;
	}

	// private void SendJsonToServer(JSONObject obj) {
	// HttpClient client = new DefaultHttpClient();
	// HttpConnectionParams.setConnectionTimeout(client.getParams(), 10000);
	// // Timeout Limit
	// HttpResponse response;
	//
	// try {
	// // TODO Update with correct server URL
	// HttpPost post = new HttpPost("OUR SERVER URL");
	// StringEntity se = new StringEntity(obj.toString());
	// se.setContentType(new BasicHeader(HTTP.CONTENT_TYPE,
	// "application/json"));
	// post.setEntity(se);
	// response = client.execute(post);
	//
	// /* Checking response */
	// if (response != null) {
	// InputStream in = response.getEntity().getContent();
	// // Get the data in the entity
	// }
	//
	// } catch (Exception e) {
	// e.printStackTrace();
	// }
	// }

	/**
	 * Runs the client
	 */
	public static void main(String[] args) throws Exception {
		ROVER_12 client = new ROVER_12();
		client.run();
	}
}