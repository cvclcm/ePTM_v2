//VKS: Blown-up node tree for junction routing decision making
//Adapted from: https://github.com/eugenp/tutorials/blob/master/data-structures/
//              src/main/java/com/baeldung/tree/BinaryTree.java
package DWR.DMS.PTM;

import java.util.HashMap;
import java.util.Random;
import java.util.ArrayList;

public class DecisionTree {
	// Initializing the random number generator
	Random generator = new Random();

	// Defining the root node (entry channel into the junction)
	Chan root;
	HashMap<Integer, Chan> dTnodes = new HashMap<Integer, Chan>();
	HashMap<Integer, Chan> realChannels = new HashMap<Integer, Chan>();

	ArrayList<Integer> optionsTried = new ArrayList<Integer>();
	int particleID;
	String reportString;

	// Enable/disable echoing of reportString to the console
	boolean echoReportString = false;

	// Constructor
	public DecisionTree(int particleID) {
		this.particleID = particleID;
		reportString = Integer.toString(particleID);
	}

	// Method to start filling in the tree from the root
	public void add(int cP, int channelNumber, int dsChan, int flag, float Q) {
		float tempQ; 
		Chan tempChan;

		// Determine the flow value to use
		if (Math.abs(channelNumber) >= 1000) {
			tempQ = -999999999.9f;
		} else {
			tempQ = Q;
		}

		// Create the new decision tree node and save it to the hash
		tempChan = new Chan(channelNumber, flag, tempQ);
		dTnodes.put(channelNumber, tempChan);

		// Keep track of all the real channels
		if (Math.abs(channelNumber) < 1000) {
			realChannels.put(channelNumber, tempChan);
		}

		if (cP == 0) {
			// Create a new decision tree with tempChan as the root
			root = tempChan;
			reportString += "," + Integer.toString(channelNumber);
		} else {						
			// Add the channel to the appropriate node of the corresponding parent
			if (dsChan==1) {
				// Right downstream channel
				dTnodes.get(cP).right = tempChan;
			} else {
				// Left downstream channel
				dTnodes.get(cP).left = tempChan;
			}
		}
	}

	// Recursively sum flows in all children for dummy nodes
	public float recursivelySumFlows(Chan chan) {

		float rootFlow;

		// Return flow = 0.0 for undefined children
		if (chan == null) {
			return 0.0f;
		}

		// For root, spawn recursivelySumFlows but don't set chan.Q
		if (chan==root) { 
			rootFlow = recursivelySumFlows(chan.right) + recursivelySumFlows(chan.left);
			reportString += "," + Integer.toString(chan.channelNumber) + "Q" + Float.toString(chan.Q);			
			return rootFlow;
		}

		// If this is a dummy channel, add flows from right and left children
		if (Math.abs(chan.channelNumber) >= 1000) {
			chan.Q = recursivelySumFlows(chan.right) + recursivelySumFlows(chan.left);
		} 
		reportString += "," + Integer.toString(chan.channelNumber) + "Q" + Float.toString(chan.Q);
		return chan.Q;
	}

	// Method to determine which channel the particle will be pushed into
	public int pushIntoChannel(float yNonDim, ArrayList<Integer> optionsTried) {
		this.optionsTried = optionsTried;
		Chan currentWB = recursePush(root, yNonDim, "enterDecisionTree");

		if (echoReportString) {
			System.out.println(reportString);
		}

		return currentWB.channelNumber;
	}

