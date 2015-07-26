package gr.watchful.moddetectorapi;

import org.json.JSONException;
import org.json.JSONObject;
import org.objectweb.asm.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class ModFinder {
    // We need a central place to add ID's for when we can't return what we want
    private Mod workingMod;
    private int rawClasses;

    public ArrayList<Mod> discoverMods(File folder) {
        rawClasses = 0;
        ArrayList<Mod> mods = new ArrayList<>();

        if(folder == null || !folder.exists()) return mods;

        Mod temp;
        if(folder.isFile()) {
            try {
                temp = processFile(folder);
                if (temp != null) {
                    mods.add(temp);
                }
            } catch (IOException | ClassNotFoundException e) {
                mods.add(new Mod(folder));
            }
        } else {
            for (File file : folder.listFiles()) {
                if (file.isDirectory()) mods.addAll(discoverMods(file));
                else {
                    try {
                        temp = processFile(file);
                        if (temp != null) {
                            mods.add(temp);
                        }
                    } catch (IOException | ClassNotFoundException e) {
                        mods.add(new Mod(file));
                    }
                }
            }
        }
        for(Mod mod : mods) {
            if (mod.modIDs.size() == 0) {
                String MD5 = Utils.getMD5(mod.files.get(0));
                if (MD5 == null) {
                    continue;
                } else {
                    workingMod.modIDs.add(MD5);
                }
            }
        }
        if(rawClasses > 0) System.out.println("Raw class files are present in the mods folder\n"+
                "This is not supported by this tool");
        return mods;
    }

    public Mod processFile(File modArchive) throws IOException, ClassNotFoundException {
        workingMod = new Mod(modArchive);

        String ext = Utils.getFileExtension(modArchive);

        if(ext.equals("jar") || ext.equals("zip") || ext.equals("litemod") || ext.equals("disabled")) {
            ZipFile file;
            try {
                file = new ZipFile(modArchive);
            } catch (Exception e) {
                return workingMod;
            }
            Enumeration<? extends ZipEntry> files = file.entries();
            boolean hasClassFiles = false;
            while(files.hasMoreElements()) {
                ZipEntry item = files.nextElement();
                if(item.getName().equals("litemod.json")) {
                    BufferedReader streamReader = new BufferedReader(new InputStreamReader(file.getInputStream(item), "UTF-8"));
                    StringBuilder responseStrBuilder = new StringBuilder();

                    String inputStr;
                    while ((inputStr = streamReader.readLine()) != null)
                        responseStrBuilder.append(inputStr);
                    JSONObject litemodJSON;
                    try {
                        litemodJSON = new JSONObject(responseStrBuilder.toString());
                        String name = (String) litemodJSON.get("name");
                        workingMod.addID(name);
                    } catch (JSONException e) {
                        e.printStackTrace();
                        continue;
                    }
                }
                if(item.getName().equals("META-INF/MANIFEST.MF")) {
                    BufferedReader streamReader = new BufferedReader(new InputStreamReader(file.getInputStream(item), "UTF-8"));

                    String inputStr;
                    while ((inputStr = streamReader.readLine()) != null) {
                        if(inputStr.startsWith("TweakClass:")) {
                            workingMod.addID(inputStr.split(" ")[1]);
                        } else if(inputStr.startsWith("Main-Class:")) {
                            workingMod.addID(inputStr.split(" ")[1]);
                        }
                    }
                }
                if(item.isDirectory() || !item.getName().endsWith("class")) continue;
                hasClassFiles = true;

                ClassReader reader;
                try {
                    reader = new ClassReader(file.getInputStream(item));
                } catch (Exception e) {
                    continue;
                }
                try {
                    reader.accept(new ModClassVisitor(), 0);
                } catch (Exception e) {
                    // do nothing
                }
            }
            file.close();
            if(hasClassFiles) {
                return workingMod;
            } else {
                return null;
            }
        } else if(ext.equals("class")) {
            rawClasses++;
        }
        return null;
    }

    public class ModClassVisitor extends ClassVisitor {
        boolean tmp;

        public ModClassVisitor() {
            super(Opcodes.ASM4);
            tmp = false;
        }

        @Override
        public void visit(int version, int access, String name, String signature,
                          String superName, String[] interfaces) {
            if(superName.equals("BaseMod")) {
                workingMod.addID(name);
            } else if(superName.equals("DummyModContainer") || superName.equals("cpw/mods/fml/common/DummyModContainer")) {
                tmp = true;
                workingMod.addID(name);
            }
        }

        @Override
        public AnnotationVisitor visitAnnotation(String name, boolean runtime) {
            if (name.equals("Lcpw/mods/fml/common/Mod;") || name.equals("Lnet/minecraftforge/fml/common/Mod;") ||
                    name.equals("Lorg/spongepowered/api/plugin/Plugin;")) {
                return new ModAnnotationVisitor();
            } else {
                return new AnnotationVisitor(Opcodes.ASM4) {};
            }
        }

        @Override
        public  MethodVisitor visitMethod(int access, String name, String desc,
                                          String signature, String[] exceptions) {
            if (tmp) {
                //System.out.println("Visiting "+name);
                return new ModMethodVisitor();
            }
            //System.out.println("    "+name);
            return null;
        }
    }

    public class ModAnnotationVisitor extends AnnotationVisitor {
        String id;
        String version;

        public ModAnnotationVisitor() {
            super(Opcodes.ASM4);
        }

        @Override
        public void visit(String key, Object value) {
            if(key.equals("modid") || key.equals("id")) {
                id = value.toString();
            } else if(key.equals("version")) {
                version = value.toString();
            }
        }

        @Override
        public void visitEnum(String name, String desc, String value) {

        }

        @Override
        public void visitEnd() {
            if(id != null) workingMod.addID(id);
            if(version != null) workingMod.version = version;
        }
    }

    public class ModMethodVisitor extends MethodVisitor {
        String temp;
        String id;
        String version;

        public ModMethodVisitor() {
            super(Opcodes.ASM4);
        }

        @Override
        public void visitFieldInsn(int opc, String owner, String name, String desc) {
            if(name.equals("modId")) {
                id = temp;
            } else if(name.equals("version")) {
                version = temp;
            }
        }

        @Override
        public void visitLdcInsn(Object cst) {
            temp = cst.toString();
        }

        @Override
        public void visitEnd() {
            if(id != null) workingMod.addID(id);
            if(version != null) workingMod.version = version;
        }
    }
}
