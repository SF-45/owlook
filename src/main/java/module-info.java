import space.sadfox.owlook.api.Workspace;
import space.sadfox.owlook.base.moduleapi.OwlookModule;
import space.sadfox.owlook.base.moduleapi.OwlookModuleComponent;
import space.sadfox.owlook.base.owl.OwlEntity;
module space.sadfox.owlook {
  requires transitive owlook.base;

  requires transitive javafx.controls;
  requires transitive javafx.fxml;
  requires transitive javafx.web;
  requires transitive javafx.graphics;

  requires transitive org.kordamp.ikonli.javafx;
  requires transitive org.kordamp.ikonli.core;
  requires transitive org.kordamp.ikonli.dashicons;
  requires transitive org.kordamp.ikonli.codicons;
  requires org.apache.commons.io;

  requires java.sql;

  requires eu.hansolo.tilesfx;

  // requires org.fxmisc.richtext;
  // requires org.fxmisc.flowless;
  // requires reactfx;

  opens space.sadfox.owlook.logger to jakarta.xml.bind;
  opens space.sadfox.owlook.utils to jakarta.xml.bind;
  opens space.sadfox.owlook.owlery to jakarta.xml.bind;
  opens space.sadfox.owlook.ui to javafx.fxml;
  opens space.sadfox.owlook.ui.tools to javafx.fxml;

  opens space.sadfox.owlook.moduleloader to javafx.fxml;


  // api

  exports space.sadfox.owlook to javafx.graphics, owlook.base;
  exports space.sadfox.owlook.logger to owlook.base;
  exports space.sadfox.owlook.owlery;
  exports space.sadfox.owlook.api;
  exports space.sadfox.owlook.utils;
  exports space.sadfox.owlook.ui.base;
  exports space.sadfox.owlook.ui.tools;
  exports space.sadfox.owlook.moduleloader;

  uses OwlookModule;
  uses Workspace;
  uses OwlookModuleComponent;
  uses OwlEntity;

}

