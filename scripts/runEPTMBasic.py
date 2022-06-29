import os

jarFile = "PATH_TO_JAR_FILE"
configFile = "PATH_TO_EPTM_CONFIG_FILE"
outFile = "PATH_TO_OUTPUT_FILE"
configPath = "PATH_TO_CONFIG_FOLDER"

javaCommand = "java -Xss5M -Xms512M -Xmx1024M -jar " + jarFile + " " + configFile + " " + behaviorFile + " " + outFile + " " + configPath + " " + configPath " " + \
              "ChansOrientsBends812.csv Junctions812.csv latVel.csv latEps.csv latdEp.csv latd2Ep.csv VerEps.csv VerdEp.csv" 
os.system(javaCommand)
