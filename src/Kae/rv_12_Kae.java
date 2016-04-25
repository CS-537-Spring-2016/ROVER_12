package Kae;

/*  scan map size is 11 x 11
 */
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.reflect.Type;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import common.Coord;
import common.MapTile;
import common.ScanMap;
import enums.Science;
import enums.Terrain;

/**
 * The seed that this program is built on is a chat program example found here:
 * http://cs.lmu.edu/~ray/notes/javanetexamples/ Many thanks to the authors for
 * publishing their code examples
 * 
 * allowed # request to the server per sec = 500 2 req / sec
 */

public class rv_12_Kae {

	BufferedReader in;
	PrintWriter out;
	String rovername;
	// ScanMap scanMap;
	ScanMap scanMap;
	int sleepTime;
	String SERVER_ADDRESS = "localhost";
	static final int PORT_ADDRESS = 9537;
	Random rd = new Random();
	MapTile[][] scanMapTiles;

	CoordUtil currentLoc = null;
	Coord previousLoc = null;

	String currentDir = "";
	Set<String> blockedDirs = new HashSet<String>();
	Set<String> openDirs = new HashSet<String>();
	String[] cardinals = new String[4];

	MapTile[][] mapJournal = new MapTileUtil[100][100];
	boolean[][] footPrints = new boolean[100][100];

	// use deque for time complexity sake?
	// please kindly msg me if you know this is a totally wrong approach (at
	// se1k1h1mawar1@gmail.com)
	Deque<String> scienceBag = new ArrayDeque<String>();

	public rv_12_Kae() {
		System.out.println("ROVER_12 rover object constructed");
		rovername = "ROVER_12";
		SERVER_ADDRESS = "localhost";
		// this should be a safe but slow timer value
		sleepTime = 300; // in milliseconds - smaller is faster, but the server
							// will cut connection if it is too small
	}

	public rv_12_Kae(String serverAddress) {
		System.out.println("ROVER_12 rover object constructed");
		rovername = "ROVER_12";
		SERVER_ADDRESS = serverAddress;
		sleepTime = 300; // in milliseconds - smaller is faster, but the server
							// will cut connection if it is too small

	}

	/**
	 * Connects to the server then enters the processing loop.
	 */
	// KSTD - set visibility back to private
	public void run() throws IOException, InterruptedException {

		int rdNum;
		// String currentDir;

		// TODO - need to close this socket
		makeConnAndInitStream();

		// Gson gson = new GsonBuilder().setPrettyPrinting().create();
		processServerMsgAndWaitForIDRequestCall();

		this.doScan();

		System.out.println("did scan, now print scanMap:");
		// scanMap.debugPrintMap();

		// ******** Rover logic *********
		String line = "";
		boolean stuck = false;
		boolean blocked = false;
		cardinals[0] = "E";
		cardinals[1] = "S";
		cardinals[2] = "W";
		cardinals[3] = "N";

		ArrayList<String> equipment = getEquipment();
		System.out.println("ROVER_12 equipment list " + equipment + "\n");

		// moveRover12ToAClearArea();

		// ******** Rover motion *********
		while (true) {

			// out.println("MOVE S");
			// out.println("MOVE E");

			setCurrentLoc(currentLoc);
			// debugPrint4Dirs(currentLoc);

			// if (previousLoc != null && previousLoc.equals(currentLoc)) {
			// stuck = true;
			// }

			// previousLoc = currentLoc;
			// scanMapTiles = pullLocalMap();

			// doThisWhenStuck(currentLoc, scanMapTiles);

			// sinusoidal(cardinals);
			sinusoidal_LR(cardinals, 6, 4);
			random(cardinals);
			Thread.sleep(sleepTime);

			System.out
					.println("\nROVER_12 ------------ bottom process control --------------");
		}
	}

	private void doThisWhenStuck(Coord currentLoc, MapTile[][] scanMapTiles)
			throws InterruptedException {

		String currentDir;
		getOpenDir(currentLoc);
		currentDir = openDirs.toArray(new String[1])[0];
		for (int i = 0; i < 4; i++) {
			out.println("MOVE " + currentDir);
			Thread.sleep(300);
		}
	}

	private void makeConnAndInitStream() throws UnknownHostException,
			IOException {
		Socket socket = new Socket(SERVER_ADDRESS, PORT_ADDRESS); // set port
																	// here
		in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		out = new PrintWriter(socket.getOutputStream(), true);
	}

