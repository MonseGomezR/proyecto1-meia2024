package cur.meia.gpx_pv.database.models;

import cur.meia.gpx_pv.database.DBConexion;

public class Usuario {
    private int id;
    private String nombre;
    private int tipoUsuario;
    private String h;
    private String s;
    private int id_sucursal;

    public Usuario(int id, String nombre, int tipoUsuario, String h, String s, int id_sucursal) {
        this.id = id;
        this.nombre = nombre;
        this.tipoUsuario = tipoUsuario;
        this.h = h;
        this.s = s;
        this.id_sucursal = id_sucursal;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public int getTipoUsuario() {
        return tipoUsuario;
    }

    public void setTipoUsuario(int tipoUsuario) {
        this.tipoUsuario = tipoUsuario;
    }

    public String getH() {
        return h;
    }

    public void setH(String h) {
        this.h = h;
    }

    public String getS() {
        return s;
    }

    public void setS(String s) {
        this.s = s;
    }

    public int getId_sucursal() {
        return id_sucursal;
    }

    public void setId_sucursal(int id_sucursal) {
        this.id_sucursal = id_sucursal;
    }
}
