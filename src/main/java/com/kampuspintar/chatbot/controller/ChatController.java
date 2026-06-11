package com.kampuspintar.chatbot.controller;

import com.kampuspintar.chatbot.model.ChatMessage;
import com.kampuspintar.chatbot.model.KnowledgeBase;
import com.kampuspintar.chatbot.strategy.ApiBasedStrategy;
import com.kampuspintar.chatbot.strategy.QuestionProcessor;
import com.kampuspintar.chatbot.strategy.RuleBasedStrategy;
import com.kampuspintar.chatbot.util.ChatHistoryManager;

import com.kampuspintar.chatbot.util.UserSession;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
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

    // === Komponen bisnis logika ===
    /** Processor untuk memproses pertanyaan dengan strategi yang aktif */
    private QuestionProcessor questionProcessor;

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
        // Ambil username yang sedang login
        String currentUser = UserSession.getInstance().getUsername();
        if (currentUser == null || currentUser.isEmpty()) {
            currentUser = "guest"; // Fallback aman
        }

        // Knowledge base berisi daftar FAQ
        KnowledgeBase knowledgeBase = new KnowledgeBase("data/faq.json");
        ruleBasedStrategy = new RuleBasedStrategy(knowledgeBase);
        apiBasedStrategy = null;

        // Inisialisasi history manager dengan username tersebut
        historyManager = new ChatHistoryManager(currentUser);
        chatMessages = new ArrayList<>(historyManager.loadHistory());

        // Inisialisasi QuestionProcessor dengan strategi default (rule-based)
        questionProcessor = new QuestionProcessor(ruleBasedStrategy);

        // Setup ComboBox mode strategi
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

        // Tampilkan pesan selamat datang
        displayWelcomeMessage();

        // Render riwayat chat yang sudah ada
        for (ChatMessage msg : chatMessages) {
            displayMessage(msg);
        }

        // Auto-scroll ke bawah setelah render
        scrollToBottom();

        // Update sidebar riwayat
        updateHistorySidebar();

        // Focus ke input field
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

    @FXML
    private void handleLogout() {
        try {
            UserSession.getInstance().clearSession();

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/kampuspintar/chatbot/auth-view.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) sendButton.getScene().getWindow();
            Scene scene = new Scene(root, 900, 650);
            stage.setScene(scene);
            stage.setTitle("Login Kampus Pintar");
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Error Logout", "Gagal mengembalikan ke halaman login: " + e.getMessage());
            e.printStackTrace();
        }
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
