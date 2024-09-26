package cur.meia.gpx_pv.database.models;

public class Tarjeta {
    private int numero;
    private int tipo;
    private String nombreTipo;
    private int nit;
    private int puntos;

    public Tarjeta(int numero, int tipo, int nit) {
        this.numero = numero;
        this.tipo = tipo;
        this.nit = nit;
        switch (this.tipo) {
            case 1:
                this.nombreTipo = "Comun";
                break;
            case 2:
                this.nombreTipo = "Oro";
                break;
            case 3:
                this.nombreTipo = "Platino";
                break;
            case 4:
                this.nombreTipo = "Diamante";
                break;
        }
    }

    public int getNumero() {
        return numero;
    }

    public void setNumero(int numero) {
        this.numero = numero;
    }

    public int getTipo() {
        return tipo;
    }

    public void setTipo(int tipo) {
        this.tipo = tipo;
    }

    public String getNombreTipo() {
        return nombreTipo;
    }

    public void setNombreTipo(String nombreTipo) {
        this.nombreTipo = nombreTipo;
    }

    public int getNit() {
        return nit;
    }

    public void setNit(int nit) {
        this.nit = nit;
    }

    public int getPuntos() {
        return puntos;
    }

    public void setPuntos(int puntos) {
        this.puntos = puntos;
    }
}
