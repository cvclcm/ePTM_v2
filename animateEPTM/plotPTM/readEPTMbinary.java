//Java class to read in the ePTM animation file information
import java.io.BufferedReader;		//Importing the input-output operations
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;			//Importing the array operations
import java.util.HashMap;
import java.util.Arrays;			
	
import ncsa.hdf.hdf5lib.exceptions.HDF5SymbolTableException;
import ch.systemsx.cisd.hdf5.HDF5Factory;
import ch.systemsx.cisd.hdf5.IHDF5SimpleReader;
import ch.systemsx.cisd.hdf5.IHDF5SimpleWriter;
									//Importing the HDF5 operations
public class readEPTMbinary 		//Program being executed	
{
	//Variable declarations
	public static String animFolder; //Master folder for animations
	public static String filename; 
    public static String animDBFile;									       
	public static String convertedAnimDBFile;
									//Files to read and write to

	//Main program
	public static void main(String[] args) 
	{
		animFolder = args[0];		//passing arguments		
		filename = args[1];
		
		animDBFile = animFolder + "/" + filename + ".bin"; 
		convertedAnimDBFile = animFolder + "/" + filename + "_anim.h5";
									//Assigning animation HDF5 file		
		
		readPTMOutput();			//Reading the output of the animation file	
		
	}
	
	public static void readPTMOutput()
	{
		DataInputStream inputStream = null;
		IHDF5SimpleWriter writer;
		String date;
		int time;
		int numParticles;
		File binFile = new File(animDBFile), h5File = new File(convertedAnimDBFile);
		
		int tempParticleNum, tempChannelNum, tempNormXDist, scratch;
		int[] particleNum, channelNum, normXDist;
		int[] newParticleNum, newChannelNum, newNormXDist;
		int dataPoints;
		boolean EOF;
		int timeStep;
	
		writer = HDF5Factory.open(convertedAnimDBFile);
			
		// Open the input file
		try
		{
			inputStream = new DataInputStream(new FileInputStream(animDBFile));
		} catch (FileNotFoundException e)
		{
			System.out.println("Could not open file " + animDBFile + " Does it exist?");
			System.exit(1);
		}

		// Read the file until the end
		EOF = false;
		timeStep = 0;
		while(!EOF)
		{
			try
			{
				date = inputStream.readUTF();
				time = (int)inputStream.readShort();	
				numParticles = (int)inputStream.readShort();
				
				// Initialize arrays for this time step
				particleNum = new int[numParticles];
				channelNum = new int[numParticles];
				normXDist = new int[numParticles];
					
				dataPoints = 0;
				for(int i=0; i<numParticles; i++)
				{
					tempParticleNum = (int)inputStream.readShort();
					tempChannelNum = (int)inputStream.readShort();
					tempNormXDist = (int)inputStream.readShort();
					scratch = (int)inputStream.readShort();
					scratch = (int)inputStream.readShort();
					scratch = (int)inputStream.readShort();
						
					// Store the data only if the particle is actually in a channel
					if(tempChannelNum != -1)
					{
						particleNum[dataPoints] = tempParticleNum;
						channelNum[dataPoints] = tempChannelNum; 
						normXDist[dataPoints] = tempNormXDist;
						dataPoints++;
					}
				}
					
				newParticleNum = new int[dataPoints];
				newChannelNum = new int[dataPoints];
				newNormXDist = new int[dataPoints];
				System.arraycopy(particleNum, 0, newParticleNum, 0, dataPoints);
				System.arraycopy(channelNum, 0, newChannelNum, 0, dataPoints);
				System.arraycopy(normXDist, 0, newNormXDist, 0, dataPoints);		
				
				// Write to the HDF5 file
				writer.writeString(Integer.toString(timeStep) + "/modelDate", date, 9);
				writer.writeInt(Integer.toString(timeStep) + "/modelTime", time);
				writer.writeIntArray(Integer.toString(timeStep) +"/particleNumber", newParticleNum);
				writer.writeIntArray(Integer.toString(timeStep) +"/channelNumber", newChannelNum);
				writer.writeIntArray(Integer.toString(timeStep) +"/normXDistance", newNormXDist);
				writer.writeInt(Integer.toString(timeStep) + "/numParticles", numParticles);
					
				timeStep++;
				
			} catch (EOFException e)
			{
				EOF = true;
			} catch (IOException e)
			{
				e.printStackTrace();
			}
		}

		writer.close();
				
		System.out.println("Done reading " + animDBFile + " into " + convertedAnimDBFile);
		
	}

}
