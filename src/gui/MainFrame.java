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
import logic.RaicesSolver;
import logic.SturmSolver;                 
import model.Polinomio;
import model.RaizResult;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

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
    private static final String M_HORNER = "Horner / Ruffini (división)";
    private static final String M_NEWTON = "Newton";
    private static final String M_HALLEY = "Halley";
    private static final String M_RAICES = "Raíces enteras y racionales";
    private static final String M_BAIRSTOW = "Bairstow";
    private static final String M_COTAS = "Cotas de las raíces";
    private static final String M_STURM = "Sturm";   

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
    private TextField campoPuntos;  //  - Sturm: puntos alfa (opcional)
    private HBox grupoSturm;        //  fila con el campo de puntos de Sturm
    private Button botonIntervalo;  //  Sturm: "Calcular cotas"
    private Label etiquetaIntervalo; // Sturm: muestra el intervalo valido al lado del boton
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

        Button minimizar = botonVentana("—", false);
        minimizar.setOnAction(e -> stage.setIconified(true));

        Button maximizar = botonVentana("□", false);
        maximizar.setOnAction(e -> alternarMaximizar(stage, pantalla));

        Button cerrar = botonVentana("✕", true);
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
        Label titulo = new Label("Raíces de Polinomios");
        titulo.setStyle("-fx-font-size: 26px; -fx-font-weight: bold; -fx-text-fill: "
                + COLOR_ENCABEZADO + ";");
        HBox zonaTitulo = new HBox(titulo);
        zonaTitulo.setAlignment(Pos.CENTER);
        zonaTitulo.setPadding(new Insets(6, 20, 0, 20));
        zonaTitulo.setStyle("-fx-background-color: " + COLOR_FONDO + ";");

        // ---------- Selector de metodo ----------
        Label etiquetaMetodo = new Label("Operación:");
        etiquetaMetodo.setStyle("-fx-font-size: 18px;");
        metodo = new ComboBox<>();
        metodo.getItems().addAll(M_HORNER, M_NEWTON, M_HALLEY, M_BAIRSTOW, M_RAICES, M_COTAS, M_STURM);  
        metodo.setValue(M_HORNER);
        metodo.setStyle("-fx-font-size: 16px;");
        etiquetaCota = new Label("Cota por:");
        etiquetaCota.setStyle("-fx-font-size: 18px;");
        metodoCota = new ComboBox<>();
        metodoCota.getItems().addAll("Lagrange", "Laguerre", "Newton");
        metodoCota.setValue("Lagrange");
        metodoCota.setStyle("-fx-font-size: 16px;");
        botonIntervalo = boton("Calcular cotas", this::verIntervalo);
        botonIntervalo.setMinWidth(140);
        etiquetaIntervalo = new Label();
        etiquetaIntervalo.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: " + COLOR_ENCABEZADO + ";");
        HBox filaMetodo = new HBox(10, etiquetaMetodo, metodo, etiquetaCota, metodoCota, botonIntervalo, etiquetaIntervalo);
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
        campoIter.setPromptText("máx. iteraciones");
        campoIter.setStyle(ESTILO_CAMPO);
        campoIter.setPrefColumnCount(5);


        Label lx0 = new Label("x₀ =");
        Label leps = new Label("ε =");
        Label liter = new Label("Máx. iter. =");
        lx0.setStyle("-fx-font-size: 18px;");
        leps.setStyle("-fx-font-size: 18px;");
        liter.setStyle("-fx-font-size: 18px;");
        Label lr0 = new Label("r₀ =");
        Label ls0 = new Label("s₀ =");
        lr0.setStyle("-fx-font-size: 18px;");
        ls0.setStyle("-fx-font-size: 18px;");
        grupoX0 = new HBox(10, lx0, campoX0);
        grupoX0.setAlignment(Pos.CENTER_LEFT);
        grupoRS = new HBox(10, lr0, campoR0, ls0, campoS0);
        grupoRS.setAlignment(Pos.CENTER_LEFT);
        parametrosNewton = new HBox(10, grupoX0, grupoRS, leps, campoEps, liter, campoIter);
        campoPuntos = new TextField();
        campoPuntos.setPromptText("Toc\u00e1 \"Calcular cotas\": los puntos por defecto aparecen ac\u00e1 y los pod\u00e9s cambiar");   
        campoPuntos.setDisable(true);   
        campoPuntos.setStyle(ESTILO_CAMPO);
        Label lPuntos = new Label("Puntos α =");
        lPuntos.setStyle("-fx-font-size: 18px;");
        grupoSturm = new HBox(10, lPuntos, campoPuntos);
        grupoSturm.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(campoPuntos, Priority.ALWAYS);
        parametrosNewton.setAlignment(Pos.CENTER_LEFT);

        // Enter (la tecla) en cualquier campo ejecuta el calculo
        campo.setOnAction(e -> calcular());
        campoQ.setOnAction(e -> calcular());
        campoX0.setOnAction(e -> calcular());
        campoR0.setOnAction(e -> calcular());
        campoS0.setOnAction(e -> calcular());
        campoEps.setOnAction(e -> calcular());
        campoIter.setOnAction(e -> calcular());
        campoPuntos.setOnAction(e -> calcular());   

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
        campoPuntos.focusedProperty().addListener((o, a, enfocado) -> {   
            if (enfocado) activo = campoPuntos;
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
        formulario.add(grupoSturm, 0, 3, 2, 1);   
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
        teclado.add(boton("π", () -> insertar("π")), 3, 0);
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
        for (TextField tf : new TextField[]{campo, campoQ, campoX0, campoEps, campoIter, campoR0, campoS0, campoPuntos})
            tf.textProperty().addListener((o, a, b) -> graficar.setDisable(true));
        metodo.valueProperty().addListener((o, a, b) -> graficar.setDisable(true));
        metodoCota.valueProperty().addListener((o, a, b) -> graficar.setDisable(true));
        campo.textProperty().addListener((o, a, b) -> desactivarPuntos());
        metodo.valueProperty().addListener((o, a, b) -> desactivarPuntos());
        metodoCota.valueProperty().addListener((o, a, b) -> desactivarPuntos());
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
            if (M_STURM.equals(b)) metodoCota.setValue("Newton");  
            actualizarModo();
            error.setText("");
            panelResultado.getChildren().clear();
        });
        actualizarModo();

        VBox raiz = new VBox(barra, zonaTitulo, contenido);
        raiz.setStyle("-fx-background-color: " + COLOR_FONDO + ";");

        stage.setTitle("Raíces de Polinomios");
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

    private boolean esSturm() {   
        return M_STURM.equals(metodo.getValue());
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
        boolean sturm = esSturm();               
        boolean cotas = esCotas() || sturm;     
        etiquetaCota.setVisible(cotas);
        etiquetaCota.setManaged(cotas);
        metodoCota.setVisible(cotas);
        metodoCota.setManaged(cotas);
        grupoSturm.setVisible(sturm);            
        grupoSturm.setManaged(sturm);            
        botonIntervalo.setVisible(sturm);        
        botonIntervalo.setManaged(sturm);        
        etiquetaIntervalo.setVisible(sturm);     
        etiquetaIntervalo.setManaged(sturm);     
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
                throw new IllegalArgumentException("Falta P(x): escribí un polinomio.");
            Polinomio p = parsear(texto, "P(x)"); // valida P(x)

            if (esIterativo()) {
                metodoIterativo(p, esHalley());
            } else if (esRaices()) {
                raicesPolinomio(p);
            } else if (esBairstow()) {
                metodoBairstow(p);
            } else if (esCotas()) {
                metodoCotas(p);
            } else if (esSturm()) {   
                metodoSturm(p);
            } else {
                String textoQ = campoQ.getText().trim();
                if (textoQ.isEmpty())
                    throw new IllegalArgumentException("Falta Q(x): escribí el divisor.");
                Polinomio q = parsear(textoQ, "Q(x)");
                dividirPolinomios(p, q);
            }
            botonGraficar.setDisable(false);   // calculo OK: ya se puede graficar
        } catch (RuntimeException ex) {
            panelResultado.getChildren().clear();
            String msg = ex.getMessage() != null ? ex.getMessage() : ex.toString();
            error.setText("Error: " + msg);
        } catch (Throwable ex) {   
            ex.printStackTrace();
            panelResultado.getChildren().clear();
            error.setText("Error interno: " + ex + "  (¿están actualizadas todas las clases de logic? Hacé Clean and Build)");
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
                throw new IllegalArgumentException("Falta P(x): escribí un polinomio.");
            Polinomio p = parsear(textoP, "P(x)");
            if (esConstante(p))
                throw new ArithmeticException("P(x) es constante: no hay nada que graficar");

            GraficoPolinomio g = new GraficoPolinomio(p, "P(x) = " + texto(p));

            if (esIterativo()) {
                // Newton / Halley: marca la raiz que encuentra RaicesSolver
                if (campoX0.getText().trim().isEmpty())
                    throw new IllegalArgumentException("Falta x0: escribí el valor inicial.");
                double x0 = parsearNumero(campoX0.getText().trim(), "x0");
                double eps = leerEpsilon();
                int maxIter = leerMaxIter();
                try {
                    double raiz = resolver(p, x0, eps, maxIter, esHalley()).getValorRaiz();
                    g.agregarPunto(raiz, "(" + GraficoPolinomio.num(raiz) + ", 0)", GraficoPolinomio.ROJO);
                } catch (RuntimeException ex) {   // derivada ~ 0 o no convergio
                    g.setNota("No convergió: no hay raíz que marcar. Probá con otro x0.");
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
                if (cantidad == 0) g.setNota("P(x) no tiene raíces enteras ni racionales.");
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
                    nota += "No hay raíces positivas.  ";
                }
                if (infNeg != 0) {
                    g.agregarBanda(infNeg, supNeg, GraficoPolinomio.ROJO);
                    g.agregarPunto(infNeg, "(" + GraficoPolinomio.num(infNeg) + ", 0)", GraficoPolinomio.ROJO);
                    g.agregarPunto(supNeg, "(" + GraficoPolinomio.num(supNeg) + ", 0)", GraficoPolinomio.ROJO);
                } else {
                    nota += "No hay raíces negativas.";
                }
                g.setNota(nota.trim());
            } else if (esBairstow()) {
                // Bairstow: marca las raices reales y avisa de las complejas (no se pueden dibujar en el eje x)
                if (campoR0.getText().trim().isEmpty())
                    throw new IllegalArgumentException("Falta r0: escribí el valor inicial de r.");
                if (campoS0.getText().trim().isEmpty())
                    throw new IllegalArgumentException("Falta s0: escribí el valor inicial de s.");
                double r0 = parsearNumero(campoR0.getText().trim(), "r0");
                double s0 = parsearNumero(campoS0.getText().trim(), "s0");
                double epsB = leerEpsilon();
                int maxIterB = leerMaxIter();
                List<RaizCompleja> raicesB = null;
                try {
                    raicesB = BairstowSolver.bairstow(p, r0, s0, epsB, maxIterB);
                } catch (ArithmeticException ex) {
                    g.setNota("Bairstow no convergió: no hay raíces que marcar.");
                }
                if (raicesB != null) {
                    List<Double> marcadas = new ArrayList<>();
                    StringBuilder complejas = new StringBuilder();
                    for (RaizCompleja z : raicesB) {
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
                            complejas.append(GraficoPolinomio.num(z.getReal())).append(" ± ")
                                     .append(GraficoPolinomio.num(z.getImaginaria())).append("i");
                        }
                    }
                    String nota = marcadas.isEmpty() ? "No hay raíces reales." : "";
                    if (complejas.length() > 0)
                        nota += (nota.isEmpty() ? "" : "  ") + "Complejas (no se dibujan): " + complejas;
                    g.setNota(nota);
                }
            }
            if (esSturm()) {   
                for (double[] iv : sturmIntervalos)
                    g.agregarBanda(iv[0], iv[1], GraficoPolinomio.VERDE);
                for (double r : sturmRaicesExactas)   // raices exactas encontradas: punto rojo
                    g.agregarPunto(r, "(" + fraccionTexto(r) + ", 0)", GraficoPolinomio.ROJO);
                g.setNota("Cotas: " + sturmTextoCotas + "     "
                        + (sturmIntervalos.isEmpty() ? "No hay raíces reales en los puntos elegidos."
                           : "Verde: en cada intervalo hay una raíz."));
            }
            // Horner / Ruffini: solo la curva

            g.ajustarRango();
            Stage ventana = new Stage();
            ventana.setTitle("Gráfico – " + metodo.getValue());
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

            if (i > 0 && Math.abs(xi - anterior) < eps && Math.abs(valor) < eps) return xi;
            if (Math.abs(valor) == 0) return xi;
            if (i == maxIter) return Double.NaN;

            double siguiente;
            if (halley) {
                double den = 2.0 * deriv * deriv - valor * segunda.evaluar(xi);
                if (Math.abs(den) < 1e-24) {
                    if (Math.abs(valor) < eps || Math.abs(xi - anterior) < eps) {
                        return xi;
                    }
                    return Double.NaN;
                }
                siguiente = xi - (2.0 * valor * deriv) / den;
            } else {
                if (Math.abs(deriv) < 1e-14) {
                    if (Math.abs(valor) < eps) return xi;
                    return Double.NaN;
                }
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

    // =====================- INICIO: raices enteras y racionales =====================
    // - antes este metodo calculaba todo con BigInteger (horner, esRaizRacional, escalaEntera,
    // - divisores, mcd); ahora solo muestra los pasos que guarda AnalisisPolinomio
    /**
     * Raices enteras y racionales de P(x). Todo lo calcula AnalisisPolinomio;
     * aca solo se muestran los pasos que guarda (getTablaEnteras, getTablaRacionales, divisores...).
     *  1) Enteras: a0, D = divisores de a0, evaluacion por Horner y polinomio factorizado.
     *  2) Racionales: a0 y an, D(a0), D(an), candidatos p/q no enteros, evaluacion por Horner
     *     y factorizacion.
     */
    private void raicesPolinomio(Polinomio p) {
        if (esConstante(p))
            throw new ArithmeticException("P(x) es constante: no tiene raíces");
        //- limite: AnalisisPolinomio busca los divisores probando uno por uno: con coeficientes muy grandes tardaria demasiado
        for (double c : p.getCoeficientes())
            if (Math.abs(c) > 1e8)
                throw new IllegalArgumentException(
                        "Los coeficientes son demasiado grandes (más de 100.000.000) para buscar raíces enteras y racionales");

        // - calculo: lo hace AnalisisPolinomio y se leen los pasos guardados
        AnalisisPolinomio analisis = new AnalisisPolinomio();
        List<Integer> enteras = analisis.raicesEnteras(p);
        Polinomio entero = analisis.getPolinomioEntero();          // P(x) con coeficientes enteros
        if (entero == null)
            throw new IllegalArgumentException(
                    "Los coeficientes no son racionales (por ejemplo tienen sen, ln, π...): "
                    + "no se pueden analizar raíces enteras ni racionales");
        Polinomio sinCeros = analisis.getPolinomioSinCeros();      // sin el factor x^m
        int m = analisis.getMultiplicidadCero();
        List<Integer> divA0 = new ArrayList<>(analisis.getDivisoresA0());
        List<double[]> pruebasEnteras = new ArrayList<>(analisis.getTablaEnteras());
        List<Double> racionales = analisis.raicesRacionales(p);
        boolean hayMas = sinCeros.getGrado() > 0;                  // todavia tiene x
        double a0 = sinCeros.getCoeficiente(sinCeros.getGrado());
        double an = sinCeros.getCoeficiente(0);

        // ---------------- Raices enteras ----------------
        panelResultado.getChildren().add(tituloSeccion("Raíces enteras de un Polinomio"));
        panelResultado.getChildren().add(textoMono("P(x) = " + texto(p)));
        if (entero != p)   // AnalisisPolinomio lo multiplico por k para que tenga coeficientes enteros
            panelResultado.getChildren().add(textoMono("Con coeficientes enteros: "
                    + formato(entero.getCoeficiente(0) / p.getCoeficiente(0)) + " · P(x) = " + texto(entero)));
        if (m > 0)
            panelResultado.getChildren().add(textoMono(
                    "El término independiente es 0: x = 0 es raíz (multiplicidad " + m
                    + "). Se saca factor x" + (m > 1 ? "^" + m : "") + "."));

        if (hayMas) {
            panelResultado.getChildren().add(filaSub("a_{0} =", formato(a0)));
            panelResultado.getChildren().add(filaSub("D =", llave(divA0)));
            panelResultado.getChildren().add(negrita("Evaluando por Horner"));
            FlowPane evaluaciones = new FlowPane(30, 6);
            for (double[] prueba : pruebasEnteras)   //pruebas guardadas por AnalisisPolinomio
                evaluaciones.getChildren().add(celdaPrueba(formato(prueba[0]), prueba[1] == 1));
            panelResultado.getChildren().add(evaluaciones);
        }

        //factorizado con OperacionesPolinomios.dividirHorner (antes horner con BigInteger)
        List<String> factores = new ArrayList<>();
        if (m > 0) factores.add(m == 1 ? "x" : "x^{" + m + "}");
        Polinomio q = sinCeros;
        for (int e : enteras) {
            if (e == 0) continue;   // el 0 ya esta en x^m
            int mult = 0;
            while (q.getGrado() > 0 && esRaizDe(q, e)) {
                q = OperacionesPolinomios.dividirHorner(q, -e).cociente;
                mult++;
            }
            if (mult > 0) {
                String f = "(x " + (e < 0 ? "+ " + (-e) : "- " + e) + ")";
                factores.add(mult == 1 ? f : f + "^{" + mult + "}");
            }
        }
        if (factores.isEmpty()) {
            panelResultado.getChildren().add(textoMono("P(x) no tiene raíces enteras."));
        } else {
            String prefijo = "";
            String cola = "";
            if (esConstante(q)) {
                double c = q.getCoeficiente(0);
                if (Math.abs(c + 1) < 1e-9) prefijo = "-";
                else if (Math.abs(c - 1) > 1e-9) prefijo = formato(c) + " ";
            } else {
                cola = " (" + texto(q) + ")";
            }
            panelResultado.getChildren().add(
                    formula("P(x) = " + prefijo + String.join(" ", factores) + cola, 20));
        }

        if (!hayMas) return;   // nada mas que analizar

        // ---------------- Raices racionales ----------------
        List<Integer> divAn = analisis.getDivisoresAn();
        List<double[]> pruebasRacionales = analisis.getTablaRacionales();

        panelResultado.getChildren().add(new Separator());
        panelResultado.getChildren().add(tituloSeccion("Raíces Racionales de un Polinomio"));
        panelResultado.getChildren().add(filaSub("a_{0} =", formato(a0)));
        panelResultado.getChildren().add(filaSub("a_{n} =", formato(an)));

        if (divAn.isEmpty()) {   //AnalisisPolinomio no busca fracciones si an = 1 o -1
            panelResultado.getChildren().add(textoMono(
                    "a_n = ±1: las raíces racionales son enteras. P(x) no tiene raíces racionales (no enteras)."));
            return;
        }
        panelResultado.getChildren().add(filaSub("D_{a0} =", llave(divA0)));
        panelResultado.getChildren().add(filaSub("D_{an} =", llave(divAn)));

        // candidatos: divisores de a0 y fracciones p/q que probo AnalisisPolinomio
        List<String> todos = new ArrayList<>();
        for (int d : divA0) todos.add(String.valueOf(d));
        for (double[] prueba : pruebasRacionales) todos.add(analisis.aFraccion(prueba[0]));
        panelResultado.getChildren().add(filaSub("D =", "{" + String.join(", ", todos) + "}"));

        if (pruebasRacionales.isEmpty()) {
            panelResultado.getChildren().add(textoMono("P(x) no tiene raíces racionales."));
            return;
        }

        panelResultado.getChildren().add(negrita("Analizando por Horner"));
        final int MAX_MOSTRAR = 300;
        FlowPane evaluaciones = new FlowPane(30, 6);
        for (int i = 0; i < pruebasRacionales.size() && i < MAX_MOSTRAR; i++) {
            double[] prueba = pruebasRacionales.get(i);
            evaluaciones.getChildren().add(celdaPrueba(analisis.aFraccion(prueba[0]), prueba[1] == 1));
        }
        panelResultado.getChildren().add(evaluaciones);
        if (pruebasRacionales.size() > MAX_MOSTRAR)
            panelResultado.getChildren().add(textoMono(
                    "(se muestran los primeros " + MAX_MOSTRAR + " de " + pruebasRacionales.size() + " candidatos)"));

        // Factorizado: P(x) = (x - p/q) ... C(x), dividiendo por Horner por cada raiz racional
        if (racionales.isEmpty()) {
            panelResultado.getChildren().add(textoMono("P(x) no tiene raíces racionales."));
            return;
        }
        List<String> encontradas = new ArrayList<>();
        for (double x : racionales) encontradas.add(analisis.aFraccion(x));
        panelResultado.getChildren().add(textoMono("Raíces racionales: " + String.join(", ", encontradas)));

        Polinomio actual = p;
        List<String> factoresRac = new ArrayList<>();
        for (double x : racionales) {
            int mult = 0;
            while (actual.getGrado() > 0 && esRaizDe(actual, x)) {
                actual = OperacionesPolinomios.dividirHorner(actual, -x).cociente;
                mult++;
            }
            if (mult > 0) {
                String f = "(x " + (x < 0 ? "+ " : "- ") + analisis.aFraccion(Math.abs(x)) + ")";
                factoresRac.add(mult == 1 ? f : f + "^{" + mult + "}");
            }
        }

        if (factoresRac.isEmpty()) {
            panelResultado.getChildren().add(textoMono("No se pudo armar la factorización."));
        } else {
            String cociente;
            if (actual.getGrado() == 0) {
                double c0 = actual.getCoeficiente(0);
                if (Math.abs(c0 - 1) < 1e-9) cociente = "";
                else if (Math.abs(c0 + 1) < 1e-9) cociente = " · (-1)";
                else cociente = " · (" + texto(actual) + ")";
            } else {
                cociente = " · (" + texto(actual) + ")";
            }
            panelResultado.getChildren().add(
                    formula("P(x) = " + String.join(" ", factoresRac) + cociente, 20));
        }
    }

    // ---------- Auxiliares de raices enteras / racionales (solo para mostrar) ----------

    /** true si x es raiz de p: el resto de dividir por (x - raiz) con Horner es ~0. */
    private static boolean esRaizDe(Polinomio p, double raiz) {
        double tam = 1;
        for (double cf : p.getCoeficientes()) tam = Math.max(tam, Math.abs(cf));
        return Math.abs(OperacionesPolinomios.dividirHorner(p, -raiz).resto) <= 1e-9 * tam;
    }

    /** "P(c) = 0" en negrita o "P(c) ≠ 0". */
    private Label celdaPrueba(String candidato, boolean esRaiz) {
        Label l = textoCelda("P(" + candidato + ") " + (esRaiz ? "=" : "≠") + " 0");
        if (esRaiz) l.setStyle(FUENTE_MONO + "-fx-font-weight: bold;");
        return l;
    }

    /** {1, -1, 2, -2}: la lista tal como la devuelve AnalisisPolinomio. */
    private static String llave(List<Integer> numeros) {
        List<String> t = new ArrayList<>();
        for (int d : numeros) t.add(String.valueOf(d));
        return "{" + String.join(", ", t) + "}";
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

    // ------------------------------------------------------------------
    // Newton / Halley (RaicesSolver)
    // ------------------------------------------------------------------

    /** Resultado final del metodo (RaicesSolver). Lanza excepcion si no converge. */
    private static RaizResult resolver(Polinomio p, double x0, double eps, int maxIter, boolean halley) {
        return halley ? RaicesSolver.halleyPolinomio(p, x0, eps, maxIter)
                      : RaicesSolver.newtonPolinomio(p, x0, eps, maxIter);
    }

    /**
     * Un solo paso del metodo: x_{i+1} a partir de x_i, calculado por RaicesSolver.
     * Con epsilon infinito y maxIter = 1, RaicesSolver corta despues de la primera iteracion.
     * Lanza excepcion si el metodo no puede seguir (derivada ~ 0, NaN o infinito).
     */
    private static double siguienteX(Polinomio p, double xi, boolean halley) {
        return resolver(p, xi, Double.POSITIVE_INFINITY, 1, halley).getValorRaiz();
    }

    /**
     * Metodos iterativos Newton y Halley. Arma la tabla:
     *   Newton: i | xi | P(xi) | P'(xi) | |xi - x(i-1)|
     *   Halley: i | xi | P(xi) | P'(xi) | P''(xi) | |xi - x(i-1)|
     * (en la primera fila no hay x anterior, por eso se muestra "-").
     * Cada x(i+1) lo calcula RaicesSolver; aca solo se muestran los valores.
     */
    private void metodoIterativo(Polinomio p, boolean halley) {
        String nombre = halley ? "Halley" : "Newton";

        // --- Parametros ---
        if (campoX0.getText().trim().isEmpty())
            throw new IllegalArgumentException("Falta x0: escribí el valor inicial.");
        if (campoEps.getText().trim().isEmpty())
            throw new IllegalArgumentException("Falta epsilon: escribí la tolerancia.");
        if (campoIter.getText().trim().isEmpty())
            throw new IllegalArgumentException("Falta el máximo de iteraciones.");
        double x0 = parsearNumero(campoX0.getText().trim(), "x0");
        double epsilon;
        int maxIter;
        try {
            epsilon = Double.parseDouble(campoEps.getText().trim().replace(',', '.'));
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("epsilon no es un número válido");
        }
        try {
            maxIter = Integer.parseInt(campoIter.getText().trim());
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("El máximo de iteraciones debe ser un entero");
        }
        if (epsilon <= 0) throw new IllegalArgumentException("epsilon debe ser mayor que 0");
        if (maxIter < 1) throw new IllegalArgumentException("El máximo de iteraciones debe ser al menos 1");
        if (esConstante(p))
            throw new ArithmeticException("P(x) es constante: no hay raíz que buscar con " + nombre);

        Polinomio segunda = p.derivar().derivar(); // P''(x), solo para la columna de Halley

        // --- Titulo, P(x) y formula ---
        Label lMetodo = new Label("Algoritmo: " + nombre);
        lMetodo.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: "
                + COLOR_ENCABEZADO + ";");
        panelResultado.getChildren().add(lMetodo);
        panelResultado.getChildren().add(textoMono("P(x) = " + texto(p)));
        panelResultado.getChildren().add(halley ? formulaHalley() : formulaNewton());
        panelResultado.getChildren().add(formula(
                "Corte: |x_{i} - x_{i-1}| < ε   y   |P(x_{i})| < ε", 18));

        // --- Tabla ---
        GridPane t = new GridPane();
        String[] encabezados = halley
                ? new String[]{"i", "x_{i}", "|P(x_{i})|", "P'(x_{i})", "P''(x_{i})", "|x_{i} - x_{i-1}|"}
                : new String[]{"i", "x_{i}", "|P(x_{i})|", "P'(x_{i})", "|x_{i} - x_{i-1}|"};
        for (int c = 0; c < encabezados.length; c++)
            t.add(celdaNewton(formula(encabezados[c], 18), 0, c), c, 0);

        double xi = x0;
        double xAnterior = x0;
        boolean convergio = false;
        String motivoCorte = null;
        int fila = 1;

        for (int i = 0; i <= maxIter; i++) {
            // Solo para mostrar las columnas: P(xi) y P'(xi) con Horner doble
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

            // Condición estricta: tanto el cambio en x como el valor de P deben ser menores a epsilon
            if (i > 0 && dif < epsilon && Math.abs(valor) < epsilon) {   
                convergio = true;
                break;
            }
            if (valor == 0) {              
                convergio = true;
                break;
            }
            if (i == maxIter) break;

            double siguiente;
            if (halley) {
                double den = 2.0 * deriv * deriv - valor * deriv2;
                if (Math.abs(den) < 1e-24) {
                    if (Math.abs(valor) < epsilon || Math.abs(xi - xAnterior) < epsilon) {
                        convergio = true;
                        break;
                    }
                    motivoCorte = "El denominador de Halley ≈ 0 en x = " + formatoNewton(xi)
                            + ": el método no puede continuar. Probá con otro x0.";
                    break;
                }
                siguiente = xi - (2.0 * valor * deriv) / den;
            } else {
                if (Math.abs(deriv) < 1e-14) {
                    if (Math.abs(valor) < epsilon) {
                        convergio = true;
                        break;
                    }
                    motivoCorte = "P'(x) ≈ 0 en x = " + formatoNewton(xi)
                            + ": el método no puede continuar. Probá con otro x0.";
                    break;
                }
                siguiente = xi - valor / deriv;
            }
            xAnterior = xi;
            xi = siguiente;
        }

        panelResultado.getChildren().add(t);

        // --- Conclusion: el resultado final lo da RaicesSolver ---
        if (convergio) {
            RaizResult rr = resolver(p, x0, epsilon, maxIter, halley);
            panelResultado.getChildren().add(textoMono(
                    "Raíz aproximada: x ≈ " + formatoNewton(xi)
                    + "   (" + (fila - 2) + " iteraciones)"));
        } else if (motivoCorte != null) {
            panelResultado.getChildren().add(textoMono(motivoCorte));
        } else {
            panelResultado.getChildren().add(textoMono(
                    "No convergió en " + maxIter + " iteraciones. Probá con otro x0 o más iteraciones."));
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
        if (t.isEmpty()) throw new IllegalArgumentException("Falta epsilon: escribí la tolerancia.");
        double eps;
        try {
            eps = Double.parseDouble(t.replace(',', '.'));
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("epsilon no es un número válido");
        }
        if (eps <= 0) throw new IllegalArgumentException("epsilon debe ser mayor que 0");
        return eps;
    }

    private int leerMaxIter() {
        String t = campoIter.getText().trim();
        if (t.isEmpty()) throw new IllegalArgumentException("Falta el máximo de iteraciones.");
        int it;
        try {
            it = Integer.parseInt(t);
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("El máximo de iteraciones debe ser un entero");
        }
        if (it < 1) throw new IllegalArgumentException("El máximo de iteraciones debe ser al menos 1");
        return it;
    }

    /** Numero para la tabla de Bairstow ("-" si no hay valor). */
    private static String celdaNumero(double v) {
        return Double.isNaN(v) ? "-" : formatoNewton(v);
    }

    /**
     * Bairstow con tabla de iteraciones.
     * El calculo lo hace BairstowSolver.bairstow(); la tabla se arma con los datos que guarda
     * (BairstowSolver.getTabla() y getPolinomios()). Aca no se repite el algoritmo.
     *
     * Columnas: k | r | s | dr | ds | |dr/r| | |ds/s|
     * Cada fila k muestra el r y s con los que se calcula ese paso y los dr, ds resultantes
     * (r_{k+1} = r_k + dr). Asi la fila 0 ya trae r0, s0 y sus dr, ds.
     */
    private void metodoBairstow(Polinomio p) {
        if (campoR0.getText().trim().isEmpty())
            throw new IllegalArgumentException("Falta r0: escribí el valor inicial de r.");
        if (campoS0.getText().trim().isEmpty())
            throw new IllegalArgumentException("Falta s0: escribí el valor inicial de s.");
        final double r0 = parsearNumero(campoR0.getText().trim(), "r0");
        final double s0 = parsearNumero(campoS0.getText().trim(), "s0");
        final double epsilon = leerEpsilon();
        final int maxIter = leerMaxIter();
        if (esConstante(p))
            throw new ArithmeticException("P(x) es constante: no hay raíces que buscar con Bairstow");

        // ---- Calculo: lo hace BairstowSolver (si no converge, la tabla igual queda guardada) ----
        List<RaizCompleja> raices = null;
        String error = null;
        try {
            raices = BairstowSolver.bairstow(p, r0, s0, epsilon, maxIter);
        } catch (ArithmeticException ex) {
            error = ex.getMessage();
        }
        List<double[]> tabla = BairstowSolver.getTabla();          // {factor, k, r, s, dr, ds, errR, errS, detCero}
        List<double[]> polinomios = BairstowSolver.getPolinomios(); // P1, P2, ... (y el resto si convergio)

        Label lMetodo = new Label("Algoritmo: Bairstow");
        lMetodo.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: "
                + COLOR_ENCABEZADO + ";");
        panelResultado.getChildren().add(lMetodo);
        panelResultado.getChildren().add(textoMono("P(x) = " + texto(p)));
        panelResultado.getChildren().add(textoMono(
                "Factor cuadrático: x^2 - r x - s     r = r + Δr     s = s + Δs     "
                + "(los factores siguientes siguen desde el r y s del anterior)"));

        // Si convergio, el ultimo polinomio guardado es el resto (no es un factor)
        int cantidadFactores = (error == null) ? polinomios.size() - 1 : polinomios.size();
        int filaTabla = 0;   // posicion en la tabla de BairstowSolver

        for (int f = 1; f <= cantidadFactores; f++) {
            panelResultado.getChildren().add(new Separator());
            panelResultado.getChildren().add(negrita(
                    "Factor " + f + ":  P" + f + "(x) = " + texto(new Polinomio(polinomios.get(f - 1)))));

            GridPane t = new GridPane();
            String[] encabezados = {"k", "r", "s", "Δr", "Δs", "|Δr/r|", "|Δs/s|"};
            for (int c = 0; c < encabezados.length; c++)
                t.add(celdaNewton(formula(encabezados[c], 18), 0, c), c, 0);
            int fila = 1;

            List<String> notas = new ArrayList<>();
            double rFinal = Double.NaN, sFinal = Double.NaN;
            int kFinal = 0;
            // filas de este factor
            while (filaTabla < tabla.size() && (int) tabla.get(filaTabla)[0] == f) {
                double[] d = tabla.get(filaTabla++);
                int k = (int) d[1];
                fila = filaBairstow(t, fila, k, d[2], d[3], d[4], d[5], d[6], d[7]);
                if (d[8] == 1)
                    notas.add("En la iteración " + (k + 1) + " el determinante fue ≈ 0: "
                            + "se suma 1 a r y a s y se reinicia el contador k.");
                rFinal = d[2] + d[4];   // r + dr
                sFinal = d[3] + d[5];   // s + ds
                kFinal = k + 1;
            }

            boolean fallo = (error != null && f == cantidadFactores);   // este factor no convergio
            if (!fallo) filaBairstowFinal(t, fila, kFinal, rFinal, sFinal);   // fila final en negrita
            panelResultado.getChildren().add(t);
            for (String nota : notas) panelResultado.getChildren().add(textoMono(nota));

            if (fallo) {
                panelResultado.getChildren().add(negrita(error
                        + ": el método se detiene acá. Probá con otros r0, s0 o más iteraciones."));
                return;
            }

            panelResultado.getChildren().add(formula("Q(x) = " + factorCuadratico(rFinal, sFinal), 20));
            // Raices del factor: la misma formula que usa BairstowSolver (sirve aunque un factor posterior falle)
            panelResultado.getChildren().add(textoMono(
                    "Raíces del factor: " + unirRaices(BairstowSolver.formulaCuad(rFinal, sFinal))));
            panelResultado.getChildren().add(textoMono(
                    "Cociente: " + texto(new Polinomio(polinomios.get(f)))));
        }

        // Lo que queda al final (grado 2 o 1): sus raices son las ultimas de la lista
        double[] resto = polinomios.get(polinomios.size() - 1);
        List<RaizCompleja> raicesResto = raices.subList(2 * cantidadFactores, raices.size());
        if (resto.length == 3) {
            panelResultado.getChildren().add(textoMono(
                    "Queda " + texto(new Polinomio(resto)) + " (grado 2): fórmula cuadrática."));
            panelResultado.getChildren().add(textoMono("Raíces del factor: " + unirRaices(raicesResto)));
        } else if (resto.length == 2) {
            panelResultado.getChildren().add(textoMono(
                    "Queda " + texto(new Polinomio(resto)) + " (grado 1)."));
            panelResultado.getChildren().add(textoMono("Raíz: " + unirRaices(raicesResto)));
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
            throw new ArithmeticException("P(x) es constante: no tiene raíces que acotar");
        String nombre = metodoCota.getValue();
        if (nombre == null)
            throw new IllegalArgumentException("Falta elegir el m\u00e9todo de cota (Lagrange, Laguerre o Newton).");
        int num = numeroMetodoCota(nombre); // 1 = Lagrange, 2 = Laguerre, 3 = Newton

        CotasDePolinomio cotas = new CotasDePolinomio();

        Label lMetodo = new Label("Cotas de las raíces – método: " + nombre);
        lMetodo.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: "
                + COLOR_ENCABEZADO + ";");
        panelResultado.getChildren().add(lMetodo);
        panelResultado.getChildren().add(textoMono("P(x) = " + texto(p)));
        if (p.getCoeficiente(p.getGrado()) == 0)
            panelResultado.getChildren().add(textoMono(
                    "El término independiente es 0: x = 0 es raíz. Las cotas son para las demás raíces."));

        // 1) Cota superior de las raices positivas: se aplica directo a P(x)
        double supPos = cotas.cotaSuperiorPositiva(p, num);
        mostrarCota("1) Cota superior de las raíces positivas",
                supPos == 0 ? "No hay raíces positivas."
                            : "Cota superior positiva = " + formato(supPos));

        // 2) Cota inferior de las raices positivas: x = 1/t
        double infPos = cotas.cotaInferiorPositiva(p, num);
        mostrarCota("2) Cota inferior de las raíces positivas",
                infPos == 0 ? "No hay raíces positivas."
                            : "Cota inferior positiva = 1 / " + formato(1 / infPos) + " = " + formato(infPos));

        // 3) Cota inferior de las raices negativas: x = -t
        double infNeg = cotas.cotaInferiorNegativa(p, num);
        mostrarCota("3) Cota inferior de las raíces negativas",
                infNeg == 0 ? "No hay raíces negativas."
                            : "Cota inferior negativa = -" + formato(-infNeg) + " = " + formato(infNeg));

        // 4) Cota superior de las raices negativas: x = -1/t
        double supNeg = cotas.cotaSuperiorNegativa(p, num);
        mostrarCota("4) Cota superior de las raíces negativas",
                supNeg == 0 ? "No hay raíces negativas."
                            : "Cota superior negativa = -1 / " + formato(-1 / supNeg) + " = " + formato(supNeg));

        panelResultado.getChildren().add(new Separator());
        panelResultado.getChildren().add(tituloSeccion("Intervalos"));
        panelResultado.getChildren().add(negrita(supPos == 0 ? "No hay ra\u00edces positivas"
                : "Ra\u00edces positivas en [" + redondear4(infPos) + ", " + redondear4(supPos) + "]"));
        panelResultado.getChildren().add(negrita(infNeg == 0 ? "No hay ra\u00edces negativas"
                : "Ra\u00edces negativas en [" + redondear4(infNeg) + ", " + redondear4(supNeg) + "]"));
    }

   
    private static double redondear4(double x) {
        return Math.round(x * 10000) / 10000.0;
    }

    // ==================== INICIO: Sturm =====================

    /** intervalos con raiz del ultimo calculo de Sturm (para el grafico): {a, b} */
    private List<double[]> sturmIntervalos = new ArrayList<>();
    /** raices exactas encontradas en los puntos del ultimo calculo de Sturm (en rojo en el grafico) */
    private List<Double> sturmRaicesExactas = new ArrayList<>();
    /** texto de las cotas del ultimo calculo de Sturm (para el grafico) */
    private String sturmTextoCotas = "";
    /** cotas usadas en el ultimo calculo de Sturm (para el grafico) */
    private List<Double> sturmCotas = new ArrayList<>();

    /**
     * Sucesion de Sturm (SturmSolver) y tabla de signos.
     * Primero se calculan las cotas (CotasDePolinomio, con el metodo elegido); los puntos alfa son
     * las cotas y los enteros que hay entre ellas, mas los que se escriban en "Puntos" (para refinar).
     */
    private void metodoSturm(Polinomio p) {
        if (esConstante(p))
            throw new ArithmeticException("P(x) es constante: no tiene raíces");

        SturmSolver st = new SturmSolver();
        ArrayList<Polinomio> suc = st.generarSucesion(p);            // la que se usa para contar
        ArrayList<Polinomio> original = st.getSucesionOriginal();
        Polinomio mcd = st.getMcd();

        Label lMetodo = new Label("Sucesión de Sturm");
        lMetodo.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: " + COLOR_ENCABEZADO + ";");
        panelResultado.getChildren().add(lMetodo);
        panelResultado.getChildren().add(textoMono("P(x) = " + texto(p)));

        // ---- 1) Sucesion ----
        for (int i = 0; i < original.size(); i++)
            panelResultado.getChildren().add(formula("f_{" + i + "}(x) = " + texto(original.get(i)), 20));
        if (mcd != null) {
            panelResultado.getChildren().add(formula("f_{" + original.size() + "}(x) = 0", 20));
            panelResultado.getChildren().add(negrita("La sucesión termina en 0: P(x) tiene raíces múltiples. "
                    + "Se divide P(x) por f_" + (original.size() - 1) + "(x) = " + texto(mcd) + " y se arma de nuevo:"));
            for (int i = 0; i < suc.size(); i++)
                panelResultado.getChildren().add(formula("f_{" + i + "}(x) = " + texto(suc.get(i)), 20));
        }

        // ---- 2) Puntos alfa ----
        List<Double> puntos = new ArrayList<>();
        sturmCotas = new ArrayList<>();
        // primero SIEMPRE las cotas: los puntos de la tabla salen de ahi
        String nombre = metodoCota.getValue();
         int num = numeroMetodoCota(nombre); ;   // 1 = Lagrange, 2 = Laguerre, 3 = Newton
        CotasDePolinomio cotas = new CotasDePolinomio();
        double supPos = cotas.cotaSuperiorPositiva(p, num);
        double infPos = cotas.cotaInferiorPositiva(p, num);
        double infNeg = cotas.cotaInferiorNegativa(p, num);
        double supNeg = cotas.cotaSuperiorNegativa(p, num);
        String txtPos = supPos == 0 ? "sin raíces positivas"
                : fraccionTexto(infPos) + " < raíces positivas < " + fraccionTexto(supPos);
        String txtNeg = infNeg == 0 ? "sin raíces negativas"
                : fraccionTexto(infNeg) + " < raíces negativas < " + fraccionTexto(supNeg);
        panelResultado.getChildren().add(textoMono("Cotas (" + nombre + "):  " + txtPos + "     " + txtNeg));
        sturmTextoCotas = txtNeg + "     " + txtPos;   // para el texto del grafico
        if (infNeg != 0) { sturmCotas.add(infNeg); sturmCotas.add(supNeg); }
        if (supPos != 0) { sturmCotas.add(infPos); sturmCotas.add(supPos); }

        // dos intervalos: negativas [infNeg, supNeg] y positivas [infPos, supPos]
        sturmIntervalos = new ArrayList<>();
        List<double[]> tramos = tramosCotas(infNeg, supNeg, infPos, supPos);
        if (tramos.isEmpty()) {
            panelResultado.getChildren().add(negrita("Seg\u00fan las cotas no hay ra\u00edces reales (salvo quiz\u00e1 x = 0)."));
            if (Math.abs(p.evaluar(0)) < 1e-12)
                panelResultado.getChildren().add(textoMono("P(0) = 0: x = 0 es ra\u00edz."));
            campoPuntos.setDisable(true);
            etiquetaIntervalo.setText("Sin ra\u00edces reales seg\u00fan las cotas");   
            return;
        }
        etiquetaIntervalo.setText(textoTramos(infNeg, supNeg, infPos, supPos));   
        campoPuntos.setDisable(false);   // ya hay cotas: se pueden cambiar los puntos

        //campo vacio: se escriben ahi los puntos por defecto (de 0.5 en 0.5 dentro de cada intervalo)
        if (campoPuntos.getText().trim().isEmpty()) {
            campoPuntos.setText(textoPuntosPorDefecto(tramos));
            panelResultado.getChildren().add(textoMono("Puntos por defecto: de 0.5 en 0.5 dentro de cada intervalo"
                    + " (quedan escritos en \"Puntos \u03b1\": pod\u00e9s borrar o agregar y apretar Enter)."));
        }

        // se usan los puntos del campo, solo los validos (dentro de alguno de los intervalos)
        List<String> descartados = new ArrayList<>();
        for (String parte : campoPuntos.getText().trim().split("[,;\\s]+")) {
            if (parte.isEmpty()) continue;
            double a;
            try {
                a = Parser.parse(parte).valorConstante();
            } catch (RuntimeException ex) {
                descartados.add(parte + " (no es un n\u00famero)");
                continue;
            }
            double[] tramo = tramoDe(a, tramos);
            if (tramo == null) {
                descartados.add(parte + " (fuera de las cotas)");
                continue;
            }
            puntos.add(Math.max(tramo[0], Math.min(tramo[1], a)));   // los extremos se escriben redondeados
        }
        if (!descartados.isEmpty())
            panelResultado.getChildren().add(negrita("Descartados: " + String.join(", ", descartados)));

        // ordenar y sacar repetidos ( los puntos que son raiz SE QUEDAN en la tabla, marcados)
        puntos.sort(Double::compare);
        List<Double> usados = new ArrayList<>();
        for (double a : puntos) {
            if (!usados.isEmpty() && Math.abs(usados.get(usados.size() - 1) - a) < 1e-12) continue;
            usados.add(a);
        }
        if (usados.size() < 2) {   //  hacen falta 2 puntos para armar un intervalo
            panelResultado.getChildren().add(negrita("Hacen falta al menos 2 puntos válidos dentro de las cotas ("
                    + textoTramos(infNeg, supNeg, infPos, supPos) + "). Borrá el campo y apretá Enter para usar los puntos por defecto."));
            return;
        }
        boolean[] esRaiz = new boolean[usados.size()];   
        sturmRaicesExactas = new ArrayList<>();
        for (int j = 0; j < usados.size(); j++) {
            esRaiz[j] = Math.abs(p.evaluar(usados.get(j))) < 1e-9;
            if (esRaiz[j]) sturmRaicesExactas.add(usados.get(j));
        }
        boolean ceroEsRaiz = Math.abs(p.evaluar(0)) < 1e-12;
        if (ceroEsRaiz && !sturmRaicesExactas.contains(0.0)) sturmRaicesExactas.add(0.0);
        sturmRaicesExactas.sort(Double::compare);   

        // ---- 3) Tabla de signos y N(alfa) ----
        int[] n = new int[usados.size()];
        for (int j = 0; j < usados.size(); j++) n[j] = st.calcularCambiosDeSigno(suc, usados.get(j));

        final String ROJO_RAIZ = "-fx-text-fill: #d62728; -fx-font-weight: bold;";   
        GridPane t = new GridPane();
        t.add(celdaNewton(textoCelda(""), 0, 0), 0, 0);
        for (int j = 0; j < usados.size(); j++) {
            t.add(celdaNewton(textoCelda(fraccionTexto(usados.get(j))), 0, j + 1), j + 1, 0);
        }
        for (int i = 0; i < suc.size(); i++) {
            t.add(celdaNewton(formula("f_{" + i + "}(x)", 18), i + 1, 0), 0, i + 1);
            for (int j = 0; j < usados.size(); j++) {
                double v = suc.get(i).evaluar(usados.get(j));
                String signo = Math.abs(v) < 1e-12 || (i == 0 && esRaiz[j]) ? "0" : (v > 0 ? "+" : "−");
                Label celdaSigno = textoCelda(signo);
                if (i == 0 && esRaiz[j]) celdaSigno.setStyle(FUENTE_MONO + ROJO_RAIZ);   //f0 = 0 en rojo
                t.add(celdaNewton(celdaSigno, i + 1, j + 1), j + 1, i + 1);
            }
        }
        int filaN = suc.size() + 1;
        t.add(celdaNewton(formula("N(α)", 18), filaN, 0), 0, filaN);
        for (int j = 0; j < usados.size(); j++) {
            Label l = textoCelda(String.valueOf(n[j]));
            l.setStyle(FUENTE_MONO + "-fx-font-weight: bold;");
            t.add(celdaNewton(l, filaN, j + 1), j + 1, filaN);
        }
        panelResultado.getChildren().add(t);

        // ---- 4) Raices exactas y raices en intervalos ----
        //  una raiz exacta en el punto b se cuenta en (a, b]; como ya se conoce, se descuenta del intervalo
        for (int j = 0; j < usados.size(); j++)
            if (esRaiz[j]) {
                panelResultado.getChildren().add(negrita("✔ x = " + fraccionTexto(usados.get(j)) + " es raíz exacta (P("
                        + fraccionTexto(usados.get(j)) + ") = 0)."));
            }
        if (ceroEsRaiz) {
            panelResultado.getChildren().add(negrita("✔ x = 0 es raíz exacta (P(0) = 0)."));
        }
        sturmIntervalos = new ArrayList<>();
        List<String> sugeridos = new ArrayList<>();   
        List<String> textoIntervalos = new ArrayList<>();
        int enIntervalos = 0;
        for (int j = 0; j + 1 < usados.size(); j++) {
            double ia = usados.get(j), ib = usados.get(j + 1);
            int d = Math.abs(n[j] - n[j + 1]);
            if (esRaiz[j + 1]) d--;                                   // la raiz exacta b ya se conoce
            if (ceroEsRaiz && ia < 0 && ib > 0) d--;                  // el 0 ya se conoce
            if (d <= 0) continue;
            enIntervalos += d;
            sturmIntervalos.add(new double[]{ia, ib});
            textoIntervalos.add("(" + fraccionTexto(ia) + ", " + fraccionTexto(ib) + ")");
            String txt = "|N(" + fraccionTexto(ia) + ") - N(" + fraccionTexto(ib) + ")| = " + Math.abs(n[j] - n[j + 1])
                    + "   → hay " + d + (d == 1 ? " raíz" : " raíces") + " entre " + fraccionTexto(ia) + " y " + fraccionTexto(ib)
                    + (esRaiz[j + 1] ? " (además de la raíz exacta " + fraccionTexto(ib) + ")" : "");
            if (d > 1) {   
                List<String> sug = new ArrayList<>();
                double medio = SturmSolver.puntoMedio(p, ia, ib);
                if (tramoDe(medio, tramos) != null) {
                    sug.add(fraccionTexto(medio));
                } else {   // el medio cae entre las cotas (no valido): se sugieren los extremos de las cotas
                    for (double[] tr : tramos)
                        for (double e : tr)
                            if (e > ia + 1e-9 && e < ib - 1e-9 && Math.abs(p.evaluar(e)) > 1e-9) sug.add(fraccionTexto(e));
                }
                sugeridos.addAll(sug);
                txt += sug.isEmpty() ? ": agregá más puntos para separarlas"
                        : ": agregá " + String.join(", ", sug) + " para separarlas";
            }
            panelResultado.getChildren().add(negrita(txt));
        }
        if (!sugeridos.isEmpty())   
            panelResultado.getChildren().add(negrita("Sugerencia: agregá " + String.join(", ", sugeridos)
                    + " en \"Puntos α\" y apretá Enter."));

        // resumen por cota: cuantas raices hay en el intervalo de las negativas y en el de las positivas
        panelResultado.getChildren().add(new Separator());
        panelResultado.getChildren().add(negrita("Raíces por cota"));
        for (double[] tr : tramos) {
            int na = st.calcularCambiosDeSigno(suc, tr[0]);
            int nb = st.calcularCambiosDeSigno(suc, tr[1]);
            String tipo = tr[1] <= 0 ? "negativas" : "positivas";
            panelResultado.getChildren().add(textoMono("Raíces " + tipo + " en [" + fraccionTexto(tr[0]) + ", " + fraccionTexto(tr[1])
                    + "]:  |N(" + fraccionTexto(tr[0]) + ") - N(" + fraccionTexto(tr[1]) + ")| = |" + na + " - " + nb + "| = " + Math.abs(na - nb)));
        }
        if (infNeg == 0) panelResultado.getChildren().add(textoMono("Raíces negativas: no hay (según las cotas)"));
        if (supPos == 0) panelResultado.getChildren().add(textoMono("Raíces positivas: no hay (según las cotas)"));

        // resumen final: las que ya estan y las que falta buscar
        List<String> exactas = new ArrayList<>();
        for (double r : sturmRaicesExactas) exactas.add(fraccionTexto(r));
        panelResultado.getChildren().add(textoMono("Raíces exactas: " + (exactas.isEmpty() ? "ninguna" : String.join(", ", exactas))));
        panelResultado.getChildren().add(textoMono("Raíces en intervalos (para buscar con Newton): "
                + (textoIntervalos.isEmpty() ? "ninguna" : String.join(", ", textoIntervalos))));
        panelResultado.getChildren().add(negrita("Total de raíces reales distintas: " + (exactas.size() + enIntervalos)
                + (mcd != null ? "   (P(x) tiene raíces múltiples: se cuentan una sola vez)" : "")));
    }

    
    private static void agregarRango(List<Double> puntos, double desde, double hasta) {
        double paso = pasoRango(desde, hasta);
        puntos.add(desde);
        for (double k = (Math.floor(desde / paso) + 1) * paso; k < hasta - 1e-9; k += paso)
            if (k > desde + 1e-9) puntos.add(Math.round(k / paso) * paso);   // sin ruido de decimales
        puntos.add(hasta);
    }

    /** paso de los puntos por defecto: 0.5, o uno mayor si quedarian demasiados. */
    private static double pasoRango(double desde, double hasta) {
        final int MAX_PUNTOS = 40;
        double paso = 0.5;
        if ((hasta - desde) / paso > MAX_PUNTOS) {
            double crudo = (hasta - desde) / MAX_PUNTOS;
            double magnitud = Math.pow(10, Math.floor(Math.log10(crudo)));
            for (double m : new double[]{1, 2, 5, 10}) {
                paso = m * magnitud;
                if (paso >= crudo) break;
            }
        }
        return paso;
    }

    /*borra los puntos escritos y desactiva el campo (las cotas cambiaron o no se calcularon). */
    private void desactivarPuntos() {
        if (campoPuntos == null) return;
        campoPuntos.clear();
        campoPuntos.setDisable(true);
        if (etiquetaIntervalo != null) etiquetaIntervalo.setText("");
    }

    /**
     * boton "Calcular cotas": calcula las cotas de P(x) con el metodo elegido, muestra el
     * intervalo al lado del boton y escribe los puntos por defecto en "Puntos alfa" (editables).
     */
    private void verIntervalo() {
        error.setText("");
        try {
            String t = campo.getText().trim();
            if (t.isEmpty()) throw new IllegalArgumentException("Falta P(x): escrib\u00ed un polinomio.");
            Polinomio p = parsear(t, "P(x)");
            if (esConstante(p)) throw new ArithmeticException("P(x) es constante: no tiene ra\u00edces");
            int num = numeroMetodoCota(metodoCota.getValue());
            CotasDePolinomio cotas = new CotasDePolinomio();
            double supPos = cotas.cotaSuperiorPositiva(p, num);
            double infPos = cotas.cotaInferiorPositiva(p, num);
            double infNeg = cotas.cotaInferiorNegativa(p, num);
            double supNeg = cotas.cotaSuperiorNegativa(p, num);
            List<double[]> tramos = tramosCotas(infNeg, supNeg, infPos, supPos);
            if (tramos.isEmpty()) {
                etiquetaIntervalo.setText("Sin ra\u00edces reales seg\u00fan las cotas");
                campoPuntos.setDisable(true);
                return;
            }
            etiquetaIntervalo.setText(textoTramos(infNeg, supNeg, infPos, supPos));
            campoPuntos.setDisable(false);
            campoPuntos.setText(textoPuntosPorDefecto(tramos));   // los puntos por defecto quedan a la vista
            campoPuntos.requestFocus();
        } catch (RuntimeException ex) {
            error.setText("Error: " + (ex.getMessage() != null ? ex.getMessage() : ex.toString()));
        }
    }

    /** los intervalos de las cotas: [infNeg, supNeg] (si hay negativas) y [infPos, supPos] (si hay positivas). */
    private static List<double[]> tramosCotas(double infNeg, double supNeg, double infPos, double supPos) {
        List<double[]> tramos = new ArrayList<>();
        if (infNeg != 0) tramos.add(new double[]{infNeg, supNeg});
        if (supPos != 0) tramos.add(new double[]{infPos, supPos});
        return tramos;
    }

    /* "Negativas: [-5/2, -2/5]   Positivas: [1/2, 3]" (o "sin negativas" / "sin positivas"). */
    private static String textoTramos(double infNeg, double supNeg, double infPos, double supPos) {
        String neg = infNeg != 0 ? "Negativas: [" + fraccionTexto(infNeg) + ", " + fraccionTexto(supNeg) + "]" : "Sin negativas";
        String pos = supPos != 0 ? "Positivas: [" + fraccionTexto(infPos) + ", " + fraccionTexto(supPos) + "]" : "Sin positivas";
        return neg + "    " + pos;
    }

    /** el intervalo de las cotas que contiene a "a" (con tolerancia por el redondeo), o null si ninguno. */
    private static double[] tramoDe(double a, List<double[]> tramos) {
        double tol = 1e-6 * Math.max(1, Math.abs(a));
        for (double[] t : tramos)
            if (a >= t[0] - tol && a <= t[1] + tol) return t;
        return null;
    }

    /** los puntos por defecto (de 0.5 en 0.5 dentro de cada intervalo) como texto: "-5/2, -2, ...". */
    private static String textoPuntosPorDefecto(List<double[]> tramos) {
        List<Double> pts = new ArrayList<>();
        for (double[] t : tramos) agregarRango(pts, t[0], t[1]);
        List<String> t = new ArrayList<>();
        for (double v : pts) t.add(fraccionTexto(v));
        return String.join(", ", t);
    }

    /**numero como fraccion si es posible (1/2, -1/3), si no con decimales. */
    private static String fraccionTexto(double v) {
        String s = new AnalisisPolinomio().aFraccion(v);
        if (s.contains("/") && s.length() > 9) return formato(v);   // fracciones muy largas: mejor decimal
        return s.contains(".") ? formato(v) : s;
    }

    // =====================  FIN: Sturm =====================
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
    /** Bloque de una cota: titulo y resultado. */
    private void mostrarCota(String titulo, String resultado) {
        panelResultado.getChildren().add(new Separator());
        panelResultado.getChildren().add(negrita(titulo));
        panelResultado.getChildren().add(negrita(resultado));
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
        if (m == 0) {
            if (qc[0] == 0)
                throw new IllegalArgumentException("No se puede dividir por el polinomio nulo (0)");
            throw new IllegalArgumentException(
                    "El divisor Q(x) debe tener al menos grado 1 (no se admite división por una constante en Horner)");
        }

        Polinomio cociente;
        Polinomio resto;
        String metodoTxt;
        int tipo; // 1 = Ruffini, 2 = Horner cuadratico, 0 = sin tabla
        double[] sumas = null; //fila de abajo de la tabla: cociente + resto (lo devuelve OperacionesPolinomios)

        if (m == 1 && n >= 1) {
            OperacionesPolinomios.ResultadoDivisionLineal r =
                    OperacionesPolinomios.dividirHornerGral(p, qc[0], qc[1]);
            cociente = r.cociente;
            resto = new Polinomio(new double[]{r.resto});
            metodoTxt = "Horner / Ruffini";
            tipo = 1;
            // para la tabla: division por (x + beta/alfa) sin dividir por alfa
            OperacionesPolinomios.ResultadoDivisionLineal sinEscalar =
                    OperacionesPolinomios.dividirHorner(p, qc[1] / qc[0]);
            sumas = unir(sinEscalar.cociente.getCoeficientes(), sinEscalar.resto);   
        } else if (m == 2 && n >= 3 && Math.abs(qc[0] - 1) < 1e-12) {
            OperacionesPolinomios.ResultadoDivisionCuadratica r =
                    OperacionesPolinomios.dividirHornerCuadratico(p, qc[1], qc[2]);
            cociente = r.cociente;
            resto = new Polinomio(new double[]{r.resto1, r.resto0});
            metodoTxt = "Horner / Ruffini doble ";
            tipo = 2;
            sumas = unir(r.cociente.getCoeficientes(), r.resto1, r.resto0);   
        } else {
            OperacionesPolinomios.ResultadoDivisionGeneral r =
                    OperacionesPolinomios.divisionGeneral(p, q);
            cociente = r.cociente;
            resto = r.resto;
            metodoTxt = "División general";
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
            agregarTablaRuffini(p, q, sumas);           
        } else if (tipo == 2) {
            agregarTablaHornerCuadratico(p, q, sumas);  
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

    /**
     * Tabla de Horner / Ruffini para un divisor lineal alfa*x + beta.
     *  "abajo" (cociente + resto) ya viene calculado por OperacionesPolinomios;
     * aca solo se arma la fila de productos para mostrarla.
     */
    private void agregarTablaRuffini(Polinomio p, Polinomio q, double[] abajo) {
        double[] a = p.getCoeficientes();
        double[] qc = q.getCoeficientes();
        double r = -qc[1] / qc[0];          // raiz del divisor
        int n = a.length;

        double[] medio = new double[n];     // productos
        for (int i = 1; i < n; i++)
            medio[i] = abajo[i - 1] * r;    // solo el producto que se muestra (las sumas no se recalculan)

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

    /**
     * Tabla de Horner para dividir entre x^2 + a x + b.
     * 
     * aca solo se arman las filas de productos para mostrarlas.
     */
    private void agregarTablaHornerCuadratico(Polinomio p, Polinomio q, double[] s) {
        double[] P = p.getCoeficientes();
        double[] qc = q.getCoeficientes();
        double a = qc[1], b = qc[2];
        int n = P.length - 1;                 // grado de P

        double[] fila1 = new double[n + 1];   // productos de -a
        double[] fila2 = new double[n + 1];   // productos de -b
        for (int j = 1; j <= n - 1; j++) fila1[j] = -a * s[j - 1];   
        for (int j = 2; j <= n; j++)     fila2[j] = -b * s[j - 2];   // (las sumas no se recalculan)

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

    /*une los coeficientes del cociente con el resto: la fila de abajo de la tabla. */
    private static double[] unir(double[] cociente, double... resto) {
        double[] fila = new double[cociente.length + resto.length];
        System.arraycopy(cociente, 0, fila, 0, cociente.length);
        System.arraycopy(resto, 0, fila, cociente.length, resto.length);
        return fila;
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
