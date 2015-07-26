package gr.watchful.moddetectorapi;

import java.io.File;
import java.util.ArrayList;

public class Mod implements Comparable<Mod> {
    public ArrayList<File> files;
    public ArrayList<String> modIDs;
    public String version;
    public String shortName;

    public transient ArrayList<String> codeVersion;
    public transient String mcmodVersion;
    public transient String mcmodName = null;
    public transient String dominentModID;

    public Mod(File file) {
        files = new ArrayList<>();
        files.add(file);
        modIDs = new ArrayList<>();

        codeVersion = new ArrayList<>();
    }

    public void addID(String ID) {
        modIDs.add(ID);
    }

    public int compareTo(Mod otherMod) {
        if(otherMod == null) return 1;

        String modName = shortName == null ? mcmodName : ModRegistry.getInstance().getInfo(shortName).modName;
        if(modName == null) return -1;

        String otherModName = otherMod.shortName == null ? otherMod.mcmodName : ModRegistry.getInstance().getInfo(otherMod.shortName).modName;
        if(otherModName == null) return 1;

        return modName.compareToIgnoreCase(otherModName);
    }
}
