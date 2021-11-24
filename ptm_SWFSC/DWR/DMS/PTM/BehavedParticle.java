package DWR.DMS.PTM;

import java.util.HashMap;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.Random;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import ncsa.hdf.hdf5lib.exceptions.HDF5SymbolTableException;
import ch.systemsx.cisd.hdf5.HDF5Factory;
import ch.systemsx.cisd.hdf5.IHDF5SimpleReader;
import ch.systemsx.cisd.hdf5.IHDF5SimpleWriter;

/**
 * @author Doug Jackson doug.jackson@noaa.gov
 */
public class BehavedParticle extends Particle {
	// Static fields
	public static String sunriseTime, sunsetTime;
	public static int tideCountThr;
	public static int immortal = 0;
	public static int[] checkpoints;

	// Metadata parameters for calibration
	public static String releaseLocation;
	public static int releaseGroup;
	public static String runID;

	public static int swimCode;
	public static float filterK;
	public static int variableMigrationRate;
	public static float initProbOrient;
	public static int nodeDecision;
	public static double minProbOrient;

	// Channel-specific parameters
	public static double[][] channelPars;
	public static HashMap<Integer, Double> channelLambda = new HashMap<Integer, Double>();
	public static HashMap<Integer, Double> channelOmega = new HashMap<Integer, Double>();
	public static HashMap<Integer, Double> channelMeanMigrationRate = new HashMap<Integer, Double>();
	public static HashMap<Integer, Double> channelHoldThr = new HashMap<Integer, Double>();
	public static HashMap<Integer, Double> channelConstProbOrient = new HashMap<Integer, Double>();
	public static HashMap<Integer, Double> channelSlopeProbOrient = new HashMap<Integer, Double>();
	public static HashMap<Integer, Double> channelPPersistence = new HashMap<Integer, Double>(); // VKS: Creating new hashmap for pPersistence
	public static HashMap<Integer, Double> channelDaytimeSwimProb = new HashMap<Integer, Double>();
	public static HashMap<Integer, Double> channelStdMigrationRate = new HashMap<Integer, Double>();
	public static HashMap<Integer, Double> channelPSystem = new HashMap<Integer, Double>(); // VKS: Creating new hashmap for pSystem
	public static HashMap<Integer, Double> channelHoldProb = new HashMap<Integer, Double>(); // VKS: Creating new hashmap for holdProb

	public static String outputFilename = MainPTM.getBehaviorOutputFilename();
	public static IHDF5SimpleWriter writer = initializeWriter();
	public static IHDF5SimpleReader reader;
	public static String behaviorParameterFile = MainPTM.getBehaviorInputFilename();

	public static int numParticles;
	public static HashMap<String, Integer> checkpointIndices = new HashMap<String, Integer>();

	// Arrays to store insertions, deaths, and first arrival datetimes
	public static String[] insertionArray;
	public static String[] deathArray;
	public static String[][] firstArrivalArray;

	// Code indicating the type of testing output to write
	public static int testOutType;
	public static BufferedWriter testWriter;

	// Maximum number of repeated routing attempts to allow before making the particle wait
	public static final int MAX_MULTIPLE_ROUTE_ATTEMPTS = 20;

	// Static initializer
	static {

		// Open the HDF5 file that contains the parameter values
		try {
			reader = HDF5Factory.openForReading(behaviorParameterFile);
			System.out.println("Opened " + behaviorParameterFile);

			// Read in the sunrise and sunset times and set the appropriate hours in MainPTM
			sunriseTime = reader.readString("sunriseTime");
			sunsetTime = reader.readString("sunsetTime");
			MainPTM.sunriseHour = Integer.parseInt(sunriseTime.substring(0, 2));
			MainPTM.sunriseMin = Integer.parseInt(sunriseTime.substring(2, 4));
			MainPTM.sunsetHour = Integer.parseInt(sunsetTime.substring(0, 2));
			MainPTM.sunsetMin = Integer.parseInt(sunsetTime.substring(2, 4));

			// Read in the tideCountThr and set the class variable in SmartChannel
			tideCountThr = reader.readInt("tideCountThr");
			SmartChannel.setTideCountThr(tideCountThr);

			// Read in the immortal flag
			immortal = reader.readInt("immortal");

			// Read the checkpoints and sort them so we can use Arrays.binarySearch() to see if the
			// list contains a particular checkpoint
			checkpoints = reader.readIntArray("checkpoints");
			Arrays.sort(checkpoints);

			releaseLocation = reader.readString("releaseLocation");
			releaseGroup = reader.readInt("releaseGroup");
			runID = reader.readString("runID");
			swimCode = reader.readInt("swimCode");
			filterK = reader.readFloat("filterK");
			variableMigrationRate = reader.readInt("variableMigrationRate");
			initProbOrient = reader.readFloat("initProbOrient");

			nodeDecision = reader.readInt("nodeDecision");

			minProbOrient = 0.5;

			// channelPars
			// read in HDF5 file, store channel specific values in hashmaps
			// 0: channel
			// 1: lambda
			// 2: omega
			// 3: meanMigrationRate
			// 4: HoldThr
			// 5: ConstProbOrient
			// 6: SlopeProbOrient
			// 7: pPersistence
			// 8: daytimeSwimProb
			// 9: stdMigrationRate
			// 10: pSystem
			// 11: holdProb
			channelPars = reader.readDoubleMatrix("channelPars");
			for (int i = 0; i < channelPars.length; i++) {
				channelLambda.put((int) Math.floor(channelPars[i][0]), channelPars[i][1]);
				channelOmega.put((int) Math.floor(channelPars[i][0]), channelPars[i][2]);
				channelMeanMigrationRate.put((int) Math.floor(channelPars[i][0]), channelPars[i][3]);
				channelHoldThr.put((int) Math.floor(channelPars[i][0]), channelPars[i][4]);
				channelConstProbOrient.put((int) Math.floor(channelPars[i][0]), channelPars[i][5]);
				channelSlopeProbOrient.put((int) Math.floor(channelPars[i][0]), channelPars[i][6]);
				channelPPersistence.put((int) Math.floor(channelPars[i][0]), channelPars[i][7]); // VKS: Additional assignment
				channelDaytimeSwimProb.put((int) Math.floor(channelPars[i][0]), channelPars[i][8]);
				channelStdMigrationRate.put((int) Math.floor(channelPars[i][0]), channelPars[i][9]);
				channelPSystem.put((int) Math.floor(channelPars[i][0]), channelPars[i][10]);
				channelHoldProb.put((int) Math.floor(channelPars[i][0]), channelPars[i][11]);
			}

			System.out.println("swimCode = " + swimCode + 
					"\nimmortal = " + immortal + 
					"\nvariableMigrationRate = " + variableMigrationRate + 
					"\nminProbOrient = " + minProbOrient + 
					"\ninitProbOrient = " + initProbOrient + 
					"\nsunriseTime = " + sunriseTime + 
					"\nsunsetTime = " + sunsetTime +																															// NA
					"\ncheckpoints = " + Arrays.toString(checkpoints) + 
					"\nnodeDecision = " + nodeDecision + "\n");

			writer.writeInt("swimCode", swimCode);
			writer.writeInt("immortal", immortal);
			writer.writeInt("variableMigrationRate", variableMigrationRate);
			writer.writeInt("tideCountThr", tideCountThr);
			writer.writeFloat("filterK", filterK);
			writer.writeFloat("initProbOrient", initProbOrient);
			writer.writeString("sunriseTime", sunriseTime);
			writer.writeString("sunsetTime", sunsetTime);
			writer.writeIntArray("checkpoints", checkpoints);
			writer.writeInt("nodeDecision", nodeDecision);			
			writer.writeDoubleMatrix("channelPars", channelPars);
			writer.writeString("releaseLocation", releaseLocation);
			writer.writeInt("releaseGroup", releaseGroup);
			writer.writeString("runID", runID);

			// Read the debugging output code
			testOutType = reader.readInt("testOutType");

			// Creating the testing output file if enabled
			if (testOutType>0) {
				createTestOut();
			}

		} catch (HDF5SymbolTableException e) {
			System.out.println(e);
			System.out.println("Could not find one of the parameters in the HDF5 file " + behaviorParameterFile);
			System.exit(2);
		} catch (Exception e) {
			System.out.println("Failed to open " + behaviorParameterFile + ". Does it exist?");
			System.out.println("Aborting execution");
			System.out.println(e.getMessage());
			System.exit(1);
		}

	}

