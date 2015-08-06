package gr.watchful.moddetectorapi;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

public class main {
    public static void main(String[] args) {
        Logger.init();
        Logger logger = Logger.getInstance();

        if(args.length == 0) {
            logger.message("No args, aborting");
            return;
        }
        if(args[0].equals("-h") || args[0].equals("--help")) {
            logger.message("Usage: ModDetector [Options] Folder1 [Folder2]");
            logger.message("This program prints the mods and versions in the given folder.\n" +
                    "If a second folder is given, a changelog from Folder2 to Folder1 is printed.");
            logger.message("\n  -v      Verbose logging\n" +
                    "  -h      Display this help");
            return;
        }
        boolean onSecondFolder = false;
        File folder = null;
        File oldFolder = null;
        for(String arg : args) {
            if(arg.equals("-v") || arg.equals("--verbose")) {
                logger.logLevel = Logger.INFO;
            } else if(arg.equals("-h") || arg.equals("--help")) {
                logger.message("do not use " + arg + " with other arguments, ignoring");
            } else if(!onSecondFolder) {
                folder = new File(arg);
                onSecondFolder = true;
            } else {
                oldFolder = new File(arg);
            }
        }

        if(folder == null) {
            logger.error("No folder given, aborting");
            return;
        }

        if(!folder.exists())
        {
            logger.error("Folder does not exist, aborting: " + folder.getPath());
            return;
        }

        if(oldFolder != null && !oldFolder.exists()) {
            logger.error("Folder does not exist, aborting: " + oldFolder.getPath());
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

        if(oldFolder != null) {
            ArrayList<Mod> oldMods = new ModFinder().discoverMods(oldFolder);
            oldMods = modRegistry.processMods(oldMods);

            HashMap<String, Mod> oldModsHash = new HashMap<>();
            for(Mod mod : oldMods) {
                String name = mod.shortName == null ? mod.mcmodName : mod.shortName;
                oldModsHash.put(name, mod);
            }

            ArrayList<Mod> addedMods = new ArrayList<>();
            ArrayList<Mod[]> updatedMods = new ArrayList<>();
            ArrayList<Mod> removedMods = new ArrayList<>();

            for(Mod mod : mods) {
                String name = mod.shortName == null ? mod.mcmodName : mod.shortName;
                if(oldModsHash.containsKey(name)) {
                    if(mod.version == null) {
                        logger.warn("Hit a null version, in theory this should never happen");
                    } else {
                        if (!mod.version.equals(oldModsHash.get(name).version)) updatedMods.add(new Mod[]{oldModsHash.get(name),mod});
                    }
                    oldModsHash.remove(name);
                } else {
                    addedMods.add(mod);
                }
            }
            for(Mod mod : oldModsHash.values()) {
                removedMods.add(mod);
            }
            Collections.sort(removedMods);

            for(Mod mod : addedMods) {
                String name = mod.shortName == null ? mod.mcmodName : modRegistry.getInfo(mod.shortName).modName;
                logger.message("Added: " + name + " : " + mod.version);
            }
            for(Mod[] modPair : updatedMods) {
                String name = modPair[1].shortName == null ? modPair[1].mcmodName : modRegistry.getInfo(modPair[1].shortName).modName;
                logger.message("Updated: " + name + " : " + modPair[0].version + " - " + modPair[1].version);
            }
            for(Mod mod : removedMods) {
                String name = mod.shortName == null ? mod.mcmodName : modRegistry.getInfo(mod.shortName).modName;
                logger.message("Removed: " + name);
            }

        } else {
            for (Mod mod : mods) {
                if (mod.shortName == null) logger.message(mod.mcmodName + " : " + mod.version);
                else logger.message(modRegistry.getInfo(mod.shortName).modName + " : " + mod.version);
            }
        }
    }
}
