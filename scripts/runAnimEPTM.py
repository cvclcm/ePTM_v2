#When converting the output of the the animation binary only
import os

animJar = "D:/Projects/ePTMTutorial/ePTM/ePTM_v2/animateEPTM.jar"
animPath = "D:/Projects/ePTMTutorial/Study/Outputs/"
animFile  = "ePTM_test_anim"

javaCommand = "java -Xss5M -Xms512M -Xmx1024M -jar " + animJar + " " + animPath + " " + animFile
os.system(javaCommand)