package gr.watchful.moddetectorapi;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Utils {
    public static String getFileExtension(File file) {
        if(file == null || !file.exists()) return null;
        int extPos = file.getAbsolutePath().lastIndexOf(".");
        int sepPos = file.getAbsolutePath().lastIndexOf(File.pathSeparator);
        if(extPos == -1) return null;
        if(sepPos > extPos) return null;
        return file.getAbsolutePath().substring(extPos+1);
    }

    public static String getMD5(File file) {
        try (FileInputStream inputStream = new FileInputStream(file)) {
            MessageDigest digest = MessageDigest.getInstance("MD5");

            byte[] bytesBuffer = new byte[1024];
            int bytesRead = -1;

            while ((bytesRead = inputStream.read(bytesBuffer)) != -1) {
                digest.update(bytesBuffer, 0, bytesRead);
            }

            byte[] hashedBytes = digest.digest();

            return convertByteArrayToHexString(hashedBytes);
        } catch (NoSuchAlgorithmException | IOException ex) {
            System.out.println("Couldn't generate hash");
            return null;
        }
    }

    private static String convertByteArrayToHexString(byte[] arrayBytes) {
        StringBuffer stringBuffer = new StringBuffer();
        for (int i = 0; i < arrayBytes.length; i++) {
            stringBuffer.append(Integer.toString((arrayBytes[i] & 0xff) + 0x100, 16)
                    .substring(1));
        }
        return stringBuffer.toString();
    }

    public static String downloadToString(String url)  {
        URL website = null;
        try {
            website = new URL(url);
        } catch (MalformedURLException e) {
            System.out.println("Couldn't connect to "+url);
            return null;
        }
        URLConnection connection = null;
        try {
            connection = website.openConnection();
        } catch (IOException e) {
            System.out.println("Couldn't connect to " + url);
            return null;
        }
        BufferedReader in = null;
        try {
            in = new BufferedReader(
                    new InputStreamReader(connection.getInputStream()));
        } catch (IOException e) {
            System.out.println("Couldn't read " + url);
            return null;
        }

        StringBuilder response = new StringBuilder();
        String inputLine;

        try {
            while ((inputLine = in.readLine()) != null)
                response.append(inputLine);
        } catch (IOException e) {
            System.out.println("Trouble reading " + url);
            return null;
        }

        try {
            in.close();
        } catch (IOException e) {
            System.out.println("Couldn't close connection to " + url);
        }

        return response.toString();
    }

    public static Object getObject(String JSON, Object object) {
        Gson gson = new Gson();
        Object tempObject;
        try {
            tempObject = gson.fromJson(JSON, object.getClass());
        } catch (JsonSyntaxException excp) {
            System.out.println("returning null");
            return null;
        }
        return tempObject;
    }
}
