package cur.meia.gpx_pv.database;

import cur.meia.gpx_pv.database.models.Usuario;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConexion {

    public static Connection conexion;
    public static Usuario usuarioActual;

    public static Connection  iniciar() {
        Connection connection = null;

        String url = "jdbc:postgresql://localhost:5432/gamerproxela-db";
        String user = "postgres";
        String password = "2212";

        try {
            connection = DriverManager.getConnection(url, user, password);
            System.out.println("Conexión exitosa a PostgreSQL");

        } catch (SQLException e) {
            System.out.println("Error al conectarse a la base de datos: " + e.getMessage());
        }

        conexion = connection;
        return connection;
    }

    public static void cambiarDBUser() {

    }
}