	// Method to recursively push the particle into subsequent downstream channels according to the side of
	// the bifurcating streamline it is on until it ultimately ends up in one of the leaf nodes (exit water
	// bodies
	private Chan recursePush(Chan prevWB, float yNonDim, String decisionType) {
		boolean prevInflow, rightOutflow, leftOutflow;
		float Q, Q1, Q2;
		int qFlag = (int) Math.signum(prevWB.Q);
		int qFlag1, qFlag2 = 0;
		float bifStr;
		Integer randChannelNum;

		// Add the channel and decision to reportString
		reportString += "," + Integer.toString(prevWB.channelNumber) + "_" + decisionType;

		// Have reached a leaf node (real exit channel)
		if (prevWB.right == null && prevWB.left == null) {

			// Check if we've already attempted to enter this channel
			if (optionsTried.contains(prevWB.channelNumber)) {
				while (true) {
					randChannelNum = (Integer) realChannels.keySet().toArray()[new Random().nextInt(realChannels.size())];

					// Check if this is a gate
					if (realChannels.get(randChannelNum).Q!=0) {
						reportString += "," + Integer.toString(realChannels.get(randChannelNum).channelNumber) + "_finalRandom";
						return realChannels.get(randChannelNum);
					}
				}

			}
			reportString += "," + Integer.toString(prevWB.channelNumber) + "_final";
			return prevWB;
		} else {
			// Continue to recurse
			Q1 = Math.abs(prevWB.right.Q);
			Q2 = Math.abs(prevWB.left.Q);
			qFlag1 = (int) Math.signum(prevWB.right.Q);
			qFlag2 = (int) Math.signum(prevWB.left.Q);

			// Change sign of flow in exit channels in case we recurse into them
			prevWB.left.Q = -prevWB.left.Q;
			prevWB.right.Q = -prevWB.right.Q;

			if (qFlag == 0) {
				// There is a gate in this channel
				reportString += "," + Integer.toString(prevWB.channelNumber) + "_finalPrevWBgate";
				return prevWB;
			} else {
				// Check for gates
				if (qFlag1 == 0 && qFlag2 != 0) {
					// Gate in right-side channel
					return recursePush(prevWB.left, (float) generator.nextDouble(), "rightGate");
				} else if (qFlag2 == 0 && qFlag1 != 0) {
					// Gate in left-side channel
					return recursePush(prevWB.right, (float) generator.nextDouble(), "leftGate");
				} else if (qFlag1 == 0 && qFlag2 == 0) {
					// Gates in both channels
					reportString += "," + Integer.toString(prevWB.channelNumber) + "_finalBothGates";
					return prevWB; 
				} else {
					// No gates
					prevInflow = (qFlag == -1) ? true : false;
					rightOutflow = (qFlag1 == 1) ? true : false;
					leftOutflow = (qFlag2 == 1) ? true : false;

					// We have to push the fish into the downstream channel with exit flow when flow exits
					// in the current channel
					if (!prevInflow) {
						if (!rightOutflow && leftOutflow) {
							// Push into left channel as flow exits there
							return recursePush(prevWB.left, (float) generator.nextDouble(), "prevOutflowLeftOutflow");
						} else if (rightOutflow && !leftOutflow) {
							// Push into right channel as flow exits there
							return recursePush(prevWB.right, (float) generator.nextDouble(), "prevOutflowRightOutflow"); 
						} else {
							// Both the right and left channels have inflow => flip a coin
							return recursePush((generator.nextDouble() <= 0.5) ? prevWB.right : prevWB.left, (float) generator.nextDouble(), "prevOutflowBothInflow");
						}
					} else {
						// Estimate position of bifurcating streamline with respect to dimensionless lateral
						// position and push fish into the downstream channel accordingly

						if (rightOutflow && !leftOutflow) {
							// Flow in left-side channel is into node => bifurcating streamline precludes the left-side channel
							return recursePush(prevWB.right, (float) generator.nextDouble(), "prevInflowRightOutflow");
						} else if (!rightOutflow && leftOutflow) {
							// Flow in right-side channel is into node => bifurcating streamline precludes the right-side channel
							return recursePush(prevWB.left, (float) generator.nextDouble(), "prevInflowLeftOutflow");
						} else if (rightOutflow && leftOutflow) {
							// Right and left channels both have outflow => need to calculate the bifurcating streamline
							Q = Math.abs(prevWB.Q);

							// Downstream end is connected
							if (prevWB.flag == 1) {
								bifStr = Q1 / Q;
								return recursePush(yNonDim <= bifStr ? prevWB.right : prevWB.left, (float) generator.nextDouble(), "prevInflowBifurcDwn");
							}

							// Upstream end is connected
							if (prevWB.flag == -1) {
								bifStr = Q2 / Q;
								return recursePush(yNonDim <= bifStr ? prevWB.left : prevWB.right, (float) generator.nextDouble(), "prevInflowBifurcUp");
							}
						}
					}
				}
			}
		}
		reportString += "," + Integer.toString(prevWB.channelNumber) + "_" + "fallThrough";
		return prevWB;
	}

	// Method to delete the tree from the root
	public void delete() {
		// Due to automatic garbage collection
		root = null;
	}

	// Auxiliary class containing the connected channels
	class Chan {
		// Values Chan will store at each node (or channel) in the tree
		int channelNumber;
		int dsChan;
		int flag;
		boolean goRight;
		float Q;

		// The child nodes (or downstream channels) in the tree
		Chan left;
		Chan right;

		// Constructor to generate the tree for a real node (channel). Note that for a dummy node,
		// just set Q to -999999999.9
		Chan(int channelNumber, int flag, float Q) {
			// Arguments this constructor will take
			this.channelNumber = channelNumber;
			this.flag = flag;
			this.Q = Q;

			// Initializing to null
			right = null;
			left = null;
		}
	}
}