	// Behavior parameters - instance variables
	// Channel-specific parameters
	public double lambda;
	public double omega;
	public float meanMigrationRate;
	public float holdThr;
	public float constProbOrient;
	public float slopeProbOrient;
	public float pPersistence; // VKS: Adding new behavior element for memory persistence
	public float daytimeSwimProb;
	public float stdMigrationRate;
	public float pSystem;
	public float holdProb; // VKS: Holding threshold probability as proposed in July 23 2019 developer meeting

	// Flag to indicate whether this particle has entered a channel yet
	public boolean enteredChannel;

	public boolean swimTime;
	public float lastDecisionAttemptTime;
	public float migrationRate;

	public boolean madeDecision;
	public ArrayList<Integer> optionsTried;

	public double probOrient;
	public float orientFactor;
	public float channelDir;
	public float flowVelocity;
	public float baseParticleVel;
	public float particleVelocity;
	public float baseSwimVel;
	public float swimVelocity;

	public Waterbody previousWB;

	// Reservoir residence time variables
	public boolean inReservoir;
	public int reservoirID;
	public float reservoirResTime;

	// mortality parameters
	Random generator = new Random();
	public double realizedSurvProb;

	// Checkpoint parameters
	public int ChippsPassCount;
	public int ExitPassCount;
	public int SWPpassCount;
	public int CVPpassCount;
	public int[] checkpointsPassCount;

	////////////////////////////////////////////////////////////////////
	// Instance methods
	////////////////////////////////////////////////////////////////////

	public BehavedParticle(ParticleFixedInfo pFI) // Constructor called when a particle is created
	{
		super(pFI);

		// lastDecisionTime == -999.0f indicates that makeNodeDecision has never been run
		lastDecisionAttemptTime = -999.0f;

		madeDecision = true;
		optionsTried = new ArrayList<Integer>();
		inReservoir = false;
		reservoirID = -999;
		reservoirResTime = 0.0f;

		channelDir = 1.0f;

		// Initialize checkpoint pass counts
		ChippsPassCount = 0;
		ExitPassCount = 0;
		CVPpassCount = 0;
		SWPpassCount = 0;
		checkpointsPassCount = new int[checkpoints.length];

		// Initialize channel-specific parameters
		lambda = 0.0;
		omega = 0.0;
		meanMigrationRate = 0.0f;
		stdMigrationRate = 0.0f;
		holdThr = 0.0f;
		constProbOrient = 0.0f;
		slopeProbOrient = 0.0f;
		pPersistence = 0.0f;
		daytimeSwimProb = 0.0f;
		pSystem = 0.0f;
		holdProb = 0.0f;

		enteredChannel = false;
		swimTime = true;

		// Initialize the migrationRate to 0.0 just to be safe
		migrationRate = 0.0f;

		// Write the realized migrationRate for each particle to the output file, but
		// only do this if variableMigrationRate==0
		if (variableMigrationRate == 0) {
			writer.writeFloat("migrationRate/particleNum/" + Integer.toString(this.getId()), migrationRate);
		}

		// VKS: Check to see if memory persists
		if (generator.nextDouble() < (double) pPersistence) {
			orientFactor = 1.0f; // Fish is oriented with the flow in this case
		} else {
			// VKS: Initialize orientFactor (-1 is oriented opposite to the flow)
			if (generator.nextDouble() < initProbOrient) {
				orientFactor = 1.0f;
			} else {
				orientFactor = -1.0f;
			}
		}

		// Initializations
		probOrient = initProbOrient;
		realizedSurvProb = 1.0;
	}

	// Main swim behavior stuff here
	/**
	 * Create some swimming behaviors for movement along the channel axis. 
	 * 0: never move (used to test particle environment queries) 
	 * 1: passive drift (basic PTM) 
	 * 11: swim "downstream" when the velocity is above -holdThr, with "downstream" varying by channel; hold otherwise
	 */

	/**
	 * Behaviors that were previously tried:
	 * 2: swim downstream all the time 
	 * 3: swim downstream when the flow is towards the bay (positive), otherwise drift 
	 * 4: hold still when flow is negative, swim towards bay when positive 
	 * 5: swim with the flow when tide falls, otherwise drift 
	 * 6: swim with the flow when tide falls, otherwise hold still 
	 * 7: swim towards higher salinity (EC) all the time 
	 * 8: swim downstream during certain times (e.g., nighttime), otherwise hold (Note: with the addition of dielSwimPeriod, swimCode 8==2) 
	 * 9: swim "downstream" at a constant migrationRate; with advection 
	 * 10: swim "downstream" at a constant migrationRate, with "downstream" varying by channel; with advection 
	 */

	/**
	 * Externally induced deterministic (the effect of flow in the channel)
	 */
	@Override
	protected float calcXVelocityExtDeterministic() {
		flowVelocity = getFlowVelocity();

		particleVelocity = 0.0f;

		switch (swimCode) {
		case 0:
			particleVelocity = 0.0f;
			break;

		case 1: case 11:
			particleVelocity = flowVelocity;
			break;

		default:
			throw new IllegalArgumentException("Unrecognized swimCode");
		}
		baseParticleVel = particleVelocity;

		// STST and daytimeSwimProb adjustments for swimCode 11
		if(swimCode==11) {

			// If flow is more negative than the STST hold threshold, reduce velocity by holdProb to implicitly
			// account for STST holding
			if (flowVelocity*channelDir <= -holdThr) {
				particleVelocity*=(1-holdProb);
			}

			// If daytime, multiply velocity by daytimeSwimProb to implicitly account for reduced activity (holding)
			if (MainPTM.isDaytime) {
				particleVelocity*=daytimeSwimProb;
			}
		}

		return particleVelocity;
	}

