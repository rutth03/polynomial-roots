package gui;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import logic.AnalisisPolinomio;
import logic.BairstowSolver;
import logic.BairstowSolver.RaizCompleja;
import logic.CotasDePolinomio;
import logic.OperacionesPolinomios;
import logic.OperacionesPolinomios.ResultadoHornerDoble;
import logic.Parser;
import model.Polinomio;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

public class MainFrame extends Application {

    // Paleta de colores
    private static final String COLOR_ENCABEZADO = "#203B67";
    private static final String COLOR_BOTONES    = "#2865c5";
    private static final String COLOR_FONDO      = "#F1F4FA";
    private static final String COLOR_CAMPOS     = "#C9D8F3";

    private static final String ESTILO_CAMPO =
            "-fx-font-size: 18px; -fx-background-color: " + COLOR_CAMPOS + ";";
    private static final String ESTILO_BOTON =
            "-fx-font-size: 16px; -fx-background-color: " + COLOR_BOTONES
            + "; -fx-text-fill: white; -fx-background-radius: 6;";

    private static final String ESTILO_VENTANA_BTN =
            "-fx-background-color: transparent; -fx-text-fill: white; -fx-font-size: 16px;"
            + " -fx-background-radius: 0;";
    private static final String ESTILO_VENTANA_BTN_HOVER =
            "-fx-background-color: #345a96; -fx-text-fill: white; -fx-font-size: 16px;"
            + " -fx-background-radius: 0;";
    private static final String ESTILO_CERRAR_HOVER =
            "-fx-background-color: #e81123; -fx-text-fill: white; -fx-font-size: 16px;"
            + " -fx-background-radius: 0;";

    private static final String FUENTE_MONO =
            "-fx-font-family: 'Consolas', 'Monospaced'; -fx-font-size: 18px;";

    // Metodos disponibles
    private static final String M_HORNER = "Horner / Ruffini (divisi\u00f3n)";
    private static final String M_NEWTON = "Newton";
    private static final String M_HALLEY = "Halley";
    private static final String M_RAICES = "Ra\u00edces enteras y racionales";
    private static final String M_BAIRSTOW = "Bairstow";
    private static final String M_COTAS = "Cotas de las ra\u00edces";

    private ComboBox<String> metodo;  // metodo elegido
    private TextField campo;        // P(x)
    private TextField campoQ;       // Q(x), divisor
    private Label etiquetaQ;        // etiqueta "Q(x) ="
    private TextField campoX0;      // Newton: valor inicial
    private TextField campoEps;     // Newton: tolerancia
    private TextField campoIter;    // Newton: maximo de iteraciones
    private HBox parametrosNewton;  // fila con x0 (o r0 y s0), epsilon, maxIter
    private TextField campoR0;      // Bairstow: r inicial
    private TextField campoS0;      // Bairstow: s inicial
    private HBox grupoX0;           // x0 (Newton / Halley)
    private HBox grupoRS;           // r0 y s0 (Bairstow)
    private Label etiquetaCota;     // etiqueta "Cota por:"
    private ComboBox<String> metodoCota; // Lagrange / Laguerre / Newton (solo en Cotas)
    private TextField activo;       // campo donde escriben los botones
    private VBox panelResultado;    // contenido del resultado
    private Label error;
    private Button botonGraficar;  // se habilita despues de calcular

    // Para arrastrar y maximizar la ventana sin decoracion
    private double offsetX, offsetY;
    private boolean maximizada = false;
    private double prevX, prevY, prevAncho, prevAlto;