	private void processServerMsgAndWaitForIDRequestCall() throws IOException {
		while (true) {
			String line = in.readLine();
			if (line.startsWith("SUBMITNAME")) {
				out.println(rovername);
				break;
			}
		}
	}

	private void resetOpenDir() {
		openDirs.add("E");
		openDirs.add("W");
		openDirs.add("S");
		openDirs.add("N");
	}

	private void getOpenDir(Coord currentLoc) {
		// KSTD - do I need to run findBlockedDirs every time I do getopendir()?
		resetOpenDir();
		findBlockedDirs(currentLoc);

		// DEBUG - remove before submission
		System.out.print("\n" + "BLOCK CHECK:");
		System.out.print("\n" + "blocked:");
		for (String s : blockedDirs) {
			System.out.print(s + " ");
		}

		for (String dir : blockedDirs) {
			if (blockedDirs.contains(dir)) {
				openDirs.remove(dir);
			}
		}

		// DEBUG - remove before submission
		System.out.print("\n" + "open:");
		for (String s : openDirs) {
			System.out.print(s + " ");
		}
	}

	// ******* currently developing ****************
	// KSTD - implement
	private boolean isTreasureSpot() {

		return false;
	}

	// KSTD - implement
	private boolean isSand(Coord currentLoc) {
		return mapJournal[currentLoc.ypos][currentLoc.xpos].getTerrain()
				.equals(Terrain.SAND);
	}

	// KSTD - implement
	private boolean theQuadrantContainsSand() {
		return true;
	}

	// **********************************************

	private void findBlockedDirs(Coord currentLoc) {
		int centerIndex = (scanMap.getEdgeSize() - 1) / 2;

		System.out.println("scan map size ( findBlockedDirs() ): "
				+ scanMap.getEdgeSize());

		debugPrint4Dirs(scanMapTiles, centerIndex);

		if (scanMapTiles[centerIndex][centerIndex - 1].getHasRover()
				|| scanMapTiles[centerIndex][centerIndex - 1].getTerrain() == Terrain.ROCK
				|| scanMapTiles[centerIndex][centerIndex - 1].getTerrain() == Terrain.NONE
				|| scanMapTiles[centerIndex][centerIndex - 1].getTerrain() == Terrain.SAND) {
			System.out.println("north blocked");
			blockedDirs.add("N");
		}

		if (scanMapTiles[centerIndex][centerIndex + 1].getHasRover()
				|| scanMapTiles[centerIndex][centerIndex + 1].getTerrain() == Terrain.ROCK
				|| scanMapTiles[centerIndex][centerIndex + 1].getTerrain() == Terrain.NONE
				|| scanMapTiles[centerIndex][centerIndex + 1].getTerrain() == Terrain.SAND) {
			System.out.println("south blocked");
			blockedDirs.add("S");
		}

		if (scanMapTiles[centerIndex + 1][centerIndex].getHasRover()
				|| scanMapTiles[centerIndex + 1][centerIndex].getTerrain() == Terrain.ROCK
				|| scanMapTiles[centerIndex + 1][centerIndex].getTerrain() == Terrain.NONE
				|| scanMapTiles[centerIndex + 1][centerIndex].getTerrain() == Terrain.SAND) {
			System.out.println("east blocked");
			blockedDirs.add("E");
		}

		if (scanMapTiles[centerIndex - 1][centerIndex].getHasRover()
				|| scanMapTiles[centerIndex - 1][centerIndex].getTerrain() == Terrain.ROCK
				|| scanMapTiles[centerIndex - 1][centerIndex].getTerrain() == Terrain.NONE
				|| scanMapTiles[centerIndex - 1][centerIndex].getTerrain() == Terrain.SAND) {
			System.out.println("west blocked");
			blockedDirs.add("W");
		}

	}

	// TODO - incomplete
	private void debugPrint4Dirs(Coord currLoc) {
		// System.out.println("center: "+
		// getScanMap().[currLoc.getYpos()][currLoc.getXpos()]);
		scanMap.debugPrintMap();
	}

