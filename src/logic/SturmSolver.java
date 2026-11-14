
package logic;

import java.util.ArrayList;
import model.Polinomio;

public class SturmSolver{
    //interfaz
	private ArrayList<Polinomio> sucesionOriginal = new ArrayList<>();
    private Polinomio mcd;

    public ArrayList<Polinomio> getSucesionOriginal() {
        return sucesionOriginal;
    }

    public Polinomio getMcd() {
        return mcd;
    }

    /**
     * Construye la sucesión de Sturm para un polinomio P(x).
     * f0(x) = P(x)
     * f1(x) = P'(x)
     * fk(x) = - resto(f_{k-2}, f_{k-1}) * lambda_k
     */
    public ArrayList<Polinomio> generarSucesion(Polinomio p){
	    sucesionOriginal = new ArrayList<>();
        mcd = null;
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
            OperacionesPolinomios.ResultadoDivisionGeneral div =
                OperacionesPolinomios.divisionGeneral2(
                    sturm.get(i-1), sturm.get(i)
                );

            Polinomio resto = div.resto;

            //si resto = 0, la sucesion regular termina
            if(resto.getGrado() == 0 &&
               Math.abs(resto.getCoeficiente(0)) < 1e-12){
		sucesionOriginal = new ArrayList<>(sturm);
		mcd = sturm.get(sturm.size() - 1);
		break;
		}
                

            //invertimos el signo del resto
            Polinomio siguiente =
                OperacionesPolinomios.multiplicarEscalar(resto, -1.0);

            sturm.add(siguiente);
            i++;

            if(sturm.get(i).getGrado() == 0)
                break;
        }
	if(sucesionOriginal.isEmpty()){
		sucesionOriginal = new ArrayList<>(sturm);
	}
        return sturm;
    }

    /**
     * Cuenta la cantidad de cambios de signo N(alpha)
     * en la sucesión de Sturm al evaluar en x = alpha.
     * No se cuentan los ceros exactos según la teoría de Sturm.
     */
    public int calcularCambiosDeSigno(
        ArrayList<Polinomio> sturm, double alpha
    ){
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
    public int contarRaicesEnIntervalo(
        Polinomio p, double a, double b
    ){
        ArrayList<Polinomio> sturm = generarSucesion(p);

        int na = calcularCambiosDeSigno(sturm, a);
        int nb = calcularCambiosDeSigno(sturm, b);

        return Math.abs(na - nb);
    }

    public String contarRaicesConCotas(Polinomio p, int metodo) {

        CotasDePolinomio cotas = new CotasDePolinomio();

        // Obtenemos las cuatro cotas automáticamente
        double supPositiva = cotas.cotaSuperiorPositiva(p, metodo);
        double infPositiva = cotas.cotaInferiorPositiva(p, metodo);
        double infNegativa = cotas.cotaInferiorNegativa(p, metodo);
        double supNegativa = cotas.cotaSuperiorNegativa(p, metodo);

        String resultado = "";

        // Analizamos las raices positivas
        if (supPositiva != 0) {
            int raicesPositivas =
                contarRaicesEnIntervalo(p, infPositiva, supPositiva);

            resultado += "Intervalo positivo: [" + infPositiva + ", "
                + supPositiva + "]\n";

            resultado += "Cantidad de raices positivas: "
                + raicesPositivas + "\n";

        } else {
            resultado += "No hay raices positivas.\n";
        }

        // Analizamos las raices negativas
        if (infNegativa != 0) {
            int raicesNegativas =
                contarRaicesEnIntervalo(p, infNegativa, supNegativa);

            resultado += "Intervalo negativo: [" + infNegativa + ", "
                + supNegativa + "]\n";

            resultado += "Cantidad de raices negativas: "
                + raicesNegativas + "\n";

        } else {
            resultado += "No hay raices negativas.\n";
        }

        return resultado;
    }

    // SUBDIVISION AUTOMATICA DE INTERVALOS
    // Obtiene las cotas y busca automaticamente los subintervalos
    public String aislarRaicesConCotas(Polinomio p, int metodo) {

        CotasDePolinomio cotas = new CotasDePolinomio();

        double supPositiva = cotas.cotaSuperiorPositiva(p, metodo);
        double infPositiva = cotas.cotaInferiorPositiva(p, metodo);
        double infNegativa = cotas.cotaInferiorNegativa(p, metodo);
        double supNegativa = cotas.cotaSuperiorNegativa(p, metodo);

        StringBuilder resultado = new StringBuilder();

        resultado.append("SUBINTERVALOS DE RAICES\n\n");

        // Buscamos las raices positivas
        if (supPositiva != 0) {
            resultado.append("Raices positivas:\n");
            buscarSubintervalos(
                p, infPositiva, supPositiva, resultado, 0
            );
        } else {
            resultado.append("No hay raices positivas.\n");
        }

        resultado.append("\n");

        // Buscamos las raices negativas
        if (infNegativa != 0) {
            resultado.append("Raices negativas:\n");
            buscarSubintervalos(
                p, infNegativa, supNegativa, resultado, 0
            );
        } else {
            resultado.append("No hay raices negativas.\n");
        }

        return resultado.toString();
    }

    // Divide los intervalos hasta aislar las raices
    private void buscarSubintervalos(
        Polinomio p, double a, double b,
        StringBuilder resultado, int profundidad
    ) {

        double anchoMaximo = 1.0;
        int profundidadMaxima = 60;

        // Contamos las raices del intervalo actual
        int cantidad = contarRaicesEnIntervalo(p, a, b);

        // Si no hay raices, descartamos el intervalo
        if (cantidad == 0) {
            return;
        }

        // Si hay una sola raiz y el intervalo es pequeño, lo mostramos
        if (cantidad == 1 && (b - a) <= anchoMaximo) {
            resultado.append("[")
                .append(a).append(", ")
                .append(b).append("] -> 1 raiz\n");
            return;
        }

        // Evitamos subdivisiones infinitas
        if (profundidad >= profundidadMaxima) {
            resultado.append("[")
                .append(a).append(", ")
                .append(b).append("] -> ")
                .append(cantidad)
                .append(" raices (limite de profundidad)\n");
            return;
        }

        // Calculamos el punto medio
        double medio = a + (b - a) / 2.0;

        // Comprobamos que se pueda seguir dividiendo
        if (medio == a || medio == b) {
            resultado.append("[")
                .append(a).append(", ")
                .append(b).append("] -> ")
                .append(cantidad).append(" raices\n");
            return;
        }

        // Dividimos el intervalo en dos partes
        buscarSubintervalos(
            p, a, medio, resultado, profundidad + 1
        );

        buscarSubintervalos(
            p, medio, b, resultado, profundidad + 1
        );
    }

    // Obtiene los intervalos aislantes como datos numericos para graficarlos
    public ArrayList<double[]> obtenerIntervalosAislantes(
        Polinomio p, int metodo
    ) {

        CotasDePolinomio cotas = new CotasDePolinomio();

        double supPositiva = cotas.cotaSuperiorPositiva(p, metodo);
        double infPositiva = cotas.cotaInferiorPositiva(p, metodo);
        double infNegativa = cotas.cotaInferiorNegativa(p, metodo);
        double supNegativa = cotas.cotaSuperiorNegativa(p, metodo);

        ArrayList<double[]> intervalos = new ArrayList<>();

        if (supPositiva != 0) {
            buscarIntervalosGraficos(
                p, infPositiva, supPositiva, intervalos, 0
            );
        }

        if (infNegativa != 0) {
            buscarIntervalosGraficos(
                p, infNegativa, supNegativa, intervalos, 0
            );
        }

        return intervalos;
    }

    // Busca intervalos que contienen una sola raiz
    private void buscarIntervalosGraficos(
        Polinomio p, double a, double b,
        ArrayList<double[]> intervalos, int profundidad
    ) {

        double anchoMaximo = 1.0;
        int profundidadMaxima = 60;

        int cantidad = contarRaicesEnIntervalo(p, a, b);

        if (cantidad == 0) {
            return;
        }

        if (cantidad == 1 && (b - a) <= anchoMaximo) {
            intervalos.add(new double[]{a, b});
            return;
        }

        if (profundidad >= profundidadMaxima) {
            return;
        }

        double medio = a + (b - a) / 2.0;

        if (medio == a || medio == b) {
            return;
        }

        buscarIntervalosGraficos(
            p, a, medio, intervalos, profundidad + 1
        );

        buscarIntervalosGraficos(
            p, medio, b, intervalos, profundidad + 1
        );
    }

	public static double puntoMedio(Polinomio p, double a, double b) {
    return a + (b - a) / 2.0;
}
}
