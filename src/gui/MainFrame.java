package gui;

import javafx.application.Application;
import javafx.stage.Stage;
import javafx.scene.Scene;

import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;


public class MainFrame extends Application {

    @Override
    public void start(Stage primaryStage) {
        // Crear una etiqueta simple
        Label label = new Label("¡Hola Mundo desde JavaFX!");
        
        // Colocar la etiqueta en un panel centralizado
        StackPane root = new StackPane(label);
        
        // Crear la escena de 400x200 píxeles
        Scene scene = new Scene(root, 400, 200);

        primaryStage.setTitle("Prueba de GUI - Raíces de Polinomios");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}