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

//$Id: Waterbody.java,v 1.3.6.2 2006/01/27 19:52:24 eli2 Exp $
package DWR.DMS.PTM;

import edu.cornell.RngPack.*;

/**
 * Waterbody is an abstract entity which is connected to other waterbodies via nodes. Each Waterbody is identified by its unique id #. Each Waterbody
 * has a type that is specified by the subtype that creates it.
 *
 * @author Nicky Sandhu
 * @version $Id: Waterbody.java,v 1.3.6.2 2006/01/27 19:52:24 eli2 Exp $
 */
public abstract class Waterbody {

	/**
	 *
	 */
	public static final int RESERVOIR = 101;
	/**
	 *
	 */
	public static final int CHANNEL = 100;
	/**
	 *
	 */
	public static final int SINK = 104;
	/**
	 *
	 */
	public static final int BOUNDARY_WATERBODY = 105;
	/**
	 *
	 */
	public static final int CONVEYOR = 106;
	/**
	 *
	 */
	public static final int BOUNDARY = BOUNDARY_WATERBODY;
	/**
	 *
	 */
	public static final int OUTFLOW = -1;
	/**
	 *
	 */
	public static final int INFLOW = 1;
	/**
	 *
	 */
	public static final int NULL = -10001;

	public static double PTMtimeStep = 15; 

	/**
	 *
	 */
	public int INITIAL_SEED = 10000;

	/**
	 * Construct an empty channel with no nodes
	 */
	public Waterbody() {
		this(CHANNEL, 0, null);
	}


	/**
	 * Construct a Waterbody with the given type and a given number of nodes
	 */
	public Waterbody(int wtype, int nId, int[] nodeIds) {
		type = wtype;
		EnvIndex = nId;
		// numberId = nId;
		// set # of nodes and nodeArray
		if (nodeIds != null) {
			nNodes = nodeIds.length;
			nodeIdArray = nodeIds;
			nodeArray = new Node[nNodes];
			depthAt = new float[nNodes][2];
			flowAt = new float[nNodes][2];
			qualityAt = new float[nNodes][getNumConstituents()];
		}
		if (randomNumberGenerator == null)
			randomNumberGenerator = new Ranecu(INITIAL_SEED);
	}

	/**
	 * Get the signed flow into a particular Node from the view of current Waterbody used for particle node decision Node is identified by its local
	 * index Sign convention: Inflows into Node - positive (no sign change) Outflows from Node - negative (sign change)
	 */
	public final float getFlowInto(int nodeId, float tmLeft) {
		float flow;

		float hydroInterpFac = calcHydroInterpFac(tmLeft);

		flow = flowAt[nodeId][0] + (flowAt[nodeId][1] - flowAt[nodeId][0]) * hydroInterpFac;

		// float flow=0.0f;
		if (flowType(nodeId) == OUTFLOW)
			return (flow);
		else
			return (-flow);
	}

	/**
	 * Get flow direction sign OUTFLOW & INFLOW do not represent their physical meanings i.e. intermediate var for particle decision making at
	 * junction node
	 */
	public abstract int flowType(int nodeId);

	/**
	 * Get the type from particle's point of view
	 */
	public int getPTMType() {
		return getType();
	}

	/**
	 * Return actual type of Waterbody
	 */
	public int getType() {
		return type;
	}

	/**
	 * The type of Waterbody from a hydrodynamic point of view
	 */
	public abstract int getHydroType();

	/**
	 * Get this Waterbody's unique id
	 */
	public final int getEnvIndex() {
		return (EnvIndex);
	}

	/**
	 * Set pointer information in Node pointer array
	 */
	final void setNodeArray(Node[] nodePtrArray) {
		for (int i = 0; i < nNodes; i++) {
			nodeArray[i] = nodePtrArray[i];
		}
	}

	/**
	 * Get the local index of a Node (ie. within the indexing system of this Waterbody, from 0 to nNodes-1) from the its global index
	 */
	public final int getNodeLocalIndex(int nodeIdGlobal) {
		int nodeIdLocal = -1;
		boolean notFound = true;
		int id = 0;
		while (id < nNodes && notFound) {
			if (nodeIdArray[id] == nodeIdGlobal) {
				nodeIdLocal = id;
				notFound = false;
			}
			id++;
		}
		// if (notFound)
		// throw new nodeNotFoundException(" Exception in " + this.toString());
		return nodeIdLocal;
	}

	/**
	 * Get the Node's global index by its local index
	 */
	// public final int getNodeId(int nodeId){
	// return(nodeIdArray[nodeId]);
	// }
	/**
	 * the Node's EnvIndex from its local index
	 */
	public final int getNodeEnvIndex(int localIndex) {
		return (nodeIdArray[localIndex]);
	}

