# Python script to cycle through each design point for a release-reach combination
# Vamsi Krishna Sridharan and Doug Jackson 11/18/2020 vamsi.sridharan@noaa.gov, doug@qedaconsulting.com

from __future__ import print_function
import os
import argparse
import string
import subprocess

# User inputs
parser = argparse.ArgumentParser(description="A program to setup and run an instance of ePTM")
                                        #Description of parses object    
parser.add_argument("iS", type=str)     #Release
args = parser.parse_args()              #Passing arguments
iS = args.iS

#Setup
behaviorFile = "BEHAVIOR_PARAMETERS_FILENAME"   #<---- ***These entries to be modified by the user***
configFile = "EPTM_CONFIG_FILENAME"               
outFile = "EPTM_OUTPUT_FILENAME"
mainPath = "PATH_TO_JAR_FILE"          
calibPath = "PATH_TO_EPTM_CONFIG_FILE"          #<---- ***Put the behavior H5 file here as well***
outPath = "PATH_TO_OUTPUTS_FOLDER"
configPath = "PATH_TO_FOLDER_CONTAINING_EPTM_CONFIGURATION_CSVs"     

# Assigning the model grid
nodesIntExtFile = os.path.join(configPath, "nodesIntExt_v8_1_2.csv")            #<---- ***If you develop your own DSM2 model, make sure to update these files to reflect the new grid
channelsIntExtFile = os.path.join(configPath, "channelsIntExt_v8_1_2.csv")    

#Running the model
javaCommand = "java -Xss5M -Xms512M -Xmx1024M -jar "+mainPath+"ePTM.jar "+setupPath+configFile+iS+".inp "+setupPath+behaviorFile+".h5 "+ \
              outPath+outFile+iS+"_out.h5 "+configPath+" "+configPath+" "+"ChansOrientsBends812.csv Junctions812.csv latVel.csv latEps.csv latdEp.csv latd2Ep.csv VerEps.csv VerdEp.csv"
                                        #Command to execute  <--- ***You can change velocity and mixing profiles in your instance of the model. If you do so, make sure to update the last six files above accordingly***
print(javaCommand)
os.system(javaCommand)                  #Run the java command