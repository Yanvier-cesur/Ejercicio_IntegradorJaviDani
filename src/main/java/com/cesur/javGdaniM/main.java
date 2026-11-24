package com.cesur.javGdaniM;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

public class main extends Application {

    private static final String FXML_PRINCIPAL = "/vista/principal.fxml";

    @Override
    public void start(Stage primaryStage) throws IOException {
        URL fxmlUrl = getClass().getResource(FXML_PRINCIPAL);
        if (fxmlUrl == null) {
            throw new IllegalStateException("No se encontró el archivo FXML en: " + FXML_PRINCIPAL);
        }

        FXMLLoader loader = new FXMLLoader(fxmlUrl);
        Parent root = loader.load();

        Scene scene = new Scene(root, 1140, 680);
        primaryStage.setTitle("Gestión de Candidaturas v2.0");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}