	private void debugPrint4Dirs(MapTile[][] scanMapTiles, int centerIndex) {
		System.out.println("center: "
				+ scanMapTiles[centerIndex][centerIndex].getHasRover());
		System.out
				.println("s: "
						+ scanMapTiles[centerIndex][centerIndex + 1]
								.getElevation()
						+ " "
						+ scanMapTiles[centerIndex][centerIndex + 1]
								.getHasRover()
						+ " "
						+ scanMapTiles[centerIndex][centerIndex + 1]
								.getTerrain()
						+ " "
						+ scanMapTiles[centerIndex][centerIndex + 1]
								.getScience());
		System.out
				.println("n: "
						+ scanMapTiles[centerIndex][centerIndex - 1]
								.getElevation()
						+ " "
						+ scanMapTiles[centerIndex][centerIndex - 1]
								.getHasRover()
						+ " "
						+ scanMapTiles[centerIndex][centerIndex - 1]
								.getTerrain()
						+ " "
						+ scanMapTiles[centerIndex][centerIndex - 1]
								.getScience());
		System.out
				.println("w: "
						+ scanMapTiles[centerIndex - 1][centerIndex]
								.getElevation()
						+ " "
						+ scanMapTiles[centerIndex - 1][centerIndex]
								.getHasRover()
						+ " "
						+ scanMapTiles[centerIndex - 1][centerIndex]
								.getTerrain()
						+ " "
						+ scanMapTiles[centerIndex - 1][centerIndex]
								.getScience());
		System.out
				.println("e: "
						+ scanMapTiles[centerIndex + 1][centerIndex]
								.getElevation()
						+ " "
						+ scanMapTiles[centerIndex + 1][centerIndex]
								.getHasRover()
						+ " "
						+ scanMapTiles[centerIndex + 1][centerIndex]
								.getTerrain()
						+ " "
						+ scanMapTiles[centerIndex + 1][centerIndex]
								.getScience());

		blockedDirs.clear();
		if (currentDir.equals("E")) {
			if (scanMapTiles[centerIndex][centerIndex + 1].getHasRover()
					|| scanMapTiles[centerIndex][centerIndex + 1].getTerrain() == Terrain.ROCK
					|| scanMapTiles[centerIndex][centerIndex + 1].getTerrain() == Terrain.NONE
					|| scanMapTiles[centerIndex][centerIndex + 1].getTerrain() == Terrain.SAND) {
				System.out.println("east blocked");
				blockedDirs.add("E");
			}
		}
	}

	// TODO - must be implemented
	private void harvestScience() {
		String thisScience = "";
		scienceBag.add(thisScience);
	}

	private MapTile[][] pullLocalMap() throws IOException {

		MapTile[][] scanMapTiles = scanMap.getScanMap();
		return scanMapTiles;
	}

	private void takeOppositeDirection() {
		if (currentDir.equals("E"))
			currentDir = "W";
		if (currentDir.equals("W"))
			currentDir = "E";
		if (currentDir.equals("N"))
			currentDir = "S";
		if (currentDir.equals("S"))
			currentDir = "N";
	}

	private void setCurrentLoc(Coord loc) throws IOException {
		String line;
		out.println("LOC");
		line = in.readLine();
		if (line == null) {
			// System.out.println("ROVER_12 check connection to server");
			line = "";
		}
		if (line.startsWith("LOC")) {
			// loc = line.substring(4);
			currentLoc = (CoordUtil) extractLOC(line);
		}
		// DEBUG
		System.out.println("ROVER_12 currentLoc at start: " + currentLoc);
	}

	private void snake(String[] cardinals, int scanRange) {
		// TODO Auto-generated method stub

	}

	private void sinusoidal(String[] cardinals) throws InterruptedException {

		int waveLength = 3, waveHeight = 6, steps = waveLength;
		cardinals[0] = "E";
		cardinals[1] = "S";
		cardinals[2] = "E";
		cardinals[3] = "N";
		for (int i = 0; i < cardinals.length; i++) {

			currentDir = cardinals[i];
			if (currentDir.equals("E") || currentDir.equals("W")) {
				steps = waveLength;
			} else {
				steps = waveHeight;
			}

			for (int j = 0; j < steps; j++) {
				out.println("MOVE " + currentDir);
				Thread.sleep(700);
			}
		}
	}

	private boolean isStuck(Coord curr, Coord prev) {
		return curr.equals(prev);
	}

	// TODO - must be implemented
	private void awayFromSand() {
	}

