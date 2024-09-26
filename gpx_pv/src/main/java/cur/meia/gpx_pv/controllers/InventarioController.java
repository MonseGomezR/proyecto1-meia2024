package cur.meia.gpx_pv.controllers;

import cur.meia.gpx_pv.HelloApplication;
import cur.meia.gpx_pv.database.DBConexion;
import cur.meia.gpx_pv.database.models.Producto;
import cur.meia.gpx_pv.models.BodegaModel;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
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
import javafx.util.Duration;

import java.io.IOException;
import java.sql.SQLException;

public class InventarioController {
    @FXML
    private Label welcomeText, nombreProducto;
    @FXML
    private Button colocarProductoButton, ingresarProductoButton, cancelarButton;
    @FXML
    private AnchorPane panel;
    @FXML
    private TextField cantField;
    @FXML
    private ComboBox<Producto> productosDisponibles;
    @FXML
    private TableView<Producto> productosTable;
    @FXML
    private TableColumn<Producto, Integer> numC, productoC, cantC;
    @FXML
    private TableColumn<Producto, String> nameC;

    private int bodega_actual;
    private BodegaModel bodegaModel;
    private ObservableList<Producto> inventarioList;
    private ObservableList<Producto> productos;
    private Producto producto;

    @FXML
    public void initialize() {
        inventarioList = FXCollections.observableArrayList();
        productos = FXCollections.observableArrayList();
        try {
            welcomeText.textProperty().bind(Bindings.concat("Bienvenido ", DBConexion.usuarioActual.getNombre()));
            bodegaModel = new BodegaModel();
            bodega_actual = bodegaModel.getBodegaActual(DBConexion.usuarioActual.getId_sucursal());
            loadData();
            getAllProducts();

            productosDisponibles.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue != null) {
                    producto = newValue;
                    cantField.setEditable(true);
                    cantField.setDisable(false);
                }
            });

            cantField.textProperty().addListener((observable, oldValue, newValue) -> {
                ingresarProductoButton.setDisable(newValue.isEmpty());
            });
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @FXML
    public void colocarProducto() {
        try {
            bodegaModel.ingresarEstanteria(producto.getId(), Integer.parseInt(cantField.getText()), bodega_actual);
            nombreProducto.setText("Productos ingresados exitosamente.");
            refreshTable();
            cancelar();
        } finally {
            Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(5), event2 -> nombreProducto.setText("")));
            timeline.setCycleCount(1);
            timeline.play();
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

    @FXML
    public void cancelar() {
        cantField.setText("");
        productosDisponibles.setSelectionModel(null);
        cancelarButton.setDisable(true);
        panel.setDisable(true);
        ingresarProductoButton.setDisable(true);
        colocarProductoButton.setDisable(false);
    }

    private void getAllProducts() {
        try {
            productos.clear();
            productosDisponibles.setItems(productos);
            productos.setAll(bodegaModel.getAllDetalles());
            productosDisponibles.setItems(productos);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void loadData() {
        numC.setCellValueFactory(new PropertyValueFactory<>("num"));
        nameC.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        productoC.setCellValueFactory(new PropertyValueFactory<>("id"));
        cantC.setCellValueFactory(new PropertyValueFactory<>("cantidad"));

        refreshTable();
    }

    private void refreshTable() {
        try {
            var resultado = bodegaModel.getAllInventario();
            inventarioList.clear();
            productosTable.setItems(inventarioList);
            for(int i = 0; i < resultado.size(); i++) {
                resultado.get(i).setNum(i + 1);
                inventarioList.add(resultado.get(i));
            }
            productosTable.setItems(inventarioList);
        } catch (SQLException e) {
            throw new RuntimeException(e);

        }
    }

    @FXML
    public void agregarProducto() {
        colocarProductoButton.setDisable(true);
        panel.setDisable(false);
        cancelarButton.setDisable(false);
    }
}
