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

import java.util.*;
import edu.cornell.RngPack.*;
import java.util.HashMap;
import java.util.Random;

/**
 * 
 * This class is the core definition of a Particle and its movement in a Waterbody. Most of the movement functions are separated to make this a
 * modular class. The core function is updatePosition() and its related.
 * <p>
 *
 * CODING NOTES
 * 
 * 
 * CODING NOTES: POSITION DETERMINATION A Particle is modeled as a non interacting object.
 * 
 * Position in any direction is calculated by adding those components of various displacements over the time step.
 * <p>
 * 
 * For example
 * <p>
 * DX = DX_External_Deterministic + DX_External_Random + DX_Internal_Deterministic + DX_Internal_Random
 * <p>
 * This leads to a more modular approach and helps one to separate out the effect of different components of movement. updatePosition() is the core
 * function for the particle obj.
 * <p>
 * 
 * 
 * CODING NOTES: JUNCTION DECISION Particle's junction decision is based on flow ratios.
 * <p>
 * 
 * CODING NOTES: BOUNDARY REFLECTIONS A Particle during its movement may step out of the boundaries of a Waterbody. This effect is minimized by
 * choosing a sub time step small enough to limit this jump to.
 * <p>
 * However when the Particle does step out of bounds it is bounced back the extraneous distance is traveled in a manner similar to a ball bouncing off
 * the floor.
 * <p>
 * 
 * 
 * CODING NOTES: CROSSING RESERVOIRS The total volume of the Reservoir = Vtotal Flow volume out of Node i = Vi Then the probability that a Particle
 * will leave the Reservoir through a certain Node is proportional to the ratio of Vi:Vtotal.
 * <p>
 * 
 * CODING NOTES: RESERVOIRS PROBLEMS A Particle's swimming in the Reservoir is not simulated instead it is assumed that a Particle entering a
 * Reservoir is fully mixed and therefore it is possible that a Particle entering a Reservoir could exit the Reservoir from some other Node in the
 * next time step giving it almost the speed of light !!! One solution may be to keep track of a Particle's position for the first few time steps in
 * the Reservoir or some other method. Any other solutions can be mailed to use folks.
 * <p>
 * 
 * @author Nicky Sandhu
 * @version $Id: Particle.java,v 1.6.6.1 2006/04/04 18:16:25 eli2 Exp $
 * 
 */
public class Particle {

	/**
	 * unique Particle identity
	 */
	public int Id;
	/**
	 * x position is the distance along the length of the Channel. direction being from upnode to downnode direction
	 */
	public float x;
	/**
	 * y position is the distance from the center line. direction being to the right side if one is facing the +ve x direction.
	 */
	public float y;
	/**
	 * z position is the distance from the bottom of the Channel. direction being the direction from the bottom to the top.
	 */
	public float z;
	/**
	 * age of Particle in minutes since insertion
	 */
	public float age;
	/**
	 * keeps track of if Particle alive or dead
	 */
	public boolean isDead;
	/**
	 * inserted or not inserted
	 */
	public boolean inserted;
	/**
	 *
	 */
	public static boolean DEBUG = false;
	/**
	 * VKS: Type of cross-sectional movement model: 0: No mixing 1: Constant mixing 2: Naive vertical mixing and constant lateral mixing (PTM) 3:
	 * Correct vertical and transverse mixing 4: Advanced length-scale dependent discounting 5: Fish movement control model
	 */
	public static int mixCase;
	public static float minTimeStepMax = 10.0f;

	// Keep track of distance and time traveled
	public HashMap<Integer, double[]> movementTimeDistance = new HashMap<Integer, double[]>();
	public double totalDistance = 0.0;
	public double totalTime = 0.0;

	Random generator = new Random(); // VKS: RNG

	/**
	 * Creates a default Particle The random number generator is initialized to random_seed The vertical/transverse profiles are set to true
	 */
	public Particle(ParticleFixedInfo pFI) {
		totalNumberOfParticles++;
		Id = totalNumberOfParticles;
		if (DEBUG)
			System.out.println("Initializing particle " + Id);
		if (Id == 1)
			Particle.setFixedInfo(pFI);
		if (DEBUG)
			System.out.println("Initializing static info for particle ");
		first = true;
		inserted = false;// particle not in the system yet
		Particle.dfac = 0.1f;
		age = 0;
		wb = null;
		// todo: eli did this work?
		// wb = NullWaterbody.getInstance();
		nd = null;
		isDead = false;
		// if (DEBUG) System.out.println("Fall velocity");
		// fallvel = pFI.getFallVelocity();
		// behaviorData = pFI.getBehavior();
	}

	/**
	 * Sets the location of Particle by identifying the Waterbody it is in and the co-ordinate position w.r.t the Waterbody
	 */
	public final void setLocation(Waterbody w, float xPos, float yPos, float zPos) {
		wb = w;
		x = xPos;
		y = yPos;
		z = zPos;
	}

	/**
	 * Sets the location of Particle by Node Id # and random positioning of Particle within the Waterbody it enters thereupon.
	 */
	public final void setLocation(Node n) {
		nd = n;
	}

