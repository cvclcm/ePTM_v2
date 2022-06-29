#When converting the output of the the animation binary only
import os

animJar = "PATH_TO_ANIMATE_EPTM_JAR"
animPath = "PATH_TO_FOLDER_CONTAINING_ANIMATION_OUTPUT"
animFile  = "NAME_OF_ANIMATION_OUTPUT_BINARY_FILE"

javaCommand = "java -Xss5M -Xms512M -Xmx1024M -jar " + animJar + " " + animPath + " " + animFile
os.system(javaCommand)