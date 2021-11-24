# Python script to run the ePTM in batch mode
# Vamsi Krishna Sridharan and Doug Jackson 11/18/2020 vamsi.sridharan@noaa.gov, doug@qedaconsulting.com
import os
import string

# User inputs
runID = 'RUN_ID'    #<---- ***This should be modified by the user***

#<--- ***This block runs the code. As it stands, this will run a single instance of ePTM. You can be creative in your instantiation including sequential and parallel runs of the model from here.
#        As the model does not have interacting simulated fish, and as fish can be released anywhere on the DSM2 grid, one can solve even very large spatial and temporal fish release histories 
#        as an embarrasingly parallel problem      
os.chdir("PATH_TO_THIS_FOLDER")         #<---- ***This should be modified by the user***
os.system("start cmd /k python ePTMValidation.py "+runID)
                                        #Launching a new window for each release-reach combination
                                        #to cycle through all the parameter design points
                                        #<---- ***Use /c instead of /k to close the new instance once it is done***
                                        