	/**
	 * Sets fixed info for Particle
	 */
	public final static void setFixedInfo(ParticleFixedInfo pFI) {

		if (DEBUG)
			System.out.println("in setfixedinfo for particle");
		Particle.verticalConstant = pFI.getVerticalConstant();
		if (DEBUG)
			System.out.println("vertical constant = " + verticalConstant);
		Particle.transverseConstant = pFI.getTransverseConstant();
		if (DEBUG)
			System.out.println("trans constant = " + transverseConstant);
		Particle.CtCv = (float) (Math.sqrt(transverseConstant / verticalConstant));
		if (DEBUG)
			System.out.println("CtCv = " + CtCv);

		Particle.vertMove = pFI.moveVertically();
		if (DEBUG)
			System.out.println("vert move");
		Particle.transMove = pFI.moveLaterally();
		if (DEBUG)
			System.out.println("trans move");
		// Particle.behavior = pFI.getBehaviorExists();
		Channel.useVertProfile = pFI.doVerticalProfile();
		Channel.useTransProfile = pFI.doTransverseProfile();
		Channel.constructProfile();
		Channel.constructProfile();
		// if (DEBUG) System.out.println("set random seed");
		if (randomNumberGenerator == null)
			randomNumberGenerator = new Ranecu(pFI.getRandomSeed());
	}

	/**
	 * Installs observer. This observer observes events such as change from one Waterbody to another and sends a message to other objects from there.
	 */
	public final void installObserver(ParticleObserver ob) {
		observer = ob;
	}

	/**
	 * uninstalls observer, ie. sets it to null. This may save some runtime, however no flux information can be gleaned from the run.
	 */
	public final void uninstallObserver() {
		observer = null;
	}

	/**
	 * gets the location of Particle by returning a pointer to the Waterbody recursionCounter=0; currently only used for animation
	 */
	public final Waterbody getLocation(float[] px, float[] py, float[] pz) {
		Waterbody w;
		w = wb;
		if (inserted) {
			if (wb.getPTMType() == Waterbody.CHANNEL) {
				px[0] = x;
				py[0] = y;
				pz[0] = z;
			} else {
				px[0] = -1;// -1 for animation output use
				py[0] = -1;
				pz[0] = -1;
			}
			return (w);
		} else {
			px[0] = -1;
			py[0] = -1;
			pz[0] = -1;
			return (null);
		}
	}

	/**
	 * returns the unique id of Particle
	 */
	public final int getId() {
		return Id;
	}

	/**
	 * returns the current model time
	 */
	public final int getCurrentParticleTime() {
		return (Globals.currentModelTime);
	}

	/**
	 * updates the position and parameters of Particle.
	 */
	public void updatePosition(float delT) {
		// Initialize tmLeft, since it's now used to interpolate between hydro time steps
		tmLeft = delT;

		particleWait = false; // set or reset particle wait variable
		if (DEBUG)
			System.out.println("In updating position for particle " + this);

		if (inserted) {// after initial insertion
			recursionCounter = 0;
			updateXYZPosition(delT);
			updateOtherParameters(delT);
			// System.out.println("update "+Id);
			if (!isDead)
				checkHealth();
			// if (Id == 1) System.out.println(Id+" "+age+" "+getFallVel());
		} else if (!inserted && Globals.currentModelTime >= insertionTime) {// when current time reach insertion time
			if ((Globals.currentModelTime - insertionTime) / 60.0 > delT)
				// insertion time may set as way before PTM start time
				warning("Particle insertion time specification may be incorrect");
			// may include particles 1 time step before the 1st insertion
			insert();
			recursionCounter = 0;
			updateXYZPosition(delT);
			updateOtherParameters(delT);
		}
	}

	/**
	 * Insertion time and insertion Node
	 */
	public final void setInsertionInfo(int particleInsertionTime, Node injectionNode) {
		this.insertionTime = particleInsertionTime;
		setLocation(injectionNode);
	}

	/**
	 * Get the recent Node which particle just passed or was inserted in
	 */
	public final Node getRecentNode() {
		return nd;
	}

	/**
	 * Get current Waterbody
	 */
	public final Waterbody getCurrentWaterbody() {
		return wb;
	}

	/**
	 * Channel parameters
	 */
	protected float channelLength, channelWidth, channelDepth, channelVave, channelArea, channelStage;

	/**
	 * a flag to see if vertical movement is to be allowed, in other words if vertical mixing is to be allowed
	 */
	protected static boolean vertMove;

	/**
	 * a flag to check if transverse movement is to be allowed.
	 */
	protected static boolean transMove;

	/**
	 * The transverse constant for mixing
	 */
	protected static float transverseConstant;

	/**
	 * The vertical constant for mixing
	 */
	protected static float verticalConstant;

	/**
	 * A transverse diffusivity factor based on the Darcy-Wiesbach friction factor
	 */
	protected static float CtCv;

	/**
	 * Limiting factor for the movement during 1 time step due to mixing. Used for sub-time step calculation to prevent excessive bouncing of
	 * particles at boundaries usually keep particle movement <10% channel (width, depth) in 1 sub-time step
	 */
	protected static float dfac;

	/**
	 * Total number of particles in the system.
	 */
	protected static int totalNumberOfParticles;

	/**
	 * true if it is the first update of position after insertion
	 */
	protected boolean first;

	/**
	 * the Waterbody which the particle currently stays in
	 */
	protected Waterbody wb;

	/**
	 * the Node which the particle was inserted or just passed
	 */
	protected Node nd;

	/**
	 * Time left for completing the current PTM input time step
	 */
	protected float tmLeft;

