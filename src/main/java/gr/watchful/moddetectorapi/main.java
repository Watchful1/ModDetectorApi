package gr.watchful.moddetectorapi;

import java.io.File;
import java.util.ArrayList;

public class main {
    public static void main(String[] args) {
        if(args.length == 0) {
            System.out.println("No args, aborting");
            return;
        }
        if(args[0].equals("-h") || args[0].equals("--help")) {
            System.out.println("Usage: ModDetector [Options] Folder1 [Folder2]");
            System.out.println("This program prints the mods and versions in the given folder.\n" +
                               "If a second folder is given, a changelog between the two is printed.");
            System.out.println("\n  -v      Verbose logging\n" +
                                 "  -h      Display this help");
            return;
        }
        File folder = new File(args[0]);
        if(!folder.exists()) {
            System.out.println("Folder does not exist, aborting: "+folder.getPath());
            return;
        }

        ModRegistry.init();
        ModRegistry modRegistry = ModRegistry.getInstance();

        String json = Utils.downloadToString(Statics.jsonUrl);
        if(json == null) {
            System.out.println("Could not download json, aborting");
            return;
        }

        ModInfo[] modInfos;
        try {
            modInfos = (ModInfo[]) Utils.getObject(json, new ModInfo[1]);
        } catch (Exception e) {
            System.out.println("Unable to parse json, aborting");
            return;
        }
        modRegistry.loadMappings(modInfos);

        ArrayList<Mod> mods = new ModFinder().discoverMods(folder);
        mods = modRegistry.processMods(mods);

        for(Mod mod : mods) {
            System.out.println(modRegistry.getInfo(mod.shortName).modName + " : " + mod.version);
        }
    }
}
