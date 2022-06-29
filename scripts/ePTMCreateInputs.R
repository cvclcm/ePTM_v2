#R script to write behavior HDF5 and PTM configuration files for the different parameter values
library(rhdf5)                                                   #Library for creating HDF5 files
library(zoo)                                                     #Library for handling dates
library(lubridate)
library(tidyverse)                                               #Library for string operations
library(dplyr)                                                   #Library for handling dataframes

#--------------------------------------------------------------------------------------------------------------------
#Static data <----***USER TO MODIFY ALL ENTRIES HERE THAT ARE PLACEHOLDERS
dsm2ConfigDir <- 'PATH_TO_DSM2_CONFIG_FILE'
dsm2ConfigFile <- 'DSM2_CONFIG_FILENAME'
channelPars <- read.csv('PATH_TO_CONFIG_FILES/ePTMv2CalibValidParams812.csv') 
                                                                 #Calibrated ePTM v2 parameters

#Validation datasets
mainDir <- 'PATH_TO_CONFIG_FOLDER'                               #Config folder
inpDir <- 'PATH_TO_INPUTS_FOLDER'                                #Inputs folder
modifier = 'RUN_ID'                           		 		           #Standard filename modifier  
releaseRegime <- 'CSV_WITH_RELEASE_LOCATION_AND_RELEASE_DATE'    #A release timeseries file which contains the release node and the release date for each fish
nRep = NUMBER_OF_STOCHASTIC_REPLICATE_FISH                       #Since ePTM v2 is a stochastic model, it would be a good idea to simulate replicates for each release. Simulate at least 10 replicates and no more than 100
																                                 #for stability 
releaseLocation <- 'LOCATION_COMMON_NAME'                        #Starting location
releaseNode <- LIST_OF_DSM2_INTERNAL_NODE                        #Specify ocations where simulated fish should be released 
checkpoints <- LIST_OF_DSM2_INTERNAL_NODES                    	 #Locations where particle arrival histories are desired. Restrict to about 10 for stability

#-----------------------------------------------------------
#Writing to HDF5 files for junction types
behaviorFile <- paste(inpDir,'BEHAVIOR_PARAMETERS_FILENAME.h5', sep='')
                                                               #HDF5 filename: <--- *This file is read in by ePTM v2 and assigns behavior parameters to fish in different parts of the model domain***
scratch <- h5createFile(behaviorFile)                          #Generating a HDF5 file
h5write(t(as.matrix(channelPars)), file=behaviorFile, name="channelPars")
                                                               #Behavior parameters
h5write(checkpoints, file=behaviorFile, name="checkpoints")    #Checkpoints at reach end
h5write(0.1, file=behaviorFile, name="filterK")                #Legacy parameter not used
h5write(0, file=behaviorFile, name="immortal")                 #Specifying whether particles can die or not: Cycle between 0 and 1. 1 simulates fish that do not die but their survival can be compounded over time.
h5write(0.5, file=behaviorFile, name="initProbOrient")         #Initial random orientation
h5write(4, file=behaviorFile, name="nodeDecision")             #Routing model: Cycle between 0 and 4. Look at Particle.java for a description of modes
h5write('0730', file=behaviorFile, name="sunriseTime")         #Daytime fixed over the entire season
h5write('1700', file=behaviorFile, name="sunsetTime")
h5write(11, file=behaviorFile, name="swimCode")                #Particles with the latest behavior modules
h5write(3, file=behaviorFile, name="testOutType")              #Output writer related variable
h5write(2, file=behaviorFile, name="tideCountThr")             #Deprecated and superceded by pPersistence
h5write(1, file=behaviorFile, name="variableMigrationRate")    #Particle migration speeds can vary with time
h5write(0, file=behaviorFile, name="reservoirResMean")         #Can potentially add reservoir residence behavior if
h5write(0, file=behaviorFile, name="reservoirResStd")          #warranted
h5write(releaseLocation, file=behaviorFile, name="releaseLocation")
                                                               #Release location name
