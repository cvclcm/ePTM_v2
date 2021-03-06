/*<license>
C!    Copyright (C) 1996, 1997, 1998, 2001, 2007, 2009 State of California,
C!    Department of Water Resources.
C!    This file is part of DSM2.

C!    DSM2 is free software: you can redistribute it and/or modify
C!    it under the terms of the GNU General Public !<license as published by
C!    the Free Software Foundation, either version 3 of the !<license, or
C!    (at your option) any later version.

C!    DSM2 is distributed in the hope that it will be useful,
C!    but WITHOUT ANY WARRANTY; without even the implied warranty of
C!    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
C!    GNU General Public !<license for more details.

C!    You should have received a copy of the GNU General Public !<license
C!    along with DSM2.  If not, see <http://www.gnu.org/!<licenses/>.
</license>*/
package DWR.DMS.PTM;

/**
 * Channel is a Waterbody which has two nodes and a direction of flow between those nodes. In addition it has a length and other properties such as
 * cross-sections.
 *
 * @author Nicky Sandhu
 * @version $Id: Channel.java,v 1.4.6.1 2006/04/04 18:16:24 eli2 Exp $
 */
public class Channel extends Waterbody {
	/**
	 * a constant for vertical velocity profiling.
	 */
	public final static float VONKARMAN = 0.4f;
	/**
	 * local index of up stream node
	 */
	public final static int UPNODE = 0;
	/**
	 * local index of down stream node
	 */
	public final static int DOWNNODE = 1;
	/**
	 * a constant defining the resolution of velocity profile
	 */
	public final static int MAX_PROFILE = 1000;
	/**
	 * an array of coefficients for profile
	 */
	public static float[] vertProfile = new float[Channel.MAX_PROFILE];
	/**
	 * use vertical profile
	 */
	public static boolean useVertProfile;
	/**
	 * use transverse profile
	 */
	public static boolean useTransProfile;

	/**
	 * sets fixed information for Channel
	 */
	public Channel(int nId, int gnId, int[] xSIds, float len, int[] nodeIds, float[] xSectDist) {

		super(Waterbody.CHANNEL, nId, nodeIds);
		length = len;
		// set #of xSections and the idArray
		nXsects = xSIds.length;
		xSArray = new XSection[nXsects];
		xSectionIds = xSIds;
		xSectionDistance = xSectDist;

		// Arrays to hold current width, area, depth, and stage
		widthAt = new float[getNumberOfNodes()][2];
		areaAt = new float[getNumberOfNodes()][2];
		depthAt = new float[getNumberOfNodes()][2];
		stageAt = new float[getNumberOfNodes()][2];

		// Arrays to hold the width, area, depth, and stage at the next time step

	}

	/**
	 * Gets the length of Channel
	 */
	public final float getLength() {
		return (length);
	}

	/**
	 * Gets the width of the Channel at that particular x position
	 */
	public final float getWidth(float xPos, float tmLeft) {
		// Widths calculated at the flanking hydro time steps
		float width0, width1;

		float hydroInterpFac = calcHydroInterpFac(tmLeft);

		float alfx = xPos / length;

		width0 = (alfx * widthAt[1][0] + (1 - alfx) * widthAt[0][0]);
		width1 = (alfx * widthAt[1][1] + (1 - alfx) * widthAt[0][1]);
		return width0 + (width1 - width0) * hydroInterpFac;
	}

	/**
	 * Gets the depth of the Channel at that particular x position
	 */
	public final float getDepth(float xPos, float tmLeft) {
		// Depths calculated at the flanking hydro time steps
		float depth0, depth1;

		float hydroInterpFac = calcHydroInterpFac(tmLeft);

		float alfx = xPos / length;

		depth0 = alfx * depthAt[DOWNNODE][0] + (1 - alfx) * depthAt[UPNODE][0];
		depth1 = alfx * depthAt[DOWNNODE][1] + (1 - alfx) * depthAt[UPNODE][1];

		return depth0 + (depth1 - depth0) * hydroInterpFac;
	}

