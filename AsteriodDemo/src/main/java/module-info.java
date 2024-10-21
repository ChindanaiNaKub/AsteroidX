module se233.asterioddemo {
    requires javafx.controls;
    requires javafx.fxml;


    opens se233.asterioddemo to javafx.fxml;
    exports se233.asterioddemo;
}