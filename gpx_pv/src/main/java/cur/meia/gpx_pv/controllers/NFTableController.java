package cur.meia.gpx_pv.controllers;

import cur.meia.gpx_pv.database.DBConexion;
import cur.meia.gpx_pv.database.models.*;
import cur.meia.gpx_pv.models.FacturaModel;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.Duration;

import java.sql.SQLException;


public class NFTableController {
    @FXML
    private TableView<DetalleV> productTable;
    @FXML
    private TableColumn<DetalleV, Integer> numC, codeC, cantC;
    @FXML
    private TableColumn<DetalleV, String> productC;
    @FXML
    private TableColumn<DetalleV, Double> totalC, unitC;
    @FXML
    private TextField cantidadProducto;
    @FXML
    private Label agregarProductoMsg;
    @FXML
    public Button agregarProductoButton, eliminarProductoButton;
    @FXML
    public ComboBox<Producto> productosDisponibles;

    FacturaModel fm;
    Producto producto;
    NFController nfController;

    ObservableList<DetalleV> productList = FXCollections.observableArrayList();
    ObservableList<Producto> disponibilidad = FXCollections.observableArrayList();

    public void initialize() {
        productTable.setPlaceholder(new Label(""));
        fm = new FacturaModel();

        productosDisponibles.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                producto = newValue;
                cantidadProducto.setEditable(true);
                cantidadProducto.setDisable(false);
            }
        });

        cantidadProducto.textProperty().addListener((observable, oldValue, newValue) -> {
            agregarProductoButton.setDisable(newValue.isEmpty());
        });

        productTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            eliminarProductoButton.setDisable(newSelection == null);
        });

        agregarProductoButton.setDisable(true);
        cantidadProducto.setEditable(false);
        loadDate();
        buscarDisponibilidad(DBConexion.usuarioActual.getId_sucursal());
    }

    private void refreshTable() {
        try {
            var resultado = fm.getAllDetalles(NFController.id_factura);
            productList.clear();
            productTable.setItems(productList);
            for(int i = 0; i < resultado.size(); i++) {
                resultado.get(i).setId(i);
                productList.add(resultado.get(i));
            }
            productTable.setItems(productList);
        } catch (SQLException e) {
            throw new RuntimeException(e);

        }
    }

    @FXML
    private void agregarDetalleVenta() {
        DetalleV nuevo = new DetalleV(productList.size()+1, NFController.id_factura, producto.getId(), producto.getNombre(), Integer.parseInt(cantidadProducto.getText()), producto.getPrecio(), producto.getPrecio() * Integer.parseInt(cantidadProducto.getText()));
        try {
            agregarProductoMsg.setText(fm.agregarDetalle(nuevo));
            cantidadProducto.setText("");
            agregarProductoButton.setDisable(true);
            refreshTable();
            buscarDisponibilidad(DBConexion.usuarioActual.getId_sucursal());
            nfController.actualizarTotal();
            Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(5), event2 -> agregarProductoMsg.setText("")));
            timeline.setCycleCount(1);
            timeline.play();
        } catch (SQLException e) {
            agregarProductoMsg.setText(e.getMessage().split("\n")[0]);
        }
    }

    @FXML
    public void eliminarProducto() {
        var selectedDetalle = productTable.getSelectionModel().getSelectedItem();
        if (selectedDetalle != null) {
            productList.remove(selectedDetalle);
            try {
                fm.borrarDetalle(selectedDetalle.id_producto);
                nfController.actualizarTotal();
                buscarDisponibilidad(DBConexion.usuarioActual.getId_sucursal());
                eliminarProductoButton.setDisable(true);
                refreshTable();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void setNfController (NFController nfController) {
        this.nfController = nfController;
    }

    private void loadDate() {
        numC.setCellValueFactory(new PropertyValueFactory<>("id"));
        codeC.setCellValueFactory(new PropertyValueFactory<>("id_producto"));
        productC.setCellValueFactory(new PropertyValueFactory<>("nombre_producto"));
        cantC.setCellValueFactory(new PropertyValueFactory<>("cantidad"));
        unitC.setCellValueFactory(new PropertyValueFactory<>("precioUnitario"));
        totalC.setCellValueFactory(new PropertyValueFactory<>("subTotal"));

        refreshTable();
    }

    private void buscarDisponibilidad(int id) {
        try {
            disponibilidad.clear();
            productosDisponibles.setItems(disponibilidad);
            disponibilidad = fm.buscarProductos(id);
            productosDisponibles.setItems(disponibilidad);
        } catch (SQLException ignored) {
        }

    }
}
