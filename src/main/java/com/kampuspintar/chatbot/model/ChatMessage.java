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

    /**
     * Konstruktor dengan semua field.
     * Jika timestamp tidak disediakan (null atau kosong), akan dibuat otomatis
     * menggunakan waktu saat ini.
     *
     * @param sender    Pengirim pesan ("user" atau "bot")
     * @param content   Isi pesan
     * @param timestamp Timestamp dalam format ISO, atau null untuk auto-generate
     */
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

    /**
     * Konstruktor default tanpa argumen.
     * Diperlukan oleh Gson untuk proses deserialisasi dari JSON.
     * Timestamp akan di-generate otomatis menggunakan waktu saat ini.
     */
    public ChatMessage() {
        this.timestamp = LocalDateTime.now().format(ISO_FORMATTER);
    }

    /**
     * Mendapatkan pengirim pesan.
     *
     * @return Pengirim pesan ("user" atau "bot")
     */
    public String getSender() {
        return sender;
    }

    /**
     * Mengatur pengirim pesan.
     *
     * @param sender Pengirim pesan ("user" atau "bot")
     */
    public void setSender(String sender) {
        this.sender = sender;
    }

    /**
     * Mendapatkan isi/konten pesan.
     *
     * @return Isi pesan
     */
    public String getContent() {
        return content;
    }

    /**
     * Mengatur isi/konten pesan.
     *
     * @param content Isi pesan
     */
    public void setContent(String content) {
        this.content = content;
    }

    /**
     * Mendapatkan timestamp pesan dalam format ISO.
     *
     * @return Timestamp dalam format ISO string
     */
    public String getTimestamp() {
        return timestamp;
    }

    /**
     * Mengatur timestamp pesan.
     *
     * @param timestamp Timestamp dalam format ISO string
     */
    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    /**
     * Representasi string dari objek ChatMessage.
     * Berguna untuk keperluan debugging dan logging.
     *
     * @return String representasi pesan dalam format [timestamp] sender: content
     */
    @Override
    public String toString() {
        return String.format("[%s] %s: %s", timestamp, sender, content);
    }
}