	/**
	 * Internally induced deterministic (the particle's swimming behavior)
	 */
	@Override
	protected float calcXVelocityIntDeterministic() {
		flowVelocity = getFlowVelocity();
		swimVelocity = 0.0f;

		switch (swimCode) {
		case 0: case 1:
			swimVelocity = 0.0f;
			break;

		case 11:
			swimVelocity = migrationRate * channelDir * orientFactor;
			break;

		default:
			throw new IllegalArgumentException("Unrecognized swimCode");

		}
		baseSwimVel = swimVelocity;

		// STST and daytimeSwimProb adjustments for swimCode 11
		if(swimCode==11) {

			// If flow is more negative than the STST hold threshold, reduce velocity by holdProb to implicitly
			// account for STST holding
			if (flowVelocity*channelDir <= -holdThr) {
				swimVelocity*=(1-holdProb);
			}

			// If daytime, multiply velocity by daytimeSwimProb to implicitly account for reduced activity (holding)
			if (MainPTM.isDaytime) {
				swimVelocity*=daytimeSwimProb;
			}
		}

		return swimVelocity;
	}

	@Override
	/**
	 * Decide which WaterBody to enter into next
	 */
	protected void makeNodeDecision() {
		int choiceIndex = 0;
		int numWaterBodies = nd.getNumberOfWaterbodies();
		double[] weightVector;
		double sumWeightVector;
		int tempWBnum;
		int[] indexVector;

		// VKS: Additions for new junction model
		Waterbody tempWB;
		int flag = 1;
		float[] currentQ;
		int[] chansCurrent;
		int[] cFlag, cQFlag;
		int[] currentWBNum;

		ArrayList<Integer> options;
		int numOptions;

		int prevPos, currPos1, currPos2, cNum, ind, ind1;

		float yND, zND, scratch;
		int currWB, cc, cf;

		ArrayList<int[]> junctionBlowUp = new ArrayList<int[]>();

		madeDecision = false;

		previousWB = wb; // VKS: The water body that the particle currently is in

		// Send message to observer about change
		if (observer != null) {
			observer.observeChange(ParticleObserver.NODE_CHANGE, this);
		}

		// Loop until a decision is made
		do {
			//  Clear optionsTried unless this is a retry of an unsuccessful choice and possibly check to see
			// if the fish becomes confused unless this is a retry of an unsuccessful choice
			if (((float) Globals.currentModelTime + tmLeft) != lastDecisionAttemptTime) {
				optionsTried = new ArrayList<Integer>();

				// If this is a junction, check to see if the fish is going with or against the flow
				if (nd.getNumChannels() > 2) {
					// There are more than two channels
					checkOrientation();
				}
			}

			// Remember the last currentModelTime && tmLeft combination when we attempted to make a decision
			lastDecisionAttemptTime = ((float) Globals.currentModelTime + tmLeft);

			// Create a list of all the channels at this node
			options = new ArrayList<Integer>();
			for (int i=0; i<numWaterBodies; i++ ) {

				if ((!(nd.getWaterbody(i) instanceof Boundary) || nd.getWaterbodyEnvIndex(i) == 901 || nd.getWaterbodyEnvIndex(i) == 915)) {
					options.add(i);
				}
			}
			numOptions = options.size();

			// If there are no choices, either move out into the channel a small amount if it's a
			// dead end, or wait
			if (numOptions<2) {
				if (numWaterBodies == 1) {
					x = getPerturbedXLocation();
				} else {
					particleWait = true;
				}
				return;
			}

			/* Determine if the same channel has been tried multiple times within the same time step.
				We already have some logic in DecisionTree.java that attempts to resolve these conditions by
				placing the particle in a random channel. However, a truly pathological case could be stuck in an 
				infinite loop, so we'll want to make the particle wait if the particle has been stuck for a long time.
			 */
			HashSet<Integer> tempSet = new HashSet<Integer>(optionsTried);
			if((optionsTried.size()-tempSet.size())>MAX_MULTIPLE_ROUTE_ATTEMPTS) {
				particleWait = true;
				return;
			}

			weightVector = new double[options.size()];
			sumWeightVector = 0.0;
			indexVector = new int[options.size()];

			currentQ = new float[options.size()]; // VKS: Array initialization
			cFlag = new int[options.size()];
			cQFlag = new int[options.size()];
			currentWBNum = new int[options.size()];

			switch (nodeDecision) {

			// Outflow-based decision
			case 0:
				for (int i = 0; i < options.size(); i++) {
					weightVector[i] = nd.getFilterOp(options.get(i)) * nd.getOutflow(options.get(i), tmLeft);
					sumWeightVector += weightVector[i];
					indexVector[i] = options.get(i);
				}
				break;

				// VKS: Streamline based junction rule
				// Choose a water body based on the relative position of the fish to the bifurcating streamline
			case 4:
				// First get flow in current water body at the node connected to the junction
				for (int i = 0; i < numOptions; i++) {
					tempWB = nd.getWaterbody(options.get(i));
					currentWBNum[i] = tempWB.getEnvIndex();
					// Current waterbody in list
					// Details for all the waterbodies
					if (tempWB instanceof SmartChannel) {
						if (((SmartChannel) tempWB).getUpNodeId() == nd.getEnvIndex())
							cFlag[i] = -1;
						else
							cFlag[i] = 1;
					} else // For reservoirs
						cFlag[i] = 1;

					currentQ[i] = nd.getSignedOutflow(options.get(i), tmLeft);
					cQFlag[i] = (int) Math.signum(currentQ[i]);
				}

				// Assign flags based on what the flow and fish are doing relative to the orientation of the
				// channel
				if (previousWB instanceof SmartChannel) {
					// For channels
					if (((SmartChannel) previousWB).getUpNodeId() == nd.getEnvIndex()) { 
						flag = -1; // Particle is at upstream end of the channel
					}

					if (((SmartChannel) previousWB).getDownNodeId() == nd.getEnvIndex()) {
						flag = 1; // Particle is at downstream end of the channel
					}

					// 2-channel junction
					if (numOptions == 2) {

						// If the exit channel is a gate, wait. Otherwise, choose it.
						for (int i = 0; i < numOptions; i++) {
							if (nd.getWaterbody(options.get(i)) == previousWB) {
								weightVector[i] = 0.0f;
							} else if (nd.getSignedOutflow(options.get(i), tmLeft) == 0.0f) {
								// Gate
								particleWait = true;
								return;						
							} else {
								weightVector[i] = 1.0f;
								choiceIndex = i;
							}
							indexVector[i] = options.get(i);
						}
					} else {
						// More than 2 channels at the junction
						// Start from the entry channel and build the list
						// All channels in the row containing the entry channel
						chansCurrent = JunctionsInput.getRow(previousWB.getEnvIndex(), flag == 1 ? true : false);

						prevPos = findIndex(chansCurrent, flag * previousWB.getEnvIndex());
						// Position on this row of the entry channel
						currPos1 = ((prevPos % 3) + 1) % 3; // Positions of the other two channels clockwise from
						currPos2 = ((prevPos % 3) + 2) % 3; // the entry channel

						// Creating an ordered matrix containing all the artificial sub-junctions
						junctionBlowUp.add(new int[] {chansCurrent[prevPos], chansCurrent[currPos1], chansCurrent[currPos2]});

						// 4-channel junction
						if (numOptions == 4) {
							if (Math.abs(chansCurrent[currPos1])>=1000) {
								// Right channel is dummy
								junctionBlowUp.add(populateJBU(junctionBlowUp.get(0)[1]));
							} else if (Math.abs(chansCurrent[currPos2])>=1000) {
								// Left channel is dummy
								junctionBlowUp.add(populateJBU(junctionBlowUp.get(0)[2]));
							} 
						} else if (numOptions == 5) {
							if (Math.abs(chansCurrent[currPos1])>=1000 && Math.abs(chansCurrent[currPos2])<1000) {
								// Only right channel of first entry is dummy
								junctionBlowUp.add(populateJBU(junctionBlowUp.get(0)[1]));

								// Add next entry
								cNum = Math.abs(junctionBlowUp.get(1)[1]);

								if (cNum>=1000) {
									// Right channel of second entry is dummy
									junctionBlowUp.add(populateJBU(junctionBlowUp.get(1)[1]));
								} else {
									// Left channel of second entry is dummy
									junctionBlowUp.add(populateJBU(junctionBlowUp.get(1)[2]));
								}
							} else if (Math.abs(chansCurrent[currPos1])<1000 && Math.abs(chansCurrent[currPos2])>=1000) {
								// Only left channel of first entry is dummy
								junctionBlowUp.add(populateJBU(junctionBlowUp.get(0)[2]));

								// Add next entry
								cNum = Math.abs(junctionBlowUp.get(1)[1]);

								if (cNum>=1000) {
									// Right channel of second entry is dummy
									junctionBlowUp.add(populateJBU(junctionBlowUp.get(1)[1]));
								} else {
									// Left channel of second entry is dummy
									junctionBlowUp.add(populateJBU(junctionBlowUp.get(1)[2]));
								}	
							} else {
								// Both channels of the first entry are dummies
								junctionBlowUp.add(populateJBU(junctionBlowUp.get(0)[1]));
								junctionBlowUp.add(populateJBU(junctionBlowUp.get(0)[2]));
							}	
						} else if (numOptions==6) {
							if (Math.abs(chansCurrent[currPos1])>=1000 && Math.abs(chansCurrent[currPos2])<1000) {
								// Only right channel of first entry is a dummy
								junctionBlowUp.add(populateJBU(junctionBlowUp.get(0)[1]));

								if (Math.abs(junctionBlowUp.get(1)[1])>=1000 && Math.abs(junctionBlowUp.get(1)[2])<1000) {
									// Only right channel of second entry is a dummy
									junctionBlowUp.add(populateJBU(junctionBlowUp.get(1)[1]));

									// Add next entry
									cNum = Math.abs(junctionBlowUp.get(2)[1]);

									if (cNum>=1000) {
										// Right channel of third entry is a dummy
										junctionBlowUp.add(populateJBU(junctionBlowUp.get(2)[1]));
									} else {
										// Left channel of third entry is a dummy
										junctionBlowUp.add(populateJBU(junctionBlowUp.get(2)[2]));
									}
								} else if (Math.abs(junctionBlowUp.get(1)[1])<1000 && Math.abs(junctionBlowUp.get(1)[2])>=1000) {
									// Only left channel of second entry is dummy
									junctionBlowUp.add(populateJBU(junctionBlowUp.get(1)[2]));

									// Add next entry
									cNum = Math.abs(junctionBlowUp.get(2)[1]);

									if (cNum>=1000) {
										// Right channel of third entry is a dummy
										junctionBlowUp.add(populateJBU(junctionBlowUp.get(2)[1]));
									} else {
										// Left channel of third entry is a dummy
										junctionBlowUp.add(populateJBU(junctionBlowUp.get(2)[2]));
									}									
								} else if (Math.abs(junctionBlowUp.get(1)[1])>=1000 && Math.abs(junctionBlowUp.get(1)[2])>=1000) {
									// Both channels of second entry are dummies
									junctionBlowUp.add(populateJBU(junctionBlowUp.get(1)[1]));
									junctionBlowUp.add(populateJBU(junctionBlowUp.get(1)[2]));
								}
							} else if (Math.abs(chansCurrent[currPos1])<1000 && Math.abs(chansCurrent[currPos2])>=1000) {
								// Only left channel of first entry is a dummy
								junctionBlowUp.add(populateJBU(junctionBlowUp.get(0)[2]));

								if (Math.abs(junctionBlowUp.get(1)[1])>=1000 && Math.abs(junctionBlowUp.get(1)[2])<1000) {
									// Only right channel of second entry is a dummy
									junctionBlowUp.add(populateJBU(junctionBlowUp.get(1)[1]));

									// Add next entry
									cNum = Math.abs(junctionBlowUp.get(2)[1]);

									if (cNum>=1000) {
										// Right channel of third entry is a dummy
										junctionBlowUp.add(populateJBU(junctionBlowUp.get(2)[1]));
									} else {
										// Left channel of third entry is a dummy
										junctionBlowUp.add(populateJBU(junctionBlowUp.get(2)[2]));
									}
								} else if (Math.abs(junctionBlowUp.get(1)[1])<1000 && Math.abs(junctionBlowUp.get(1)[2])>=1000) {
									// Only left channel of second entry is dummy
									junctionBlowUp.add(populateJBU(junctionBlowUp.get(1)[2]));

									// Add next entry
									cNum = Math.abs(junctionBlowUp.get(2)[1]);

									if (cNum>=1000) {
										// Right channel of third entry is a dummy
										junctionBlowUp.add(populateJBU(junctionBlowUp.get(2)[1]));
									} else {
										// Left channel of third entry is a dummy
										junctionBlowUp.add(populateJBU(junctionBlowUp.get(2)[2]));
									}									
								} else if (Math.abs(junctionBlowUp.get(1)[1])>=1000 && Math.abs(junctionBlowUp.get(1)[2])>=1000) {
									// Both channels of second entry are dummies
									junctionBlowUp.add(populateJBU(junctionBlowUp.get(1)[1]));
									junctionBlowUp.add(populateJBU(junctionBlowUp.get(1)[2]));
								}
							} else if (Math.abs(chansCurrent[currPos1])>=1000 && Math.abs(chansCurrent[currPos2])>=1000) {
								// Both channels in the first entry are dummies
								junctionBlowUp.add(populateJBU(junctionBlowUp.get(0)[1]));
								junctionBlowUp.add(populateJBU(junctionBlowUp.get(0)[2]));

								// Add next entry
								if (Math.abs(junctionBlowUp.get(1)[1])>=1000) {
									// Right channel of second entry is dummy
									junctionBlowUp.add(populateJBU(junctionBlowUp.get(1)[1]));
								} else if (Math.abs(junctionBlowUp.get(1)[2])>=1000) {
									// Left channel of second entry is dummy
									junctionBlowUp.add(populateJBU(junctionBlowUp.get(1)[2]));
								}

								// Add next entry
								if (Math.abs(junctionBlowUp.get(2)[1])>=1000) {
									// Right channel of second entry is dummy
									junctionBlowUp.add(populateJBU(junctionBlowUp.get(2)[1]));
								} else if (Math.abs(junctionBlowUp.get(2)[2])>=1000) {
									// Left channel of second entry is dummy
									junctionBlowUp.add(populateJBU(junctionBlowUp.get(2)[2]));
								}
							}

						} else if (numOptions>6){
							System.out.println("Error: junctions should have six or fewer valid exits. numOptions = " + numOptions);
						}

						// Constructing the junction tree
						DecisionTree dT = new DecisionTree(this.getId());

						for (int i=0; i < junctionBlowUp.size(); i++) {

							if (i == 0) {
								// Root
								ind = findIndex(currentWBNum, Math.abs(junctionBlowUp.get(i)[0]));
								dT.add(0, currentWBNum[ind], 0, cFlag[ind], currentQ[ind]);

								// Right node
								cc = Math.abs(junctionBlowUp.get(i)[1]);
								cf = (int) Math.signum(junctionBlowUp.get(i)[1]);

								if (cc > 1000) {
									dT.add(currentWBNum[ind], cc, 1, cf, -999999999.9f);
								} else {
									ind1 = findIndex(currentWBNum, Math.abs(junctionBlowUp.get(i)[1]));
									dT.add(currentWBNum[ind], currentWBNum[ind1], 1, cFlag[ind1], currentQ[ind1]);
								}

								// Left node
								cc = Math.abs(junctionBlowUp.get(i)[2]);
								cf = (int) Math.signum(junctionBlowUp.get(i)[2]);

								if (cc > 1000) {
									dT.add(currentWBNum[ind], cc, 2, cf, -999999999.9f);
								} else {
									ind1 = findIndex(currentWBNum, Math.abs(junctionBlowUp.get(i)[2]));
									dT.add(currentWBNum[ind], currentWBNum[ind1], 2, cFlag[ind1], currentQ[ind1]);
								}
							} else {
								// Subsequent nodes
								tempWBnum = Math.abs(junctionBlowUp.get(i)[0]);

								// Right node
								cc = Math.abs(junctionBlowUp.get(i)[1]);
								cf = (int) Math.signum(junctionBlowUp.get(i)[1]);

								if (cc > 1000) {
									dT.add(tempWBnum, cc, 1, cf, -999999999.9f);
								} else {
									ind1 = findIndex(currentWBNum, Math.abs(junctionBlowUp.get(i)[1]));
									dT.add(tempWBnum, currentWBNum[ind1], 1, cFlag[ind1], currentQ[ind1]);
								}

								// Left node
								cc = Math.abs(junctionBlowUp.get(i)[2]);
								cf = (int) Math.signum(junctionBlowUp.get(i)[2]);

								if (cc > 1000) {
									dT.add(tempWBnum, cc, 2, cf, -999999999.9f);
								} else {
									ind1 = findIndex(currentWBNum, Math.abs(junctionBlowUp.get(i)[2]));
									dT.add(tempWBnum, currentWBNum[ind1], 2, cFlag[ind1], currentQ[ind1]);
								}
							}
						}

						// Assigning flows
						scratch = dT.recursivelySumFlows(dT.root);

						// Moving the particle through the tree
						yND = 0.5f + y / channelWidth; // Dimensionless lateral position
						zND = z / channelDepth;
						currWB = dT.pushIntoChannel(yND, optionsTried);

						// Destroying the tree
						dT.delete();

						// Hook into subsequent methods to move the particle along
						for (int i = 0; i < numOptions; i++) {
							weightVector[i] = nd.getFilterOp(options.get(i)) * nd.getOutflow(options.get(i), tmLeft);
							if (currentWBNum[i] == currWB) {
								if (Math.abs(nd.getFilterOp(options.get(i))) == 0.0f)	//If there is a filter in place
								{
									weightVector[i] = 1.0f;
									for (int j = 0; j < numOptions; j++) {
										if (j != i)
											weightVector[j] = 0;
									}
									break;
								}
							}
							indexVector[i] = options.get(i);							
						}

						//VKS: Make the entry water body decision here itself to simplify the downstream workflow
						sumWeightVector = 0.0;
						for (int i=0; i<weightVector.length; i++) {
							sumWeightVector+=weightVector[i];
						}

						// If there are viable options, make a choice.
						if (sumWeightVector > 0) {

							// Normalize the weight vector
							for (int i = 0; i < weightVector.length; i++) {
								weightVector[i] /= sumWeightVector;
							}

							// Make the choice and remove it from the list of possible future choices for this node
							choiceIndex = weightedChoice(weightVector);
						}						
					}
				} else { 
					// For reservoirs, just randomize the position but with a time delay
					for (int i = 0; i < numOptions; i++) {
						if (nd.getWaterbody(options.get(i)) == previousWB) {
							weightVector[i] = 0.0f;
						} else if (nd.getSignedOutflow(options.get(i), tmLeft) == 0.0f) {
							weightVector[i] = 0.0f;							
						} else {
							weightVector[i] = nd.getFilterOp(options.get(i)) * nd.getOutflow(options.get(i), tmLeft);
						}
						indexVector[i] = options.get(i);
					}

					//VKS: Make the entry water body decision here itself to simplify the downstream workflow
					sumWeightVector = 0.0;
					for (int i=0; i<weightVector.length; i++) {
						sumWeightVector+=weightVector[i];
					}

					// If there are viable options, make a choice.
					if (sumWeightVector > 0) {

						// Normalize the weight vector
						for (int i = 0; i < weightVector.length; i++) {
							weightVector[i] /= sumWeightVector;
						}

						// Make the choice and remove it from the list of possible future choices for this node
						choiceIndex = weightedChoice(weightVector);
					}
				}
				break;

			default:
				throw new IllegalArgumentException("Unrecognized decision type in nodeDecisions");
			}

			// Calculate sumWeightVector
			switch (nodeDecision) {

			case 0:
				sumWeightVector = 0.0;
				for (int i=0; i<weightVector.length; i++) {
					sumWeightVector+=weightVector[i];
				}

				// If there are viable options, make a choice.
				if (sumWeightVector > 0) {

					// Normalize the weight vector
					for (int i = 0; i < weightVector.length; i++) {
						weightVector[i] /= sumWeightVector;
					}

					// Make the choice and remove it from the list of possible future choices for this node
					choiceIndex = weightedChoice(weightVector);
					madeDecision = true;
				}	

			case 4:	
				madeDecision = true;				
			}


		} while (!madeDecision);

		// Get a pointer to the water body that the particle entered
		wb = nd.getWaterbody(indexVector[choiceIndex]);

		optionsTried.add(wb.getEnvIndex());

		// Update parameters, etc., when entering a new channel
		enterChannel();

		// See if the particle has passed any checkpoints
		checkCheckpoints();

		// Send message to observer about change
		if (observer != null)
			observer.observeChange(ParticleObserver.WATERBODY_CHANGE, this);

		// Set x as beginning of Channel...
		x = getXLocationInChannel();

	}

