package logic;

import model.Polinomio;
import model.RaizResult;
import logic.OperacionesPolinomios.ResultadoHornerDoble;
import logic.OperacionesPolinomios.ResultadoDivisionLineal;

public class RaicesSolver {

    /* Algoritmo 7: Método de Newton para Polinomios */
    public static RaizResult newtonPolinomio(Polinomio pol, double x0, double epsilon, int maxIter) {
        double xn = x0;
        double xn1 = x0;
        int iter = 0;

        do {
            xn = xn1;
            // Evaluamos P(xn) y P'(xn) usando Horner Doble
            ResultadoHornerDoble res = OperacionesPolinomios.hornerDoble(pol, xn);
            
            if (Math.abs(res.getDerivada()) < 1e-14) {
                throw new ArithmeticException("Derivada cercana a cero en el método de Newton. No se puede continuar.");
            }

            // Fórmula de Newton: x_{n+1} = x_n - P(x_n) / P'(x_n)
            xn1 = xn - (res.getValor() / res.getDerivada());
            iter++;

            /* Condición de corte: |x_{n+1} - x_n| < epsilon ó |P(x_{n+1})| < epsilon
            ResultadoHornerDoble resSiguiente = OperacionesPolinomios.hornerDoble(pol, xn1);
            if (Math.abs(xn1 - xn) < epsilon && Math.abs(resSiguiente.getValor()) < epsilon) {
                break;
            }*/

            /* Condición de corte: |x_{n+1} - x_n| */
            if (Math.abs(xn1 - xn) < epsilon) { 
                break;
            }

            if (iter >= maxIter) {
                throw new RuntimeException("El método de Newton superó el número máximo de iteraciones sin converger.");
            }

        } while (true);

        return new RaizResult(xn1, iter, Math.abs(xn1 - xn), "Newton");
    }

    /* Algoritmo 8: Método de Halley para Polinomio */
    public static RaizResult halleyPolinomio(Polinomio pol, double x0, double epsilon, int maxIter) {
        double xn = x0;
        double xn1 = x0;
        int iter = 0;

        do {
            xn = xn1;
            
            // 1. Obtenemos el cociente C(x) y el resto P(x) dividiendo por (x - xn)
            // Nota: El divisor (x - xn) equivale a b = -xn en la división sintética de Horner lineal.
            ResultadoDivisionLineal divRes = OperacionesPolinomios.dividirHorner(pol, -xn);
            Polinomio cocienteC = divRes.cociente;
            double valorP = divRes.resto; // El resto de dividir por (x - xn) es exactamente P(xn)

            // 2. Evaluamos el cociente C(x) y su derivada C'(x) usando Horner Doble en xn
            // Esto nos permite calcular P'(xn) y P''(xn) de forma eficiente.
            ResultadoHornerDoble hornerC = OperacionesPolinomios.hornerDoble(cocienteC, xn);
            double valorDP = hornerC.getValor();     // Corresponde a P'(xn)
            double valorD2P = hornerC.getDerivada(); // Corresponde a P''(xn) / 2 (según propiedades de Taylor/Horner) o calculada correctamente.
            
            // Ajuste estricto al Algoritmo 8:
            // {dP; d2P} = HornerDoble(c[], n-1, x_n)
            // Formula Halley: x_{n+1} = x_n - (2 * P * dP) / (2 * dP * dP - P * d2P)
            double numerador = 2.0 * valorP * valorDP;
            double denominador = (2.0 * valorDP * valorDP) - (valorP * valorD2P);

            if (Math.abs(denominador) < 1e-14) {
                throw new ArithmeticException("Denominador cercano a cero en el método de Halley.");
            }

            xn1 = xn - (numerador / denominador);
            iter++;

            /* Condición de corte: |x_{n+1} - x_n| < epsilon ó |P(x_{n+1})| < epsilon
            ResultadoHornerDoble resSiguiente = OperacionesPolinomios.hornerDoble(pol, xn1);
            if (Math.abs(xn1 - xn) < epsilon || Math.abs(resSiguiente.getValor()) < epsilon) {
                break;
            } */

            /* Condición de corte: |x_{n+1} - x_n| */
            if (Math.abs(xn1 - xn) < epsilon) {
                break;
            } 

            if (iter >= maxIter) {
                throw new RuntimeException("El método de Halley superó el número máximo de iteraciones sin converger.");
            }

        } while (true);

        return new RaizResult(xn1, iter, Math.abs(xn1 - xn), "Halley");
    }
}