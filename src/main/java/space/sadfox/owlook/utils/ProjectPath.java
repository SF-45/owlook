package space.sadfox.owlook.utils;


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public enum ProjectPath {
    CONFiG("conf",false),
    RESOURCES("res",false),
    DATA("data", false),
    TEMP("temp", true),
    LOG("log", false),
    MODULE("module", false),
    MODULE_LIB("module-lib", false),
    MODULE_CONFIG("module-conf", false);

    private Path path;
    private boolean isTemp;

    ProjectPath(String path, boolean isTemp) {
        this.path = Paths.get(path);
        this.isTemp = isTemp;
    }

    public Path getPath() {
        if (!Files.exists(path)) {
            try {
                Files.createDirectory(path);
                if (isTemp) path.toFile().deleteOnExit();
            } catch (IOException e) {
                OwlLogger.registerException(1, e);
            }
        }
        return path;
    }
}
