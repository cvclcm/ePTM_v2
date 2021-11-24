package DWR.DMS.PTM;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.File;
import java.util.HashMap;

public class ChannelOrientBendData {

	// Instance variables
	public String inputDir;
	public String inputFile;
	public double[][] channelOrientBend;
	public HashMap<String, Integer> channelOrientBendColIndex = new HashMap<String, Integer>();
	public HashMap<Integer, Integer> channelOrientBendRowIndex = new HashMap<Integer, Integer>();

	public ChannelOrientBendData(String inputDir, String inputFile) {

		this.inputDir = inputDir;
		this.inputFile = inputFile;

		try {
			readChannelOrientBend();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	// Read ChannelOrientBend.csv
	public void readChannelOrientBend() throws IOException {

		String line;
		String[] values;
		File file;
		String path;
		int channelNum, rowIndex;
		int[] dimensions = new int[2];
		BufferedReader bReader = null;

		file = new File(inputDir, inputFile);
		path = file.getPath();
		
		// Determine the dimensions of the input file
		dimensions = getDimensions(path);

		channelOrientBend = new double[dimensions[0]][dimensions[1]];

		try {
			bReader = new BufferedReader(new FileReader(path));
			// Read the header line
			line = bReader.readLine();
			values = line.split(",");

			for (int i = 1; i < values.length; i++) {
				channelOrientBendColIndex.put(values[i], i - 1);
			}

			// Read the data
			rowIndex = 0;
			while ((line = bReader.readLine()) != null) {
				values = line.split(",");
				channelNum = Integer.parseInt(values[0]);
				channelOrientBendRowIndex.put(channelNum, rowIndex);

				for (int i = 1; i < values.length; i++) {
					channelOrientBend[rowIndex][i - 1] = Double.parseDouble(values[i]);
				}
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

	// Get specific ChannelOrientBend value
	public double getChannelOrientBend(Integer channelNum, String parameter) {
		Integer rowIndex, colIndex;

		rowIndex = channelOrientBendRowIndex.get(channelNum);
		colIndex = channelOrientBendColIndex.get(parameter);

		if (rowIndex != null && colIndex != null) {
			return channelOrientBend[rowIndex][colIndex];
		} else {
			System.out.println(
					"Could not find value for channelNum " + channelNum + ", parameter " + parameter + " in ChannelOrientBend. Returning -999");
			return -999.0;
		}
	}

	// Return the number of rows and columns in the input table, excluding indices
	public int[] getDimensions(String path) throws IOException {

		String line;
		String[] values;
		int numRows = 0;
		int numCols = 0;
		BufferedReader bReader = null;

		try {

			bReader = new BufferedReader(new FileReader(path));
					
			// Read the header line
			line = bReader.readLine();
			values = line.split(",");

			numCols = values.length - 1;

			// Read the data
			numRows = 0;
			while ((line = bReader.readLine()) != null) {

				numRows++;
			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (bReader != null) bReader.close();
		}

		return new int[] {numRows, numCols};
	}
}
