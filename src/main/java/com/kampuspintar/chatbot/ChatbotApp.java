package com.kampuspintar.chatbot;

import atlantafx.base.theme.CupertinoDark;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.Objects;

/**
 * Kelas utama aplikasi Chatbot Kampus Pintar.
 *
 * Menginisialisasi tema AtlantaFX CupertinoDark sebagai base theme,
 * kemudian memuat layout FXML dan menerapkan custom dark theme CSS
 * untuk tampilan premium yang modern.
 */
public class ChatbotApp extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        // 1. Set AtlantaFX CupertinoDark sebagai user agent stylesheet (base theme)
        Application.setUserAgentStylesheet(new CupertinoDark().getUserAgentStylesheet());

        // 2. Muat layout FXML utama
        FXMLLoader loader = new FXMLLoader(getClass().getResource("auth-view.fxml"));

        // 3. Buat scene dengan ukuran default
        Scene scene = new Scene(loader.load(), 900, 650);

        // 4. Tambahkan custom stylesheet di atas base theme
        scene.getStylesheets().add(
                Objects.requireNonNull(getClass().getResource("styles/dark-theme.css")).toExternalForm()
        );

        // 5. Konfigurasi primary stage
        primaryStage.setTitle("Chatbot Kampus Pintar");
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(800);
        primaryStage.setMinHeight(600);

        // 6. Tampilkan aplikasi
        primaryStage.show();
    }

    /**
     * Entry point utama aplikasi.
     *
     * @param args argumen command line
     */
    public static void main(String[] args) {
        launch(args);
    }
}