	/**
	 * Gets the depth of the Channel at that particular x position
	 */
	public final float getStage(float xPos, float tmLeft) {
		// Stages calculated at the flanking hydro time steps
		float stage0, stage1;

		float hydroInterpFac = calcHydroInterpFac(tmLeft);

		float alfx = xPos / length;

		stage0 = alfx * stageAt[DOWNNODE][0] + (1 - alfx) * stageAt[UPNODE][0];
		stage1 = alfx * stageAt[DOWNNODE][1] + (1 - alfx) * stageAt[UPNODE][1];
		return stage0 + (stage1 - stage0) * hydroInterpFac;
	}

	/**
	 * Gets the velocity of water in Channel at that particular x,y,z position
	 */
	public final float getVelocity(float xPos, float yPos, float zPos, float tmLeft) {
		// returns v >= sign(v)*0.001
		float v = getAverageVelocity(xPos, tmLeft);
		// calculate vertical/ transverse profiles if needed..
		float vp = 1.0f, tp = 1.0f;
		if (useVertProfile)
			vp = calcVertProfile(zPos, getDepth(xPos, tmLeft));
		if (useTransProfile)
			tp = calcTransProfile(yPos, getWidth(xPos, tmLeft));

		return (v * vp * tp);
	}

	/**
	 * A more efficient calculation of velocity if average velocity, width and depth has been pre-calculated.
	 */
	public final float getVelocity(float xPos, float yPos, float zPos, float averageVelocity, float width, float depth) {
		float vp = 1.0f, tp = 1.0f;
		if (useVertProfile)
			vp = calcVertProfile(zPos, depth);
		if (useTransProfile)
			tp = calcTransProfile(yPos, width);

		return (averageVelocity * vp * tp);
	}

	/**
	 * Calculate the flow without interpolating between hydro time steps. This is used only when inferring the hydro time step size.
	 */
	public final float getFlow(float xPos) {
		float alfx = xPos / length;

		return alfx * flowAt[DOWNNODE][0] + (1 - alfx) * flowAt[UPNODE][0];
	}

	/**
	 * the flow at that particular x position
	 */
	public final float getFlow(float xPos, float tmLeft) {
		// Flows at the flanking hydro time steps
		float flow0, flow1;

		float hydroInterpFac = calcHydroInterpFac(tmLeft);

		float alfx = xPos / length;

		flow0 = alfx * flowAt[DOWNNODE][0] + (1 - alfx) * flowAt[UPNODE][0];
		flow1 = alfx * flowAt[DOWNNODE][1] + (1 - alfx) * flowAt[UPNODE][1];
		return flow0 + (flow1 - flow0) * hydroInterpFac;
	}

	/**
	 * Gets the type from particle's point of view
	 */
	@Override
	public int getPTMType() {
		return Waterbody.CHANNEL;
	}

	/**
	 * Returns the hydrodynamic type of Channel
	 */
	public int getHydroType() {
		return FlowTypes.channell;
	}

	/**
	 * Gets the EnvIndex of the upstream node
	 */
	public final int getUpNodeId() {
		return (getNodeEnvIndex(UPNODE));
	}

	/**
	 * Gets the EnvIndex of the down node
	 */
	public final int getDownNodeId() {
		return (getNodeEnvIndex(DOWNNODE));
	}

	/**
	 * Gets the Transverse velocity A coefficient
	 */
	public float getTransverseACoef() {
		return Globals.Environment.pInfo.getTransverseACoef();
	}

	/**
	 * Gets the Transverse velocity B coefficient
	 */
	public float getTransverseBCoef() {
		return Globals.Environment.pInfo.getTransverseBCoef();
	}

	/**
	 * Gets the Transverse velocity A coefficient
	 */
	public float getTransverseCCoef() {
		return Globals.Environment.pInfo.getTransverseCCoef();
	}

