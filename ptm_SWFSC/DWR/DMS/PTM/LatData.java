package DWR.DMS.PTM;

class LatData extends ProfileData {

	// Class variables
	// Number of data columns and angle increments between columns (radians)
	public static int numCols = 65;
	public static double colIncrement_rad = 2 * Math.PI / (numCols - 1);

	public LatData(String inputDir, String filename) {
		super(inputDir, filename);
	}

	// Get specific value
	public double getData(Integer latPos, String parameter, boolean flipProfile) {
		Integer rowIndex, colIndex;

		if (flipProfile) {
			latPos = -1*latPos;
		}

		rowIndex = rowIndices.get(latPos);
		colIndex = colIndices.get(parameter);

		if (rowIndex != null && colIndex != null) {
			return data[rowIndex][colIndex];
		} else {
			System.out.println("Could not find value for latPos '" + latPos + "', parameter '" + parameter + "' in " + filename + ". Returning -999");
			return -999.0;
		}
	}

	// Convert radians into column name
	public static String convertRadToHeader(double rad) {
		Integer colIndex, thetaIndex;
		double thisCol;

		// Find the first column that equals or exceeds rad
		for (colIndex = 0; colIndex < numCols; colIndex++) {
			thisCol = colIndex * colIncrement_rad;
			if (Math.abs(thisCol - rad) < 0.0001 || thisCol > rad) {
				break;
			}
		}
		thetaIndex = colIndex + 1;

		return "theta" + thetaIndex.toString();
	}
}
