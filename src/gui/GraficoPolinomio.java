package gui;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import model.Polinomio;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/**
 * Grafico de P(x) con ejes x e y.
 * - agregarPunto: marca un punto sobre el eje x con su etiqueta "(x, 0)".
 * - agregarBanda: sombrea un intervalo (para las cotas).
 * - Rueda del mouse: zoom. Arrastrar: mover. Doble clic: volver a la vista inicial.
 */
public class GraficoPolinomio extends Pane {

    public static final Color ROJO = Color.web("#d62728");
    public static final Color VERDE = Color.web("#2e9e4f");
    public static final Color NARANJA = Color.web("#e67e22");
    private static final Color CURVA = Color.web("#2865c5");
    private static final double M = 55; // margen

    private static class Punto {
        final double x;
        final String texto;
        final Color color;

        Punto(double x, String texto, Color color) {
            this.x = x;
            this.texto = texto;
            this.color = color;
        }
    }

    private static class Banda {
        final double desde, hasta;
        final Color color;

        Banda(double desde, double hasta, Color color) {
            this.desde = desde;
            this.hasta = hasta;
            this.color = color;
        }
    }

    private final Canvas canvas = new Canvas();
    private final Polinomio p;
    private final String titulo;
    private final List<Punto> puntos = new ArrayList<>();
    private final List<Banda> bandas = new ArrayList<>();
    private String nota = "";

    private double xMin, xMax, yMin, yMax;
    private double xMinInicial, xMaxInicial;
    private double pw, ph;                 // tamano del area de dibujo
    private double dragX, dragMin, dragMax;

