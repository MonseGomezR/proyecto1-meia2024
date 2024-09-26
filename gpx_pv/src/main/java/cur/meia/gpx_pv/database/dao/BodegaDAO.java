package cur.meia.gpx_pv.database.dao;

import cur.meia.gpx_pv.database.DBConexion;
import cur.meia.gpx_pv.database.models.Producto;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class BodegaDAO {
    private final Connection connection;

    public BodegaDAO(Connection connection) {
        this.connection = connection;
    }

    public void ingresarProductos(int producto, int cantidad, int bodega) throws SQLException {
        String sql = "SELECT inventario.ingresar_producto_bodega(?, ?, ?)";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setLong(1, bodega);
        ps.setLong(2, producto);
        ps.setLong(3, cantidad);
        ResultSet rs = ps.executeQuery();

    }

    public int bodegaActual(int sucursal) throws SQLException {
        String sql = "SELECT codigo FROM inventario.bodega WHERE id_sucursal= ?";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setLong(1, sucursal);
        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            return rs.getInt("codigo");
        }

        return 0;
    }

    public ObservableList<Producto> allDetalles () throws SQLException {
        ObservableList<Producto> productList = FXCollections.observableArrayList();
        String sql = "SELECT * FROM inventario.\"productos_disponibles(bodega)\" WHERE id_sucursal = ?";
        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        preparedStatement.setLong(1, DBConexion.usuarioActual.getId_sucursal());
        ResultSet resultSet = preparedStatement.executeQuery();

        while (resultSet.next()){
            Producto nuevo = new Producto(
                    resultSet.getInt("id_producto"),
                    resultSet.getString("nombre"), 0, "");
            nuevo.setCantidad(resultSet.getInt("stock"));
            productList.add(nuevo);
        }
        return productList;
    }

    public ObservableList<Producto> allProductos() throws SQLException {
        String sql = "SELECT * FROM inventario.productos";
        ObservableList<Producto> resultado = FXCollections.observableArrayList();
        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        ResultSet resultSet = preparedStatement.executeQuery();
        while(resultSet.next()) {
            Producto nuevo = new Producto(resultSet.getInt("id"),
                    resultSet.getString("nombre"),
                    resultSet.getDouble("precio"),
                    resultSet.getString("descripcion"));
            resultado.add(nuevo);
        }
        return resultado;
    }

    public ObservableList<Producto> allInventario () throws SQLException {
        ObservableList<Producto> productList = FXCollections.observableArrayList();
        String sql = "SELECT * FROM inventario.\"productos_disponibles(estanteria)\" WHERE id_sucursal = ?";
        PreparedStatement ps = connection.prepareStatement(sql);
        ps.setLong(1, DBConexion.usuarioActual.getId_sucursal());
        ResultSet resultSet = ps.executeQuery();

        while (resultSet.next()){
            Producto nuevo = new Producto(
                    resultSet.getInt("id_producto"),
                    resultSet.getString("nombre"), 0, "");
            nuevo.setCantidad(resultSet.getInt("stock"));
            productList.add(nuevo);
        }
        return productList;
    }

    public void ingresarEstanteria(int producto, int cantidad, int sucursal) {
        
    }
}
