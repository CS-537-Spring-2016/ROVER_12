package Kae;

import static org.junit.Assert.*;

import org.junit.Test;

import common.Coord;
import common.MapTile;
import d1.Fruit;
import enums.Terrain;
import enums.Science;
import common.ScanMap;

public class test_ks {

	
	@Test
	public void testIncDecXY() {
		CoordUtil cd = new CoordUtil(3,4);
		System.out.println(cd);
		cd.incrementX();
		System.out.println(cd);
		
	}
	//@Test
	public void testBooleanDefault() {
		boolean[][] tf = new boolean[3][3];
		System.out.println(tf[0][2]);
	}
	//@Test
	public void testContainsSci() {
		ScanMapUtil mt = (ScanMapUtil) constructMockScanMap_noSci();
		ScanMapUtil mts = (ScanMapUtil) constructMockScanMap_withSci();
		printMapTileArray(mt.getScanMap());
		System.out.println("contains science? " + mt.containsScience());
		printMapTileArray(mts.getScanMap());
		System.out.println("contains science? " + mts.containsScience());
	}

	// @Test
	public void testContainsSand() {
		ScanMapUtil mt = (ScanMapUtil) constructMockScanMap_noSand();
		ScanMapUtil mts = (ScanMapUtil) constructMockScanMap_withSand();
		printMapTileArray(mt.getScanMap());
		System.out.println("contains soil? " + mt.containsSand());
		printMapTileArray(mts.getScanMap());
		System.out.println("contains soil? " + mts.containsSand());
	}

	// @Test
	public void testMockScanMapConstruction() {
		ScanMap mt = constructMockScanMap_noSand();
		ScanMap mts = constructMockScanMap_withSand();
		printMapTileArray(mt.getScanMap());
		printMapTileArray(mts.getScanMap());

	}

	// @Test
	public void testMapTileConstruction() {

		MapTile[][] mt = {
				{ new MapTileUtil(Terrain.ROCK, 0),
						new MapTileUtil(Terrain.ROCK, 0),
						new MapTileUtil(Terrain.ROCK, 0) },
				{ new MapTileUtil(Terrain.GRAVEL, 0),
						new MapTileUtil(Terrain.ROCK, 0),
						new MapTileUtil(Terrain.ROCK, 0) } };

		for (int i = 0; i < mt.length; i++) {
			for (int j = 0; j < (mt[i].length); j++) {
				System.out.print(mt[i][j].getTerrain() + " ");
			}
			System.out.println();
		}
	}

	// @Test
	public void testFrt() {

		Fruit fr = new Fruit("fruit", "fruit sent");
		Orange o = new Orange("org", "orange sent");

	}

	// ********* non-test methods ******************
	public ScanMap constructMockScanMap_noSand() {

		MapTile[][] mt = {
				{ new MapTileUtil(Terrain.ROCK, 0),
						new MapTileUtil(Terrain.ROCK, 0),
						new MapTileUtil(Terrain.ROCK, 0) },
				{ new MapTileUtil(Terrain.GRAVEL, 0),
						new MapTileUtil(Terrain.ROCK, 0),
						new MapTileUtil(Terrain.ROCK, 0) } };

		ScanMap smu = new ScanMapUtil(mt);

		return smu;
	}

	public ScanMap constructMockScanMap_withSand() {

		MapTile[][] mt = {
				{ new MapTileUtil(Terrain.ROCK, 0),
						new MapTileUtil(Terrain.ROCK, 0),
						new MapTileUtil(Terrain.ROCK, 0) },
				{ new MapTileUtil(Terrain.GRAVEL, 0),
						new MapTileUtil(Terrain.SAND, 0),
						new MapTileUtil(Terrain.ROCK, 0) } };

		ScanMap smu = new ScanMapUtil(mt);

		return smu;
	}

	public ScanMap constructMockScanMap_noSci() {

		MapTile[][] mt = {
				{ new MapTileUtil(Terrain.NONE, Science.NONE, 0, false),
						new MapTileUtil(Terrain.NONE, Science.NONE, 0, false),
						new MapTileUtil(Terrain.NONE, Science.NONE, 0, false) },
				{ new MapTileUtil(Terrain.NONE, Science.NONE, 0, false),
						new MapTileUtil(Terrain.NONE, Science.NONE, 0, false),
						new MapTileUtil(Terrain.NONE, Science.NONE, 0, false) } };

		ScanMap smu = new ScanMapUtil(mt);

		return smu;
	}

	public ScanMap constructMockScanMap_withSci() {

		MapTile[][] mt = {
				{
						new MapTileUtil(Terrain.NONE, Science.ARTIFACT, 0,
								false),
						new MapTileUtil(Terrain.NONE, Science.NONE, 0, false),
						new MapTileUtil(Terrain.NONE, Science.CRYSTAL, 0, false) },
				{ new MapTileUtil(Terrain.NONE, Science.NONE, 0, false),
						new MapTileUtil(Terrain.NONE, Science.NONE, 0, false),
						new MapTileUtil(Terrain.NONE, Science.NONE, 0, false) } };

		ScanMap smu = new ScanMapUtil(mt);

		return smu;
	}

	public void printMapTileArray(MapTile[][] mta) {
		for (int i = 0; i < mta.length; i++) {
			for (int j = 0; j < mta[i].length; j++) {
				System.out.print(mta[i][j].getTerrain() + "("
						+ mta[i][j].getScience() + ")" + " ");
			}
			System.out.println();
		}
	}
}
