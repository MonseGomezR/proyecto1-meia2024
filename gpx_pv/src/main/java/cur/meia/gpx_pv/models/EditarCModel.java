package cur.meia.gpx_pv.models;

import cur.meia.gpx_pv.database.DBConexion;
import cur.meia.gpx_pv.database.dao.ClienteDAO;
import cur.meia.gpx_pv.database.models.Cliente;
import cur.meia.gpx_pv.database.models.DetalleV;
import javafx.collections.ObservableList;

import java.sql.SQLException;
import java.util.Map;

public class EditarCModel {

    private ClienteDAO clienteDAO;

    public EditarCModel() {
        if(DBConexion.conexion == null) {
            DBConexion.iniciar();
        }

        this.clienteDAO = new ClienteDAO(DBConexion.conexion);
    }

    public  ObservableList<Cliente> getAllClientes() throws SQLException {
        return clienteDAO.allClientes();
    }
}