	/**
	 * Mixing co-efficients
	 */
	protected float Ev, dEvdz, Evdt, dEvdzdt, Et, dEtdy, Etdt, dEtdydt;
	// VKS: Adding correct diffusivity profile

	/**
	 * Falling velocity of Particle through water
	 */
	protected float fallvel;

	/**
	 * A Particle may be asked to wait instead of move with the velocity An example is when seep flows control at a Node and Particle to seep has been
	 * turned off. The Particle is asked to remain at its current position.
	 */
	protected boolean particleWait;

	/**
	 * Gaussian random number generator for y and z dispersive movements
	 */
	protected static RandomElement randomNumberGenerator;

	/**
	 * Particle observer
	 */
	protected ParticleObserver observer;

	/**
	 * updates the Particle position for the given time step; input time step is usually divided into small sub-time step to complete the calculation;
	 * The Particle is moved for the time step given; The new position of the Particle is available as Particle.x, Particle.y and Particle.z
	 */
	protected final void updateXYZPosition(float delT) {
		if (wb.getPTMType() == Waterbody.CHANNEL) {
			if (DEBUG)
				System.out.println("Particle " + this + " in channel " + wb.getEnvIndex());
			tmLeft = delT;

			// update sub-time step due to y & z mixing
			int numOfSubTimeSteps = getSubTimeSteps(delT);
			float tmstep = delT / numOfSubTimeSteps;
			// PTM internal calculation time step
			float tmToAdv = 0;

			// y, z set up for particles which are just out of reservoir and conveyor
			if (Macro.APPROX_EQ(y, MISSING) || Macro.APPROX_EQ(z, MISSING)) {
				setYZLocationInChannel();
			}

			// update particle's x,y,z position every sub-time step
			while (tmLeft > 0 && isDead == false) {
				if (tmLeft >= tmstep) {// for all sub-time steps except the last
					tmToAdv = tmstep;
				} else {// for the last sub-time step; deal with division precision & truncation
					tmToAdv = tmLeft;
				}

				age += tmToAdv;
				updateAllParameters(tmToAdv);
				if (particleWait == false) {
					x = calcXPosition(tmToAdv);
					// particle into reservoir/conveyor, out of the whole function
					if (wb.getPTMType() != Waterbody.CHANNEL)
						return;
					if (isDead == false) {// save time if particle's dead
						y = calcYPosition(tmToAdv);
						z = calcZPosition(tmToAdv);
					}
				} // end if(particleWait)
				tmLeft -= tmToAdv;
			} // end while
		} // end if(CHANNEL)

		else if (wb.getPTMType() == Waterbody.RESERVOIR) {
			if (DEBUG)
				System.out.println("Particle " + this + " in reservoir " + wb.getEnvIndex());

			tryCrossReservoir(delT);
		}

		else if (wb.getPTMType() == Waterbody.CONVEYOR) {
			if (DEBUG)
				System.out.println("Particle " + this + " in conveyor " + wb.getEnvIndex());
			// zero time delay
			moveInConveyor(delT);
		}

		else if (wb.getPTMType() == Waterbody.BOUNDARY) {
			if (DEBUG)
				System.out.println("Particle " + this + " in boundary " + wb.getEnvIndex());
			isDead = true;
		}
	}

	/**
	 * x,y,z positioning called after Particle insertion or returned from Reservoir/Conveyor
	 */
	protected final void setXYZLocationInChannel() {
		if (wb.getPTMType() == Waterbody.CHANNEL) {
			x = getXLocationInChannel();
			if (first)
				updateChannelParameters();
			setYZLocationInChannel();
		} else {
			x = MISSING;
			y = MISSING;
			z = MISSING;
		}
	}

	/**
	 * y, z positioning for particle just out of reservoir/conveyor w random numbers generation
	 */
	protected final void setYZLocationInChannel() {
		y = ((Channel) wb).getWidth(x, tmLeft) * (wb.getRandomNumber() - 0.5f);
		z = ((Channel) wb).getDepth(x, tmLeft) * wb.getRandomNumber();
	}

	/**
	 * X Position calculation for the time step given
	 */
	protected final float calcXPosition(float timeStep) {
		// get current position
		float xPos = this.x;

		// calculate position after timeStep
		xPos = xPos + calcXDisplacementExtDeterministic(timeStep) + calcXDisplacementExtRandom(timeStep) + calcXDisplacementIntDeterministic(timeStep)
		+ calcXDisplacementIntRandom(timeStep);

		// when particle crossing the coming node, into next wb
		if (isNodeReached(xPos) == true) {
			float timeToReachNode = calcTimeToNode(xPos);
			tmLeft -= timeToReachNode;
			age = age - timeStep + timeToReachNode;

			// update time spent and distance moved in this waterbody
			recordMovement(xPos, true, timeToReachNode);

			// block particle before it enters a node, with filter operation 0
			if (nd.inFilter(wb)) {
				tmLeft = 0;
				age = age - timeToReachNode + timeStep;// Kijin: should this be 2*timeStep
				if (xPos <= 0.0f) {// upstream
					xPos = 0;
				} else if (xPos >= channelLength) {// downstream
					xPos = channelLength;
				}
			} else {
				// make decision on what wb to be entered
				y = calcYPosition(timeToReachNode);
				z = calcZPosition(timeToReachNode);
				makeNodeDecision();
			}

			// if (recursionCounter++ > 5) error("Too many recursions in calcXPosition(float)");
			if (recursionCounter++ > 5) {
				if (repositionFactor < MAX_R_F) {
					repositionFactor += RFIncrement;
					//					if (wb instanceof Channel)
					//					{
					//						System.out.println("Reposition Factor set to "
					//								+ repositionFactor + " for particle " + getId()
					//								+ " at node " + ((Channel) wb).getUpNodeId()
					//								+ ", tmLeft = " + Float.toString(tmLeft)
					//								+ ", currentModelTime = "
					//								+ Integer.toString(Globals.currentModelTime));
					//					} else
					//					{
					//						System.out
					//								.println("Reposition Factor set to "
					//										+ repositionFactor
					//										+ " for particle "
					//										+ getId()
					//										+ " at node connected to a non-channel waterbody");
					//					}
				}
				recursionCounter = 0;
			}

			// update XYZ for the rest of 1 PTM input time step
			if (tmLeft > 1.0e-3f) {
				updateXYZPosition(tmLeft);
			}

			return x;
		} // end if (nodeReached)
		else {
			// update time spent and distance moved in this waterbody
			recordMovement(xPos, false, timeStep);
			return xPos;
		}
	}

