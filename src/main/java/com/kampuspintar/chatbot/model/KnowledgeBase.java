package com.kampuspintar.chatbot.model;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Mengelola knowledge base FAQ (Frequently Asked Questions).
 * Menyediakan operasi CRUD (Create, Read, Update, Delete) dan pencarian keyword.
 * Data disimpan dalam format JSON untuk persistensi lokal.
 *
 * <p>Kelas ini bertanggung jawab untuk:</p>
 * <ul>
 *     <li>Memuat data FAQ dari file JSON</li>
 *     <li>Menyimpan data FAQ ke file JSON</li>
 *     <li>Menambah, menghapus, dan memperbarui entri FAQ</li>
 *     <li>Mencari FAQ berdasarkan pencocokan keyword dari query pengguna</li>
 * </ul>
 *
 * @author Kampus Pintar Team
 * @version 1.0
 */
public class KnowledgeBase {

    /** Daftar FAQ yang dimuat dalam memori */
    private List<FAQ> faqList;

    /** Instance Gson untuk serialisasi/deserialisasi JSON */
    private final Gson gson;

    /** Path ke file JSON tempat FAQ disimpan */
    private String filePath;

    /**
     * Konstruktor KnowledgeBase.
     * Menginisialisasi Gson dengan format pretty printing dan memuat
     * data FAQ dari file yang ditentukan. Jika file tidak ditemukan,
     * akan dibuat daftar FAQ kosong.
     *
     * @param filePath Path ke file JSON yang menyimpan data FAQ
     */
    public KnowledgeBase(String filePath) {
        // Inisialisasi Gson dengan pretty printing agar file JSON mudah dibaca
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        this.filePath = filePath;
        this.faqList = new ArrayList<>();

        // Muat data FAQ dari file; jika gagal, tetap gunakan list kosong
        loadFromFile();
    }

    /**
     * Memuat data FAQ dari file JSON ke dalam memori.
     * Jika file tidak ditemukan atau terjadi error saat membaca,
     * daftar FAQ akan diinisialisasi sebagai list kosong.
     */
    public void loadFromFile() {
        Path path = Paths.get(filePath);

        // Periksa apakah file ada sebelum mencoba membaca
        if (!Files.exists(path)) {
            System.out.println("[KnowledgeBase] File tidak ditemukan: " + filePath
                    + ". Menggunakan daftar FAQ kosong.");
            this.faqList = new ArrayList<>();
            return;
        }

        try (Reader reader = Files.newBufferedReader(path)) {
            // Definisikan tipe List<FAQ> untuk deserialisasi Gson
            Type listType = new TypeToken<List<FAQ>>() {}.getType();
            List<FAQ> loaded = gson.fromJson(reader, listType);

            // Pastikan hasil deserialisasi tidak null
            this.faqList = (loaded != null) ? loaded : new ArrayList<>();
            System.out.println("[KnowledgeBase] Berhasil memuat " + faqList.size()
                    + " FAQ dari file.");
        } catch (IOException e) {
            System.err.println("[KnowledgeBase] Error saat membaca file: " + e.getMessage());
            this.faqList = new ArrayList<>();
        } catch (Exception e) {
            System.err.println("[KnowledgeBase] Error saat parsing JSON: " + e.getMessage());
            this.faqList = new ArrayList<>();
        }
    }

    /**
     * Menyimpan daftar FAQ dari memori ke file JSON.
     * Direktori induk akan dibuat secara otomatis jika belum ada.
     */
    public void saveToFile() {
        Path path = Paths.get(filePath);

        try {
            // Buat direktori induk jika belum ada
            Path parent = path.getParent();
            if (parent != null && !Files.exists(parent)) {
                Files.createDirectories(parent);
            }

            // Tulis daftar FAQ ke file dalam format JSON
            try (Writer writer = Files.newBufferedWriter(path)) {
                gson.toJson(faqList, writer);
            }
            System.out.println("[KnowledgeBase] Berhasil menyimpan " + faqList.size()
                    + " FAQ ke file.");
        } catch (IOException e) {
            System.err.println("[KnowledgeBase] Error saat menyimpan file: " + e.getMessage());
        }
    }

    /**
     * Menambahkan FAQ baru ke knowledge base dan menyimpan perubahan ke file.
     *
     * @param faq Objek FAQ yang akan ditambahkan
     * @throws IllegalArgumentException jika parameter faq bernilai null
     */
    public void addFAQ(FAQ faq) {
        if (faq == null) {
            throw new IllegalArgumentException("FAQ tidak boleh null.");
        }
        faqList.add(faq);
        saveToFile();
    }

