package cur.meia.gpx_pv.database.models;

public class Cliente {
    private int nit;
    private String nombre;
    private double gastos;
    private int compras;
    private Tarjeta tarjeta;

    public Cliente(int nit, String nombre, double gastos) {
        this.nit = nit;
        this.nombre = nombre;
        this.gastos = gastos;
    }

    public int getNit() {
        return nit;
    }
    public void setNit(int nit) {
        this.nit = nit;
    }

    public String getNombre() {
        return nombre;
    }
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public double getGastos() {
        return gastos;
    }
    public void setGastos(double gastos) {
        this.gastos = gastos;
    }

    public int getCompras() {
        return compras;
    }

    public void setCompras(int compras) {
        this.compras = compras;
    }

    public Tarjeta getTarjeta() {
        return tarjeta;
    }

    public void setTarjeta(Tarjeta tarjeta) {
        this.tarjeta = tarjeta;
    }
}
