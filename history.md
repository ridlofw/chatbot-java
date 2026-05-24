# 📜 History — Chatbot Kampus Pintar

Log aktivitas pengembangan proyek Chatbot Kampus Pintar.

---

## 2026-05-24: Inisialisasi Proyek

### Fase 1: Project Setup & Konfigurasi
- ✅ **pom.xml** — Dibuat konfigurasi Maven dengan dependensi:
  - JavaFX 21 (javafx-controls, javafx-fxml)
  - AtlantaFX 2.1.0 (modern dark theme)
  - Google GenAI SDK 1.1.0 (Gemini API)
  - Gson 2.11.0 (JSON processing)
  - Maven Compiler Plugin 3.13.0 (Java 21)
  - JavaFX Maven Plugin 0.0.8
- ✅ **.gitignore** — Dibuat dengan exclude: target/, .idea/, config.properties, .maven/, maven.zip
- ✅ **config.properties** — Dibuat konfigurasi API key Gemini, model, dan password admin
- ✅ **module-info.java** — Dibuat deklarasi modul JPMS
  - **Fix**: Modul `com.google.genai` diubah menjadi `google.genai` karena library GenAI adalah automatic module

### Fase 2: Model Classes
- ✅ **ChatMessage.java** — Model pesan chat (sender, content, timestamp)
- ✅ **FAQ.java** — Model FAQ (question, answer, keywords)
- ✅ **KnowledgeBase.java** — Manager knowledge base dengan CRUD dan pencarian keyword

### Fase 3: Strategy Pattern
- ✅ **AnsweringStrategy.java** — Interface strategy pattern
- ✅ **RuleBasedStrategy.java** — Implementasi rule-based dengan keyword matching
- ✅ **ApiBasedStrategy.java** — Implementasi Gemini API
  - **Fix**: Konstruktor `Client(apiKey)` diubah menjadi `Client()` karena SDK v1.1.0 menggunakan system property `GOOGLE_API_KEY`
- ✅ **QuestionProcessor.java** — Context class untuk strategy pattern

### Fase 4: Utility Classes
- ✅ **ChatHistoryManager.java** — Manajemen riwayat chat persisten (JSON)
- ✅ **ConfigManager.java** — Singleton konfigurasi (config.properties)

### Fase 5: GUI (FXML + CSS)
- ✅ **chat-view.fxml** — Layout utama aplikasi (BorderPane)
- ✅ **admin-view.fxml** — Layout panel admin FAQ (tanpa controller, programmatic)
- ✅ **dark-theme.css** — Custom dark theme premium (675 baris CSS)

### Fase 6: Controller & Main App
- ✅ **ChatController.java** — Controller utama (752 baris, full implementation)
  - Chat handling, strategy switching, typing indicator
  - Admin panel dengan password authentication
  - Riwayat chat sidebar
- ✅ **ChatbotApp.java** — Main application entry point

### Fase 7: Data Files
- ✅ **faq.json** — 15 FAQ default tentang kampus
- ✅ **chat_history.json** — File riwayat kosong

### Verifikasi
- ✅ **Maven Compile** — `mvn clean compile` berhasil (BUILD SUCCESS)
- ✅ **Maven diunduh lokal** — Apache Maven 3.9.16 di folder `.maven/`

### Bug Fixes & Refinements
1. **module-info.java**: `requires com.google.genai` → `requires google.genai` (automatic module)
2. **ApiBasedStrategy.java**: Inisialisasi `Client` diperbaiki menggunakan pola `Builder` (`Client.builder().apiKey(apiKey).build()`) karena SDK v1.1.0 membutuhkan API key secara eksplisit untuk inisialisasi yang stabil.
3. **ChatController.java**: Mengubah teks dropdown mode AI dari "API (Gemini AI)" menjadi "API-based" agar lebih konsisten.
4. **dark-theme.css**: Memperbaiki masalah animasi warna teks berkedip pada tabel admin dengan menghapus selektor `-fx-fill` yang bentrok dengan tema dasar AtlantaFX dan menetapkan warna yang stabil.

### Maven Setup
- ✅ Maven 3.9.16 berhasil diunduh dan dipasang secara lokal.
- ✅ Maven kemudian diinstal secara **global** di `C:\Users\Lenovo\AppData\Local\Programs\apache-maven` dan ditambahkan ke PATH sistem pengguna.
- ✅ Variabel environment `MAVEN_HOME` dan `JAVA_HOME` telah dikonfigurasi secara permanen.
