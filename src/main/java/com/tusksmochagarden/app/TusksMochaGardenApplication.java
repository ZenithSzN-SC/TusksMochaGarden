package com.tusksmochagarden.app;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.InputStream;

public class TusksMochaGardenApplication extends Application {

    private static void loadFonts() {
        String[] fonts = {"Outfit-Regular.ttf", "Outfit-Bold.ttf", "Fraunces-SemiBold.ttf"};
        for (String font : fonts) {
            InputStream is = TusksMochaGardenApplication.class.getResourceAsStream("/com/tusksmochagarden/fonts/" + font);
            if (is != null) {
                Font.loadFont(is, 12);
            }
        }
    }

    @Override
    public void start(Stage stage) throws IOException {
        loadFonts();
        new Thread(SchemaUpdater::ensureSchema, "schema-migration").start();
        FXMLLoader fxmlLoader = new FXMLLoader(TusksMochaGardenApplication.class.getResource("/com/tusksmochagarden/login.fxml"));
        Parent root = fxmlLoader.load();
        Scene scene = new Scene(root);
        stage.setTitle("Tusks Mocha Garden");
        stage.setMinHeight(640);
        stage.setMinWidth(1000);
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}