	/**
	 * Return flow direction sign OUTFLOW->Positive (no sign change) if node is upstream node INFLOW->Negative (sign change) if downstream node
	 */
	public int flowType(int nodeId) {
		if (nodeId == UPNODE)
			return OUTFLOW;
		else if (nodeId == DOWNNODE)
			return INFLOW;
		else {
			throw new IllegalArgumentException();
		}
	}

	/**
	 * vertical profile multiplier
	 */
	private final float calcVertProfile(float z, float depth) {
		float zfrac = z / depth * (MAX_PROFILE - 1);
		return Math.max(0.0f, vertProfile[(int) zfrac]);
	}

	// VKS: Public method to access calcVertProfile from SmartChannel.java
	public final float callCalcVertProfile(float z, float depth) {
		float vp;
		vp = calcVertProfile(z, depth);
		return vp;
	}

	/**
	 * transverse profile multiplier
	 */
	public final float calcTransProfile(float y, float width) {
		float yfrac = 2.0f * y / width;
		float yfrac2 = yfrac * yfrac;
		float a = getTransverseACoef();
		float b = getTransverseBCoef();
		float c = getTransverseCCoef();
		return a + b * yfrac2 + c * yfrac2 * yfrac2; // quartic profile across Channel width
	}

	/**
	 * returns the number of cross sections
	 */
	public final int getNumberOfXSections() {
		return (nXsects);
	}

	/**
	 * Gets the EnvIndex of cross sections given the local index of the cross section
	 */
	public final int getXSectionEnvIndex(int localIndex) {
		return (xSectionIds[localIndex]);
	}

	/**
	 * Sets pointer information for XSection pointer array
	 */
	public final void setXSectionArray(XSection[] xSPtrArray) {
		for (int i = 0; i < nXsects; i++) {
			xSArray[i] = xSPtrArray[i];
			// fill up regular XSection with additional information
			if (xSArray[i].isIrregular() == false) {
				xSArray[i].setDistance(xSectionDistance[i]);
				xSArray[i].setChannelNumber(getEnvIndex());
			} // end if
		} // end for
		// sort by ascending order of distance...
		sortXSections();
	}

	/**
	 * Returns a pointer to specified XSection
	 */
	public final XSection getXSection(int localIndex) {
		return (xSArray[localIndex]);
	}

	/**
	 * Set depth information
	 */
	public final void setDepth(float[] depthArray, int timeStepIndex) {
		depthAt[UPNODE][timeStepIndex] = depthArray[0];
		depthAt[DOWNNODE][timeStepIndex] = depthArray[1];

		if (Globals.currentModelTime == Globals.Environment.getStartTime()) {
			depthAt[UPNODE][timeStepIndex] = depthAt[UPNODE][timeStepIndex] / 0.6f;
			depthAt[DOWNNODE][timeStepIndex] = depthAt[DOWNNODE][timeStepIndex] / 0.6f;
		}
	}

	/**
	 * Set depth information
	 */
	public final void setStage(float[] stageArray, int timeStepIndex) {
		stageAt[UPNODE][timeStepIndex] = stageArray[0];
		stageAt[DOWNNODE][timeStepIndex] = stageArray[1];

		if (Globals.currentModelTime == Globals.Environment.getStartTime()) {
			stageAt[UPNODE][timeStepIndex] = stageAt[UPNODE][timeStepIndex] / 0.6f;
			stageAt[DOWNNODE][timeStepIndex] = stageAt[DOWNNODE][timeStepIndex] / 0.6f;
		}
	}

