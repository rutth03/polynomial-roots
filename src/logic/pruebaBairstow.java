package logic;

import java.util.List;

import model.Polinomio;

public class pruebaBairstow {
    public static void main(String[] args) {
        double[] coeficientes = {1, -2, 0, 0, -1, 2}; //grado 5
       // double[] coeficientes = {1, -11, 78, -14, -444}; //grado 4
        Polinomio p = new Polinomio(coeficientes);

        List<BairstowSolver.RaizCompleja> raices =BairstowSolver.bairstow(p, 0.5, -1, 0.001, 100);
       // List<BairstowSolver.RaizCompleja> raices =BairstowSolver.bairstow(p, 1, -2, 0.001, 100);

        System.out.println("Polinomio: " + p);
        System.out.println("\nRaices:");

        for (BairstowSolver.RaizCompleja raiz : raices) {
            System.out.println(raiz);
        }
    }
}