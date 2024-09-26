package cur.meia.gpx_pv.database.dao;

import cur.meia.gpx_pv.database.models.Usuario;
import cur.meia.gpx_pv.extras.PassHash;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UsuarioDAO {

    private final Connection connection;

    public UsuarioDAO(Connection connection) {
        this.connection = connection;
    }

    public Usuario login(String id, String password) throws SQLException {
        Usuario usuario;
        String query = "SELECT * FROM administracion.empleados WHERE id = ?";
        PreparedStatement statement = connection.prepareStatement(query);
        statement.setLong(1, Long.parseLong(id));
        ResultSet rs = statement.executeQuery();

        if (rs.next()) {
            // Obtener el hash almacenado y la sal del usuario
            String storedHash = rs.getString("hp");
            String storedSalt = rs.getString("salt");

            // Usar el método de hashing para generar el hash de la contraseña ingresada
            boolean isPassword = false;
            try {
                isPassword = PassHash.verifyPassword(password, storedHash, storedSalt);
            } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
                throw new RuntimeException(e);
            }

            // Verificar si el hash generado coincide con el almacenado
            if (isPassword) {
                System.out.println("Contraseña correcta");
                return new Usuario(
                        rs.getInt("id"),
                        rs.getString("nombre"),
                        rs.getInt("tipo"),
                        rs.getString("hp"),
                        rs.getString("salt"),
                        rs.getInt("id_sucursal")
                );
            } else {
                System.out.println("Contraseña incorrecta.");
            }
        } else {
            System.out.println("Usuario no encontrado.");
        }

        return null; // Si las credenciales son incorrectas
    }
}
