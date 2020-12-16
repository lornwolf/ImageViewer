package com.lornwolf.common;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

public class PropertyUtil {

    public static Properties properties;

    public static String readProperties(String key) {
        try {
            if (properties == null) {
                String currentPath = new File(".").getAbsoluteFile().getParent();
                File settingFile = new File(currentPath + "/properties/environment.ini");
                properties =  new Properties();
                if (settingFile.exists()) {
                    properties.load(new InputStreamReader(new FileInputStream(settingFile), "UTF-8"));
                }
            }
            return properties.getProperty(key);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    synchronized public static void setProperties(String key, String value) {
        String currentPath = new File(".").getAbsoluteFile().getParent();
        String filepath = currentPath + "/properties/environment.ini";
        File settingFile = new File(filepath);

        try {
            if (properties == null) {
                if (settingFile.exists()) {
                    properties.load(new InputStreamReader(new FileInputStream(settingFile), "UTF-8"));
                } else {
                    Path path = Paths.get(filepath);
                    if (!path.getParent().toFile().exists()) {
                        Files.createDirectory(path.getParent());
                    }
                    Files.createFile(path);
                }
            }

            properties.setProperty(key, value);
            properties.store(new OutputStreamWriter(new FileOutputStream(settingFile), "UTF-8"), "Comments");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void refresh() {
        try {
            if (properties != null) {
                properties.clear();
            } else {
                properties =  new Properties();
            }
            String currentPath = new File(".").getAbsoluteFile().getParent();
            File settingFile = new File(currentPath + "/properties/environment.ini");
            if (settingFile.exists()) {
                properties.load(new InputStreamReader(new FileInputStream(settingFile), "UTF-8"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String readProperties(String file, String key) {
        Properties property = new Properties();
        try {
            String currentPath = new File(".").getAbsoluteFile().getParent();
            File settingFile = new File(currentPath + file);

            if (settingFile.exists()) {
                property.load(new InputStreamReader(new FileInputStream(settingFile), "UTF-8"));
                return property.getProperty(key);
            } else {
                Path path = Paths.get(currentPath + file);
                if (!path.getParent().toFile().exists()) {
                    Files.createDirectory(path.getParent());
                }
                Files.createFile(path);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    synchronized public static void setProperties(String file, String key, String value) {
        Properties property = new Properties();
        try {
            String currentPath = new File(".").getAbsoluteFile().getParent();
            File settingFile = new File(currentPath + file);

            if (settingFile.exists()) {
                property.load(new InputStreamReader(new FileInputStream(settingFile), "UTF-8"));
            } else {
                Path path = Paths.get(currentPath + file);
                if (!path.getParent().toFile().exists()) {
                    Files.createDirectory(path.getParent());
                }
                Files.createFile(path);
            }

            property.setProperty(key, value);
            property.store(new OutputStreamWriter(new FileOutputStream(settingFile), "UTF-8"), "Comments");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
