package cur.meia.gpx_pv.models;

import cur.meia.gpx_pv.database.DBConexion;
import cur.meia.gpx_pv.database.dao.AdminDAO;
import cur.meia.gpx_pv.database.models.Producto;
import cur.meia.gpx_pv.database.models.Sucursal;
import cur.meia.gpx_pv.database.models.Venta;
import javafx.collections.ObservableList;

import java.sql.SQLException;

public class AdminModel {

    private AdminDAO adminDAO;

    public AdminModel() {
        if(DBConexion.conexion == null) {
            DBConexion.iniciar();
        }
        this.adminDAO = new AdminDAO(DBConexion.conexion);
    }

    public ObservableList<Producto> topProductos()  throws SQLException {
        return adminDAO.topProductos();
    }

    public ObservableList<Venta> topVentas()  throws SQLException {
        return adminDAO.topVentas();
    }

    public ObservableList<Sucursal> topSucursales()  throws SQLException {
        return adminDAO.topSucursales();
    }
}
