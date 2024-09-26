package cur.meia.gpx_pv.database.models;

import java.sql.Date;

public class Venta {
    public int id;
    public int nit;
    public int empleado;
    public Date fecha;
    public double total;

    public Venta(int id, int nit, int empleado, Date fecha, double total) {
        this.id = id;
        this.nit = nit;
        this.empleado = empleado;
        this.fecha = fecha;
        this.total = total;
    }
}