	/**
	 * Set area information
	 */
	public final void setArea(float[] areaArray, int timeStepIndex) {
		areaAt[UPNODE][timeStepIndex] = areaArray[0];
		areaAt[DOWNNODE][timeStepIndex] = areaArray[1];

		if (Globals.currentModelTime == Globals.Environment.getStartTime()) {
			areaAt[UPNODE][timeStepIndex] = areaAt[UPNODE][timeStepIndex] / 0.6f;
			areaAt[DOWNNODE][timeStepIndex] = areaAt[DOWNNODE][timeStepIndex] / 0.6f;
		}
		widthAt[UPNODE][timeStepIndex] = areaAt[UPNODE][timeStepIndex] / depthAt[UPNODE][timeStepIndex];
		widthAt[DOWNNODE][timeStepIndex] = areaAt[DOWNNODE][timeStepIndex] / depthAt[DOWNNODE][timeStepIndex];
	}

	/**
	 * Get average velocity
	 */
	public final float getAverageVelocity(float xPos, float tmLeft) {
		float[] velocityAt = new float[2];

		int upX = 0, downX = 1;
		downX = getDownSectionId(xPos);
		upX = downX - 1;
		float v;
		float alfx = xPos / length;

		// Flows at the upNode and downNode
		float flowUpNode, flowDownNode;

		float hydroInterpFac = calcHydroInterpFac(tmLeft);

		flowUpNode = flowAt[upX][0] + (flowAt[upX][1] - flowAt[upX][0]) * hydroInterpFac;
		flowDownNode = flowAt[downX][0] + (flowAt[downX][1] - flowAt[downX][0]) * hydroInterpFac;

		velocityAt[upX] = calcVelocity(flowUpNode, 0, tmLeft);
		velocityAt[downX] = calcVelocity(flowDownNode, length, tmLeft);
		v = alfx * velocityAt[downX] + (1 - alfx) * velocityAt[upX];

		// ? what if velocity is negative due to negative flows??
		if (v != 0)
			return v / Math.abs(v) * Math.max(0.001f, Math.abs(v));
		else
			return 0.001f;
	}

	/**
	 * Get the flow area
	 */
	public final float getFlowArea(float xpos, float tmLeft) {
		return getDepth(xpos, tmLeft) * getWidth(xpos, tmLeft);
	}

	/**
	 * An efficient way of calculating all Channel parameters i.e. length, width, depth, average velocity and area all at once.
	 */
	public final void updateChannelParameters(float xPos, float[] channelLength, float[] channelWidth, float[] channelDepth, float[] channelVave,
			float[] channelArea, float[] channelStage, float tmLeft) {
		channelLength[0] = this.length;

		float alfx = xPos / this.length;
		float nalfx = 1.0f - alfx;

		// Temporary variables to hold interpolated values at the upNode and downNode
		float depthUpNode, depthDownNode, widthUpNode, widthDownNode, stageUpNode, stageDownNode, flowUpNode, flowDownNode;

		float hydroInterpFac = calcHydroInterpFac(tmLeft);

		depthUpNode = depthAt[UPNODE][0] + (depthAt[UPNODE][1] - depthAt[UPNODE][0]) * hydroInterpFac;
		depthDownNode = depthAt[DOWNNODE][0] + (depthAt[DOWNNODE][1] - depthAt[DOWNNODE][0]) * hydroInterpFac;
		channelDepth[0] = alfx * depthDownNode + nalfx * depthUpNode;

		widthUpNode = widthAt[UPNODE][0] + (widthAt[UPNODE][1] - widthAt[UPNODE][0]) * hydroInterpFac;
		widthDownNode = widthAt[DOWNNODE][0] + (widthAt[DOWNNODE][1] - widthAt[DOWNNODE][0]) * hydroInterpFac;
		channelWidth[0] = alfx * widthDownNode + nalfx * widthUpNode;

		channelArea[0] = channelDepth[0] * channelWidth[0];

		stageUpNode = stageAt[UPNODE][0] + (stageAt[UPNODE][1] - stageAt[UPNODE][0]) * hydroInterpFac;
		stageDownNode = stageAt[DOWNNODE][0] + (stageAt[DOWNNODE][1] - stageAt[DOWNNODE][0]) * hydroInterpFac;
		channelStage[0] = alfx * stageDownNode + nalfx * stageUpNode;

		flowUpNode = flowAt[UPNODE][0] + (flowAt[UPNODE][1] - flowAt[UPNODE][0]) * hydroInterpFac;
		flowDownNode = flowAt[DOWNNODE][0] + (flowAt[DOWNNODE][1] - flowAt[DOWNNODE][0]) * hydroInterpFac;

		float Vave = (alfx * flowDownNode + nalfx * flowUpNode) / channelArea[0];

		if (Vave > -0.001 & Vave < 0.0) {
			Vave = -0.001f;
		}
		if (Vave >= 0.0 & Vave < 0.001) {
			Vave = 0.001f;
		}

		channelVave[0] = Vave;
	}