	// Create a new entry for junctionBlowUp
	public int[] populateJBU(int jSignedNum) {
		int[] chansCurrent;
		int[] newRow = new int[3];
		int currPos1, currPos2, cNum, ind, cF;

		cNum = Math.abs(jSignedNum);
		cF = -(int) Math.signum(jSignedNum);
		chansCurrent = JunctionsInput.getRow(cNum, cF == 1 ? true : false);
		ind = findIndex(chansCurrent, cNum*cF);
		currPos1 = ((ind % 3) + 1) % 3;
		currPos2 = ((ind % 3) + 2) % 3;

		newRow[0] = chansCurrent[ind];
		newRow[1] = chansCurrent[currPos1];
		newRow[2] = chansCurrent[currPos2];
		return(newRow);
	}

	public float getFlowVelocity() { // VKS: To get the instantaneous water velocity at the location of the particle

		float flowVelocity;

		if (wb instanceof SmartChannel) {
			// Getting the curvature and bend direction
			flowVelocity = ((SmartChannel) wb).getVel(x, y, z, channelVave, channelWidth, channelDepth);
		} else
			flowVelocity = 0.0f;

		if (Float.isNaN(flowVelocity)) {
			System.out.println("x=" + x + ", y=" + y + ", z=" + z + ", ID=" + wb.getEnvIndex());
			System.exit(2);
		}
		return flowVelocity;
	}