	/**
	 * Y Position calculation for the time step given
	 */
	protected final float calcYPosition(float timeStep) {
		// get current position
		float yPos = this.y;

		// calculate position after timeStep
		yPos = yPos + calcYDisplacementExtDeterministic(timeStep) + calcYDisplacementExtRandom(timeStep) + calcYDisplacementIntDeterministic(timeStep)
		+ calcYDisplacementIntRandom(timeStep);

		// reflection from banks of Channel
		int k = 0;
		int MAX_BOUNCING = 100; // max num of iterations to do reflection
		float halfWidth = channelWidth / 2.0f;
		while ((yPos < -halfWidth || yPos > halfWidth) && (k <= MAX_BOUNCING)) {
			if (yPos < -halfWidth)
				yPos = -halfWidth + (-halfWidth - yPos);
			if (yPos > halfWidth)
				yPos = halfWidth - (yPos - halfWidth);
			k++;
		}

		if (k > MAX_BOUNCING)
			error("Too many iterations in calcYPosition()");
		return (yPos);
	}

	/**
	 * Z Position calculation for time step given
	 */
	protected final float calcZPosition(float timeStep) {
		// get current position
		float zPos = this.z;
		// calculate position after timeStep

		zPos = zPos + calcZDisplacementExtDeterministic(timeStep) + calcZDisplacementExtRandom(timeStep) + calcZDisplacementIntDeterministic(timeStep)
		+ calcZDisplacementIntRandom(timeStep);

		// reflections from bottom of Channel and water surface
		int k = 0;
		int MAX_BOUNCING = 100;
		while ((zPos < 0.0 || zPos > channelDepth) && (k <= MAX_BOUNCING)) {
			if (zPos < 0.0)
				zPos = -zPos;
			else if (zPos > channelDepth)
				zPos = channelDepth - (zPos - channelDepth);
			k++;
		}
		if (k > MAX_BOUNCING) {
			error("Too many iterations in calcZPosition()");
		}
		return (zPos);
	}

	/**
	 * Externally induced Deterministic x
	 */
	protected float calcXDisplacementExtDeterministic(float timeStep) {
		float xVel = calcXVelocityExtDeterministic();
		return (xVel * timeStep);
	}

	/**
	 * Externally induced Random x
	 */
	protected float calcXDisplacementExtRandom(float timeStep) {
		return 0.0f;
	}

	/**
	 * Internally induced Deterministic x
	 */
	protected float calcXDisplacementIntDeterministic(float timeStep) {
		float xVel = calcXVelocityIntDeterministic();
		return (xVel * timeStep);
	}

	/**
	 * Internally induced Random x
	 */
	protected float calcXDisplacementIntRandom(float timeStep) {
		return 0.0f;
	}

	/**
	 * Externally induced Deterministic y
	 */
	protected float calcYDisplacementExtDeterministic(float timeStep) {
		return 0.0f;
	}

	/**
	 * Externally induced Random y
	 */
	protected float calcYDisplacementExtRandom(float timeStep) {
		// get y random mixing component
		float dy = (float) (randomNumberGenerator.gaussian() * Etdt);
		// return the random y movement if transverse mixing allowed
		if (transMove)
			return (dy);
		else
			return 0.0f;
	}

	/**
	 * Internally induced Deterministic y
	 */
	protected float calcYDisplacementIntDeterministic(float timeStep) {
		return 0.0f;
	}

	/**
	 * Internally induced Random y
	 */
	protected float calcYDisplacementIntRandom(float timeStep) {
		return 0.0f;
	}

	/**
	 * Externally induced Deterministic z
	 */
	protected float calcZDisplacementExtDeterministic(float timeStep) {
		// return(-getFallVel()*timeStep);
		return 0.0f;
	}

	/**
	 * Externally induced Random z
	 */
	protected float calcZDisplacementExtRandom(float timeStep) {
		// get z random mixing component
		float dz = (float) ((randomNumberGenerator.gaussian() * Evdt) + dEvdzdt);
		// VKS: Adding derivative to get correct stochastic solution
		// return the random z movement if vertical mixing allowed
		if (vertMove)
			return (dz);
		else
			return 0.0f;
	}

