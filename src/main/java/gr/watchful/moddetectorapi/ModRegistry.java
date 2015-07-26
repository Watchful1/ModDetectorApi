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
                logger.info("Skipping, no id's: "+modInfo.shortName);
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

        ArrayList<String> shortNames;
        for(Mod mod : mods) {
            shortNames = processMod(mod);

            for(String shortName : shortNames) {
                if(hashMods.containsKey(shortName)) {
                    hashMods.get(shortName).files.addAll(mod.files);
                    hashMods.get(shortName).modIDs.addAll(mod.modIDs);
                } else {
                    mod.shortName = shortName;
                    hashMods.put(shortName, mod);
                }
            }
        }
        hashMods.remove("ignore");

        ArrayList<Mod> modsOut = new ArrayList<>();
        for(Mod mod : hashMods.values()) {
            modsOut.add(mod);
        }
        Collections.sort(modsOut);
        return modsOut;
    }

    private ArrayList<String> processMod(Mod mod) {
        ArrayList<String> shortNames = new ArrayList<>();

        String result;
        HashSet<String> identifiedShortNames = new HashSet<>();
        if(mod.modIDs.size() > 0) {
            for(String modID : mod.modIDs) {
                result = checkID(modID);
                if(result != null) identifiedShortNames.add(result);
            }
        } else {
            logger.warn("Hit a mod without any mod ids. This shouldn't happen: "+mod.files.get(0).getName());
        }

        for(String ID : identifiedShortNames) {
            shortNames.add(ID);
        }
        return shortNames;
    }
}