h5write(0, file=behaviorFile, name="releaseGroup")             #Particle release group
h5write('Randomizing', file=behaviorFile, name="runID")        #Run identifier

#Generating parameter assignments to channels
releases <- read.csv(paste(inpDir,releaseRegime,'.csv',sep=''),as.is=TRUE)
releases$time <- mdy_hm(releases$time, tz = "America/Los_Angeles") 
                                                                 #<--- ***Release information: make sure there is a datetime field labeled "time" that contains the release times

attach(releases) 
releases <- releases[order(time),]                             #Sorting by release timing
detach(releases)
  
#-----------------------------------------------------------
#Writing ePTM configuration file
#Getting release schedule and simulation duration information
startDatetime <- releases$time[1] - days(7)                    #Starting and ending timestamps of the simulation     
endDatetime   <- releases$time[nrow(releases)] + days(10)  

startStamp <- paste(sprintf("%02d",day(startDatetime)),        #ePTM simulation timestamps
                    toupper(as.character(month(startDatetime, label = TRUE, abbr = TRUE))),
                    as.character(year(startDatetime)),sep='')
endStamp   <- paste(sprintf("%02d",day(endDatetime)),
                    toupper(as.character(month(endDatetime, label = TRUE, abbr = TRUE))),
                    as.character(year(endDatetime)),sep='')

