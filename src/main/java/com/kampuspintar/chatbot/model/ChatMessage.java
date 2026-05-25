package com.kampuspintar.chatbot.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Model untuk menyimpan data pesan chat.
 * Setiap pesan memiliki pengirim (user/bot), isi pesan, dan timestamp.
 *
 * <p>Kelas ini digunakan untuk merepresentasikan satu unit pesan dalam
 * percakapan antara pengguna dan chatbot. Timestamp disimpan dalam
 * format ISO string agar kompatibel dengan serialisasi Gson.</p>
 *
 * @author Kampus Pintar Team
 * @version 1.0
 */
public class ChatMessage {

    /** Pengirim pesan: "user" untuk pengguna, "bot" untuk chatbot */
    private String sender;

    /** Isi/konten dari pesan */
    private String content;

    /** Timestamp dalam format ISO (yyyy-MM-dd'T'HH:mm:ss) */
    private String timestamp;

    /** Formatter untuk menghasilkan timestamp dalam format ISO */
    private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    public ChatMessage(String sender, String content, String timestamp) {
        this.sender = sender;
        this.content = content;
        // Jika timestamp tidak disediakan, generate otomatis dari waktu sekarang
        if (timestamp == null || timestamp.isEmpty()) {
            this.timestamp = LocalDateTime.now().format(ISO_FORMATTER);
        } else {
            this.timestamp = timestamp;
        }
    }

    public ChatMessage() {
        this.timestamp = LocalDateTime.now().format(ISO_FORMATTER);
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return String.format("[%s] %s: %s", timestamp, sender, content);
    }
}