	public void checkCheckpoints() {
		int checkpointIndex;

		// Check to see if the particle has reached Chipps Island yet
		if (previousWB != null) {
			if (wb instanceof Channel && ((wb.getEnvIndex() == 422 || wb.getEnvIndex() == 417)
					&& (previousWB.getEnvIndex() == 275 || previousWB.getEnvIndex() == 281 || previousWB.getEnvIndex() == 278))) {
				ChippsPassCount++;
				recordCheckpoint(this, "Chipps", ChippsPassCount);

				// Write the realized survival to the output file
				writer.writeDouble("realizedSurvProb/" + this.getId(), realizedSurvProb);
			}
		}

		// Check to see if the particle has exited the system
		if (nd.getEnvIndex() == 412) {
			ExitPassCount++;
			recordCheckpoint(this, "Exit", ExitPassCount);
		}

		// Check to see if the particle was exported via SWP
		if (wb instanceof Reservoir) {
			if (((Reservoir) wb).getName().equals("clifton_court")) {
				SWPpassCount++;
				recordCheckpoint(this, "SWP", SWPpassCount);
				isDead = true;
				recordDeath(this);
			}
		}

		if (wb.getEnvIndex() == 204) {
			CVPpassCount++;
			recordCheckpoint(this, "CVP", CVPpassCount);
			isDead = true;
			recordDeath(this);
		}

		// Check to see if the particle passed one of the other checkpoints
		checkpointIndex = Arrays.binarySearch(checkpoints, nd.getEnvIndex());
		if (checkpointIndex >= 0) {
			checkpointsPassCount[checkpointIndex]++;
			recordCheckpoint(this, Integer.toString(nd.getEnvIndex()), checkpointsPassCount[checkpointIndex]);
		}
	}

