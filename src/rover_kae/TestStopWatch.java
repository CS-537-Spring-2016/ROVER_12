package rover_kae;

import java.util.Date;

import org.junit.Test;

public class TestStopWatch {

	//@Test
	public void testarray() {

		int[][] array = { { 1, 2, 3 }, { 4, 5, 6 }, { 7, 8, 9 } };
		int edgeSize = 3;

		for (int j = 0; j < edgeSize; j++) {
			for (int i = 0; i < edgeSize; i++) {
				System.out.print(array[j][i] + " ");
			}
			System.out.println();
		}
		System.out.println(array[1][0]);
	}

	 @Test
	public void test() {
		long start, end;
		start = System.currentTimeMillis();
		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		end = System.currentTimeMillis();
		System.out.println("elapsed time: " + (end - start));
	}

}
