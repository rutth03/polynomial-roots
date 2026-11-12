package logic;

import model.Polinomio;
import java.util.ArrayList;
import java.util.List;


public class BairstowSolver {
    private static double[] hornerDoble(double[] P, double r, double s){
        int n = P.length - 1;
        double[] resultado = new double[n + 1];

        resultado[0] = P[0];

        if(n >= 1)
            resultado[1] = P[1] + r * resultado[0];

        for(int i = 2; i <= n; i++)
            resultado[i] = P[i] + r * resultado[i - 1] + s * resultado[i - 2];
        
        return resultado;
    }

    private static double[] iterBairstow(double[] P, double r, double s, double e, int maxIter){
        int n = P.length - 1;
        int iter = 0;

        double errorR = Double.POSITIVE_INFINITY;
        double errorS = Double.POSITIVE_INFINITY;

        int reintentosDet = 0;

        while((errorR > e || errorS > e) && iter <= maxIter){
            iter++;

            //calculo de b y c por Horner doble
            double[] b = hornerDoble(P, r, s);
            double[] c = hornerDoble(b, r, s);

            //calculo del determinante
            double det = c[n - 2] * c[n - 2] - c[n - 3] * c[n - 1];

            if(Double.isFinite(det) && Math.abs(det) > 1e-12){
                //calculo de correcciones
                double dr = (-b[n - 1] * c[n - 2] + b[n] * c[n - 3]) / det;
                double ds = (b[n - 1] * c[n - 1] - b[n] * c[n - 2]) / det;

                if(!Double.isFinite(dr) || !Double.isFinite(ds)){
                    throw new ArithmeticException("Correcciones no validas");
                }

                //actualizacion de r y s
                r += dr;
                s += ds;

                //calculo de errores
                errorR = (r != 0) ? Math.abs(dr / r) : Math.abs(dr);
                errorS = (s != 0) ? Math.abs(ds / s) : Math.abs(ds);

                reintentosDet = 0; // Reiniciar el contador de reintentos si se encuentra un determinante válido
                
            } else {
                //si det = 0, modificar r y s
                r += 1.0;
                s += 1.0;

                iter = 0;

                reintentosDet++;

                if(reintentosDet > maxIter){
                    throw new ArithmeticException("No se pudo encontrar un determinante distinto de cero");
                }
            }
        }
        //comprobar convergencia
        if(errorR > e || errorS > e){
            throw new ArithmeticException("No se pudo converger a una solución en " + maxIter + " iteraciones");
        }

        //valores finales de r y s
        return new double[]{r, s};
    }

    public static List<RaizCompleja> formulaCuad(double r, double s){
        List<RaizCompleja> raices = new ArrayList<>();
        double discriminante = r * r + 4.0 * s;

        if(discriminante >= 0.0){
            double x1 = (r + Math.sqrt(discriminante)) / 2.0;
            double x2 = (r - Math.sqrt(discriminante)) / 2.0;

            raices.add(new RaizCompleja(x1, 0.0));
            raices.add(new RaizCompleja(x2, 0.0));
        }else{
            double real = r / 2.0;
            double imaginaria = Math.sqrt(Math.abs(-discriminante)) / 2.0;

            raices.add(new RaizCompleja(real, imaginaria));
            raices.add(new RaizCompleja(real, -imaginaria));
        }

        return raices;
    }

    public static class RaizCompleja {
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
            double realMostrar = Math.abs(real) < 1e-10 ? 0.0 : real;
            double imaginariaMostrar = Math.abs(imaginaria) < 1e-10 ? 0.0 : imaginaria;

            return realMostrar + " + " + imaginariaMostrar + "i";
        }
    }

    private static double[] obtenerCociente(double[] P, double r, double s){
        double[] b = hornerDoble(P, r, s);
        int nvoGrado = P.length - 3; 
        double[] cociente = new double[nvoGrado + 1];

        for(int i = 0; i <= nvoGrado; i++)
            cociente[i] = b[i];
        
        return cociente;
    }

    public static List<RaizCompleja> bairstow(Polinomio p, double r, double s, double e, int maxIter){
        double[] P = p.getCoeficientes();
        int n = p.getGrado();
        List<RaizCompleja> raices = new ArrayList<>();

        while(n > 2){
            //buscar r y s usando iteraciones de bairstow
            double[] resultado = iterBairstow(P, r, s, e, maxIter);
            r = resultado[0];
            s = resultado[1];

            //raices del factor cuadratico
            List<RaizCompleja> nuevasRaices = formulaCuad(r, s);
            raices.addAll(nuevasRaices);

            //cociente del polinomio 
            P = obtenerCociente(P, r, s);
            n -= 2;
        }

        //resolver el polinomio restante
        if(n == 2){
            //normalizar el polinomio para obtener x^2 - r*s - s
            r = -P[1] / P[0];
            s = -P[2] / P[0];

            List<RaizCompleja> nuevasRaices = formulaCuad(r, s);
            raices.addAll(nuevasRaices);
        } else if(n == 1){
            //resolver el polinomio lineal
            double x = -P[1] / P[0];
            raices.add(new RaizCompleja(x, 0));
        }

        return raices;
    }
}