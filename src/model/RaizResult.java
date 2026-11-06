package model;

public class RaizResult {
    private final double valorRaiz;
    private final int iteraciones;
    private final double errorAlcanzado;
    private final String metodo;

    public RaizResult(double valorRaiz, int iteraciones, double errorAlcanzado, String metodo) {
        this.valorRaiz = valorRaiz;
        this.iteraciones = iteraciones;
        this.errorAlcanzado = errorAlcanzado;
        this.metodo = metodo;
    }

    public double getValorRaiz() { return valorRaiz; }
    public int getIteraciones() { return iteraciones; }
    public double getErrorAlcanzado() { return errorAlcanzado; }
    public String getMetodo() { return metodo; }

    @Override
    public String toString() {
        return String.format("Raíz: %.6f (%s) | Iteraciones: %d | Error: %.2e", 
                valorRaiz, metodo, iteraciones, errorAlcanzado);
    }
}