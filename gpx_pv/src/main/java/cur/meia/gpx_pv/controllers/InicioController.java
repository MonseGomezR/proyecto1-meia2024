package cur.meia.gpx_pv.controllers;

import cur.meia.gpx_pv.HelloApplication;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;

import java.net.URL;
import java.util.Objects;
import java.util.ResourceBundle;

public class InicioController implements Initializable {

    @FXML
    protected void onHelloButtonClick (ActionEvent event) throws Exception {
        Stage stage;
        Scene scene;
        FXMLLoader load;
        Parent root;

        load = new FXMLLoader(HelloApplication.class.getResource("login-view.fxml"));
        root = load.load();
        scene = ((Node) event.getSource()).getScene();

        scene.setRoot(root);

    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

    }
}