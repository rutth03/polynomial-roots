package logic;

import java.util.Arrays;
import java.util.Locale;

/**
 * Polinomio en una variable (x) con coeficientes reales.
 *
 * Constructores:
 *   new Parser()                      -> 0
 *   new Parser(1, 0, -3)              -> x^2 - 3   (orden descendente)
 *   new Parser("(x+1)^2")             -> x^2 + 2x + 1
 *   new Parser(otroPolinomio)         -> copia
 *
 * Los coeficientes pueden ser expresiones numericas: sen(2), cos 1, tg(0.5),
 * ln(3), raiz(2), pi, π, e, fracciones (1/2, 3/4x) ... Las funciones solo
 * aceptan constantes (sen(x) no, porque ya no seria un polinomio).
 * Los angulos van en radianes.
 */
public class Parser {

    private static final double EPS = 1e-12;

    /** c[i] = coeficiente de x^i (orden ascendente, interno). */
    private double[] c;

    // ------------------------------------------------------------------
    // Constructores
    // ------------------------------------------------------------------

    /** Polinomio cero. */
    public Parser() {
        this.c = new double[]{0};
    }

    /** Desde coeficientes en orden descendente: {1, 0, -3} = x^2 - 3 */
    public Parser(double... descendente) {
        setCoeficientes(descendente);
    }

    /** Desde un texto: "x^2 + x^2 - 3x + x - 2" */
    public Parser(String expresion) {
        setExpresion(expresion);
    }

    /** Constructor copia. */
    public Parser(Parser otro) {
        this.c = Arrays.copyOf(otro.c, otro.c.length);
    }

    // ------------------------------------------------------------------
    // Fabricas
    // ------------------------------------------------------------------

    /** Convierte un texto en polinomio (equivale a new Parser(texto)). */
    public static Parser parse(String texto) {
        if (texto == null || texto.trim().isEmpty())
            throw new IllegalArgumentException("Expresion vacia");
        Lector p = new Lector(texto.toLowerCase().replaceAll("\\s+", ""));
        Parser r = p.expresion();
        if (!p.fin())
            throw new IllegalArgumentException(
                    "Caracter inesperado '" + p.actual() + "' en la posicion " + p.pos);
        return r;
    }

    public static Parser constante(double v) {
        return ascendente(new double[]{v});
    }

    public static Parser x() {
        return ascendente(new double[]{0, 1});
    }

    private static Parser ascendente(double[] asc) {
        Parser p = new Parser();
        p.c = normalizar(asc);
        return p;
    }

    /** Quita ceros a la izquierda (de mayor grado) y limpia ruido numerico. */
    private static double[] normalizar(double[] asc) {
        int n = asc.length - 1;
        while (n > 0 && Math.abs(asc[n]) < EPS) n--;
        if (n < 0) return new double[]{0};
        double[] r = Arrays.copyOf(asc, n + 1);
        if (n == 0 && Math.abs(r[0]) < EPS) r[0] = 0;
        return r;
    }

    // ------------------------------------------------------------------
    // Getters
    // ------------------------------------------------------------------

    /** Coeficientes en orden descendente: x^2-3 -> {1, 0, -3}. Devuelve una copia. */
    public double[] getCoeficientes() {
        double[] r = new double[c.length];
        for (int i = 0; i < c.length; i++) r[i] = c[c.length - 1 - i];
        return r;
    }

    /** Coeficiente que acompana a x^grado (0 si el grado es mayor al del polinomio). */
    public double getCoeficiente(int grado) {
        if (grado < 0) throw new IllegalArgumentException("El grado no puede ser negativo");
        return grado < c.length ? c[grado] : 0;
    }

    public int getGrado() {
        return c.length - 1;
    }

    /** Coeficiente principal (el de mayor grado). */
    public double getCoeficientePrincipal() {
        return c[c.length - 1];
    }

    /** Termino independiente (a_0). */
    public double getTerminoIndependiente() {
        return c[0];
    }

    public boolean esConstante() {
        return c.length == 1;
    }

    public boolean esCero() {
        return c.length == 1 && c[0] == 0;
    }

    public double valorConstante() {
        if (!esConstante())
            throw new ArithmeticException("El valor depende de x; se esperaba una constante");
        return c[0];
    }

    // ------------------------------------------------------------------
    // Setters
    // ------------------------------------------------------------------