    /**
     * Menghapus FAQ berdasarkan indeks dari knowledge base dan menyimpan perubahan.
     *
     * @param index Indeks FAQ yang akan dihapus (dimulai dari 0)
     * @throws IndexOutOfBoundsException jika indeks di luar rentang yang valid
     */
    public void removeFAQ(int index) {
        if (index < 0 || index >= faqList.size()) {
            throw new IndexOutOfBoundsException(
                    "Indeks " + index + " di luar rentang. Jumlah FAQ: " + faqList.size());
        }
        faqList.remove(index);
        saveToFile();
    }

    /**
     * Memperbarui FAQ pada indeks tertentu dan menyimpan perubahan ke file.
     *
     * @param index Indeks FAQ yang akan diperbarui (dimulai dari 0)
     * @param faq   Objek FAQ baru yang akan menggantikan FAQ lama
     * @throws IndexOutOfBoundsException jika indeks di luar rentang yang valid
     * @throws IllegalArgumentException  jika parameter faq bernilai null
     */
    public void updateFAQ(int index, FAQ faq) {
        if (index < 0 || index >= faqList.size()) {
            throw new IndexOutOfBoundsException(
                    "Indeks " + index + " di luar rentang. Jumlah FAQ: " + faqList.size());
        }
        if (faq == null) {
            throw new IllegalArgumentException("FAQ tidak boleh null.");
        }
        faqList.set(index, faq);
        saveToFile();
    }

    /**
     * Mendapatkan salinan daftar semua FAQ dalam knowledge base.
     * Mengembalikan salinan untuk mencegah modifikasi langsung dari luar.
     *
     * @return Salinan daftar FAQ
     */
    public List<FAQ> getAllFAQs() {
        return new ArrayList<>(faqList);
    }

    /**
     * Mencari FAQ yang paling relevan berdasarkan pencocokan keyword dari query.
     *
     * <p>Algoritma pencarian:</p>
     * <ol>
     *     <li>Normalisasi query ke huruf kecil (lowercase)</li>
     *     <li>Pecah query menjadi kata-kata individual</li>
     *     <li>Untuk setiap FAQ, hitung berapa keyword-nya yang cocok dengan kata-kata query</li>
     *     <li>Kembalikan FAQ dengan skor kecocokan tertinggi (minimal 1 keyword cocok)</li>
     *     <li>Jika tidak ada kecocokan, kembalikan null</li>
     * </ol>
     *
     * @param query Pertanyaan/query dari pengguna
     * @return FAQ yang paling relevan, atau null jika tidak ditemukan kecocokan
     */
    public FAQ searchFAQ(String query) {
        // Validasi input: jika query kosong atau null, tidak ada hasil
        if (query == null || query.trim().isEmpty()) {
            return null;
        }

        // Langkah 1: Normalisasi query ke huruf kecil
        String normalizedQuery = query.toLowerCase().trim();

        // Langkah 2: Pecah query menjadi set kata-kata unik
        Set<String> queryWords = Arrays.stream(normalizedQuery.split("\\s+"))
                .collect(Collectors.toSet());

        FAQ bestMatch = null;
        int highestScore = 0;

        // Langkah 3: Iterasi setiap FAQ dan hitung skor kecocokan keyword
        for (FAQ faq : faqList) {
            List<String> keywords = faq.getKeywords();
            if (keywords == null || keywords.isEmpty()) {
                continue;
            }

            // Hitung jumlah keyword FAQ yang muncul dalam kata-kata query
            int score = 0;
            for (String keyword : keywords) {
                if (keyword == null) {
                    continue;
                }
                String normalizedKeyword = keyword.toLowerCase().trim();
                // Cek apakah keyword ada di salah satu kata query,
                // atau apakah query mengandung keyword (untuk frasa multi-kata)
                if (queryWords.contains(normalizedKeyword)
                        || normalizedQuery.contains(normalizedKeyword)) {
                    score++;
                }
            }

            // Langkah 4: Simpan FAQ dengan skor tertinggi (minimal 1 kecocokan)
            if (score > highestScore) {
                highestScore = score;
                bestMatch = faq;
            }
        }

        // Langkah 5: Kembalikan hasil (null jika tidak ada kecocokan)
        return bestMatch;
    }
}
