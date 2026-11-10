package logic;

import model.Polinomio;
import java.util.List;

public class MainPrueba {
    public static void main(String[] args) {
        // Probamos con P(x) = x^2 - 4 (coeficientes: [1.0, 0.0, -4.0])
        Polinomio pol = new Polinomio(new double[]{1.0, 0.0, -4.0});
        
        List<BairstowSolver.RaizCompleja> raices = BairstowSolver.bairstow(pol, 0.5, 0.5, 1e-6, 100);
        
        System.out.println("Raíces encontradas:");
        for (BairstowSolver.RaizCompleja r : raices) {
            System.out.println(r);
        }
    }
}