    /** Reemplaza todos los coeficientes (orden descendente). */
    public void setCoeficientes(double... descendente) {
        if (descendente == null || descendente.length == 0) {
            this.c = new double[]{0};
            return;
        }
        double[] asc = new double[descendente.length];
        for (int i = 0; i < descendente.length; i++)
            asc[i] = descendente[descendente.length - 1 - i];
        this.c = normalizar(asc);
    }

    /** Cambia el coeficiente de x^grado (agranda el polinomio si hace falta). */
    public void setCoeficiente(int grado, double valor) {
        if (grado < 0) throw new IllegalArgumentException("El grado no puede ser negativo");
        double[] asc = Arrays.copyOf(c, Math.max(c.length, grado + 1));
        asc[grado] = valor;
        this.c = normalizar(asc);
    }

    /** Reemplaza el polinomio por el que resulta de interpretar un texto. */
    public void setExpresion(String expresion) {
        this.c = parse(expresion).c;
    }

    
    /** Evalua el polinomio en x (Horner). */
    public double evaluar(double x) {
        double r = 0;
        for (int i = c.length - 1; i >= 0; i--) r = r * x + c[i];
        return r;
    }

    public Parser derivada() {
        if (c.length == 1) return constante(0);
        double[] d = new double[c.length - 1];
        for (int i = 1; i < c.length; i++) d[i - 1] = c[i] * i;
        return ascendente(d);
    }

    public Parser sumar(Parser o) {
        double[] r = new double[Math.max(c.length, o.c.length)];
        for (int i = 0; i < c.length; i++) r[i] += c[i];
        for (int i = 0; i < o.c.length; i++) r[i] += o.c[i];
        return ascendente(r);
    }

    public Parser restar(Parser o) {
        return sumar(o.negar());
    }

    public Parser negar() {
        return multiplicarEscalar(-1);
    }

    public Parser multiplicarEscalar(double k) {
        double[] r = new double[c.length];
        for (int i = 0; i < c.length; i++) r[i] = c[i] * k;
        return ascendente(r);
    }

    public Parser multiplicar(Parser o) {
        double[] r = new double[c.length + o.c.length - 1];
        for (int i = 0; i < c.length; i++)
            for (int j = 0; j < o.c.length; j++)
                r[i + j] += c[i] * o.c[j];
        return ascendente(r);
    }

    /** Division entre un polinomio constante (fracciones). */
    public Parser dividir(Parser o) {
        if (!o.esConstante())
            throw new ArithmeticException("Solo se puede dividir entre constantes");
        if (Math.abs(o.c[0]) < EPS)
            throw new ArithmeticException("Division entre cero");
        return multiplicarEscalar(1.0 / o.c[0]);
    }

    /** Potencia: exponente entero >= 0 si hay x; cualquier real si es constante. */
    public Parser potencia(Parser exp) {
        if (!exp.esConstante())
            throw new ArithmeticException("El exponente no puede contener x");
        double e = exp.c[0];
        if (esConstante()) {
            double v = Math.pow(c[0], e);
            if (Double.isNaN(v) || Double.isInfinite(v))
                throw new ArithmeticException("Potencia no valida");
            return constante(v);
        }
        if (Math.abs(e - Math.rint(e)) > EPS || e < 0)
            throw new ArithmeticException(
                    "Con x el exponente debe ser un entero no negativo");
        int n = (int) Math.rint(e);
        Parser r = constante(1);
        for (int i = 0; i < n; i++) r = r.multiplicar(this);
        return r;
    }

    // ------------------------------------------------------------------
    // Presentacion
    // ------------------------------------------------------------------

