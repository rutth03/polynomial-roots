package logic;

import model.Polinomio;

public class OperacionesPolinomios {
    // Clase auxiliar para retornar cociente y resto en divisiones lineales
    public static class ResultadoDivisionLineal {
        public Polinomio cociente;
        public double resto;

        public ResultadoDivisionLineal(Polinomio cociente, double resto) {
            this.cociente = cociente;
            this.resto = resto;
        }
    }

    // Clase auxiliar para división cuadrática (Algoritmo 4)
    public static class ResultadoDivisionCuadratica {
        public Polinomio cociente;
        public double resto1; // Coeficiente de x en el resto
        public double resto0; // Término independiente del resto

        public ResultadoDivisionCuadratica(Polinomio cociente, double resto1, double resto0) {
            this.cociente = cociente;
            this.resto1 = resto1;
            this.resto0 = resto0;
        }
    }

    // Clase auxiliar para división general de polinomios (Algoritmo 5)
    public static class ResultadoDivisionGeneral {
        public Polinomio cociente;
        public Polinomio resto;

        public ResultadoDivisionGeneral(Polinomio cociente, Polinomio resto) {
            this.cociente = cociente;
            this.resto = resto;
        }
    }

    // Algoritmo 2: Horner para dividir P_n(x) por (x + b) 
    public static ResultadoDivisionLineal dividirHorner(Polinomio p, double b) {
        double[] coefs = p.getCoeficientes();
        int n = p.getGrado();
        
        double[] c = new double[n]; // El cociente tiene grado n-1 (n coeficientes)
        c[0] = coefs[0];
        
        for (int i = 1; i < n; i++) {
            c[i] = c[i - 1] * (-b) + coefs[i];
        }
        double resto = c[n - 1] * (-b) + coefs[n];
        
        return new ResultadoDivisionLineal(new Polinomio(c), resto);
    }

    // Algoritmo 3: Horner para dividir P_n(x) por (ax + b) 
    public static ResultadoDivisionLineal dividirHornerGral(Polinomio p, double alpha, double beta) {
        double b = beta / alpha;
        ResultadoDivisionLineal res = dividirHorner(p, b);
        
        double[] coefsCociente = res.cociente.getCoeficientes();
        for (int i = 0; i < coefsCociente.length; i++) {
            coefsCociente[i] = coefsCociente[i] / alpha;
        }
        
        return new ResultadoDivisionLineal(new Polinomio(coefsCociente), res.resto);
    }

    // Algoritmo 4: División de P_n(x) por (x^2 + ax + b)
    public static ResultadoDivisionCuadratica dividirHornerCuadratico(Polinomio p, double a, double b) {
        double[] P = p.getCoeficientes();
        int n = p.getGrado();
        
        double[] C = new double[n - 1]; // Cociente de grado n-2
        C[0] = P[0];
        C[1] = (-a) * C[0] + P[1];
        
        for (int i = 2; i < n - 1; i++) {
            C[i] = (-a) * C[i - 1] + (-b) * C[i - 2] + P[i];
        }
        
        double r1 = (-a) * C[n - 2] + (-b) * C[n - 3] + P[n - 1];
        double r0 = (-b) * C[n - 2] + P[n];
        
        return new ResultadoDivisionCuadratica(new Polinomio(C), r1, r0);
    }

    /*
     * Algoritmo 5: División general de polinomios
     * Divide un polinomio de grado n entre otro de grado m (con n >= m).
     */
    public static ResultadoDivisionGeneral divisionGeneral(Polinomio pDividendo, Polinomio pDivisor) {
        double[] A = pDividendo.getCoeficientes();
        double[] B = pDivisor.getCoeficientes();
        int n = pDividendo.getGrado();
        int m = pDivisor.getGrado();

        if (n < m) {
            return new ResultadoDivisionGeneral(new Polinomio(new double[]{0.0}), pDividendo);
        }

        double[] R = A.clone();
        double[] C = new double[n - m + 1];

        for (int i = 0; i <= n - m; i++) {
            C[i] = R[i] / B[0];
            for (int j = 0; j <= m; j++) {
                R[i + j] -= C[i] * B[j];
            }
        }

        double[] restoCoefs = new double[m];
        System.arraycopy(R, n - m + 1, restoCoefs, 0, m);

        return new ResultadoDivisionGeneral(new Polinomio(C), new Polinomio(restoCoefs));
    }
    /*
     * Devuelve la representación en cadena de la equivalencia para división lineal:
     * P(x) = C(x) * (x + b) + Resto
     */
    public static String formatearEquivalenciaLineal(Polinomio pOriginal, Polinomio cociente, double b, double resto) {
        // Si b es positivo, el binomio original era (x - b) o viceversa según cómo se ingresó
        String divisorStr = (b >= 0) ? "(x + " + b + ")" : "(x - " + (-b) + ")";
        return "P(x) = (" + cociente.toString() + ") * " + divisorStr + " + (" + resto + ")";
    }

    /*
     * Devuelve la representación en cadena de la equivalencia para división cuadrática o general:
     * P(x) = C(x) * Q(x) + R(x)
     */
    public static String formatearEquivalenciaPolinomica(Polinomio pOriginal, Polinomio cociente, Polinomio divisor, Polinomio resto) {
        return "P(x) = (" + cociente.toString() + ") * (" + divisor.toString() + ") + (" + resto.toString() + ")";
    }
}