package model;
import java.util.Arrays;

public class Polinomio {
    // Coeficientes desde el término de mayor grado hasta el independiente: [a_n, a_{n-1}, ..., a_0]
    private final double[] coeficientes;
    private final int grado;

    public Polinomio(double[] coeficientes) {
        if (coeficientes == null || coeficientes.length == 0) {
            throw new IllegalArgumentException("El polinomio debe tener al menos un coeficiente.");
        }
        this.coeficientes = coeficientes.clone(); 
        // clone() -> crea un nuevo arreglo en un bloque de memoria separado y copia los valores numéricos uno a uno
        this.grado = coeficientes.length - 1;
    }

    public double[] getCoeficientes() {
        return coeficientes.clone();
    }

    public int getGrado() {
        return grado;
    }

    public double getCoeficiente(int indice) {
        return coeficientes[indice];
    }

    public double evaluar(double x) {
    /* Evalúa el polinomio en x usando el algoritmo de Horner */
        return 0;
    }

    public Polinomio derivar() {
    /* Calcula la primera derivada del polinomio */
        return new Polinomio();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        return sb;
    }
}