    public GraficoPolinomio(Polinomio p, String titulo) {
        this.p = p;
        this.titulo = titulo;
        setStyle("-fx-background-color: white;");
        getChildren().add(canvas);
        canvas.widthProperty().bind(widthProperty());
        canvas.heightProperty().bind(heightProperty());
        canvas.widthProperty().addListener((o, a, b) -> dibujar());
        canvas.heightProperty().addListener((o, a, b) -> dibujar());

        ajustarRango();

        setOnScroll(e -> {
            if (pw <= 0 || (xMax - xMin < 1e-9 && e.getDeltaY() > 0)) return;
            double cx = xMin + (e.getX() - M) / pw * (xMax - xMin);
            double f = e.getDeltaY() > 0 ? 0.85 : 1 / 0.85;
            xMin = cx - (cx - xMin) * f;
            xMax = cx + (xMax - cx) * f;
            dibujar();
        });
        setOnMousePressed(e -> {
            dragX = e.getX();
            dragMin = xMin;
            dragMax = xMax;
        });
        setOnMouseDragged(e -> {
            if (pw <= 0) return;
            double dx = (e.getX() - dragX) / pw * (dragMax - dragMin);
            xMin = dragMin - dx;
            xMax = dragMax - dx;
            dibujar();
        });
        setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                xMin = xMinInicial;
                xMax = xMaxInicial;
                dibujar();
            }
        });
    }

    // ---------------- API ----------------

    public void agregarPunto(double x, String texto, Color color) {
        if (Double.isFinite(x)) puntos.add(new Punto(x, texto, color));
    }

    public void agregarBanda(double desde, double hasta, Color color) {
        if (Double.isFinite(desde) && Double.isFinite(hasta)) bandas.add(new Banda(desde, hasta, color));
    }

    public void setNota(String nota) {
        this.nota = nota == null ? "" : nota;
        dibujar();
    }

    /** Rango de x: el radio de Cauchy (todas las raices reales caen adentro) ampliado para incluir puntos y bandas. */
    public void ajustarRango() {
        double[] c = p.getCoeficientes();
        double a0 = c[0];
        double R = 10;
        if (a0 != 0) {
            double max = 0;
            for (int i = 1; i < c.length; i++) max = Math.max(max, Math.abs(c[i] / a0));
            R = 1 + max;
        }
        double lo = -R, hi = R;
        for (Punto q : puntos) {
            lo = Math.min(lo, q.x);
            hi = Math.max(hi, q.x);
        }
        for (Banda b : bandas) {
            lo = Math.min(lo, Math.min(b.desde, b.hasta));
            hi = Math.max(hi, Math.max(b.desde, b.hasta));
        }
        double margen = (hi - lo) * 0.05;
        xMin = lo - margen;
        xMax = hi + margen;
        xMinInicial = xMin;
        xMaxInicial = xMax;
        dibujar();
    }

    /** Numero sin ceros de mas (3 en vez de 3.000000). */
    public static String num(double v) {
        if (Math.abs(v) < 1e-12) return "0";
        if (Math.abs(v - Math.rint(v)) < 1e-9) return String.valueOf((long) Math.rint(v));
        String s = String.format(Locale.US, "%.6f", v);
        return s.replaceAll("0+$", "").replaceAll("\\.$", "");
    }

    // ---------------- Dibujo ----------------

    private double px(double x) {
        return M + (x - xMin) / (xMax - xMin) * pw;
    }

    private double py(double y) {
        return M + ph - (y - yMin) / (yMax - yMin) * ph;
    }

    /** El eje y se ajusta a la parte visible de la curva, sin dejar que los extremos aplasten las raices. */
    private void ajustarY() {
        int n = 400;
        double[] abs = new double[n + 1];
        int c = 0;
        double mn = Double.POSITIVE_INFINITY, mx = Double.NEGATIVE_INFINITY;
        for (int i = 0; i <= n; i++) {
            double x = xMin + i * (xMax - xMin) / n;
            double y = p.evaluar(x);
            if (Double.isFinite(y)) {
                abs[c++] = Math.abs(y);
                mn = Math.min(mn, y);
                mx = Math.max(mx, y);
            }
        }
        if (c == 0) {
            yMin = -1;
            yMax = 1;
            return;
        }
        double[] orden = Arrays.copyOf(abs, c);
        Arrays.sort(orden);
        double lim = orden[(int) (0.6 * (c - 1))] * 2;
        if (lim < 1e-9) lim = 1;
        yMin = Math.min(Math.max(mn, -lim), 0);
        yMax = Math.max(Math.min(mx, lim), 0);
        double pad = (yMax - yMin) * 0.1;
        if (pad == 0) pad = 1;
        yMin -= pad;
        yMax += pad;
    }

    private void dibujar() {
        try {
            dibujarInterno();
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
    }

    private void dibujarInterno() {
        double w = canvas.getWidth(), h = canvas.getHeight();
        GraphicsContext g = canvas.getGraphicsContext2D();
        g.setFill(Color.WHITE);
        g.fillRect(0, 0, w, h);
        pw = w - 2 * M;
        ph = h - 2 * M;
        if (pw <= 10 || ph <= 10 || xMax - xMin <= 0) return;
        ajustarY();

        g.setFont(Font.font("Consolas", 12));
        g.setLineWidth(1);

        // cuadricula y numeros
        double sx = paso(xMax - xMin), sy = paso(yMax - yMin);
        g.setTextAlign(TextAlignment.CENTER);
        long k0 = (long) Math.ceil(xMin / sx), k1 = (long) Math.floor(xMax / sx);
        for (long k = k0; k <= k1 && k - k0 < 200; k++) {
            double x = k * sx, X = px(x);
            g.setStroke(Color.web("#e3e3e3"));
            g.strokeLine(X, M, X, M + ph);
        }
        g.setTextAlign(TextAlignment.RIGHT);
        long j0 = (long) Math.ceil(yMin / sy), j1 = (long) Math.floor(yMax / sy);
        for (long k = j0; k <= j1 && k - j0 < 200; k++) {
            double y = k * sy, Y = py(y);
            g.setStroke(Color.web("#e3e3e3"));
            g.strokeLine(M, Y, M + pw, Y);
        }
        g.setStroke(Color.GRAY);
        g.strokeRect(M, M, pw, ph);

        // todo lo que sigue se recorta al area del grafico
        g.save();
        g.beginPath();
        g.rect(M, M, pw, ph);
        g.clip();

        // bandas (cotas): un parentesis "(" ")" en cada extremo y un tramo grueso sobre el eje x
        double Yb = Math.max(M + 26, Math.min(M + ph - 26, py(0)));
        for (Banda b : bandas) {
            double x1 = px(Math.min(b.desde, b.hasta)), x2 = px(Math.max(b.desde, b.hasta));
            g.save();
            g.setStroke(b.color);
            g.setLineWidth(4);
            g.setGlobalAlpha(0.45);
            g.strokeLine(x1, Yb, x2, Yb);
            g.setGlobalAlpha(1);
            g.setLineWidth(2.6);
            // "(" en el extremo izquierdo
            g.beginPath();
            g.moveTo(x1 + 6, Yb - 24);
            g.quadraticCurveTo(x1 - 6, Yb, x1 + 6, Yb + 24);
            g.stroke();
            // ")" en el extremo derecho
            g.beginPath();
            g.moveTo(x2 - 6, Yb - 24);
            g.quadraticCurveTo(x2 + 6, Yb, x2 - 6, Yb + 24);
            g.stroke();
            g.restore();
        }

        // ejes
        g.setStroke(Color.BLACK);
        g.setLineWidth(1.8);
        if (yMin <= 0 && yMax >= 0) g.strokeLine(M, py(0), M + pw, py(0));
        if (xMin <= 0 && xMax >= 0) g.strokeLine(px(0), M, px(0), M + ph);

        // curva
        g.setStroke(CURVA);
        g.setLineWidth(2.4);
        g.beginPath();
        boolean empezo = false;
        int pasos = (int) pw;
        for (int i = 0; i <= pasos; i++) {
            double x = xMin + i * (xMax - xMin) / pasos;
            double y = p.evaluar(x);
            if (!Double.isFinite(y)) {
                empezo = false;
                continue;
            }
            double Y = Math.max(-1e6, Math.min(1e6, py(y)));
            if (!empezo) {
                g.moveTo(M + i, Y);
                empezo = true;
            } else {
                g.lineTo(M + i, Y);
            }
        }
        g.stroke();
        g.restore();

        // letras de los ejes
        g.setFill(Color.BLACK);
        g.setTextAlign(TextAlignment.LEFT);
        if (yMin <= 0 && yMax >= 0) g.fillText("x", M + pw + 6, py(0) + 4);
        if (xMin <= 0 && xMax >= 0) g.fillText("y", px(0) - 3, M - 8);

        // numeros y marcas SOBRE los ejes (si un eje no se ve, van en el borde del grafico)
        boolean ejeXvisible = yMin <= 0 && yMax >= 0;
        boolean ejeYvisible = xMin <= 0 && xMax >= 0;
        boolean ambos = ejeXvisible && ejeYvisible;
        double yNumX = ejeXvisible ? Math.min(py(0) + 17, M + ph - 4) : M + ph + 16;
        double xNumY = ejeYvisible ? px(0) - 7 : M - 6;
        g.setFont(Font.font("Consolas", 12));
        g.setTextAlign(TextAlignment.CENTER);
        for (long k = k0; k <= k1 && k - k0 < 200; k++) {
            if (k == 0 && ambos) continue;
            double X = px(k * sx);
            if (ejeXvisible) {
                g.setStroke(Color.BLACK);
                g.setLineWidth(1.5);
                g.strokeLine(X, py(0) - 4, X, py(0) + 4);
            }
            etiqueta(g, num(k * sx), X, yNumX, Color.DIMGRAY);
        }
        g.setTextAlign(TextAlignment.RIGHT);
        for (long k = j0; k <= j1 && k - j0 < 200; k++) {
            if (k == 0 && ambos) continue;
            double Y = py(k * sy);
            if (ejeYvisible) {
                g.setStroke(Color.BLACK);
                g.setLineWidth(1.5);
                g.strokeLine(px(0) - 4, Y, px(0) + 4, Y);
            }
            etiqueta(g, num(k * sy), xNumY, Y + 4, Color.DIMGRAY);
        }
        if (ambos) etiqueta(g, "0", px(0) - 7, py(0) + 17, Color.DIMGRAY);

        // puntos con su etiqueta "(x, 0)"
        List<Punto> orden = new ArrayList<>(puntos);
        orden.sort(Comparator.comparingDouble(q -> q.x));
        double Y0 = py(0);
        int idx = 0;
        for (Punto q : orden) {
            if (q.x < xMin || q.x > xMax) continue;
            double X = px(q.x);
            g.setFill(q.color);
            g.fillOval(X - 5, Y0 - 5, 10, 10);
            g.setStroke(Color.WHITE);
            g.setLineWidth(1.5);
            g.strokeOval(X - 5, Y0 - 5, 10, 10);

            int nivel = idx % 3;                       // escalona las etiquetas para que no se pisen
            double ly = Y0 - 12 - nivel * 17;
            if (ly < M + 12) ly = Y0 + 20 + nivel * 17;
            double ancho = q.texto.length() * 7.3 + 6;
            double lx = Math.max(M + ancho / 2, Math.min(M + pw - ancho / 2, X));
            g.setFill(Color.rgb(255, 255, 255, 0.85));
            g.fillRect(lx - ancho / 2, ly - 12, ancho, 16);
            g.setFill(q.color);
            g.setTextAlign(TextAlignment.CENTER);
            g.fillText(q.texto, lx, ly);
            idx++;
        }

        // titulo, nota y ayuda
        g.setTextAlign(TextAlignment.LEFT);
        g.setFill(Color.BLACK);
        g.setFont(Font.font("Consolas", FontWeight.BOLD, 15));
        g.fillText(titulo, M, 24);
        g.setFont(Font.font("Consolas", 12));
        if (!nota.isEmpty()) {
            g.setFill(Color.web("#c0392b"));
            g.fillText(nota, M, 42);
        }
        g.setFill(Color.GRAY);
        g.fillText("Rueda: zoom   \u00b7   Arrastrar: mover   \u00b7   Doble clic: restablecer", M, h - 8);
    }

    /** Texto con un borde blanco para que se lea aunque pase la curva o la cuadricula por detras. */
    private static void etiqueta(GraphicsContext g, String t, double x, double y, Color c) {
        g.setStroke(Color.WHITE);
        g.setLineWidth(3.5);
        g.strokeText(t, x, y);
        g.setFill(c);
        g.fillText(t, x, y);
    }

    /** Paso "lindo" para la cuadricula (1, 2, 5, 10...). */
    private static double paso(double rango) {
        double raw = rango / 8;
        double mag = Math.pow(10, Math.floor(Math.log10(raw)));
        double f = raw / mag;
        double nice = f < 1.5 ? 1 : f < 3 ? 2 : f < 7 ? 5 : 10;
        return nice * mag;
    }
}