    private static String fmt(double v) {
        if (Math.abs(v - Math.rint(v)) < 1e-9) return String.valueOf((long) Math.rint(v));
        String s = String.format(Locale.US, "%.6f", v);
        return s.replaceAll("0+$", "").replaceAll("\\.$", "");
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i = c.length - 1; i >= 0; i--) {
            double a = c[i];
            if (Math.abs(a) < EPS) continue;
            boolean primero = sb.length() == 0;
            if (a < 0) sb.append(primero ? "-" : " - ");
            else if (!primero) sb.append(" + ");
            double abs = Math.abs(a);
            if (i == 0 || Math.abs(abs - 1) > 1e-9) sb.append(fmt(abs));
            if (i >= 1) sb.append("x");
            if (i > 1) sb.append("^").append(i);
        }
        return sb.length() == 0 ? "0" : sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Parser && Arrays.equals(c, ((Parser) o).c);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(c);
    }

    // ------------------------------------------------------------------
    // Lector (descenso recursivo)
    //
    //  expresion := termino (('+' | '-') termino)*
    //  termino   := unario (('*' | '/' | <multiplicacion implicita>) unario)*
    //  unario    := ('+' | '-') unario | potencia
    //  potencia  := primario ('^' unario)?
    //  primario  := numero | x | pi | e | funcion argumento | '(' expresion ')'
    // ------------------------------------------------------------------

    private static class Lector {
        final String s;
        int pos = 0;

        Lector(String s) {
            this.s = s;
        }

        boolean fin() {
            return pos >= s.length();
        }

        char actual() {
            return s.charAt(pos);
        }

        boolean consumir(char ch) {
            if (!fin() && actual() == ch) {
                pos++;
                return true;
            }
            return false;
        }

        boolean empiezaPrimario() {
            if (fin()) return false;
            char ch = actual();
            return Character.isDigit(ch) || ch == '.' || ch == '('
                    || Character.isLetter(ch) || ch == '\u221a';
        }

        Parser expresion() {
            Parser r = termino();
            while (!fin()) {
                if (consumir('+')) r = r.sumar(termino());
                else if (consumir('-')) r = r.restar(termino());
                else break;
            }
            return r;
        }

        Parser termino() {
            Parser r = unario();
            while (!fin()) {
                if (consumir('*')) r = r.multiplicar(unario());
                else if (consumir('/')) r = r.dividir(unario());
                else if (empiezaPrimario()) r = r.multiplicar(potencia()); // ej: 3x, 2sen(1)x
                else break;
            }
            return r;
        }

        Parser unario() {
            if (consumir('-')) return unario().negar();
            if (consumir('+')) return unario();
            return potencia();
        }

        Parser potencia() {
            Parser base = primario();
            if (consumir('^')) return base.potencia(unario());
            return base;
        }

        Parser primario() {
            if (fin()) throw new IllegalArgumentException("La expresion termina inesperadamente");
            char ch = actual();

            if (ch == '(') {
                pos++;
                Parser r = expresion();
                if (!consumir(')')) throw new IllegalArgumentException("Falta cerrar parentesis");
                return r;
            }
            if (Character.isDigit(ch) || ch == '.') return numero();

            if (nombre("pi") || nombre("\u03c0")) return constante(Math.PI); // pi o el simbolo π
            if (nombre("\u221a")) return funcion("raiz", argumento());        // simbolo √
            if (nombre("x")) return x();

            String[] funciones = {"sen", "sin", "cos", "tan", "tg", "ln", "log", "raiz", "sqrt"};
            for (String f : funciones) {
                if (nombre(f)) return funcion(f, argumento());
            }
            if (nombre("e")) return constante(Math.E);

            throw new IllegalArgumentException("Simbolo desconocido '" + ch + "' en la posicion " + pos);
        }

        /** Argumento de una funcion: (expresion) o un primario simple (sen 2, raiz 9). */
        Parser argumento() {
            if (consumir('(')) {
                Parser r = expresion();
                if (!consumir(')')) throw new IllegalArgumentException("Falta cerrar parentesis");
                return r;
            }
            return primario();
        }

        boolean nombre(String n) {
            if (s.startsWith(n, pos)) {
                pos += n.length();
                return true;
            }
            return false;
        }

        Parser numero() {
            int ini = pos;
            while (!fin() && (Character.isDigit(actual()) || actual() == '.')) pos++;
            try {
                return constante(Double.parseDouble(s.substring(ini, pos)));
            } catch (NumberFormatException ex) {
                throw new IllegalArgumentException("Numero invalido: " + s.substring(ini, pos));
            }
        }

        Parser funcion(String nombre, Parser arg) {
            if (!arg.esConstante())
                throw new IllegalArgumentException(
                        nombre + " solo admite constantes: con x ya no seria un polinomio");
            double a = arg.valorConstante();
            double r;
            switch (nombre) {
                case "sen":
                case "sin": r = Math.sin(a); break;
                case "cos": r = Math.cos(a); break;
                case "tan":
                case "tg":  r = Math.tan(a); break;
                case "ln":  r = Math.log(a); break;
                case "log": r = Math.log10(a); break;
                default:    r = Math.sqrt(a); break; // raiz, sqrt
            }
            if (Double.isNaN(r) || Double.isInfinite(r))
                throw new ArithmeticException(nombre + "(" + a + ") no esta definido");
            return constante(r);
        }
    }
}