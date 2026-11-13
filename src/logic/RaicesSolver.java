package logic;

import model.Polinomio;
import model.RaizResult;
import logic.OperacionesPolinomios.ResultadoHornerDoble;
import logic.OperacionesPolinomios.ResultadoDivisionLineal;

public class RaicesSolver {

    /* Algoritmo 7: Método de Newton para Polinomios */
    public static RaizResult newtonPolinomio(Polinomio pol, double x0, double epsilon, int maxIter) {
        double xn = x0;
        int iter = 0;

        // 1. Verificación inicial por si x0 ya es la raíz exacta
        ResultadoHornerDoble resActual = OperacionesPolinomios.hornerDoble(pol, xn);
        if (Math.abs(resActual.getValor()) < epsilon) {
            return new RaizResult(xn, 0, 0.0, "Newton");
        }

        do {
            // Evaluamos P(xn) y P'(xn) usando Horner Doble
            ResultadoHornerDoble res = OperacionesPolinomios.hornerDoble(pol, xn);
            double valorP = res.getValor();
            double derivadaP = res.getDerivada();
            
           // Si ya llegamos a la raíz con la tolerancia deseada
            if (Math.abs(valorP) < epsilon) {
                return new RaizResult(xn, iter, 0.0, "Newton");
            }

            // Control de derivada cercana a cero (colapso numérico o pendiente horizontal)
            if (Math.abs(derivadaP) < 1e-14) {
                if (Math.abs(valorP) < epsilon) {
                    return new RaizResult(xn, iter, 0.0, "Newton");
                }
                throw new ArithmeticException("Derivada cercana a cero en el método de Newton. No se puede continuar.");
            }

            // Fórmula de Newton: x_{n+1} = x_n - P(x_n) / P'(x_n)
            double delta = valorP / derivadaP;
            double xn1 = xn - delta;
            iter++;

            // Evaluamos el siguiente punto
            ResultadoHornerDoble resSiguiente = OperacionesPolinomios.hornerDoble(pol, xn1);
            double deltaX = Math.abs(xn1 - xn);
            double valorPNext = Math.abs(resSiguiente.getValor());

            // CONDICIÓN DE CORTE ESTRICTA CON && (Ambas deben cumplirse)
            if (deltaX < epsilon && valorPNext < epsilon) {
                return new RaizResult(xn1, iter, deltaX, "Newton");
            }

            // Control de iteraciones máximas
            if (iter >= maxIter) {
                // Si al llegar al límite el valor ya está muy cerca de cero, lo aceptamos
                if (valorPNext < 1e-4) {
                    return new RaizResult(xn1, iter, deltaX, "Newton");
                }
                throw new RuntimeException("El método de Newton superó el número máximo de iteraciones sin converger.");
            }
            xn = xn1;
        } while (true);
    }

    /* Algoritmo 8: Método de Halley para Polinomio */
    public static RaizResult halleyPolinomio(Polinomio pol, double x0, double epsilon, int maxIter) {
        double xn = x0;
        int iter = 0;

        // Verificación inicial por si x0 ya es la raíz
        ResultadoHornerDoble resActual = OperacionesPolinomios.hornerDoble(pol, xn);
        if (Math.abs(resActual.getValor()) < epsilon) {
            return new RaizResult(xn, 0, 0.0, "Halley");
        }
        do {
            // 1. Obtenemos el cociente C(x) y el resto P(x) dividiendo por (x - xn)
            ResultadoDivisionLineal divRes = OperacionesPolinomios.dividirHorner(pol, -xn);
            Polinomio cocienteC = divRes.cociente;
            double valorP = divRes.resto; // corresponde a P(xn)

            // 2. Evaluamos el cociente C(x) y su derivada C'(x) usando Horner Doble en xn
            // Esto nos permite calcular P'(xn) y P''(xn) de forma eficiente.
            ResultadoHornerDoble hornerC = OperacionesPolinomios.hornerDoble(cocienteC, xn);
            double valorDP = hornerC.getValor();     // Corresponde a P'(xn)
            double valorD2P = hornerC.getDerivada(); // Corresponde a P''(xn) / 2 (según propiedades de Taylor/Horner) o calculada correctamente.
            
            // {dP; d2P} = HornerDoble(c[], n-1, x_n)
            // Formula Halley: x_{n+1} = x_n - (2 * P * dP) / (2 * dP * dP - P * d2P)
            double numerador = 2.0 * valorP * valorDP;
            double denominador = (2.0 * valorDP * valorDP) - (2.0 * valorP * valorD2P);

            // Control de colapso del denominador por límite de precisión de la máquina en double
            if (Math.abs(denominador) < 1e-14) {
                if (Math.abs(valorP) < epsilon) { 
                    return new RaizResult(xn, iter, 0.0, "Halley");
                }
                throw new ArithmeticException("Denominador cercano a cero o derivadas linealmente dependientes en Halley.");
            }

            double delta = numerador / denominador;
            double xn1 = xn - delta;
            iter++;

            // Evaluamos el siguiente punto
            ResultadoHornerDoble resSiguiente = OperacionesPolinomios.hornerDoble(pol, xn1);
            double deltaX = Math.abs(xn1 - xn);
            double valorPNext = Math.abs(resSiguiente.getValor());

            // Condición de corte por cambio en x o por valor de P en el siguiente punto
            if (deltaX < epsilon && valorPNext < epsilon) {
                return new RaizResult(xn1, iter, deltaX, "Halley");
            }

            if (iter >= maxIter) {
                if (valorPNext < 1e-4) { // Si el valor de P(xn1) es suficientemente pequeño, consideramos que hemos convergido
                    return new RaizResult(xn1, iter, deltaX, "Halley");
                }
                throw new RuntimeException("El método de Halley superó el número máximo de iteraciones sin converger.");
            }
            xn=xn1;
        } while (true);
    }
}