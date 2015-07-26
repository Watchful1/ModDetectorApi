package gr.watchful.moddetectorapi;

import java.io.File;
import java.util.ArrayList;

public class Mod implements Comparable<Mod> {
    public ArrayList<File> files;
    public ArrayList<String> modIDs;
    public String version;
    public String shortName;

    public Mod(File file) {
        files = new ArrayList<>();
        files.add(file);
        modIDs = new ArrayList<>();
    }

    public void addID(String ID) {
        modIDs.add(ID);
    }

    public int compareTo(Mod otherMod) {
        if(otherMod == null || otherMod.shortName == null) return 1;
        if(shortName == null) return -1;
        return ModRegistry.getInstance().getInfo(shortName).modName.compareToIgnoreCase(
                ModRegistry.getInstance().getInfo(otherMod.shortName).modName);
    }
}
