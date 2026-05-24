package com.kampuspintar.chatbot.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Model untuk menyimpan data FAQ (Frequently Asked Questions).
 * Setiap FAQ memiliki pertanyaan, jawaban, dan daftar keyword untuk pencocokan.
 *
 * <p>Kelas ini merepresentasikan satu entri dalam knowledge base chatbot.
 * Daftar keyword digunakan oleh mesin pencarian untuk mencocokkan
 * pertanyaan pengguna dengan FAQ yang relevan.</p>
 *
 * @author Kampus Pintar Team
 * @version 1.0
 */
public class FAQ {

    /** Pertanyaan yang sering ditanyakan */
    private String question;

    /** Jawaban untuk pertanyaan tersebut */
    private String answer;

    /** Daftar kata kunci untuk pencocokan dengan pertanyaan pengguna */
    private List<String> keywords;

    /**
     * Konstruktor dengan semua field.
     *
     * @param question Pertanyaan FAQ
     * @param answer   Jawaban FAQ
     * @param keywords Daftar kata kunci untuk pencocokan
     */
    public FAQ(String question, String answer, List<String> keywords) {
        this.question = question;
        this.answer = answer;
        // Buat salinan list agar tidak terjadi modifikasi dari luar
        this.keywords = (keywords != null) ? new ArrayList<>(keywords) : new ArrayList<>();
    }

    /**
     * Konstruktor default tanpa argumen.
     * Diperlukan oleh Gson untuk proses deserialisasi dari JSON.
     * Menginisialisasi keywords sebagai list kosong.
     */
    public FAQ() {
        this.keywords = new ArrayList<>();
    }

    /**
     * Mendapatkan pertanyaan FAQ.
     *
     * @return Pertanyaan FAQ
     */
    public String getQuestion() {
        return question;
    }

    /**
     * Mengatur pertanyaan FAQ.
     *
     * @param question Pertanyaan FAQ
     */
    public void setQuestion(String question) {
        this.question = question;
    }

    /**
     * Mendapatkan jawaban FAQ.
     *
     * @return Jawaban FAQ
     */
    public String getAnswer() {
        return answer;
    }

    /**
     * Mengatur jawaban FAQ.
     *
     * @param answer Jawaban FAQ
     */
    public void setAnswer(String answer) {
        this.answer = answer;
    }

    /**
     * Mendapatkan daftar kata kunci FAQ.
     *
     * @return Salinan daftar kata kunci
     */
    public List<String> getKeywords() {
        return keywords;
    }

    /**
     * Mengatur daftar kata kunci FAQ.
     *
     * @param keywords Daftar kata kunci baru
     */
    public void setKeywords(List<String> keywords) {
        this.keywords = (keywords != null) ? new ArrayList<>(keywords) : new ArrayList<>();
    }

    /**
     * Representasi string dari objek FAQ.
     * Berguna untuk keperluan debugging dan logging.
     *
     * @return String representasi FAQ dalam format yang mudah dibaca
     */
    @Override
    public String toString() {
        return String.format("FAQ{question='%s', answer='%s', keywords=%s}",
                question, answer, keywords);
    }
}
