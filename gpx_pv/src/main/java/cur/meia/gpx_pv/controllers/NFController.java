package cur.meia.gpx_pv.controllers;

import cur.meia.gpx_pv.database.DBConexion;
import cur.meia.gpx_pv.database.models.Cliente;
import cur.meia.gpx_pv.database.models.Venta;
import cur.meia.gpx_pv.models.FacturaModel;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.util.Duration;
import java.sql.SQLException;
import javafx.scene.paint.Color;


public class NFController {

    @FXML
    private TextField nitField;
    @FXML
    private TextField nameField;
    @FXML
    private Button addCliente, cancelarVentaButton, finalizarButton;
    @FXML
    private RadioButton rbCF, rbCanjearPuntos;
    @FXML
    private Label addClientMsg;
    @FXML
    private Label subtotalLabel, compraLabel;

    public static int id_factura = 0;
    public static Venta ventaActual = null;
    private CajeroController cajeroController;
    private NFTableController nfTableController;

    @FXML
    public void handleNewClient() {
        if(!nameField.getText().isEmpty()) {
            Cliente nuevo = new Cliente(Integer.parseInt(nitField.getText()), nameField.getText(), 0);
            try {
                facturaModel.insertarCliente(nuevo);
                addClientMsg.setTextFill(Color.rgb(94,35,157));
                addClientMsg.setText("Cliente agregado exitosamente.");
                nameField.setEditable(false);
                addCliente.setDisable(true);
            } catch (SQLException e) {
                addClientMsg.setTextFill(Color.rgb(246,16,103));
                addClientMsg.setText("Error al agregar cliente.");
            }finally {
                Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(5), event2 -> addClientMsg.setText("")));
                timeline.setCycleCount(1);
                timeline.play();
            }
        }
        nameField.setDisable(true);
    }

    private static FacturaModel facturaModel;

    public void initialize() {

        facturaModel = new FacturaModel();
        try {
            id_factura = facturaModel.nuevaVenta(DBConexion.usuarioActual.getId());
        } catch (SQLException ignored) {

        }

        nitField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.length() >= 8) {
                searchNit(newValue);
            }else {
                nameField.setText("");
            }
        });

        rbCF.selectedProperty().addListener((observable, oldValue, newValue) -> CFaction(newValue));
        rbCanjearPuntos.selectedProperty().addListener((observable, oldValue, newValue) -> {

        });

    }

    public void searchNit(String texto) {
        Cliente cliente;
        try {
            cliente = facturaModel.buscarCliente(texto);
            if(cliente != null) {
                addCliente.setDisable(true);
                nameField.setEditable(false);
                rbCanjearPuntos.setDisable(false);
                nameField.setText(cliente.getNombre());
            }else {
                rbCanjearPuntos.setDisable(true);
                addCliente.setDisable(false);
                nameField.setText("Cliente no registrado, ingrese nombre...");
                nameField.setEditable(true);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }


    public void CFaction(boolean activo) {

        nitField.setDisable(activo);
        nameField.setDisable(activo);
        rbCanjearPuntos.setDisable(activo);

        if(activo) {
            nitField.setText("C/F");
            nameField.setText("CF");
        } else {
            nitField.setText("");
            nameField.setText("");
        }
    }

    public void canjearPuntos(boolean activo) {

    }

    public void actualizarTotal() {
        try {
            ventaActual = facturaModel.actualizarDatosVenta(id_factura);
            subtotalLabel.setText("Q. " + ventaActual.total);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void setCajeroController(CajeroController cajeroController) {
        this.cajeroController = cajeroController;
    }

    public void setNfTableController(NFTableController nfTableController) {
        this.nfTableController = nfTableController;
    }

    @FXML
    public void cerrarVistas() {
        try {
            facturaModel.cerrarVistas(id_factura);
            cajeroController.cerrarVenta();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @FXML
    public void finalizar() {
        try {
            int nit_cliente;
            if(nitField.getText().isEmpty()) {
                nit_cliente = 0;
            }else {
                nit_cliente = Integer.parseInt(nitField.getText());
            }
            facturaModel.finalizarVenta(id_factura, nit_cliente);
            compraLabel.setText("Venta finalizada.");
            cancelarVentaButton.setDisable(true);
            finalizarButton.setDisable(true);
            addCliente.setDisable(true);
            nfTableController.agregarProductoButton.setDisable(true);
            nfTableController.eliminarProductoButton.setDisable(true);
            Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(3), event2 -> cajeroController.cerrarVenta()));
            timeline.setCycleCount(1);
            timeline.play();

        }catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