	/**
	 * Generates a random number on subsequent calls which are gaussian distributed.
	 */
	public final float getGRandomNumber() {
		return ((float) randomNumberGenerator.gaussian());
	}// no use

	/**
	 * Generate a uniform random real number in (0,1)
	 */
	public final float getRandomNumber() {
		return ((float) randomNumberGenerator.uniform(0, 1));
	}

	/**
	 * Get the number of nodes connecting to this Waterbody
	 */
	public final int getNumberOfNodes() {
		return (nNodes);
	}

	/**
	 * Get a Node, given its local index within this Waterbody. To get a nodes local index use getNodeLocalIndex(int i)
	 * 
	 * @return the Node object
	 */
	public final Node getNode(int localIndex) {
		return (nodeArray[localIndex]);
	}

	/**
	 * Set flow information to given flow array.
	 */
	public final void setFlow(float[] flowArray, int timeStepIndex) {
		for (int nodeId = 0; nodeId < nNodes; nodeId++)
			flowAt[nodeId][timeStepIndex] = flowArray[nodeId];
	}

	// Calculate the hydro interpolation factor based on the current model time,
	// the PTM time step (minutes), and the amount of time left in the current PTM time step (seconds)
	public float calcHydroInterpFac(float tmLeft) {

		float hydroInterpFac = 0.0f;

		// Minutes between the start of the PTM time step and the last hydro time step
		int diffStart = Globals.currentModelTime - Globals.Environment.getLastHydroReadTime();

		// Minutes between the actual current timeand the last hydro time step
		double diffActual = (double) diffStart + (PTMtimeStep - (double) tmLeft / 60.0);

		hydroInterpFac = (float) (diffActual / (double) Globals.Environment.getHydroStepSize());
		hydroInterpFac = Math.max(0.0f, Math.min(1.0f, hydroInterpFac));

		return hydroInterpFac;

	}

	/**
	 * sets the accounting name
	 * 
	 * public void setAccountingType(int type){ _aType = type; } /** Returns the accounting name
	 * 
	 * public int getAccountingType(){ return _aType; }
	 */
	/**
	 * sets the object number
	 */
	public void setObjectType(int type) {
		_oType = type;
	}

	/**
	 * Returns the object number
	 */
	public int getObjectType() {
		return _oType;
	}

	/**
	 * sets the group number
	 */
	public void setGroup(int group) {
		_group = group;
		// System.out.println(numberId+", group "+group);
	}

	/**
	 * Returns the group number
	 */
	public int getGroup() {
		return _group;
	}

	/**
	 * returns upstream water quality for constituent
	 */
	public float getQuality(int nodeNum, int constituent) {
		return qualityAt[nodeNum][constituent];
	}

	/**
	 * returns number of available quality constituents
	 */
	public int getNumConstituents() {
		return PTMFixedData.getQualConstituentNames().length;
	}

	/**
	 * String representation
	 */
	public String toString() {
		String rep = " ";
		if (this != null) {
			String typeRep = null;
			if (this.type == CHANNEL)
				typeRep = "CHANNEL";
			if (this.type == RESERVOIR)
				typeRep = "RESERVOIR";
			if (this.type == BOUNDARY_WATERBODY)
				typeRep = "BOUNDARY_WATERBODY";
			if (this.type == CONVEYOR)
				typeRep = "CONVEYOR";
			rep = " Waterbody # " + this.EnvIndex + "\n" + " Number of Nodes = " + this.nNodes + "\n" + " Waterbody Type is " + typeRep + "\n";
			for (int i = 0; i < nNodes; i++) {
				if (nodeArray[i] != null)
					rep += "Node[" + i + "] = " + nodeArray[i].getEnvIndex();
				else
					rep += "Node[" + i + "] = null";
			}
		}
		return rep;
	}

	/**
	 * Index in PTMEnv Waterbody array
	 */
	private int EnvIndex;
	/**
	 * Id of array in the DSM2 hydro grid
	 */
	// private int numberId;
	/**
	 * Type of Waterbody channel/reservoir/boundary/conveyor
	 */
	private int type;
	/**
	 * Number of nodes connecting to this Waterbody
	 */
	private int nNodes;
	/**
	 * Indices array of Nodes connecting to this Waterbody
	 */
	private int[] nodeIdArray;
	/**
	 * Node array connecting to this Waterbody
	 */
	private Node[] nodeArray;

	/**
	 * Flow, depth, velocity, width and area information read from tide file
	 */
	protected float[][] flowAt, depthAt, widthAt;
	/**
	 * Water quality information read from Qual binary file
	 */
	protected float[][] qualityAt;
	/**
	 * Gaussian random number generator
	 */
	private RandomElement randomNumberGenerator;

	/**
	 * The object name
	 */
	private int _oType;
	/**
	 * The group number
	 */
	private int _group;
}
