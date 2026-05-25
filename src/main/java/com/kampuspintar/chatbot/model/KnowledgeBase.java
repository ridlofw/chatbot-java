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
    private List<FAQ> faqList;
    private final Gson gson;
    private String filePath;

    public KnowledgeBase(String filePath) {
        // Inisialisasi Gson dengan pretty printing agar file JSON mudah dibaca
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        this.filePath = filePath;
        this.faqList = new ArrayList<>();

        // Muat data FAQ dari file; jika gagal, tetap gunakan list kosong
        loadFromFile();
    }

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

    public void addFAQ(FAQ faq) {
        if (faq == null) throw new IllegalArgumentException("FAQ tidak boleh null.");

        faqList.add(faq);
        saveToFile();
    }

    public void removeFAQ(int index) {
        if (index < 0 || index >= faqList.size()) {
            String message = "Indeks " + index + " di luar rentang. Jumlah FAQ: " + faqList.size();
            throw new IndexOutOfBoundsException(message);
        }

        faqList.remove(index);
        saveToFile();
    }

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

    public List<FAQ> getAllFAQs() {
        return new ArrayList<>(faqList);
    }

    public FAQ searchFAQ(String query) {
        // Validasi input: jika query kosong atau null, tidak ada hasil
        if (query == null || query.trim().isEmpty()) {
            return null;
        }

        // Langkah 1: Normalisasi query ke huruf kecil
        String normalizedQuery = query.toLowerCase().trim();

        // Langkah 2: Pecah query menjadi set kata-kata unik
        Set<String> queryWords = Arrays
                .stream(normalizedQuery.split("\\s+"))
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