	/**
	 * Internally induced Deterministic z
	 */
	protected float calcZDisplacementIntDeterministic(float timeStep) {
		return 0.0f;
	}

	/**
	 * Internally induced Random z
	 */
	protected float calcZDisplacementIntRandom(float timeStep) {
		return 0.0f;
	}

	/**
	 * Externally induced Deterministic x
	 */
	protected float calcXVelocityExtDeterministic() {
		float xVel;

		if (wb instanceof SmartChannel) {
			// return (((Channel) wb).getVelocity(x, y, z, channelVave, channelWidth, channelDepth));
			// Getting the curvature and bend direction
			xVel = ((SmartChannel) wb).getVel(x, y, z, channelVave, channelWidth, channelDepth);
		} else
			xVel = 0.0f;

		return (xVel);

	}

	/**
	 * Externally induced Random x
	 */
	protected float calcXVelocityExtRandom() {
		return 0.0f;
	}

	/**
	 * Internally induced Deterministic x
	 */
	protected float calcXVelocityIntDeterministic() {
		return 0.0f;
	}

	/**
	 * Internally induced Random x
	 */
	protected float calcXVelocityIntRandom() {
		return 0.0f;
	}

	/**
	 * Makes Node decision on which Waterbody to enter into next; update nd, wb, x
	 */
	protected void makeNodeDecision() {

		// Node is the current Node in which particle entered
		// get total outflow from Node and multiply it by random number
		// send message to observer about change
		if (observer != null)
			observer.observeChange(ParticleObserver.NODE_CHANGE, this);
		float outflow = nd.getTotalEffectiveOutflow(false, tmLeft);

		// if the Node is at a Node with zero flow, for example at the
		// end of a slough, then move the pParticle into the Channel a
		// small amount.
		if (outflow == 0.0f && nd.getNumberOfWaterbodies() == 1) {
			x = getPerturbedXLocation();
			return;
		}
		// float out2 = outflow;

		float rand = nd.getRandomNumber();
		outflow = rand * outflow;

		float flow = 0.0f;

		if (outflow == 0.0) {
			particleWait = true;
			return;
		}

		// loop determines which wb is for particle to enter
		int waterbodyId = -1;
		do {
			waterbodyId++;
			// this conditional statement added to exclude seepage
			// this should be read in as an argument
			// @todo: disabled this feature
			// if(nd.getWaterbody(waterbodyId).getAccountingType() != flowTypes.evap){
			flow += nd.getFilterOp(waterbodyId) * nd.getOutflow(waterbodyId, tmLeft);
			// }
		} while (flow < outflow && waterbodyId < nd.getNumberOfWaterbodies());

		// get a pointer to the waterbody in which pParticle entered.
		wb = nd.getWaterbody(waterbodyId);
		// send message to observer about change
		if (observer != null)
			observer.observeChange(ParticleObserver.WATERBODY_CHANGE, this);
		// set x as beginning of Channel...
		x = getXLocationInChannel();
		// @todo: redesign the coding structure of this feature
	}

	/**
	 * updates pParticle position after calling makeReservoirDecision
	 */
	protected final void tryCrossReservoir(float timeStep) {

		// adjust time and age
		age += timeStep;
		tmLeft = tmLeft - timeStep;

		// Record the amount of time spent in the reservoir
		recordMovement(0.0f, true, timeStep);

		// get a pointer to the Node into which pParticle enters from Reservoir
		nd = makeReservoirDecision(timeStep);

		if (nd != null) {
			// makes decision of which Waterbody to go into
			makeNodeDecision();
			// set previous depth and width to current depth and width
			first = true;
			// ? what should be new x,y,z for the pParticle in the Waterbody?
			setXYZLocationInChannel();
		}
	}

	/**
	 * moves to the Node with inflow and decides where to go from there...
	 */
	protected void moveInConveyor(float delT) {
		Conveyor c = (Conveyor) wb;
		if (DEBUG)
			System.out.println("Particle in conveyor: " + c);
		float flow = c.getFlowInto(0, tmLeft);
		// TODO
		if ((flow > 0) && !(c.getNode(1).inFilter(wb)))// if no filter
			setLocation(c.getNode(1));
		else
			setLocation(c.getNode(0));
		if (DEBUG)
			System.out.println("Current node: " + nd);
		makeNodeDecision();
		if (DEBUG)
			System.out.println("Current wb: " + wb);
		if (DEBUG)
			System.out.println(" wb type: " + wb.getPTMType() + ", waterbody.CHANNEL=" + Waterbody.CHANNEL);
		if (wb.getPTMType() == Waterbody.CHANNEL) {
			first = true;
			setXYZLocationInChannel();
		}
	}

	/**
	 * returns Node to which pParticle transitions or null
	 */
	protected Node makeReservoirDecision(float timeStep) {

		// Get total volume of Reservoir and multiply by random number
		float totvol = ((Reservoir) wb).getTotalVolume(timeStep);
		float rand = wb.getRandomNumber();
		totvol = totvol * rand;

		// Get flow volume out first Node
		int nodeId = -1;
		float flowvol = 0.0f;
		// while outflow volume over last i nodes is less than random value
		do {
			nodeId++;
			nd = wb.getNode(nodeId);
			if (nd.inFilter(wb)) {
				continue;
			} // skip the node if it has a res->nd filter
			flowvol += Math.max(0.0f, ((Reservoir) wb).getVolumeOutflow(nodeId, timeStep, tmLeft));
		} while (flowvol < totvol && nodeId < wb.getNumberOfNodes() - 1);

		if (flowvol > totvol) {
			return wb.getNode(nodeId);
		}
		else {
			return null;
		}
	}

