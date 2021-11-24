package DWR.DMS.PTM;

class VertData extends ProfileData {

	public VertData(String inputDir, String filename) {
		super(inputDir, filename);
	}

	// Get specific value from vertical distributions
	public double getData(Integer vertPos) {
		Integer rowIndex, colIndex;

		rowIndex = rowIndices.get(vertPos);
		colIndex = 0;

		if (rowIndex != null && colIndex != null) {
			return data[rowIndex][colIndex];
		} else {
			System.out.println("Could not find value for VertPos '" + vertPos + "' in " + filename + ". Returning -999");
			return -999.0;
		}
	}
}
