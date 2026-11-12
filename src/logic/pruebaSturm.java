package logic;

import java.util.ArrayList;

import model.Polinomio;

public class pruebaSturm {
    public static void main(String[] args) {
        //P4(x) = 36x^4 - 12x^3 + -221x^2 + 37x + 70
        double[] coeficientes = {36, -12, -221, 37, 70};
        Polinomio p = new Polinomio(coeficientes);

        System.out.println("=========================================");
        System.out.println(" PRUEBA DEL MÉTODO DE STURM");
        System.out.println("=========================================");
        System.out.println("Polinomio original P(x) = " + p);
        System.out.println();

        SturmSolver sturmLogic = new SturmSolver();
        
        // Generamos la sucesión de Sturm (f0, f1, f2, ...)
        ArrayList<Polinomio> sucesion = sturmLogic.generarSucesion(p);

        System.out.println("--- Sucesión de Sturm generada ---");
        for (int i = 0; i < sucesion.size(); i++) {
            System.out.println("f_" + i + "(x) = " + sucesion.get(i));
        }
        System.out.println();

        double alpha1 = 0.5;
        double alpha2 = 3.0;

        int nAlpha1 = sturmLogic.calcularCambiosDeSigno(sucesion, alpha1);
        int nAlpha2 = sturmLogic.calcularCambiosDeSigno(sucesion, alpha2);

        System.out.println("--- Evaluación de Cambios de Signo N(alpha) ---");
        System.out.println("N(" + alpha1 + ") = " + nAlpha1);
        System.out.println("N(" + alpha2 + ") = " + nAlpha2);
        System.out.println();

        int raices = sturmLogic.contarRaicesEnIntervalo(p, alpha1, alpha2);
        System.out.println("-----------------------------------------");
        System.out.println("Cantidad de raíces reales en el intervalo [" + alpha1 + ", " + alpha2 + "] = " + raices);
        System.out.println("-----------------------------------------");
    }
    
}
