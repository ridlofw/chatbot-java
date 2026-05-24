package com.kampuspintar.chatbot.controller;

import com.kampuspintar.chatbot.model.ChatMessage;
import com.kampuspintar.chatbot.model.FAQ;
import com.kampuspintar.chatbot.model.KnowledgeBase;
import com.kampuspintar.chatbot.strategy.ApiBasedStrategy;
import com.kampuspintar.chatbot.strategy.QuestionProcessor;
import com.kampuspintar.chatbot.strategy.RuleBasedStrategy;
import com.kampuspintar.chatbot.util.ChatHistoryManager;
import com.kampuspintar.chatbot.util.ConfigManager;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Controller utama untuk tampilan chat.
 * Menangani semua logika interaksi pengguna, pengiriman pesan,
 * pergantian strategi, manajemen riwayat, dan panel admin FAQ.
 *
 * @author Kampus Pintar Team
 * @version 1.0
 */
public class ChatController {

    // === Komponen FXML yang di-inject ===
    @FXML private VBox chatContainer;
    @FXML private ScrollPane chatScrollPane;
    @FXML private TextField inputField;
    @FXML private Button sendButton;
    @FXML private ComboBox<String> modeComboBox;
    @FXML private VBox historyContainer;
    @FXML private Button adminButton;

    // === Komponen bisnis logika ===
    /** Processor untuk memproses pertanyaan dengan strategi yang aktif */
    private QuestionProcessor questionProcessor;

    /** Knowledge base berisi daftar FAQ */
    private KnowledgeBase knowledgeBase;

    /** Manager untuk menyimpan/memuat riwayat chat */
    private ChatHistoryManager historyManager;

    /** Daftar semua pesan chat dalam sesi ini */
    private List<ChatMessage> chatMessages;

    /** Strategi rule-based (pencocokan keyword) */
    private RuleBasedStrategy ruleBasedStrategy;

    /** Strategi API-based (Gemini AI) */
    private ApiBasedStrategy apiBasedStrategy;

    /** ID khusus untuk node typing indicator agar bisa dihapus */
    private static final String TYPING_INDICATOR_ID = "typing-indicator-node";

    /** Formatter untuk menampilkan timestamp yang mudah dibaca */
    private static final DateTimeFormatter DISPLAY_FORMATTER =
            DateTimeFormatter.ofPattern("HH:mm, dd MMM yyyy");

