package cur.meia.gpx_pv.database.dao;

import cur.meia.gpx_pv.database.models.DetalleV;
import cur.meia.gpx_pv.database.models.Producto;
import cur.meia.gpx_pv.database.models.Sucursal;
import cur.meia.gpx_pv.database.models.Venta;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class  AdminDAO{
    private final Connection connection;

    public AdminDAO(Connection connection) {
        this.connection = connection;
    }

    public ObservableList<Venta> topVentas() throws SQLException {
        String sql = "SELECT * FROM administracion.top_ventas;";
        ObservableList<Venta> resultado = FXCollections.observableArrayList();
        PreparedStatement ps = connection.prepareStatement(sql);
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            Venta venta = new Venta(rs.getInt("n_factura"),
            rs.getInt("nit_cliente"),
            rs.getInt("id_empleado"),
                    rs.getDate("fecha"),
            rs.getDouble("total"));
            resultado.add(venta);
        }
        return resultado;
    }

    public ObservableList<Producto> topProductos() throws SQLException {
        String sql = "SELECT * FROM administracion.top_productos;";
        ObservableList<Producto> resultado = FXCollections.observableArrayList();
        PreparedStatement ps = connection.prepareStatement(sql);
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            Producto producto = new Producto(rs.getInt("id_producto"),
                    rs.getString("nombre"),
                    0.0, "");
            producto.setCantidad(rs.getInt("sum"));
            resultado.add(producto);
        }
        return resultado;
    }

    public ObservableList<Sucursal> topSucursales() throws SQLException {
        String sql = "SELECT * FROM administracion.top_sucursales;";
        ObservableList<Sucursal> resultado = FXCollections.observableArrayList();
        PreparedStatement ps = connection.prepareStatement(sql);
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            Sucursal sucursal = new Sucursal(rs.getInt("id_sucursal"),
                    rs.getString("nombre"), "");
            sucursal.setVendido(rs.getDouble("total_vendido"));
            resultado.add(sucursal);
        }
        return resultado;
    }
}