	// Check to see if the fish orients with the flow or not
	public void checkOrientation() {

		float sgnFlow = Math.signum(getFlowVelocity());
		if(sgnFlow==0) {sgnFlow=1.0f;}

		// VKS: Check to see if memory persists; only update orientFactor if not persisting.
		boolean persist = generator.nextDouble() < (double) pPersistence;
		if (!persist) {

			// VKS: Fish becomes oriented with the flow with probability=probOrient

			//VKS: update at each timestep
			updateProbOrient();

			if (generator.nextDouble() < probOrient) {
				orientFactor = sgnFlow*channelDir;
			} else {
				orientFactor = -sgnFlow*channelDir;
			}
		}
	}

	// Update parameters, etc., when a fish enters a new channel
	public void enterChannel() {

		if (wb instanceof SmartChannel) {
			meanMigrationRate = channelMeanMigrationRate.get(wb.getEnvIndex()).floatValue();
			stdMigrationRate = channelStdMigrationRate.get(wb.getEnvIndex()).floatValue();
			holdThr = channelHoldThr.get(wb.getEnvIndex()).floatValue();
			constProbOrient = channelConstProbOrient.get(wb.getEnvIndex()).floatValue();
			slopeProbOrient = channelSlopeProbOrient.get(wb.getEnvIndex()).floatValue();
			pPersistence = channelPPersistence.get(wb.getEnvIndex()).floatValue(); // VKS: Update value
			daytimeSwimProb = channelDaytimeSwimProb.get(wb.getEnvIndex()).floatValue();
			pSystem = channelPSystem.get(wb.getEnvIndex()).floatValue(); // VKS: Update value
			holdProb = channelHoldProb.get(wb.getEnvIndex()).floatValue();

			channelDir = (float) RiverBendsInput.getChannelOrientBend(wb.getEnvIndex(), "OceanOrient");
		} else {
			channelDir = 1.0f;
		}
		enteredChannel = true;
	}

	// Update the probability of orientation based on the dimensionless instantaneous velocity of the current channel
	public void updateProbOrient() {
		double lnVel, term;
		float flowVelocity;

		int julianMin = Globals.currentModelTime; // <--- VKS: Get the current model time
		int startTime = Globals.Environment.getStartTime();

		// Only change probOrient if the fish is currently in a smart channel.
		if (julianMin == startTime) {
			// Need a different rule for the first time a particle is released because we don't have its flow info yet
			// System.out.println("A");
			if (wb instanceof SmartChannel) { // System.out.println("Yes");
				probOrient = initProbOrient; // <--- We begin with fully confused particles
			}
		} else {
			if (wb instanceof SmartChannel) { // <---The first timestep must be different: subsequent timesteps can have this routine: [Seek DJ's
				// help]
				flowVelocity = getFlowVelocity(); // VKS:Using flow velocity at that location instead of signal
				// System.out.println("Velocity: "+flowVelocity);
				// VKS: New method based on absolute value of local velocity divided by grand mean velocity in the Delta between 1962 and 2016
				lnVel = Math.log((double) (Math.abs((double) (flowVelocity / 0.957f + 0.000001f))));
				// Note: slopeProbOrient should be positive.
				term = Math.exp(constProbOrient + slopeProbOrient * lnVel);
				probOrient = minProbOrient + (pSystem - minProbOrient) * term / (1 + term + 0.000001f);
			}
		}
	}

