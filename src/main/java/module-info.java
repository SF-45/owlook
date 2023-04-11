module space.sadfox.owlook {
	requires transitive javafx.base;
    requires transitive javafx.controls;
    requires transitive javafx.fxml;
    requires transitive javafx.web;
    requires transitive javafx.graphics;

    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.ikonli.dashicons;
    requires org.kordamp.ikonli.core;


    requires org.controlsfx.controls;
    requires eu.hansolo.tilesfx;

    requires transitive java.persistence;
    requires transitive java.sql;
    requires transitive java.naming;
    requires transitive java.desktop;

    requires transitive jakarta.xml.bind;

    requires org.fxmisc.richtext;
    requires org.fxmisc.flowless;
    requires reactfx;
    
    opens space.sadfox.owlook.components.logger to jakarta.xml.bind;
    opens space.sadfox.owlook.components.bootpatch to jakarta.xml.bind;
    
    exports space.sadfox.owlook to javafx.graphics;
    
    
    // api
    
    exports space.sadfox.owlook.moduleapi;
    exports space.sadfox.owlook.jaxb;
    exports space.sadfox.owlook.utils;
    exports space.sadfox.owlook.components.bootpatch;
    exports space.sadfox.owlook.ui.base;
    
    uses space.sadfox.owlook.moduleapi.Module;
    
    opens space.sadfox.owlook.ui to javafx.fxml;
    
    

//    opens space.sadfox.owlook.ui to javafx.base;
//    opens space.sadfox.owlook.ui.controllers to javafx.fxml;
//    opens space.sadfox.owlook.ui.controllers.setting to javafx.fxml;
//    opens space.sadfox.owlook.ui.fxelements to javafx.fxml;
//    opens space.sadfox.owlook.ui.schemeviews to javafx.base;
//
//    opens space.sadfox.owlook.jaxb to jakarta.xml.bind, javafx.base;
//    opens space.sadfox.owlook.utils to jakarta.xml.bind, javafx.base;
//
//
//    exports space.sadfox.owlook to javafx.graphics;
//    exports space.sadfox.owlook.jaxb.adapters;
}