	private void sinusoidal_LR(String[] cardinals, int waveLength,
			int waveHeight) throws InterruptedException {
		int steps;

		steps = waveLength;
		String currentDir;
		cardinals[0] = "E";
		cardinals[1] = "S";
		cardinals[2] = "E";
		cardinals[3] = "N";

		try {
			setCurrentLoc(currentLoc);
		} catch (IOException e) {
			e.printStackTrace();
		}

		if (previousLoc != null && isStuck(currentLoc, previousLoc)) {
			doThisWhenStuck(currentLoc, scanMapTiles);
		}
		previousLoc = currentLoc;

		for (int i = 0; i < cardinals.length; i++) {

			currentDir = cardinals[i];
			if (currentDir.equals("E") || currentDir.equals("E")) {
				steps = waveLength;
			} else {
				steps = waveHeight;
			}

			for (int j = 0; j < steps; j++) {
				out.println("MOVE " + currentDir);
				Thread.sleep(700);
			}
		}
	}

	private void move(String dir) {
		switch (dir) {
		case "E":
			moveEast();
			break;
		case "W":
			moveWest();
			break;
		case "N":
			moveNorth();
			break;
		case "S":
			moveSouth();
			break;
		default:
			break;
		}
	}

	private void moveWest() {
		out.println("MOVE W");
		currentLoc.decrementX();
		footPrints[currentLoc.getY()][currentLoc.getX()] = true;
	}

	private void moveEast() {
		out.println("MOVE E");
		currentLoc.incrementX();
		footPrints[currentLoc.getY()][currentLoc.getX()] = true;
	}

	private void moveNorth() {
		out.println("MOVE N");
		currentLoc.decrementY();
		footPrints[currentLoc.getY()][currentLoc.getX()] = true;
	}

	private void moveSouth() {
		out.println("MOVE S");
		currentLoc.incrementY();
		footPrints[currentLoc.getY()][currentLoc.getX()] = true;
	}

	private void sinusoidal_RL(String[] cardinals, int waveLength,
			int waveHeight) throws InterruptedException {
		int steps;

		steps = waveLength;
		String currentDir;
		cardinals[0] = "W";
		cardinals[1] = "S";
		cardinals[2] = "W";
		cardinals[3] = "N";
		for (int i = 0; i < cardinals.length; i++) {

			currentDir = cardinals[i];
			if (currentDir.equals("E") || currentDir.equals("E")) {
				steps = waveLength;
			} else {
				steps = waveHeight;
			}

			for (int j = 0; j < steps; j++) {
				if (!isSand(currentLoc))
					out.println("MOVE " + currentDir);
				Thread.sleep(700);
			}
		}
	}

	private void random(String[] cardinals) throws InterruptedException {
		int rdNum;
		String currentDir;
		for (int i = 0; i < 5; i++) {
			rdNum = randomNum(0, 3);
			currentDir = cardinals[rdNum];

			for (int j = 0; j < 3; j++) {
				out.println("MOVE " + currentDir);
				Thread.sleep(300);
			}
		}
	}

	private void moveRover12ToAClearArea() throws InterruptedException {
		for (int i = 0; i < 5; i++) {
			out.println("MOVE E");
			Thread.sleep(700);
		}
		for (int i = 0; i < 5; i++) {
			// get out of the crowd of rovers
			out.println("MOVE S");
			Thread.sleep(700);
		}
	}

	// ################ Support Methods ###########################

	private void clearReadLineBuffer() throws IOException {
		while (in.ready()) {
			System.out.println("ROVER_12 clearing readLine()");
			String garbage = in.readLine();
		}
	}

	// method to retrieve a list of the rover's equipment from the server
	private ArrayList<String> getEquipment() throws IOException {
		System.out.println("ROVER_12 method getEquipment()");
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		out.println("EQUIPMENT");

		String jsonEqListIn = in.readLine(); // grabs the string that was
												// returned first
		if (jsonEqListIn == null) {
			jsonEqListIn = "";
		}
		StringBuilder jsonEqList = new StringBuilder();
		System.out
				.println("ROVER_12 incomming EQUIPMENT result - first readline: "
						+ jsonEqListIn);

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

	public void initMapJournal() {
		for (int i = 0; i < mapJournal.length; i++) {
			for (int j = 0; j < mapJournal[i].length; j++) {

			}
		}
	}

	// sends a SCAN request to the server and puts the result in the scanMap
	// array
	public void doScan() throws IOException {
		System.out.println("ROVER_12 method doScan()");
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		out.println("SCAN");

		String jsonScanMapIn = in.readLine(); // grabs the string that was
												// returned first
		System.out.println("DBG jsonScanMapIn 336 = " + jsonScanMapIn);

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
				System.out.println("ROVER_12 doScan() bottom of while");
			}
		} else {
			// in case the server call gives unexpected results
			clearReadLineBuffer();
			return; // server response did not start with "SCAN"
		}
		// System.out.println("ROVER_12 finished scan while");

