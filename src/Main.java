import logic.OperacionesPolinomios;
import logic.OperacionesPolinomios.ResultadoDivisionLineal;
import logic.OperacionesPolinomios.ResultadoDivisionCuadratica;
import model.Polinomio;

public class Main {
    public static void main(String[] args) {
        System.out.println("=== PRUEBAS DEL EJERCICIO N° 2 ===");

        // -------------------------------------------------------------
        // Inciso a) P1(x) = 2x^6 + x^3 + 3x + 2  entre  Q1(x) = x + 1
        // Coeficientes P1: [2, 0, 0, 1, 0, 3, 2] (grados 6 al 0)
        // -------------------------------------------------------------
        System.out.println("\n--- Inciso a) ---");
        Polinomio p1 = new Polinomio(new double[]{2, 0, 0, 1, 0, 3, 2});
        // Q(x) = x + 1 -> b = 1 (ya que el divisor es x + b)
        ResultadoDivisionLineal resA = OperacionesPolinomios.dividirHorner(p1, 1.0);
        
        System.out.println("P(x) = " + p1);
        System.out.println("Cociente C(x) = " + resA.cociente);
        System.out.println("Resto R(x) = " + resA.resto);
        System.out.println("Forma equivalente: " + OperacionesPolinomios.formatearEquivalenciaLineal(p1, resA.cociente, 1.0, resA.resto));

        // -------------------------------------------------------------
        // Inciso b) P2(x) = x^3 - 2x^2 + 5x + 3  entre  Q2(x) = x - 2
        // Coeficientes P2: [1, -2, 5, 3]
        // -------------------------------------------------------------
        System.out.println("\n--- Inciso b) ---");
        Polinomio p2 = new Polinomio(new double[]{1, -2, 5, 3});
        // Q(x) = x - 2 -> Para usar la forma (x + b), si es (x - 2), b debe ser -2
        ResultadoDivisionLineal resB = OperacionesPolinomios.dividirHorner(p2, -2.0);
        
        System.out.println("P(x) = " + p2);
        System.out.println("Cociente C(x) = " + resB.cociente);
        System.out.println("Resto R(x) = " + resB.resto);
        System.out.println("Forma equivalente: " + OperacionesPolinomios.formatearEquivalenciaLineal(p2, resB.cociente, -2.0, resB.resto));

        // -------------------------------------------------------------
        // Inciso c) P3(x) = 2x^6 + x^3 + 3x + 2  entre  Q3(x) = 2x + 2
        // Divisor general (ax + b) donde alpha = 2, beta = 2
        // -------------------------------------------------------------
        System.out.println("\n--- Inciso c) ---");
        Polinomio p3 = new Polinomio(new double[]{2, 0, 0, 1, 0, 3, 2});
        ResultadoDivisionLineal resC = OperacionesPolinomios.dividirHornerGral(p3, 2.0, 2.0);
        
        System.out.println("P(x) = " + p3);
        System.out.println("Cociente C(x) = " + resC.cociente);
        System.out.println("Resto R(x) = " + resC.resto);
        // Para (2x + 2), en el formato lineal podemos mostrar la estructura general o el divisor directo
        System.out.println("Forma equivalente: P(x) = (" + resC.cociente + ") * (2.0x + 2.0) + (" + resC.resto + ")");

        // -------------------------------------------------------------
        // Inciso d) P4(x) = x^3 - (2/5)x^2 + 5x + 3  entre  Q4(x) = 5x - 2
        // Coeficientes P4: [1, -0.4, 5, 3]. Divisor (5x - 2) -> alpha = 5, beta = -2
        // -------------------------------------------------------------
        System.out.println("\n--- Inciso d) ---");
        Polinomio p4 = new Polinomio(new double[]{1, -0.4, 5, 3});
        ResultadoDivisionLineal resD = OperacionesPolinomios.dividirHornerGral(p4, 5.0, -2.0);
        
        System.out.println("P(x) = " + p4);
        System.out.println("Cociente C(x) = " + resD.cociente);
        System.out.println("Resto R(x) = " + resD.resto);
        System.out.println("Forma equivalente: P(x) = (" + resD.cociente + ") * (5.0x - 2.0) + (" + resD.resto + ")");

        // -------------------------------------------------------------
        // Inciso e) Usar Horner cuadrático para P(x)/Q(x) del ejercicio 1
        // P(x) = x^4 - 3x^3 + (3/2)x + (1/4) -> [1, -3, 0, 1.5, 0.25]
        // Q(x) = x^2 - (5/2)x + 7 -> x^2 + ax + b  => a = -2.5, b = 7
        // -------------------------------------------------------------
        System.out.println("\n--- Inciso e) ---");
        Polinomio pEx1 = new Polinomio(new double[]{1, -3, 0, 1.5, 0.25});
        ResultadoDivisionCuadratica resE = OperacionesPolinomios.dividirHornerCuadratico(pEx1, -2.5, 7.0);

        System.out.println("P(x) = " + pEx1);
        System.out.println("Cociente C(x) = " + resE.cociente);
        System.out.println("Resto R(x) = (" + resE.resto1 + ")x + " + resE.resto0);
        
        Polinomio divisorCuadratico = new Polinomio(new double[]{1.0, -2.5, 7.0});
        Polinomio restoPolinomio = new Polinomio(new double[]{resE.resto1, resE.resto0});
        System.out.println("Forma equivalente: " + OperacionesPolinomios.formatearEquivalenciaPolinomica(pEx1, resE.cociente, divisorCuadratico, restoPolinomio));
    }
}