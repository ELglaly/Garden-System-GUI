module org.example.projectt6 {
    requires javafx.controls;
    requires javafx.fxml;


    opens org.example.projectt6 to javafx.fxml;
    exports org.example.projectt6;
}