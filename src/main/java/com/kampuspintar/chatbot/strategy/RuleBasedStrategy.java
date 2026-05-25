package com.kampuspintar.chatbot.strategy;

import com.kampuspintar.chatbot.model.FAQ;
import com.kampuspintar.chatbot.model.KnowledgeBase;

/**
 * Strategi menjawab pertanyaan berbasis aturan (rule-based).
 * Menggunakan pencocokan keyword dari KnowledgeBase untuk menemukan
 * jawaban yang paling relevan dengan pertanyaan pengguna.
 *
 * <p>Strategi ini cocok digunakan ketika:</p>
 * <ul>
 *     <li>Tidak ada koneksi internet</li>
 *     <li>Pertanyaan bersifat standar dan sudah ada di knowledge base</li>
 *     <li>Respon cepat diperlukan tanpa latensi API</li>
 * </ul>
 *
 * <p>Jika tidak ditemukan kecocokan dalam knowledge base, akan mengembalikan
 * pesan fallback yang mengarahkan pengguna untuk mencoba kata kunci lain
 * atau beralih ke mode API.</p>
 *
 * @author Kampus Pintar Team
 * @version 1.0
 */
public class RuleBasedStrategy implements AnsweringStrategy {
    private final KnowledgeBase knowledgeBase;
    private static final String FALLBACK_MESSAGE =
            "Maaf, saya belum bisa menjawab pertanyaan tersebut. "
            + "Silakan coba dengan kata kunci yang berbeda atau "
            + "gunakan mode API untuk jawaban yang lebih cerdas.";

    public RuleBasedStrategy(KnowledgeBase knowledgeBase) {
        if (knowledgeBase == null) {
            throw new IllegalArgumentException("KnowledgeBase tidak boleh null.");
        }
        this.knowledgeBase = knowledgeBase;
    }

    @Override
    public String getAnswer(String question) {
        // Validasi input: jika pertanyaan kosong, langsung kembalikan fallback
        if (question == null || question.trim().isEmpty()) {
            return FALLBACK_MESSAGE;
        }

        // Cari FAQ yang cocok menggunakan pencocokan keyword
        FAQ matchedFaq = knowledgeBase.searchFAQ(question);

        // Kembalikan jawaban jika ditemukan, atau pesan fallback jika tidak
        if (matchedFaq != null && matchedFaq.getAnswer() != null) {
            return matchedFaq.getAnswer();
        }

        return FALLBACK_MESSAGE;
    }
}