    /**
     * Inisialisasi controller. Dipanggil otomatis oleh JavaFX setelah
     * semua komponen FXML selesai di-inject.
     */
    @FXML
    public void initialize() {
        // 1. Inisialisasi KnowledgeBase dari file data/faq.json
        knowledgeBase = new KnowledgeBase("data/faq.json");

        // 2. Inisialisasi strategi-strategi
        ruleBasedStrategy = new RuleBasedStrategy(knowledgeBase);
        // ApiBasedStrategy dibuat lazy (saat dipilih) agar tidak error jika API key kosong
        apiBasedStrategy = null;

        // 3. Inisialisasi QuestionProcessor dengan strategi default (rule-based)
        questionProcessor = new QuestionProcessor(ruleBasedStrategy);

        // 4. Inisialisasi ChatHistoryManager
        historyManager = new ChatHistoryManager();

        // 5. Muat riwayat chat dari file
        chatMessages = new ArrayList<>(historyManager.loadHistory());

        // 6. Setup ComboBox mode strategi
        modeComboBox.setItems(FXCollections.observableArrayList(
                "Rule-based", "API-based"
        ));
        modeComboBox.setValue("Rule-based");

        // Listener untuk mengganti strategi saat mode diubah
        modeComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            if ("API-based".equals(newVal)) {
                // Buat ApiBasedStrategy secara lazy saat pertama kali dipilih
                if (apiBasedStrategy == null) {
                    try {
                        apiBasedStrategy = new ApiBasedStrategy();
                    } catch (Exception e) {
                        // Jika gagal membuat API strategy, kembalikan ke rule-based
                        showAlert(Alert.AlertType.ERROR, "Error API",
                                "Gagal menginisialisasi Gemini API: " + e.getMessage()
                                + "\nPastikan API key sudah dikonfigurasi di config.properties.");
                        Platform.runLater(() -> modeComboBox.setValue("Rule-based"));
                        return;
                    }
                }
                questionProcessor.setStrategy(apiBasedStrategy);
                addBotSystemMessage("Mode diubah ke API-based. "
                        + "Jawaban akan dihasilkan oleh Gemini AI. Membutuhkan koneksi internet.");
            } else {
                questionProcessor.setStrategy(ruleBasedStrategy);
                addBotSystemMessage("Mode diubah ke Rule-based. "
                        + "Jawaban berdasarkan pencocokan keyword dari knowledge base.");
            }
        });

        // 7. Tampilkan pesan selamat datang
        displayWelcomeMessage();

        // 8. Render riwayat chat yang sudah ada
        for (ChatMessage msg : chatMessages) {
            displayMessage(msg);
        }

        // 9. Auto-scroll ke bawah setelah render
        scrollToBottom();

        // 10. Update sidebar riwayat
        updateHistorySidebar();

        // 11. Focus ke input field
        Platform.runLater(() -> inputField.requestFocus());
    }

    /**
     * Handler untuk mengirim pesan. Dipanggil saat tombol Kirim ditekan
     * atau Enter ditekan pada input field.
     */
    @FXML
    private void handleSendMessage() {
        String userInput = inputField.getText().trim();
        if (userInput.isEmpty()) return;

        // 1. Bersihkan input field
        inputField.clear();

        // 2. Buat pesan user dan tambahkan ke daftar
        ChatMessage userMessage = new ChatMessage("user", userInput, null);
        chatMessages.add(userMessage);

        // 3. Tampilkan bubble pesan user
        displayMessage(userMessage);

        // 4. Simpan riwayat
        historyManager.saveHistory(chatMessages);

        // 5. Nonaktifkan input sementara
        inputField.setDisable(true);
        sendButton.setDisable(true);

        // 6. Tampilkan indikator mengetik
        showTypingIndicator();

        // 7. Proses jawaban di background thread (terutama untuk mode API)
        Thread backgroundThread = new Thread(() -> {
            try {
                // Dapatkan jawaban dari QuestionProcessor
                String answer = questionProcessor.processQuestion(userInput);

                // Update UI di JavaFX Application Thread
                Platform.runLater(() -> {
                    // Hapus indikator mengetik
                    removeTypingIndicator();

                    // Buat pesan bot dan tambahkan ke daftar
                    ChatMessage botMessage = new ChatMessage("bot", answer, null);
                    chatMessages.add(botMessage);

                    // Tampilkan bubble pesan bot
                    displayMessage(botMessage);

                    // Simpan riwayat
                    historyManager.saveHistory(chatMessages);

                    // Update sidebar riwayat
                    updateHistorySidebar();

                    // Aktifkan kembali input
                    inputField.setDisable(false);
                    sendButton.setDisable(false);
                    inputField.requestFocus();
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    removeTypingIndicator();

                    // Tampilkan pesan error sebagai respons bot
                    ChatMessage errorMessage = new ChatMessage("bot",
                            "Maaf, terjadi kesalahan: " + e.getMessage(), null);
                    chatMessages.add(errorMessage);
                    displayMessage(errorMessage);
                    historyManager.saveHistory(chatMessages);

                    inputField.setDisable(false);
                    sendButton.setDisable(false);
                    inputField.requestFocus();
                });
            }
        });
        backgroundThread.setDaemon(true);
        backgroundThread.start();
    }

    /**
     * Menampilkan pesan selamat datang dari bot di area chat.
     */
    private void displayWelcomeMessage() {
        VBox welcomeContainer = new VBox(5);
        welcomeContainer.setAlignment(Pos.CENTER);
        welcomeContainer.setPadding(new Insets(20, 40, 20, 40));

        Label welcomeLabel = new Label(
                "🎓 Selamat datang di Chatbot Kampus Pintar!\n\n"
                + "Saya siap membantu menjawab pertanyaan Anda seputar kampus, "
                + "seperti jadwal kuliah, dosen, ruangan, KRS, dan lainnya.\n\n"
                + "Silakan ketik pertanyaan Anda di bawah. 😊"
        );
        welcomeLabel.getStyleClass().add("welcome-bubble");
        welcomeLabel.setWrapText(true);

        welcomeContainer.getChildren().add(welcomeLabel);
        chatContainer.getChildren().add(welcomeContainer);
    }

    /**
     * Menampilkan satu pesan chat (user atau bot) sebagai bubble di area chat.
     *
     * @param message Objek ChatMessage yang akan ditampilkan
     */
    private void displayMessage(ChatMessage message) {
        boolean isUser = "user".equals(message.getSender());

        // Container utama untuk satu pesan
        VBox messageContainer = new VBox(2);
        messageContainer.getStyleClass().add(
                isUser ? "message-container-user" : "message-container-bot"
        );
        messageContainer.setAlignment(isUser ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);

        // Label pengirim
        Label senderLabel = new Label(isUser ? "👤 Anda" : "🤖 Kampus Pintar");
        senderLabel.getStyleClass().add(
                isUser ? "message-sender-user" : "message-sender-bot"
        );

        // Label isi pesan (bubble)
        Label contentLabel = new Label(message.getContent());
        contentLabel.getStyleClass().add(
                isUser ? "message-bubble-user" : "message-bubble-bot"
        );
        contentLabel.setWrapText(true);
        contentLabel.setMaxWidth(500);

        // Label timestamp
        Label timestampLabel = new Label(formatTimestamp(message.getTimestamp()));
        timestampLabel.getStyleClass().add("message-timestamp");

        // Susun elemen berdasarkan pengirim
        messageContainer.getChildren().addAll(senderLabel, contentLabel, timestampLabel);

        // Tambahkan ke chat container
        chatContainer.getChildren().add(messageContainer);

        // Auto-scroll ke bawah
        scrollToBottom();
    }

    /**
     * Menampilkan indikator "Bot sedang mengetik..." di area chat.
     */
    private void showTypingIndicator() {
        HBox typingBox = new HBox(5);
        typingBox.setId(TYPING_INDICATOR_ID);
        typingBox.setAlignment(Pos.CENTER_LEFT);
        typingBox.setPadding(new Insets(2, 60, 2, 10));

        Label typingLabel = new Label("🤖 Bot sedang mengetik...");
        typingLabel.getStyleClass().add("typing-indicator");

        typingBox.getChildren().add(typingLabel);
        chatContainer.getChildren().add(typingBox);
        scrollToBottom();
    }

    /**
     * Menghapus indikator mengetik dari area chat.
     */
    private void removeTypingIndicator() {
        chatContainer.getChildren().removeIf(
                node -> TYPING_INDICATOR_ID.equals(node.getId())
        );
    }

    /**
     * Menampilkan pesan sistem singkat dari bot (misalnya saat ganti mode).
     *
     * @param text Teks pesan sistem
     */
    private void addBotSystemMessage(String text) {
        ChatMessage systemMsg = new ChatMessage("bot", text, null);
        chatMessages.add(systemMsg);
        displayMessage(systemMsg);
        historyManager.saveHistory(chatMessages);
    }

    /**
     * Memperbarui sidebar riwayat chat dengan daftar pesan user terbaru.
     * Menampilkan maksimal 20 item terakhir.
     */
    private void updateHistorySidebar() {
        historyContainer.getChildren().clear();

        // Filter hanya pesan user, ambil 20 terakhir, balik urutannya (terbaru dulu)
        List<ChatMessage> userMessages = chatMessages.stream()
                .filter(msg -> "user".equals(msg.getSender()))
                .collect(Collectors.toList());

        int start = Math.max(0, userMessages.size() - 20);
        List<ChatMessage> recentMessages = new ArrayList<>(
                userMessages.subList(start, userMessages.size())
        );
        // Balik urutan agar terbaru di atas
        java.util.Collections.reverse(recentMessages);

        for (ChatMessage msg : recentMessages) {
            String displayText = msg.getContent();
            // Potong teks jika terlalu panjang
            if (displayText.length() > 35) {
                displayText = displayText.substring(0, 35) + "...";
            }

            Label historyItem = new Label("💬 " + displayText);
            historyItem.getStyleClass().add("history-item");
            historyItem.setMaxWidth(Double.MAX_VALUE);
            historyItem.setTooltip(new Tooltip(msg.getContent()));

            // Klik untuk scroll ke pesan tersebut di chat
            final String originalContent = msg.getContent();
            historyItem.setOnMouseClicked(event -> scrollToMessage(originalContent));

            historyContainer.getChildren().add(historyItem);
        }
    }

    /**
     * Scroll ke pesan tertentu di area chat berdasarkan konten pesan.
     *
     * @param content Konten pesan yang dicari
     */
    private void scrollToMessage(String content) {
        for (Node node : chatContainer.getChildren()) {
            if (node instanceof VBox) {
                VBox container = (VBox) node;
                for (Node child : container.getChildren()) {
                    if (child instanceof Label) {
                        Label label = (Label) child;
                        if (content.equals(label.getText())) {
                            // Hitung posisi relatif dan scroll ke sana
                            double totalHeight = chatContainer.getHeight();
                            double nodeY = node.getBoundsInParent().getMinY();
                            if (totalHeight > 0) {
                                chatScrollPane.setVvalue(nodeY / totalHeight);
                            }
                            return;
                        }
                    }
                }
            }
        }
    }

    /**
     * Auto-scroll area chat ke bagian paling bawah.
     */
    private void scrollToBottom() {
        Platform.runLater(() -> {
            chatScrollPane.applyCss();
            chatScrollPane.layout();
            chatScrollPane.setVvalue(1.0);
        });
    }

    /**
     * Memformat timestamp ISO menjadi format yang mudah dibaca.
     *
     * @param isoTimestamp Timestamp dalam format ISO
     * @return Timestamp yang diformat, atau string asli jika parsing gagal
     */
    private String formatTimestamp(String isoTimestamp) {
        if (isoTimestamp == null || isoTimestamp.isEmpty()) {
            return "";
        }
        try {
            LocalDateTime dateTime = LocalDateTime.parse(isoTimestamp,
                    DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            return dateTime.format(DISPLAY_FORMATTER);
        } catch (Exception e) {
            return isoTimestamp;
        }
    }

    // =========================================================================
    // ADMIN PANEL
    // =========================================================================

    /**
     * Handler tombol Admin FAQ.
     * Menampilkan dialog password sebelum membuka panel admin.
     */
    @FXML
    private void handleAdminButton() {
        // Buat dialog custom dengan PasswordField (bukan TextInputDialog)
        Dialog<String> passwordDialog = new Dialog<>();
        passwordDialog.setTitle("Autentikasi Admin");
        passwordDialog.setHeaderText("🔒 Masukkan password admin");

        // Tombol dialog
        ButtonType loginButtonType = new ButtonType("Masuk", ButtonBar.ButtonData.OK_DONE);
        passwordDialog.getDialogPane().getButtonTypes().addAll(
                loginButtonType, ButtonType.CANCEL
        );

        // Layout dialog dengan PasswordField
        VBox dialogContent = new VBox(10);
        dialogContent.setPadding(new Insets(15));
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password admin...");
        passwordField.setPrefWidth(280);
        dialogContent.getChildren().addAll(
                new Label("Password:"),
                passwordField
        );
        passwordDialog.getDialogPane().setContent(dialogContent);

        // Focus ke password field saat dialog terbuka
        Platform.runLater(passwordField::requestFocus);

        // Konversi hasil dialog
        passwordDialog.setResultConverter(dialogButton -> {
            if (dialogButton == loginButtonType) {
                return passwordField.getText();
            }
            return null;
        });

        // Tampilkan dialog dan proses hasil
        Optional<String> result = passwordDialog.showAndWait();
        result.ifPresent(password -> {
            String adminPassword = ConfigManager.getInstance().getAdminPassword();
            if (adminPassword.equals(password)) {
                openAdminPanel();
            } else {
                showAlert(Alert.AlertType.ERROR, "Akses Ditolak",
                        "Password salah! Silakan coba lagi.");
            }
        });
    }

    /**
     * Membuka panel admin untuk manajemen FAQ.
     * Memuat admin-view.fxml dan mengonfigurasi semua kontrol secara programatik.
     */
    @SuppressWarnings("unchecked")
    private void openAdminPanel() {
        try {
            // 1. Muat admin-view.fxml
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/kampuspintar/chatbot/admin-view.fxml")
            );
            VBox adminRoot = loader.load();

            // 2. Buat Stage baru untuk panel admin
            Stage adminStage = new Stage();
            adminStage.initStyle(StageStyle.UTILITY);
            adminStage.initModality(Modality.APPLICATION_MODAL);
            adminStage.setTitle("⚙️ Manajemen FAQ - Admin Panel");

            Scene adminScene = new Scene(adminRoot, 800, 600);
            adminScene.getStylesheets().add(
                    getClass().getResource("/com/kampuspintar/chatbot/styles/dark-theme.css")
                            .toExternalForm()
            );
            adminStage.setScene(adminScene);
            adminStage.setMinWidth(700);
            adminStage.setMinHeight(500);

            // 3. Cari semua komponen berdasarkan fx:id menggunakan namespace FXML
            // Karena tidak ada controller, kita cari node via lookup dari root
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

            // 4. Setup kolom tabel dengan PropertyValueFactory
            colQuestion.setCellValueFactory(new PropertyValueFactory<>("question"));
            colAnswer.setCellValueFactory(new PropertyValueFactory<>("answer"));
            // Kolom keywords menampilkan list sebagai string dipisahkan koma
            colKeywords.setCellValueFactory(cellData -> {
                List<String> keywords = cellData.getValue().getKeywords();
                String joined = (keywords != null) ? String.join(", ", keywords) : "";
                return new javafx.beans.property.SimpleStringProperty(joined);
            });

            // 5. Isi tabel dengan data FAQ dari knowledge base
            ObservableList<FAQ> faqData = FXCollections.observableArrayList(
                    knowledgeBase.getAllFAQs()
            );
            faqTable.setItems(faqData);

            // 6. Listener seleksi tabel: isi form dengan data FAQ yang dipilih
            faqTable.getSelectionModel().selectedItemProperty().addListener(
                    (obs, oldSelection, newSelection) -> {
                        if (newSelection != null) {
                            faqQuestionField.setText(newSelection.getQuestion());
                            faqAnswerField.setText(newSelection.getAnswer());
                            String keywordsStr = (newSelection.getKeywords() != null)
                                    ? String.join(", ", newSelection.getKeywords()) : "";
                            faqKeywordsField.setText(keywordsStr);
                        }
                    }
            );

            // 7. Tombol Tambah FAQ
            addFaqButton.setOnAction(event -> {
                String question = faqQuestionField.getText().trim();
                String answer = faqAnswerField.getText().trim();
                String keywordsStr = faqKeywordsField.getText().trim();

                // Validasi input
                if (question.isEmpty() || answer.isEmpty()) {
                    showAlert(Alert.AlertType.WARNING, "Validasi",
                            "Pertanyaan dan Jawaban tidak boleh kosong!");
                    return;
                }

                // Parse keywords dari string yang dipisahkan koma
                List<String> keywords = parseKeywords(keywordsStr);

                // Buat FAQ baru dan tambahkan ke knowledge base
                FAQ newFaq = new FAQ(question, answer, keywords);
                knowledgeBase.addFAQ(newFaq);

                // Refresh tabel dan bersihkan form
                refreshFaqTable(faqTable);
                clearAdminForm(faqQuestionField, faqAnswerField, faqKeywordsField);

                showAlert(Alert.AlertType.INFORMATION, "Berhasil",
                        "FAQ baru berhasil ditambahkan!");
            });

            // 8. Tombol Edit FAQ
            editFaqButton.setOnAction(event -> {
                FAQ selectedFaq = faqTable.getSelectionModel().getSelectedItem();
                if (selectedFaq == null) {
                    showAlert(Alert.AlertType.WARNING, "Peringatan",
                            "Pilih FAQ yang ingin diedit terlebih dahulu!");
                    return;
                }

                String question = faqQuestionField.getText().trim();
                String answer = faqAnswerField.getText().trim();
                String keywordsStr = faqKeywordsField.getText().trim();

                // Validasi input
                if (question.isEmpty() || answer.isEmpty()) {
                    showAlert(Alert.AlertType.WARNING, "Validasi",
                            "Pertanyaan dan Jawaban tidak boleh kosong!");
                    return;
                }

                // Parse keywords
                List<String> keywords = parseKeywords(keywordsStr);

                // Cari indeks FAQ yang dipilih di knowledge base
                int index = findFaqIndex(selectedFaq);
                if (index >= 0) {
                    FAQ updatedFaq = new FAQ(question, answer, keywords);
                    knowledgeBase.updateFAQ(index, updatedFaq);

                    // Refresh tabel dan bersihkan form
                    refreshFaqTable(faqTable);
                    clearAdminForm(faqQuestionField, faqAnswerField, faqKeywordsField);

                    showAlert(Alert.AlertType.INFORMATION, "Berhasil",
                            "FAQ berhasil diperbarui!");
                } else {
                    showAlert(Alert.AlertType.ERROR, "Error",
                            "FAQ tidak ditemukan dalam knowledge base!");
                }
            });

            // 9. Tombol Hapus FAQ
            deleteFaqButton.setOnAction(event -> {
                FAQ selectedFaq = faqTable.getSelectionModel().getSelectedItem();
                if (selectedFaq == null) {
                    showAlert(Alert.AlertType.WARNING, "Peringatan",
                            "Pilih FAQ yang ingin dihapus terlebih dahulu!");
                    return;
                }

                // Konfirmasi penghapusan
                Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
                confirmDialog.setTitle("Konfirmasi Hapus");
                confirmDialog.setHeaderText("Hapus FAQ?");
                confirmDialog.setContentText(
                        "Apakah Anda yakin ingin menghapus FAQ:\n\""
                        + selectedFaq.getQuestion() + "\"?"
                );

                Optional<ButtonType> confirmResult = confirmDialog.showAndWait();
                if (confirmResult.isPresent() && confirmResult.get() == ButtonType.OK) {
                    int index = findFaqIndex(selectedFaq);
                    if (index >= 0) {
                        knowledgeBase.removeFAQ(index);
                        refreshFaqTable(faqTable);
                        clearAdminForm(faqQuestionField, faqAnswerField, faqKeywordsField);

                        showAlert(Alert.AlertType.INFORMATION, "Berhasil",
                                "FAQ berhasil dihapus!");
                    }
                }
            });

            // 10. Tombol Tutup
            closeFaqButton.setOnAction(event -> adminStage.close());

            // 11. Tampilkan panel admin
            adminStage.show();

        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Error",
                    "Gagal membuka panel admin: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Refresh data tabel FAQ dari knowledge base.
     *
     * @param table TableView yang akan di-refresh
     */
    private void refreshFaqTable(TableView<FAQ> table) {
        ObservableList<FAQ> faqData = FXCollections.observableArrayList(
                knowledgeBase.getAllFAQs()
        );
        table.setItems(faqData);
        table.refresh();
    }

    /**
     * Membersihkan form input admin.
     *
     * @param questionField Field pertanyaan
     * @param answerField   Field jawaban
     * @param keywordsField Field keywords
     */
    private void clearAdminForm(TextField questionField, TextArea answerField,
                                TextField keywordsField) {
        questionField.clear();
        answerField.clear();
        keywordsField.clear();
    }

    /**
     * Parse string keywords yang dipisahkan koma menjadi List.
     *
     * @param keywordsStr String keywords yang dipisahkan koma
     * @return List keyword yang sudah di-trim
     */
    private List<String> parseKeywords(String keywordsStr) {
        if (keywordsStr == null || keywordsStr.isEmpty()) {
            return new ArrayList<>();
        }
        return Arrays.stream(keywordsStr.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }

    /**
     * Mencari indeks FAQ di knowledge base berdasarkan pertanyaannya.
     *
     * @param targetFaq FAQ yang dicari
     * @return Indeks FAQ, atau -1 jika tidak ditemukan
     */
    private int findFaqIndex(FAQ targetFaq) {
        List<FAQ> allFaqs = knowledgeBase.getAllFAQs();
        for (int i = 0; i < allFaqs.size(); i++) {
            FAQ faq = allFaqs.get(i);
            if (faq.getQuestion() != null
                    && faq.getQuestion().equals(targetFaq.getQuestion())
                    && faq.getAnswer() != null
                    && faq.getAnswer().equals(targetFaq.getAnswer())) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Menampilkan dialog alert sederhana.
     *
     * @param type    Tipe alert (INFO, WARNING, ERROR, dll.)
     * @param title   Judul dialog
     * @param message Isi pesan dialog
     */
    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
