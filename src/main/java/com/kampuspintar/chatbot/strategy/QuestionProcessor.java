package com.kampuspintar.chatbot.strategy;

/**
 * Memproses pertanyaan pengguna menggunakan strategi yang ditentukan.
 * Bertindak sebagai context dalam Strategy Pattern.
 *
 * Kelas ini memegang referensi ke AnsweringStrategy dan mendelegasikan
 * pemrosesan pertanyaan ke strategi yang aktif. Strategi dapat diganti
 * saat runtime (misalnya dari rule-based ke API-based).
 */
public class QuestionProcessor {

    private AnsweringStrategy strategy;

    /**
     * Membuat QuestionProcessor dengan strategi awal.
     *
     * @param strategy strategi awal untuk menjawab pertanyaan
     */
    public QuestionProcessor(AnsweringStrategy strategy) {
        if (strategy == null) {
            throw new IllegalArgumentException("Strategy tidak boleh null.");
        }
        this.strategy = strategy;
    }

    /**
     * Mengubah strategi yang digunakan untuk menjawab pertanyaan.
     *
     * @param strategy strategi baru
     */
    public void setStrategy(AnsweringStrategy strategy) {
        if (strategy == null) {
            throw new IllegalArgumentException("Strategy tidak boleh null.");
        }
        this.strategy = strategy;
    }

    /**
     * Mendapatkan strategi yang sedang aktif.
     *
     * @return strategi yang sedang digunakan
     */
    public AnsweringStrategy getStrategy() {
        return this.strategy;
    }

    /**
     * Memproses pertanyaan pengguna dan mengembalikan jawaban
     * berdasarkan strategi yang aktif.
     *
     * @param question pertanyaan dari pengguna
     * @return jawaban dari strategi yang aktif
     */
    public String processQuestion(String question) {
        // Validasi input kosong
        if (question == null || question.trim().isEmpty()) {
            return "Silakan masukkan pertanyaan Anda.";
        }
        // Delegasi ke strategi yang aktif
        return strategy.getAnswer(question.trim());
    }
}