	/**
	 * generates error
	 */
	protected final void error(String msg) {
		System.out.println("An error occurred in particle.cc: ");
		System.out.println("ERROR: " + msg);
		System.exit(-1);
	}

	/**
	 * generates warning
	 */
	protected final void warning(String msg) {
		System.out.println("WARNING: " + msg + " !");
	}

	/**
	 * inputs state of pParticle
	 */
	/**
	 * outputs state to ostream
	 */

	private static final float Emin = 0.0001f;
	private static final int MAX_NUM_OF_SUB_TIME_STEPS = 10000;
	private static final int MISSING = -99999;

	/**
	 * insert particle in the system
	 */
	protected void insert() {
		if (observer != null)
			observer.observeChange(ParticleObserver.INSERT, this);
		inserted = true;
		makeNodeDecision();
		setXYZLocationInChannel();
	}

	/**
	 *  
	 */
	private final void updateAllParameters(float tmstep) {
		// updates length,width,depth, previousdepth, previouswidth
		updateChannelParameters();
		// updates particles: depth, width, Ev, Evdt, Etdt
		updateParticleParameters(tmstep);
	}

	/**
	 * updates channel length, width, depth, average velocity, area and previous depth, width
	 */
	private final void updateChannelParameters() {
		((Channel) wb).updateChannelParameters(x, cL, cW, cD, cV, cA, cS, tmLeft);
		channelLength = cL[0];
		channelWidth = cW[0];
		channelDepth = cD[0];
		channelVave = cV[0];
		channelArea = cA[0];
		if (first) {
			// previous=current, if transfer from reservoir/conveyor to channel
			previousChannelDepth = channelDepth;
			previousChannelWidth = channelWidth;
			first = false;
		}
		channelStage = cS[0];
	}

	/**
	 *  
	 */
	protected void updateOtherParameters(float delT) {
	}

	/**
	 * updates particle y, z position, Ev, Evdt, Etdt
	 */
	protected void updateParticleParameters(float timeStep) {
		// VKS: Variables needed for profile use
		float zP, yP;
		float yfrac, zfrac;
		String curvHead;
		int bendDir;
		float channelVaveAbs = Math.abs(channelVave);

		// map y & z in new xsection over the node
		z = z * channelDepth / previousChannelDepth;
		y = y * channelWidth / previousChannelWidth;

		// set previouses to the news..
		previousChannelDepth = channelDepth;
		previousChannelWidth = channelWidth;

		switch (mixCase) {
		case 0: // No mixing

			// Vertical diffusion
			Evdt = 0.0f;
			dEvdzdt = 0.0f;

			// Lateral diffusion
			Etdt = 0.0f;
			dEtdydt = 0.0f;

			break;

		case 1: // Constant mixing

			// Vertical diffusion
			Ev = 0.07f * (0.05f * channelVaveAbs) * channelDepth;
			Evdt = (float) Math.sqrt(2.0f * Ev * timeStep);
			dEvdzdt = 0.0f;

			// Lateral diffusion
			Et = CtCv * Ev;
			Etdt = (float) Math.sqrt(2.0f * Et * timeStep);
			dEtdydt = 0.0f;

			break;

		case 2: // Naive vertical mixing

			// Vertical diffusion
			Ev = 0.05f * 0.41f * channelVaveAbs * z * (1 - z / channelDepth); // VKS: Parabolic profile
			Ev = Math.max(Ev, Emin);
			Evdt = (float) Math.sqrt(2.0f * Ev * timeStep);
			dEvdzdt = 0.0f;

			// Lateral diffusion
			Et = 0.6f * (0.05f * channelVaveAbs) * channelWidth;
			Etdt = (float) Math.sqrt(2.0f * Et * timeStep);
			dEtdydt = 0.0f;

			break;

		case 3: // With Visser (1997) correction

			// Vertical diffusion
			dEvdz = 0.05f * 0.41f * channelVaveAbs * (1 - 2 * (z / channelDepth)); // VKS: Linear profile

			zP = z + 0.5f * dEvdz * timeStep;
			if (zP > channelDepth) // Left water surface
				zP = Math.max(2*channelDepth - zP, 0);
			else if (zP < 0.0f) // Left channel bottom
				zP = Math.min(-zP, channelDepth);

			Ev = 0.05f * 0.41f * channelVaveAbs * zP * (1 - zP / channelDepth);
			Ev = Math.max(Ev, Emin);
			dEvdz = Math.max(dEvdz, Emin); // VKS: To account for very small value
			Evdt = (float) Math.sqrt(2.0f * Ev * timeStep);
			dEvdzdt = (float) (dEvdz * timeStep); // VKS: deterministic addition to trajectory

			// Lateral diffusion
			Et = 0.6f * (0.05f * channelVaveAbs) * channelWidth;
			Etdt = (float) Math.sqrt(2.0f * Et * timeStep);

			dEtdydt = 0.0f;

			break;

		case 4: // Correct diffusion without cognizance of fish scales of motion
			// Note: Fix timestep at 10s and see how it changes vs what it is now.
			// Vertical diffusion
			zfrac = z / channelDepth; 

			dEvdz = channelVaveAbs * RiverBendsInput.getVertPosVal(zfrac,2); //VKS: Linear profile with
			//Ross and Sharples (2005) approximations

			Ev = 0.07f * (0.05f * channelVaveAbs) * channelDepth;
			Evdt = (float) Math.sqrt(2.0f * Ev * timeStep);
			dEvdzdt = 0.0f;

			// Lateral diffusion
			if (wb instanceof SmartChannel) {
				// Getting the curvature and bend direction
				curvHead = ((SmartChannel)wb).curvHead;
				bendDir = ((SmartChannel)wb).bendDir;

				yfrac = 2.0f * y / channelWidth;
				dEtdy = channelVaveAbs * RiverBendsInput.getLatPosVal(yfrac, curvHead, bendDir, 2);

				yP = y + 0.5f * dEtdy * timeStep;
				if (yP > (channelWidth/2.0f)) // Left right bank
					yP = Math.max(channelWidth - yP, -channelWidth/2.0f);
				else if (yP < -(channelWidth/2.0f)) // Left left bank
					yP = Math.min(-channelWidth - yP, channelWidth/2.0f);

				yfrac = 2.0f * yP / channelWidth;
				Et = channelVaveAbs * channelWidth * RiverBendsInput.getLatPosVal(yfrac, curvHead, bendDir, 1);
				Et = Math.max(Et, Emin);
				dEtdy = channelVaveAbs * RiverBendsInput.getLatPosVal(yfrac, curvHead, bendDir, 2);
				Etdt = (float) Math.sqrt(2.0f * Et * timeStep);
				dEtdydt = (float) (dEtdy * timeStep); // VKS: deterministic addition to trajectory
			} else {
				Etdt = 0.0f;
				dEtdydt = 0.0f;
			}
			break;			
		}
	}