	/**
	 * calculate profile
	 */
	public final static void constructProfile() {
		vertProfile[0] = (float) (1.0f + 0.1f * (1.0f + Math.log((0.01f) / MAX_PROFILE)) / VONKARMAN);
		for (int i = 1; i < MAX_PROFILE; i++)
			vertProfile[i] = (float) (1.0f + (0.1f / VONKARMAN) * (1.0f + Math.log(((float) i) / MAX_PROFILE)));
	}

	/**
	 * Number of cross sections
	 */
	private int nXsects;
	/**
	 * Array of XSection object indices contained in this Channel
	 */
	private int[] xSectionIds;
	/**
	 * Pointers to XSection objects contained in this Channel
	 */
	private XSection[] xSArray;
	/**
	 * Array containing distance of cross sections from upstream end
	 */
	private float[] xSectionDistance;
	/**
	 * Length of Channel
	 */
	private float length;
	/**
	 * Area of Channel/reservoir
	 */
	private float area;
	/**
	 * Flow, depth, velocity, width and area information read from tide file
	 */
	private float[][] areaAt;
	private float[][] depthAt;
	private float[][] stageAt;

	/**
	 * Bottom elevation of Channel or reservoir
	 */
	private float bottomElevation;

	// private static final float a=1.62f,b=-2.22f,c=0.60f;

	private final float calcVelocity(float flow, float xpos, float tmLeft) {
		return flow / getFlowArea(xpos, tmLeft);
	}

	/**
	 *  
	 */
	private final int getDownSectionId(float xPos) {
		// check distance vs x till distance of XSection > xPos
		// that XSection mark it as downX and the previous one as upX
		boolean notFound = true;
		int sectionNumber = -1;

		while ((sectionNumber < nXsects) && notFound) {
			sectionNumber++;
			if (Macro.APPROX_EQ(xPos, 0.0f)) {
				sectionNumber = 1;
				notFound = false;
			}
			if (Macro.APPROX_EQ(xPos, length)) {
				sectionNumber = nXsects - 1;
				notFound = false;
			}
			if (xPos < xSArray[sectionNumber].getDistance()) {
				notFound = false;
			}
		} // end while
		return (sectionNumber);
	}

	private final void sortXSections() {
		int i, j;
		float currentSection;
		XSection xSPtr;
		boolean Inserted = false;

		for (j = 1; j < nXsects; j++) {
			currentSection = xSArray[j].getDistance();
			xSPtr = xSArray[j];
			i = j - 1;
			Inserted = false;

			while (i >= 0 && !Inserted) {
				if (xSArray[i].getDistance() <= currentSection) {
					Inserted = true;
					xSArray[i + 1].setDistance(currentSection);
					xSArray[i + 1] = xSPtr;
				} // end if
				else {
					xSArray[i + 1].setDistance(xSArray[i].getDistance());
					xSArray[i + 1] = xSArray[i];
				} // end else
				i--;
			} // end while
			if (!Inserted) {
				xSArray[0].setDistance(currentSection);
				xSArray[0] = xSPtr;
			}
		} // end for
	}
}
