package DWR.DMS.PTM;

import java.util.LinkedList;
import java.util.ListIterator;

// A subclass of Channel with methods to provide channel-specific 
// information that BehavedParticles can use
// @author Doug Jackson
// doug.jackson@noaa.gov
public class SmartChannel extends Channel {
	// Class variables
	public static int tideCountThr;
	public float velFilterK;

	// Instance variables
	public float velIntegrator;
	public float previousFilteredVel;
	public float previousFilteredVelChange;
	public int tideCount;
	public float channelDir;
	public LinkedList<Float> vel;
	public int velIndex;
	public double signalToNoise;
	public float curvature;
	public String curvHead;
	public int bendDir;

	// Constructor
	public SmartChannel(int nId, int gnId, int[] xSIds, float len, int[] nodeIds, float[] xSectDist) {
		super(nId, gnId, xSIds, len, nodeIds, xSectDist);

		velIntegrator = 0.0f;
		previousFilteredVel = -999.0f;
		previousFilteredVelChange = 0.0f;
		tideCount = 0;
		channelDir = 1.0f;

		// A filter constant to avoid false detections of slack tides
		velFilterK = 0.1f;

		// A linked list to store the velocities
		vel = new LinkedList<Float>();

		// Initialize signalToNoise to a large value so the fish don't get confused right away
		signalToNoise = 999;

	}

	// Instance methods
	public void updateChannelDir() {
		float filteredVel;
		float filteredVelChange;

		// Variables to store output of updateChannelParameters (depends on side effects)
		float[] cL = new float[1];
		float[] cW = new float[1];
		float[] cD = new float[1];
		float[] cV = new float[1];
		float[] cA = new float[1];
		float[] cS = new float[1];

		// Update the channel, using tmLeft=0.0f, since this update occurs at the beginning
		// of the PTM step
		this.updateChannelParameters(getLength() * 0.5f, cL, cW, cD, cV, cA, cS, 0.0f);

		vel.add(cV[0]);

		// Initialize the previousFilteredFlow
		if (previousFilteredVel == -999.0f) {
			previousFilteredVel = vel.getLast();
		}

		filteredVel = velFilterK * vel.getLast() + (1 - velFilterK) * previousFilteredVel;

		filteredVelChange = filteredVel - previousFilteredVel;

		// Detect when the velChange, which is related to the tide, crosses zero from the negative direction
		if (filteredVelChange > 0 && previousFilteredVelChange < 0) {
			tideCount++;
		}

		// If the number of tide cycles >= tideCountThr, determine the channel's flow direction
		// and restart the integration
		if (tideCount >= tideCountThr) {
			if (velIntegrator < 0) {
				channelDir = -1.0f;
			} else {
				channelDir = 1.0f;
			}

			calcSignalToNoise();

			// Reset the integrator and velocity history
			velIntegrator = vel.getLast();
			tideCount = 0;
			vel.clear();
		} else {
			velIntegrator += vel.getLast();
		}

		// Memory for the next time
		previousFilteredVel = filteredVel;
		previousFilteredVelChange = filteredVelChange;
	}

	public float getChannelDir() {
		return channelDir;
	}

	public void calcSignalToNoise() {
		float sumVel = 0.0f, sumSquaredDiff = 0.0f;
		float meanVel;
		double stdVel;
		int numVel = vel.size();
		boolean reverseFlow = false;

		// Calculate the mean velocity
		for (float v : vel) {
			if (v < 0)
				reverseFlow = true;
			sumVel += v;
		}
		meanVel = sumVel / numVel;

		// Calculate the standard deviation
		for (float v : vel) {
			sumSquaredDiff += Math.pow((v - meanVel), 2.0);
		}
		stdVel = Math.sqrt(sumSquaredDiff / numVel);

		// Calculate the signal to noise ratio, meanVel/stdVel
		signalToNoise = Math.abs(meanVel) / stdVel;
	}

	public double getSignalToNoise() {
		return signalToNoise;
	}

	// Class methods
	public static void setTideCountThr(int t) {
		tideCountThr = t;
	}

	// VKS: Functions to get velocity at the particle location written here not to interfere with
	// channel.java
	public float getVel(float xPos, float yPos, float zPos, float averageVelocity, float width, float depth) {
		float vp, tp;
		float yfrac;

		// If the particle's location is undefined, return NaN (Note that this will cause the program to exit)
		if (Float.isNaN(xPos) || Float.isNaN(yPos) || Float.isNaN(zPos)) {
			return Float.NaN;
		}

		// Vertical velocity function value at the point
		vp = callCalcVertProfile(zPos, depth); // From channel.java

		// Vertical velocity function value at the point
		yfrac = 2.0f * yPos / width;

		tp = RiverBendsInput.getLatPosVal(yfrac, curvHead, bendDir, 0);

		// Total velocity
		return (averageVelocity * vp * tp);
	}

	public float getVel(float xPos, float yPos, float zPos, float tmLeft) {
		float v;
		float width;
		float vp, tp;
		float yfrac;

		// Average velocity
		v = getAverageVelocity(xPos, tmLeft);

		// Vertical velocity function value at the point
		vp = callCalcVertProfile(zPos, getDepth(xPos, tmLeft));

		// Getting to the lookup table
		width = getWidth(xPos, tmLeft);
		yfrac = 2.0f * yPos / width;
		tp = RiverBendsInput.getLatPosVal(yfrac, curvHead, bendDir, 0);

		// Total velocity
		return (v * vp * tp);
	}

	public void updateChannelOrientBend() {
		curvature = (float) RiverBendsInput.getChannelOrientBend(this.getEnvIndex(), "Curvature");
		curvHead = LatData.convertRadToHeader(curvature);
		bendDir = (int) RiverBendsInput.getChannelOrientBend(this.getEnvIndex(), "BendDir");
	}

}