	/**
	 * true is the particle reach the next node within 1 timestep
	 */
	private final boolean isNodeReached(float xpos) {
		if (xpos < 0.0f) {// crossed starting Node of Channel
			nd = wb.getNode(Channel.UPNODE);
		} else if (xpos > channelLength) {// crossed ending Node of Channel
			nd = wb.getNode(Channel.DOWNNODE);
		} else
			return false;

		if (particleWait)
			return false; // false if the particle is asked to wait

		return true;
	}

	/**
	 * calculate the rest time for particle reaching its target node
	 */
	private final float calcTimeToNode(float xpos) {

		float dT = 0.0f;
		// get velocity sum in X direction
		float xVel = (calcXVelocityExtDeterministic() + calcXVelocityIntDeterministic() + calcXVelocityExtRandom() + calcXVelocityIntRandom());

		float xStart = x;

		// calculate time taken to reach Node
		if (xpos < 0.0f) {// if starting node
			dT = -xStart / xVel;
		} else if (xpos > channelLength) {// if ending node
			dT = (channelLength - xStart) / xVel;
		}

		return dT;
	}

	/**
	 * get the sub-time step used for PTM calculation it is always <= input time step
	 */
	private final int getSubTimeSteps(float timeStep) {
		float minTimeStep = timeStep;

		if ((vertMove != true) && (transMove != true))
			minTimeStep = timeStep;
		else
			minTimeStep = getMinTimeStep();

		int numOfSubTimeSteps = 1;
		if (minTimeStep < timeStep)
			numOfSubTimeSteps = (int) (timeStep / minTimeStep + 1);
		else
			numOfSubTimeSteps = 1;

		if (numOfSubTimeSteps > MAX_NUM_OF_SUB_TIME_STEPS) {
			warning("Number Of Sub Time Steps exceeds a maximum of " + MAX_NUM_OF_SUB_TIME_STEPS);
			return MAX_NUM_OF_SUB_TIME_STEPS;
		} else
			return numOfSubTimeSteps;
	}

	/**
	 * Minimum time step with vertical or transverse mixing turned on with consider of terminal fall velocity & particle's travel distance <10%
	 * width/depth in 1 sub-time step
	 */
	private final float getMinTimeStep() {
		// fallvel -. input behavior
		// float terminalVelocity=Math.max(getFallVel(),1.0e-10f);
		float terminalVelocity = getTerminalVelocity();

		// get a factor of the Channel
		// the maximum distance a pParticlecan travel in a time step
		updateAllParameters(0.0f); // time step not really a factor here.
		float dzmax = dfac * channelDepth;
		float dymax = dfac * channelWidth;
		float dtz = Math.min(dzmax / terminalVelocity, dzmax * dzmax / Ev);
		float dty = (dymax * dymax) / (CtCv * CtCv * Ev);

		float minTimeStep = 0.0f;

		if ((vertMove == true) && (transMove == true))
			minTimeStep = Math.min(Math.min(dty, dtz), minTimeStepMax);
		else if ((vertMove == true) && (transMove != true))
			minTimeStep = Math.min(dtz, minTimeStepMax);
		else if ((vertMove != true) && (transMove == true))
			minTimeStep = Math.min(dty, minTimeStepMax);

		return minTimeStep;
	}

	/**
	 * factor used for calculating minimum time step set outside of method to allow overwriting to include fall velocity
	 */
	private float getTerminalVelocity() {
		return 1.0e-10f;
	}

	/**
	 * Counts number of recursions done to calculate X position avoid too much small time-step recursion at dead-end channel or it may crash the
	 * computer
	 */
	private static int recursionCounter;

