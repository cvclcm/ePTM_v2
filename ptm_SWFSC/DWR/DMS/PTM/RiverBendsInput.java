package DWR.DMS.PTM;

class RiverBendsInput {

	// Class variables
	public static String inputDir;
	public static String channelOrientBendInput;
	public static String velocityInput;
	public static String epsilonTInput;
	public static String dEpsilonTdyInput;
	public static String epsilonVInput;
	public static String dEpsilonVdzInput;
	public static String d2EpsilonTdy2Input;

	public static ChannelOrientBendData channelOrientBend;
	public static LatData latEps;
	public static LatData latdEP;
	public static LatData latd2EP;
	public static LatData latVel;
	public static VertData vertEps;
	public static VertData vertdEP;

	// Read the input data
	public static void readData() {
		channelOrientBend = new ChannelOrientBendData(inputDir, channelOrientBendInput);
		latEps = new LatData(inputDir, epsilonTInput);
		latdEP = new LatData(inputDir, dEpsilonTdyInput);
		latd2EP = new LatData(inputDir, d2EpsilonTdy2Input);
		latVel = new LatData(inputDir, velocityInput);
		vertEps = new VertData(inputDir, epsilonVInput);
		vertdEP = new VertData(inputDir, dEpsilonVdzInput);
	}

	// Get specific ChannelOrientBend value
	public static double getChannelOrientBend(Integer channelNum, String parameter) {
		return channelOrientBend.getChannelOrientBend(channelNum, parameter);
	}

	// Get specific latEps value
	public static double getLatEps(Integer latPos, String parameter, boolean flipProfile) {
		return latEps.getData(latPos, parameter, flipProfile);
	}

	// Get specific latdEP value
	public static double getLatdEP(Integer latPos, String parameter, boolean flipProfile) {
		return latdEP.getData(latPos, parameter, flipProfile);
	}

	// Get specific latd2Ep value (VKS)
	public static double getLatd2EP(Integer latPos, String parameter, boolean flipProfile) {
		return latd2EP.getData(latPos, parameter, flipProfile);
	}

	// Get specific latVel value
	public static double getLatVel(Integer latPos, String parameter, boolean flipProfile) {
		return latVel.getData(latPos, parameter, flipProfile);
	}

	// Get specific VertEps value
	public static double getVertEps(Integer vertPos) {
		return vertEps.getData(vertPos);
	}

	// Get specific VertdEP value
	public static double getVertdEP(Integer vertPos) {
		return vertdEP.getData(vertPos);
	}

	// VKS: Function to get the value of a hydrodynamic quantity for a given point from the lateral profiles
	public static float getLatPosVal(float yfrac, String curvHead, int bendDir, int valFlag) {
		int yPsign;
		Integer yFracInt;
		float tp;

		// Get the sign of yfrac
		yPsign = (int) Math.signum(yfrac);

		// Convert three decimals of yfrac to an integer
		yFracInt = Math.round(yfrac * 1000);

		// Adjust to the nearest odd number
		if (yFracInt % 2 == 0) {
			yFracInt -= yPsign;
		}

		// Handle the zero case
		if(yFracInt==0) {
			yFracInt=1;
		}

		// Lateral velocity function value at the point
		if (bendDir == -1) {
			if (valFlag == 0)
				tp = (float) RiverBendsInput.getLatVel(yFracInt, curvHead, true);
			else if (valFlag == 1)
				tp = (float) RiverBendsInput.getLatEps(yFracInt, curvHead, true);
			else if (valFlag == 2)
				tp = (float) RiverBendsInput.getLatdEP(yFracInt, curvHead, true);
			else
				tp = (float) RiverBendsInput.getLatd2EP(yFracInt, curvHead, true);
		} else {
			if (valFlag == 0)
				tp = (float) RiverBendsInput.getLatVel(yFracInt, curvHead, false);
			else if (valFlag == 1)
				tp = (float) RiverBendsInput.getLatEps(yFracInt, curvHead, false);
			else if (valFlag == 2)
				tp = (float) RiverBendsInput.getLatdEP(yFracInt, curvHead, false);
			else
				tp = (float) RiverBendsInput.getLatd2EP(yFracInt, curvHead, false);
		}

		return (tp);
	}

	// VKS: Function to get the value of a hydrodynamic quantity for a given point from the vertical profiles
	public static float getVertPosVal(float zfrac, int valFlag) {

		Integer zFracInt;
		float tp;

		// Convert three decimals of yfrac to an integer
		zFracInt = Math.round(zfrac * 1000);

		// Adjust to the nearest odd number
		if (zFracInt % 2 == 0) {
			zFracInt -= 1;
		}

		// Handle the zero and negative (after adjustment above) cases
		if(zFracInt<=0) {
			zFracInt=1;
		}

		// Lateral velocity function value at the point
		if (valFlag == 1)
			tp = (float) RiverBendsInput.getVertEps(zFracInt);
		else
			tp = (float) RiverBendsInput.getVertdEP(zFracInt);

		return (tp);

	}
}
