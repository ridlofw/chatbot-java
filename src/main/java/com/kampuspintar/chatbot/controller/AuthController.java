package com.kampuspintar.chatbot.controller;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.kampuspintar.chatbot.model.FAQ;
import com.kampuspintar.chatbot.model.KnowledgeBase;
import com.kampuspintar.chatbot.util.ConfigManager;
import com.kampuspintar.chatbot.util.UserSession;
import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.Type;
import java.util.*;
import java.util.stream.Collectors;

public class AuthController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Button loginButton;
    @FXML private Label errorLabel;
    @FXML private Button adminButton;

    private static final String VALID_USERNAME = "admin";
    private static final String VALID_PASSWORD = "admin123";
    private KnowledgeBase adminKnowledgeBase;

    @FXML
    public void initialize() {
        Platform.runLater(() -> usernameField.requestFocus());

        usernameField.textProperty().addListener((obs, o, n) -> hideError());
        passwordField.textProperty().addListener((obs, o, n) -> hideError());
    }

    @FXML
    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        if (authenticate(username, password)) {
            UserSession.getInstance().setUsername(username);
            navigateToChatView();
        } else {
            showError("Username atau password salah.");
            passwordField.clear();
            passwordField.requestFocus();
        }
    }

    // =========================================================================
    // ADMIN PANEL (Dipindahkan dari ChatController)
    // =========================================================================

    @FXML
    private void handleAdminButton() {
        Dialog<String> passwordDialog = new Dialog<>();
        passwordDialog.setTitle("Autentikasi Admin");
        passwordDialog.setHeaderText("🔒 Masukkan password admin");

        ButtonType loginButtonType = new ButtonType("Masuk", ButtonBar.ButtonData.OK_DONE);
        passwordDialog.getDialogPane().getButtonTypes().addAll(loginButtonType, ButtonType.CANCEL);

        VBox dialogContent = new VBox(10);
        dialogContent.setPadding(new Insets(15));
        PasswordField dialogPasswordField = new PasswordField();
        dialogPasswordField.setPromptText("Password admin...");
        dialogPasswordField.setPrefWidth(280);
        dialogContent.getChildren().addAll(new Label("Password:"), dialogPasswordField);
        passwordDialog.getDialogPane().setContent(dialogContent);

        Platform.runLater(dialogPasswordField::requestFocus);

        passwordDialog.setResultConverter(dialogButton -> {
            if (dialogButton == loginButtonType) {
                return dialogPasswordField.getText();
            }
            return null;
        });

        Optional<String> result = passwordDialog.showAndWait();
        result.ifPresent(password -> {
            String adminPassword = ConfigManager.getInstance().getAdminPassword();
            if (adminPassword.equals(password)) {
                openAdminPanel();
            } else {
                showAlert(Alert.AlertType.ERROR, "Akses Ditolak", "Password salah! Silakan coba lagi.");
            }
        });
    }

    @SuppressWarnings("unchecked")
    private void openAdminPanel() {
        try {
            // Inisialisasi knowledge base saat admin panel dibuka
            adminKnowledgeBase = new KnowledgeBase("data/faq.json");

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/kampuspintar/chatbot/admin-view.fxml"));
            VBox adminRoot = loader.load();

            Stage adminStage = new Stage();
            adminStage.initStyle(StageStyle.UTILITY);
            adminStage.initModality(Modality.APPLICATION_MODAL);
            adminStage.setTitle("⚙️ Manajemen FAQ - Admin Panel");

            Scene adminScene = new Scene(adminRoot, 800, 600);
            adminScene.getStylesheets().add(getClass().getResource("/com/kampuspintar/chatbot/styles/dark-theme.css").toExternalForm());
            adminStage.setScene(adminScene);
            adminStage.setMinWidth(700);
            adminStage.setMinHeight(500);

            TableView<FAQ> faqTable = (TableView<FAQ>) adminRoot.lookup("#faqTable");
            TableColumn<FAQ, String> colQuestion = (TableColumn<FAQ, String>) faqTable.getColumns().get(0);
            TableColumn<FAQ, String> colAnswer = (TableColumn<FAQ, String>) faqTable.getColumns().get(1);
            TableColumn<FAQ, String> colKeywords = (TableColumn<FAQ, String>) faqTable.getColumns().get(2);
            TextField faqQuestionField = (TextField) adminRoot.lookup("#faqQuestionField");
            TextArea faqAnswerField = (TextArea) adminRoot.lookup("#faqAnswerField");
            TextField faqKeywordsField = (TextField) adminRoot.lookup("#faqKeywordsField");
            Button addFaqButton = (Button) adminRoot.lookup("#addFaqButton");
            Button editFaqButton = (Button) adminRoot.lookup("#editFaqButton");
            Button deleteFaqButton = (Button) adminRoot.lookup("#deleteFaqButton");
            Button closeFaqButton = (Button) adminRoot.lookup("#closeFaqButton");

            colQuestion.setCellValueFactory(new PropertyValueFactory<>("question"));
            colAnswer.setCellValueFactory(new PropertyValueFactory<>("answer"));
            colKeywords.setCellValueFactory(cellData -> {
                List<String> keywords = cellData.getValue().getKeywords();
                String joined = (keywords != null) ? String.join(", ", keywords) : "";
                return new javafx.beans.property.SimpleStringProperty(joined);
            });

            ObservableList<FAQ> faqData = FXCollections.observableArrayList(adminKnowledgeBase.getAllFAQs());
            faqTable.setItems(faqData);

            faqTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
                if (newSelection != null) {
                    faqQuestionField.setText(newSelection.getQuestion());
                    faqAnswerField.setText(newSelection.getAnswer());
                    String keywordsStr = (newSelection.getKeywords() != null) ? String.join(", ", newSelection.getKeywords()) : "";
                    faqKeywordsField.setText(keywordsStr);
                }
            });

            addFaqButton.setOnAction(event -> {
                String question = faqQuestionField.getText().trim();
                String answer = faqAnswerField.getText().trim();
                String keywordsStr = faqKeywordsField.getText().trim();

                if (question.isEmpty() || answer.isEmpty()) {
                    showAlert(Alert.AlertType.WARNING, "Validasi", "Pertanyaan dan Jawaban tidak boleh kosong!");
                    return;
                }

                List<String> keywords = parseKeywords(keywordsStr);
                FAQ newFaq = new FAQ(question, answer, keywords);
                adminKnowledgeBase.addFAQ(newFaq);

                refreshFaqTable(faqTable);
                clearAdminForm(faqQuestionField, faqAnswerField, faqKeywordsField);
                showAlert(Alert.AlertType.INFORMATION, "Berhasil", "FAQ baru berhasil ditambahkan!");
            });

            editFaqButton.setOnAction(event -> {
                FAQ selectedFaq = faqTable.getSelectionModel().getSelectedItem();
                if (selectedFaq == null) {
                    showAlert(Alert.AlertType.WARNING, "Peringatan", "Pilih FAQ yang ingin diedit terlebih dahulu!");
                    return;
                }

                String question = faqQuestionField.getText().trim();
                String answer = faqAnswerField.getText().trim();
                String keywordsStr = faqKeywordsField.getText().trim();

                if (question.isEmpty() || answer.isEmpty()) {
                    showAlert(Alert.AlertType.WARNING, "Validasi", "Pertanyaan dan Jawaban tidak boleh kosong!");
                    return;
                }

                List<String> keywords = parseKeywords(keywordsStr);
                int index = findFaqIndex(selectedFaq);
                if (index >= 0) {
                    FAQ updatedFaq = new FAQ(question, answer, keywords);
                    adminKnowledgeBase.updateFAQ(index, updatedFaq);

                    refreshFaqTable(faqTable);
                    clearAdminForm(faqQuestionField, faqAnswerField, faqKeywordsField);
                    showAlert(Alert.AlertType.INFORMATION, "Berhasil", "FAQ berhasil diperbarui!");
                } else {
                    showAlert(Alert.AlertType.ERROR, "Error", "FAQ tidak ditemukan dalam knowledge base!");
                }
            });

            deleteFaqButton.setOnAction(event -> {
                FAQ selectedFaq = faqTable.getSelectionModel().getSelectedItem();
                if (selectedFaq == null) {
                    showAlert(Alert.AlertType.WARNING, "Peringatan", "Pilih FAQ yang ingin dihapus terlebih dahulu!");
                    return;
                }

                Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
                confirmDialog.setTitle("Konfirmasi Hapus");
                confirmDialog.setHeaderText("Hapus FAQ?");
                confirmDialog.setContentText("Apakah Anda yakin ingin menghapus FAQ:\n\"" + selectedFaq.getQuestion() + "\"?");

                Optional<ButtonType> confirmResult = confirmDialog.showAndWait();
                if (confirmResult.isPresent() && confirmResult.get() == ButtonType.OK) {
                    int index = findFaqIndex(selectedFaq);
                    if (index >= 0) {
                        adminKnowledgeBase.removeFAQ(index);
                        refreshFaqTable(faqTable);
                        clearAdminForm(faqQuestionField, faqAnswerField, faqKeywordsField);
                        showAlert(Alert.AlertType.INFORMATION, "Berhasil", "FAQ berhasil dihapus!");
                    }
                }
            });

            closeFaqButton.setOnAction(event -> adminStage.close());
            adminStage.show();

        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Gagal membuka panel admin: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void refreshFaqTable(TableView<FAQ> table) {
        ObservableList<FAQ> faqData = FXCollections.observableArrayList(adminKnowledgeBase.getAllFAQs());
        table.setItems(faqData);
        table.refresh();
    }

    private void clearAdminForm(TextField questionField, TextArea answerField, TextField keywordsField) {
        questionField.clear();
        answerField.clear();
        keywordsField.clear();
    }

    private List<String> parseKeywords(String keywordsStr) {
        if (keywordsStr == null || keywordsStr.isEmpty()) {
            return new ArrayList<>();
        }
        return Arrays.stream(keywordsStr.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }

    private int findFaqIndex(FAQ targetFaq) {
        List<FAQ> allFaqs = adminKnowledgeBase.getAllFAQs();
        for (int i = 0; i < allFaqs.size(); i++) {
            FAQ faq = allFaqs.get(i);
            if (faq.getQuestion() != null && faq.getQuestion().equals(targetFaq.getQuestion())
                    && faq.getAnswer() != null && faq.getAnswer().equals(targetFaq.getAnswer())) {
                return i;
            }
        }
        return -1;
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // =========================================================================

    private boolean authenticate(String username, String password) {
        try (Reader reader = new FileReader("data/users.json")) {
            Gson gson = new Gson();
            Type type = new TypeToken<Map<String, String>>(){}.getType();
            Map<String, String> users = gson.fromJson(reader, type);

            // Cek apakah username ada dan passwordnya cocok
            if (users != null && users.containsKey(username)) {
                return users.get(username).equals(password);
            }
        } catch (Exception e) {
            System.err.println("Gagal membaca users.json: " + e.getMessage());
            // Fallback ke akun default jika file gagal dibaca
            if ("admin".equals(username) && "admin123".equals(password)) return true;
        }
        return false;
    }

    private void navigateToChatView() {
        loginButton.setDisable(true);
        loginButton.setText("Memuat...");

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/kampuspintar/chatbot/chat-view.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) loginButton.getScene().getWindow();
            Scene scene = new Scene(root, 900, 650);

            root.setOpacity(0);
            stage.setScene(scene);
            stage.setTitle("Chatbot Kampus Pintar");
            stage.show();

            FadeTransition fade = new FadeTransition(Duration.millis(300), root);
            fade.setFromValue(0);
            fade.setToValue(1);
            fade.play();

        } catch (IOException e) {
            loginButton.setDisable(false);
            loginButton.setText("🔑  Masuk");
            showError("Gagal memuat aplikasi. Silakan coba lagi.");
            e.printStackTrace();
        }
    }

    private void showError(String message) {
        errorLabel.setText("❌  " + message);

        if (!errorLabel.isVisible()) {
            errorLabel.setOpacity(0);
            errorLabel.setVisible(true);
            errorLabel.setManaged(true);

            FadeTransition fade = new FadeTransition(Duration.millis(200), errorLabel);
            fade.setFromValue(0);
            fade.setToValue(1);
            fade.play();
        }
    }

    private void hideError() {
        if (errorLabel.isVisible()) {
            errorLabel.setVisible(false);
            errorLabel.setManaged(false);
        }
    }
}