package space.sadfox.owlook.utils;

import org.kordamp.ikonli.Ikon;
import org.kordamp.ikonli.codicons.Codicons;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;

public enum MessageLevel {
  DEBUG(Codicons.DEBUG, Color.GREEN), INFO(Codicons.INFO, Color.ROYALBLUE), WARNING(
      Codicons.WARNING, Color.DARKORANGE), ERROR(Codicons.ERROR, Color.RED);

  private Ikon icon;
  private Paint color;

  private MessageLevel(Ikon icon, Paint color) {
    this.icon = icon;
    this.color = color;
  }

  public Ikon getIkon() {
    return icon;
  }

  public Paint getPaint() {
    return color;
  }
}
