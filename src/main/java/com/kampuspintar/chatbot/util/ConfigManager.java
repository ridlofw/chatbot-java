package com.kampuspintar.chatbot.util;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

/**
 * Singleton untuk mengelola konfigurasi aplikasi.
 * Membaca pengaturan dari file {@code config.properties} dan menyediakan
 * akses terpusat ke semua nilai konfigurasi.
 *
 * <p>Kelas ini mengimplementasikan pola Singleton untuk memastikan
 * hanya ada satu instance ConfigManager di seluruh aplikasi.
 * Konfigurasi dimuat sekali saat instance pertama kali dibuat.</p>
 *
 * <p>Properti yang didukung:</p>
 * <ul>
 *     <li>{@code gemini.api.key} - API key untuk Google Gemini AI</li>
 *     <li>{@code gemini.model} - Nama model Gemini yang digunakan</li>
 *     <li>{@code admin.password} - Password untuk akses admin</li>
 *     <li>{@code app.name} - Nama aplikasi</li>
 * </ul>
 *
 * <p>Jika file konfigurasi tidak ditemukan, nilai default akan digunakan.</p>
 *
 * @author Kampus Pintar Team
 * @version 1.0
 */
public class ConfigManager {

    /** Instance tunggal ConfigManager (Singleton) */
    private static ConfigManager instance;

    /** Objek Properties untuk menyimpan pasangan key-value konfigurasi */
    private final Properties properties;

    /** Nama file konfigurasi yang akan dibaca */
    private static final String CONFIG_FILE = "config.properties";

    /**
     * Konstruktor private untuk mencegah instansiasi dari luar (Singleton).
     * Memuat konfigurasi dari file saat pertama kali dibuat.
     */
    private ConfigManager() {
        properties = new Properties();
        loadConfig();
    }

    /**
     * Mendapatkan instance tunggal ConfigManager.
     * Jika belum ada instance, akan dibuat instance baru.
     * Metode ini thread-safe menggunakan synchronized block.
     *
     * @return Instance tunggal ConfigManager
     */
    public static synchronized ConfigManager getInstance() {
        if (instance == null) {
            instance = new ConfigManager();
        }
        return instance;
    }

    /**
     * Memuat konfigurasi dari file config.properties.
     *
     * <p>Proses pemuatan:</p>
     * <ol>
     *     <li>Pertama, coba muat dari file sistem (working directory)</li>
     *     <li>Jika tidak ditemukan, coba muat dari classpath (resources)</li>
     *     <li>Jika keduanya gagal, gunakan nilai default</li>
     * </ol>
     */
    private void loadConfig() {
        // Strategi 1: Coba muat dari file di working directory
        Path configPath = Paths.get(CONFIG_FILE);
        if (Files.exists(configPath)) {
            try (InputStream inputStream = new FileInputStream(configPath.toFile())) {
                properties.load(inputStream);
                System.out.println("[ConfigManager] Konfigurasi berhasil dimuat dari file: "
                        + configPath.toAbsolutePath());
                return;
            } catch (IOException e) {
                System.err.println("[ConfigManager] Error saat membaca file konfigurasi: "
                        + e.getMessage());
            }
        }

        // Strategi 2: Coba muat dari classpath (folder resources)
        try (InputStream inputStream = getClass().getClassLoader()
                .getResourceAsStream(CONFIG_FILE)) {
            if (inputStream != null) {
                properties.load(inputStream);
                System.out.println("[ConfigManager] Konfigurasi berhasil dimuat dari classpath.");
                return;
            }
        } catch (IOException e) {
            System.err.println("[ConfigManager] Error saat membaca konfigurasi dari classpath: "
                    + e.getMessage());
        }

        // Strategi 3: Gunakan nilai default jika file tidak ditemukan
        System.out.println("[ConfigManager] File konfigurasi tidak ditemukan. "
                + "Menggunakan nilai default.");
    }

    /**
     * Mendapatkan API key untuk Google Gemini AI.
     *
     * @return API key Gemini, atau string kosong jika tidak dikonfigurasi
     */
    public String getApiKey() {
        return properties.getProperty("gemini.api.key", "");
    }

    /**
     * Mendapatkan nama model Gemini yang digunakan.
     *
     * @return Nama model Gemini (default: "gemini-2.0-flash-lite")
     */
    public String getModel() {
        return properties.getProperty("gemini.model", "gemini-2.0-flash-lite");
    }

    /**
     * Mendapatkan password admin untuk akses fitur administrasi.
     *
     * @return Password admin (default: "admin123")
     */
    public String getAdminPassword() {
        return properties.getProperty("admin.password", "admin123");
    }

    /**
     * Mendapatkan nama aplikasi.
     *
     * @return Nama aplikasi (default: "Chatbot Kampus Pintar")
     */
    public String getAppName() {
        return properties.getProperty("app.name", "Chatbot Kampus Pintar");
    }
}
