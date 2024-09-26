module cur.meia.gpx_pv {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires org.kordamp.ikonli.javafx;
    requires java.sql;
    requires java.desktop;

    opens cur.meia.gpx_pv to javafx.fxml;
    exports cur.meia.gpx_pv;
    exports cur.meia.gpx_pv.controllers;
    opens cur.meia.gpx_pv.controllers to javafx.fxml;
    opens cur.meia.gpx_pv.database.models to javafx.base;
}