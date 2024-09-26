package cur.meia.gpx_pv.database.dao;

import cur.meia.gpx_pv.controllers.NFController;
import cur.meia.gpx_pv.database.DBConexion;
import cur.meia.gpx_pv.database.models.DetalleV;
import cur.meia.gpx_pv.database.models.Producto;
import cur.meia.gpx_pv.database.models.Venta;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;

public class VentaDAO {

    private final Connection connection;

    public VentaDAO(Connection connection) {
        this.connection = connection;
    }

    public long nuevaVenta(int empleado) throws SQLException {
        String sql = "INSERT INTO ventas.ventas(id_empleado, fecha) VALUES (?, ?) RETURNING n_factura;";
        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        Date currentDate = new Date(System.currentTimeMillis());
        preparedStatement.setLong(1, empleado);
        preparedStatement.setDate(2, currentDate);

        ResultSet rs = preparedStatement.executeQuery();

        if (rs.next()) {
            return rs.getInt("n_factura");
        }
        return 0;
    }

    public Venta actVenta(int id) throws SQLException {
        String sql = "SELECT * FROM ventas.ventas WHERE n_factura = ?";
        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        preparedStatement.setLong(1, id);
        ResultSet resultSet = preparedStatement.executeQuery();
        if(resultSet.next()) {
            return new Venta(id,
                    resultSet.getInt("id_empleado"),
                    resultSet.getInt("nit_cliente"),
                    resultSet.getDate("fecha"),
                    resultSet.getDouble("total"));
        }
        return null;
    }

    public ObservableList<DetalleV> allDetallesVentas(int idFactura) throws SQLException {
        ObservableList<DetalleV> productList = FXCollections.observableArrayList();
        String sql = "SELECT * FROM ventas.detalle_venta_full WHERE num_factura = ?;";
        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        preparedStatement.setLong(1, idFactura);
        ResultSet resultSet = preparedStatement.executeQuery();

        while (resultSet.next()){
            DetalleV nuevo = new DetalleV(0, idFactura, resultSet.getInt("id_producto"),
                                            resultSet.getString("nombre_producto"),
                                            resultSet.getInt("cantidad"),
                                            resultSet.getDouble("precio"),
                                            resultSet.getDouble("subtotal"));
            productList.add(nuevo);
        }
        return productList;
    }

    public ObservableList<Producto> buscarEstanteria(int id) throws SQLException {
        String sql = "SELECT * FROM inventario.\"productos_disponibles(estanteria)\" WHERE id_sucursal = ?;";
        ObservableList<Producto> resultado = FXCollections.observableArrayList();
        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        preparedStatement.setLong(1, DBConexion.usuarioActual.getId_sucursal());
        ResultSet resultSet = preparedStatement.executeQuery();

        while (resultSet.next()){
            Producto nuevo = new Producto(resultSet.getInt("id_producto"),
                    resultSet.getString("nombre"),
                    resultSet.getDouble("precio"),
                    "");
            nuevo.setCantidad(resultSet.getInt("stock"));
            resultado.add(nuevo);
        }

        return resultado;
    }

    public String agregarDetalle(DetalleV detalle) throws SQLException{
        String sql = "INSERT INTO ventas.detalle_ventas(num_factura, id_producto, cantidad) VALUES (?, ?, ?);";
        PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setLong(1, detalle.num_factura);
            preparedStatement.setLong(2, detalle.id_producto);
            preparedStatement.setLong(3, detalle.cantidad);
            preparedStatement.executeUpdate();
            return "Producto agregado correctamente.";
    }

    public void borrarDetalle(int id) throws SQLException {
        String sql = "DELETE FROM ventas.detalle_ventas WHERE id_producto = ? AND num_factura = ?;";
        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        preparedStatement.setLong(1, id);
        preparedStatement.setLong(2, NFController.id_factura);
        preparedStatement.executeUpdate();
    }

    public void borrarVenta(int id) throws SQLException {
        String sql = "SELECT ventas.cancelar_venta(?);";
        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        preparedStatement.setLong(1, id);
        preparedStatement.executeQuery();
    }

    public void finalizarVenta(int id, int nit) throws SQLException {
        String sql;
        PreparedStatement preparedStatement;
        if(nit == 0) {
            sql = "UPDATE ventas.ventas SET finalizada=true WHERE n_factura=?;";
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setLong(1, id);
        }else {
            sql = "UPDATE ventas.ventas SET finalizada=true, nit_cliente = ?   WHERE n_factura=?;";
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setLong(1, nit);
            preparedStatement.setLong(2, id);
        }

        preparedStatement.executeUpdate();
    }
}
