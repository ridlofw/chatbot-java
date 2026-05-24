# Aplikasi Chatbot Kampus Pintar berbasis Java

**Dokumen Spesifikasi Proyek**

**Deskripsi Umum:**

Aplikasi desktop yang memungkinkan mahasiswa mengajukan pertanyaan seputar kampus (jadwal kuliah, nama dosen, lokasi ruangan, tata cara KRS, dan lainnya), dan mendapatkan respons otomatis melalui chatbot. Aplikasi ini dirancang untuk memperkenalkan prinsip dasar NLP (natural language understanding) dan penerapan konsep OOP dalam konteks praktis, interaktif, dan bermanfaat.

**Tujuan Pembelajaran:**

- Menerapkan prinsip OOP dalam membangun sistem modular.
- Merancang dan membangun GUI menggunakan JavaFX/Swing.
- Mengimplementasikan chatbot dengan strategi rule-based atau API eksternal.
- Mengembangkan logika pencocokan pertanyaan dan penyajian respons.
- Mengelola knowledge base statis atau dinamis secara efisien.

**Fitur Utama:**

| **Fitur** | **Deskripsi** |
| --- | --- |
| Input Pertanyaan | Pengguna dapat mengetik pertanyaan seputar kampus. |
| Jawaban Otomatis | Sistem memberikan respons otomatis berbasis rule atau API. |
| Antarmuka Chatting | Simulasi percakapan (user dan bot) dalam jendela GUI. |
| Pilihan Mode | Mode jawaban sederhana (rule-based) atau cerdas (API GPT/NLP Cloud). |
| Riwayat Chat | Menyimpan riwayat tanya jawab. |
| Manajemen FAQ (opsional) | Admin dapat mengedit/menambah daftar pertanyaan dan jawaban. |

**Struktur OOP (Kelas dan Relasi):**

1. **ChatbotApp**
    - Kelas utama yang menjalankan aplikasi GUI.
2. **ChatController**
    - Menangani logika interaksi pengguna dan bot.
    - Memanggil QuestionProcessor dan menampilkan hasil.
3. **QuestionProcessor**
    - Menentukan jenis pertanyaan dan strategi yang digunakan.
4. **AnsweringStrategy *(interface)***
    - Metode: String getAnswer(String question)
5. **RuleBasedStrategy *(implements AnsweringStrategy)***
    - Menggunakan aturan pencocokan keyword sederhana.
6. **ApiBasedStrategy *(implements AnsweringStrategy)***
    - Menghubungkan ke API (misalnya OpenAI, HuggingFace, NLP Cloud).
7. **KnowledgeBase**
    - Menyimpan daftar pertanyaan-jawaban (FAQ).
    - Format bisa berupa JSON atau file lokal sederhana.
8. **ChatMessage**
    - Struktur data untuk menyimpan isi pesan (user/bot, isi, timestamp).

**Antarmuka Pengguna (GUI):**

| **Komponen** | **Fungsi** |
| --- | --- |
| Chat Area | Menampilkan percakapan antara user dan chatbot. |
| Text Field Input | Tempat mengetik pertanyaan. |
| Tombol Kirim | Memproses input ke chatbot. |
| ComboBox Mode | Memilih strategi bot: Rule-based / API-based (jika tersedia). |
| Panel Riwayat | Menampilkan pertanyaan dan jawaban sebelumnya. |
| Menu Admin (opsional) | Mengedit/menambahkan FAQ jika mode admin diaktifkan. |

**Integrasi API:**

Jika menggunakan strategi cerdas (abstractive), mahasiswa bisa memakai:

- **OpenAI GPT (text-davinci/gpt-3.5)**
- **HuggingFace Inference API (bert-small QA model)**
- **NLPCloud / DeepAI**Pemanggilan dilakukan dengan HTTP request (HttpURLConnection atau OkHttp/Unirest di Java).

**Output:**

- Respons jawaban dari bot tampil di antarmuka.
- Riwayat percakapan tersimpan dalam log lokal.
- Jawaban diambil dari base knowledge (rule-based) atau API (jika aktifkan).

**Deliverables:**

- Source code lengkap dan runnable.
- Laporan proyek (PDF): berisi analisis kebutuhan, desain class diagram, dan penjelasan fitur.
- Video demo penggunaan aplikasi.