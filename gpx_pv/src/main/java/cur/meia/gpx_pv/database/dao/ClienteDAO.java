package cur.meia.gpx_pv.database.dao;

import cur.meia.gpx_pv.database.models.Cliente;
import cur.meia.gpx_pv.database.models.DetalleV;
import cur.meia.gpx_pv.database.models.Tarjeta;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class ClienteDAO {

    private final Connection connection;

    public ClienteDAO(Connection connection) {
        this.connection = connection;
    }

    public void insertCliente(Cliente cliente) throws SQLException {
        String sql = "INSERT INTO administracion.clientes VALUES (?,?,?)";
        PreparedStatement statement = connection.prepareStatement(sql);
        statement.setLong(1, cliente.getNit());
        statement.setString(2, cliente.getNombre());
        statement.setDouble(3, cliente.getGastos());
        var estado = statement.executeUpdate();
        if (estado == 0) {
            throw new SQLException("No se pudo insertar el cliente");
        }
    }

    public Cliente selectCliente(String nit)  throws SQLException {
        Cliente cliente = null;

        String query = "SELECT * FROM administracion.clientes WHERE nit = ?";
        PreparedStatement statement = connection.prepareStatement(query);
        statement.setLong(1, Long.parseLong(nit));
        ResultSet rs = statement.executeQuery();

        if (rs.next()) {
            cliente = new Cliente(rs.getInt("nit"), rs.getString("nombre"), rs.getDouble("gastos"));
        }
        return cliente;
    }

    public ObservableList<Cliente> allClientes() throws SQLException {
        ObservableList<Cliente> clientList = FXCollections.observableArrayList();
        String sql = "SELECT * FROM administracion.all_clientes_tarjeta;";
        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        ResultSet resultSet = preparedStatement.executeQuery();

        while (resultSet.next()){
            Cliente nuevo = new Cliente(resultSet.getInt("nit"),
                    resultSet.getString("nombre"),
                    resultSet.getDouble("gastos"));
            nuevo.setCompras(resultSet.getInt("compras"));
            Tarjeta tarjeta = new Tarjeta(resultSet.getInt("codigo_tarjeta"),
                    resultSet.getInt("tipo_tarjeta"),
                    resultSet.getInt("nit"));
            nuevo.setTarjeta(tarjeta);
            clientList.add(nuevo);
        }
        return clientList;
    }
}
