module se233.asterioddemo {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.media;
    requires java.logging;
    requires java.xml;
    requires java.desktop;


    opens se233.asterioddemo to javafx.fxml;
    exports se233.asterioddemo;
}