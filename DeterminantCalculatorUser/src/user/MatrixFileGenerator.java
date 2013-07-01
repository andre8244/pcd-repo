package user;

import log.l;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;

public class MatrixFileGenerator {

	private static BufferedWriter writer;
	private static Random rand;
	private static String me = "matrixUtil";

	public static void generate(int order, double minAbs, double maxAbs, String fileName) {
		long startTime = System.currentTimeMillis();
		rand = new Random();

		File file = new File(fileName);
		l.l("MATRIXUTIL ", file.getAbsolutePath());

		try {
			writer = new BufferedWriter(new FileWriter(fileName));
			double val;
			l.l(me, "Writing matrix to file...");

			for (int row = 0; row < order; row++) {
				for (int col = 0; col < order; col++) {
					val = minAbs + rand.nextDouble() * (maxAbs - minAbs);
					if (rand.nextBoolean()) {
						val = -val;
					}
					if (col == order - 1) {
						writer.write(val + "\n");
					} else {
						writer.write(val + " ");
					}
				}
				if (row % 500 == 0) {
					l.l(me, (int) ((double) row / order * 100) + " %");
				}
			}
			l.l(me, "matrix wrote to file, duration: " + ((System.currentTimeMillis() - startTime) / (double) 1000) + " sec");
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (writer != null) {
				try {
					writer.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
