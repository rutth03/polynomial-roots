package logic;

import model.Polinomio;
import java.util.ArrayList;
import java.util.List;

public class BairstowSolver {
    //clase auxiliar para representar raíces complejas (a + bi)
    public static class RaizCompleja{
        private final double real;
        private final double imaginaria;
        
        public RaizCompleja(double real, double imaginaria) {
            this.real = real;
            this.imaginaria = imaginaria;
        }

        public double getReal() {
            return real;
        }
        public double getImaginaria() {
            return imaginaria;
        }
        
        @Override 
        public String toString() {
            if(Math.abs(imaginaria) < 1e-10) {
                return String.format("%.4f", real);
            } else if(Math.abs(imaginaria) > 0) {
                return String.format("%.4f + %.4fi",real, imaginaria);
            } else {
                return String.format("%.4f - %.4fi", real, Math.abs(imaginaria));
            }
        }
    }
 //formula cuadratica para el factor: x^2 - r*x -s
 //retorna una lista con las dos raices obtenidas
    public static List<RaizCompleja> formulaCuad(double r, double s){
        List<RaizCompleja> raices = new ArrayList<>();
        double disc = r * r + 4.0 * s; //dicriminante

        if(disc > 0){
            //raices reales distintas
            double rReal1 = (r + Math.sqrt(disc)) / 2.0;
            double rReal2 = (r - Math.sqrt(disc)) / 2.0;
            raices.add(new RaizCompleja(rReal1, 0.0));
            raices.add(new RaizCompleja(rReal2, 0.0));
        }else{
            //raices complejas conjugadas
            double realPart = r / 2.0;
            double imagPart = Math.sqrt(Math.abs(disc)) / 2.0;
            raices.add(new RaizCompleja(realPart, imagPart));
            raices.add(new RaizCompleja(realPart, -imagPart));
        }
        return raices;
    }
//reduce el polinomio dividiendolo de forma iterativa por un factor cuadartico (x^2 - r*x - s) hasta extraer todas sus raices
/*
* @param p: polinomio a resolver
* @param r0: valor inicial de r 
* @param s0: valor inicial de s
* @param e: tolerancia para el error relativo
* @param maxIter: numero maximo de iteraciones
* @return lista de raices (reales o complejas) del polinomio p
*/ 
    public static List<RaizCompleja> bairstow(Polinomio p, double r0, double s0, double e, int maxIter) {
        List<RaizCompleja> raices = new ArrayList<>();
        //clonamos los coeficientes del polinomio para no modificar el original
        double [] coef = p.getCoeficientes().clone();
        int n = coef.length - 1; //grado del polinomio

        double r = r0;
        double s = s0;
        int iter = 0;
        //bucle principal: mientras el polinomio sea de grado mayor a 2
        while(n > 2 && iter <= maxIter){
            iter = 0;
            double errorR, errorS;
            //bucle iterativo de N-Raphson de dos variables para refinar r y s
            do{
                iter++;
                //calculamos los coeficientes b y c por Horner
                double[]b = calcularB(coef, r, s);
                double[]c = calcularB(b, r, s);
                //extraccion de restos de b[]
                double bN1 = b[b.length - 1];
                double bN0 = b[b.length - 2];
                //extraccion de coeficientes de c[] 
                double termC1 = c[c.length - 3 >= 0 ? c.length - 3 : 0];
                double termC2 = c[c.length - 2];
                double termC3 = c[c.length - 1];
                //determinante del sistema lineal para hallar delta r y delta s
                double det = (termC2 * termC2) - (termC1 * termC3);

                if(Math.abs(det) > 1e-14){
                    //calculo de las diferencias de dr y ds 
                    double dr = (bN0 * termC3 - bN1 * termC2) / det;
                    double ds = (bN1 * termC1 - bN0 * termC2) / det;
                    //actualizamos r y s
                    r += dr;
                    s += ds;
                    //calculo del error relativo
                    errorR = Math.abs(dr / (r != 0 ? r : 1.0));
                    errorS = Math.abs(ds / (s != 0 ? s : 1.0));
                }else{
                    //si det = 0, desplazamos r y s
                    r += 1.0;
                    s += 1.0;
                    iter = 0;
                    errorR = 1.0;
                    errorS = 1.0;
                }
                //criterios de parada: tolerancia alcanzada o limite de iteraciones superado
                if((errorR <= e && errorS <= e) || iter > maxIter){
                    break;
                }
            }while(true);
            //una vez refinados r y s, calculamos las raices del factor cuadratico
            List<RaizCompleja> raicesParciales = formulaCuad(r, s);
            raices.addAll(raicesParciales);
            //actualizamos el polinomio dividiendolo por el factor cuadratico (x^2 - r*x - s)
            n = n - 2;
            coef = obtenerCocientePolinomio(coef, r, s);
        }
        //si el polinomio restante es de grado 2 o 1, resolvemos directamente
        if(iter <= maxIter){
            if(n == 2){
                //grado 2: usamos la formula cuadratica
                double rFin = -coef[1] / coef[0];
                double sFin = -coef[2] / coef[0];

                raices.addAll(formulaCuad(rFin, sFin));
            }else if(n == 1){
                //grado 1: solucion unica
                double xUnica = -coef[1] / coef[0];
                raices.add(new RaizCompleja(xUnica, 0.0));
            }
        }
        return raices;
    }
//metodo auxiliar para calcular los coeficientes b[] de Horner
    private static double[] calcularB(double[] coef, double r, double s){
        int n = coef.length - 1;
        double[] b = new double[n + 1];
        b[0] = coef[0];

        if(n >= 1){
            b[1] = coef[1] + r * b[0];
        }

        for(int i = 2; i <= n; i++){
            b[i] = coef[i] + r * b[i - 1] + s * b[i - 2];
        }
        return b;
    }
//metodo auxiliar para obtener el cociente del polinomio al dividirlo por el factor cuadratico (x^2 - r*x - s)
    private static double[] obtenerCocientePolinomio(double[] coef, double r, double s){
        int n = coef.length - 1;
        double[] b = calcularB(coef, r, s);
        double[] cociente = new double[n - 1];

        for(int i = 0; i < n - 1; i++){
            cociente[i] = b[i];
        }
        return cociente;
    }
}
