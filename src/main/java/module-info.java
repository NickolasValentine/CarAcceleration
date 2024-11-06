module com.example.caracceleration {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.example.caracceleration to javafx.fxml;
    exports com.example.caracceleration;
}