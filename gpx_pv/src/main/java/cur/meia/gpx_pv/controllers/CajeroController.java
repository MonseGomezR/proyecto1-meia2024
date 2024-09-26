package cur.meia.gpx_pv.controllers;

import cur.meia.gpx_pv.HelloApplication;
import cur.meia.gpx_pv.database.DBConexion;
import javafx.beans.binding.Bindings;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import java.io.IOException;
import java.sql.SQLException;

public class CajeroController {
    @FXML
    private AnchorPane contentArea;
    @FXML
    private StackPane infoArea;
    @FXML
    private Button nfButton, ecButton, csButton;
    @FXML
    private Label welcomeText;

    @FXML
    public void handleNuevaFactura() {
        cargarFactura();
    }

    @FXML
    public void handleEditarCliente() {
        cargarEditor();
    }

    @FXML
    public void handleCerrarSesion(ActionEvent event) {
        try {
            FXMLLoader load = new FXMLLoader(HelloApplication.class.getResource("inicio-view.fxml"));
            Parent root = load.load();
            Scene scene = ((Node) event.getSource()).getScene();
            scene.setRoot(root);
            DBConexion.usuarioActual = null;
            DBConexion.conexion.close();
        } catch (IOException | SQLException e) {
            throw new RuntimeException(e);
        }
    }

        @FXML
    public void initialize() {
        welcomeText.textProperty().bind(Bindings.concat("Bienvenido ", DBConexion.usuarioActual.getNombre()));
    }

    private void cargarFactura() {
        try {
            FXMLLoader loader = new FXMLLoader(HelloApplication.class.getResource("nueva-factura-info-view.fxml"));
            Node newContent = loader.load();
            infoArea.getChildren().clear();
            infoArea.getChildren().add(newContent);
            NFController nfController = loader.getController();

            FXMLLoader loader2 = new FXMLLoader(HelloApplication.class.getResource("nueva-factura-view.fxml"));
            newContent = loader2.load();
            contentArea.getChildren().clear();
            contentArea.getChildren().add(newContent);
            NFTableController nfTableController = loader2.getController();


            nfTableController.setNfController(nfController);
            nfController.setNfTableController(nfTableController);
            nfController.setCajeroController(this);

            ecButton.setDisable(true);
            nfButton.setDisable(true);
            csButton.setDisable(true);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void cargarEditor() {

        try {
            FXMLLoader loader = new FXMLLoader(HelloApplication.class.getResource("editar-cliente-view.fxml"));
            Node newContent = loader.load();
            contentArea.getChildren().clear();
            contentArea.getChildren().add(newContent);
            EditarClienteController editarClienteController = loader.getController();
            editarClienteController.setCajeroController(this);

            ecButton.setDisable(true);
            nfButton.setDisable(true);
            csButton.setDisable(true);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void volverEditor() {
        ecButton.setDisable(false);
        nfButton.setDisable(false);
        csButton.setDisable(false);
        contentArea.getChildren().clear();
    }

    public void cerrarVenta() {
        ecButton.setDisable(false);
        nfButton.setDisable(false);
        csButton.setDisable(false);
        contentArea.getChildren().clear();
        infoArea.getChildren().clear();
    }




}
