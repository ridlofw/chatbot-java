# 🎓 Chatbot Kampus Pintar

<div align="center">

![Java](https://img.shields.io/badge/Java_21-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![JavaFX](https://img.shields.io/badge/JavaFX_21-0095D5?style=for-the-badge&logo=java&logoColor=white)
![Gemini AI](https://img.shields.io/badge/Google_Gemini_AI-4285F4?style=for-the-badge&logo=google&logoColor=white)
![Maven](https://img.shields.io/badge/Apache_Maven-C71A36?style=for-the-badge&logo=apachemaven&logoColor=white)
![AtlantaFX](https://img.shields.io/badge/AtlantaFX-7B2D8E?style=for-the-badge&logo=css3&logoColor=white)

**Aplikasi Desktop Chatbot Interaktif untuk Universitas Dian Nuswantoro (UDINUS)**

*Menggabungkan Rule-Based Engine & Google Gemini AI dalam satu antarmuka modern bertema gelap.*

---

</div>

## 📋 Daftar Isi

- [Tentang Proyek](#-tentang-proyek)
- [Fitur Utama](#-fitur-utama)
- [Arsitektur & Design Pattern](#-arsitektur--design-pattern)
- [Struktur Proyek](#-struktur-proyek)
- [Prasyarat](#-prasyarat)
- [Instalasi & Menjalankan Aplikasi](#-instalasi--menjalankan-aplikasi)
- [Konfigurasi](#-konfigurasi)
- [Cara Penggunaan](#-cara-penggunaan)
- [Knowledge Base (FAQ)](#-knowledge-base-faq)
- [Teknologi yang Digunakan](#-teknologi-yang-digunakan)
- [Kontribusi](#-kontribusi)
- [Lisensi](#-lisensi)

---

## 📖 Tentang Proyek

**Chatbot Kampus Pintar** adalah aplikasi desktop berbasis Java yang dirancang untuk membantu mahasiswa dan civitas akademika **Universitas Dian Nuswantoro (UDINUS)** dalam mencari informasi seputar kampus secara cepat, akurat, dan interaktif. Aplikasi ini mensimulasikan percakapan antara pengguna dan bot melalui antarmuka grafis (GUI) yang modern.

Proyek ini dibangun sebagai implementasi dari konsep **Object-Oriented Programming (OOP)**, **Design Pattern**, dan pengenalan dasar **Natural Language Processing (NLP)** dalam konteks pembuatan chatbot.

### Tujuan Pembelajaran

| No | Tujuan |
|:--:|--------|
| 1 | Menerapkan prinsip OOP (Encapsulation, Inheritance, Polymorphism, Abstraction) dalam membangun sistem modular |
| 2 | Merancang dan membangun GUI interaktif menggunakan **JavaFX + FXML** |
| 3 | Mengimplementasikan chatbot dengan dua strategi: **Rule-Based** dan **API-Based (Gemini AI)** |
| 4 | Menerapkan **Strategy Design Pattern** untuk memisahkan logika pemrosesan jawaban |
| 5 | Mengelola *knowledge base* statis (FAQ dalam format JSON) dengan fitur CRUD |
| 6 | Mengintegrasikan API eksternal (Google Gemini) untuk kemampuan jawaban yang lebih cerdas |

---

## ✨ Fitur Utama

### 🤖 Dua Mode Chatbot

| Mode | Deskripsi | Kelebihan |
|------|-----------|-----------|
| **Rule-Based** | Menjawab berdasarkan pencocokan *keyword* dari Knowledge Base lokal (`data/faq.json`) | Cepat, tidak butuh internet, respons konsisten |
| **API-Based (Gemini AI)** | Menjawab menggunakan Google Gemini 3.1 Flash-Lite melalui API | Jawaban lebih fleksibel, memahami konteks, bahasa alami |

> Pengguna dapat **berganti mode kapan saja** melalui ComboBox di header aplikasi tanpa perlu restart.

### 💬 Antarmuka Chat Interaktif
- Desain *chat bubble* modern (mirip Discord/Telegram) — **biru** untuk user, **gelap** untuk bot.
- **Typing indicator** ("Bot sedang mengetik...") saat memproses jawaban.
- *Auto-scroll* ke pesan terbaru.
- Input field berbentuk *pill* (rounded) dengan efek *glow* saat fokus.
- Tombol kirim dengan animasi *hover scale* dan *pressed shrink*.

### 📜 Riwayat Percakapan
- Sidebar di sisi kanan menampilkan **20 pesan terakhir** (terbaru di atas).
- Klik item riwayat untuk langsung *scroll* ke pesan tersebut di area chat.
- Riwayat tersimpan secara **persisten** di `data/chat_history.json` — tetap ada meskipun aplikasi ditutup.

### 🔐 Panel Admin FAQ (Dilindungi Password)
- Akses melalui tombol **"Admin FAQ"** di sidebar → masukkan password admin.
- Membuka jendela modal terpisah (800×600) dengan tabel FAQ lengkap.
- Fitur **CRUD** (Create, Read, Update, Delete):
  - ➕ **Tambah**: Isi form pertanyaan, jawaban, dan keywords → validasi otomatis → simpan.
  - ✏️ **Edit**: Pilih baris di tabel → form terisi otomatis → ubah → simpan.
  - 🗑️ **Hapus**: Pilih baris → konfirmasi → hapus.
- Semua perubahan langsung tersimpan ke file `data/faq.json`.

### 🛡️ Keamanan AI (Anti-Jailbreak)
Pada mode API-Based, sistem menerapkan **system prompt** yang ketat agar Gemini AI:
- Hanya menjawab pertanyaan seputar kampus dan akademik UDINUS.
- Menolak *prompt injection* dan percobaan manipulasi instruksi.
- Memberikan pesan penolakan standar jika pertanyaan di luar domain.

### 🎨 Tema Gelap Premium
- Berbasis **AtlantaFX CupertinoDark** dengan overlay CSS kustom (677 baris).
- Terinspirasi dari **Discord & VS Code dark theme**.
- Header dengan gradient animasi, custom scrollbar tipis, dan efek bayangan halus.

---

## 🏗️ Arsitektur & Design Pattern

Proyek ini mengimplementasikan beberapa *design pattern* untuk menjaga kode tetap modular, bersih, dan mudah dikembangkan.

### 1. Strategy Pattern ⭐ (Pattern Utama)

Digunakan untuk memisahkan logika pemrosesan jawaban agar dapat diganti secara *runtime*.

```
┌─────────────────────────┐
│    QuestionProcessor     │  ← Context
│  (holds strategy ref)    │
└────────────┬────────────┘
             │ delegates to
             ▼
┌─────────────────────────┐
│   «interface»           │
│   AnsweringStrategy     │  ← Strategy Interface
│  + getAnswer(question)  │
└──────┬──────────┬───────┘
       │          │
       ▼          ▼
┌──────────┐ ┌────────────┐
│ Rule     │ │ ApiBased   │  ← Concrete Strategies
│ Based    │ │ Strategy   │
│ Strategy │ │ (Gemini)   │
└──────────┘ └────────────┘
```

- **Interface** — `AnsweringStrategy`: Mendefinisikan kontrak `getAnswer(String question)`.
- **Concrete Strategy 1** — `RuleBasedStrategy`: Menggunakan `KnowledgeBase.searchFAQ()` untuk pencocokan keyword.
- **Concrete Strategy 2** — `ApiBasedStrategy`: Mengirim prompt ke Google Gemini AI melalui SDK resmi.
- **Context** — `QuestionProcessor`: Memegang referensi ke strategi aktif, mendelegasikan pemrosesan pertanyaan. Strategi dapat diganti via `setStrategy()`.

### 2. MVC Pattern (Model-View-Controller)

| Layer | Komponen | Keterangan |
|-------|----------|------------|
| **Model** | `ChatMessage`, `FAQ`, `KnowledgeBase` | Struktur data dan logika bisnis |
| **View** | `chat-view.fxml`, `admin-view.fxml`, `dark-theme.css` | Tampilan antarmuka (FXML + CSS) |
| **Controller** | `ChatController` | Menangani interaksi pengguna dan menghubungkan Model ↔ View |

### 3. Singleton Pattern

`ConfigManager` menggunakan *Singleton* dengan akses `synchronized` untuk memastikan hanya ada satu instance konfigurasi di seluruh aplikasi.

### 4. Builder Pattern

Digunakan saat menginisialisasi Gemini AI Client:
```java
Client client = Client.builder().apiKey(apiKey).build();
```

---

## 📂 Struktur Proyek

```
chatbot-java/
│
├── 📄 pom.xml                          # Konfigurasi Maven (dependensi & build)
├── 📄 config.properties                # Konfigurasi lokal (API key, password) — TIDAK di-commit
├── 📄 config.properties.example        # Template konfigurasi untuk developer lain
├── 📄 README.md                        # Dokumentasi proyek (file ini)
├── 📄 .gitignore                       # Daftar file/folder yang diabaikan Git
│
├── 📁 data/                            # Data persisten aplikasi
│   ├── 📄 faq.json                     # Knowledge Base — daftar FAQ (15 entri)
│   └── 📄 chat_history.json            # Riwayat percakapan tersimpan
│
└── 📁 src/main/
    ├── 📁 java/
    │   ├── 📄 module-info.java         # Java Module descriptor
    │   │
    │   └── 📁 com/kampuspintar/chatbot/
    │       │
    │       ├── 📄 ChatbotApp.java              # 🚀 Entry point — Application class
    │       │
    │       ├── 📁 controller/
    │       │   └── 📄 ChatController.java      # 🎮 Controller utama (chat, admin, history)
    │       │
    │       ├── 📁 model/
    │       │   ├── 📄 ChatMessage.java         # 💬 Model pesan chat (sender, content, timestamp)
    │       │   ├── 📄 FAQ.java                 # ❓ Model FAQ (question, answer, keywords)
    │       │   └── 📄 KnowledgeBase.java       # 📚 Manajemen FAQ — load/save/search/CRUD
    │       │
    │       ├── 📁 strategy/
    │       │   ├── 📄 AnsweringStrategy.java   # 📐 Interface Strategy Pattern
    │       │   ├── 📄 RuleBasedStrategy.java   # 🔍 Strategi pencocokan keyword lokal
    │       │   ├── 📄 ApiBasedStrategy.java    # 🤖 Strategi Gemini AI API
    │       │   └── 📄 QuestionProcessor.java   # 🔄 Context — mendelegasikan ke strategi aktif
    │       │
    │       └── 📁 util/
    │           ├── 📄 ConfigManager.java       # ⚙️ Singleton konfigurasi (config.properties)
    │           └── 📄 ChatHistoryManager.java  # 💾 Persistensi riwayat chat (JSON)
    │
    └── 📁 resources/com/kampuspintar/chatbot/
        ├── 📄 chat-view.fxml                   # 🖼️ Layout utama (chat + sidebar)
        ├── 📄 admin-view.fxml                  # 🖼️ Layout panel admin FAQ
        └── 📁 styles/
            └── 📄 dark-theme.css               # 🎨 Tema gelap kustom (677 baris)
```

---

## 📋 Prasyarat

Pastikan sistem Anda telah memiliki komponen berikut sebelum menjalankan aplikasi:

| Komponen | Versi Minimum | Keterangan |
|----------|:-------------:|------------|
| **Java Development Kit (JDK)** | 21 | Diperlukan untuk JavaFX 21 dan fitur modern Java |
| **Apache Maven** | 3.8+ | Untuk manajemen dependensi dan build. Sudah tersedia di folder `.maven/` |
| **Koneksi Internet** | — | Diperlukan saat pertama kali build (download dependensi) dan saat menggunakan mode API-Based |
| **API Key Google Gemini** | — | Opsional — hanya jika ingin menggunakan mode API-Based. Dapatkan gratis di [Google AI Studio](https://aistudio.google.com/) |

---

## 🚀 Instalasi & Menjalankan Aplikasi

### 1️⃣ Clone Repositori

```bash
git clone https://github.com/username-anda/chatbot-java.git
cd chatbot-java
```

### 2️⃣ Konfigurasi Environment

Salin file template konfigurasi dan isi dengan data Anda:

```bash
# Linux / macOS
cp config.properties.example config.properties

# Windows (Command Prompt)
copy config.properties.example config.properties

# Windows (PowerShell)
Copy-Item config.properties.example config.properties
```

Buka `config.properties` dan masukkan API Key Gemini Anda:

```properties
# Ganti YOUR_API_KEY_HERE dengan API Key dari Google AI Studio
gemini.api.key=YOUR_API_KEY_HERE
gemini.model=gemini-3.1-flash-lite

# Ganti password admin sesuai keinginan Anda
admin.password=admin123

app.name=Chatbot Kampus Pintar
app.version=1.0
```

> [!NOTE]
> File `config.properties` sudah masuk ke `.gitignore` sehingga **tidak akan ikut ter-commit** ke GitHub. API Key Anda aman.

> [!TIP]
> Jika Anda tidak memiliki API Key Gemini, aplikasi tetap dapat berjalan menggunakan **mode Rule-Based** tanpa koneksi internet.

### 3️⃣ Build & Run

```bash
# Compile proyek
mvn clean compile

# Jalankan aplikasi
mvn javafx:run
```

Atau jika Anda menggunakan Maven yang tersedia di folder `.maven/`:

```bash
# Windows
.maven\bin\mvn clean compile
.maven\bin\mvn javafx:run
```

---

## ⚙️ Konfigurasi

Seluruh konfigurasi aplikasi dikelola melalui file `config.properties` yang dibaca oleh class `ConfigManager` (Singleton).

| Property | Default | Deskripsi |
|----------|---------|-----------|
| `gemini.api.key` | *(kosong)* | API Key Google Gemini untuk mode AI-Based |
| `gemini.model` | `gemini-2.0-flash-lite` | Nama model Gemini yang digunakan |
| `admin.password` | `admin123` | Password untuk mengakses panel Admin FAQ |
| `app.name` | `Chatbot Kampus Pintar` | Nama aplikasi yang ditampilkan |
| `app.version` | `1.0` | Versi aplikasi |

**Urutan pencarian file konfigurasi oleh `ConfigManager`:**
1. File `config.properties` di *working directory* (root proyek).
2. Resource `config.properties` di classpath.
3. Jika keduanya tidak ditemukan → menggunakan nilai default.

---

## 🖥️ Cara Penggunaan

### Mode Rule-Based (Default)

1. Jalankan aplikasi → mode **Rule-Based** aktif secara default.
2. Ketik pertanyaan di input field bawah, contoh: *"Bagaimana cara mengisi KRS?"*
3. Bot akan mencocokkan keyword pertanyaan Anda dengan database FAQ.
4. Jika ditemukan kecocokan → jawaban ditampilkan. Jika tidak → pesan fallback muncul.

### Mode API-Based (Gemini AI)

1. Pastikan `gemini.api.key` sudah diisi di `config.properties`.
2. Pilih **"API-based (Gemini AI)"** pada ComboBox di header.
3. Ketik pertanyaan apa saja seputar kampus → Gemini AI akan memberikan jawaban cerdas.
4. Bot dilengkapi *system prompt* sehingga hanya menjawab topik akademik/kampus.

### Mengelola FAQ (Admin)

1. Klik tombol **"Admin FAQ"** di sidebar kanan.
2. Masukkan password admin (default: `admin123`).
3. Gunakan panel admin untuk **Tambah / Edit / Hapus** FAQ.
4. Isi field **Keywords** dengan kata kunci dipisahkan koma (contoh: `krs, isi krs, pengisian`).
5. Perubahan langsung tersimpan ke `data/faq.json`.

---

## 📚 Knowledge Base (FAQ)

Aplikasi ini dilengkapi dengan **15 entri FAQ** bawaan yang mencakup informasi penting seputar UDINUS:

| No | Topik Pertanyaan | Contoh Keywords |
|:--:|------------------|-----------------|
| 1 | Cara mengisi KRS | `krs`, `isi krs`, `pengisian krs`, `siakad` |
| 2 | Jadwal kuliah | `jadwal`, `kuliah`, `jadwal kuliah`, `jam kuliah` |
| 3 | Lokasi ruang kelas | `ruang kelas`, `lokasi`, `gedung`, `peta kampus` |
| 4 | Dosen pengampu | `dosen`, `pengampu`, `dosen pengajar`, `pengajar` |
| 5 | Surat keterangan aktif | `surat aktif`, `keterangan aktif`, `surat mahasiswa` |
| 6 | Jadwal UTS & UAS | `uts`, `uas`, `ujian`, `jadwal ujian` |
| 7 | Cuti akademik | `cuti`, `cuti akademik`, `izin cuti` |
| 8 | Perpustakaan | `perpustakaan`, `perpus`, `pinjam buku` |
| 9 | Jam layanan TU | `tata usaha`, `tu`, `administrasi` |
| 10 | KTM (Kartu Tanda Mahasiswa) | `ktm`, `kartu mahasiswa`, `kartu tanda mahasiswa` |
| 11 | Syarat kelulusan | `lulus`, `kelulusan`, `syarat lulus`, `wisuda` |
| 12 | Pengajuan skripsi | `skripsi`, `tugas akhir`, `proposal skripsi` |
| 13 | Lab komputer | `lab`, `komputer`, `laboratorium` |
| 14 | E-learning | `e-learning`, `kuliah online`, `daring` |
| 15 | Beasiswa | `beasiswa`, `kip`, `ppa`, `bantuan biaya` |

> [!TIP]
> FAQ dapat ditambah atau diubah kapan saja melalui **panel Admin** atau dengan mengedit langsung file `data/faq.json`.

### Algoritma Pencocokan (Rule-Based)

Saat menerima pertanyaan, `KnowledgeBase.searchFAQ()` melakukan langkah berikut:

1. **Normalisasi** — pertanyaan diubah ke *lowercase*.
2. **Tokenisasi** — pertanyaan dipecah menjadi `Set<String>` kata-kata.
3. **Scoring** — untuk setiap FAQ, dihitung berapa keyword yang cocok dengan kata-kata pertanyaan (menggunakan **exact match** via `Set.contains()` DAN **substring match** via `String.contains()` untuk keyword multi-kata).
4. **Seleksi** — FAQ dengan skor tertinggi (minimal 1 keyword cocok) dipilih sebagai jawaban.

---

## 🔧 Teknologi yang Digunakan

| Teknologi | Versi | Fungsi |
|-----------|:-----:|--------|
| **Java (JDK)** | 21 | Bahasa pemrograman utama |
| **JavaFX** | 21 | Framework GUI desktop (controls + FXML) |
| **AtlantaFX** | 2.1.0 | Library tema modern untuk JavaFX (CupertinoDark) |
| **Google GenAI SDK** | 1.1.0 | SDK resmi untuk integrasi Google Gemini AI |
| **Gson** | 2.11.0 | Serialisasi/deserialisasi JSON (FAQ & chat history) |
| **Apache Maven** | — | Build tool & manajemen dependensi |
| **FXML** | — | Deklarasi layout GUI secara terpisah dari logika |
| **CSS** | — | Styling kustom tema gelap (677 baris) |

---

## 🗂️ Diagram Kelas (Class Diagram)

```
                            ┌──────────────────┐
                            │   ChatbotApp     │
                            │  (Application)   │
                            │──────────────────│
                            │ + start(Stage)   │
                            │ + main(String[]) │
                            └────────┬─────────┘
                                     │ loads
                                     ▼
┌──────────────┐  uses   ┌───────────────────────┐  uses   ┌────────────────────┐
│ KnowledgeBase│◄────────│    ChatController      │────────►│ ChatHistoryManager │
│──────────────│         │───────────────────────│         │────────────────────│
│ - faqs: List │         │ - chatContainer       │         │ - filePath: String │
│ - filePath   │         │ - inputField          │         │ + saveHistory()    │
│──────────────│         │ - modeComboBox        │         │ + loadHistory()    │
│ + loadFile() │         │ - questionProcessor   │         │ + clearHistory()   │
│ + saveFile() │         │───────────────────────│         └────────────────────┘
│ + searchFAQ()│         │ + initialize()        │
│ + addFAQ()   │         │ + handleSendMessage() │  uses   ┌────────────────────┐
│ + updateFAQ()│         │ + displayMessage()    │────────►│  ConfigManager     │
│ + removeFAQ()│         │ + openAdminPanel()    │         │  «Singleton»       │
│ + getAllFAQs()│         └───────────┬───────────┘         │────────────────────│
└──────────────┘                     │                     │ + getInstance()    │
                                     │ uses                │ + getApiKey()      │
       ┌─────────────┐              ▼                     │ + getModel()       │
       │ ChatMessage  │    ┌──────────────────┐            │ + getAdminPassword()│
       │─────────────│    │QuestionProcessor │            └────────────────────┘
       │ - sender    │    │  (Context)       │
       │ - content   │    │──────────────────│
       │ - timestamp │    │ - strategy       │
       └─────────────┘    │ + setStrategy()  │
                          │ + processQuestion()│
       ┌──────────┐       └────────┬─────────┘
       │   FAQ    │                │ delegates to
       │──────────│                ▼
       │ - question│    ┌─────────────────────┐
       │ - answer  │    │ «interface»         │
       │ - keywords│    │ AnsweringStrategy   │
       └──────────┘    │ + getAnswer(String)  │
                        └──────┬──────┬───────┘
                               │      │
                    ┌──────────┘      └──────────┐
                    ▼                            ▼
          ┌──────────────────┐        ┌──────────────────┐
          │ RuleBasedStrategy│        │ ApiBasedStrategy  │
          │──────────────────│        │──────────────────│
          │ - knowledgeBase  │        │ - client: Client │
          │ + getAnswer()    │        │ - SYSTEM_PROMPT  │
          └──────────────────┘        │ + getAnswer()    │
                                      └──────────────────┘
```

---

## 🔄 Alur Kerja Aplikasi (Application Flow)

```
┌─────────┐    ┌──────────────┐    ┌────────────────┐    ┌──────────────┐
│  START   │───►│ ChatbotApp   │───►│ Set Theme      │───►│ Load FXML    │
│          │    │  .main()     │    │ CupertinoDark  │    │ chat-view    │
└─────────┘    └──────────────┘    └────────────────┘    └──────┬───────┘
                                                                │
                                                                ▼
┌──────────────────────────────────────────────────────────────────────────┐
│                    ChatController.initialize()                          │
│  1. Load FAQ dari data/faq.json → KnowledgeBase                        │
│  2. Buat RuleBasedStrategy (default)                                   │
│  3. Buat QuestionProcessor dengan strategy                             │
│  4. Load riwayat chat dari data/chat_history.json                      │
│  5. Setup ComboBox listener untuk pergantian mode                      │
│  6. Tampilkan pesan selamat datang                                     │
│  7. Render riwayat chat yang sudah ada                                 │
│  8. Update sidebar riwayat                                             │
└───────────────────────────────┬──────────────────────────────────────────┘
                                │
                                ▼
               ┌────────────────────────────┐
               │  Menunggu Input Pengguna   │◄──────────────────────────┐
               └──────────────┬─────────────┘                          │
                              │ User mengetik & klik Kirim             │
                              ▼                                        │
               ┌────────────────────────────┐                          │
               │ 1. Buat ChatMessage (user) │                          │
               │ 2. Tampilkan bubble biru   │                          │
               │ 3. Simpan ke history       │                          │
               │ 4. Disable input           │                          │
               │ 5. Tampilkan typing...     │                          │
               └──────────────┬─────────────┘                          │
                              │ Background Thread                      │
                              ▼                                        │
               ┌────────────────────────────┐                          │
               │ questionProcessor          │                          │
               │   .processQuestion(input)  │                          │
               │                            │                          │
               │  ┌─ Rule? → searchFAQ()    │                          │
               │  └─ API?  → Gemini call    │                          │
               └──────────────┬─────────────┘                          │
                              │ Kembali ke FX Thread                   │
                              ▼                                        │
               ┌────────────────────────────┐                          │
               │ 1. Hapus typing indicator  │                          │
               │ 2. Buat ChatMessage (bot)  │                          │
               │ 3. Tampilkan bubble gelap  │                          │
               │ 4. Simpan ke history       │                          │
               │ 5. Update sidebar          │                          │
               │ 6. Enable input kembali    │                          │
               └────────────┬───────────────┘                          │
                            │                                          │
                            └──────────────────────────────────────────┘
```

---

## 🤝 Kontribusi

Kontribusi sangat terbuka! Jika Anda ingin menambahkan fitur, memperbaiki bug, atau meningkatkan dokumentasi:

1. **Fork** repositori ini.
2. Buat **branch** fitur baru:
   ```bash
   git checkout -b fitur/NamaFiturBaru
   ```
3. Lakukan perubahan dan **commit**:
   ```bash
   git commit -m "Menambahkan NamaFiturBaru"
   ```
4. **Push** ke branch Anda:
   ```bash
   git push origin fitur/NamaFiturBaru
   ```
5. Buka **Pull Request** di GitHub.

### Ide Kontribusi

- 🌐 Menambahkan dukungan multi-bahasa (Indonesia & English).
- 📊 Menambahkan fitur statistik percakapan.
- 🔔 Menambahkan notifikasi desktop.
- 🗃️ Migrasi knowledge base dari JSON ke SQLite.
- 🧪 Menambahkan unit test untuk setiap strategi.
- 🤖 Integrasi dengan API AI lain (OpenAI, HuggingFace).

---

## 📄 Lisensi

Proyek ini dibuat untuk keperluan akademik di **Universitas Dian Nuswantoro (UDINUS)**.

---

<div align="center">

**⭐ Jika proyek ini bermanfaat, jangan lupa berikan bintang di GitHub! ⭐**

*Dibuat dengan ☕ dan semangat OOP.*

</div>