	@Override
	public void insert() {
		super.insert();

		// Record the insertion time
		recordInsertion(this);
	}

	@Override
	// Override updatePosition to implement BehavedParticle actions that occur every 15 minutes
	public void updatePosition(float delT) {
		float logEpsMigrationRate; // VKS: Variables required in the estimation of log-normal draws of velocity
		float truncPoint;
		float M, S2;
		double A, B;

		// Update probOrient whenever a new SmartChannel is entered
		if (madeDecision) {
			// VKS: Set with flow direction for one timestep after new channel entry
			probOrient = 1.0f;
		}
		checkOrientation();

		// Draw a new migrationRate every delT seconds (which will be 15 minutes)
		if (variableMigrationRate == 1) {
			// VKS: Draw logEpsMigrationRate from a normal distribution with mean "meanMigrationRate" and
			// standard deviation "stdMigrationRate":
			A = 2.0f * Math.log((double) (meanMigrationRate + 0.000001f));
			B = Math.log((double) (meanMigrationRate * meanMigrationRate + stdMigrationRate * stdMigrationRate + 0.000001f));
			M = (float) (A - 0.5f * B); // Mean of the adjoint normal distribution
			S2 = Math.max((float) ((double) (B - A)), 0.000001f); // Standard deviation of the adjoint normal
			// distribution

			// VKS: We have M and S from the log-normal distribution, which are simply the mean and
			// standard deviation of the adjoint normal distribution in ft/sec as the calibration parameters.
			// The log-normal distribution will be truncated at an impossibly large value of
			// exp(mu+sqrt(2)*1.16*sigma) blps
			// For example: 21.4 bodylengths per second for a typical 200mm long fish, which corresponds to a
			// migration rate draw of 14.042ft/sec. For fish smaller than this, migration rate draws will be
			// larger than 21.4 blps.
			// Fish are unlikely to be larger than 200mm as smolts (Quinn 2005) - thanks to Peter Dudley,
			// NMFS.
			// At this truncation value, the untruncated and truncated distributions differ only by 5%
			// (see attached figure)
			truncPoint = Math.max((float) ((double) (M + (1.64048f * Math.sqrt((double) S2)) + 0.000001)), 2.7f);
			logEpsMigrationRate = (float) (generator.nextGaussian() * Math.sqrt((double) S2) + M);

			while (logEpsMigrationRate > truncPoint) {
				// Empirically truncate
				logEpsMigrationRate = (float) (generator.nextGaussian() * Math.sqrt((double) S2) + M);
			}

			migrationRate = (float) (Math.exp((double) logEpsMigrationRate)); // VKS: Convert epsMigrationRate
			//System.out.println("A," + A + ",B," + B + ",M," + M + ",S2," + S2 + 
			//		", meanMigrationRate" + meanMigrationRate + ", stdMigrationRate," + stdMigrationRate + ", truncPoint," + truncPoint);
			//System.out.println("mean,"+meanMigrationRate+",std,"+stdMigrationRate+",LogEps,"+logEpsMigrationRate+",Eps,"+epsMigrationRate);
		}

		// Clear the memory of time spent and movement in the previous time step
		movementTimeDistance.clear();

		super.updatePosition(delT);

		// Write testing output
		if (testOutType==1 || testOutType==2) {
			BehavedParticle.writeTestOut(this);
		}

	}

	@Override
	// Particle mortality
	protected void checkHealth() {
		double survivalProb = 1.0;
		int channelNum;
		double time, distance;
		double[] timeDistance;

		// Loop through all of the movementTimeDistance entries
		for (Map.Entry<Integer, double[]> entry : movementTimeDistance.entrySet()) {

			channelNum = entry.getKey();
			if (channelLambda.containsKey(channelNum)) {
				lambda = channelLambda.get(channelNum);
			} else {
				System.out.println("Could not find lambda for channel " + Integer.toString(channelNum));
			}

			if (channelOmega.containsKey(channelNum)) {
				omega = channelOmega.get(channelNum);
			} else {
				System.out.println("Could not find omega for channel " + Integer.toString(channelNum));
			}

			// Retrieve the amount of time and distance traveled in this channel
			timeDistance = entry.getValue();
			time = timeDistance[0];
			distance = timeDistance[1];

			// From Anderson, J. J., Gurarie, E., & Zabel, R. W. (2005). Mean free-path length theory of
			// predatorñprey interactions: Application to juvenile salmon migration. Ecological Modelling,
			// 186(2), 196ñ211. doi:10.1016/j.ecolmodel.2005.01.014
			// Units of lambda are feet; units of omega are feet/sec.
			survivalProb *= Math.exp((-1.0 / lambda) * Math.sqrt((Math.pow(distance, 2.0) + (Math.pow(omega, 2.0) * Math.pow(time, 2.0)))));

		}

		realizedSurvProb *= survivalProb;

		// Particle dies with P(1-survivalProb)
		if (generator.nextDouble() > survivalProb && immortal == 0) {
			isDead = true;
			//			observer.observeChange(ParticleObserver.DEATH,this);
			recordDeath(this);
		}
	}

	@Override
	// Override Particle.makeReservoirDecision() to add instrumentation
	protected Node makeReservoirDecision(float timeStep) {

		Node nd;

		// Detect first entry into reservoir
		if (!inReservoir) {
			inReservoir = true;
			reservoirID = wb.getEnvIndex();
			reservoirResTime = 0.0f;

		}

		// Add this time step to the cumulative reservoir residence time
		reservoirResTime+=timeStep;

		// Call Particle.makeReservoirDecision()
		nd = super.makeReservoirDecision(timeStep);

		// Detect exit from reservoir and reset in preparation for next reservoir
		if (nd != null) {
			writeTestOut(this);
			inReservoir = false;
			reservoirResTime = 0.0f;
		}

		return nd;
	}

	////////////////////////////////////////////////////////////////////
	// Class methods
	////////////////////////////////////////////////////////////////////
	public static void createTestOut() {
		try {
			testWriter = new BufferedWriter(new FileWriter("ePTMtest.out"));
			if (testOutType==1) {
				testWriter.write("modelTime,particleNum,holdThr,flowVelocity,channelDir,baseParticleVel,particleVel,baseSwimVel,swimVel,isDaytime,wbNum");
			}
			else if (testOutType==2) {
				testWriter.write("modelTime,tmStamp,particleNum,wbNum");
			}
			else if (testOutType==3) {
				testWriter.write("particleNum,wbNum,reservoirResTime");
			}
			testWriter.newLine();
		}
		catch (Exception e) {
			System.out.println("Could not create the testing output file.");
			System.exit(1);
		}
	}

