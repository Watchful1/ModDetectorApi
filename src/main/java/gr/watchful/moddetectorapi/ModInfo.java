package gr.watchful.moddetectorapi;

import java.util.ArrayList;

public class ModInfo {
    public static final int OPEN = 1;
    public static final int NOTIFY = 2;
    public static final int REQUEST = 3;
    public static final int FTB = 4;
    public static final int CLOSED = 5;
    public static final int UNKNOWN = 6;

    public String shortName;

    public String modName;
    public String modAuthors;
    public String modLink;

    public String licenseLink;
    public String privateLicenseLink;

    public String publicStringPolicy;
    public String privateStringPolicy;

    public String modids;

    private transient int publicPolicy;
    private transient int privatePolicy;

    public ModInfo(String shortName) {
        this.shortName = shortName;

        init();
    }

    public void init() {
        if(shortName == null || shortName.equals("")) System.out.println("Trying to init a ModInfo with a null shortname, this is bad");
        if(modName == null) modName = "";
        if(modAuthors == null) modAuthors = "";
        if(modLink == null) modLink = "";
        if(licenseLink == null) licenseLink = "";
        if(privateLicenseLink == null) privateLicenseLink = "";
        if(publicStringPolicy == null) publicStringPolicy = "Unknown";
        if(privateStringPolicy == null) privateStringPolicy = "Unknown";
        if(publicPolicy == 0) publicPolicy = stringToIntPolicy(publicStringPolicy);
        if(privatePolicy == 0) privatePolicy = stringToIntPolicy(privateStringPolicy);
    }

    private int stringToIntPolicy(String policy) {
        switch (policy) {
            case "Open":
                return OPEN;
            case "Notify":
                return NOTIFY;
            case "Request":
                return REQUEST;
            case "FTB":
                return FTB;
            case "Closed":
                return CLOSED;
            default:
                return UNKNOWN;
        }
    }

    public boolean hasPublic() {
        if(publicPolicy == OPEN || publicPolicy == FTB) return true;
        else return false;
    }

    public boolean hasPrivate() {
        if(privatePolicy == OPEN || privatePolicy == FTB) return true;
        else return false;
    }

    public String getPermLink(boolean isPublic) {
        if(isPublic || privateLicenseLink.equals("")) return licenseLink;
        else return privateLicenseLink;
    }

    public int getPolicy(boolean isPublic) {
        if(isPublic) return publicPolicy;
        else return privatePolicy;
    }

    public static String getStringPolicy(int num) {
        switch (num) {
            case OPEN:
                return "Open";
            case FTB:
                return "FTB";
            case NOTIFY:
                return "Notify";
            case REQUEST:
                return "Request";
            case CLOSED:
                return "Closed";
            default:
                return "Unknown";
        }
    }
}
