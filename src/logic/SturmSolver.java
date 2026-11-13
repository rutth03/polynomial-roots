package logic;

import java.util.ArrayList;

import model.Polinomio;

public class SturmSolver{
    // interfaz
    private ArrayList<Polinomio> sucesionOriginal = new ArrayList<>(); 
    private Polinomio mcd = null;                                      

    public ArrayList<Polinomio> getSucesionOriginal(){ return sucesionOriginal; } 
    public Polinomio getMcd(){ return mcd; }                                      

    /**
     * Construye la sucesión de Sturm para un polinomio P(x).
     * f0(x) = P(x)
     * f1(x) = P'(x)
     * fk(x) = - resto(f_{k-2}, f_{k-1}) * lambda_k
     */

    public ArrayList<Polinomio> generarSucesion(Polinomio p){
        ArrayList<Polinomio> sturm = new ArrayList<>();
        mcd=null;
        //f0(x) = P(x)
        sturm.add(p);

        //f1(x) = P'(x)
        Polinomio f1 = p.derivar();
        if(f1.getGrado() == 0 && f1.getCoeficiente(0) == 0.0){
            sucesionOriginal = new ArrayList<>(sturm);
            return sturm; //si f' es cero
        }

        sturm.add(f1);

        boolean terminoEnCero = completar(sturm); 
        sucesionOriginal = new ArrayList<>(sturm);

        //  (raices multiples)
        Polinomio ultimo = sturm.get(sturm.size() - 1);
        if(terminoEnCero && ultimo.getGrado() > 0){
            mcd = ultimo;
            ArrayList<Polinomio> reducida = new ArrayList<>();
            Polinomio g0 = quitarCeros(OperacionesPolinomios.divisionGeneral(sturm.get(0), mcd).cociente); 
            reducida.add(g0);
            if(g0.getGrado() > 0){
                reducida.add(g0.derivar()); 
                completar(reducida);
            }
            return reducida;
        }
        return sturm;
    }

    /* agrega f2, f3, ... a la sucesion. Devuelve true si termino porque el resto dio 0. */
    private boolean completar(ArrayList<Polinomio> sturm){
        int i = sturm.size() - 1;
        while(true){
            if(sturm.get(i).getGrado() == 0) 
                return false;
            OperacionesPolinomios.ResultadoDivisionGeneral div = OperacionesPolinomios.divisionGeneral(sturm.get(i-1),sturm.get(i));
            Polinomio resto = quitarCeros(div.resto); 

            //si resto = 0, la sucesion regular termina
            if(resto.getGrado() == 0 && Math.abs(resto.getCoeficiente(0)) < 1e-12)
                return true;

            //invertimos el signo del resto
            Polinomio siguiente = OperacionesPolinomios.multiplicarEscalar(resto, -1.0);

            sturm.add(normalizar(siguiente)); 
            i++;
        }
    }

    private static Polinomio quitarCeros(Polinomio p){
        double[] c = p.getCoeficientes();
        double max = 0;
        for(double v : c) max = Math.max(max, Math.abs(v));
        int i = 0;
        while(i < c.length - 1 && Math.abs(c[i]) <= 1e-9 * Math.max(1, max)) i++;
        double[] r = new double[c.length - i];
        System.arraycopy(c, i, r, 0, r.length);
        if(r.length == 0) r = new double[]{0.0};
        return new Polinomio(r);
    }

    private static Polinomio normalizar(Polinomio p){
        double[] c = p.getCoeficientes();
        for(int k = 1; k <= 100000; k++){
            long[] e = new long[c.length];
            boolean enteros = true;
            for(int i = 0; i < c.length && enteros; i++){
                double v = c[i] * k;
                if(Math.abs(v) > 1e15 || Math.abs(v - Math.rint(v)) > 1e-6 * Math.max(1, Math.abs(v))) enteros = false;
                else e[i] = Math.round(v);
            }
            if(enteros){
                long g = 0;
                for(long v : e) g = mcdEnteros(g, Math.abs(v));
                if(g == 0) return p;
                double[] r = new double[c.length];
                for(int i = 0; i < c.length; i++) r[i] = (double) (e[i] / g);
                return new Polinomio(r);
            }
        }
        // si no se encontraron enteros: se divide por el valor absoluto del coeficiente principal
        return OperacionesPolinomios.multiplicarEscalar(p, 1.0 / Math.abs(c[0]));
    }

    private static long mcdEnteros(long a, long b){ 
        while(b != 0){ long t = a % b; a = b; b = t; }
        return a;
    }

    /**
     * Cuenta la cantidad de cambios de signo N(alpha) en la sucesión de Sturm al evaluar en x = alpha.
     * No se cuentan los ceros exactos según la teoría de Sturm.
     */
    public int calcularCambiosDeSigno(ArrayList<Polinomio> sturm, double alpha){
        ArrayList<Double> valores = new ArrayList<>();
        for(Polinomio pol : sturm)
            valores.add(pol.evaluar(alpha));

        int cambios = 0;
        Double signoAnterior = null;

        for(double val : valores){
            //si el valor es cero se ignora
            if(Math.abs(val) < 1e-12)
                continue;

            double signoActual = Math.signum(val);
            if(signoAnterior != null){
                if(signoAnterior * signoActual < 0)
                    cambios++;
            }
            signoAnterior = signoActual;
        }
        return cambios;
    }

    
    public ArrayList<Double> subdividir(Polinomio p, ArrayList<Polinomio> sturm, ArrayList<Double> puntos){
        ArrayList<Double> pts = new ArrayList<>(puntos);
        pts.sort(Double::compare);
        for(int vuelta = 0; vuelta < 60; vuelta++){ // tope de seguridad
            boolean agrego = false;
            for(int j = 0; j + 1 < pts.size(); j++){
                double a = pts.get(j), b = pts.get(j + 1);
                int d = Math.abs(calcularCambiosDeSigno(sturm, a) - calcularCambiosDeSigno(sturm, b));
                if(d > 1 && b - a > 1e-9){
                    pts.add(j + 1, puntoMedio(p, a, b));
                    j++;
                    agrego = true;
                }
            }
            if(!agrego) break;
        }
        return pts;
    }

    public static double puntoMedio(Polinomio p, double a, double b){
        double[] t = {0.5, 0.25, 0.75, 0.375, 0.625};
        for(double k : t){
            double m = a + (b - a) * k;
            if(Math.abs(p.evaluar(m)) > 1e-9) return m;
        }
        return a + (b - a) * 0.4142;
    }

    /**
     * Calcula la cantidad de raíces reales de P(x) en el intervalo [a, b]
     * utilizando el Teorema de Sturm: N_raices = |N(a) - N(b)|
     */
    public int contarRaicesEnIntervalo(Polinomio p, double a, double b){
        ArrayList<Polinomio> sturm = generarSucesion(p);
        int na = calcularCambiosDeSigno(sturm, a);
        int nb = calcularCambiosDeSigno(sturm, b);
        return Math.abs(na - nb);
    }
}
