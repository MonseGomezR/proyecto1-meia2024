package cur.meia.gpx_pv.controllers;

import cur.meia.gpx_pv.HelloApplication;
import cur.meia.gpx_pv.database.DBConexion;
import cur.meia.gpx_pv.models.LoginModel;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class LoginController implements Initializable {

    @FXML
    private TextField userField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label errorMessage;

    private final LoginModel loginModel;

    public LoginController() {
        // Inicializamos el modelo de autenticación
        loginModel = new LoginModel();
    }

    @FXML
    private void handleLogin(ActionEvent event) {
        String username = userField.getText();
        String password = passwordField.getText();

        Scene scene;
        FXMLLoader load;
        Parent root;

        try {
            if (loginModel.authenticate(username, password)) {
                errorMessage.setText("Login exitoso");

                switch (DBConexion.usuarioActual.getTipoUsuario()) {
                    case 1:
                        load = new FXMLLoader(HelloApplication.class.getResource("admin-view.fxml"));
                        root = load.load();
                        scene = ((Node) event.getSource()).getScene();
                        scene.setRoot(root);

                        break;
                    case 2:
                        load = new FXMLLoader(HelloApplication.class.getResource("cajero-view.fxml"));
                        root = load.load();
                        scene = ((Node) event.getSource()).getScene();
                        scene.setRoot(root);
                        break;
                    case 3:
                        load = new FXMLLoader(HelloApplication.class.getResource("bodega-view.fxml"));
                        root = load.load();
                        scene = ((Node) event.getSource()).getScene();
                        scene.setRoot(root);
                        break;
                    case 4:
                        load = new FXMLLoader(HelloApplication.class.getResource("inventario-view.fxml"));
                        root = load.load();
                        scene = ((Node) event.getSource()).getScene();
                        scene.setRoot(root);
                        break;
                }

            } else {
                errorMessage.setText("Credenciales incorrectas");
            }
        } catch (SQLException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

    }
}
