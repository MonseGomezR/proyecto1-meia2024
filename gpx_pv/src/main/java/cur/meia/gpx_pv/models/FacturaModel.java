package cur.meia.gpx_pv.models;

import cur.meia.gpx_pv.database.DBConexion;
import cur.meia.gpx_pv.database.dao.ClienteDAO;
import cur.meia.gpx_pv.database.dao.VentaDAO;
import cur.meia.gpx_pv.database.models.Cliente;
import cur.meia.gpx_pv.database.models.DetalleV;
import cur.meia.gpx_pv.database.models.Producto;
import cur.meia.gpx_pv.database.models.Venta;
import javafx.collections.ObservableList;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class FacturaModel {

    private ClienteDAO clienteDAO;
    private VentaDAO ventaDAO;

    public FacturaModel() {
        if(DBConexion.conexion == null) {
            DBConexion.iniciar();
        }

        this.clienteDAO = new ClienteDAO(DBConexion.conexion);
        this.ventaDAO =  new VentaDAO(DBConexion.conexion);
    }

    public Cliente buscarCliente(String nit) throws SQLException {
        return clienteDAO.selectCliente(nit);
    }

    public void insertarCliente(Cliente cliente) throws SQLException {
        clienteDAO.insertCliente(cliente);
    }

    public ObservableList<DetalleV> getAllDetalles(int factura) throws SQLException {
        return ventaDAO.allDetallesVentas(factura);
    }

    public int nuevaVenta(int empleado) throws SQLException {
        return (int) ventaDAO.nuevaVenta(empleado);
    }

    public Venta actualizarDatosVenta(int id) throws SQLException {
        return ventaDAO.actVenta(id);
    }

    public ObservableList<Producto> buscarProductos(int id) throws SQLException {
        return ventaDAO.buscarEstanteria(id);
    }

    public String agregarDetalle(DetalleV detalle) throws SQLException {
        return ventaDAO.agregarDetalle(detalle);
    }

    public void cerrarVistas(int id) throws SQLException {
        ventaDAO.borrarVenta(id);
    }

    public void borrarDetalle(int id) throws SQLException {
        ventaDAO.borrarDetalle(id);
    }

    public void finalizarVenta(int id, int nit) throws SQLException {
        ventaDAO.finalizarVenta(id, nit);
    }
}
