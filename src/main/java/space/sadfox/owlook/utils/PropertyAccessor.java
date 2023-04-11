package space.sadfox.owlook.utils;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class PropertyAccessor {

    private static final File configName = ProjectPath.CONFiG.getPath().resolve("config").toFile();
    private static Properties properties = new Properties();
    private static List<PropertyListener> listenerList = new ArrayList<>();

    static {
        try  {
            if (!configName.exists()) configName.createNewFile();
            try (FileInputStream inputStream = new FileInputStream(configName)) {
                properties.load(inputStream);
            }
        } catch (IOException e) {
            ErrorLogger.registerException(e);
        }
    }
    public static String getProperty(ProjectProperty property) {
        if (properties.containsKey(property.toString())) {
            return properties.getProperty(property.toString());
        } else {
            setProperty(property, property.getDefaultValue());
            return property.getDefaultValue();
        }
    }

    public static void setProperty(ProjectProperty property, String value) {
        try (FileOutputStream outputStream = new FileOutputStream(configName)) {
            properties.setProperty(property.toString(), value);
            properties.store(outputStream, "");
            notifyListeners(property);
        } catch (IOException e) {
            ErrorLogger.registerException(e);
        }
    }
    public static void addListener(PropertyListener listener) {
        listenerList.add(listener);
    }
    public static void notifyListeners(ProjectProperty property) {
        listenerList.forEach(e -> e.update(property));
    }
}