    @Override
    public void start(Stage stage) {
        stage.initStyle(StageStyle.UNDECORATED);

        Rectangle2D pantalla = Screen.getPrimary().getVisualBounds();
        double ancho = pantalla.getWidth() * 0.85;
        double alto = pantalla.getHeight() * 0.85;

        // ---------- Barra de titulo (azul marino) ----------
        Region espacio = new Region();
        HBox.setHgrow(espacio, Priority.ALWAYS);

        Button minimizar = botonVentana("\u2014", false);
        minimizar.setOnAction(e -> stage.setIconified(true));

        Button maximizar = botonVentana("\u25a1", false);
        maximizar.setOnAction(e -> alternarMaximizar(stage, pantalla));

        Button cerrar = botonVentana("\u2715", true);
        cerrar.setOnAction(e -> stage.close());

        HBox barra = new HBox(espacio, minimizar, maximizar, cerrar);
        barra.setAlignment(Pos.CENTER_RIGHT);
        barra.setStyle("-fx-background-color: " + COLOR_ENCABEZADO + ";");
        barra.setMinHeight(34);
        barra.setPrefHeight(34);

        barra.setOnMousePressed(e -> {
            offsetX = e.getSceneX();
            offsetY = e.getSceneY();
        });
        barra.setOnMouseDragged(e -> {
            if (!maximizada) {
                stage.setX(e.getScreenX() - offsetX);
                stage.setY(e.getScreenY() - offsetY);
            }
        });
        barra.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) alternarMaximizar(stage, pantalla);
        });

        // ---------- Titulo centrado (fondo = fondo principal) ----------
        Label titulo = new Label("Ra\u00edces de Polinomios");
        titulo.setStyle("-fx-font-size: 26px; -fx-font-weight: bold; -fx-text-fill: "
                + COLOR_ENCABEZADO + ";");
        HBox zonaTitulo = new HBox(titulo);
        zonaTitulo.setAlignment(Pos.CENTER);
        zonaTitulo.setPadding(new Insets(6, 20, 0, 20));
        zonaTitulo.setStyle("-fx-background-color: " + COLOR_FONDO + ";");

        // ---------- Selector de metodo ----------
        Label etiquetaMetodo = new Label("Operaci\u00f3n:");
        etiquetaMetodo.setStyle("-fx-font-size: 18px;");
        metodo = new ComboBox<>();
        metodo.getItems().addAll(M_HORNER, M_NEWTON, M_HALLEY, M_BAIRSTOW, M_RAICES, M_COTAS);
        metodo.setValue(M_HORNER);
        metodo.setStyle("-fx-font-size: 16px;");
        etiquetaCota = new Label("Cota por:");
        etiquetaCota.setStyle("-fx-font-size: 18px;");
        metodoCota = new ComboBox<>();
        metodoCota.getItems().addAll("Lagrange", "Laguerre", "Newton");
        metodoCota.setValue("Lagrange");
        metodoCota.setStyle("-fx-font-size: 16px;");
        HBox filaMetodo = new HBox(10, etiquetaMetodo, metodo, etiquetaCota, metodoCota);
        filaMetodo.setAlignment(Pos.CENTER_LEFT);

        // ---------- Entradas: P(x) y Q(x) ----------
        campo = new TextField();
        campo.setPromptText("Escribe un polinomio, por ejemplo (x+1)^2 + sen(2)x");
        campo.setStyle(ESTILO_CAMPO);

        campoQ = new TextField();
        campoQ.setPromptText("Divisor, por ejemplo x-2  (Horner / Ruffini)");
        campoQ.setStyle(ESTILO_CAMPO);

        etiquetaQ = new Label("Q(x) =");
        etiquetaQ.setStyle("-fx-font-size: 18px;");

        // ---------- Parametros de Newton ----------
        campoX0 = new TextField("1");
        campoX0.setPromptText("x0");
        campoX0.setStyle(ESTILO_CAMPO);
        campoX0.setPrefColumnCount(8);

        campoR0 = new TextField("-1");
        campoR0.setPromptText("r0");
        campoR0.setStyle(ESTILO_CAMPO);
        campoR0.setPrefColumnCount(6);

        campoS0 = new TextField("-1");
        campoS0.setPromptText("s0");
        campoS0.setStyle(ESTILO_CAMPO);
        campoS0.setPrefColumnCount(6);

        campoEps = new TextField("0.000001");
        campoEps.setPromptText("epsilon");
        campoEps.setStyle(ESTILO_CAMPO);
        campoEps.setPrefColumnCount(10);

        campoIter = new TextField("50");
        campoIter.setPromptText("m\u00e1x. iteraciones");
        campoIter.setStyle(ESTILO_CAMPO);
        campoIter.setPrefColumnCount(5);


        Label lx0 = new Label("x\u2080 =");
        Label leps = new Label("\u03b5 =");
        Label liter = new Label("M\u00e1x. iter. =");
        lx0.setStyle("-fx-font-size: 18px;");
        leps.setStyle("-fx-font-size: 18px;");
        liter.setStyle("-fx-font-size: 18px;");
        Label lr0 = new Label("r\u2080 =");
        Label ls0 = new Label("s\u2080 =");
        lr0.setStyle("-fx-font-size: 18px;");
        ls0.setStyle("-fx-font-size: 18px;");
        grupoX0 = new HBox(10, lx0, campoX0);
        grupoX0.setAlignment(Pos.CENTER_LEFT);
        grupoRS = new HBox(10, lr0, campoR0, ls0, campoS0);
        grupoRS.setAlignment(Pos.CENTER_LEFT);
        parametrosNewton = new HBox(10, grupoX0, grupoRS, leps, campoEps, liter, campoIter);
        parametrosNewton.setAlignment(Pos.CENTER_LEFT);

        // Enter (la tecla) en cualquier campo ejecuta el calculo
        campo.setOnAction(e -> calcular());
        campoQ.setOnAction(e -> calcular());
        campoX0.setOnAction(e -> calcular());
        campoR0.setOnAction(e -> calcular());
        campoS0.setOnAction(e -> calcular());
        campoEps.setOnAction(e -> calcular());
        campoIter.setOnAction(e -> calcular());

        // Los botones escriben en el ultimo campo en el que estuvo el cursor
        activo = campo;
        campo.focusedProperty().addListener((o, a, enfocado) -> {
            if (enfocado) activo = campo;
        });
        campoQ.focusedProperty().addListener((o, a, enfocado) -> {
            if (enfocado) activo = campoQ;
        });
        campoX0.focusedProperty().addListener((o, a, enfocado) -> {
            if (enfocado) activo = campoX0;
        });
        campoR0.focusedProperty().addListener((o, a, enfocado) -> {
            if (enfocado) activo = campoR0;
        });
        campoS0.focusedProperty().addListener((o, a, enfocado) -> {
            if (enfocado) activo = campoS0;
        });

        Label etiquetaP = new Label("P(x) =");
        etiquetaP.setStyle("-fx-font-size: 18px;");

        GridPane formulario = new GridPane();
        formulario.setHgap(10);
        formulario.setVgap(10);
        formulario.add(etiquetaP, 0, 0);
        formulario.add(campo, 1, 0);
        formulario.add(etiquetaQ, 0, 1);
        formulario.add(campoQ, 1, 1);
        formulario.add(parametrosNewton, 0, 2, 2, 1);
        GridPane.setHgrow(campo, Priority.ALWAYS);
        GridPane.setHgrow(campoQ, Priority.ALWAYS);

        // ---------- Botones ----------
        GridPane teclado = new GridPane();
        teclado.setHgap(6);
        teclado.setVgap(6);
        teclado.setPadding(new Insets(8));
        teclado.setStyle("-fx-background-color: " + COLOR_CAMPOS + "; -fx-background-radius: 8;");

        // Fila 1: sen cos tg pi  x ^ ( )  Borrar
        teclado.add(boton("sen()", () -> insertarFuncion("sen")), 0, 0);
        teclado.add(boton("cos()", () -> insertarFuncion("cos")), 1, 0);
        teclado.add(boton("tg()", () -> insertarFuncion("tg")), 2, 0);
        teclado.add(boton("\u03c0", () -> insertar("\u03c0")), 3, 0);
        teclado.add(boton("x", () -> insertar("x")), 4, 0);
        teclado.add(boton("^", () -> insertar("^")), 5, 0);
        teclado.add(boton("(", () -> insertar("(")), 6, 0);
        teclado.add(boton(")", () -> insertar(")")), 7, 0);
        Button borrar = boton("Borrar", () -> {
            activo.clear();
            activo.requestFocus();
        });
        teclado.add(borrar, 8, 0);

        // Fila 2: ln log raiz e  + - * /  punto
        teclado.add(boton("ln()", () -> insertarFuncion("ln")), 0, 1);
        teclado.add(boton("log()", () -> insertarFuncion("log")), 1, 1);
        teclado.add(boton("raiz()", () -> insertarFuncion("raiz")), 2, 1);
        teclado.add(boton("e", () -> insertar("e")), 3, 1);
        teclado.add(boton("+", () -> insertar("+")), 4, 1);
        teclado.add(boton("-", () -> insertar("-")), 5, 1);
        teclado.add(boton("*", () -> insertar("*")), 6, 1);
        teclado.add(boton("/", () -> insertar("/")), 7, 1);
        teclado.add(boton(".", () -> insertar(".")), 8, 1);

        // ---------- Error ----------
        error = new Label();
        error.managedProperty().bind(error.textProperty().isNotEmpty());   // sin texto no ocupa lugar
        error.setStyle("-fx-text-fill: #c0392b; -fx-font-size: 14px;");

        // ---------- Resultado (panel con scroll) ----------
        panelResultado = new VBox(10);
        panelResultado.setPadding(new Insets(14));
        panelResultado.setStyle("-fx-background-color: " + COLOR_CAMPOS + ";");

        ScrollPane scroll = new ScrollPane(panelResultado);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background: " + COLOR_CAMPOS + "; -fx-background-color: "
                + COLOR_CAMPOS + "; -fx-background-radius: 8;");
        scroll.setMinHeight(220);
        VBox.setVgrow(scroll, Priority.ALWAYS);

        Button enter = boton("Enter", this::calcular);
        enter.setMinWidth(140);
        enter.setStyle(ESTILO_BOTON + " -fx-font-weight: bold;");
        Button graficar = boton("Graficar", this::graficar);
        graficar.setMinWidth(140);
        graficar.setStyle(ESTILO_BOTON + " -fx-font-weight: bold;");
        botonGraficar = graficar;
        graficar.setDisable(true);   // se habilita solo despues de calcular con Enter
        for (TextField tf : new TextField[]{campo, campoQ, campoX0, campoEps, campoIter, campoR0, campoS0})
            tf.textProperty().addListener((o, a, b) -> graficar.setDisable(true));
        metodo.valueProperty().addListener((o, a, b) -> graficar.setDisable(true));
        metodoCota.valueProperty().addListener((o, a, b) -> graficar.setDisable(true));
        Region separadorEnter = new Region();
        HBox.setHgrow(separadorEnter, Priority.ALWAYS);
        HBox filaEnter = new HBox(enter, separadorEnter, graficar);
        filaEnter.setAlignment(Pos.CENTER_LEFT);

        VBox contenido = new VBox(8,
                filaMetodo,
                formulario,
                teclado,
                filaEnter,
                error,
                scroll);
        contenido.setPadding(new Insets(4, 20, 14, 20));
        VBox.setVgrow(contenido, Priority.ALWAYS);

        // Cambio de metodo: muestra/oculta los campos que corresponden
        metodo.valueProperty().addListener((o, a, b) -> {
            actualizarModo();
            error.setText("");
            panelResultado.getChildren().clear();
        });
        actualizarModo();

        VBox raiz = new VBox(barra, zonaTitulo, contenido);
        raiz.setStyle("-fx-background-color: " + COLOR_FONDO + ";");

        stage.setTitle("Ra\u00edces de Polinomios");
        stage.setScene(new Scene(raiz, ancho, alto));
        stage.setX(pantalla.getMinX() + (pantalla.getWidth() - ancho) / 2);
        stage.setY(pantalla.getMinY() + (pantalla.getHeight() - alto) / 2);
        stage.show();
        campo.requestFocus();
    }

    private boolean esNewton() {
        return M_NEWTON.equals(metodo.getValue());
    }

    private boolean esRaices() {
        return M_RAICES.equals(metodo.getValue());
    }

    private boolean esBairstow() {
        return M_BAIRSTOW.equals(metodo.getValue());
    }

    private boolean esCotas() {
        return M_COTAS.equals(metodo.getValue());
    }

    private boolean esHalley() {
        return M_HALLEY.equals(metodo.getValue());
    }

    /** Newton y Halley comparten parametros (x0, epsilon, iteraciones) y tabla. */
    private boolean esIterativo() {
        return esNewton() || esHalley();
    }

    /** Muestra Q(x) para Horner/Ruffini o los parametros para Newton. */
    private void actualizarModo() {
        boolean horner = M_HORNER.equals(metodo.getValue());
        boolean iterativo = esIterativo();
        etiquetaQ.setVisible(horner);
        etiquetaQ.setManaged(horner);
        campoQ.setVisible(horner);
        campoQ.setManaged(horner);
        boolean bairstow = esBairstow();
        parametrosNewton.setVisible(iterativo || bairstow);
        parametrosNewton.setManaged(iterativo || bairstow);
        grupoX0.setVisible(iterativo);
        grupoX0.setManaged(iterativo);
        grupoRS.setVisible(bairstow);
        grupoRS.setManaged(bairstow);
        boolean cotas = esCotas();
        etiquetaCota.setVisible(cotas);
        etiquetaCota.setManaged(cotas);
        metodoCota.setVisible(cotas);
        metodoCota.setManaged(cotas);
        if (!horner && activo == campoQ) {
            activo = campo;
        }
    }

    /** Boton de la barra de titulo (minimizar, maximizar, cerrar). */
    private Button botonVentana(String texto, boolean esCerrar) {
        Button b = new Button(texto);
        b.setStyle(ESTILO_VENTANA_BTN);
        b.setPrefSize(46, 34);
        b.setMinSize(46, 34);
        b.setFocusTraversable(false);
        b.setOnMouseEntered(e -> b.setStyle(esCerrar ? ESTILO_CERRAR_HOVER : ESTILO_VENTANA_BTN_HOVER));
        b.setOnMouseExited(e -> b.setStyle(ESTILO_VENTANA_BTN));
        return b;
    }

    /** Maximiza o restaura la ventana (respeta la barra de tareas). */
    private void alternarMaximizar(Stage stage, Rectangle2D pantalla) {
        if (!maximizada) {
            prevX = stage.getX();
            prevY = stage.getY();
            prevAncho = stage.getWidth();
            prevAlto = stage.getHeight();
            stage.setX(pantalla.getMinX());
            stage.setY(pantalla.getMinY());
            stage.setWidth(pantalla.getWidth());
            stage.setHeight(pantalla.getHeight());
            maximizada = true;
        } else {
            stage.setX(prevX);
            stage.setY(prevY);
            stage.setWidth(prevAncho);
            stage.setHeight(prevAlto);
            maximizada = false;
        }
    }

    private Button boton(String texto, Runnable accion) {
        Button b = new Button(texto);
        b.setStyle(ESTILO_BOTON);
        b.setMinWidth(90);
        b.setFocusTraversable(false); // el cursor se queda en el campo de texto
        b.setOnAction(e -> accion.run());
        return b;
    }

    /** Inserta texto en la posicion del cursor del campo activo. */
    private void insertar(String texto) {
        activo.insertText(activo.getCaretPosition(), texto);
        activo.requestFocus();
    }

    /** Inserta nombre() y deja el cursor entre los parentesis. */
    private void insertarFuncion(String nombre) {
        insertar(nombre + "()");
        activo.positionCaret(activo.getCaretPosition() - 1);
    }

    // ------------------------------------------------------------------
    // Calculo
    // ------------------------------------------------------------------

    /** Se ejecuta con el boton Enter (o la tecla Enter en un campo). Muestra un error si falta algo. */
    private void calcular() {
        botonGraficar.setDisable(true);
        error.setText("");
        panelResultado.getChildren().clear();

        try {
            String texto = campo.getText().trim();
            if (texto.isEmpty())
                throw new IllegalArgumentException("Falta P(x): escrib\u00ed un polinomio.");
            Polinomio p = parsear(texto, "P(x)"); // valida P(x)

            if (esIterativo()) {
                metodoIterativo(p, esHalley());
            } else if (esRaices()) {
                raicesPolinomio(p);
            } else if (esBairstow()) {
                metodoBairstow(p);
            } else if (esCotas()) {
                metodoCotas(p);
            } else {
                String textoQ = campoQ.getText().trim();
                if (textoQ.isEmpty())
                    throw new IllegalArgumentException("Falta Q(x): escrib\u00ed el divisor.");
                Polinomio q = parsear(textoQ, "Q(x)");
                dividirPolinomios(p, q);
            }
            botonGraficar.setDisable(false);   // calculo OK: ya se puede graficar
        } catch (RuntimeException ex) {
            panelResultado.getChildren().clear();
            String msg = ex.getMessage() != null ? ex.getMessage() : ex.toString();
            error.setText("Error: " + msg);
        }
    }

    // ------------------------------------------------------------------
    // Grafico (boton "Graficar"): abre una ventana nueva segun el metodo elegido
    // ------------------------------------------------------------------

    private void graficar() {
        error.setText("");
        try {
            String textoP = campo.getText().trim();
            if (textoP.isEmpty())
                throw new IllegalArgumentException("Falta P(x): escrib\u00ed un polinomio.");
            Polinomio p = parsear(textoP, "P(x)");
            if (esConstante(p))
                throw new ArithmeticException("P(x) es constante: no hay nada que graficar");

            GraficoPolinomio g = new GraficoPolinomio(p, "P(x) = " + texto(p));

            if (esIterativo()) {
                // Newton / Halley: marca la raiz a la que se aproxima
                boolean halley = esHalley();
                if (campoX0.getText().trim().isEmpty())
                    throw new IllegalArgumentException("Falta x0: escrib\u00ed el valor inicial.");
                double x0 = parsearNumero(campoX0.getText().trim(), "x0");
                double eps = leerEpsilon();
                int maxIter = leerMaxIter();
                double raiz = aproximarRaiz(p, x0, eps, maxIter, halley);
                if (Double.isNaN(raiz)) {
                    g.setNota("No convergi\u00f3: no hay ra\u00edz que marcar. Prob\u00e1 con otro x0.");
                } else {
                    g.agregarPunto(raiz, "(" + GraficoPolinomio.num(raiz) + ", 0)", GraficoPolinomio.ROJO);
                }
            } else if (esRaices()) {
                // Enteras (rojo) y racionales (naranja)
                AnalisisPolinomio analisis = new AnalisisPolinomio();
                int cantidad = 0;
                for (int e : analisis.raicesEnteras(p)) {
                    g.agregarPunto(e, "(" + e + ", 0)", GraficoPolinomio.ROJO);
                    cantidad++;
                }
                for (double x : analisis.raicesRacionales(p)) {
                    g.agregarPunto(x, "(" + analisis.aFraccion(x) + ", 0)", GraficoPolinomio.NARANJA);
                    cantidad++;
                }
                if (cantidad == 0) g.setNota("P(x) no tiene ra\u00edces enteras ni racionales.");
            } else if (esCotas()) {
                // Cotas: marca los extremos con (x, 0) y sombrea los intervalos
                String nombre = metodoCota.getValue();
                if (nombre == null)
                    throw new IllegalArgumentException("Falta elegir el m\u00e9todo de cota (Lagrange, Laguerre o Newton).");
                int num = numeroMetodoCota(nombre);
                CotasDePolinomio cotas = new CotasDePolinomio();
                double supPos = cotas.cotaSuperiorPositiva(p, num);
                double infPos = cotas.cotaInferiorPositiva(p, num);
                double infNeg = cotas.cotaInferiorNegativa(p, num);
                double supNeg = cotas.cotaSuperiorNegativa(p, num);
                String nota = "";
                if (supPos != 0) {
                    g.agregarBanda(infPos, supPos, GraficoPolinomio.VERDE);
                    g.agregarPunto(infPos, "(" + GraficoPolinomio.num(infPos) + ", 0)", GraficoPolinomio.VERDE);
                    g.agregarPunto(supPos, "(" + GraficoPolinomio.num(supPos) + ", 0)", GraficoPolinomio.VERDE);
                } else {
                    nota += "No hay ra\u00edces positivas.  ";
                }
                if (infNeg != 0) {
                    g.agregarBanda(infNeg, supNeg, GraficoPolinomio.ROJO);
                    g.agregarPunto(infNeg, "(" + GraficoPolinomio.num(infNeg) + ", 0)", GraficoPolinomio.ROJO);
                    g.agregarPunto(supNeg, "(" + GraficoPolinomio.num(supNeg) + ", 0)", GraficoPolinomio.ROJO);
                } else {
                    nota += "No hay ra\u00edces negativas.";
                }
                g.setNota(nota.trim());
            } else if (esBairstow()) {
                // Bairstow: marca las raices reales y avisa de las complejas (no se pueden dibujar en el eje x)
                if (campoR0.getText().trim().isEmpty())
                    throw new IllegalArgumentException("Falta r0: escrib\u00ed el valor inicial de r.");
                if (campoS0.getText().trim().isEmpty())
                    throw new IllegalArgumentException("Falta s0: escrib\u00ed el valor inicial de s.");
                final double r0 = parsearNumero(campoR0.getText().trim(), "r0");
                final double s0 = parsearNumero(campoS0.getText().trim(), "s0");
                final double epsB = leerEpsilon();
                final int maxIterB = leerMaxIter();
                final AtomicReference<List<RaizCompleja>> salida = new AtomicReference<>();
                final AtomicReference<Throwable> falla = new AtomicReference<>();
                Thread hilo = new Thread(() -> {
                    try {
                        salida.set(BairstowSolver.bairstow(p, r0, s0, epsB, maxIterB));
                    } catch (Throwable t) {
                        falla.set(t);
                    }
                });
                hilo.setDaemon(true);
                hilo.start();
                try {
                    hilo.join(2000);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }
                if (hilo.isAlive() || falla.get() != null || salida.get() == null) {
                    g.setNota("Bairstow no termin\u00f3 o no convergi\u00f3: no hay ra\u00edces que marcar.");
                } else {
                    List<Double> marcadas = new ArrayList<>();
                    StringBuilder complejas = new StringBuilder();
                    for (RaizCompleja z : salida.get()) {
                        if (Math.abs(z.getImaginaria()) < 1e-9) {
                            boolean repetida = false;
                            for (double m : marcadas) if (Math.abs(m - z.getReal()) < 1e-6) repetida = true;
                            if (!repetida) {
                                marcadas.add(z.getReal());
                                g.agregarPunto(z.getReal(), "(" + GraficoPolinomio.num(z.getReal()) + ", 0)",
                                        GraficoPolinomio.ROJO);
                            }
                        } else if (z.getImaginaria() > 0) {
                            if (complejas.length() > 0) complejas.append("   ");
                            complejas.append(GraficoPolinomio.num(z.getReal())).append(" \u00b1 ")
                                     .append(GraficoPolinomio.num(z.getImaginaria())).append("i");
                        }
                    }
                    String nota = marcadas.isEmpty() ? "No hay ra\u00edces reales." : "";
                    if (complejas.length() > 0)
                        nota += (nota.isEmpty() ? "" : "  ") + "Complejas (no se dibujan): " + complejas;
                    g.setNota(nota);
                }
            }
            // Horner / Ruffini: solo la curva

            g.ajustarRango();
            Stage ventana = new Stage();
            ventana.setTitle("Gr\u00e1fico \u2013 " + metodo.getValue());
            ventana.setScene(new Scene(g, 900, 600));
            ventana.show();
            ventana.toFront();
            ventana.requestFocus();
            error.setText("");
        } catch (Throwable ex) {
            ex.printStackTrace();
            String msg = ex.getMessage() != null ? ex.getMessage() : ex.toString();
            error.setText("Error al graficar: " + msg);
        }
    }

    /** Mismo bucle que la tabla de metodoIterativo; devuelve la raiz o NaN si no converge. */
    private static double aproximarRaiz(Polinomio p, double x0, double eps, int maxIter, boolean halley) {
        Polinomio segunda = p.derivar().derivar();
        double xi = x0;
        double anterior = x0;
        for (int i = 0; i <= maxIter; i++) {
            if (!Double.isFinite(xi)) return Double.NaN;
            ResultadoHornerDoble r = OperacionesPolinomios.hornerDoble(p, xi);
            double valor = r.getValor();
            double deriv = r.getDerivada();
            if (i > 0 && Math.abs(xi - anterior) < eps) return xi;
            if (valor == 0) return xi;
            if (i == maxIter) return Double.NaN;
            double siguiente;
            if (halley) {
                double den = 2 * deriv * deriv - valor * segunda.evaluar(xi);
                if (Math.abs(den) < 1e-14) return Double.NaN;
                siguiente = xi - 2 * valor * deriv / den;
            } else {
                if (Math.abs(deriv) < 1e-14) return Double.NaN;
                siguiente = xi - valor / deriv;
            }
            anterior = xi;
            xi = siguiente;
        }
        return Double.NaN;
    }

    /** Texto -> model.Polinomio. Parser se usa solo como parser (y ya devuelve el polinomio normalizado). */
    private Polinomio parsear(String texto, String nombre) {
        try {
            return new Polinomio(Parser.parse(texto).getCoeficientes());
        } catch (RuntimeException ex) {
            throw new IllegalArgumentException(nombre + ": " + ex.getMessage());
        }
    }

    /** Texto -> numero real (acepta pi, sen(1), 1/2, ...). Para x0. */
    private double parsearNumero(String texto, String nombre) {
        try {
            return Parser.parse(texto).valorConstante();
        } catch (RuntimeException ex) {
            throw new IllegalArgumentException(nombre + ": " + ex.getMessage());
        }
    }

    // ------------------------------------------------------------------
    // Presentacion de model.Polinomio
    // ------------------------------------------------------------------

    /**
     * Texto prolijo de un Polinomio. Polinomio.toString() no quita ceros a la izquierda
     * (ej. un resto {0, 0} sale como "0x") ni redondea ruido numerico, asi que se
     * pasa por Parser solo para mostrarlo.
     */
    private static String texto(Polinomio p) {
        return new Parser(p.getCoeficientes()).toString();
    }

    private static boolean esConstante(Polinomio p) {
        return p.getGrado() == 0;
    }

    // ------------------------------------------------------------------
    // Raices enteras y racionales (Teorema de la raiz racional)
    // ------------------------------------------------------------------

    /**
     * Raices enteras y racionales de P(x):
     *  1) Enteras: a0, D = divisores de a0, evaluacion por Horner y polinomio factorizado.
     *  2) Racionales: a0 y an, D(a0), D(an), combinaciones p/q, solo las no enteras,
     *     evaluacion por Horner y esquema de la factorizacion (sin calcularla).
     * Todo se hace con enteros exactos (BigInteger), sin errores de redondeo.
     */
    private void raicesPolinomio(Polinomio p) {
        if (esConstante(p))
            throw new ArithmeticException("P(x) es constante: no tiene ra\u00edces");

        // Coeficientes enteros (si hace falta se multiplica por k; las raices no cambian)
        double[] desc = p.getCoeficientes();
        long k = escalaEntera(desc);
        BigInteger[] a = new BigInteger[desc.length];
        for (int i = 0; i < desc.length; i++)
            a[i] = BigInteger.valueOf(Math.round(desc[i] * k));

        // Si a0 = 0, x = 0 es raiz: se saca el factor x^m
        int m = 0;
        while (a[a.length - 1 - m].signum() == 0) m++;
        BigInteger[] r = Arrays.copyOf(a, a.length - m);   // polinomio sin el factor x^m
        boolean hayMas = r.length > 1;                      // todavia tiene x

        // ---------------- Raices enteras ----------------
        panelResultado.getChildren().add(tituloSeccion("Ra\u00edces enteras de un Polinomio"));
        panelResultado.getChildren().add(textoMono("P(x) = " + texto(p)));
        if (k != 1)
            panelResultado.getChildren().add(textoMono(
                    "Con coeficientes enteros: " + k + " \u00b7 P(x) = " + aPolinomio(a)));
        if (m > 0)
            panelResultado.getChildren().add(textoMono(
                    "El t\u00e9rmino independiente es 0: x = 0 es ra\u00edz (multiplicidad " + m
                    + "). Se saca factor x" + (m > 1 ? "^" + m : "") + "."));

        List<BigInteger> raices = new ArrayList<>();
        List<Long> divA0 = new ArrayList<>();
        BigInteger a0 = r[r.length - 1];
        BigInteger an = r[0];

        if (hayMas) {
            divA0 = divisores(a0);
            panelResultado.getChildren().add(filaSub("a_{0} =", a0.toString()));
            panelResultado.getChildren().add(filaSub("D =", llaveConSignos(divA0)));

            panelResultado.getChildren().add(negrita("Evaluando por Horner"));
            FlowPane evaluaciones = new FlowPane(30, 6);
            for (long d : divA0) {
                for (int signo : new int[]{1, -1}) {
                    BigInteger x = BigInteger.valueOf(signo * d);
                    BigInteger[] b = horner(r, x);                 // el ultimo es P(x)
                    boolean esRaiz = b[b.length - 1].signum() == 0;
                    Label l = textoCelda("P(" + x + ") " + (esRaiz ? "=" : "\u2260") + " 0");
                    if (esRaiz) {
                        l.setStyle(FUENTE_MONO + "-fx-font-weight: bold;");
                    }
                    evaluaciones.getChildren().add(l);
                }
            }
            panelResultado.getChildren().add(evaluaciones);
        }

        // Raices enteras: las calcula AnalisisPolinomio (el 0 ya se trato arriba como factor x^m)
        AnalisisPolinomio analisis = new AnalisisPolinomio();
        for (int e : analisis.raicesEnteras(p)) {
            if (e != 0) raices.add(BigInteger.valueOf(e));
        }

        // Polinomio factorizado (dividiendo por Horner las veces que sea raiz)
        List<String> factores = new ArrayList<>();
        if (m > 0) factores.add(m == 1 ? "x" : "x^{" + m + "}");
        BigInteger[] q = r;
        for (BigInteger x : raices) {
            int mult = 0;
            while (q.length > 1) {
                BigInteger[] b = horner(q, x);
                if (b[b.length - 1].signum() != 0) break;
                q = Arrays.copyOf(b, b.length - 1);
                mult++;
            }
            if (mult > 0) {
                String f = "(x " + (x.signum() < 0 ? "+ " + x.negate() : "- " + x) + ")";
                factores.add(mult == 1 ? f : f + "^{" + mult + "}");
            }
        }
        if (factores.isEmpty()) {
            panelResultado.getChildren().add(textoMono("P(x) no tiene ra\u00edces enteras."));
        } else {
            String prefijo = "";
            String cola = "";
            if (q.length == 1) {
                BigInteger c = q[0];
                if (c.equals(BigInteger.ONE)) prefijo = "";
                else if (c.equals(BigInteger.ONE.negate())) prefijo = "-";
                else prefijo = c + " ";
            } else {
                cola = " (" + aPolinomio(q) + ")";
            }
            panelResultado.getChildren().add(
                    formula("P(x) = " + prefijo + String.join(" ", factores) + cola, 20));
        }

        if (!hayMas) return;   // nada mas que analizar

        // ---------------- Raices racionales ----------------
        panelResultado.getChildren().add(new Separator());
        panelResultado.getChildren().add(tituloSeccion("Ra\u00edces Racionales de un Polinomio"));
        panelResultado.getChildren().add(filaSub("a_{0} =", a0.toString()));
        panelResultado.getChildren().add(filaSub("a_{n} =", an.toString()));

        List<Long> divP = divA0;
        List<Long> divQ = divisores(an);
        panelResultado.getChildren().add(filaSub("D_{a0} =", llaveConSignos(divP)));
        panelResultado.getChildren().add(filaSub("D_{an} =", llaveConSignos(divQ)));

        // Todas las combinaciones p/q (reducidas y sin repetir)
        List<long[]> candidatos = new ArrayList<>();
        Set<String> vistos = new HashSet<>();
        for (long den : divQ) {
            for (long num : divP) {
                long g = mcd(num, den);
                long n = num / g;
                long d = den / g;
                for (int signo : new int[]{1, -1}) {
                    if (vistos.add((signo * n) + "/" + d)) candidatos.add(new long[]{signo * n, d});
                }
            }
        }
        List<String> todos = new ArrayList<>();
        List<long[]> noEnteros = new ArrayList<>();
        for (long[] c : candidatos) {
            todos.add(fraccion(c));
            if (c[1] != 1) noEnteros.add(c);
        }
        panelResultado.getChildren().add(filaSub("D =", "{" + String.join(", ", todos) + "}"));

        // Raices racionales (no enteras): las calcula AnalisisPolinomio
        List<String> encontradas = new ArrayList<>();
        List<Double> racionales = new ArrayList<>();
        for (double x : analisis.raicesRacionales(p)) {
            encontradas.add(analisis.aFraccion(x));
            racionales.add(x);
        }

        if (noEnteros.isEmpty()) {
            panelResultado.getChildren().add(textoMono("P(x) no tiene ra\u00edces racionales."));
            return;
        }

        panelResultado.getChildren().add(negrita("Analizando por Horner"));
        final int MAX_MOSTRAR = 300;
        FlowPane evaluaciones = new FlowPane(30, 6);
        int mostrados = 0;
        for (long[] c : noEnteros) {
            boolean esRaiz = esRaizRacional(r, c[0], c[1]);
            if (mostrados < MAX_MOSTRAR) {
                Label l = textoCelda("P(" + fraccion(c) + ") " + (esRaiz ? "=" : "\u2260") + " 0");
                if (esRaiz) l.setStyle(FUENTE_MONO + "-fx-font-weight: bold;");
                evaluaciones.getChildren().add(l);
                mostrados++;
            }
        }
        panelResultado.getChildren().add(evaluaciones);
        if (noEnteros.size() > MAX_MOSTRAR)
            panelResultado.getChildren().add(textoMono(
                    "(se muestran los primeros " + MAX_MOSTRAR + " de " + noEnteros.size() + " candidatos)"));

        // Factorizado: P(x) = (x - p/q) ... C(x), dividiendo por Horner por cada raiz racional
        if (encontradas.isEmpty()) {
            panelResultado.getChildren().add(textoMono("P(x) no tiene ra\u00edces racionales."));
        } else {
            panelResultado.getChildren().add(textoMono("Ra\u00edces racionales: " + String.join(", ", encontradas)));

            Polinomio actual = p;
            List<String> factoresRac = new ArrayList<>();
            for (double x : racionales) {
                int mult = 0;
                while (actual.getGrado() > 0) {
                    double tam = 1;
                    for (double cf : actual.getCoeficientes()) tam = Math.max(tam, Math.abs(cf));
                    OperacionesPolinomios.ResultadoDivisionLineal div = OperacionesPolinomios.dividirHorner(actual, -x);
                    if (Math.abs(div.resto) > 1e-9 * tam) break;   // ya no es raiz
                    actual = div.cociente;
                    mult++;
                }
                if (mult > 0) {
                    String f = "(x " + (x < 0 ? "+ " : "- ") + analisis.aFraccion(Math.abs(x)) + ")";
                    factoresRac.add(mult == 1 ? f : f + "^{" + mult + "}");
                }
            }

            if (factoresRac.isEmpty()) {
                panelResultado.getChildren().add(textoMono("No se pudo armar la factorizaci\u00f3n."));
            } else {
                String cociente;
                if (actual.getGrado() == 0) {
                    double c0 = actual.getCoeficiente(0);
                    if (Math.abs(c0 - 1) < 1e-9) cociente = "";
                    else if (Math.abs(c0 + 1) < 1e-9) cociente = " \u00b7 (-1)";
                    else cociente = " \u00b7 (" + texto(actual) + ")";
                } else {
                    cociente = " \u00b7 (" + texto(actual) + ")";
                }
                panelResultado.getChildren().add(
                        formula("P(x) = " + String.join(" ", factoresRac) + cociente, 20));
            }
        }
    }

    // ---------- Auxiliares de raices enteras / racionales ----------

    /** Division sintetica de Horner por (x - r): devuelve {cociente..., resto}. a en orden descendente. */
    private static BigInteger[] horner(BigInteger[] a, BigInteger r) {
        BigInteger[] b = new BigInteger[a.length];
        b[0] = a[0];
        for (int i = 1; i < a.length; i++) b[i] = b[i - 1].multiply(r).add(a[i]);
        return b;
    }

    /** Horner para r = p/q sin dividir: q^n * P(p/q) = 0 <=> p/q es raiz. */
    private static boolean esRaizRacional(BigInteger[] a, long p, long q) {
        BigInteger bp = BigInteger.valueOf(p);
        BigInteger bq = BigInteger.valueOf(q);
        BigInteger res = a[0];
        BigInteger potenciaQ = BigInteger.ONE;
        for (int i = 1; i < a.length; i++) {
            potenciaQ = potenciaQ.multiply(bq);
            res = res.multiply(bp).add(a[i].multiply(potenciaQ));
        }
        return res.signum() == 0;
    }

    /** Menor k >= 1 tal que k * coeficientes son todos enteros. */
    private static long escalaEntera(double[] c) {
        for (long k = 1; k <= 1000; k++) {
            boolean ok = true;
            for (double v : c) {
                double x = v * k;
                if (Math.abs(x) > 1e15 || Math.abs(x - Math.rint(x)) > 1e-9) {
                    ok = false;
                    break;
                }
            }
            if (ok) return k;
        }
        throw new IllegalArgumentException(
                "Los coeficientes no son racionales (por ejemplo tienen sen, ln, \u03c0...): "
                + "no se pueden analizar ra\u00edces enteras ni racionales");
    }

    /** Divisores positivos de |v|, en orden ascendente. */
    private static List<Long> divisores(BigInteger v) {
        BigInteger abs = v.abs();
        if (abs.bitLength() > 40)
            throw new IllegalArgumentException("Los coeficientes son demasiado grandes para buscar divisores");
        long n = abs.longValue();
        List<Long> chicos = new ArrayList<>();
        List<Long> grandes = new ArrayList<>();
        for (long i = 1; i * i <= n; i++) {
            if (n % i == 0) {
                chicos.add(i);
                if (i != n / i) grandes.add(0, n / i);
            }
        }
        chicos.addAll(grandes);
        return chicos;
    }

    private static long mcd(long a, long b) {
        while (b != 0) {
            long t = a % b;
            a = b;
            b = t;
        }
        return Math.abs(a);
    }

    private static String fraccion(long[] c) {
        return c[1] == 1 ? String.valueOf(c[0]) : c[0] + "/" + c[1];
    }

    /** {1, -1, 2, -2}: cada divisor positivo seguido de su opuesto. */
    private static String llaveConSignos(List<Long> divisores) {
        List<String> t = new ArrayList<>();
        for (long d : divisores) {
            t.add(String.valueOf(d));
            t.add(String.valueOf(-d));
        }
        return "{" + String.join(", ", t) + "}";
    }

    private static String aPolinomio(BigInteger[] a) {
        double[] d = new double[a.length];
        for (int i = 0; i < a.length; i++) d[i] = a[i].doubleValue();
        return texto(new Polinomio(d));
    }

    private Label tituloSeccion(String texto) {
        Label l = new Label(texto);
        l.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: " + COLOR_ENCABEZADO + ";");
        return l;
    }

    private Label negrita(String texto) {
        Label l = textoMono(texto);
        l.setStyle(FUENTE_MONO + "-fx-font-weight: bold;");
        return l;
    }

    /** Fila "etiqueta (con subindices) contenido", por ejemplo a_{0} = 6. */
    private HBox filaSub(String etiqueta, String contenido) {
        Label l = textoMono(contenido);
        HBox.setHgrow(l, Priority.ALWAYS);
        HBox h = new HBox(8, formula(etiqueta, 18), l);
        h.setAlignment(Pos.TOP_LEFT);
        return h;
    }

    /**
     * Metodos iterativos Newton y Halley. Arma la tabla:
     *   Newton: i | xi | P(xi) | P'(xi) | |xi - x(i-1)|
     *   Halley: i | xi | P(xi) | P'(xi) | P''(xi) | |xi - x(i-1)|
     * (en la primera fila no hay x anterior, por eso se muestra "-").
     * P(xi) y P'(xi) se calculan juntos con Horner doble.
     */
    private void metodoIterativo(Polinomio p, boolean halley) {
        String nombre = halley ? "Halley" : "Newton";

        // --- Parametros ---
        if (campoX0.getText().trim().isEmpty())
            throw new IllegalArgumentException("Falta x0: escrib\u00ed el valor inicial.");
        if (campoEps.getText().trim().isEmpty())
            throw new IllegalArgumentException("Falta epsilon: escrib\u00ed la tolerancia.");
        if (campoIter.getText().trim().isEmpty())
            throw new IllegalArgumentException("Falta el m\u00e1ximo de iteraciones.");
        double x0 = parsearNumero(campoX0.getText().trim(), "x0");
        double epsilon;
        int maxIter;
        try {
            epsilon = Double.parseDouble(campoEps.getText().trim().replace(',', '.'));
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("epsilon no es un n\u00famero v\u00e1lido");
        }
        try {
            maxIter = Integer.parseInt(campoIter.getText().trim());
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("El m\u00e1ximo de iteraciones debe ser un entero");
        }
        if (epsilon <= 0) throw new IllegalArgumentException("epsilon debe ser mayor que 0");
        if (maxIter < 1) throw new IllegalArgumentException("El m\u00e1ximo de iteraciones debe ser al menos 1");
        if (esConstante(p))
            throw new ArithmeticException("P(x) es constante: no hay ra\u00edz que buscar con " + nombre);

        Polinomio segunda = p.derivar().derivar(); // P''(x), solo lo usa Halley

        // --- Titulo, P(x) y formula ---
        Label lMetodo = new Label("Algoritmo: " + nombre);
        lMetodo.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: "
                + COLOR_ENCABEZADO + ";");
        panelResultado.getChildren().add(lMetodo);
        panelResultado.getChildren().add(textoMono("P(x) = " + texto(p)));
        panelResultado.getChildren().add(halley ? formulaHalley() : formulaNewton());

        // --- Tabla ---
        GridPane t = new GridPane();
        String[] encabezados = halley
                ? new String[]{"i", "x_{i}", "P(x_{i})", "P'(x_{i})", "P''(x_{i})", "|x_{i} - x_{i-1}|"}
                : new String[]{"i", "x_{i}", "P(x_{i})", "P'(x_{i})", "|x_{i} - x_{i-1}|"};
        for (int c = 0; c < encabezados.length; c++)
            t.add(celdaNewton(formula(encabezados[c], 18), 0, c), c, 0);

        double xi = x0;
        double xAnterior = x0;
        boolean convergio = false;
        String motivoCorte = null;
        int fila = 1;

        for (int i = 0; i <= maxIter; i++) {
            ResultadoHornerDoble r = OperacionesPolinomios.hornerDoble(p, xi);
            double valor = r.getValor();
            double deriv = r.getDerivada();
            double deriv2 = halley ? segunda.evaluar(xi) : 0;
            double dif = Math.abs(xi - xAnterior);

            String[] celdas = halley
                    ? new String[]{String.valueOf(i), formatoNewton(xi), formatoNewton(valor),
                        formatoNewton(deriv), formatoNewton(deriv2), i == 0 ? "-" : formatoNewton(dif)}
                    : new String[]{String.valueOf(i), formatoNewton(xi), formatoNewton(valor),
                        formatoNewton(deriv), i == 0 ? "-" : formatoNewton(dif)};
            for (int c = 0; c < celdas.length; c++)
                t.add(celdaNewton(textoCelda(celdas[c]), fila, c), c, fila);
            fila++;

            if (i > 0 && dif < epsilon) {   // corte: |xi - x(i-1)| < epsilon
                convergio = true;
                break;
            }
            if (valor == 0) {               // xi ya es raiz exacta
                convergio = true;
                break;
            }
            if (i == maxIter) break;

            double siguiente;
            if (halley) {
                double den = 2 * deriv * deriv - valor * deriv2;
                if (Math.abs(den) < 1e-14) {
                    motivoCorte = "El denominador de Halley \u2248 0 en x = " + formatoNewton(xi)
                            + ": el m\u00e9todo no puede continuar. Prob\u00e1 con otro x0.";
                    break;
                }
                siguiente = xi - 2 * valor * deriv / den;
            } else {
                if (Math.abs(deriv) < 1e-14) {
                    motivoCorte = "P'(x) \u2248 0 en x = " + formatoNewton(xi)
                            + ": el m\u00e9todo no puede continuar. Prob\u00e1 con otro x0.";
                    break;
                }
                siguiente = xi - valor / deriv;
            }
            xAnterior = xi;
            xi = siguiente;
        }

        panelResultado.getChildren().add(t);

        // --- Conclusion ---
        if (convergio) {
            panelResultado.getChildren().add(textoMono(
                    "Ra\u00edz aproximada: x \u2248 " + formatoNewton(xi)
                    + "   (" + (fila - 2) + " iteraciones)"));
        } else if (motivoCorte != null) {
            panelResultado.getChildren().add(textoMono(motivoCorte));
        } else {
            panelResultado.getChildren().add(textoMono(
                    "No convergi\u00f3 en " + maxIter + " iteraciones. Prob\u00e1 con otro x0 o m\u00e1s iteraciones."));
        }
    }

    /**
     * Arma un texto con subindices y potencias: lo que va en _{...} se dibuja chico y
     * bajado, y lo que va en ^{...} chico y subido.
     * Ejemplo: "P(x_{i})" o "[P'(x_{i})]^{2}".
     */
    private HBox formula(String texto, int tam) {
        HBox h = new HBox(0);
        h.setAlignment(Pos.BASELINE_LEFT);
        String estilo = FUENTE_MONO.replaceAll("font-size: \\d+px", "font-size: " + tam + "px");
        String estiloChico = FUENTE_MONO.replaceAll("font-size: \\d+px", "font-size: " + (int) (tam * 0.7) + "px");
        int i = 0;
        StringBuilder normal = new StringBuilder();
        while (i < texto.length()) {
            boolean sub = texto.startsWith("_{", i);
            boolean sup = texto.startsWith("^{", i);
            if (sub || sup) {
                if (normal.length() > 0) {
                    Text t = new Text(normal.toString());
                    t.setStyle(estilo);
                    h.getChildren().add(t);
                    normal.setLength(0);
                }
                int fin = texto.indexOf('}', i);
                Text chico = new Text(texto.substring(i + 2, fin));
                chico.setStyle(estiloChico);
                chico.setTranslateY(sub ? tam * 0.25 : -tam * 0.3);
                h.getChildren().add(chico);
                i = fin + 1;
            } else {
                normal.append(texto.charAt(i));
                i++;
            }
        }
        if (normal.length() > 0) {
            Text t = new Text(normal.toString());
            t.setStyle(estilo);
            h.getChildren().add(t);
        }
        return h;
    }

    /** Formula con fraccion: izquierda + (numerador sobre denominador). */
    private HBox formulaConFraccion(String izquierda, String numerador, String denominador) {
        HBox num = formula(numerador, 20);
        HBox den = formula(denominador, 20);
        num.setAlignment(Pos.CENTER);
        den.setAlignment(Pos.CENTER);
        Region linea = new Region();
        linea.setMinHeight(2);
        linea.setPrefHeight(2);
        linea.setMaxHeight(2);
        linea.setStyle("-fx-background-color: " + COLOR_ENCABEZADO + ";");
        VBox fraccion = new VBox(2, num, linea, den);
        fraccion.setAlignment(Pos.CENTER);

        HBox f = new HBox(8, formula(izquierda, 20), fraccion);
        f.setAlignment(Pos.CENTER_LEFT);
        return f;
    }

    /** x_{i+1} = x_i - P(x_i) / P'(x_i) */
    private HBox formulaNewton() {
        return formulaConFraccion("x_{i+1} = x_{i} -", "P(x_{i})", "P'(x_{i})");
    }

    /** x_{i+1} = x_i - 2 P P' / (2 [P']^2 - P P'') */
    private HBox formulaHalley() {
        return formulaConFraccion("x_{i+1} = x_{i} -",
                "2 P(x_{i}) P'(x_{i})",
                "2 [P'(x_{i})]^{2} - P(x_{i}) P''(x_{i})");
    }

    /** Texto simple para una celda de la tabla de Newton. */
    private Label textoCelda(String texto) {
        Label l = new Label(texto);
        l.setStyle(FUENTE_MONO);
        return l;
    }

    /**
     * Celda de las tablas de Newton/Halley: fondo blanco y borde de linea sin espacios entre celdas.
     * La primera fila (encabezado) lleva el contorno remarcado (linea mas gruesa).
     */
    private HBox celdaNewton(Node contenido, int fila, int col) {
        HBox c = new HBox(contenido);
        c.setAlignment(Pos.CENTER);
        c.setMinWidth(90);
        c.setPadding(new Insets(6, 10, 6, 10));
        String grosor;
        if (fila == 0) {
            grosor = "3 3 3 " + (col == 0 ? 3 : 0);   // arriba, derecha, abajo, izquierda
        } else {
            grosor = "0 1 1 " + (col == 0 ? 1 : 0);
        }
        c.setStyle("-fx-background-color: white; -fx-border-color: " + COLOR_ENCABEZADO + ";"
                + "-fx-border-width: " + grosor + ";");
        return c;
    }

    /** Numeros para la tabla de Newton: decimal normal o notacion cientifica si es muy chico/grande. */
    private static String formatoNewton(double v) {
        if (v == 0) return "0";
        double a = Math.abs(v);
        if (a < 1e-4 || a >= 1e7) return String.format(Locale.US, "%.4e", v);
        String s = String.format(Locale.US, "%.8f", v);
        return s.replaceAll("0+$", "").replaceAll("\\.$", "");
    }

    // ------------------------------------------------------------------
    // Bairstow (BairstowSolver)
    // ------------------------------------------------------------------

    private double leerEpsilon() {
        String t = campoEps.getText().trim();
        if (t.isEmpty()) throw new IllegalArgumentException("Falta epsilon: escrib\u00ed la tolerancia.");
        double eps;
        try {
            eps = Double.parseDouble(t.replace(',', '.'));
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("epsilon no es un n\u00famero v\u00e1lido");
        }
        if (eps <= 0) throw new IllegalArgumentException("epsilon debe ser mayor que 0");
        return eps;
    }

    private int leerMaxIter() {
        String t = campoIter.getText().trim();
        if (t.isEmpty()) throw new IllegalArgumentException("Falta el m\u00e1ximo de iteraciones.");
        int it;
        try {
            it = Integer.parseInt(t);
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("El m\u00e1ximo de iteraciones debe ser un entero");
        }
        if (it < 1) throw new IllegalArgumentException("El m\u00e1ximo de iteraciones debe ser al menos 1");
        return it;
    }

    /** Numero para la tabla de Bairstow ("-" si no hay valor). */
    private static String celdaNumero(double v) {
        return Double.isNaN(v) ? "-" : formatoNewton(v);
    }

    /**
     * Bairstow con tabla de iteraciones.
     * La clase BairstowSolver solo devuelve las raices, asi que la tabla se arma repitiendo aca
     * el mismo algoritmo (mismos coeficientes c, mismo determinante, mismas formulas de dr y ds,
     * mismos criterios de corte). Despues se llama a BairstowSolver.bairstow() de verdad para comparar.
     *
     * Columnas: k | r | s | dr | ds | |dr/r| | |ds/s|
     * Cada fila k muestra el r y s con los que se calcula ese paso y los dr, ds resultantes
     * (r_{k+1} = r_k + dr). Asi la fila 0 ya trae r0, s0 y sus dr, ds.
     */
    private void metodoBairstow(Polinomio p) {
        if (campoR0.getText().trim().isEmpty())
            throw new IllegalArgumentException("Falta r0: escrib\u00ed el valor inicial de r.");
        if (campoS0.getText().trim().isEmpty())
            throw new IllegalArgumentException("Falta s0: escrib\u00ed el valor inicial de s.");
        final double r0 = parsearNumero(campoR0.getText().trim(), "r0");
        final double s0 = parsearNumero(campoS0.getText().trim(), "s0");
        final double epsilon = leerEpsilon();
        final int maxIter = leerMaxIter();
        if (esConstante(p))
            throw new ArithmeticException("P(x) es constante: no hay ra\u00edces que buscar con Bairstow");

        Label lMetodo = new Label("Algoritmo: Bairstow");
        lMetodo.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: "
                + COLOR_ENCABEZADO + ";");
        panelResultado.getChildren().add(lMetodo);
        panelResultado.getChildren().add(textoMono("P(x) = " + texto(p)));
        panelResultado.getChildren().add(textoMono(
                "Factor cuadr\u00e1tico: x^2 - r x - s     r = r + \u0394r     s = s + \u0394s     "
                + "(los factores siguientes siguen desde el r y s del anterior)"));

        // ---- 1) Tabla: mismo algoritmo que BairstowSolver.bairstow ----
        double[] coef = p.getCoeficientes();
        int n = coef.length - 1;
        double r = r0;
        double s = s0;
        int iter = 0;
        List<RaizCompleja> raices = new ArrayList<>();
        boolean cortado = false;
        int numero = 1;

        while (n > 2 && iter <= maxIter && !cortado) {
            iter = 0;
            panelResultado.getChildren().add(new Separator());
            panelResultado.getChildren().add(negrita(
                    "Factor " + numero + ":  P" + numero + "(x) = " + texto(new Polinomio(coef))));

            GridPane t = new GridPane();
            String[] encabezados = {"k", "r", "s", "\u0394r", "\u0394s", "|\u0394r/r|", "|\u0394s/s|"};
            for (int c = 0; c < encabezados.length; c++)
                t.add(celdaNewton(formula(encabezados[c], 18), 0, c), c, 0);
            int fila = 1;

            List<String> notas = new ArrayList<>();
            double errorR, errorS;
            int pasos = 0;
            final int TOPE_PASOS = 10 * maxIter + 100; // corte de seguridad de la app
            do {
                iter++;
                pasos++;
                final double rAntes = r;   // r de esta fila (antes de actualizar)
                final double sAntes = s;   // s de esta fila (antes de actualizar)

                double[] b = bairstowB(coef, r, s);
                double[] c = bairstowB(b, r, s);
                // residuos de b[] (orden descendente: los ultimos elementos)
                // b0 = resto verdadero de la division: b[n] - r*b[n-1]  (= P[n] + s*b[n-2])
                double b0 = b[b.length - 1];
                double b1 = b[b.length - 2];
                // coeficientes de c[]
                double c1 = c[c.length - 2];
                double c2 = c[c.length - 3];
                double c3 = c[c.length - 4];
                double det = (c2 * c2) - (c1 * c3);

                double dr, ds;
                int kFila = iter;
                if (Double.isFinite(det) && Math.abs(det) > 1e-12) {
                    dr = (b0 * c3 - b1 * c2) / det;
                    ds = (b1 * c1 - b0 * c2) / det;
                    r += dr;
                    s += ds;
                    errorR = Math.abs(dr / (r != 0 ? r : 1.0));
                    errorS = Math.abs(ds / (s != 0 ? s : 1.0));
                } else {
                    dr = 1.0;
                    ds = 1.0;
                    r += 1.0;
                    s += 1.0;
                    notas.add("En la iteraci\u00f3n " + kFila + " el determinante fue \u2248 0: se suma 1 a r y a s y se reinicia el contador k.");
                    iter = 0;
                    errorR = 1.0;
                    errorS = 1.0;
                }
                fila = filaBairstow(t, fila, kFila - 1, rAntes, sAntes, dr, ds, errorR, errorS);

                if (Double.isNaN(r) || Double.isNaN(s) || Double.isInfinite(r) || Double.isInfinite(s)) {
                    notas.add("r o s se volvi\u00f3 NaN/infinito: la app corta ac\u00e1.");
                    cortado = true;
                    break;
                }
                if (pasos >= TOPE_PASOS) {
                    notas.add("Corte de seguridad de la app tras " + pasos + " pasos.");
                    cortado = true;
                    break;
                }
                if ((errorR <= epsilon && errorS <= epsilon) || iter > maxIter) {
                    break;
                }
            } while (true);
            // fila final: k = ultima iteracion + 1, solo r y s (en negrita), sin Delta
            if (!cortado) filaBairstowFinal(t, fila, iter, r, s);
            panelResultado.getChildren().add(t);
            for (String nota : notas) panelResultado.getChildren().add(textoMono(nota));
            if (cortado) break;

            boolean convergio = false;
            // convergio si salio por tolerancia (no por pasarse de iteraciones)
            if (iter <= maxIter) convergio = true;
            panelResultado.getChildren().add(formula("Q(x) = " + factorCuadratico(r, s), 20));
            if (!convergio)
                panelResultado.getChildren().add(negrita(
                        "No convergi\u00f3 en " + maxIter + " iteraciones (igual se usan estos r y s y se sigue)."));

            List<RaizCompleja> parciales = BairstowSolver.formulaCuad(r, s);
            raices.addAll(parciales);
            panelResultado.getChildren().add(textoMono("Ra\u00edces del factor: " + unirRaices(parciales)));

            n = n - 2;
            double[] b = bairstowB(coef, r, s);
            double[] cociente = new double[coef.length - 2];
            for (int i = 0; i < cociente.length; i++) cociente[i] = b[i];
            coef = cociente;
            panelResultado.getChildren().add(textoMono("Cociente: " + texto(new Polinomio(coef))));
            numero++;
        }

        if (!cortado && iter <= maxIter) {
            if (n == 2) {
                List<RaizCompleja> fin = BairstowSolver.formulaCuad(-coef[1] / coef[0], -coef[2] / coef[0]);
                raices.addAll(fin);
                panelResultado.getChildren().add(textoMono(
                        "Queda " + texto(new Polinomio(coef)) + " (grado 2): f\u00f3rmula cuadr\u00e1tica."));
                panelResultado.getChildren().add(textoMono("Ra\u00edces del factor: " + unirRaices(fin)));
            } else if (n == 1) {
                RaizCompleja lineal = new RaizCompleja(-coef[1] / coef[0], 0.0);
                raices.add(lineal);
                panelResultado.getChildren().add(textoMono(
                        "Queda " + texto(new Polinomio(coef)) + " (grado 1)."));
                panelResultado.getChildren().add(textoMono("Ra\u00edz: " + lineal));
            }
        }
    }

    /** Agrega una fila de la tabla de Bairstow (NaN se muestra como "-"). Devuelve la fila siguiente. */
    private int filaBairstow(GridPane t, int fila, int k, double r, double s,
                             double dr, double ds, double errR, double errS) {
        String[] celdas = {String.valueOf(k), celdaNumero(r), celdaNumero(s),
                celdaNumero(dr), celdaNumero(ds), celdaNumero(errR), celdaNumero(errS)};
        for (int c = 0; c < celdas.length; c++)
            t.add(celdaNewton(textoCelda(celdas[c]), fila, c), c, fila);
        return fila + 1;
    }

    /** Fila final de la tabla: k, r y s en negrita; el resto de las celdas vacias. */
    private void filaBairstowFinal(GridPane t, int fila, int k, double r, double s) {
        String[] celdas = {String.valueOf(k), formatoNewton(r), formatoNewton(s), "", "", "", ""};
        for (int c = 0; c < celdas.length; c++) {
            Label l = textoCelda(celdas[c]);
            l.setStyle(FUENTE_MONO + "-fx-font-weight: bold;");
            t.add(celdaNewton(l, fila, c), c, fila);
        }
    }

    /** Texto del factor cuadratico x^2 - r x - s, con los signos bien puestos. */
    private static String factorCuadratico(double r, double s) {
        String tr = (-r < 0 ? "- " : "+ ") + formatoNewton(Math.abs(r)) + "x";
        String ts = (-s < 0 ? "- " : "+ ") + formatoNewton(Math.abs(s));
        return "x^{2} " + tr + " " + ts;
    }

    /** Copia de calcularB de BairstowSolver: b[i] = coef[i] + r b[i-1] + s b[i-2]. */
    private static double[] bairstowB(double[] coef, double r, double s) {
        int n = coef.length - 1;
        double[] b = new double[n + 1];
        b[0] = coef[0];
        if (n >= 1) b[1] = coef[1] + r * b[0];
        for (int i = 2; i <= n; i++) b[i] = coef[i] + r * b[i - 1] + s * b[i - 2];
        return b;
    }

    private static String unirRaices(List<RaizCompleja> raices) {
        List<String> t = new ArrayList<>();
        for (RaizCompleja z : raices) t.add(z.toString());
        return String.join(",   ", t);
    }

    // ------------------------------------------------------------------
    // Cotas de las raices (CotasDePolinomio)
    // ------------------------------------------------------------------

    /**
     * Cotas de las raices reales de P(x) con el metodo elegido (Lagrange, Laguerre o Newton).
     * Muestra las cuatro cotas con su cambio de variable y sus pasos, y al final los intervalos.
     */
        private void metodoCotas(Polinomio p) {
        if (esConstante(p))
            throw new ArithmeticException("P(x) es constante: no tiene ra\u00edces que acotar");
        String nombre = metodoCota.getValue();
        if (nombre == null)
            throw new IllegalArgumentException("Falta elegir el m\u00e9todo de cota (Lagrange, Laguerre o Newton).");
        int num = numeroMetodoCota(nombre); // 1 = Lagrange, 2 = Laguerre, 3 = Newton

        CotasDePolinomio cotas = new CotasDePolinomio();

        Label lMetodo = new Label("Cotas de las ra\u00edces \u2013 m\u00e9todo: " + nombre);
        lMetodo.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: "
                + COLOR_ENCABEZADO + ";");
        panelResultado.getChildren().add(lMetodo);
        panelResultado.getChildren().add(textoMono("P(x) = " + texto(p)));
        if (p.getCoeficiente(p.getGrado()) == 0)
            panelResultado.getChildren().add(textoMono(
                    "El t\u00e9rmino independiente es 0: x = 0 es ra\u00edz. Las cotas son para las dem\u00e1s ra\u00edces."));

        // 1) Cota superior de las raices positivas: se aplica directo a P(x)
        double supPos = cotas.cotaSuperiorPositiva(p, num);
        mostrarCota("1) Cota superior de las ra\u00edces positivas", "Se usa P(x)",
                supPos == 0 ? "No hay ra\u00edces positivas."
                            : "Cota superior positiva = " + formato(supPos));

        // 2) Cota inferior de las raices positivas: x = 1/t
        Polinomio q1 = cambioInfPositiva(p);
        double infPos = cotas.cotaInferiorPositiva(p, num);
        mostrarCota("2) Cota inferior de las ra\u00edces positivas",
                "Cambio x = 1/t:  x^n \u00b7 P(1/x) = " + texto(q1),
                infPos == 0 ? "No hay ra\u00edces positivas."
                            : "Cota inferior positiva = 1 / " + formato(1 / infPos) + " = " + formato(infPos));

        // 3) Cota inferior de las raices negativas: x = -t
        Polinomio q2 = cambioInfNegativa(p);
        double infNeg = cotas.cotaInferiorNegativa(p, num);
        mostrarCota("3) Cota inferior de las ra\u00edces negativas",
                "Cambio x = -t:  P(-x) = " + texto(q2),
                infNeg == 0 ? "No hay ra\u00edces negativas."
                            : "Cota inferior negativa = -" + formato(-infNeg) + " = " + formato(infNeg));

        // 4) Cota superior de las raices negativas: x = -1/t
        Polinomio q3 = cambioSupNegativa(p);
        double supNeg = cotas.cotaSuperiorNegativa(p, num);
        mostrarCota("4) Cota superior de las ra\u00edces negativas",
                "Cambio x = -1/t:  x^n \u00b7 P(-1/x) = " + texto(q3),
                supNeg == 0 ? "No hay ra\u00edces negativas."
                            : "Cota superior negativa = -1 / " + formato(-1 / supNeg) + " = " + formato(supNeg));

        // Intervalos finales (los arma CotasDePolinomio)
        panelResultado.getChildren().add(new Separator());
        panelResultado.getChildren().add(tituloSeccion("Intervalos"));
        for (String linea : cotas.intervalos(p, num).split("\n"))
            panelResultado.getChildren().add(negrita(linea));
    }

    private int numeroMetodoCota(String nombre) {
        switch (nombre) {
            case "Lagrange":
                return 1;
            case "Laguerre":
                return 2;
            case "Newton":
                return 3;
            default:
                throw new IllegalArgumentException(
                        "Metodo de cota no valido: " + nombre);
        }
    }

    /** Bloque de una cota: titulo, polinomio usado y resultado. */
    private void mostrarCota(String titulo, String polinomio, String resultado) {
        panelResultado.getChildren().add(new Separator());
        panelResultado.getChildren().add(negrita(titulo));
        panelResultado.getChildren().add(textoMono(polinomio));
        panelResultado.getChildren().add(negrita(resultado));
    }

    // Cambios de variable (copias de los que son private en CotasDePolinomio vieja)
    private static Polinomio cambioInfPositiva(Polinomio p) {
        double[] c = p.getCoeficientes();
        int fin = c.length;
        while (fin > 1 && c[fin - 1] == 0) fin--;          // saca el factor x
        double[] nuevo = new double[fin];
        for (int i = 0; i < fin; i++) nuevo[i] = c[fin - 1 - i];   // invierte el orden
        return new Polinomio(nuevo);
    }

    private static Polinomio cambioInfNegativa(Polinomio p) {
        double[] c = p.getCoeficientes();
        int n = p.getGrado();
        double[] nuevo = new double[c.length];
        for (int i = 0; i <= n; i++) nuevo[i] = c[i] * Math.pow(-1, n - i);
        return new Polinomio(nuevo);
    }

    private static Polinomio cambioSupNegativa(Polinomio p) {
        return cambioInfNegativa(cambioInfPositiva(p));
    }

    // ------------------------------------------------------------------
    // Horner / Ruffini
    // ------------------------------------------------------------------

    /**
     * Divide P(x) entre Q(x) eligiendo el algoritmo segun el grado del divisor
     * y arma el panel de resultado:
     * metodo, P(x), tabla, cociente, resto y P(x) = C(x)*Q(x) + R(x).
     */
    private void dividirPolinomios(Polinomio p, Polinomio q) {
        double[] qc = q.getCoeficientes(); // de mayor a menor grado
        int n = p.getGrado();
        int m = q.getGrado();
        if (m == 0 && qc[0] == 0) throw new ArithmeticException("Q(x) no puede ser cero");

        Polinomio cociente;
        Polinomio resto;
        String metodoTxt;
        int tipo; // 1 = Ruffini, 2 = Horner cuadratico, 0 = sin tabla

        if (m == 0) {
            // Q(x) constante: divisionGeneral devolveria un resto vacio (y Polinomio no admite 0 coeficientes)
            double[] cc = p.getCoeficientes();
            for (int i = 0; i < cc.length; i++) cc[i] /= qc[0];
            cociente = new Polinomio(cc);
            resto = new Polinomio(new double[]{0.0});
            metodoTxt = "Divisi\u00f3n por una constante";
            tipo = 0;
        } else if (m == 1 && n >= 1) {
            OperacionesPolinomios.ResultadoDivisionLineal r =
                    OperacionesPolinomios.dividirHornerGral(p, qc[0], qc[1]);
            cociente = r.cociente;
            resto = new Polinomio(new double[]{r.resto});
            metodoTxt = "Horner / Ruffini";
            tipo = 1;
        } else if (m == 2 && n >= 3 && Math.abs(qc[0] - 1) < 1e-12) {
            OperacionesPolinomios.ResultadoDivisionCuadratica r =
                    OperacionesPolinomios.dividirHornerCuadratico(p, qc[1], qc[2]);
            cociente = r.cociente;
            resto = new Polinomio(new double[]{r.resto1, r.resto0});
            metodoTxt = "Horner / Ruffini doble ";
            tipo = 2;
        } else {
            OperacionesPolinomios.ResultadoDivisionGeneral r =
                    OperacionesPolinomios.divisionGeneral(p, q);
            cociente = r.cociente;
            resto = r.resto;
            metodoTxt = "Divisi\u00f3n general";
            tipo = 0;
        }

        // 1) Metodo
        Label lMetodo = new Label("Algoritmo: " + metodoTxt);
        lMetodo.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: "
                + COLOR_ENCABEZADO + ";");
        panelResultado.getChildren().add(lMetodo);

        // 2) P(x)
        panelResultado.getChildren().add(textoMono("P(x) = " + texto(p)));

        // 3) Como se hace (tabla)
        if (tipo == 1) {
            agregarTablaRuffini(p, q);
        } else if (tipo == 2) {
            agregarTablaHornerCuadratico(p, q);
        }

        // 4) Cociente y resto
        panelResultado.getChildren().add(textoMono("Cociente C(x) = " + texto(cociente)));
        panelResultado.getChildren().add(textoMono("Resto R(x)    = " + texto(resto)));

        // 5) P(x) = C(x) * Q(x) + R(x)
        panelResultado.getChildren().add(textoMono(
                "P(x) = (" + texto(cociente) + ") * (" + texto(q) + ") + (" + texto(resto) + ")"));
    }

    // ------------------------------------------------------------------
    // Tablas
    // ------------------------------------------------------------------

    /** Tabla de Horner / Ruffini para un divisor lineal alfa*x + beta. */
    private void agregarTablaRuffini(Polinomio p, Polinomio q) {
        double[] a = p.getCoeficientes();
        double[] qc = q.getCoeficientes();
        double r = -qc[1] / qc[0];          // raiz del divisor
        int n = a.length;

        double[] medio = new double[n];     // productos
        double[] abajo = new double[n];     // sumas
        abajo[0] = a[0];
        for (int i = 1; i < n; i++) {
            medio[i] = abajo[i - 1] * r;
            abajo[i] = a[i] + medio[i];
        }

        GridPane t = new GridPane();

        // Fila 0: coeficientes de P(x)
        for (int i = 0; i < n; i++)
            t.add(celda(formato(a[i]), i == 0, false, false), i + 1, 0);

        // Fila 1: raiz y productos
        t.add(celda(formato(r), false, false, false), 0, 1);
        t.add(celda("", true, false, false), 1, 1);
        for (int i = 1; i < n; i++)
            t.add(celda(formato(medio[i]), false, false, false), i + 1, 1);

        // Fila 2: sumas (el ultimo es el resto)
        for (int i = 0; i < n; i++)
            t.add(celda(formato(abajo[i]), i == 0, true, i == n - 1), i + 1, 2);
        panelResultado.getChildren().add(t);
    }

    /** Tabla de Horner para dividir entre x^2 + a x + b. */
    private void agregarTablaHornerCuadratico(Polinomio p, Polinomio q) {
        double[] P = p.getCoeficientes();
        double[] qc = q.getCoeficientes();
        double a = qc[1], b = qc[2];
        int n = P.length - 1;                 // grado de P

        double[] fila1 = new double[n + 1];   // productos de -a
        double[] fila2 = new double[n + 1];   // productos de -b
        double[] s = new double[n + 1];       // sumas

        for (int j = 0; j <= n; j++) {
            if (j >= 1 && j <= n - 1) fila1[j] = -a * s[j - 1];
            if (j >= 2)               fila2[j] = -b * s[j - 2];
            s[j] = P[j] + fila1[j] + fila2[j];
        }

        GridPane t = new GridPane();

        // Fila 0: coeficientes de P(x)
        for (int j = 0; j <= n; j++)
            t.add(celda(formato(P[j]), j == 0, false, false), j + 1, 0);

        // Fila 1: -a y sus productos
        t.add(celda(formato(-a), false, false, false), 0, 1);
        t.add(celda("", true, false, false), 1, 1);
        for (int j = 1; j <= n - 1; j++)
            t.add(celda(formato(fila1[j]), false, false, false), j + 1, 1);

        // Fila 2: -b y sus productos
        t.add(celda(formato(-b), false, false, false), 0, 2);
        t.add(celda("", true, false, false), 1, 2);
        for (int j = 2; j <= n; j++)
            t.add(celda(formato(fila2[j]), false, false, false), j + 1, 2);

        // Fila 3: sumas (los dos ultimos son el resto)
        for (int j = 0; j <= n; j++)
            t.add(celda(formato(s[j]), j == 0, true, j >= n - 1), j + 1, 3);

        panelResultado.getChildren().add(t);
    }

    /** Celda de la tabla. conLineaIzq: linea vertical; conLineaSup: linea horizontal; resto: resaltada. */
    private Label celda(String texto, boolean conLineaIzq, boolean conLineaSup, boolean resto) {
        Label l = new Label(texto);
        l.setMinWidth(70);
        l.setAlignment(Pos.CENTER);
        l.setPadding(new Insets(6, 10, 6, 10));
        String borde = "-fx-border-color: " + COLOR_ENCABEZADO + ";"
                + "-fx-border-width: " + (conLineaSup ? 2 : 0) + " 0 0 " + (conLineaIzq ? 2 : 0) + ";";
        String fondo = resto
                ? "-fx-background-color: " + COLOR_BOTONES + "; -fx-text-fill: white;"
                : "";
        l.setStyle(FUENTE_MONO + borde + fondo);
        return l;
    }

    /** Etiqueta de texto con fuente monoespaciada. */
    private Label textoMono(String texto) {
        Label l = new Label(texto);
        l.setWrapText(true);
        l.setStyle(FUENTE_MONO);
        return l;
    }

    /** Enteros sin decimales (3 en vez de 3.0); el resto con hasta 6 decimales. */
    private static String formato(double v) {
        if (Math.abs(v - Math.rint(v)) < 1e-9) return String.valueOf((long) Math.rint(v));
        String s = String.format(Locale.US, "%.6f", v);
        return s.replaceAll("0+$", "").replaceAll("\\.$", "");
    }

    public static void main(String[] args) {
        launch(args);
    }
}