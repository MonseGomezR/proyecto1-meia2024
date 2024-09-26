package cur.meia.gpx_pv.controllers;

import cur.meia.gpx_pv.database.models.Cliente;
import cur.meia.gpx_pv.database.models.DetalleV;
import cur.meia.gpx_pv.models.EditarCModel;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.sql.SQLException;

public class EditarClienteController {
    @FXML
    private TableView<Cliente> clientsTable;
    @FXML
    private TableColumn<Cliente, Integer> numC, buysC, nitC, tarjetaC;
    @FXML
    private TableColumn<Cliente, String> nameC, rangoC;
    @FXML
    private TableColumn<Cliente, Double> gastosC;
    @FXML
    private Button regresarButton, editarClienteButton;

    private CajeroController cajeroController;

    EditarCModel em;
    ObservableList<Cliente> clientList = FXCollections.observableArrayList();

    @FXML
    private void volver() {
        cajeroController.volverEditor();
    }

    public void initialize() {
        clientsTable.setPlaceholder(new Label(""));

        /*clientsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            editarClienteButton.setDisable(newSelection == null); // Habilitar si hay un ítem seleccionado
        });*/
        em = new EditarCModel();

        loadDate();
    }

    private void loadDate() {
        nameC.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        nitC.setCellValueFactory(new PropertyValueFactory<>("nit"));
        buysC.setCellValueFactory(new PropertyValueFactory<>("compras"));
        gastosC.setCellValueFactory(new PropertyValueFactory<>("gastos"));
        refreshTable();
    }

    private void refreshTable() {
        try {
            var resultado = em.getAllClientes();
            clientList.clear();
            clientsTable.setItems(clientList);
            clientList.addAll(resultado);
            clientsTable.setItems(clientList);
        } catch (SQLException e) {
            throw new RuntimeException(e);

        }
    }

    public void setCajeroController(CajeroController cajeroController) {
        this.cajeroController = cajeroController;
    }
}
