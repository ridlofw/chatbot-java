package com.kampuspintar.chatbot.strategy;

/**
 * Interface Strategy Pattern untuk menjawab pertanyaan pengguna.
 *
 * <p>Interface ini mendefinisikan kontrak untuk berbagai strategi
 * menjawab pertanyaan dalam chatbot. Setiap implementasi menyediakan
 * mekanisme yang berbeda untuk menghasilkan jawaban.</p>
 *
 * <p>Implementasi yang tersedia:</p>
 * <ul>
 *     <li>{@link RuleBasedStrategy} - Menjawab berdasarkan pencocokan keyword dari knowledge base</li>
 *     <li>{@link ApiBasedStrategy} - Menjawab menggunakan Gemini AI API</li>
 * </ul>
 *
 * <p>Penggunaan Strategy Pattern memungkinkan pergantian strategi
 * secara dinamis saat runtime tanpa mengubah kode klien.</p>
 *
 * @author Kampus Pintar Team
 * @version 1.0
 */
public interface AnsweringStrategy {

    /**
     * Memberikan jawaban berdasarkan pertanyaan yang diberikan.
     *
     * <p>Setiap implementasi memiliki cara yang berbeda dalam
     * memproses pertanyaan dan menghasilkan jawaban yang sesuai.</p>
     *
     * @param question Pertanyaan dari pengguna yang memerlukan jawaban
     * @return Jawaban yang dihasilkan oleh strategi yang digunakan,
     *         tidak pernah null (selalu mengembalikan pesan fallback jika gagal)
     */
    String getAnswer(String question);
}
