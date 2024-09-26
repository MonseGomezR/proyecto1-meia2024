package cur.meia.gpx_pv.models;

import cur.meia.gpx_pv.database.DBConexion;
import cur.meia.gpx_pv.database.dao.UsuarioDAO;
import cur.meia.gpx_pv.database.models.Usuario;
import java.sql.SQLException;

public class LoginModel {

    public boolean authenticate(String username, String password) throws SQLException {

        if( username != null && password != null && !password.isEmpty()) {
            if(DBConexion.conexion == null || DBConexion.conexion.isClosed()) {
                DBConexion.iniciar();
            }
            Usuario usuario;
            UsuarioDAO usuarioDAO = new UsuarioDAO(DBConexion.conexion);
            usuario = usuarioDAO.login(username, password);
            if(usuario != null) {
                DBConexion.usuarioActual = usuario;

                return true;
            }
        }
        return false;
    }
}

