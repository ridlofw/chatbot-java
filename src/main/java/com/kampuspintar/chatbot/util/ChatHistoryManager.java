package com.kampuspintar.chatbot.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.kampuspintar.chatbot.model.ChatMessage;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Mengelola penyimpanan dan pemuatan riwayat chat spesifik per akun pengguna.
 * Data disimpan dalam format JSON ke file lokal untuk persistensi.
 *
 * <p>Kelas ini menyediakan fungsionalitas untuk:</p>
 * <ul>
 * <li>Menyimpan daftar pesan chat ke file JSON berdasarkan user</li>
 * <li>Memuat riwayat chat dari file JSON milik user tertentu</li>
 * <li>Menghapus semua riwayat chat user terkait</li>
 * </ul>
 *
 * @author Kampus Pintar Team
 * @version 1.1
 */
public class ChatHistoryManager {

    /** Path relatif ke file penyimpanan riwayat chat yang dinamis sesuai user */
    private final String historyFile;

    /** Instance Gson untuk serialisasi/deserialisasi JSON */
    private final Gson gson;

    /**
     * Konstruktor ChatHistoryManager.
     * Menginisialisasi nama file riwayat chat secara spesifik berdasarkan nama pengguna.
     * Menggunakan format pretty printing agar file JSON mudah dibaca oleh manusia.
     * * @param username Nama pengguna (akun) yang sedang aktif digunakan
     */
    public ChatHistoryManager(String username) {
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        this.historyFile = "data/history_" + username + ".json";
    }

    /**
     * Menyimpan daftar pesan chat ke file JSON milik user aktif.
     * Direktori induk akan dibuat secara otomatis jika belum ada.
     *
     * <p>Jika parameter messages bernilai null, akan disimpan
     * sebagai list kosong untuk menghindari error.</p>
     *
     * @param messages Daftar pesan chat yang akan disimpan
     */
    public void saveHistory(List<ChatMessage> messages) {
        Path path = Paths.get(this.historyFile);

        try {
            // Buat direktori induk jika belum ada (misalnya folder "data/")
            Path parent = path.getParent();
            if (parent != null && !Files.exists(parent)) {
                Files.createDirectories(parent);
            }

            // Gunakan list kosong jika messages bernilai null
            List<ChatMessage> toSave = (messages != null) ? messages : new ArrayList<>();

            // Tulis daftar pesan ke file dalam format JSON
            try (Writer writer = Files.newBufferedWriter(path)) {
                gson.toJson(toSave, writer);
            }

            System.out.println("[ChatHistoryManager] Berhasil menyimpan "
                    + toSave.size() + " pesan ke riwayat (" + this.historyFile + ").");

        } catch (IOException e) {
            System.err.println("[ChatHistoryManager] Error saat menyimpan riwayat chat: "
                    + e.getMessage());
        }
    }

    /**
     * Memuat riwayat chat dari file JSON milik user aktif.
     * Jika file tidak ditemukan atau terjadi error saat membaca,
     * akan mengembalikan list kosong.
     *
     * @return Daftar pesan chat dari riwayat, atau list kosong jika file tidak ada
     */
    public List<ChatMessage> loadHistory() {
        Path path = Paths.get(this.historyFile);

        // Periksa apakah file riwayat ada
        if (!Files.exists(path)) {
            System.out.println("[ChatHistoryManager] File riwayat " + this.historyFile
                    + " tidak ditemukan. Mengembalikan list kosong.");
            return new ArrayList<>();
        }

        try (Reader reader = Files.newBufferedReader(path)) {
            // Definisikan tipe List<ChatMessage> untuk deserialisasi Gson
            Type listType = new TypeToken<List<ChatMessage>>() {}.getType();
            List<ChatMessage> messages = gson.fromJson(reader, listType);

            // Pastikan hasil deserialisasi tidak null
            if (messages != null) {
                System.out.println("[ChatHistoryManager] Berhasil memuat "
                        + messages.size() + " pesan dari riwayat (" + this.historyFile + ").");
                return messages;
            }

            return new ArrayList<>();

        } catch (IOException e) {
            System.err.println("[ChatHistoryManager] Error saat membaca riwayat chat: "
                    + e.getMessage());
            return new ArrayList<>();
        } catch (Exception e) {
            System.err.println("[ChatHistoryManager] Error saat parsing JSON riwayat: "
                    + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Menghapus semua riwayat chat milik user aktif.
     * Implementasi dilakukan dengan menyimpan list kosong ke file,
     * sehingga file tetap ada namun isinya kosong.
     */
    public void clearHistory() {
        saveHistory(new ArrayList<>());
        System.out.println("[ChatHistoryManager] Riwayat chat untuk " + this.historyFile + " berhasil dihapus.");
    }
}