package gr.watchful.moddetectorapi;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;

public class ModRegistry {
    private static volatile ModRegistry instance = null;

    Logger logger;

    private HashMap<String, String> shortNameMappings;
    private HashMap<String, ModInfo> modInfoMappings;

    public ModRegistry() {
        logger = Logger.getInstance();

        shortNameMappings = new HashMap<>();
        modInfoMappings = new HashMap<>();
    }

    public static void init() {
        synchronized (ModRegistry.class) {
            instance = new ModRegistry();
        }
    }

    public static ModRegistry getInstance() {
        return instance;
    }

    public void loadMappings(ModInfo[] modInfos) {
        logger.info("Loading " + modInfos.length + " mods from api");

        for(ModInfo modInfo : modInfos) {
            if(modInfo.shortName == null) {
                logger.info("Skipping, no shortname");
                continue;
            }
            if(modInfo.modids == null || modInfo.modids.length() == 0) {
                logger.info("Skipping, no id's: " + modInfo.shortName);
                continue;
            }

            for(String modid : modInfo.modids.split(", ")) {
                shortNameMappings.put(modid, modInfo.shortName);
            }

            modInfo.init();
            modInfoMappings.put(modInfo.shortName, modInfo);
        }
    }

    public String checkID(String modID) {
        return shortNameMappings.get(modID);
    }

    public boolean shortnameExists(String shortName) {
        return shortNameMappings.containsValue(shortName);
    }

    public ModInfo getInfo(String shortName) {
        return modInfoMappings.get(shortName);
    }

    public ArrayList<Mod> processMods(ArrayList<Mod> mods) {
        HashMap<String, Mod> hashMods = new HashMap<>();
        ArrayList<Mod> modsOut = new ArrayList<>();

        String shortName;
        for(Mod mod : mods) {
            shortName = processMod(mod);
            if(shortName == null) {
                modsOut.add(mod);
            } else {
                if (hashMods.containsKey(shortName)) {
                    hashMods.get(shortName).files.addAll(mod.files);
                    hashMods.get(shortName).modIDs.addAll(mod.modIDs);
                } else {
                    mod.shortName = shortName;
                    hashMods.put(shortName, mod);
                }
            }
        }
        hashMods.remove("ignore");

        for(Mod mod : hashMods.values()) {
            modsOut.add(mod);
        }
        Collections.sort(modsOut);
        return modsOut;
    }

    private String processMod(Mod mod) {
        String result;
        String shortName = null;
        if(mod.modIDs.size() > 0) {
            for(String modID : mod.modIDs) {
                result = checkID(modID);
                if(result != null) {
                    if(shortName == null) shortName = result;
                    else if(modID.equals(mod.dominentModID)) shortName = result;
                }
            }
        } else {
            logger.warn("Hit a mod without any mod ids. This shouldn't happen: " + mod.files.get(0).getName());
        }

        return shortName;
    }
}
