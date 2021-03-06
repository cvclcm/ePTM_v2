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

import java.io.IOException;
import edu.cornell.RngPack.*;
import java.util.HashMap;

/**
 * 
 * Node
 * 
 * Node is defined as the connection between two or more waterbodies. Node handles connection information as well as constructing outflow from/to kind
 * of information
 * <p>
 * 
 * FUTURE DIRECTIONS
 * <p>
 */

public class Node {
	private RandomElement randomNumberGenerator;
	private static int INITIAL_SEED = 10000;
	private int numChannels;

	/**
	 * Node constructor
	 */
	public Node(int nId, int[] wbIdArray, String bdType) {
		EnvIndex = nId;
		// set the number of waterbodies node connected, number upstream /
		// downstream channels
		// ? This is number of channels only.. add ag. drains, pumps, reservoirs
		// ? later as that would information would have to be extracted from
		// ? reservoirs, pumps, and ag. drains.
		numberOfWaterbodies = wbIdArray.length;
		// create arrays to store index of array of waterbodies in PTMEnv
		// ? This will later be converted to pointer information.. ie. an
		// ? array of pointers to waterbodies..
		wbIndexArray = new int[numberOfWaterbodies];
		wbArray = new Waterbody[numberOfWaterbodies];
		for (int i = 0; i < numberOfWaterbodies; i++) {
			wbIndexArray[i] = wbIdArray[i];
		}
		boundaryType = bdType;
		randomNumberGenerator = new Ranecu(INITIAL_SEED);
		filterArr = new HashMap<String, Filter>();

		// -999 indicates that numChannels hasn't been calculated yet
		numChannels = -999;
	}

	/**
	 * simpler Node initializer
	 */
	public Node(int nId) {
		EnvIndex = nId;
		numberOfWaterbodies = 0;
		LABoundaryType = -1;
	}

	/**
	 * Clean up only if initialized
	 */

	/**
	 * Returns the next random number using drand48
	 */
	public final float getRandomNumber() {
		return (float) randomNumberGenerator.uniform(0, 1);
	}

	/**
	 * Returns number of waterbodies node connected
	 */
	public final int getNumberOfWaterbodies() {
		return (numberOfWaterbodies);
	}

	public int getNumChannels() {
		// Calculate the number of channels if it hasn't been done yet
		if (numChannels == -999) {
			numChannels = 0;
			for (int i = 0; i < numberOfWaterbodies; i++) {
				// Increment numChannels if this waterbody is a channel
				if (wbArray[i].getType() == 100) {
					numChannels++;
				}
			}
		}

		return numChannels;
	}

	/**
	 * Returns an integer pointing to the number Id of the Waterbody
	 */
	public final int getWaterbodyId(int id) {
		return (wbArray[id].getEnvIndex());
	}

	/**
	 * Returns a pointer to desired Waterbody
	 */
	public final Waterbody getWaterbody(int id) {
		return (wbArray[id]);
	}

	/**
	 * Checks to see if junction is a dead end.
	 */
	public final boolean isJunctionDeadEnd() {
		return (false);
	}

	/**
	 * Get total positive outflow from Node.. add up all flows leaving the Node NOT USED; replaced by getTotalEffectiveOutflow
	 */
	public final float getTotalOutflow(boolean addSeep, float tmLeft) {
		float outflow = 0.0f;
		// System.out.println(this.toString());
		// for each Waterbody connected to junction add the outflows
		for (int id = 0; id < numberOfWaterbodies; id++) {
			if (!addSeep) { // @todo: hobbled seep feature
				// if(getWaterbody(id).getAccountingType() != flowTypes.evap){
				outflow += getOutflow(id, tmLeft);
				// }
			} else {
				outflow += getOutflow(id, tmLeft);
			}
		}
		return (outflow);
	}

	/**
	 * Get total positive outflow from Node.. add up all flows leaving the Node multiplied by a filter operation for each out-node filter for particle
	 * decision making when facing multiple waterbodies outflow from one node
	 */
	public final float getTotalEffectiveOutflow(boolean addSeep, float tmLeft) {
		float outflow = 0.0f;
		// System.out.println(this.toString());
		// for each Waterbody connected to junction add the outflows
		for (int id = 0; id < numberOfWaterbodies; id++) {
			if (!addSeep) { // @todo: hobbled seep feature
				// if(getWaterbody(id).getAccountingType() != flowTypes.evap){
				outflow += getFilterOp(id) * getOutflow(id, tmLeft);
				// }
			} else {
				outflow += getFilterOp(id) * getOutflow(id, tmLeft);
			}
		}
		return (outflow);
	}

	/**
	 * get the operation for the specified filter at current timestamp used to modify the outflows' weights for particle decision making
	 */
	public float getFilterOp(int localChannelId) {
		float filterOp = 1.0f;
		int wbEnvIndex = this.getWaterbodyEnvIndex(localChannelId);
		String key = "" + getEnvIndex() + "," + "" + wbEnvIndex;

		if (filterArr.containsKey(key)) {
			filterOp = getFilter(wbEnvIndex).getFilterOp();
		}
		return filterOp;
	}

