package DWR.DMS.PTM;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.File;
import java.util.HashMap;

public class JunctionsInput {

	// Class variables
	public static String inputDir;
	public static String inputFile;
	public static HashMap<Integer, Integer> channelRows = new HashMap<Integer, Integer>();
	public static HashMap<Integer, int[]> rowValues = new HashMap<Integer, int[]>();

	// Read junctions.csv
	public static void readData() throws IOException {

		String line;
		String[] values;
		String path;
		File file;
		int numCols, rowIndex, thisValue;
		int[] thisRowValues;
		BufferedReader bReader = null;

		file = new File(inputDir, inputFile);
		path = file.getPath();
		
		try {
			bReader = new BufferedReader(new FileReader(path));
					
			// Read the header line
			line = bReader.readLine();
			values = line.split(",");
			numCols = values.length - 1;

			// Read the data
			rowIndex = 0;
			while ((line = bReader.readLine()) != null) {
				values = line.split(",");

				thisRowValues = new int[numCols];

				for (int i = 1; i < values.length; i++) {
					thisValue = Integer.parseInt(values[i]);
					channelRows.put(thisValue, rowIndex);

					thisRowValues[i - 1] = thisValue;
				}

				rowValues.put(rowIndex, thisRowValues);

				rowIndex++;
			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (bReader != null) bReader.close();
		}
	}

	public static int[] getRow(int channelNum, boolean oceanOrient) {
		Integer rowIndex;
		rowIndex = oceanOrient ? channelRows.get(channelNum) : channelRows.get(channelNum * -1);

		if (rowIndex != null) {
			return rowValues.get(rowIndex);
		} else {
			System.out.println("Could not find row values for channel " + (oceanOrient ? channelNum : (channelNum*-1)) + " in junctions.csv. Returning [-999, -999, -999].");
			return new int[] {-999, -999, -999};
		}
	}

}