	public static void writeTestOut(BehavedParticle particle) {

		String modelTime = Globals.getModelTime(Globals.currentModelTime);

		if (particle.isDead) {
			return;
		}

		try {
			if (testOutType==1 && particle.enteredChannel) {
				testWriter.write(String.format("%s,%d,%g,%g,%g,%g,%g,%g,%g,%s,%d",
						modelTime,
						particle.getId(),
						particle.holdThr,
						particle.flowVelocity,
						particle.channelDir,
						particle.baseParticleVel,
						particle.particleVelocity,
						particle.baseSwimVel,
						particle.swimVelocity,
						Boolean.toString(MainPTM.isDaytime),
						particle.wb.getEnvIndex()));
				testWriter.newLine();
			}
			else if (testOutType==2 && particle.wb!=null) {
				testWriter.write(String.format("%s,%d,%d,%d",
						modelTime,
						particle.getCurrentParticleTime(),
						particle.getId(),
						particle.wb.getEnvIndex()));
				testWriter.newLine();
			}
			else if (testOutType==3) {
				testWriter.write(String.format("%d,%d,%g", 
						particle.getId(),
						particle.reservoirID,
						particle.reservoirResTime));
				testWriter.newLine();
			}

		} catch (IOException e) {
			System.out.println("Could not write to testing output file.");
			System.exit(1);
		}
	}

	public static IHDF5SimpleWriter initializeWriter() {
		// Delete outputFilename if it already exists
		File testFile = new File(outputFilename);
		try {
			if (testFile.exists()) {
				testFile.delete();
			}

		} catch (Exception e) {
			System.out.println("Cannot delete old output file " + outputFilename);
			System.out.println("Try deleting it manually and restarting. Aborting execution");
			System.exit(1);
		}
		IHDF5SimpleWriter w = HDF5Factory.open(outputFilename);
		System.out.println("Opened " + outputFilename + " for writing.");
		return w;
	}

	public static void recordDeath(BehavedParticle bP) {
		int julianMin = Globals.currentModelTime;
		String modelDate, modelTime;

		modelDate = Globals.getModelDate(julianMin);
		modelTime = Globals.getModelTime(julianMin);

		// Write to the HDF5 file
		writer.writeString("died/particleNum/" + Integer.toString(bP.getId()) + "/modelDate", modelDate);
		writer.writeInt("died/particleNum/" + Integer.toString(bP.getId()) + "/modelTime", new Integer(modelTime).intValue());
		writer.writeInt("died/particleNum/" + Integer.toString(bP.getId()) + "/waterBody", bP.getCurrentWaterbody().getEnvIndex());
		
		// Write to deathArray
		deathArray[bP.getId() - 1] = Integer.toString(bP.getId()) + "_" + 
									 bP.getCurrentWaterbody().getEnvIndex() + "_" +
									 modelDate + "_" + 
									 modelTime;

	}

	public static void recordCheckpoint(BehavedParticle bP, String checkpoint, int passCount) {
		int julianMin = Globals.currentModelTime;
		String modelDate, modelTime;

		modelDate = Globals.getModelDate(julianMin);
		modelTime = Globals.getModelTime(julianMin);

		// Write to the HDF5 file
		writer.writeString(checkpoint + "/particleNum/" + Integer.toString(bP.getId()) + "/modelDate_" + passCount, modelDate);
		writer.writeInt(checkpoint + "/particleNum/" + Integer.toString(bP.getId()) + "/modelTime_" + passCount, new Integer(modelTime).intValue());

		if (passCount == 1) {
			recordFirstArrival(bP.getId(), checkpoint, modelDate + "_" + modelTime);
		}
	}

	public static void recordInsertion(BehavedParticle bP) {
		int julianMin = Globals.currentModelTime;
		String modelDate, modelTime;

		modelDate = Globals.getModelDate(julianMin);
		modelTime = Globals.getModelTime(julianMin);

		// Write to the HDF5 file
		writer.writeString("inserted/particleNum/" + Integer.toString(bP.getId()) + "/modelDate", modelDate);
		writer.writeInt("inserted/particleNum/" + Integer.toString(bP.getId()) + "/modelTime", new Integer(modelTime).intValue());
		writer.writeInt("inserted/particleNum/" + Integer.toString(bP.getId()) + "/insertionNode", bP.nd.getEnvIndex());
		
		// Write to insertionArray
		insertionArray[bP.getId() - 1] = Integer.toString(bP.getId()) + "_" + 
										 Integer.toString(bP.nd.getEnvIndex()) + "_" +
										 modelDate + "_" + 
										 modelTime;		
	}

	// VKS: Function to perform linear search for an element in an array (adapted from:
	// https://www.geeksforgeeks.org/find-the-index-of-an-array-element-in-java/
	public static int findIndex(int[] arr, int item) {
		// if array is Null
		if (arr == null) {
			return -1;
		}
		// find length of array
		int len = arr.length;
		int i = 0;

		// traverse in the array
		while (i < len) {

			// if the i-th element is item
			// then return the index
			if (arr[i] == item) {
				return i;
			} else {
				i = i + 1;
			}
		}
		return -1;
	}

	// Create an arrays to hold insertions, deaths, and first arrival datetimes for each checkpoint
	public static void createOutputArrays(int numberOfParticles) {
		numParticles = numberOfParticles;
		
		// Create arrays
		insertionArray = new String[numParticles];
		deathArray = new String[numParticles];
		
		firstArrivalArray = new String[checkpoints.length + 4][numParticles];
		int lastIndex = 0;
		for (int i = 0; i < checkpoints.length; i++) {
			checkpointIndices.put(Integer.toString(checkpoints[i]), i);
			lastIndex = i;
		}
		checkpointIndices.put("Chipps", lastIndex + 1);
		checkpointIndices.put("CVP", lastIndex + 2);
		checkpointIndices.put("SWP", lastIndex + 3);
		checkpointIndices.put("Exit", lastIndex + 4);
	}

	// Store a first arrival datetime
	public static void recordFirstArrival(int particleNum, String checkpoint, String datetime) {
		firstArrivalArray[checkpointIndices.get(checkpoint)][particleNum - 1] = datetime;
	}

	public static void destructor() {
		
		// Replace null values in insertionArray and deathArray with "NA"
		for (int i=0; i<numParticles; i++) {
			if(insertionArray[i]==null) {
				insertionArray[i] = "NA";
			}
			if(deathArray[i]==null) {
				deathArray[i] = "NA";
			}
		}
		
		// Replace null values in firstArrivalArray with "NA"
		for (int i = 0; i < (checkpoints.length + 4); i++) {
			for (int j = 0; j < numParticles; j++) {
				if (firstArrivalArray[i][j] == null) {
					firstArrivalArray[i][j] = "NA";
				}
			}
		}

		// Write the insertion and death arrays
		writer.writeStringArray("insertionArray", insertionArray);
		writer.writeStringArray("deathArray", deathArray);
		
		// Write the first arrival arrays
		for (HashMap.Entry<String, Integer> entry : checkpointIndices.entrySet()) {
			writer.writeStringArray("firstArrivalArrays/" + entry.getKey(), firstArrivalArray[entry.getValue()]);
		}

		reader.close();
		System.out.println("Closed " + behaviorParameterFile);
		writer.close();
		System.out.println("Closed " + outputFilename);

		if (testOutType>0) {
			try {
				testWriter.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

}
