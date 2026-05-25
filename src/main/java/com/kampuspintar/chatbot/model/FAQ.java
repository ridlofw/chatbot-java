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
    private String question;
    private String answer;
    private List<String> keywords;

    public FAQ(String question, String answer, List<String> keywords) {
        this.question = question;
        this.answer = answer;

        // Buat salinan list agar tidak terjadi modifikasi dari luar
        this.keywords = (keywords != null) ? new ArrayList<>(keywords) : new ArrayList<>();
    }

    public FAQ() {
        this.keywords = new ArrayList<>();
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public List<String> getKeywords() {
        return keywords;
    }

    public void setKeywords(List<String> keywords) {
        this.keywords = (keywords != null) ? new ArrayList<>(keywords) : new ArrayList<>();
    }

    @Override
    public String toString() {
        return String.format("FAQ{question='%s', answer='%s', keywords=%s}",
                question, answer, keywords);
    }
}
