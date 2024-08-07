package space.sadfox.owlook.utils;


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public enum ProjectPath {
  OWLERY("owlery"), CONFiG("conf"), DATA("data"), TEMP("temp"), MODULE("module"), MODULE_LIB(
      "module-lib"), MODULE_CONFIG("module-conf");

  private Path path;

  ProjectPath(String path) {
    this.path = Paths.get(path).toAbsolutePath();
  }

  public Path getPath() {
    if (!Files.exists(path)) {
      try {
        Files.createDirectory(path);
      } catch (IOException e) {
        Owlook.registerException(e);
      }
    }
    return path;
  }
}
