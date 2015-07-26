package gr.watchful.moddetectorapi;

import sun.rmi.runtime.Log;

import java.io.File;
import java.util.ArrayList;

public class main {
    public static void main(String[] args) {
        Logger.init();
        Logger logger = Logger.getInstance();

        if(args.length == 0) {
            logger.message("No args, aborting");
            return;
        }
        int argCounter = 0;
        if(args[0].equals("-h") || args[0].equals("--help")) {
            logger.message("Usage: ModDetector [Options] Folder1 [Folder2]");
            logger.message("This program prints the mods and versions in the given folder.\n" +
                    "If a second folder is given, a changelog between the two is printed.");
            logger.message("\n  -v      Verbose logging\n" +
                    "  -h      Display this help");
            return;
        }
        if(args[0].equals("-v")) {
            logger.logLevel = Logger.INFO;
            argCounter++;
        }

        File folder = new File(args[argCounter]);
        if(!folder.exists()) {
            logger.error("Folder does not exist, aborting: " + folder.getPath());
            return;
        }

        ModRegistry.init();
        ModRegistry modRegistry = ModRegistry.getInstance();

        String json = Utils.downloadToString(Statics.jsonUrl);
        if(json == null) {
            logger.error("Could not download json, aborting");
            return;
        }

        ModInfo[] modInfos;
        try {
            modInfos = (ModInfo[]) Utils.getObject(json, new ModInfo[1]);
        } catch (Exception e) {
            logger.error("Unable to parse json, aborting");
            return;
        }
        modRegistry.loadMappings(modInfos);

        ArrayList<Mod> mods = new ModFinder().discoverMods(folder);
        mods = modRegistry.processMods(mods);

        for(Mod mod : mods) {
            logger.message(modRegistry.getInfo(mod.shortName).modName + " : " + mod.version);
        }
    }
}
