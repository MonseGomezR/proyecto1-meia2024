package cur.meia.gpx_pv.controllers;

import cur.meia.gpx_pv.HelloApplication;
import cur.meia.gpx_pv.database.DBConexion;
import cur.meia.gpx_pv.database.models.Producto;
import cur.meia.gpx_pv.database.models.Sucursal;
import cur.meia.gpx_pv.database.models.Venta;
import cur.meia.gpx_pv.models.AdminModel;
import cur.meia.gpx_pv.models.BodegaModel;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;

import java.io.IOException;
import java.sql.Date;
import java.sql.SQLException;



public class AdminController {

    @FXML
    private Label welcomeText, nombreProducto;
    @FXML
    private Button colocarProductoButton, ingresarProductoButton, cancelarButton;
    @FXML
    private TableView<Producto> productosTable;
    @FXML
    private TableView<Venta> ventasTable;
    @FXML
    private TableView<Sucursal> sucursalesTable;

    @FXML
    private TableColumn<Producto, Integer> pNumC, cantC;
    @FXML
    private TableColumn<Producto, String> productoC;

    @FXML
    private TableColumn<Sucursal, Integer> sNumC;
    @FXML
    private TableColumn<Sucursal, Double> sVentasC;
    @FXML
    private TableColumn<Sucursal, String> sucursalC;

    @FXML
    private TableColumn<Venta, Integer> vNumC, nitC, empleadoC;
    @FXML
    private TableColumn<Venta, Date> fechaC;
    @FXML
    private TableColumn<Producto, String> nameC;
    @FXML
    private TableColumn<Producto, Double> vTotalC;


    private AdminModel adminModel;
    private ObservableList<Producto> topP;
    private ObservableList<Venta> topV;
    private ObservableList<Sucursal> topS;

    public void initialize() {
        topV = FXCollections.observableArrayList();
        topP = FXCollections.observableArrayList();
        topS = FXCollections.observableArrayList();
        welcomeText.textProperty().bind(Bindings.concat("Bienvenido ", DBConexion.usuarioActual.getNombre()));
        adminModel = new AdminModel();
        loadDataTV();
        loadDataTP();
        loadDataTS();
    }

    private void loadDataTS() {
        sNumC.setCellValueFactory(new PropertyValueFactory<>("id"));
        sucursalC.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        sVentasC.setCellValueFactory(new PropertyValueFactory<>("vendido"));

        refreshTableTS();
    }

    private void refreshTableTS() {
        try {
            topS.setAll(adminModel.topSucursales());
            sucursalesTable.setItems(topS);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void loadDataTP() {
        pNumC.setCellValueFactory(new PropertyValueFactory<>("id"));
        productoC.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        cantC.setCellValueFactory(new PropertyValueFactory<>("cantidad"));

        refreshTableTP();
    }

    private void refreshTableTP() {
        try {
            topP.setAll(adminModel.topProductos());
            productosTable.setItems(topP);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void loadDataTV() {
        vNumC.setCellValueFactory(new PropertyValueFactory<>("id"));
        nitC.setCellValueFactory(new PropertyValueFactory<>("nit"));
        fechaC.setCellValueFactory(new PropertyValueFactory<>("fecha"));
        vTotalC.setCellValueFactory(new PropertyValueFactory<>("total"));
        empleadoC.setCellValueFactory(new PropertyValueFactory<>("empleado"));

        refreshTableTV();
    }

    private void refreshTableTV() {
        try {
            topV.setAll(adminModel.topVentas());
            ventasTable.setItems(topV);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
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


}
