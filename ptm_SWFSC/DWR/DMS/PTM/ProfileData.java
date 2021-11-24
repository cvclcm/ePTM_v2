package DWR.DMS.PTM;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.File;
import java.util.HashMap;

abstract class ProfileData {

	// Instance variables
	public String inputDir;
	public String filename;
	public double[][] data;
	public HashMap<String, Integer> colIndices = new HashMap<String, Integer>();
	public HashMap<Integer, Integer> rowIndices = new HashMap<Integer, Integer>();

	public ProfileData(String inputDir, String filename) {

		this.inputDir = inputDir;
		this.filename = filename;

		try {
			readData();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	// Read data from *.csv
	public void readData() throws IOException {

		String line;
		String[] values;
		File file;
		String path;
		Integer latPos;
		int rowIndex;
		int[] dimensions = new int[2];
		BufferedReader bReader = null;
		
		file = new File(inputDir, filename);
		path = file.getPath();

		// Determine the dimensions of the input file
		dimensions = getDimensions(path);

		data = new double[dimensions[0]][dimensions[1]];

		try {
			
			bReader = new BufferedReader(new FileReader(path));
					
			// Read the header line
			line = bReader.readLine();
			values = line.split(",");

			for (int i = 1; i < values.length; i++) {
				colIndices.put(values[i], i - 1);
			}

			// Read the data
			rowIndex = 0;
			while ((line = bReader.readLine()) != null) {
				values = line.split(",");
				latPos = Math.round(Float.parseFloat(values[0])*1000);
				rowIndices.put(latPos, rowIndex);

				for (int i = 1; i < values.length; i++) {
					data[rowIndex][i - 1] = Double.parseDouble(values[i]);
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
