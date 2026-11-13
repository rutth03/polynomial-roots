package logic;

import model.Polinomio;
import java.util.ArrayList;
import java.util.List;


public class BairstowSolver {
    private static final List<double[]> tabla = new ArrayList<>();      // tabla - filas de la tabla
    private static final List<double[]> polinomios = new ArrayList<>(); // tabla - polinomio de cada factor
    private static int factorActual = 0;                                // tabla - factor actual

    public static List<double[]> getTabla() { return tabla; }               // tabla - devuelve las filas
    public static List<double[]> getPolinomios() { return polinomios; }     // tabla - devuelve los polinomios

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
        int pasosTotales = 0;                       // tabla - contador de pasos
        final int topePasos = 10 * maxIter + 100;   // tabla - maximo de pasos

        while((errorR > e || errorS > e) && iter <= maxIter){
            iter++;
            pasosTotales++;                         // tabla - cuenta el paso
            double rAntes = r, sAntes = s;          // tabla - r y s de la fila

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

                // tabla - guarda la fila
                tabla.add(new double[]{factorActual, iter - 1, rAntes, sAntes, dr, ds, errorR, errorS, 0});

                reintentosDet = 0; // Reiniciar el contador de reintentos si se encuentra un determinante válido

            } else {
                // tabla - guarda la fila (det = 0)
                tabla.add(new double[]{factorActual, iter - 1, rAntes, sAntes, 1.0, 1.0, Double.NaN, Double.NaN, 1});

                //si det = 0, modificar r y s
                r += 1.0;
                s += 1.0;

                iter = 0;

                reintentosDet++;

                if(reintentosDet > maxIter){
                    throw new ArithmeticException("No se pudo encontrar un determinante distinto de cero");
                }
            }

            // tabla - corta si se pasa del maximo
            if(pasosTotales >= topePasos){
                throw new ArithmeticException("No se pudo converger: se cortó tras " + pasosTotales + " pasos");
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
        /*
        @Override
        public String toString() {
            double realMostrar = Math.abs(real) < 1e-10 ? 0.0 : real;
            double imaginariaMostrar = Math.abs(imaginaria) < 1e-10 ? 0.0 : imaginaria;

            return realMostrar + " + " + imaginariaMostrar + "i";

    }*/
    @Override
    public String toString() {
        if (imaginaria == 0.0) {            // raíz real: solo el número
            return num(real);
        }
        String signo = imaginaria < 0 ? " - " : " + ";
        String parteIm = num(Math.abs(imaginaria));
        if (parteIm.equals("1")) parteIm = "";   // 1i se escribe i
        if (real == 0.0) {                  // imaginaria pura
            return (imaginaria < 0 ? "-" : "") + parteIm + "i";
        }
        return num(real) + signo + parteIm + "i";
    }

    // Valor exacto, sin redondear: solo quita el ".0" sobrante (3.0 -> 3)
    private static String num(double v) {
        if (Double.isNaN(v) || Double.isInfinite(v)) return String.valueOf(v);
        return java.math.BigDecimal.valueOf(v).stripTrailingZeros().toPlainString();
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

        tabla.clear();          // tabla - limpia filas
        polinomios.clear();     // tabla - limpia polinomios
        factorActual = 0;       // tabla - reinicia factor

        while(n > 2){
            factorActual++;             // tabla - nuevo factor
            polinomios.add(P.clone());  // tabla - guarda el polinomio

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

        polinomios.add(P.clone());      // tabla - guarda el resto

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
