package logic;

import java.util.ArrayList;

import model.Polinomio;

public class SturmSolver{
    /**
     * Construye la sucesión de Sturm para un polinomio P(x).
     * f0(x) = P(x)
     * f1(x) = P'(x)
     * fk(x) = - resto(f_{k-2}, f_{k-1}) * lambda_k
     */

    public ArrayList<Polinomio> generarSucesion(Polinomio p){
        ArrayList<Polinomio> sturm = new ArrayList<>();

        //f0(x) = P(x)
        sturm.add(p);

        //f1(x) = P'(x)
        Polinomio f1 = p.derivar();
        if(f1.getGrado() == 0 && f1.getCoeficiente(0) == 0.0)
            return sturm; //si f' es cero

        sturm.add(f1);

        int i = 1;
        while(true){
            //dividimos f_{i-1} entre f_i para obtener el resto
            OperacionesPolinomios.ResultadoDivisionGeneral div = OperacionesPolinomios.divisionGeneral(sturm.get(i-1),sturm.get(i));
            Polinomio resto = div.resto;

            //si resto = 0, la sucesion regular termina
            if(resto.getGrado() == 0 && Math.abs(resto.getCoeficiente(0)) < 1e-12)
                break;

            //invertimos el signo del resto
            Polinomio siguiente = OperacionesPolinomios.multiplicarEscalar(resto, -1.0);

            sturm.add(siguiente);
            i++;

            if(sturm.get(i).getGrado() == 0)
                break;

        }
        return sturm;
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