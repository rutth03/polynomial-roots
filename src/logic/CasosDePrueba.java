package logic;

import java.util.List;

public final class CasosDePrueba {
    public static final String EVALUAR = "Evaluar polinomio en un punto";
    public static final String HORNER = "División";
    public static final String NEWTON = "Newton";
    public static final String HALLEY = "Halley";
    public static final String BAIRSTOW = "Bairstow";
    public static final String RAICES = "Raíces enteras y racionales";
    public static final String COTAS = "Cotas de las raíces";
    public static final String STURM = "Sturm";

    public record Caso(String operacion, String nombre, String polinomio, String divisor,
            String x0, String r0, String s0, String epsilon, String iteraciones,
            String metodoCota, String puntosSturm, String puntoEvaluacion) {
        @Override
        public String toString() {
            return nombre;
        }
    }

    private static final List<Caso> CASOS = List.of(
            casoEvaluar("Evaluar x^2 + 2x + 1 en x = 2", "x^2 + 2x + 1", "2"),
            casoEvaluar("Evaluar x^3 - 2x - 5 en x = 2", "x^3 - 2x - 5", "2"),
            casoEvaluar("Evaluar 2x^2 + 3x - 2 en x = 1.5", "2x^2 + 3x - 2", "1.5"),

            casoDivision(HORNER, "P(x) = 6x^4 + x^3 + 5x^2 + 8x + 1; Q(x) = 2x^2 - x + 3", "6x^4 + x^3 + 5x^2 + 8x + 1", "2x^2 - x + 3"),
            casoDivision(HORNER, "P(x) = 6x^4 - 19x^3 + 7x^2 + 23x - 3 ; Q(x) = 3x - 2", "6x^4 - 19x^3 + 7x^2 + 23x - 3", "3x - 2"),
            casoDivision(HORNER, "P(x) = x^4 - 3x^3 - x + 1; Q(x) = x^2 + 2x - 3", "x^4 - 3x^3 - x + 1", "x^2 + 2x - 3"),

            casoIterativo(NEWTON, "Raíz múltiple (x-1)^2(x+2)", "(x-1)^2(x+2)", null, "2", null, null, "0.000001", "100"),
            casoIterativo(NEWTON, "Raíz de x^8 - 17x^4 + 16", "x^8 - 17x^4 + 16", null, "3", null, null, "0.00000001", "50"),
            casoIterativo(NEWTON, "Raíz de x^2 - 2", "x^2 - 2", null, "1", null, null, "0.00000001", "50"),

            casoIterativo(HALLEY, "Raíz múltiple (x-1)^2(x+2)", "(x-1)^2(x+2)", null, "2", null, null, "0.000001", "50"),
            casoIterativo(HALLEY, "Raíz de x^8 - 17x^4 + 16", "x^8 - 17x^4 + 16", null, "3", null, null, "0.00000001", "50"),
            casoIterativo(HALLEY, "Raíz de x^2 - 2", "x^2 - 2", null, "1", null, null, "0.00000001", "50"),

            casoIterativo(BAIRSTOW, "x^7 - 4x^6 + 5x^5 + 2x^4 - 33x^3 + 36x^2 - 37x + 30", "x^7 - 4x^6 + 5x^5 + 2x^4 - 33x^3 + 36x^2 - 37x + 30", null, null, "0.5", "0.5", "0.000001", "100"),
            casoIterativo(BAIRSTOW, "x^4 - 11x^3 + 78x^2 - 14x - 444", "x^4 - 11x^3 + 78x^2 - 14x - 444", null, null, "0.5", "0.5", "0.000001", "100"),
            casoIterativo(BAIRSTOW, "x^4 + 1", "x^4 + 1", null, null, "0.5", "0.5", "0.000001", "100"),

            casoSimple(RAICES, "Raíces enteras", "x^3 - x"),
            casoSimple(RAICES, "Raíces racionales", "6x^2 + x - 2"),
            casoSimple(RAICES, "Raíces enteras y racionales", "6x^5 - 493x^4 - 12913x^3 + 221030x^2 + 39600x - 1260000"),

            casoSimple(COTAS, "P(x) = x^2 - 4", "x^3 - 3x^2 - 6x + 8"),
            casoSimple(COTAS, "P(x) = x^3 - 6x^2 + 11x - 6", "x^3 - 6x^2 + 11x - 6"),
            casoSimple(COTAS, "P(x) = x^2 + 3x + 2", "x^2 + 3x + 2"),

            casoSturm("Raíces negativas", "x^3 + 6x^2 + 11x + 6"),
            casoSturm("Raíces positivas", "x^3 - 6x^2 + 11x - 6"),
            casoSturm("Raíces negativas y positivas", "x^8 - 17x^4 + 16"),
            casoSturm("Raíces negativas y positivas (raíz múltiple)", "x^3 - 3x^2 + 2")
    );

    private CasosDePrueba() {
    }

    public static List<Caso> paraOperacion(String operacion) {
        return CASOS.stream()
                .filter(caso -> caso.operacion().equals(operacion))
                .toList();
    }

    private static Caso casoSimple(String operacion, String nombre, String polinomio) {
        return new Caso(operacion, nombre, polinomio, null, null, null, null, null,
                null, null, null, null);
    }

    private static Caso casoDivision(String operacion, String nombre, String polinomio, String divisor) {
        return new Caso(operacion, nombre, polinomio, divisor, null, null, null, null,
                null, null, null, null);
    }

    private static Caso casoIterativo(String operacion, String nombre, String polinomio, String divisor,
            String x0, String r0, String s0, String epsilon, String iteraciones) {
        return new Caso(operacion, nombre, polinomio, divisor, x0, r0, s0, epsilon,
                iteraciones, null, null, null);
    }

    private static Caso casoEvaluar(String nombre, String polinomio, String puntoEvaluacion) {
        return new Caso(EVALUAR, nombre, polinomio, null, null, null, null, null,
                null, null, null, puntoEvaluacion);
    }

    private static Caso casoSturm(String nombre, String polinomio) {
        return new Caso(STURM, nombre, polinomio, null, null, null, null, null,
                null, null, null, null);
    }
}