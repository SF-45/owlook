import space.sadfox.owlook.base.moduleapi.OwlookModule;
import space.sadfox.owlook.base.moduleapi.OwlookModuleComponent;
module space.sadfox.owlook {
	requires transitive owlook.base;

	requires transitive javafx.controls;
	requires transitive javafx.fxml;
	requires transitive javafx.web;
	requires transitive javafx.graphics;

	requires org.kordamp.ikonli.javafx;
	requires org.kordamp.ikonli.dashicons;
	requires org.kordamp.ikonli.core;
	requires org.apache.commons.io;

	requires java.sql;

	requires eu.hansolo.tilesfx;

	// requires org.fxmisc.richtext;
	// requires org.fxmisc.flowless;
	// requires reactfx;

	opens space.sadfox.owlook.logger to jakarta.xml.bind;
	opens space.sadfox.owlook to jakarta.xml.bind;
	opens space.sadfox.owlook.ui to javafx.fxml;
	opens space.sadfox.owlook.ui.tools to javafx.fxml;
	
	opens space.sadfox.owlook.moduleloader to javafx.fxml;


	// api

	exports space.sadfox.owlook to javafx.graphics, owlook.base;
	exports space.sadfox.owlook.component;
	exports space.sadfox.owlook.utils;
	exports space.sadfox.owlook.logger;
	exports space.sadfox.owlook.ui.base;
	exports space.sadfox.owlook.ui.tools;
	exports space.sadfox.owlook.moduleloader;

	uses OwlookModule;
	uses OwlookModuleComponent;

}