	/**
	 * existence of particle filter for node inflows called by Particle x-position calculation when entering a node
	 */
	public boolean inFilter(Waterbody wb) {
		boolean filterIn = false;
		int wbEnvIndex = wb.getEnvIndex();
		String key = "" + EnvIndex + "," + "" + wbEnvIndex;

		if (filterArr.containsKey(key)) {
			float filterOp = getFilter(wbEnvIndex).getFilterOp();
			if (filterOp == 0) {
				filterIn = true;
			}
		}
		return filterIn;
	}

	/**
	 * Returns the filter with the specified node and wb (exist judgment first)
	 */
	public Filter getFilter(int wbEnvIndex) {
		String key = "" + EnvIndex + "," + "" + wbEnvIndex;
		return filterArr.get(key);
	}

	/**
	 * Gets the outflow (positive) to a particular Waterbody from this Node. It returns a zero for negative outflow or inflow
	 */
	public final float getOutflow(int id, float tmLeft) {
		// get out flow from junction, returns 0 if negative
		return Math.max(0.0f, getSignedOutflow(id, tmLeft));
	}

	public final float getSignedOutflow(int id, float tmLeft) {
		int junctionIdInWaterbody = 0;
		junctionIdInWaterbody = wbArray[id].getNodeLocalIndex(EnvIndex);
		if (junctionIdInWaterbody == -1)
			System.out.println("Exception thrown in node " + this.toString());
		return wbArray[id].getFlowInto(junctionIdInWaterbody, tmLeft);
	}

	/**
	 * Returns the index to the Node array in PTMEnv
	 */
	public final int getEnvIndex() {
		return (EnvIndex);
	}

	/**
	 * Returns the index of Waterbody in PTMEnv array using local index
	 */
	public final int getWaterbodyEnvIndex(int localIndex) {
		return (wbIndexArray[localIndex]);
	}

	/**
	 * Fills the wbArray with pointer information and cleans up the index array
	 */
	public final void setWbArray(Waterbody[] wbPtrArray) {
		for (int i = 0; i < numberOfWaterbodies; i++) {
			wbArray[i] = wbPtrArray[i];
		}
		cleanUp();
	}

	/**
	 * Fills the node with filters' array
	 */
	public final void setFilterArr(String key, Filter filter) {
		this.filterArr.put(key, filter);
	}

	/**
	 * Adds Waterbody of given PTMEnv index to Node wbIndexArray. These operations should be done prior to calling setWbArray
	 */
	public final void addWaterbodyId(int envIndex) {

		if (numberOfWaterbodies > 0) {
			// store Waterbody indices and types in temporary arrays...
			int[] indexArray = new int[numberOfWaterbodies + 1];
			for (int i = 0; i < numberOfWaterbodies; i++) {
				indexArray[i] = wbIndexArray[i];
			}

			// delete the memory for these arrays
			wbIndexArray = null;
			wbArray = null;
			// increment the number of waterbodies
			numberOfWaterbodies++;

			// reallocate bigger chunks of memory
			wbIndexArray = new int[numberOfWaterbodies];
			wbArray = new Waterbody[numberOfWaterbodies];

			// fill them up again..
			for (int i = 0; i < numberOfWaterbodies - 1; i++) {
				wbIndexArray[i] = indexArray[i];
			}
			wbIndexArray[numberOfWaterbodies - 1] = envIndex;
			indexArray = null;
		} else {
			numberOfWaterbodies = 1;
			wbIndexArray = new int[numberOfWaterbodies];
			wbArray = new Waterbody[numberOfWaterbodies];
			wbIndexArray[numberOfWaterbodies - 1] = envIndex;
		}
	}

	/**
	 * String representation
	 */
	public String toString() {
		String rep = null;
		if (this != null) {
			rep = " Node # " + this.EnvIndex + "\n" + " Number of Waterbodies = " + this.numberOfWaterbodies + "\n";
			for (int i = 0; i < getNumberOfWaterbodies(); i++)
				rep += " Waterbody # " + i + " EnvIndex is " + wbIndexArray[i];
		}
		return rep;
	}

	/**
	 * Index to array in PTMEnv
	 */
	private int EnvIndex;

	/**
	 * Number of waterbodies
	 */
	private int numberOfWaterbodies;

	/**
	 * An array of pointers to waterbodies node connected
	 */
	private Waterbody[] wbArray;

	/**
	 * A storage for index of waterbodies in PTMEnv till wbArray gets filled.
	 */
	private int[] wbIndexArray;

	/**
	 * Boundary array as defined from fixed input. This is not needed for boundary Waterbody information as only waterbodies can be boundaries.
	 */
	private String boundaryType;

	/**
	 * Length of boundary array.
	 */
	private int LABoundaryType;

	/**
	 * deletes wbIndexArray and anything else to save space.
	 */
	private final void cleanUp() {
		// wbIndexArray = null;
	}

	/**
	 * filters' array on the specified node
	 */
	private HashMap<String, Filter> filterArr;

};
