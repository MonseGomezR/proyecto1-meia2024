package cur.meia.gpx_pv.models;

import cur.meia.gpx_pv.database.DBConexion;
import cur.meia.gpx_pv.database.dao.BodegaDAO;
import cur.meia.gpx_pv.database.dao.ClienteDAO;
import cur.meia.gpx_pv.database.models.DetalleV;
import cur.meia.gpx_pv.database.models.Producto;
import javafx.collections.ObservableList;

import java.sql.SQLException;

public class BodegaModel {

    private BodegaDAO bodegaDAO;

    public BodegaModel() {
        if(DBConexion.conexion == null) {
            DBConexion.iniciar();
        }

        this.bodegaDAO = new BodegaDAO(DBConexion.conexion);
    }

    public int getBodegaActual(int sucursal) throws SQLException {
        return bodegaDAO.bodegaActual(sucursal);
    }

    public ObservableList<Producto> getAllDetalles() throws SQLException {
        return bodegaDAO.allDetalles();
    }

    public ObservableList<Producto> buscarProductos() throws SQLException {
        return bodegaDAO.allProductos();
    }

    public ObservableList<Producto> getAllInventario() throws SQLException {
        return bodegaDAO.allInventario();
    }

    public void ingresarDetalle(int producto, int cantidad, int bodega) throws SQLException {
        bodegaDAO.ingresarProductos(producto, cantidad, bodega);
    }
}