ptmConfigFile <- paste('PATH_TO_EPTM_CONFIG_FILE', sep='') 
ptmTraceFile  <- paste('PATH_TO_TRACE_OUTPUT_FILE', sep='')
ptmRunFile    <- paste('PATH_TO_POF_FILE', sep='')
ptmEchoFile   <- paste('PATH_TO_ECHO_FILE', sep='')
ptmOutFile    <- paste('PATH_TO_EPTM_OUTPUT_TEXT_FILE', sep='')                     #Config filename
cText = c('# PTM2 input files\n',
          'CONFIGURATION',                                   
          paste('PATH_TO_DSM2_CONFIG_FILE',sep=''),
          'END\n\n',
          'SCALAR',
          'NAME                             VALUE',
          paste('title','                        ','"PTM Sim"',sep=''),
          '',
          paste('display_intvl','                ','1day',sep=''),
          paste('binary_output','                ','false',sep=''),
          paste('dss_direct','                   ','true',sep=''),
          paste('flush_output','                 ','15day',sep=''),
          paste('printlevel','                   ','1',sep=''),
          paste('theta','                        ','0.6',sep=''),
          '',
          paste('run_start_date','               ',startStamp,sep=''),
          paste('run_end_date','                 ',endStamp,sep=''),
          paste('run_start_time','               ','0000',sep=''),			#<--- ***run_start_time and run_end_time can be modified by the user
          paste('run_end_time','                 ','0000',sep=''),
          paste('temp_dir','                     ','${TEMPDIR}',sep=''),
          '',
          paste('ptm_time_step','                ','15min',sep=''),
          paste('ptm_no_animated','              ','2000',sep=''),
          '',
          paste('ptm_ivert','                    ','t',sep=''),
          paste('ptm_itrans','                   ','t',sep=''),
          paste('ptm_iey','                      ','t',sep=''),
          paste('ptm_iez','                      ','t',sep=''),
          '',
          paste('ptm_flux_percent','             ','t',sep=''),
          paste('ptm_group_percent','            ','t',sep=''),
          paste('ptm_flux_cumulative','          ','t',sep=''),
          '',
          paste('ptm_random_seed','              ',round(runif(1, min=10000, max=100000)),sep=''),
          paste('ptm_trans_constant','           ','0.6',sep=''),
          paste('ptm_vert_constant','            ','0.0067',sep=''),
          '',
          paste('ptm_trans_a_coef','             ','1.2',sep=''),
          paste('ptm_trans_b_coef','             ','0.3',sep=''),
          paste('ptm_trans_c_coef','             ','-1.5',sep=''),
          'END\n\n',
          'IO_FILE',
          'MODEL      TYPE      IO      INTERVAL    FILE',
          paste('ptm        trace     out     none        ',ptmTraceFile,sep=''),
          paste('ptm        output    out     none        ',ptmRunFile,sep=''),
          paste('ptm        echo      out     none        ',ptmEchoFile,sep=''),
          'END\n\n', 
          'TIDEFILE',
          'START_DATE      END_DATE      FILE',    
          'runtime         length        ${HYDROTIDEFILE}',
          'END\n\n',
          'GROUP',
          'NAME',
          'chipps_east', 'chipps_west', 'ag_div', 'swp', 'cvp', 'mtz', 'franks', 'whole',
          'END\n\n',
          'GROUP_MEMBER',
          'GROUP_NAME    MEMBER_TYPE     PATTERN',
          'chipps_east   channel         (288|294|291)',
          'chipps_west   channel         (442|437)',
          'ag_div        qext            dicu_div_.*',
          'ag_div        qext            bbid.*',
          'swp           qext            swp',
          'cvp           qext            cvp',
          'mtz           stage           mtz.*',
          'franks        reservoir       franks_tract',
          'whole         channel         .*',
          'whole         reservoir       .*',
          'END\n\n',
          'PARTICLE_GROUP_OUTPUT',
          'NAME         GROUP_NAME         INTERVAL      FILE',
          paste('franks       franks             15min         ',ptmOutFile,sep=''),
          paste('whole        whole              15min         ',ptmOutFile,sep=''),
          paste('whole_15min  whole              1hour         ',ptmOutFile,sep=''),
          'END\n\n',
          'PARTICLE_FLUX_OUTPUT',
          'NAME          FROM_WB              TO_WB                INTERVAL  FILE',
          paste('export_swp    res:clifton_court    group:swp            15min     ',ptmOutFile,sep=''),
          paste('export_cvp    chan:216             group:cvp            15min     ',ptmOutFile,sep=''),
          paste('past_mtz      chan:441             group:mtz            15min     ',ptmOutFile,sep=''),
          paste('past_chipps   group:chipps_east    group:chipps_west    15min     ',ptmOutFile,sep=''),
          paste('diversion_ag  group:all            group:ag_div         15min     ',ptmOutFile,sep=''),
          'END\n\n',
          'PARTICLE_INSERTION',
          'NODE     NPARTS     DELAY     DURATION')            #Standardized config inputs
  
durationStamp <- '0hour'                                       #Duration of release

delay <- rep(0, nrow(releases))                                #Initializing the delay vector
    
for (k in 1:nrow(releases))                                    #Looping through number of releases
{
  relDatetime <- as_datetime(releases$time[k], tz="America/Los_Angeles")  
                                                                 #Timestamp of the release
  delay[k] <- round(as.numeric(as.period(difftime(relDatetime, startDatetime), units = 'hours'), 
                               units='hours'))                 #Delay to particle release
} 
nPStamp <- rep(nRep, length(delay))                             #Initializing array of particles to release
nPStamp <- rowsum(nPStamp,delay)                               #Unique number of particles to release
delay <- unique(delay)                                         #Unique releases 
    
for (k in 1:(length(delay)))                                   #Looping through number of releases
{
  nodeStamp <- as.character(releaseNode)                       #Release node stamp
  delayStamp <- paste(delay[k],'hour',sep='')                  #Delay stamp
  cText <- c(cText,paste(nodeStamp,'    ',as.character(nPStamp[k]),'    ',delayStamp,'    ',
                         durationStamp,sep=''))                #Documenting release 
}
cText <- c(cText,'END')                                        #Ending this section
  
fileCon <- file(ptmConfigFile)                                 #Open file to write
writeLines(cText, fileCon)                                     #Add text to file  
close(fileCon)                                                 #Close the file