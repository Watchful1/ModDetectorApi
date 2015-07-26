# ModDetectorApi
Utility to print minecraft mods in a folder

Usage: ModDetector [Options] Folder1 [Folder2]"  
This program prints the mods and versions in the given folder.  
If a second folder is given, a changelog from Folder2 to Folder1 is printed.  
  -v      Verbose logging  
  -h      Display this help  

Some examples.

For FTB Infinity 1.9.0  
java -jar ModDetectorApi.jar 1_9_0/minecraft/mods  
http://pastebin.com/raw.php?i=Tpm22XUW

For FTB Infinity 1.8.2 to 1.9.0  
java -jar ModDetectorApi.jar 1_9_0/minecraft/mods 1_8_2/minecraft/mods  
http://pastebin.com/raw.php?i=vpFsN6nB
