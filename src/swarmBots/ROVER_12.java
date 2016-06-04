package swarmBots;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.Socket;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import common.Communication;
import common.Coord;
import common.MapTile;
import common.ScanMap;
import enums.Science;
import enums.Terrain;
import rover_kae.InABeeLine8Dir;

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
	// String SERVER_ADDRESS = "192.168.1.106", line;
	static final int PORT_ADDRESS = 9537;

	// Group 12 variables
	int numLogics = 3;
	static String myJSONStringBackupofMap;
	Coord currentLoc, previousLoc, rovergroupStartPosition = null,
			targetLocation = null;
	Map<Coord, MapTile> mapTileLog = new HashMap<Coord, MapTile>();
	Map<Coord, Boolean> visitedOnAParimeterWalk = new HashMap<Coord, Boolean>();

	HashMap<Coord, Integer> visitCounts = new HashMap<Coord, Integer>();
	public ArrayList<Coord> pathMap = new ArrayList<Coord>();

	Random rd = new Random();
	boolean[] cardinals = new boolean[4];
	boolean isTargetLocReached = false;
	Coord nextTarget;

	// Green Corp variables
	String url = "http://192.168.1.104:3000/api";
	String corp_secret = "gz5YhL70a2";

	public ROVER_12() {
		// constructor
		System.out.println("ROVER_12 rover object constructed");
		rovername = "ROVER_12";
		// SERVER_ADDRESS = "192.168.1.106";
		SERVER_ADDRESS = "localhost";
		// this should be a safe but slow timer value
		sleepTime = 600; // in milliseconds - smaller is faster, but the server
							// will cut connection if it is too small
	}

	public ROVER_12(String serverAddress) {
		// constructor
		System.out.println("ROVER_12 rover object constructed");
		rovername = "ROVER_12";
		SERVER_ADDRESS = serverAddress;
		sleepTime = 600; // in milliseconds - smaller is faster, but the server
							// will cut connection if it is too small
	}

	public ROVER_12(String serverAddress, String url, String corp_secretIn) {
		// constructor
		System.out.println("ROVER_12 rover object constructed");
		rovername = "ROVER_12";
		SERVER_ADDRESS = serverAddress;
		sleepTime = 600;
		this.url = url;
		corp_secret = corp_secretIn;
	}

	void roverMotionLogic(boolean[] cardinals, MapTile[][] scanMapTiles,
			int centerIndex, int currentXPos, int currentYPos)
			throws InterruptedException, IOException {
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
				move("E");

			} else if (scanMapTiles[centerIndex][centerIndex + 1].getScience()
					.equals("C")) {
				move("S");
			} else if (scanMapTiles[centerIndex][centerIndex - 1].getScience()
					.equals("C")) {
				move("N");
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
								move("E");
							} else {
								randomPickMotion(cardinals, centerIndex,
										scanMapTiles);
								cardinals = randomPickMotion(cardinals,
										centerIndex, scanMapTiles);
							}

						} else {
							move("N");
						}
					} else {
						move("S");
					}
				}
				// when no obstacle is in next move to east
				else {
					move("E");
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
							move("W");
						} else {
							cardinals = randomPickMotion(cardinals,
									centerIndex, scanMapTiles);

							// cardinals = moveUsingPastPath(cardinals,
							// currentXPos, currentYPos);
						}

					} else {
						move("N");
					}
				} else {
					move("S");
				}
			}
			// when no obstacle is in next move to west
			else {
				move("W");
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
							move("S");
						} else {
							cardinals = randomPickMotion(cardinals,
									centerIndex, scanMapTiles);
						}
					} else {
						move("E");
					}
				} else {
					move("W");
				}
			}
			// when no obstacle is in next move to south
			else {
				move("S");
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
							move("N");
						} else {
							cardinals = randomPickMotion(cardinals,
									centerIndex, scanMapTiles);
						}
					} else {
						move("E");
					}
				} else {
					move("W");
				}
			}
			// when no obstacle is in next move to north
			else {
				move("N");
			}
		}
	}

	// left hand side wall-follower
	void followLhsWall() throws IOException {

		String[] directions = { "N", "E", "S", "W" };
		switch (getFacingDirection()) {
		case "E":
			// use the default above
			break;
		case "S":
			directions[0] = "E";
			directions[1] = "S";
			directions[2] = "W";
			directions[3] = "N";
			break;
		case "W":
			directions[0] = "S";
			directions[1] = "W";
			directions[2] = "N";
			directions[3] = "E";
			break;
		case "N":
			directions[0] = "W";
			directions[1] = "N";
			directions[2] = "E";
			directions[3] = "S";
			break;
		default:
			break;
		}

		for (int i = 0; i < directions.length; i++) {
			if (move(directions[i])) {
				return;
			}
		}
	}

	// right hand side wall-follower
	void followRhsWall() throws IOException {

		String[] directions = { "S", "E", "N", "W" };
		switch (getFacingDirection()) {
		case "E":
			// use the default above
			break;
		case "S":
			directions[0] = "W";
			directions[1] = "S";
			directions[2] = "E";
			directions[3] = "N";
			break;
		case "W":
			directions[0] = "N";
			directions[1] = "W";
			directions[2] = "S";
			directions[3] = "E";
			break;
		case "N":
			directions[0] = "E";
			directions[1] = "N";
			directions[2] = "W";
			directions[3] = "S";
			break;
		default:
			break;
		}

		for (int i = 0; i < directions.length; i++) {
			if (move(directions[i])) {
				return;
			}
		}
	}

	void roverMotionLogicShort() throws IOException {
		// same basic structure of logic as the original roverMotionLogic
		// removed the parts that are not necessary at the moment

		String[] directions = { "N", "E", "W", "N", "S" };
		switch (getFacingDirection()) {
		case "E":
			directions[0] = "E";
			directions[1] = "N";
			directions[2] = "S";
			directions[3] = "E";
			directions[4] = "W";
			break;
		case "S":
			directions[0] = "S";
			directions[1] = "E";
			directions[2] = "W";
			directions[3] = "S";
			directions[4] = "N";
			break;
		case "W":
			directions[0] = "W";
			directions[1] = "N";
			directions[2] = "S";
			directions[3] = "W";
			directions[4] = "E";
			break;
		case "N":
			directions[0] = "N";
			directions[1] = "E";
			directions[2] = "W";
			directions[3] = "N";
			directions[4] = "S";
			break;
		default:
			break;
		}

		for (int i = 0; i < directions.length; i++) {
			if (move(directions[i])) {
				return;
			}
		}
	}

	boolean isAllDirOpen(MapTile[][] scanMapTiles, int centerIndex) {
		System.out.println("is east blocked? "
				+ isTowardsThisDirectionIsObsatacle(scanMapTiles, centerIndex,
						"E"));
		System.out.println("is south blocked? "
				+ isTowardsThisDirectionIsObsatacle(scanMapTiles, centerIndex,
						"S"));
		System.out.println("is west blocked? "
				+ isTowardsThisDirectionIsObsatacle(scanMapTiles, centerIndex,
						"W"));
		System.out.println("is north blocked? "
				+ isTowardsThisDirectionIsObsatacle(scanMapTiles, centerIndex,
						"N"));

		return isTowardsThisDirectionIsObsatacle(scanMapTiles, centerIndex, "E")
				&& isTowardsThisDirectionIsObsatacle(scanMapTiles, centerIndex,
						"S")
				&& isTowardsThisDirectionIsObsatacle(scanMapTiles, centerIndex,
						"W")
				&& isTowardsThisDirectionIsObsatacle(scanMapTiles, centerIndex,
						"N");
	}

	private boolean isSurroundedBy3Obstacles(MapTile[][] scanMapTiles,
			int centerIndex) throws IOException {
		int counter = 0;

		if (isTowardsEastIsObsatacle(scanMapTiles, centerIndex)) {
			counter++;
		}
		if (isTowardsNorthIsObsatacle(scanMapTiles, centerIndex)) {
			counter++;
		}
		if (isTowardsSouthIsObsatacle(scanMapTiles, centerIndex)) {
			counter++;
		}
		if (isTowardsWestIsObsatacle(scanMapTiles, centerIndex)) {
			counter++;
		}
		return (counter >= 3);
	}

	private void doTheWallFollowingHack(int numSteps) throws IOException,
			Exception {

		for (int i = 0; i < numSteps; i++) {

			if (isAWallAround()) {
				followRhsWall();
			} else {
				move(getFacingDirection());
			}
			setCurrentLoc();

			visitedOnAParimeterWalk.put(new Coord(currentLoc.xpos,
					currentLoc.ypos), true);

			if (visitCounts.get(currentLoc) != null) {
				visitCounts.put(currentLoc, visitCounts.get(currentLoc) + 1);
			} else {
				visitCounts.put(currentLoc, 1);
			}

		}

	}

	private void doPerimeterWalk(boolean hasHitTheNorthWall) throws Exception,
			IOException {
		// go along the perimeter
		if (hasHitTheNorthWall) {
			if (isAWallAround()) {
				followRhsWall();
			} else {
				move(getFacingDirection());
			}
		}
	}

	// set to do right-hand-side wall following
	private void doTheWallFollowingWalk() throws Exception, IOException {

		if (isAWallAround()) {
			followRhsWall();
		} else {
			move(getFacingDirection());
		}
	}

	private void outOfMaze() throws Exception, IOException {

		while (true) {

			if (isAWallAround()) {
				followRhsWall();
			} else {
				move(getFacingDirection());
				// sinusoidalRandom(waveLength, waveHeight);
			}
			setCurrentLoc();

			Thread.sleep(sleepTime);
		}
	}

	// a check function to prevent IndexOutOfBounds exception
	public boolean withinTheGrid(int x, int y, int lengthX, int lengthY) {
		return x >= 0 && y >= 0 && x < lengthX && y < lengthY;
	}

	public void sinusoidal(int waveLength, int waveHeight)
			throws InterruptedException, IOException {

		switch (getFacingDirection()) {
		case "E":
			sinusoidalRight(waveLength, waveHeight);
			break;
		case "S":
			sinusoidalDown(waveLength, waveHeight);
			break;
		case "W":
			sinusoidalLeft(waveLength, waveHeight);
			break;
		case "N":
			sinusoidalUp(waveLength, waveHeight);
			break;
		default:
			break;
		}
	}

	/*
	 * after hitting the wall, the facing direction needs to be adjusted with
	 * this function for the wall follwer (for rhs wall follwerS only)
	 */
	public void calibrateFacingDir() {

		String currFacingDir = getFacingDirection();

		switch (getFacingDirection()) {
		case "N":
			// face west
			cardinals[0] = false; // S
			cardinals[1] = false; // E
			cardinals[2] = false; // N
			cardinals[3] = true; // W
			break;
		case "W":
			// face south
			cardinals[0] = true; // S
			cardinals[1] = false; // E
			cardinals[2] = false; // N
			cardinals[3] = false; // W
			break;
		case "S":
			// face east
			cardinals[0] = false; // S
			cardinals[1] = true; // E
			cardinals[2] = false; // N
			cardinals[3] = false; // W
			break;
		case "E":
			// face north
			cardinals[0] = false; // S
			cardinals[1] = true; // E
			cardinals[2] = false; // N
			cardinals[3] = false; // W
			break;

		default:
			break;
		}
		if (currFacingDir.equals("N")) {
			// face west
		} else if (currFacingDir.equals("N")) {
			// face west
		}
		if (currFacingDir.equals("N")) {
			// face west
		}
		if (currFacingDir.equals("N")) {
			// face west
		}
	}

	public void sinusoidalRandom(int waveLength, int waveHeight)
			throws InterruptedException, IOException {

		int rand = randomNum(0, 3);
		switch (rand) {
		case 0:
			sinusoidalRight(waveLength, waveHeight);
			break;
		case 1:
			sinusoidalDown(waveLength, waveHeight);
			break;
		case 2:
			sinusoidalLeft(waveLength, waveHeight);
			break;
		case 3:
			sinusoidalUp(waveLength, waveHeight);
			break;
		default:
			break;
		}
	}

	private Coord getUndiscoveredArea(int searchSize) {

		int threshold = (searchSize * searchSize) / 2, halfSize = searchSize / 2;
		Coord tempPos;
		if (mapTileLog.get(targetLocation) == null) {
			return targetLocation;
		}
		int numReturned = 0;
		for (int j = targetLocation.ypos - halfSize; j > halfSize; j -= searchSize) {
			for (int i = targetLocation.xpos - halfSize; i > halfSize; i -= searchSize) {
				tempPos = new Coord(i, j);
				if ((numReturned = countUndiscoveredTiles(tempPos, searchSize)) >= threshold) {
					return tempPos;
				}
			}
		}

		return new Coord(targetLocation.xpos, 0);
	}

	private int countUndiscoveredTiles(Coord loc, int searchSize) {

		int halfSize = searchSize / 2, counter = 0;

		for (int j = loc.ypos - halfSize; j < loc.ypos + halfSize; j++) {
			for (int i = loc.xpos - halfSize; i < loc.xpos + halfSize; i++) {
				if (withinTheGrid(i, j, targetLocation.xpos,
						targetLocation.ypos)
						&& mapTileLog.get(new Coord(i, j)) == null) {
					counter++;
				}
			}
		}
		return counter;
	}

	private void astar() throws Exception {

		for (int i = 0; i < 3; i++) {

			loadScanMapFromSwarmServer();
			// debugPrintMapTileArray(mapTileLog);
			int idx = 0;
			Coord goal = setAstarGoal();
			InABeeLine8Dir b = new InABeeLine8Dir();
			// System.out.println("this goal: " + goal);

			String[] thePath = b.getShortestPath(currentLoc, goal, mapTileLog);

			// if(thePath[0].equals("no solution")){
			// goal = new Coord(currentLoc.xpos,currentLoc.ypos+5);
			// thePath = b.getShortestPath(currentLoc, goal, mapTileLog);
			// }
			//
			// if(thePath[0].equals("no solution")){
			// goal = new Coord(currentLoc.xpos-5,currentLoc.ypos+5);
			// thePath = b.getShortestPath(currentLoc, goal, mapTileLog);
			// }

			// astarGo = true;

			boolean hasMoved = false;

			System.out.println("thePath length: " + thePath.length);
			for (int j = 0; j < thePath.length; j++) {

				loadScanMapFromSwarmServer();
				hasMoved = move(thePath[idx]);
				if (hasMoved) {
					idx++;
				} else {
					loadScanMapFromSwarmServer();
				}
				Thread.sleep(sleepTime);
			}
		}
	}

	// if wall is on rv's north, west, north east, north west (lhs wall
	// follower)
	public boolean isWallOnNorthWest() throws Exception {
		int currX = currentLoc.xpos, currY = currentLoc.ypos;
		// west
		if (isObsatacle(new Coord(currX - 1, currY))) {
			// System.out.println("wall detected on my e");
			// Thread.sleep(3000);
			return true;
		}
		// north
		if (isObsatacle(new Coord(currX, currY - 1))) {
			// System.out.println("wall detected on my s");
			// Thread.sleep(3000);
			return true;
		}
		// north east
		if (isObsatacle(new Coord(currX + 1, currY - 1))) {
			return true;
		}
		// north west
		if (isObsatacle(new Coord(currX - 1, currY - 1))) {
			return true;
		}
		return false;
	}

	// if wall is on rv's south, south east, south west, or east (rhs wall
	// follower)
	public boolean isWallOnSouthOrEast() throws Exception {
		int currX = currentLoc.xpos, currY = currentLoc.ypos;
		// east
		if (isObsatacle(new Coord(currX + 1, currY))) {
			// System.out.println("wall detected on my e");
			// Thread.sleep(3000);
			return true;
		}
		// south
		if (isObsatacle(new Coord(currX, currY + 1))) {
			// System.out.println("wall detected on my s");
			// Thread.sleep(3000);
			return true;
		}
		// south east
		if (isObsatacle(new Coord(currX + 1, currY + 1))) {
			return true;
		}
		// south west
		if (isObsatacle(new Coord(currX - 1, currY + 1))) {
			return true;
		}

		return false;
	}

	// debug - remove debug p out
	public boolean isAWallAround() throws Exception {
		int currX = currentLoc.xpos, currY = currentLoc.ypos;
		// 1 east
		if (isAWall(new Coord(currX + 1, currY))) {
			return true;
		}
		// 2 southeast
		if (isAWall(new Coord(currX + 1, currY + 1))) {
			return true;
		}
		// 3 south
		if (isAWall(new Coord(currX, currY + 1))) {
			return true;
		}
		// 4 southwest
		if (isAWall(new Coord(currX - 1, currY + 1))) {
			return true;
		}
		// 5 west
		if (isAWall(new Coord(currX - 1, currY))) {
			return true;
		}
		// 6 northwest
		if (isAWall(new Coord(currX - 1, currY - 1))) {
			return true;
		}
		// 7 north
		if (isAWall(new Coord(currX, currY - 1))) {
			return true;
		}
		// 8 northeast
		if (isAWall(new Coord(currX + 1, currY - 1))) {
			return true;
		}
		return false;
	}

	public boolean isAWallInThe4Adj() throws Exception {
		int currX = currentLoc.xpos, currY = currentLoc.ypos;
		// east
		if (isObsatacle(new Coord(currX + 1, currY))) {
			return true;
		}

		// south
		if (isAWall(new Coord(currX, currY + 1))) {
			return true;
		}

		// west
		if (isAWall(new Coord(currX - 1, currY))) {
			return true;
		}

		// north
		if (isAWall(new Coord(currX, currY - 1))) {
			return true;
		}

		return false;
	}

	void test() throws IOException, InterruptedException {
		new ArrayList<String>();
		Socket socket = null;
		InABeeLine8Dir b = new InABeeLine8Dir();
		boolean astar = true;
		System.out.println(requestTimeRemaining(socket));

		try {

			// ***** connect to server ******
			socket = connectToSwarmServer();

			getEquipment();

			// ***** initialize critical locations ******
			rovergroupStartPosition = requestStartLoc(socket);
			targetLocation = requestTargetLoc(socket);
			nextTarget = targetLocation.clone();

			currentLoc.clone();
			cardinals[1] = true;
			/**
			 * #### Rover controller process loop ####
			 */
			// // ------ itr 1 --------
			loadScanMapFromSwarmServer();
			int centerIndex = (scanMap.getEdgeSize() - 1) / 2;
			// debugPrintMapTileArray(mapTileLog);
			boolean astarGo = false;
			String[] thePath = { "end" };
			int idx = 0;

			loadScanMapFromSwarmServer();
			// debugPrintMapTileArray(mapTileLog);
			idx = 0;
			Coord goal = setAstarGoal();
			System.out.println("this goal: " + goal);

			thePath = b.getShortestPath(currentLoc, goal, mapTileLog);

			if (thePath[0].equals("no solution")) {
				goal = new Coord(currentLoc.xpos, currentLoc.ypos + 5);
				thePath = b.getShortestPath(currentLoc, goal, mapTileLog);
			}

			if (thePath[0].equals("no solution")) {
				goal = new Coord(currentLoc.xpos - 5, currentLoc.ypos + 5);
				thePath = b.getShortestPath(currentLoc, goal, mapTileLog);
			}

			astarGo = true;
			boolean hasMoved = false;
			while (!thePath[idx].equals("end")) {

				loadScanMapFromSwarmServer();
				hasMoved = move(thePath[idx]);
				if (hasMoved) {
					idx++;
				} else {
					loadScanMapFromSwarmServer();
				}
				Thread.sleep(sleepTime + 300);
			}

			System.out
					.println("ROVER_12 ------------ bottom process control --------------");

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
	}// END of test()

	private Coord setAstarGoal() {

		switch (getTargetDirStr(targetLocation)) {
		case "E":
			return new Coord(currentLoc.xpos + 5, currentLoc.ypos);

		case "S":
			return new Coord(currentLoc.xpos, currentLoc.ypos + 5);

		case "W":
			return new Coord(currentLoc.xpos - 5, currentLoc.ypos);

		case "N":
			return new Coord(currentLoc.xpos, currentLoc.ypos - 5);

		case "NE":
			return new Coord(currentLoc.xpos + 5, currentLoc.ypos - 5);

		case "SE":
			return new Coord(currentLoc.xpos + 5, currentLoc.ypos + 5);

		case "NW":
			return new Coord(currentLoc.xpos - 5, currentLoc.ypos - 5);

		case "SW":
			return new Coord(currentLoc.xpos - 5, currentLoc.ypos + 5);

		default:
			return targetLocation;
		}
	}

	private String getTargetDirStr(Coord target) {

		int dx = target.xpos - currentLoc.xpos;
		int dy = currentLoc.ypos - currentLoc.ypos;

		// n
		if (dx == 0 && dy < 0) {
			return "N";
		}
		// s
		if (dx == 0 && dy > 0) {
			return "S";
		}
		// e
		if (dx > 0 && dy == 0) {
			return "E";
		}
		// w
		if (dx < 0 && dy == 0) {
			return "W";
		}
		// ne
		if (dx > 0 && dy < 0) {
			return "NE";
		}
		// nw
		if (dx < 0 && dy < 0) {
			return "NW";
		}
		// se
		if (dx > 0 && dy > 0) {
			return "SE";
		}
		// sw
		if (dx < 0 && dy > 0) {
			return "SW";
		}
		return "E";
	}

	Socket connectToSwarmServer() throws UnknownHostException, IOException {
		Socket socket;
		socket = new Socket(SERVER_ADDRESS, PORT_ADDRESS);

		in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		out = new PrintWriter(socket.getOutputStream(), true);

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
		return socket;
	}

	boolean isTowardsWestIsObsatacle(MapTile[][] scanMapTiles, int centerIndex) {
		if (scanMapTiles[centerIndex - 1][centerIndex].getHasRover()
				|| scanMapTiles[centerIndex - 1][centerIndex].getTerrain() == Terrain.ROCK
				|| scanMapTiles[centerIndex - 1][centerIndex].getTerrain() == Terrain.NONE
				|| scanMapTiles[centerIndex - 1][centerIndex].getTerrain() == Terrain.FLUID
				|| scanMapTiles[centerIndex - 1][centerIndex].getTerrain() == Terrain.SAND
				|| scanMapTiles[centerIndex + 1][centerIndex].getHasRover()) {
			return true;
		}
		return false;
	}

	public boolean isObsatacle(Coord focus) throws IOException {

		MapTile tile = mapTileLog.get(focus);
		if (tile == null) {
			loadScanMapFromSwarmServer();
			tile = mapTileLog.get(focus);
		}
		if (tile.getHasRover() || tile.getTerrain() == Terrain.ROCK
				|| tile.getTerrain() == Terrain.NONE
				|| tile.getTerrain() == Terrain.FLUID
				|| tile.getTerrain() == Terrain.SAND) {
			return true;
		} else {
			return false;
		}
	}

	public boolean isAWall(Coord focus) throws IOException {

		MapTile tile = mapTileLog.get(focus);
		if (tile == null) {
			loadScanMapFromSwarmServer();
			tile = mapTileLog.get(focus);
		}
		if (focus.xpos == 0 || focus.ypos == 0) {
			return true;
		}
		if (tile.getTerrain() == Terrain.ROCK
				|| tile.getTerrain() == Terrain.NONE
				|| tile.getTerrain() == Terrain.SAND) {
			return true;
		} else {
			return false;
		}
	}

	boolean isTowardsEastIsObsatacle(MapTile[][] scanMapTiles, int centerIndex) {
		if (scanMapTiles[centerIndex + 1][centerIndex].getTerrain() == Terrain.ROCK
				|| scanMapTiles[centerIndex + 1][centerIndex].getTerrain() == Terrain.NONE
				|| scanMapTiles[centerIndex + 1][centerIndex].getTerrain() == Terrain.SAND) {
			return true;
		} else {
			return false;
		}
	}

	boolean isTowardsNorthIsObsatacle(MapTile[][] scanMapTiles, int centerIndex) {
		if (scanMapTiles[centerIndex][centerIndex - 1].getTerrain() == Terrain.ROCK
				|| scanMapTiles[centerIndex][centerIndex - 1].getTerrain() == Terrain.NONE
				|| scanMapTiles[centerIndex][centerIndex - 1].getTerrain() == Terrain.SAND) {
			return true;
		}
		return false;
	}

	boolean isTowardsSouthIsObsatacle(MapTile[][] scanMapTiles, int centerIndex) {
		if (scanMapTiles[centerIndex][centerIndex + 1].getTerrain() == Terrain.ROCK
				|| scanMapTiles[centerIndex][centerIndex + 1].getTerrain() == Terrain.NONE
				|| scanMapTiles[centerIndex][centerIndex + 1].getTerrain() == Terrain.SAND) {
			return true;
		}
		return false;
	}

	boolean isTowardsThisDirectionIsObsatacle(MapTile[][] scanMapTiles,
			int centerIndex, String dir) {
		switch (dir) {
		case "E":
			return isTowardsEastIsObsatacle(scanMapTiles, centerIndex);

		case "S":
			return isTowardsSouthIsObsatacle(scanMapTiles, centerIndex);

		case "W":
			return isTowardsWestIsObsatacle(scanMapTiles, centerIndex);

		case "N":
			return isTowardsNorthIsObsatacle(scanMapTiles, centerIndex);
		default:
			return true;
		}
	}

	void setCurrentLoc() throws IOException {

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

	Coord requestCurrentLoc() throws IOException {

		String line;
		Coord clone = new Coord(-1, -1);
		// **** Request Rover Location from SwarmServer ****
		out.println("LOC");
		line = in.readLine();
		if (line == null) {
			System.out.println(rovername + " check connection to server");
			line = "";
		}
		if (line.startsWith("LOC")) {
			clone = extractLocationFromString(line);
		}
		return clone;
	}

	// ####################### Support Methods #############################

	void clearReadLineBuffer() throws IOException {
		while (in.ready()) {
			// System.out.println("ROVER_12 clearing readLine()");
			in.readLine();
		}
	}

	// method to retrieve a list of the rover's EQUIPMENT from the server
	ArrayList<String> getEquipment() throws IOException {
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
	// sets current location each time this function is called
	public void loadScanMapFromSwarmServer() throws IOException {

		// setCurrentLoc();
		// Coord scanLoc = new Coord(currentLoc.xpos, currentLoc.ypos);
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

		scanMap = gson.fromJson(jsonScanMapString, ScanMap.class);
		loadMapTilesOntoGlobalMapLog(scanMap.getScanMap(), currentLoc);
	}

	Coord requestStartLoc(Socket soc) throws IOException {

		// **** Request Rover Location from SwarmServer ****
		out.println("START_LOC");
		line = in.readLine();
		if (line == null) {
			System.out.println(rovername + " check connection to server");
			line = "";
		}
		if (line.startsWith("LOC")) {
			// loc = line.substring(4);
			currentLoc = extractLocationFromString(line);

		}
		// System.out.println(rovername + " currentLoc at start: " +
		// currentLoc);

		// out.println("START_LOC " + currentLoc.xpos + " " + currentLoc.ypos);
		// line = in.readLine();

		if (line == null || line == "") {
			System.out.println("ROVER_12 check connection to server");
			line = "";
		}

		if (line.startsWith("START")) {
			rovergroupStartPosition = extractStartLOC(line);
			// debug
			System.out.println(rovergroupStartPosition);
		}
		return rovergroupStartPosition;
	}

	Coord requestTargetLoc(Socket soc) throws IOException {

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

	int requestTimeRemaining(Socket soc) throws IOException {

		// **** Request Remaining Time from SwarmServer ****
		out.println("TIMER");
		line = in.readLine();
		int timeRemaining = -2;
		if (line == null) {
			System.out.println(rovername + " check connection to server");
			line = "";
		}
		if (line.startsWith("TIMER")) {
			timeRemaining = extractTimeRemaining(line);

		}
		return timeRemaining;
	}

	static Coord extractCurrLOC(String sStr) {
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

	// Our hats off to brilliant ROVER_11 / Group 11! Many thanks!
	void updateFromGreenCorpGlobalMap(JSONArray data) {

		for (Object o : data) {

			JSONObject jsonObj = (JSONObject) o;
			int x = (int) (long) jsonObj.get("x");
			int y = (int) (long) jsonObj.get("y");
			Coord coord = new Coord(x, y);

			if (!mapTileLog.containsKey(coord)) {
				MapTile tile = supportTools.CommunicationUtil
						.convertToMapTile(jsonObj);

				mapTileLog.put(coord, tile);
			}
		}
	}

	static Coord extractStartLOC(String sStr) {

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

	static Coord extractTargetLOC(String sStr) {
		sStr = sStr.substring(11);
		if (sStr.lastIndexOf(" ") != -1) {
			String xStr = sStr.substring(0, sStr.lastIndexOf(" "));
			// System.out.println("extracted xStr " + xStr);

			String yStr = sStr.substring(sStr.lastIndexOf(" ") + 1);
			// System.out.println("extracted yStr " + yStr);
			return new Coord(Integer.parseInt(xStr), Integer.parseInt(yStr));
		}
		return null;
	}

	static int extractTimeRemaining(String sStr) {
		sStr = sStr.substring(6);
		if (sStr.lastIndexOf(" ") != -1) {
			String timeStr = sStr.substring(0, sStr.lastIndexOf(" "));
			return Integer.parseInt(timeStr);
		}
		return -1;
	}

	// this takes the server response string, parses out the x and x values and
	// returns a Coord object
	static Coord extractLocationFromString(String sStr) {
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

	public void debugPrintMapTileArrayText(MapTile[][] globalMapCopy,
			int mapSize) {
		MapTile tile;

		for (int y = 0; y < mapSize; y++) {
			for (int x = 0; x < mapSize; x++) {
				tile = globalMapCopy[x][y];
				System.out.print("x,y=" + x + "," + y + "\t" + tile + "\t/t");
			}
		}
	}

	public void debugPrintMapTileArray(Map<Coord, MapTile> globalMapCopy) {

		// FIXME
		int edgeSize = 20;
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

	public void debugPrintMapTileArrayWithCurrPos(
			Map<Coord, MapTile> globalMapCopy, Coord loc) {

		// FIXME
		int edgeSizeX = 100, edgeSizeY = 60;

		System.out.println("edge size: " + edgeSizeY);
		for (int k = 0; k < edgeSizeY + 2; k++) {
			System.out.print("--");
		}

		System.out.print("\n");

		for (int j = 0; j < edgeSizeY; j++) {

			// System.out.print("j=" + j + "\t");

			System.out.print("| ");
			for (int i = 0; i < edgeSizeX; i++) {
				if (mapTileLog.get(new Coord(i, j)) == null) {
					System.out.print("nn");
				}// if this tile is the focus
				else if (new Coord(i, j).equals(loc)) {
					System.out.print("oo");
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
		for (int k = 0; k < edgeSizeY + 2; k++) {
			System.out.print("--");
		}
		System.out.print("\n");
	}

	void loadMapTilesOntoGlobalMapLog(MapTile[][] ptrScanMap, Coord scanLoc) {

		MapTile tempTile;
		Coord tempCoord;
		Terrain ter;
		Science sci;
		int elev;
		boolean hasR;
		int halfScanMapSize = ptrScanMap.length / 2;

		for (int y = 0; y < ptrScanMap.length; y++) {
			for (int x = 0; x < ptrScanMap.length; x++) {

				tempCoord = new Coord((scanLoc.xpos - halfScanMapSize) + x,
						scanLoc.ypos - halfScanMapSize + y);

				if (!mapTileLog.containsKey(tempCoord)) {
					ter = ptrScanMap[x][y].getTerrain();
					sci = ptrScanMap[x][y].getScience();
					elev = ptrScanMap[x][y].getElevation();
					hasR = ptrScanMap[x][y].getHasRover();

					// debug
					// if (sci == Science.CRYSTAL) {
					// System.out.println(sci + "\t" + scanLoc);
					//
					// }

					tempTile = new MapTile(ter, sci, elev, hasR);
					mapTileLog.put(tempCoord, tempTile);
				}
			}
		}
	}

	// HTTP POST request
	public void sendPost(JSONObject jsonObj) throws Exception {

		String url = "http://23.251.155.186:3000/api";
		URL obj = new URL(url);
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();
		String USER_AGENT = "ROVER 12";
		// add reuqest header
		con.setRequestMethod("POST");
		con.setRequestProperty("User-Agent", USER_AGENT);
		con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
		con.setDoOutput(true);

		// Send post request
		DataOutputStream wr = new DataOutputStream(con.getOutputStream());
		wr.writeBytes(jsonObj.toString());
		wr.flush();
		wr.close();

		int responseCode = con.getResponseCode();
		// System.out.println("\nSending 'POST' request to URL : " + url);
		// System.out.println("Post parameters : " + jsonObj.toString());
		// Thread.sleep(5000);
		// System.out.println("Response Code : " + responseCode);

		BufferedReader in = new BufferedReader(new InputStreamReader(
				con.getInputStream()));
		String inputLine;
		StringBuffer response = new StringBuffer();

		while ((inputLine = in.readLine()) != null) {
			response.append(inputLine);
		}
		in.close();

		// print result
		System.out.println(response.toString());

	}

	boolean move(String dir) throws IOException {

		int currX = currentLoc.xpos, currY = currentLoc.ypos;
		Coord prevLoc = currentLoc.clone();

		switch (dir) {
		case "E":
			if (!isObsatacle(new Coord(currX + 1, currY))) {
				moveEast();
				setCurrentLoc();
				if (prevLoc.equals(currentLoc)) {
					return false;
				} else {
					return true;
				}
			}
			break;
		case "W":
			if (!isObsatacle(new Coord(currX - 1, currY))) {
				moveWest();
				setCurrentLoc();
				if (prevLoc.equals(currentLoc)) {
					return false;
				} else {
					return true;
				}
			}
			break;
		case "N":
			if (!isObsatacle(new Coord(currX, currY - 1))) {
				moveNorth();
				setCurrentLoc();
				if (prevLoc.equals(currentLoc)) {
					return false;
				} else {
					return true;
				}
			}
			break;
		case "S":
			if (!isObsatacle(new Coord(currX, currY + 1))) {
				moveSouth();
				setCurrentLoc();
				if (prevLoc.equals(currentLoc)) {
					return false;
				} else {
					return true;
				}
			}
			break;
		default:
			return false;
		}
		return false;
	}

	void moveWest() {
		out.println("MOVE W");
		// System.out.println("ROVER_12 request move W");
		cardinals[0] = false; // S
		cardinals[1] = false; // E
		cardinals[2] = false; // N
		cardinals[3] = true; // W
	}

	void moveNorth() {
		out.println("MOVE N");
		// System.out.println("ROVER_12 request move N");
		cardinals[0] = false; // S
		cardinals[1] = false; // E
		cardinals[2] = true; // N
		cardinals[3] = false; // W
	}

	void moveSouth() {
		out.println("MOVE S");
		// System.out.println("ROVER_12 request move S");
		cardinals[0] = true; // S
		cardinals[1] = false; // E
		cardinals[2] = false; // N
		cardinals[3] = false; // W
	}

	void moveEast() {
		out.println("MOVE E");
		// System.out.println("ROVER_12 request move E");
		cardinals[0] = false; // S
		cardinals[1] = true; // E
		cardinals[2] = false; // N
		cardinals[3] = false; // W
	}

	// a check function to prevent IndexOutOfBounds exception
	public boolean isWithinTheGrid(int i, int j, int arrayLength) {
		return i >= 0 && j >= 0 && i < arrayLength && j < arrayLength;
	}

	void sendJSONToServer(JSONObject obj, String URL) {
		try {
			HttpClient client = HttpClientBuilder.create().build();
			HttpPost post = new HttpPost(URL);

			StringEntity se = new StringEntity(obj.toString());
			post.setHeader("content-type", "application/json");
			post.setEntity(se);

			HttpResponse response = client.execute(post);

			// Check response
			System.out.println(obj.toString());

			System.out.println("Response Code : "
					+ response.getStatusLine().getStatusCode());

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public int randomNum(int min, int max) {
		return rd.nextInt(max + 1) + min;
	}

	// retreives the current direction the rover is facing in string
	public String getFacingDirection() {
		if (cardinals[0] == true) {
			return "S";
		}
		if (cardinals[1] == true) {
			return "E";
		}
		if (cardinals[2] == true) {
			return "N";
		}
		return "W";
	}

	void shuffuleArray(String[] directions) {
		// Thanks, Fisher-Yates shuffle
		int idx;
		String temp;

		for (int i = directions.length - 1; i > -1; i--) {
			idx = randomNum(0, i);
			temp = directions[idx];
			directions[idx] = directions[i];
			directions[i] = temp;
		}
	}

	// take a random step (just one step) to break the pattern
	void randomStep() throws InterruptedException, IOException {

		if (mapTileLog.get(currentLoc) == null) {
			loadScanMapFromSwarmServer();
		}

		String currDir = getFacingDirection();
		String[] directions = { "N", "E", "S", "W" };
		shuffuleArray(directions);

		for (String thisDir : directions) {
			if (!currDir.equals(thisDir)) {
				System.out.println("move " + thisDir);
				move(thisDir);
				return;
			}
		}
	}

	// take a random step (just one step) to break the pattern
	void randomlyStepOut() throws Exception {

		int idx = 0;
		Coord goal = null;

		// pick a random nearby cell
		do {
			goal = new Coord(
					randomNum(currentLoc.xpos - 5, currentLoc.xpos + 5),
					randomNum(currentLoc.ypos - 5, currentLoc.ypos + 5));
		} while (mapTileLog.get(goal) == null || isObsatacle(goal));

		InABeeLine8Dir b = new InABeeLine8Dir();
		String[] thePath = b.getShortestPath(currentLoc, goal, mapTileLog);

		boolean hasMoved = false;

		// System.out.println("thePath length: " + thePath.length);
		for (int j = 0; j < thePath.length; j++) {

			loadScanMapFromSwarmServer();
			hasMoved = move(thePath[idx]);
			if (hasMoved) {
				idx++;
			} else {
				loadScanMapFromSwarmServer();
			}
			Thread.sleep(sleepTime + 300);
		}
	}

	public boolean visited(Coord pos) {
		if (mapTileLog.containsKey(pos)) {
			return true;
		}
		return false;
	}

	boolean[] randomPickMotion(boolean[] cardinals, int centerIndex,
			MapTile[][] scanMapTiles) {

		try {
			int randomNumber = randomNum(0, 3);
			if (cardinals[randomNumber] == true) {// this part causes stack
													// overflow if surrounded by
													// obstaccles in three
													// directions
				randomPickMotion(cardinals, centerIndex, scanMapTiles);
			} else {
				switch (randomNumber) {

				// going south
				case 0: {
					if (isTowardsSouthIsObsatacle(scanMapTiles, centerIndex)) {
						randomPickMotion(cardinals, centerIndex, scanMapTiles);
					} else {

						move("S");

					}
					break;
				}

				// going east
				case 1: {
					if (isTowardsEastIsObsatacle(scanMapTiles, centerIndex)) {
						randomPickMotion(cardinals, centerIndex, scanMapTiles);
					} else {
						move("E");
					}
					break;
				}

				// going north
				case 2: {

					if (isTowardsNorthIsObsatacle(scanMapTiles, centerIndex)) {
						randomPickMotion(cardinals, centerIndex, scanMapTiles);
					} else {
						move("N");
					}
					break;
				}

				// going west

				case 3: {
					if (isTowardsSouthIsObsatacle(scanMapTiles, centerIndex)) {
						randomPickMotion(cardinals, centerIndex, scanMapTiles);
					} else {
						move("S");
					}
					break;
				}
				}

			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return cardinals;
	}

	boolean isAlreadyTraveledPathTowardsWest(int currentXPos, int currentYPos) {
		int nextXPosition = currentXPos - 1;
		int nextYPosition = currentYPos;
		for (Coord coord : pathMap) {
			if ((coord.xpos == nextXPosition) && (coord.ypos == nextYPosition)) {
				return true;
			}
		}
		return false;
	}

	boolean isAlreadyTraveledPathTowardsNorth(int currentXPos, int currentYPos) {
		int nextXPosition = currentXPos;
		int nextYPosition = currentYPos - 1;
		for (Coord coord : pathMap) {
			if ((coord.xpos == nextXPosition) && (coord.ypos == nextYPosition)) {
				return true;
			}
		}
		return false;
	}

	boolean isAlreadyTraveledPathTowardsSouth(int currentXPos, int currentYPos) {
		int nextXPosition = currentXPos;
		int nextYPosition = currentYPos + 1;
		for (Coord coord : pathMap) {
			if ((coord.xpos == nextXPosition) && (coord.ypos == nextYPosition)) {
				return true;
			}
		}
		return false;
	}

	boolean isAlreadyTraveledPathTowardsEast(int currentXPos, int currentYPos) {
		int nextXPosition = currentXPos + 1;
		int nextYPosition = currentYPos;
		for (Coord coord : pathMap) {
			if ((coord.xpos == nextXPosition) && (coord.ypos == nextYPosition)) {
				return true;
			}
		}
		return false;
	}

	// wave like motion path, the pattern will be adjusted to the present
	// contour (Right -> Left)
	private void sinusoidalLeft(int waveLength, int waveHeight)
			throws InterruptedException, IOException {
		int steps, sleeptime = 900;

		steps = waveLength;
		String currentDir;
		String[] dir = { "W", "S", "W", "N" };

		previousLoc = currentLoc;

		for (int i = 0; i < dir.length; i++) {

			currentDir = dir[i];
			if (currentDir.equals("W")) {
				steps = waveLength;
			} else {
				steps = waveHeight;
			}

			for (int j = 0; j < steps; j++) {

				setCurrentLoc();
				move(currentDir);

				Thread.sleep(sleeptime);
			}
		}
	}

	// wave like motion path, the pattern will be adjusted to the present
	// contour (Left -> Right)
	private void sinusoidalRight(int waveLength, int waveHeight)
			throws InterruptedException, IOException {
		int steps, sleeptime = 900;

		steps = waveLength;
		String goDir;
		String[] dir = { "E", "S", "E", "N" };

		previousLoc = currentLoc;

		for (int i = 0; i < dir.length; i++) {

			goDir = dir[i];

			if (goDir.equals("E")) {
				steps = waveLength;
			} else {
				steps = waveHeight;
			}

			for (int j = 0; j < steps; j++) {

				setCurrentLoc();
				move(goDir);

				Thread.sleep(sleeptime);
			}
		}
	}

	// wave like motion path, the pattern will be adjusted to the present
	// contour (North -> South)
	private void sinusoidalDown(int waveLength, int waveHeight)
			throws InterruptedException, IOException {
		int steps, sleeptime = 900;

		steps = waveLength;
		String currentDir;
		String[] dir = { "W", "S", "E", "S" };

		previousLoc = currentLoc;

		for (int i = 0; i < dir.length; i++) {

			currentDir = dir[i];
			if (currentDir.equals("S")) {
				steps = waveLength;
			} else {
				steps = waveHeight;
			}

			for (int j = 0; j < steps; j++) {

				setCurrentLoc();
				move(currentDir);

				Thread.sleep(sleeptime);
			}
		}
	}

	// wave like motion path, the pattern will be adjusted to the present
	// contour (South -> North)
	private void sinusoidalUp(int waveLength, int waveHeight)
			throws InterruptedException, IOException {
		int steps, sleeptime = 900;

		steps = waveLength;
		String currentDir;
		String[] dir = { "W", "N", "E", "N" };

		previousLoc = currentLoc;

		for (int i = 0; i < dir.length; i++) {

			currentDir = dir[i];
			if (currentDir.equals("N")) {
				steps = waveLength;
			} else {
				steps = waveHeight;
			}

			for (int j = 0; j < steps; j++) {

				setCurrentLoc();
				move(currentDir);

				Thread.sleep(sleeptime);
			}
		}
	}

	// search the nearest wall(Rock, Sand, or Null tile) spiral-ward
	public Coord outwardSpiralSearch(Coord curr) throws Exception {

		int searchSize = 10;
		Coord topL, bottomR, temp;
		int x, y, xx, yy;

		for (int i = 1; i <= searchSize; i++) {
			topL = new Coord(curr.xpos - i, curr.ypos - i);
			bottomR = new Coord(curr.xpos + i, curr.ypos + i);

			// north edge
			x = topL.xpos;
			y = topL.ypos;
			for (xx = x; xx <= bottomR.xpos; xx++) {

				temp = new Coord(xx, y);
				if (isWithinTheGrid(xx, y, 50) && isObsatacle(temp)) {
					return temp;
				}
			}
			// east edge
			x = bottomR.xpos;
			y = topL.ypos + 1;
			for (yy = y; yy <= bottomR.ypos; yy++) {

				temp = new Coord(x, yy);
				if (isWithinTheGrid(x, yy, 50) && isObsatacle(temp)) {
					return temp;
				}
			}
			// south edge
			x = bottomR.xpos - 1;
			y = bottomR.ypos;
			for (xx = x; xx >= topL.xpos; xx--) {

				temp = new Coord(xx, y);
				if (isWithinTheGrid(xx, y, 50) && isObsatacle(temp)) {
					return temp;
				}
			}
			// west edge
			x = topL.xpos;
			y = bottomR.ypos - 1;
			for (yy = y; yy > topL.ypos; yy--) {

				temp = new Coord(x, yy);
				if (isWithinTheGrid(x, yy, 50) && isObsatacle(temp)) {
					return temp;
				}
			}
		}

		return null;
	}

	// given two coordinates, pick a next move-direction
	public String pickADir(Coord from, Coord to) {

		int dx = to.xpos - from.xpos;
		int dy = to.ypos - from.ypos;

		if (dx * dx > dy * dy) {
			if (dx > 0) {
				return "E";
			} else {
				return "W";
			}
		} else {
			if (dy > 0) {
				return "S";
			} else {
				return "N";
			}
		}
	}

	void run() throws IOException, InterruptedException {

		// prep corp's communication process
		if (url == null || url.equals("")) {
			url = "http://23.251.155.186:3000/api";
		}
		if (corp_secret == null || corp_secret.equals("")) {
			corp_secret = "0FSj7Pn23t";
		}
		Communication com = new Communication(url, rovername, corp_secret);

		// initialize local variables
		new ArrayList<String>();
		Socket socket = null;
		boolean astarGo = false, hasHitTheNorthWall = false, isDonePerimeterWalk = false, isNoWallsAround = true;
		int pedometer = 0;
		Coord startLocPerimeterFollowing;

		try {

			// debug
			System.out.println("corp url: " + url);
			System.out.println("corp secret: " + corp_secret);
			System.out.println("swermserver addr: " + SERVER_ADDRESS);
			// Thread.sleep(10000);

			// ***** connect to server ******
			socket = connectToSwarmServer();
			getEquipment();

			// ***** initialize critical locations ******
			rovergroupStartPosition = requestStartLoc(socket);
			targetLocation = requestTargetLoc(socket);
			nextTarget = targetLocation.clone();

			System.out.println("ROVER_12 Global Start Position: "
					+ rovergroupStartPosition);
			System.out.println("ROVER_12 Global Target Position: "
					+ targetLocation);

			// *****
			loadScanMapFromSwarmServer();

			currentLoc.clone();
			cardinals[1] = true;

			startLocPerimeterFollowing = currentLoc.clone();

			// debug --- remove it
			isNoWallsAround = true;
			while (true) {
				setCurrentLoc();
				pathMap.add(new Coord(currentLoc.xpos, currentLoc.ypos));
				// System.out.println("BEFORE: " + currentLoc + " | facing "
				// + getFacingDirection());

				// ***** do a SCAN (every 4 steps) ******
				if (pedometer % 4 == 3) {
					loadScanMapFromSwarmServer();
					// post to the communication server
					com.postScanMapTiles(currentLoc, scanMap.getScanMap());
				}

				// --------- current -------------
				// if there's a wall to follow, follow right-hand-side wall
				if (!isDonePerimeterWalk) {

					// debug
					// System.out.println("(wf)");

					
					doTheWallFollowingWalk();

					visitedOnAParimeterWalk.put(new Coord(currentLoc.xpos,
							currentLoc.ypos), true);

					if (currentLoc.equals(startLocPerimeterFollowing)) {
						// debug
						// System.out
						// .println("(wf)we are done with the perimeter walk for now");

						isDonePerimeterWalk = true;
						calibrateFacingDir();
					}
					if (!isAWallAround()) {
						isDonePerimeterWalk = true;
						calibrateFacingDir();
					}
				} else {

					if (currentLoc.xpos <= 0 || currentLoc.ypos <= 0) {
						// System.out.println("curr pos x = 0 or y = 0");
						isDonePerimeterWalk = false;
						startLocPerimeterFollowing = currentLoc.clone();
						calibrateFacingDir();
					} else {
						// System.out.println("(free walk)");
						roverMotionLogicShort();

						// if there's at least a wall (obstacle) around, and has not been wall-followed by the rover 
						if (isAWallInThe4Adj()
								&& visitedOnAParimeterWalk.get(currentLoc) == null) {
							// System.out
							// .println("(free walk)switch from free-walk to w-follower (curr loc: "
							// + currentLoc + ")");

							isDonePerimeterWalk = false;
							startLocPerimeterFollowing = currentLoc.clone();
							calibrateFacingDir();
						}
					}
				}
				setCurrentLoc();
				pedometer++;
				pathMap.add(new Coord(currentLoc.xpos, currentLoc.ypos));
				visitedOnAParimeterWalk.put(new Coord(currentLoc.xpos,
						currentLoc.ypos), true);
				pathMap.add(new Coord(currentLoc.xpos, currentLoc.ypos));

				if (visitCounts.get(currentLoc) != null) {
					visitCounts
							.put(currentLoc, visitCounts.get(currentLoc) + 1);
				} else {
					visitCounts.put(currentLoc, 1);
				}

				if (pedometer % 10 == 0) {
					System.out.println("ROVER_12 ------------ ends iteration[ "
							+ pedometer + " ]--------------");
				}
				Thread.sleep(sleepTime);

			}// end while loop
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
	}// END of run()

	/**
	 * Runs the client
	 */
	public static void main(String[] args) throws Exception {

		// take in first input argument as a SERVER_ADDRESS value
		String serverAddress = "", url = "", corps_secret = "";

		if (args.length >= 1 && args[0] != null) {
			serverAddress = args[0];
		}

		if (args.length >= 2 && args[1] != null) {
			url = args[1];
		}
		if (args.length >= 3 && args[2] != null) {
			corps_secret = args[2];
			// System.out.println("corp secret in main():" + corps_secret);
		}

		ROVER_12 client = new ROVER_12(serverAddress, url, corps_secret);
		client.run();
	}
}