	/**
	 * Factor used in repositioning when a no outflow condition is encountered
	 */
	private float repositionFactor = 0.00001f;

	/**
	 * Reposition Factor increment
	 */
	private float RFIncrement = 0.0001f;

	/**
	 * Maximum Repositioning Factor
	 */
	private float MAX_R_F = 0.01f;

	/**
	 * Location state variables to map y,z from previous xsec to new
	 */
	private float previousChannelDepth, previousChannelWidth;

	/**
	 * Insertion time for particle
	 */
	private int insertionTime;

	/**
	 * gets x location in Channel corresponding to upnode and downnode.
	 */
	protected final float getXLocationInChannel() {
		float newXPosition = 0.0f;
		if (wb.getPTMType() == Waterbody.CHANNEL) {
			if (((Channel) wb).getUpNodeId() == nd.getEnvIndex())
				newXPosition = 0;
			if (((Channel) wb).getDownNodeId() == nd.getEnvIndex())
				newXPosition = ((Channel) wb).getLength();
		}
		return newXPosition;
	}

	/**
	 * moves x location in Channel by a repositioning factor from the closest Node.
	 */
	protected float getPerturbedXLocation() {
		float newXPosition = 0.0f;
		if (wb.getPTMType() == Waterbody.CHANNEL) {
			if (((Channel) wb).getUpNodeId() == nd.getEnvIndex())
				newXPosition = channelLength * repositionFactor;

			if (((Channel) wb).getDownNodeId() == nd.getEnvIndex())
				newXPosition = ((Channel) wb).getLength() - (channelLength * repositionFactor);
		}
		return newXPosition;
	}

	protected void checkHealth() {
		// used for behavior
	}

	/**
	 * String representation
	 */
	public String toString() {
		String rep = Id + " ";
		if (this.wb != null)
			rep += this.wb.getEnvIndex() + " ";
		else
			rep += -1 + " ";
		if (this.inserted) {
			rep += this.x + " " + this.y + " " + this.z + " ";
		} else {
			rep += -1.0f + " " + -1.0f + " " + -1.0f + " ";
		}
		rep += this.insertionTime + " ";
		if (this.nd != null)
			rep += this.nd.getEnvIndex() + " ";
		else
			rep += -1 + " ";
		return rep;
	}

	public void fromString(String rep) {
		StringTokenizer sToken = new StringTokenizer(rep);
		try {
			String token = sToken.nextToken();
			Id = (new Integer(token)).intValue();
			token = sToken.nextToken();
			int wbNum = (new Integer(token)).intValue();

			if (wbNum != -1) {
				this.wb = Globals.Environment.getWaterbody(wbNum);
				this.inserted = true;
			} else {
				this.wb = null;
				this.inserted = false;
			}

			token = sToken.nextToken();
			x = (new Float(token)).floatValue();
			token = sToken.nextToken();
			y = (new Float(token)).floatValue();
			token = sToken.nextToken();
			z = (new Float(token)).floatValue();
			token = sToken.nextToken();
			insertionTime = (new Integer(token)).intValue();
			token = sToken.nextToken();
			int nodeNum = (new Integer(token)).intValue();
			if (nodeNum != -1)
				this.nd = Globals.Environment.getNode(nodeNum);
		} catch (NoSuchElementException e) {
			System.out.println("Exception while parsing particle string representation");
		}
	}

	// array applied for the convenience of vars transfer
	private float[] cL = new float[1];
	private float[] cW = new float[1];
	private float[] cD = new float[1];
	private float[] cV = new float[1];
	private float[] cA = new float[1];
	private float[] cS = new float[1];

	// Randomly choose an index based on a vector of weights
	public int weightedChoice(double weights[]) {
		int index, numWeights = weights.length;
		double rand;
		double[] cumulativeWeights = new double[numWeights];

		// Calculate the cumulative weights vector
		cumulativeWeights[0] = weights[0];
		for (int i = 1; i < numWeights; i++) {
			cumulativeWeights[i] = cumulativeWeights[i - 1] + weights[i];
		}

		// Randomly choose a location in the cumulativeWeights vector
		rand = (double) nd.getRandomNumber() * cumulativeWeights[numWeights - 1];

		for (index = 0; index < weights.length; index++) {
			if (rand < cumulativeWeights[index]) {
				break;
			}
		}

		return index;

	}

	// Keep track of time spent and distance traveled in a given channel
	public void recordMovement(float xP, boolean reachedNode, float time) {
		float xStart = this.x;
		double[] timeDistance = new double[2];
		double distance = 0.0f;

		if (reachedNode) {
			// calculate distance traveled to reach node
			if (xP < 0.0f) {
				distance = xStart;
			} else if (xP > channelLength) {
				distance = channelLength - xStart;
			}

		} else {
			distance = xP - xStart;
		}

		// Use the absolute value to account for cases when the particle is
		// moving upstream
		distance = (double) Math.abs(distance);

		this.totalDistance += distance;
		this.totalTime += time;

		// If there's already a movementTimeDistance stored for this channel,
		// just add to it
		if (movementTimeDistance.containsKey(wb.getEnvIndex())) {
			timeDistance = movementTimeDistance.get(wb.getEnvIndex());
			time += timeDistance[0];
			distance += timeDistance[1];
		}
		timeDistance[0] = time;
		timeDistance[1] = distance;
		movementTimeDistance.put(wb.getEnvIndex(), timeDistance);

	}
}