		String jsonScanMapString = jsonScanMap.toString();

		// System.out.println("ROVER_12 convert from json back to ScanMap class");
		// convert from the json string back to a ScanMap object
		scanMap = gson.fromJson(jsonScanMapString, ScanMap.class);
		// MapTile[][] ptrScanMap =
		// ((ScanMapUtil)scanMap).cloneMapTile(scanMap.getScanMap());
		MapTile[][] ptrScanMap = scanMap.getScanMap();
		Terrain ter;
		Science sci;
		int elev;
		boolean hasR;
		int scanMapHalfSize = (int) Math.floor(ptrScanMap.length / 2.);
		setCurrentLoc(currentLoc);
		Coord start = new Coord(currentLoc.getXpos() - scanMapHalfSize,
				currentLoc.getYpos() - scanMapHalfSize);
		System.out.println("start: " + start);
		System.out.println("within the grid? "
				+ withinTheGrid(start.ypos, start.xpos, mapJournal.length));

		System.out.println("ptrScanMap: ");
		debugPrintMapTileArray(ptrScanMap);

		// FIXME --- must correctly record scanned area of the map from scanMaps
		// to mapJournal

		for (int i = 0; i < ptrScanMap.length; i++) {
			for (int j = 0; j < ptrScanMap.length; j++) {

				if (withinTheGrid(start.ypos + i, start.xpos + j,
						mapJournal.length)) {
					ter = ptrScanMap[i][j].getTerrain();
					sci = ptrScanMap[i][j].getScience();
					elev = ptrScanMap[i][j].getElevation();
					hasR = ptrScanMap[i][j].getHasRover();

					System.out.println("recording map journal i,j -> " + i
							+ "," + j);
					if (mapJournal[start.ypos + i][start.xpos + j] == null) {
						mapJournal[start.ypos + i][start.xpos + j] = new MapTileUtil(
								ter, sci, elev, hasR);
					}
				}
			}
		}
		
		try {
			Thread.sleep(10000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// debug
		System.out.println("current map journal(null? " + (mapJournal == null)
				+ "):");

		debugPrintMapTileArray(mapJournal);
	}

	public boolean withinTheGrid(int i, int j, int arrayLength) {
		return i >= 0 && j >= 0 && i < arrayLength && j < arrayLength;
	}

	public void printMapTileArray(MapTile[][] array) {
		for (int i = 0; i < array.length; i++) {
			for (int j = 0; j < array[i].length; j++) {
				System.out.print(array[i][j] + " ");
			}
			System.out.println();
		}
	}

	public void printMapJournal() {
		System.out.println("current map journal status:");
		for (int i = 0; i < mapJournal.length; i++) {
			for (int j = 0; j < mapJournal.length; j++) {

				System.out.print(mapJournal[i][j].getTerrain() + "("
						+ mapJournal[i][j].getScience() + ")" + " ");
			}
			System.out.println();
		}
	}

	// this takes the LOC response string, parses out the x and y values and
	// returns a Coord object
	public static CoordUtil extractLOC(String sStr) {
		sStr = sStr.substring(4);
		if (sStr.lastIndexOf(" ") != -1) {
			String xStr = sStr.substring(0, sStr.lastIndexOf(" "));
			// System.out.println("extracted xStr " + xStr);

			String yStr = sStr.substring(sStr.lastIndexOf(" ") + 1);
			// System.out.println("extracted yStr " + yStr);
			return new CoordUtil(Integer.parseInt(xStr), Integer.parseInt(yStr));
		}
		return null;
	}

	public int randomNum(int min, int max) {
		return rd.nextInt(max + 1) + min;
	}

	/**
	 * Runs the client
	 */
	public static void main(String[] args) throws Exception {
		rv_12_Kae client = new rv_12_Kae();
		client.run();
	}

	public void debugPrintMapTileArray(MapTile[][] mapTileArray) {

		int edgeSize = mapTileArray.length;
		System.out.println("edge size: " + edgeSize);
		for (int k = 0; k < edgeSize + 2; k++) {
			System.out.print("--");
		}
		System.out.print("\n");
		for (int j = 0; j < edgeSize; j++) {
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
}