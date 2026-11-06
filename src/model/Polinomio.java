package model;

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

    /* Algoritmo 1: Evaluación de P_n(x) en un valor alpha */
    public double evaluar(double x) {
        /* Evalúa el polinomio en x */
        double p = coeficientes[0]; // Empezamos con el coeficiente principal a_n
        for (int i = 1; i <= grado; i++) {
            p = p * x + coeficientes[i];
        }
        return p;
    }

    public Polinomio derivar() {
        /* Calcula la primera derivada del polinomio y retorna un nuevo Polinomio */
        if (grado == 0) {
            // La derivada de una constante es 0
            return new Polinomio(new double[]{0.0});
        }

        double[] nuevosCoeficientes = new double[grado]; // Tendrá tamaño n (grado actual)
        for (int i = 0; i < grado; i++) {
            int exponente = grado - i; // Potencia actual del término
            nuevosCoeficientes[i] = coeficientes[i] * exponente;
        }
        return new Polinomio(nuevosCoeficientes);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        
        for (int i = 0; i <= grado; i++) {
            double c = coeficientes[i];
            
            // Si el coeficiente es 0, no lo imprimimos (a menos que todo el polinomio sea 0)
            if (c == 0.0 && grado > 0 && sb.length() > 0) {
                continue;
            }
            
            int exp = grado - i; // Exponente actual (ej: n, n-1, ..., 0)
            
            // Manejo de signos y espaciado
            if (sb.length() > 0) {
                if (c > 0) {
                    sb.append(" + ");
                } else {
                    sb.append(" - ");
                    c = -c; // Tomamos el valor absoluto para imprimir el número
                }
            } else {
                if (c < 0) {
                    sb.append("-");
                    c = -c;
                }
            }
            
            // Determinar si debemos mostrar el número (coeficiente)
            boolean esUno = (c == 1.0);
            if (!esUno || exp == 0) {
                // Si es un número entero (ej: 3.0 en vez de 3.0), podemos castearlo o dejarlo con decimales
                if (c == (int) c) {
                    sb.append((int) c);
                } else {
                    sb.append(c);
                }
            }
            
            // Agregar la variable x y su exponente si corresponde
            if (exp > 0) {
                sb.append("x");
                if (exp > 1) {
                    sb.append("^").append(exp);
                }
            }
        }
        
        // Si el polinomio quedó vacío (ej: P(x) = 0)
        if (sb.length() == 0) {
            sb.append("0");
        }
        
        return sb.toString();
    }
}