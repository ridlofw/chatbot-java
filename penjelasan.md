# 📖 Penjelasan Detail Kelas — Chatbot Kampus Pintar

Dokumen ini menjelaskan setiap kelas dalam proyek Chatbot Kampus Pintar secara detail, termasuk tujuan, atribut, method, dan hubungan antar kelas.

---

## Daftar Isi

1. [ChatbotApp](#1-chatbotapp)
2. [ChatController](#2-chatcontroller)
3. [QuestionProcessor](#3-questionprocessor)
4. [AnsweringStrategy](#4-answeringstrategy-interface)
5. [RuleBasedStrategy](#5-rulebasedstrategy)
6. [ApiBasedStrategy](#6-apibasedstrategy)
7. [KnowledgeBase](#7-knowledgebase)
8. [ChatMessage](#8-chatmessage)
9. [FAQ](#9-faq)
10. [ChatHistoryManager](#10-chathistorymanager)
11. [ConfigManager](#11-configmanager)

---

## 1. ChatbotApp

**Package:** `com.kampuspintar.chatbot`  
**File:** `src/main/java/com/kampuspintar/chatbot/ChatbotApp.java`  
**Extends:** `javafx.application.Application`

### Deskripsi
Kelas utama yang menjalankan aplikasi JavaFX. Bertanggung jawab untuk menginisialisasi tema, memuat layout FXML, dan menampilkan window utama aplikasi.

### Atribut
Tidak memiliki atribut instance — semua konfigurasi dilakukan di method `start()`.

### Method

| Method | Parameter | Return | Deskripsi |
|--------|-----------|--------|-----------|
| `start(Stage)` | `primaryStage` — Stage utama JavaFX | `void` | Menginisialisasi tema AtlantaFX CupertinoDark, memuat `chat-view.fxml`, menerapkan `dark-theme.css`, dan menampilkan window dengan ukuran 900x650 (min 800x600). |
| `main(String[])` | `args` — argumen command line | `void` | Entry point. Memanggil `launch(args)` untuk memulai JavaFX. |

### Alur Inisialisasi
1. Set `CupertinoDark` sebagai user agent stylesheet (base theme)
2. Muat `chat-view.fxml` menggunakan `FXMLLoader`
3. Buat `Scene` dengan ukuran 900x650
4. Tambahkan `dark-theme.css` sebagai overlay CSS
5. Konfigurasi `Stage` (title, min size) dan tampilkan

### Relasi
- Memuat `ChatController` melalui FXML

---

## 2. ChatController

**Package:** `com.kampuspintar.chatbot.controller`  
**File:** `src/main/java/com/kampuspintar/chatbot/controller/ChatController.java`

### Deskripsi
Controller utama yang menangani **semua logika interaksi** antara pengguna dan chatbot. Mengelola pengiriman pesan, pergantian strategi, manajemen riwayat chat, dan panel admin FAQ. Ini adalah kelas terbesar dalam proyek (752 baris).

### Atribut FXML (di-inject dari chat-view.fxml)

| Atribut | Tipe | Deskripsi |
|---------|------|-----------|
| `chatContainer` | `VBox` | Container untuk bubble chat |
| `chatScrollPane` | `ScrollPane` | Scroll area untuk chat |
| `inputField` | `TextField` | Input pertanyaan pengguna |
| `sendButton` | `Button` | Tombol kirim pesan |
| `modeComboBox` | `ComboBox<String>` | Dropdown pilihan mode (Rule-based / API) |
| `historyContainer` | `VBox` | Container sidebar riwayat chat |
| `adminButton` | `Button` | Tombol akses admin FAQ |

### Atribut Bisnis

| Atribut | Tipe | Deskripsi |
|---------|------|-----------|
| `questionProcessor` | `QuestionProcessor` | Memproses pertanyaan dengan strategi aktif |
| `knowledgeBase` | `KnowledgeBase` | Database FAQ |
| `historyManager` | `ChatHistoryManager` | Manager penyimpanan riwayat |
| `chatMessages` | `List<ChatMessage>` | Daftar semua pesan dalam sesi |
| `ruleBasedStrategy` | `RuleBasedStrategy` | Instance strategi rule-based |
| `apiBasedStrategy` | `ApiBasedStrategy` | Instance strategi API (lazy-loaded) |

### Method Utama

| Method | Deskripsi |
|--------|-----------|
| `initialize()` | Inisialisasi semua komponen: load FAQ, setup strategi, load history, setup ComboBox listener, tampilkan welcome message |
| `handleSendMessage()` | Handler tombol Kirim & Enter: ambil input → buat pesan user → tampilkan bubble → proses di background thread → tampilkan jawaban bot |
| `displayMessage(ChatMessage)` | Render satu pesan sebagai bubble chat (user = kanan biru, bot = kiri abu-abu) dengan sender label dan timestamp |
| `showTypingIndicator()` | Tampilkan "Bot sedang mengetik..." saat memproses jawaban |
| `removeTypingIndicator()` | Hapus indikator mengetik |
| `updateHistorySidebar()` | Perbarui sidebar dengan 20 pesan user terakhir (terbaru di atas) |
| `handleAdminButton()` | Tampilkan dialog password → validasi → buka admin panel jika benar |
| `openAdminPanel()` | Muat `admin-view.fxml`, setup TableView dengan data FAQ, wire tombol CRUD |
| `scrollToBottom()` | Auto-scroll chat ke bawah |
| `scrollToMessage(String)` | Scroll ke pesan tertentu saat item riwayat diklik |
| `formatTimestamp(String)` | Format ISO timestamp ke format "HH:mm, dd MMM yyyy" |
| `showAlert(...)` | Tampilkan dialog alert sederhana |

### Fitur Admin Panel
- **Autentikasi**: Dialog `PasswordField` (password di-mask), validasi terhadap `ConfigManager.getAdminPassword()`
- **CRUD FAQ**: Tambah, Edit, Hapus FAQ melalui form dan TableView
- **Validasi**: Pertanyaan dan jawaban wajib diisi
- **Konfirmasi**: Dialog konfirmasi sebelum menghapus FAQ

### Threading
- Pemrosesan jawaban dilakukan di **background thread** (`Thread` daemon)
- Update UI menggunakan `Platform.runLater()` untuk thread-safety
- Input field dan tombol dinonaktifkan selama pemrosesan

---

## 3. QuestionProcessor

**Package:** `com.kampuspintar.chatbot.strategy`  
**File:** `src/main/java/com/kampuspintar/chatbot/strategy/QuestionProcessor.java`

### Deskripsi
Bertindak sebagai **Context** dalam Strategy Pattern. Menerima pertanyaan pengguna dan mendelegasikan ke strategi yang aktif (`AnsweringStrategy`). Memungkinkan pergantian strategi secara runtime.

### Atribut

| Atribut | Tipe | Deskripsi |
|---------|------|-----------|
| `strategy` | `AnsweringStrategy` | Strategi aktif untuk menjawab pertanyaan |

### Method

| Method | Parameter | Return | Deskripsi |
|--------|-----------|--------|-----------|
| `QuestionProcessor(AnsweringStrategy)` | `strategy` | — | Constructor, set strategi awal |
| `setStrategy(AnsweringStrategy)` | `strategy` | `void` | Ganti strategi aktif (runtime switching) |
| `getStrategy()` | — | `AnsweringStrategy` | Ambil strategi aktif |
| `processQuestion(String)` | `question` | `String` | Validasi input, lalu delegasikan ke `strategy.getAnswer()` |

### Validasi Input
- Jika `question` null atau kosong → return "Silakan masukkan pertanyaan Anda."
- Jika valid → trim dan kirim ke strategi aktif

---

## 4. AnsweringStrategy (Interface)

**Package:** `com.kampuspintar.chatbot.strategy`  
**File:** `src/main/java/com/kampuspintar/chatbot/strategy/AnsweringStrategy.java`

### Deskripsi
**Interface** yang mendefinisikan kontrak untuk semua strategi menjawab pertanyaan. Merupakan bagian inti dari **Strategy Pattern**, memungkinkan aplikasi mengganti algoritma pencocokan jawaban secara dinamis tanpa mengubah kode client.

### Method

| Method | Parameter | Return | Deskripsi |
|--------|-----------|--------|-----------|
| `getAnswer(String)` | `question` — pertanyaan pengguna | `String` — jawaban | Menghasilkan jawaban berdasarkan strategi yang diimplementasikan |

### Implementasi
1. `RuleBasedStrategy` — pencocokan keyword dari knowledge base
2. `ApiBasedStrategy` — pemanggilan Gemini AI API

---

## 5. RuleBasedStrategy

**Package:** `com.kampuspintar.chatbot.strategy`  
**File:** `src/main/java/com/kampuspintar/chatbot/strategy/RuleBasedStrategy.java`  
**Implements:** `AnsweringStrategy`

### Deskripsi
Strategi menjawab pertanyaan berbasis **pencocokan keyword** dari `KnowledgeBase`. Tidak memerlukan koneksi internet dan memberikan jawaban instan.

### Atribut

| Atribut | Tipe | Deskripsi |
|---------|------|-----------|
| `knowledgeBase` | `KnowledgeBase` | Referensi ke knowledge base FAQ |

### Algoritma `getAnswer(String question)`
1. Panggil `knowledgeBase.searchFAQ(question)` untuk mencari FAQ yang cocok
2. Jika ditemukan → return jawaban FAQ tersebut
3. Jika tidak ditemukan → return pesan fallback:
   > "Maaf, saya belum bisa menjawab pertanyaan tersebut. Silakan coba dengan kata kunci yang berbeda atau gunakan mode API untuk jawaban yang lebih cerdas."

### Kelebihan & Kekurangan
| ✅ Kelebihan | ❌ Kekurangan |
|-------------|--------------|
| Respons instan | Terbatas pada FAQ yang tersedia |
| Tidak perlu internet | Tidak bisa menjawab pertanyaan di luar knowledge base |
| Konsisten | Tidak fleksibel |

---

## 6. ApiBasedStrategy

**Package:** `com.kampuspintar.chatbot.strategy`  
**File:** `src/main/java/com/kampuspintar/chatbot/strategy/ApiBasedStrategy.java`  
**Implements:** `AnsweringStrategy`

### Deskripsi
Strategi menjawab pertanyaan menggunakan **Google Gemini AI API**. Mengirim pertanyaan ke model AI dan mengembalikan respons yang dihasilkan. Memerlukan koneksi internet dan API key valid.

### Atribut

| Atribut | Tipe | Deskripsi |
|---------|------|-----------|
| `client` | `Client` | Client Google GenAI SDK |
| `model` | `String` | Nama model Gemini (default: "gemini-2.0-flash-lite") |
| `SYSTEM_PROMPT` | `String` (static) | Prompt sistem yang mengarahkan AI sebagai asisten kampus |

### Inisialisasi (Constructor)
1. Ambil API key dari `ConfigManager.getInstance().getApiKey()`
2. Set `System.setProperty("GOOGLE_API_KEY", apiKey)`
3. Buat `Client` menggunakan no-arg constructor `new Client()`
4. Ambil model name dari `ConfigManager.getInstance().getModel()`

### Algoritma `getAnswer(String question)`
1. Validasi input (null/kosong → pesan default)
2. Gabungkan `SYSTEM_PROMPT` + pertanyaan pengguna
3. Panggil `client.models.generateContent(model, fullPrompt, null)`
4. Ambil teks respons → trim → return
5. Jika respons kosong → return pesan fallback
6. Jika exception → return pesan error informatif

### System Prompt
```
Kamu adalah asisten chatbot kampus bernama 'Kampus Pintar'. 
Jawab pertanyaan berikut dengan ramah, informatif, dan dalam Bahasa Indonesia. 
Fokus pada topik seputar kampus seperti jadwal kuliah, dosen, ruangan, KRS, 
perpustakaan, dan administrasi akademik.

Pertanyaan: [pertanyaan user]
```

---

## 7. KnowledgeBase

**Package:** `com.kampuspintar.chatbot.model`  
**File:** `src/main/java/com/kampuspintar/chatbot/model/KnowledgeBase.java`

### Deskripsi
Mengelola **database FAQ** (Frequently Asked Questions). Menyediakan operasi CRUD dan pencarian keyword. Data disimpan dalam format **JSON** ke file lokal (`data/faq.json`).

### Atribut

| Atribut | Tipe | Deskripsi |
|---------|------|-----------|
| `faqList` | `List<FAQ>` | Daftar semua FAQ |
| `gson` | `Gson` | Instance Gson dengan pretty printing |
| `filePath` | `String` | Path ke file JSON |

### Method

| Method | Deskripsi |
|--------|-----------|
| `KnowledgeBase(String)` | Constructor, inisialisasi Gson dan load FAQ dari file |
| `loadFromFile()` | Baca file JSON dan parse ke `List<FAQ>` |
| `saveToFile()` | Tulis `faqList` ke file JSON (pretty printed) |
| `addFAQ(FAQ)` | Tambah FAQ baru dan simpan |
| `removeFAQ(int)` | Hapus FAQ berdasarkan indeks dan simpan |
| `updateFAQ(int, FAQ)` | Update FAQ pada indeks tertentu dan simpan |
| `getAllFAQs()` | Return salinan `faqList` |
| `searchFAQ(String)` | Cari FAQ berdasarkan keyword matching |

### Algoritma `searchFAQ(String query)`
1. Normalisasi query ke lowercase
2. Split query menjadi array kata
3. Untuk setiap FAQ, hitung berapa keyword-nya yang muncul di kata query
4. Return FAQ dengan skor tertinggi (minimum 1 keyword cocok)
5. Jika tidak ada yang cocok → return `null`

### Persistensi
- **Format**: JSON array menggunakan Gson
- **Auto-save**: Setiap operasi CRUD otomatis menyimpan ke file
- **Auto-create**: Jika file/direktori belum ada, dibuat otomatis

---

## 8. ChatMessage

**Package:** `com.kampuspintar.chatbot.model`  
**File:** `src/main/java/com/kampuspintar/chatbot/model/ChatMessage.java`

### Deskripsi
**Model data** untuk menyimpan satu pesan chat. Setiap pesan memiliki pengirim, isi pesan, dan timestamp. Desainnya kompatibel dengan Gson untuk serialisasi/deserialisasi JSON.

### Atribut

| Atribut | Tipe | Deskripsi |
|---------|------|-----------|
| `sender` | `String` | Pengirim pesan: `"user"` atau `"bot"` |
| `content` | `String` | Isi/teks pesan |
| `timestamp` | `String` | Timestamp dalam format ISO (contoh: `"2026-05-24T14:30:00"`) |

### Constructor
- `ChatMessage(String sender, String content, String timestamp)` — Jika timestamp `null`, otomatis menggunakan `LocalDateTime.now()` dalam format ISO
- `ChatMessage()` — Default constructor untuk Gson deserialisasi

### Auto-generated Timestamp
```java
if (timestamp == null) {
    this.timestamp = LocalDateTime.now()
        .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
}
```

---

## 9. FAQ

**Package:** `com.kampuspintar.chatbot.model`  
**File:** `src/main/java/com/kampuspintar/chatbot/model/FAQ.java`

### Deskripsi
**Model data** untuk menyimpan satu item FAQ. Setiap FAQ memiliki pertanyaan, jawaban, dan daftar keyword yang digunakan untuk pencocokan di `RuleBasedStrategy`.

### Atribut

| Atribut | Tipe | Deskripsi |
|---------|------|-----------|
| `question` | `String` | Pertanyaan FAQ |
| `answer` | `String` | Jawaban FAQ |
| `keywords` | `List<String>` | Daftar keyword untuk pencocokan |

### Contoh Data (faq.json)
```json
{
  "question": "Bagaimana cara mengisi KRS?",
  "answer": "Untuk mengisi KRS, login ke portal akademik, pilih menu KRS...",
  "keywords": ["krs", "isi", "cara", "mengisi", "registrasi"]
}
```

### Defensive Copying
Getter `getKeywords()` mengembalikan **salinan** list keyword, bukan referensi langsung, untuk mencegah modifikasi tidak sengaja dari luar.

---

## 10. ChatHistoryManager

**Package:** `com.kampuspintar.chatbot.util`  
**File:** `src/main/java/com/kampuspintar/chatbot/util/ChatHistoryManager.java`

### Deskripsi
Mengelola **persistensi riwayat chat** ke file JSON lokal (`data/chat_history.json`). Memastikan percakapan tersimpan antar sesi aplikasi.

### Atribut

| Atribut | Tipe | Deskripsi |
|---------|------|-----------|
| `HISTORY_FILE` | `String` (static) | Path ke file riwayat: `"data/chat_history.json"` |
| `gson` | `Gson` | Instance Gson dengan pretty printing |

### Method

| Method | Parameter | Return | Deskripsi |
|--------|-----------|--------|-----------|
| `saveHistory(List<ChatMessage>)` | `messages` | `void` | Simpan daftar pesan ke file JSON. Buat direktori parent jika belum ada. |
| `loadHistory()` | — | `List<ChatMessage>` | Muat riwayat dari file. Return empty list jika file tidak ada. |
| `clearHistory()` | — | `void` | Hapus semua riwayat (simpan list kosong). |

### Error Handling
- Jika file tidak ditemukan saat load → return `ArrayList` kosong (bukan crash)
- Jika gagal buat direktori/tulis file → print stack trace (graceful degradation)

---

## 11. ConfigManager

**Package:** `com.kampuspintar.chatbot.util`  
**File:** `src/main/java/com/kampuspintar/chatbot/util/ConfigManager.java`

### Deskripsi
**Singleton** untuk mengelola konfigurasi aplikasi. Membaca pengaturan dari file `config.properties` di root proyek.

### Design Pattern: Singleton
```java
private static ConfigManager instance;

public static synchronized ConfigManager getInstance() {
    if (instance == null) {
        instance = new ConfigManager();
    }
    return instance;
}
```

### Atribut

| Atribut | Tipe | Deskripsi |
|---------|------|-----------|
| `instance` | `ConfigManager` (static) | Instance singleton |
| `properties` | `Properties` | Java Properties untuk key-value pairs |
| `CONFIG_FILE` | `String` (static) | `"config.properties"` |

### Getter Methods

| Method | Default Value | Deskripsi |
|--------|--------------|-----------|
| `getApiKey()` | `""` | API key Gemini |
| `getModel()` | `"gemini-2.0-flash-lite"` | Nama model Gemini |
| `getAdminPassword()` | `"admin123"` | Password admin FAQ |
| `getAppName()` | `"Chatbot Kampus Pintar"` | Nama aplikasi |

### Urutan Loading Config
1. Coba baca dari file system (`config.properties` di root proyek)
2. Jika tidak ditemukan → coba baca dari classpath
3. Jika masih tidak ditemukan → gunakan default values

### Thread Safety
Method `getInstance()` menggunakan `synchronized` untuk thread-safety.

---

## Diagram Relasi Antar Kelas

```
ChatbotApp
    └── (memuat FXML) → ChatController
            ├── QuestionProcessor
            │       └── AnsweringStrategy (interface)
            │               ├── RuleBasedStrategy
            │               │       └── KnowledgeBase
            │               │               └── FAQ (model)
            │               └── ApiBasedStrategy
            │                       └── ConfigManager (singleton)
            ├── ChatHistoryManager
            │       └── ChatMessage (model)
            └── KnowledgeBase (shared)
```

---

## Design Patterns yang Digunakan

| Pattern | Kelas | Penjelasan |
|---------|-------|------------|
| **Strategy** | `AnsweringStrategy`, `RuleBasedStrategy`, `ApiBasedStrategy`, `QuestionProcessor` | Memungkinkan pergantian algoritma jawaban secara runtime tanpa mengubah kode client |
| **MVC** | `ChatbotApp` (View), `ChatController` (Controller), Model classes | Memisahkan tampilan, logika kontrol, dan data |
| **Singleton** | `ConfigManager` | Memastikan hanya ada satu instance konfigurasi di seluruh aplikasi |
| **Observer** | ComboBox listener, TableView selection listener | Pola reaktif untuk menangani perubahan UI |
