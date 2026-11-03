# AVP Blueprint — Android Video Player
> Dokumen rancangan teknis lengkap sebagai instruksi untuk AI coding agent.
> Baca seluruh dokumen ini sebelum menulis satu baris kode pun.
> Versi tech stack terakhir diverifikasi: **2 November 2026** (lihat Bagian 3).
> Patch keputusan Veo (2 November 2026): rentang dukungan **Android 10–15** (minSdk 29, targetSdk 35) dan **orientasi player bebas diatur saat runtime** — BUKAN kunci landscape di Manifest. Lihat Bagian 2, 10F, dan 12.

---

## Daftar Isi

1. [Gambaran Proyek](#1-gambaran-proyek)
2. [Batasan & Scope](#2-batasan--scope)
3. [Tech Stack](#3-tech-stack)
4. [Prinsip Arsitektur](#4-prinsip-arsitektur)
5. [Desain UI — Direction](#5-desain-ui--direction)
6. [Konfigurasi AndroidManifest](#6-konfigurasi-androidmanifest)
7. [Struktur Folder & File Lengkap](#7-struktur-folder--file-lengkap)
8. [Definisi Data Model](#8-definisi-data-model)
9. [Skema Storage](#9-skema-storage)
10. [Logic Flow Utama](#10-logic-flow-utama)
11. [Urutan Pengerjaan (Milestone)](#11-urutan-pengerjaan-milestone)
12. [Catatan Penting](#12-catatan-penting)

---

## 1. Gambaran Proyek

Aplikasi video player Android **offline-only** yang dibangun dengan Kotlin dan Jetpack Compose. Menggantikan video player bawaan yang penuh iklan dengan UI bersih, modern, dan dapat dikustomisasi.

### Tujuan Utama

- Memutar file `.mp4` dan `.mkv` (hardsub) dari penyimpanan lokal tanpa iklan
- UI fresh, modern, dan nyaman — mendukung dark/light/AMOLED mode secara dinamis
- Resume playback otomatis dari posisi terakhir per file video
- Orientasi player bebas: mengikuti orientasi perangkat secara bawaan, dapat dipaksa portrait/landscape dan dikunci/dibuka dari dalam aplikasi (lihat Bagian 10F)
- Frame capture langsung dari buffer video decoder (bukan screenshot layar)
- Kustomisasi terkendali: gaya judul player, posisi tombol via slot system, tema warna
- Filter whitelist folder: tampilkan hanya folder yang dipilih user
- Playlist manual buatan user

---

## 2. Batasan & Scope

| Kategori | Keputusan |
|---|---|
| Platform | Android only |
| Bahasa | Kotlin |
| UI Framework | Jetpack Compose 100% (`AndroidView` wrapping `PlayerView` di layar player) |
| Navigasi | **Multiple Activities** — bukan Fragment/NavComponent |
| Playback engine | Media3 ExoPlayer |
| Orientasi player | **Bebas, diatur saat runtime** — bawaan mengikuti perangkat (`FULL_USER`); tombol rotasi memaksa portrait/landscape (mengalahkan kunci rotasi HP); kembali ke mode AUTO = membuka kunci orientasi buatan aplikasi. **DILARANG** mengunci `screenOrientation="landscape"` di Manifest (lihat Bagian 10F dan 12) |
| Koneksi jaringan | ❌ Tidak ada (offline-only) |
| Background playback | ❌ Tidak — video wajib stop saat `onStop()` |
| Picture in Picture | ❌ Di-defer ke versi berikutnya |
| Floating player | ❌ Di-defer |
| Subtitle eksternal | ❌ Di-defer |
| Audio track switching | ❌ Di-defer |
| Frame capture | ✅ Ya — `MediaMetadataRetriever`, **bukan** Android ScreenCapture API |
| minSdk | **API 29** (Android 10) — rentang dukungan yang disepakati: Android 10–15 |
| targetSdk | **API 35** (Android 15) — sengaja ditahan di 35 sebagai batas atas rentang dukungan; perilaku orientasi & izin Android 10–15 adalah yang dipakai spek ini (lihat Bagian 12) |
| compileSdk | **37** — hanya urusan build (library 2026 membutuhkannya); compileSdk TIDAK menentukan dukungan perangkat, yang menentukan adalah minSdk/targetSdk |

---

## 3. Tech Stack

Versi di bawah adalah **versi stabil terbaru yang berhasil diverifikasi per 2 November 2026**. Penulisan `x` berarti: pakai patch terbaru dari seri tersebut. Tetap cek Google Maven / Maven Central sebelum implementasi — terutama KSP dan Hilt yang sering merilis patch.

### Build

```
Gradle                              9.5+ (9.6.x direkomendasikan)   ← syarat minimum AGP 9.3
JDK                                 17
Android Gradle Plugin (AGP)         9.3.2    ← Kotlin sudah built-in; plugin org.jetbrains.kotlin.android TIDAK dipakai
Kotlin                              2.4.21
Compose Compiler Gradle Plugin      2.4.21   ← org.jetbrains.kotlin.plugin.compose (versi selalu sama dengan Kotlin)
Kotlin Serialization Gradle Plugin  2.4.21   ← org.jetbrains.kotlin.plugin.serialization (versi sama dengan Kotlin)
Kotlin Symbol Processing (KSP)      2.3.12   ← gunakan KSP, bukan kapt; versi KSP tidak lagi terikat versi Kotlin
```

### UI

```
Jetpack Compose BOM                 2026.09.00   (jangan tulis versi manual untuk library di bawah ini)
  compose.ui                        1.12.1       ← dikelola BOM
  compose.foundation                1.12.1
  compose.runtime                   1.12.1
  compose.animation                 1.12.1
  compose.animation.graphics        1.12.1
  compose.material3                 1.4.0        ← stabil; 1.5.0 masih beta/alpha, jangan dipakai
  compose.material3.adaptive        1.3.0        ← untuk adaptive layout
  compose.material:material-icons-extended  1.7.8   ← versi dibekukan, tetap dikelola BOM; dibutuhkan untuk Icons.Rounded.*
  compose.ui.tooling (debug only)

Activity Compose                    1.13.0
```

### Playback

```
androidx.media3:media3-exoplayer    1.11.x
androidx.media3:media3-ui           1.11.x   ← PlayerView untuk AndroidView
androidx.media3:media3-common       1.11.x
```

### Dependency Injection

```
Hilt Android                        2.59.2   ← Hilt Gradle Plugin 2.59+ mewajibkan AGP 9.0+
Hilt Compiler (KSP)                 2.59.2
Hilt Gradle Plugin                  2.59.2   (com.google.dagger.hilt.android)
```

### Storage

```
Room Runtime   androidx.room3:room3-runtime    3.0.3   ← Room 3.0: package baru androidx.room3
Room Compiler  androidx.room3:room3-compiler   3.0.3   (KSP wajib; kapt / Java annotation processor tidak didukung)
Room Gradle Plugin  androidx.room3             3.0.3   (schemaDirectory wajib diset)
  (tidak ada artifact Room KTX terpisah — dukungan coroutine sudah bawaan)
DataStore Preferences               1.2.x    ← 1.3.0 masih alpha
```

### Lifecycle & Async

```
Lifecycle ViewModel                 2.11.x   (androidx.lifecycle:lifecycle-viewmodel; API KTX sudah ada di artifact utama sejak 2.8)
Lifecycle ViewModel Compose         2.11.x
Lifecycle Runtime Compose           2.11.x
Kotlinx Coroutines Android          1.11.x
Kotlinx Coroutines Core             1.11.x
```

### Image (Thumbnail)

```
Coil Compose   io.coil-kt.coil3:coil-compose   3.5.x   ← Coil 3: package coil3.*, bukan coil.*
Coil Video     io.coil-kt.coil3:coil-video     3.5.x   ← thumbnail video lokal (VideoFrameDecoder)
(JANGAN tambahkan coil-network-* — aplikasi offline-only)
```

### Serialisasi (untuk DataStore config)

```
Kotlinx Serialization JSON          1.11.x   (butuh Kotlin Serialization Gradle Plugin, lihat bagian Build)
```

### Permissions

```
Accompanist Permissions             0.37.3   ← rilis terakhir; library "labs" yang jarang diperbarui
                                               (alternatif tanpa dependensi: rememberLauncherForActivityResult
                                                + ActivityResultContracts.RequestPermission)
```

### Catatan Kompatibilitas Versi

- **AGP 9 + Kotlin built-in**: jangan terapkan `org.jetbrains.kotlin.android`. KSP (2.3.1+) dan Hilt Gradle Plugin (2.59+) sudah mendukung AGP 9; Hilt Gradle Plugin bahkan mewajibkan AGP 9.0+.
- **Room 3 (breaking terhadap Room 2.x)**: package/artifact baru (`androidx.room3`), KSP wajib, semua fungsi DAO non-Flow wajib `suspend`, `SQLiteDriver` wajib dipasang di builder (mis. `AndroidSQLiteDriver` dari `androidx.sqlite:sqlite-framework`, atau `BundledSQLiteDriver` dari `androidx.sqlite:sqlite-bundled`), dan callback migrasi memakai `SQLiteConnection`, bukan `SupportSQLiteDatabase`. Jika ingin tetap di Room 2.x, seri terakhirnya 2.8.x (`androidx.room`).
- **Coil 3**: package `coil3.*` (mis. `coil3.compose.AsyncImage`, `coil3.video.VideoFrameDecoder`). Tidak perlu modul `coil-network-*`.
- **Material 3**: stabil di 1.4.0. `material-icons-extended` dibekukan di 1.7.8 tetapi tetap dibutuhkan untuk `Icons.Rounded.*`.
- **Media3 1.11**: `PlayerView` di dalam `AndroidView` tetap didukung. Modul `media3-ui-compose(-material3)` tidak dipakai karena kontrol player memakai slot system kustom.
- **compileSdk 37 vs targetSdk 35**: compileSdk adalah versi API yang dipakai saat kompilasi (library AndroidX 2026 mensyaratkan yang tinggi), sedangkan targetSdk adalah level perilaku OS yang diadopsi aplikasi. Aplikasi ini ditargetkan ke API 35 (Android 15) agar perilaku orientasi & izinnya sesuai rentang dukungan Android 10–15; compileSdk yang lebih tinggi tidak mengubah dukungan perangkat (minSdk 29 tetap yang menentukan HP bisa memasang aplikasi). Aturan orientasi layar lebar untuk target 36+ TIDAK diadopsi — lihat Bagian 12 bila targetSdk suatu saat dinaikkan melewati 35.

---

## 4. Prinsip Arsitektur

### Pola: MVVM + UiState + StateFlow

Setiap Activity memiliki satu ViewModel. Komunikasi antara UI dan ViewModel sepenuhnya melalui `StateFlow<XxxUiState>`. UI bersifat **stateless dan reaktif** — hanya merender apa yang ada di dalam state.

```
Activity (setContent { AppTheme { RootScreen(viewModel) } })
  └── ViewModel — StateFlow<UiState>
        ├── Repository — aggregasi logika bisnis
        │     ├── DataSource — akses raw data (MediaStore / file)
        │     └── Storage — Room / DataStore
        └── Manager — VideoPlayerManager (kontrol ExoPlayer)
```

### Aturan Pemisahan Layer (Wajib Ditaati)

| Layer | Tanggung Jawab | Yang Dilarang |
|---|---|---|
| Activity + Composable | Render state, kirim event ke ViewModel | Akses storage atau player secara langsung |
| ViewModel | Logika UI, orkestrasi repository | Memegang referensi `Context` secara langsung |
| Repository | Gabung data dari beberapa source | Tahu tentang UI atau ViewModel |
| DataSource | Akses raw data (MediaStore query, file read) | Tahu tentang business logic |
| VideoPlayerManager | Kontrol ExoPlayer secara eksklusif | Tahu tentang ViewModel atau UI |

### Aturan Kritis Player

- Video **wajib** di-release di `onStop()` — bukan `onPause()`, bukan `onDestroy()`
- `PlayerActivity` bertanggung jawab memanggil `viewModel.onStop()` di `override fun onStop()`
- `VideoPlayerManager.release()` wajib memanggil `exoPlayer.stop()` lalu `exoPlayer.release()`
- Tidak ada kode ExoPlayer yang boleh ditulis di Activity atau Composable secara langsung

### Aturan Konfigurasi

- Tidak boleh ada warna, ukuran font, atau konfigurasi tombol yang di-hardcode di Composable
- Semua konfigurasi tampilan dibaca dari `PlayerUiConfig` yang bersumber dari `SettingsRepository`

---

## 5. Desain UI — Direction

### Filosofi Visual

- **Material Design 3** sebagai fondasi: rounded corners, elevation subtle, color roles
- **Minimalis tapi informatif**: informasi yang dibutuhkan tersedia tanpa kepadatan visual berlebihan
- **Konsistensi gerak**: gunakan `AnimatedVisibility`, `animateContentSize`, dan `Crossfade` untuk transisi state
- **Dark-first**: desain dimulai dari dark theme, light theme menyesuaikan

### Home Screen — Bottom Navigation Bar

Navigasi utama menggunakan `NavigationBar` (Material3) di bagian **bawah** layar — bukan tabs di atas.

```
┌──────────────────────────────┐
│  [Search bar / App bar]      │
│                              │
│   Konten Tab Aktif           │
│   (Video / Folder /          │
│    Daftar Putar)             │
│                              │
│                              │
├──────────────────────────────┤
│  [🎬 Video] [📁 Folder] [≡ Putar] │  ← NavigationBar
└──────────────────────────────┘
```

Ikon untuk setiap tab (gunakan `Icons.Rounded` dari Material):

| Tab | Label | Ikon (selected) | Ikon (unselected) |
|---|---|---|---|
| Video | "Video" | `PlayCircle` (filled) | `PlayCircle` (outlined) |
| Folder | "Folder" | `Folder` (filled) | `FolderOpen` |
| Daftar Putar | "Putar" | `QueueMusic` (filled) | `QueueMusic` (outlined) |

> Catatan: `Icons.Rounded.*` berasal dari `material-icons-extended` (lihat Bagian 3). Ikon yang punya varian AutoMirrored (mis. `QueueMusic`) gunakan `Icons.AutoMirrored.Rounded.*`.

### Video List (Tab Video)

Layout: `LazyColumn` dengan item card bergaya modern.

Tiap item menampilkan:
- Thumbnail video (aspect ratio 16:9, rounded corner `8.dp`, placeholder shimmer saat loading)
- Nama file video (maks 2 baris, ellipsis di akhir)
- Badge durasi di pojok kanan bawah thumbnail (background gelap semi-transparan, teks putih kecil)
- Chip folder asal di bawah nama (icon folder kecil + nama folder, `FilledTonalChip`)
- Ukuran file (kanan bawah item)
- Tombol `⋮` (kebab menu) di kanan: untuk opsi "Ganti Nama", "Properti", "Tambah ke Playlist", "Hapus dari Daftar", dll.
- Efek ripple saat ditekan

### Folder Grid (Tab Folder)

Layout: `LazyVerticalGrid` dengan 2 kolom.

Tiap item menampilkan:
- Ikon folder besar dengan warna accent (bukan ikon folder generik OS)
- Jika folder berasal dari app tertentu (deteksi dari path, contoh path mengandung "WhatsApp"), tampilkan badge ikon app kecil di pojok kanan atas ikon folder
- Nama folder (maks 2 baris)
- Judul video count ("12 video") dengan warna subdued

### Playlist Screen (Tab Daftar Putar)

Layout: `LazyColumn` dengan card per playlist.

Tiap card menampilkan:
- Strip thumbnail gabungan (2–3 thumbnail pertama dari video dalam playlist, disusun bertumpuk dengan slight offset)
- Nama playlist (bold)
- Jumlah video dan total durasi
- FAB `+` di pojok kanan bawah untuk buat playlist baru

### Player Screen

Overlay kontrol muncul/hilang dengan animasi fade. Saat visible:

```
┌─────────────────────────────────────┐
│ [←Back] Judul video          [⟳] [⋮Menu]│  ← TopBar (transparan); ⟳ = tombol rotasi (slot ORIENTATION)
│                                     │
│     ← Area Video (AndroidView) →    │
│                                     │
│ [⏮]       [▶/⏸] [⏭]    [1.0x][📷]│  ← BottomControls
│ [🔒]  ══════════════ 01:23 / 23:51  │  ← SeekBar + waktu
└─────────────────────────────────────┘
```

Layout kontrol berbasis **slot system** (posisi dapat ditukar via kustomisasi).

Orientasi layar **tidak** dikunci landscape: player harus tampil baik dalam portrait maupun landscape. Dalam portrait, area video tampil dengan rasio aslinya (letterbox) dan kontrol tetap terjangkau; dalam landscape video memenuhi layar. Tombol rotasi (slot `ORIENTATION`) menunjukkan mode aktif — Auto (ikuti HP) / Landscape / Portrait — lihat Bagian 10F.

---

## 6. Konfigurasi AndroidManifest

### Permissions

```xml
<!-- Storage — untuk scan video -->
<uses-permission android:name="android.permission.READ_MEDIA_VIDEO" />
<uses-permission
    android:name="android.permission.READ_EXTERNAL_STORAGE"
    android:maxSdkVersion="32" />

<!-- minSdk 29: simpan frame capture lewat MediaStore (RELATIVE_PATH, API 29+) — izin WRITE_EXTERNAL_STORAGE tidak diperlukan di seluruh rentang dukungan -->
```

### Activity Declarations

```xml
<!-- HomeActivity: portrait, entry point -->
<activity
    android:name=".ui.home.HomeActivity"
    android:launchMode="singleTop"
    android:exported="true"
    android:screenOrientation="portrait"
    android:windowSoftInputMode="adjustResize">
    <intent-filter>
        <action android:name="android.intent.action.MAIN" />
        <category android:name="android.intent.category.LAUNCHER" />
    </intent-filter>
</activity>

<!-- PlayerActivity: orientasi BEBAS diatur runtime (Bagian 10F), immersive, keep screen on.
     JANGAN pasang screenOrientation di sini. -->
<activity
    android:name=".ui.player.PlayerActivity"
    android:exported="false"
    android:configChanges="orientation|screenSize|screenLayout|keyboardHidden|smallestScreenSize"
    android:windowSoftInputMode="adjustNothing" />

<!-- CustomizationActivity: portrait -->
<activity
    android:name=".ui.customization.CustomizationActivity"
    android:exported="false"
    android:screenOrientation="portrait" />
```

### Intent Extras untuk PlayerActivity

```
Extra KEY (simpan di Constants.kt):
  VIDEO_PATH_KEY   = "extra_video_path"    → String, full path file video
  VIDEO_ID_KEY     = "extra_video_id"      → String, hash unik dari path
  VIDEO_TITLE_KEY  = "extra_video_title"   → String, nama tampilan video
```

### Flag yang Wajib Dipasang di PlayerActivity.onCreate()

```kotlin
window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
enableEdgeToEdge()   // androidx.activity, panggil sebelum setContent. Edge-to-edge sudah wajib untuk targetSdk 35+, setDecorFitsSystemWindows tidak dipakai lagi
// Aktifkan immersive mode via WindowInsetsControllerCompat (hide systemBars())
// Orientasi: JANGAN dipaksa landscape. PlayerActivity mengobservasi uiState.orientationMode
// dan memasang requestedOrientation yang sesuai (bawaan AUTO → SCREEN_ORIENTATION_FULL_USER).
// Lihat Bagian 10F.
```

---

## 7. Struktur Folder & File Lengkap

```
app/src/main/java/com/[package]/avp/
│
├── MainApplication.kt
│     Anotasi @HiltAndroidApp. Entry point global aplikasi.
│
├── di/
│   ├── AppModule.kt
│   │     @Module @InstallIn(SingletonComponent)
│   │     @Provides untuk: DataStore instance, AppDatabase,
│   │     semua DAO, FileScannerDataSource, PreferenceDataSource,
│   │     semua Repository, VideoFrameCapturer.
│   │
│   └── PlayerModule.kt
│         @Module @InstallIn(ActivityComponent)
│         @Provides @ActivityScoped: VideoPlayerManager.
│         VideoPlayerManager scope-nya Activity karena terikat
│         lifecycle PlayerActivity, bukan Application.
│
├── data/
│   │
│   ├── model/
│   │   ├── VideoItem.kt               → lihat Bagian 8
│   │   ├── FolderItem.kt              → lihat Bagian 8
│   │   ├── PlaybackState.kt           → lihat Bagian 8
│   │   ├── PlayerUiConfig.kt          → lihat Bagian 8
│   │   ├── TitleConfig.kt             → lihat Bagian 8
│   │   ├── ControlLayoutConfig.kt     → lihat Bagian 8
│   │   ├── ControlSlot.kt             → lihat Bagian 8
│   │   ├── ThemeConfig.kt             → lihat Bagian 8
│   │   ├── FolderVisibilityConfig.kt  → lihat Bagian 8
│   │   └── Playlist.kt                → lihat Bagian 8
│   │
│   ├── db/
│   │   ├── AppDatabase.kt
│   │   │     Room 3 (package androidx.room3.*) @Database class. Version = 1.
│   │   │     Entities: PlaybackStateEntity, PlaylistEntity, PlaylistEntryEntity.
│   │   │     Export schema lewat Room Gradle Plugin (room3 { schemaDirectory(...) }),
│   │   │     commit file JSON-nya ke repo untuk migrasi masa depan.
│   │   │     Builder wajib memasang driver:
│   │   │       Room.databaseBuilder<AppDatabase>(context, "avp.db")
│   │   │         .setDriver(AndroidSQLiteDriver()).build()
│   │   │     (jika KSP meminta @ConstructedBy, ikuti dokumentasi Room 3)
│   │   │
│   │   ├── entity/
│   │   │   ├── PlaybackStateEntity.kt
│   │   │   │     @Entity(tableName = "playback_state")
│   │   │   │     @PrimaryKey val videoId: String
│   │   │   │     val videoPath: String
│   │   │   │     val positionMs: Long
│   │   │   │     val updatedAt: Long (System.currentTimeMillis())
│   │   │   │
│   │   │   ├── PlaylistEntity.kt
│   │   │   │     @Entity(tableName = "playlists")
│   │   │   │     @PrimaryKey(autoGenerate = true) val id: Long = 0
│   │   │   │     val name: String
│   │   │   │     val createdAt: Long
│   │   │   │
│   │   │   └── PlaylistEntryEntity.kt
│   │   │         @Entity(tableName = "playlist_entries",
│   │   │                 foreignKeys = [ForeignKey(PlaylistEntity, "id", "playlistId",
│   │   │                                onDelete = CASCADE)])
│   │   │         @PrimaryKey(autoGenerate = true) val id: Long = 0
│   │   │         val playlistId: Long
│   │   │         val videoPath: String
│   │   │         val orderIndex: Int
│   │   │
│   │   └── dao/        ← Room 3: semua fungsi non-Flow WAJIB `suspend`
│   │       ├── PlaybackStateDao.kt
│   │       │     @Dao
│   │       │     @Upsert suspend fun upsert(entity: PlaybackStateEntity)
│   │       │     @Query suspend fun getByVideoId(videoId: String): PlaybackStateEntity?
│   │       │     @Query suspend fun deleteAll()
│   │       │
│   │       ├── PlaylistDao.kt
│   │       │     @Dao
│   │       │     @Insert suspend fun insertPlaylist(entity: PlaylistEntity): Long
│   │       │     @Delete suspend fun deletePlaylist(entity: PlaylistEntity)
│   │       │     @Query fun getAllPlaylists(): Flow<List<PlaylistEntity>>
│   │       │     @Query suspend fun getPlaylistById(id: Long): PlaylistEntity?
│   │       │     @Update suspend fun updatePlaylist(entity: PlaylistEntity)
│   │       │
│   │       └── PlaylistEntryDao.kt
│   │             @Dao
│   │             @Insert suspend fun insertEntry(entry: PlaylistEntryEntity): Long
│   │             @Delete suspend fun deleteEntry(entry: PlaylistEntryEntity)
│   │             @Query fun getEntriesByPlaylistId(playlistId: Long): Flow<List<PlaylistEntryEntity>>
│   │             @Query suspend fun deleteAllByPlaylistId(playlistId: Long)
│   │             @Update suspend fun updateEntry(entry: PlaylistEntryEntity)
│   │             (untuk reorder: fungsi `suspend` ber-@Transaction yang meng-update semua orderIndex sekaligus)
│   │
│   ├── source/
│   │   ├── FileScannerDataSource.kt
│   │   │     Sumber data tunggal untuk scan file dari MediaStore.
│   │   │     Menggunakan context.contentResolver.query() ke MediaStore.Video.Media.
│   │   │
│   │   │     Kolom yang di-query dari MediaStore:
│   │   │       _ID, DISPLAY_NAME, DATA (full path), DURATION, SIZE,
│   │   │       BUCKET_DISPLAY_NAME (nama folder), BUCKET_ID, DATE_ADDED
│   │   │
│   │   │     Filter ekstensi (pagar Veo 2026-11-03): hanya .mp4/.mkv/.mov
│   │   │       yang dikembalikan; ekstensi lain (mis. .ts) disaring di
│   │   │       queryVideos() sehingga tidak tampil di mana pun.
│   │   │     Layout daftar (referensi Veo 2026-11-03): metadata satu baris
│   │   │       "720P  188,19 MB" (WIDTH/HEIGHT + SIZE); bar progres tontonan
│   │   │       di tepi bawah thumbnail; judul video terakhir diputar berwarna
│   │   │       aksen; bar mini video terakhir di atas navigasi (opsional).
│   │   │       Data progres/terakhir dari PlaybackRepository (playback_state).
│   │   │     fun scanAllVideos(): List<VideoItem>
│   │   │       → query semua video, sort by DATE_ADDED DESC
│   │   │
│   │   │     fun scanVideosByFolder(folderPath: String): List<VideoItem>
│   │   │       → query video dalam satu folder saja (WHERE BUCKET_ID = ?)
│   │   │
│   │   │     fun scanAllFolders(): List<FolderItem>
│   │   │       → query DISTINCT BUCKET_ID, BUCKET_DISPLAY_NAME, DATA, COUNT(*)
│   │   │       → ambil satu thumbnail path per folder
│   │   │       → hasilnya: list folder dengan jumlah video dan path representatif
│   │   │
│   │   │     Handling permission:
│   │   │       API >= 33: butuh READ_MEDIA_VIDEO
│   │   │       API < 33 : butuh READ_EXTERNAL_STORAGE
│   │   │       Jika tidak ada permission: kembalikan list kosong, bukan crash
│   │   │
│   │   └── PreferenceDataSource.kt
│   │         Wrapper tunggal untuk DataStore<Preferences>.
│   │         Semua akses baca/tulis DataStore hanya melalui file ini.
│   │         Setiap key didefinisikan sebagai private val di dalam file ini.
│   │
│   │         Keys yang dikelola (semuanya dari Constants.kt):
│   │           THEME_MODE           : String  ("SYSTEM"|"LIGHT"|"DARK"|"AMOLED")
│   │           ACCENT_COLOR         : Long    (color int, default = lavender 0xFF6655F5 — keputusan aksen 3 Nov 2026, lihat UI.md PT-25)
│   │           USE_SYSTEM_ACCENT    : Boolean (dynamic color Android 12+)
│   │           TITLE_FONT_SIZE_SP   : Float   (range 10f–22f, default 14f)
│   │           TITLE_MAX_LINES      : Int     (1 atau 2, default 1)
│   │           TITLE_COLOR          : Long    (default = white)
│   │           TITLE_IS_BOLD        : Boolean (default false)
│   │           TITLE_MARQUEE        : Boolean (default true)
│   │           CONTROL_LAYOUT_JSON  : String  (JSON dari ControlLayoutConfig)
│   │           FOLDER_FILTER_MODE   : String  ("ALL"|"WHITELIST_ONLY")
│   │           WHITELISTED_PATHS    : String  (JSON array of paths)
│   │           SEEK_STEP_MS         : Int     (default 10000 = 10 detik)
│   │   │           ORIENTATION_MODE     : String  ("AUTO"|"LANDSCAPE"|"PORTRAIT", default "AUTO")
│   │
│   │         fun getThemeMode(): Flow<String>
│   │         fun setThemeMode(mode: String)
│   │         fun getAccentColor(): Flow<Long>
│   │         fun setAccentColor(color: Long)
│   │         ... (satu pasang getter/setter Flow per key)
│   │
│   └── repository/
│       ├── VideoRepository.kt
│       │     Mengorkestrasi FileScannerDataSource untuk daftar video.
│       │
│       │     fun getAllVideos(): Flow<List<VideoItem>>
│       │       → wrap scanAllVideos() dalam Flow (emitkan ulang jika ada perubahan)
│       │
│       │     fun getVideosByFolder(folderPath: String): Flow<List<VideoItem>>
│       │       → wrap scanVideosByFolder()
│       │
│       │     fun searchVideos(query: String): Flow<List<VideoItem>>
│       │       → filter dari getAllVideos() berdasarkan query pada nama file
│       │
│       ├── FolderRepository.kt
│       │     Menggabungkan hasil scan folder dengan FolderVisibilityConfig.
│       │     Filter whitelist diterapkan di sini, bukan di ViewModel atau UI.
│       │
│       │     fun getVisibleFolders(): Flow<List<FolderItem>>
│       │       → combine(scanAllFolders(), getVisibilityConfig())
│       │       → jika mode WHITELIST_ONLY:
│       │           filter { normalizePath(it.path) in normalizedWhitelistedPaths }
│       │       → jika mode ALL: return semua
│       │       → tandai setiap FolderItem.isWhitelisted sesuai config
│       │
│       ├── PlaybackRepository.kt
│       │     Menyimpan dan mengambil posisi terakhir pemutaran per video.
│       │
│       │     suspend fun savePosition(videoId: String, videoPath: String, positionMs: Long)
│       │       → upsert ke PlaybackStateDao
│       │
│       │     suspend fun getLastPosition(videoId: String): Long
│       │       → getByVideoId dari DAO, return positionMs atau 0L jika null
│       │
│       │     suspend fun clearAllPositions()
│       │
│       ├── PlaylistRepository.kt
│       │     Manajemen playlist manual melalui Room.
│       │
│       │     suspend fun createPlaylist(name: String): Long
│       │     suspend fun deletePlaylist(id: Long)
│       │     suspend fun renamePlaylist(id: Long, newName: String)
│       │     fun getAllPlaylists(): Flow<List<PlaylistInfo>>
│       │     suspend fun addVideoToPlaylist(playlistId: Long, videoPath: String)
│       │     suspend fun removeVideoFromPlaylist(entryId: Long)
│       │     fun getPlaylistEntries(playlistId: Long): Flow<List<PlaylistEntry>>
│       │     suspend fun reorderEntries(entries: List<PlaylistEntryEntity>)
│       │       → @Transaction update semua orderIndex sekaligus
│       │
│       └── SettingsRepository.kt
│             Single source of truth untuk semua konfigurasi dan pengaturan UI.
│             Semua akses ke PreferenceDataSource harus melalui repository ini.
│
│             fun getThemeConfig(): Flow<ThemeConfig>
│             suspend fun updateThemeConfig(config: ThemeConfig)
│             fun getTitleConfig(): Flow<TitleConfig>
│             suspend fun updateTitleConfig(config: TitleConfig)
│             fun getControlLayoutConfig(): Flow<ControlLayoutConfig>
│             suspend fun updateControlLayoutConfig(config: ControlLayoutConfig)
│             suspend fun resetControlLayoutToDefault()
│             fun getFolderVisibilityConfig(): Flow<FolderVisibilityConfig>
│             suspend fun updateFolderVisibilityConfig(config: FolderVisibilityConfig)
│             fun getSeekStepMs(): Flow<Int>
│   │   │             fun getOrientationMode(): Flow<OrientationMode>
│             fun getPlayerUiConfig(): Flow<PlayerUiConfig>
│               → combine(getTitleConfig(), getControlLayoutConfig(), getThemeConfig())
│
├── player/
│   │
│   ├── core/
│   │   ├── VideoPlayerManager.kt
│   │   │     Jantung aplikasi. Wrapper eksklusif untuk semua interaksi ExoPlayer.
│   │   │     Scope: @ActivityScoped via Hilt (satu instance per PlayerActivity).
│   │   │     Di-inject ke PlayerViewModel, tidak langsung ke Activity atau UI.
│   │   │
│   │   │     val player: ExoPlayer   ← property yang diberikan ke VideoSurface
│   │   │
│   │   │     fun prepare(videoPath: String, startPositionMs: Long)
│   │   │       → MediaItem.fromUri(Uri.parse(videoPath))
│   │   │       → exoPlayer.setMediaItem(mediaItem)
│   │   │       → exoPlayer.seekTo(startPositionMs)
│   │   │       → exoPlayer.prepare()
│   │   │       → exoPlayer.playWhenReady = true
│   │   │
│   │   │     fun play()
│   │   │     fun pause()
│   │   │     fun seekTo(positionMs: Long)
│   │   │     fun seekForward(stepMs: Long)    → seekTo(currentPosition + stepMs)
│   │   │     fun seekBackward(stepMs: Long)   → seekTo(currentPosition - stepMs).coerceAtLeast(0)
│   │   │     fun setSpeed(speed: Float)       → exoPlayer.setPlaybackSpeed(speed)
│   │   │     fun getCurrentPosition(): Long   → exoPlayer.currentPosition
│   │   │     fun getDuration(): Long          → exoPlayer.duration
│   │   │     fun isPlaying(): Boolean         → exoPlayer.isPlaying
│   │   │
│   │   │     fun release()
│   │   │       → exoPlayer.stop()
│   │   │       → exoPlayer.release()
│   │   │       (WAJIB dipanggil dari onStop PlayerActivity)
│   │   │
│   │   ├── PlayerEventListener.kt
│   │   │     Mengimplementasi Player.Listener dari Media3.
│   │   │     Mengubah event ExoPlayer menjadi PlayerEvent yang lebih sederhana.
│   │   │     Di-register ke VideoPlayerManager, callback-nya dikirim ke ViewModel via Flow/callback.
│   │   │
│   │   │     Events yang diteruskan:
│   │   │       onPlaybackStateChanged(state: Int)
│   │   │         → STATE_IDLE, STATE_BUFFERING, STATE_READY, STATE_ENDED
│   │   │       onIsPlayingChanged(isPlaying: Boolean)
│   │   │       onPlayerError(error: PlaybackException)
│   │   │       onVideoSizeChanged(videoSize: VideoSize)
│   │   │         → untuk hitung aspect ratio
│   │   │       onPositionDiscontinuity(...)
│   │   │         → untuk deteksi playback selesai / next track
│   │   │
│   │   └── PlayerState.kt
│   │         Sealed class status player — dikonsumsi ViewModel, bukan ExoPlayer.
│   │
│   │         sealed class PlayerState {
│   │           object Idle : PlayerState()
│   │           object Loading : PlayerState()
│   │           object Playing : PlayerState()
│   │           object Paused : PlayerState()
│   │           object Ended : PlayerState()
│   │           data class Error(val message: String) : PlayerState()
│   │         }
│   │
│   ├── capture/
│   │   └── VideoFrameCapturer.kt
│   │         Mengambil frame video menggunakan MediaMetadataRetriever.
│   │         TIDAK menggunakan Android ScreenCapture API.
│   │         TIDAK mengambil screenshot layar.
│   │         Hasilnya adalah frame mentah dari decoder video — tanpa UI, tanpa black bar.
│   │
│   │         suspend fun captureFrame(
│   │           videoPath: String,
│   │           positionMs: Long
│   │         ): Result<Bitmap>
│   │           → MediaMetadataRetriever().apply { setDataSource(videoPath) }
│   │           → .getFrameAtTime(
│   │                positionMs * 1000L,      ← MICROSECONDS (bukan ms!)
│   │                OPTION_CLOSEST_SYNC
│   │              )
│   │           → cek EXIF rotation via BitmapUtils.rotateBitmapIfNeeded()
│   │           → return Result.success(bitmap) atau Result.failure(exception)
│   │
│   │         suspend fun saveFrameToGallery(
│   │           bitmap: Bitmap,
│   │           videoTitle: String
│   │         ): Result<Uri>
│   │           → ContentValues {
│   │               MIME_TYPE = "image/jpeg"
│   │               RELATIVE_PATH = "Pictures/AVP"     ← API 29+
│   │               DISPLAY_NAME = "avp_${timestamp}.jpg"
│   │               IS_PENDING = 1
│   │             }
│   │           → insert ke MediaStore.Images.Media.EXTERNAL_CONTENT_URI
│   │           → buka outputStream → bitmap.compress(JPEG, 95, stream)
│   │           → update IS_PENDING = 0 (publish)
│   │           → return Result.success(uri)
│   │
│   └── gesture/
│       └── GestureZoneCalculator.kt
│             Utility stateless untuk kalkulasi zona dan nilai dari gesture.
│
│             enum class GestureZone { BRIGHTNESS, VOLUME, NONE }
│
│             fun getZone(x: Float, screenWidth: Float): GestureZone
│               → x < screenWidth * 0.5f → BRIGHTNESS (paruh kiri)
│               → else                   → VOLUME (paruh kanan)
│               (Koreksi Veo 2026-11-04: zona hanya untuk gesture VERTIKAL;
│                gesture HORIZONTAL di mana pun = seek — arah menentukan
│                aksi, bukan posisi. Model tiga zona versi awal diganti.)
│
│             fun calculateBrightnessDelta(dy: Float, screenHeight: Float): Float
│               → dy / screenHeight * -1f  (drag ke atas = terang)
│               → clamp ke -1f..1f
│
│             fun calculateVolumeDelta(dy: Float, screenHeight: Float): Float
│               → dy / screenHeight * -1f  (drag ke atas = keras)
│               → clamp ke -1f..1f
│
│             fun calculateSeekDeltaMs(
│               dx: Float,
│               screenWidth: Float,
│               durationMs: Long
│             ): Long
│               → (dx / screenWidth) * durationMs * seekSensitivity
│               → seekSensitivity default = 1.5f (bisa dikustomisasi)
│
├── ui/
│   │
│   ├── theme/
│   │   ├── Color.kt
│   │   │     Mendefinisikan semua konstanta warna default.
│   │   │     Dua set color scheme: LightColorScheme, DarkColorScheme.
│   │   │     AmoledColorScheme: identik DarkColorScheme kecuali
│   │   │       background = Color(0xFF000000), surface = Color(0xFF000000)
│   │   │
│   │   │     fun buildDynamicColorScheme(
│   │   │       accentColor: Color,
│   │   │       isDark: Boolean
│   │   │     ): ColorScheme
│   │   │       → gunakan MaterialTheme color roles dengan accentColor sebagai primary
│   │   │
│   │   ├── Typography.kt
│   │   │     Mendefinisikan Typography Material3 untuk seluruh aplikasi.
│   │   │     Font: gunakan font system default (tidak perlu bundled font dulu).
│   │   │
│   │   └── Theme.kt
│   │         Composable utama: AppTheme(themeConfig: ThemeConfig, content: @Composable () -> Unit)
│   │
│   │         Logika di dalam AppTheme:
│   │           val isDarkSystem = isSystemInDarkTheme()
│   │           val colorScheme = when (themeConfig.themeMode) {
│   │             SYSTEM   → if (isDarkSystem) DarkColorScheme else LightColorScheme
│   │             LIGHT    → LightColorScheme
│   │             DARK     → DarkColorScheme
│   │             AMOLED   → AmoledColorScheme
│   │           }
│   │           val finalScheme = if (themeConfig.useSystemAccent && Build.VERSION.SDK_INT >= 31) {
│   │             dynamicColorScheme(context, isDark = ...)
│   │           } else {
│   │             buildDynamicColorScheme(Color(themeConfig.accentColor), isDark)
│   │           }
│   │
│   │           MaterialTheme(colorScheme = finalScheme, typography = AppTypography) {
│   │             Surface(color = MaterialTheme.colorScheme.background) { content() }
│   │           }
│   │
│   ├── home/
│   │   ├── HomeActivity.kt
│   │   │     Portrait orientation. Launcher activity.
│   │   │     onCreate: request permission READ_MEDIA_VIDEO (API 33+) / READ_EXTERNAL_STORAGE.
│   │   │     setContent { AppTheme(themeConfig) { HomeScreen(viewModel) } }
│   │   │     Observasi themeConfig dari SettingsRepository via ViewModel.
│   │   │
│   │   ├── HomeViewModel.kt
│   │   │     @HiltViewModel
│   │   │     StateFlow: val uiState: StateFlow<HomeUiState>
│   │   │
│   │   │     init { loadThemeConfig(); loadVideos(); loadFolders(); loadPlaylists() }
│   │   │
│   │   │     fun loadVideos()
│   │   │       → videoRepository.getAllVideos().collect { update uiState.videos }
│   │   │
│   │   │     fun loadFolders()
│   │   │       → folderRepository.getVisibleFolders().collect { update uiState.folders }
│   │   │
│   │   │     fun loadPlaylists()
│   │   │       → playlistRepository.getAllPlaylists().collect { update uiState.playlists }
│   │   │
│   │   │     fun onTabSelected(tab: HomeTab)
│   │   │     fun onSearchQueryChanged(query: String)
│   │   │     fun onVideoClicked(video: VideoItem)
│   │   │       → emit NavigateToPlayer(videoPath, videoId, videoTitle)
│   │   │     fun onFolderClicked(folder: FolderItem)
│   │   │       → update uiState.activeFolder → tampilkan video dalam folder itu
│   │   │     fun onCreatePlaylist(name: String)
│   │   │     fun onAddVideoToPlaylist(playlistId: Long, videoPath: String)
│   │   │
│   │   ├── HomeUiState.kt
│   │   │     data class HomeUiState(
│   │   │       val videos: List<VideoItem> = emptyList(),
│   │   │       val folders: List<FolderItem> = emptyList(),
│   │   │       val playlists: List<PlaylistInfo> = emptyList(),
│   │   │       val activeTab: HomeTab = HomeTab.VIDEO,
│   │   │       val activeFolder: FolderItem? = null,
│   │   │       val searchQuery: String = "",
│   │   │       val isLoading: Boolean = true,
│   │   │       val hasPermission: Boolean = false,
│   │   │       val error: String? = null,
│   │   │       val themeConfig: ThemeConfig = ThemeConfig.Default,
│   │   │       val navigation: HomeNavigation? = null
│   │   │     )
│   │   │
│   │   │     enum class HomeTab { VIDEO, FOLDER, PLAYLIST }
│   │   │
│   │   │     sealed class HomeNavigation {
│   │   │       data class ToPlayer(val path: String, val id: String, val title: String) : HomeNavigation()
│   │   │     }
│   │   │
│   │   └── components/
│   │       ├── HomeScreen.kt
│   │       │     Root composable. Menyusun: TopAppBar + konten tab + BottomNavigationBar.
│   │       │     Handle navigation event dari uiState.navigation.
│   │       │
│   │       ├── BottomNavBar.kt
│   │       │     NavigationBar Material3 dengan 3 item.
│   │       │     Gunakan NavigationBarItem dengan icon + label.
│   │       │     Selected state: filled icon, warna accent.
│   │       │     Unselected state: outline icon, warna subdued.
│   │       │
│   │       ├── VideoListScreen.kt
│   │       │     LazyColumn berisi VideoListItem.
│   │       │     Menampilkan shimmer placeholder saat isLoading = true.
│   │       │     Menampilkan empty state jika list kosong.
│   │       │
│   │       ├── VideoListItem.kt
│   │       │     Composable satu item video di list.
│   │       │     Layout: Row { Thumbnail | Column { Title, FolderChip, Size } | MoreButton }
│   │       │     Thumbnail: AsyncImage (Coil 3 — import coil3.compose.AsyncImage), aspect 16:9, rounded 8.dp.
│   │       │     Badge durasi: Box over thumbnail, pojok kanan bawah.
│   │       │     Tap: panggil onVideoClick(video).
│   │       │     Long press / MoreButton: tampilkan DropdownMenu.
│   │       │
│   │       ├── FolderGridScreen.kt
│   │       │     LazyVerticalGrid(columns = Fixed(2)) berisi FolderGridItem.
│   │       │     Padding antar item: 8.dp.
│   │       │
│   │       ├── FolderGridItem.kt
│   │       │     Card dengan:
│   │       │       - Icon folder besar (warna MaterialTheme.colorScheme.primary)
│   │       │       - Badge ikon app kecil jika path mengandung nama app terkenal
│   │       │       - Nama folder (bold, maks 2 baris)
│   │       │       - Jumlah video (subdued text)
│   │       │     Tap: panggil onFolderClick(folder).
│   │       │
│   │       ├── PlaylistScreen.kt
│   │       │     LazyColumn berisi PlaylistCard.
│   │       │     FAB tambah playlist di pojok kanan bawah.
│   │       │     Jika list kosong: empty state dengan ilustrasi.
│   │       │
│   │       ├── PlaylistDetailScreen.kt
│   │       │     Layar detail satu playlist (navigasi in-screen, bukan Activity baru).
│   │       │     LazyColumn dengan item yang bisa di-reorder (drag handle).
│   │       │     TopBar: nama playlist + tombol back + tombol edit nama.
│   │       │     Bottom: tombol Play All + Shuffle.
│   │       │
│   │       ├── SearchBar.kt
│   │       │     Composable search bar yang muncul di atas konten saat aktif.
│   │       │     Gunakan SearchBar atau DockedSearchBar dari Material3.
│   │       │
│   │       └── ShimmerItem.kt
│   │             Composable placeholder shimmer animasi untuk loading state.
│   │             Gunakan satu ShimmerItem untuk video list dan satu untuk folder grid.
│   │
│   ├── player/
│   │   ├── PlayerActivity.kt
│   │   │     Orientasi bebas (runtime, Bagian 10F), Immersive, KeepScreenOn. Mengobservasi uiState.orientationMode lalu memasang requestedOrientation di Activity (ViewModel tidak menyentuh Activity secara langsung).
│   │   │     override fun onCreate(): setup window flags, setContent.
│   │   │     override fun onStop(): viewModel.onStop() ← KRITIS
│   │   │     Terima intent: VIDEO_PATH, VIDEO_ID, VIDEO_TITLE.
│   │   │     setContent { AppTheme(themeConfig) { PlayerScreen(viewModel) } }
│   │   │
│   │   ├── PlayerViewModel.kt
│   │   │     @HiltViewModel
│   │   │     StateFlow: val uiState: StateFlow<PlayerUiState>
│   │   │
│   │   │     fun initPlayer(videoPath: String, videoId: String, videoTitle: String)
│   │   │       → loadPlayerUiConfig()
│   │   │       → lastPos = playbackRepository.getLastPosition(videoId)
│   │   │       → videoPlayerManager.prepare(videoPath, lastPos)
│   │   │       → start collecting player events → update uiState
│   │   │       → start position ticker (setiap 500ms update positionMs di uiState)
│   │   │
│   │   │     fun play() / pause() / seekTo(ms) / seekForward() / seekBack()
│   │   │     fun setSpeed(speed: Float)
│   │   │     fun toggleLock()        → update uiState.isLocked
│   │   │     fun toggleControls()    → update uiState.isControlsVisible + reset auto-hide timer
│   │   │     fun startAutoHideTimer()
│   │   │       → coroutine delay 3 detik → set isControlsVisible = false
│   │   │       → reset timer setiap kali ada interaksi user
│   │   │
│   │   │     fun captureFrame()
│   │   │       → positionMs = videoPlayerManager.getCurrentPosition()
│   │   │       → videoFrameCapturer.captureFrame(videoPath, positionMs)
│   │   │       → jika sukses: saveFrameToGallery() → emit CaptureSuccess
│   │   │       → jika gagal: emit CaptureError
│   │   │
│   │   │     fun onStop()
│   │   │       ← DIPANGGIL DARI PlayerActivity.onStop()
│   │   │       → positionMs = videoPlayerManager.getCurrentPosition()
│   │   │       → playbackRepository.savePosition(videoId, videoPath, positionMs)
│   │   │       → videoPlayerManager.release()
│   │   │
│   │   │     fun loadPlayerUiConfig()
│   │   │       → settingsRepository.getPlayerUiConfig().collect { update uiState.playerUiConfig }
│   │   │
│   │   │     fun cycleOrientationMode()
│   │   │       → siklus AUTO → LANDSCAPE → PORTRAIT → AUTO (Bagian 10F); update uiState.orientationMode
│   │   │       → simpan mode ke SettingsRepository (PREF_ORIENTATION_MODE)
│   │   │       → Activity yang menerapkan requestedOrientation dari state ini
│   │   │
│   │   ├── PlayerUiState.kt
│   │   │     data class PlayerUiState(
│   │   │       val playerState: PlayerState = PlayerState.Idle,
│   │   │       val isPlaying: Boolean = false,
│   │   │       val currentPositionMs: Long = 0L,
│   │   │       val durationMs: Long = 0L,
│   │   │       val videoTitle: String = "",
│   │   │       val videoPath: String = "",
│   │   │       val videoId: String = "",
│   │   │       val isControlsVisible: Boolean = true,
│   │   │       val isLocked: Boolean = false,
│   │   │       val orientationMode: OrientationMode = OrientationMode.AUTO,  ← Bagian 10F
│   │   │       val systemAutoRotate: Boolean = true,  ← status kunci rotasi HP, dibaca Activity dari Settings.System
│   │   │       val playbackSpeed: Float = 1.0f,
│   │   │       val captureResult: CaptureResult? = null,
│   │   │       val playerUiConfig: PlayerUiConfig = PlayerUiConfig.Default,
│   │   │       val brightnessLevel: Float = -1f,  ← -1 = ikuti sistem
│   │   │       val volumeLevel: Float = 1f,
│   │   │       val seekPreviewMs: Long? = null,    ← non-null saat gesture seek aktif
│   │   │       val themeConfig: ThemeConfig = ThemeConfig.Default
│   │   │     )
│   │   │
│   │   │     sealed class CaptureResult {
│   │   │       data class Success(val uri: Uri) : CaptureResult()
│   │   │       data class Error(val message: String) : CaptureResult()
│   │   │     }
│   │   │
│   │   └── components/
│   │       ├── PlayerScreen.kt
│   │       │     Root composable PlayerActivity.
│   │       │     Box { VideoSurface() | GestureOverlay() | PlayerOverlay() }
│   │       │     GestureOverlay dan PlayerOverlay keduanya mengisi ukuran penuh (fillMaxSize).
│   │       │     PlayerOverlay: AnimatedVisibility(uiState.isControlsVisible) { ... }
│   │       │     Snackbar untuk captureResult.
│   │       │
│   │       ├── VideoSurface.kt
│   │       │     AndroidView(factory = { PlayerView(context).apply {
│   │       │       useController = false   ← WAJIB false, kita pakai custom controls
│   │       │       resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
│   │       │       player = videoPlayerManager.player
│   │       │     }})
│   │       │     Ukuran: fillMaxSize(), background hitam.
│   │       │
│   │       ├── GestureOverlay.kt
│   │       │     Composable transparan mengisi seluruh area video.
│   │       │     Menggunakan Modifier.pointerInput untuk mendeteksi:
│   │       │
│   │       │     Deteksi tap tunggal:
│   │       │       → viewModel.toggleControls()
│   │       │
│   │       │     Deteksi double tap:
│   │       │       → if (x < screenWidth * 0.33f): viewModel.seekBackward(seekStepMs)
│   │       │       → if (x > screenWidth * 0.67f): viewModel.seekForward(seekStepMs)
│   │       │       → tampilkan ripple animasi di zona yang di-tap
│   │       │
│   │       │     Deteksi drag vertikal:
│   │       │       → zone = GestureZoneCalculator.getZone(x, screenWidth)
│   │       │       → delta = calculateBrightnessDelta / calculateVolumeDelta
│   │       │       → update brightness via WindowManager / AudioManager
│   │       │       → tampilkan GestureFeedback composable (ikon + nilai %)
│   │       │
│   │       │     Deteksi drag horizontal:
│   │       │       → zone harus SEEK (tengah layar)
│   │       │       → deltaMs = GestureZoneCalculator.calculateSeekDeltaMs(dx, screenWidth, duration)
│   │       │       → update uiState.seekPreviewMs (tampilkan preview, BELUM seek aktual)
│   │       │       → onDragEnd: viewModel.seekTo(currentPosition + deltaMs)
│   │       │       → reset seekPreviewMs = null
│   │       │
│   │       │     Jika uiState.isLocked = true: abaikan semua gesture kecuali tap di ikon lock.
│   │       │
│   │       ├── PlayerControls.kt
│   │       │     Slot-based control layout.
│   │       │     Menerima ControlLayoutConfig, merender tombol di posisi yang ditentukan config.
│   │       │
│   │       │     Layout fisik:
│   │       │       Column {
│   │       │         Row(top)    { [topLeftSlot] ... Spacer ... [topRightSlots] }
│   │       │         Spacer(weight = 1f)   ← area video
│   │       │         SeekBarComponent(...)
│   │       │         Row(bottom) { [bottomLeftSlots] Spacer [centerSlots] Spacer [bottomRightSlots] }
│   │       │       }
│   │       │
│   │       │     Untuk merender tombol: gunakan ControlButtonMap.renderSlot(slot, uiState, onAction)
│   │       │
│   │       ├── ControlButtonMap.kt
│   │       │     Mapping dari ControlSlot → @Composable tombol yang sesuai.
│   │       │
│   │       │     fun renderSlot(slot: ControlSlot, state: PlayerUiState, onAction: (PlayerAction) -> Unit)
│   │       │
│   │       │     Setiap ControlSlot:
│   │       │       PLAY_PAUSE  → IconButton { ikon Play atau Pause sesuai isPlaying }
│   │       │       PREVIOUS    → IconButton { ikon SkipPrevious }
│   │       │       NEXT        → IconButton { ikon SkipNext }
│   │       │       LOCK        → IconButton { ikon Lock/LockOpen sesuai isLocked }
│   │       │       CAPTURE     → IconButton { ikon CameraAlt }
│   │       │       SPEED       → TextButton { teks "${speed}x" } → buka speed picker
│   │       │       SCREEN_RATIO → IconButton { ikon AspectRatio }
│   │       │       ORIENTATION   → IconButton { ikon ScreenRotation (AUTO) / StayCurrentLandscape / StayCurrentPortrait sesuai orientationMode } + label mode kecil
│   │       │
│   │       │     sealed class PlayerAction {
│   │       │       object Play, Pause, SeekForward, SeekBackward : PlayerAction()
│   │       │       object ToggleLock, CaptureFrame, ToggleSpeed : PlayerAction()
│   │       │       data class SeekTo(val ms: Long) : PlayerAction()
│   │       │       data class SetSpeed(val speed: Float) : PlayerAction()
│   │       │     }
│   │       │
│   │       ├── SeekBarComponent.kt
│   │       │     Custom composable seekbar dengan:
│   │       │       - Track background (subdued)
│   │       │       - Progress track (warna accentColor dari ThemeConfig)
│   │       │       - Draggable thumb
│   │       │       - Teks waktu: "01:23:45 / 23:51:00"
│   │       │     Saat drag aktif: thumb sedikit membesar (animasi scale)
│   │       │     Bukan Slider Material3 bawaan — buat custom agar warna bisa diatur penuh
│   │       │
│   │       └── TitleDisplay.kt
│   │             Composable judul video di bagian atas player.
│   │             Membaca TitleConfig dari uiState.playerUiConfig.titleConfig.
│   │
│   │             Text(
│   │               text = uiState.videoTitle,
│   │               fontSize = titleConfig.fontSizeSp.sp,
│   │               maxLines = titleConfig.maxLines,
│   │               fontWeight = if (titleConfig.isBold) FontWeight.Bold else FontWeight.Normal,
│   │               color = Color(titleConfig.color),
│   │               overflow = TextOverflow.Ellipsis,
│   │               modifier = if (titleConfig.marqueeEnabled) Modifier.basicMarquee() else Modifier
│   │             )
│   │
│   └── customization/
│       ├── CustomizationActivity.kt
│       │     Portrait. Dibuka dari HomeActivity (menu Settings) atau PlayerActivity.
│       │     setContent { AppTheme(themeConfig) { CustomizationScreen(viewModel) } }
│       │
│       ├── CustomizationViewModel.kt
│       │     @HiltViewModel
│       │     StateFlow: val uiState: StateFlow<CustomizationUiState>
│       │     Memuat semua config dari SettingsRepository saat init.
│       │     Setiap perubahan dari editor langsung disimpan ke SettingsRepository.
│       │
│       │     fun onTitleConfigChanged(config: TitleConfig)   → updateTitleConfig
│       │     fun onControlLayoutChanged(config: ControlLayoutConfig) → updateControlLayoutConfig
│       │     fun onThemeConfigChanged(config: ThemeConfig)   → updateThemeConfig
│       │     fun onFolderVisibilityChanged(config: FolderVisibilityConfig) → updateFolderVisibilityConfig
│       │     fun onResetControlLayout()                      → resetControlLayoutToDefault
│       │
│       │     fun swapControlSlots(posA: SlotPosition, posB: SlotPosition)
│       │       → ambil ControlLayoutConfig saat ini
│       │       → tukar ControlSlot di posA dan posB
│       │       → simpan config baru
│       │
│       │     sealed class SlotPosition {
│       │       object TopLeft : SlotPosition()
│       │       data class TopRight(val index: Int) : SlotPosition()
│       │       data class Center(val index: Int) : SlotPosition()
│       │       data class BottomLeft(val index: Int) : SlotPosition()
│       │       data class BottomRight(val index: Int) : SlotPosition()
│       │     }
│       │
│       ├── CustomizationUiState.kt
│       │     data class CustomizationUiState(
│       │       val titleConfig: TitleConfig = TitleConfig.Default,
│       │       val controlLayoutConfig: ControlLayoutConfig = ControlLayoutConfig.Default,
│       │       val themeConfig: ThemeConfig = ThemeConfig.Default,
│       │       val folderVisibilityConfig: FolderVisibilityConfig = FolderVisibilityConfig.Default,
│       │       val allFolders: List<FolderItem> = emptyList(),
│       │       val activeTab: CustomizationTab = CustomizationTab.TITLE,
│       │       val selectedSlotA: SlotPosition? = null,   ← untuk swap UI
│       │       val isLoading: Boolean = true
│       │     )
│       │
│       │     enum class CustomizationTab { TITLE, CONTROLS, THEME, WHITELIST }
│       │
│       └── components/
│           ├── CustomizationScreen.kt
│           │     Root composable dengan TabRow (4 tab) + konten per tab.
│           │     Tabs: Judul | Tombol | Tema | Whitelist
│           │
│           ├── TitleStyleEditor.kt
│           │     Editor gaya judul video di player.
│           │     Kontrol yang tersedia:
│           │       - Slider: ukuran font (10sp–22sp, step 0.5)
│           │       - SegmentedButton: 1 Baris / 2 Baris
│           │       - Toggle: Teks Tebal (Bold)
│           │       - Toggle: Marquee (teks berjalan)
│           │       - Color picker: warna judul (gunakan color swatches + custom hex input)
│           │     Preview langsung di bawah kontrol:
│           │       Card dengan background gelap, TitleDisplay menggunakan config saat ini
│           │
│           ├── ControlSwapEditor.kt
│           │     Editor posisi tombol player dengan representasi visual slot.
│           │     Tampilkan mini-preview layout player (non-interaktif, hanya sebagai referensi).
│           │     Di bawahnya: grid slot dengan tombol yang ada di dalamnya.
│           │     Mekanisme swap:
│           │       - Tap slot pertama → highlight (selectedSlotA di state)
│           │       - Tap slot kedua → trigger viewModel.swapControlSlots(A, B)
│           │       - Layout di-update real-time via StateFlow
│           │     Tombol "Reset ke Default" di bagian bawah.
│           │
│           ├── ThemeEditor.kt
│           │     Editor tema warna.
│           │     Kontrol:
│           │       - SegmentedButton 4 opsi: Sistem | Terang | Gelap | AMOLED
│           │       - Toggle: Gunakan Dynamic Color (hanya tampil di API 31+)
│           │       - Color swatches (8–10 pilihan warna preset)
│           │       - Tombol "Custom" → color picker dengan input HEX
│           │     Preview: mini card yang menampilkan warna background, surface, dan primary
│           │
│           └── WhitelistEditor.kt
│                 Editor whitelist folder.
│                 Kontrol:
│                   - Switch di atas: "Mode Whitelist Saja" ON/OFF
│                   - LazyColumn: semua folder dengan Checkbox di kiri
│                   - Jika mode OFF: list greyed out (disabled) tapi tetap terlihat
│                   - Jika mode ON: hanya folder yang dicentang yang muncul di tab Folder
│                 Info text: "Folder yang dicentang akan tampil di halaman Folder"
│
└── utils/
    ├── Constants.kt
    │     Semua string konstanta untuk mencegah typo:
    │       KEY_VIDEO_PATH, KEY_VIDEO_ID, KEY_VIDEO_TITLE
    │       PREF_THEME_MODE, PREF_ACCENT_COLOR, PREF_USE_SYSTEM_ACCENT
    │       PREF_TITLE_FONT_SIZE, PREF_TITLE_MAX_LINES, PREF_TITLE_COLOR
    │       PREF_TITLE_BOLD, PREF_TITLE_MARQUEE
    │       PREF_CONTROL_LAYOUT_JSON, PREF_FOLDER_FILTER_MODE
    │       PREF_WHITELISTED_PATHS, PREF_SEEK_STEP_MS
    │       DEFAULT_SEEK_STEP_MS = 10_000
    │       AUTO_HIDE_CONTROLS_DELAY_MS = 3_000
    │       CAPTURE_SAVE_FOLDER = "Pictures/AVP"
    │
    ├── TimeUtils.kt
    │     fun msToTimestamp(ms: Long): String → "01:23:45" atau "23:45"
    │       (format HH:mm:ss jika durasi ≥ 1 jam, mm:ss jika di bawah 1 jam)
    │     fun msToDurationReadable(ms: Long): String → "1j 23m" atau "23 mnt"
    │
    ├── FileUtils.kt
    │     fun generateVideoId(path: String): String
    │       → MD5 dari path string → return hex string sebagai ID unik
    │     fun getFileSizeReadable(bytes: Long): String
    │       → "215,94 MB" / "1,2 GB" (format lokal Indonesia)
    │     fun getFileExtension(path: String): String → "mp4" / "mkv"
    │
    ├── PathUtils.kt
    │     fun normalizePath(path: String): String
    │       → trim, lowercase, hapus trailing "/"
    │       → digunakan untuk perbandingan path whitelist agar konsisten
    │     fun getFolderName(path: String): String
    │     fun getFolderPath(filePath: String): String
    │
    └── BitmapUtils.kt
          fun rotateBitmapIfNeeded(bitmap: Bitmap, videoPath: String): Bitmap
            → baca EXIF rotation dari ExifInterface(videoPath)
            → rotate bitmap sesuai orientasi jika diperlukan
            → return bitmap yang sudah benar orientasinya
```

---

## 8. Definisi Data Model

### VideoItem.kt

```kotlin
data class VideoItem(
    val id: String,              // FileUtils.generateVideoId(path)
    val name: String,            // nama file tanpa ekstensi (untuk display)
    val fileName: String,        // nama file lengkap dengan ekstensi
    val path: String,            // full path absolut
    val durationMs: Long,        // durasi dalam milliseconds
    val sizeBytes: Long,         // ukuran file dalam bytes
    val folderName: String,      // BUCKET_DISPLAY_NAME dari MediaStore
    val folderPath: String,      // path folder induk
    val dateAdded: Long,         // timestamp dari MediaStore
    val thumbnailPath: String?   // path untuk load thumbnail via Coil (nullable)
)
```

### FolderItem.kt

```kotlin
data class FolderItem(
    val path: String,            // path absolut folder
    val name: String,            // nama folder untuk display
    val videoCount: Int,         // jumlah video di dalam folder
    val thumbnailPath: String?,  // path video pertama untuk thumbnail
    val isWhitelisted: Boolean   // apakah masuk whitelist saat ini
)
```

### TitleConfig.kt

```kotlin
data class TitleConfig(
    val fontSizeSp: Float = 14f,          // range: 10f – 22f
    val maxLines: Int = 1,                // 1 atau 2
    val color: Long = 0xFFFFFFFF,         // white default
    val isBold: Boolean = false,
    val marqueeEnabled: Boolean = true
) {
    companion object { val Default = TitleConfig() }
}
```

### ControlSlot.kt

```kotlin
enum class ControlSlot {
    PLAY_PAUSE,
    PREVIOUS,
    NEXT,
    LOCK,
    ORIENTATION,
    CAPTURE,
    SPEED,
    SCREEN_RATIO,
    EMPTY             // slot kosong / placeholder
}
```

### ControlLayoutConfig.kt

```kotlin
data class ControlLayoutConfig(
    val topLeftSlot: ControlSlot = ControlSlot.LOCK,
    val topRightSlots: List<ControlSlot> = listOf(ControlSlot.ORIENTATION, ControlSlot.SPEED, ControlSlot.SCREEN_RATIO),
    val centerSlots: List<ControlSlot> = listOf(ControlSlot.PREVIOUS, ControlSlot.PLAY_PAUSE, ControlSlot.NEXT),
    val bottomLeftSlots: List<ControlSlot> = emptyList(),
    val bottomRightSlots: List<ControlSlot> = listOf(ControlSlot.CAPTURE)
) {
    companion object { val Default = ControlLayoutConfig() }
}
```

### OrientationMode.kt

```kotlin
enum class OrientationMode {
    AUTO,       // ikuti perangkat: requestedOrientation = SCREEN_ORIENTATION_FULL_USER
                // (sensor aktif hanya jika kunci rotasi HP mati; jika kunci rotasi HP nyala, layar diam)
    LANDSCAPE,  // paksa landscape — mengalahkan kunci rotasi HP
    PORTRAIT    // paksa portrait — mengalahkan kunci rotasi HP
}
```

Mode dipilih lewat tombol rotasi dengan siklus AUTO → LANDSCAPE → PORTRAIT → AUTO (lihat Bagian 10F), disimpan di DataStore (`PREF_ORIENTATION_MODE`, bawaan `AUTO`), dan diterapkan oleh `PlayerActivity` sebagai `requestedOrientation`. Kembali ke AUTO berarti membuka kunci orientasi buatan aplikasi.

### ThemeConfig.kt

```kotlin
enum class ThemeMode { SYSTEM, LIGHT, DARK, AMOLED }

data class ThemeConfig(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val accentColor: Long = 0xFF6655F5,    // Lavender default (keputusan aksen 3 Nov 2026, lihat UI.md PT-25)
    val useSystemAccent: Boolean = false   // dynamic color, API 31+ only
) {
    companion object { val Default = ThemeConfig() }
}
```

### FolderVisibilityConfig.kt

```kotlin
enum class FolderFilterMode { ALL, WHITELIST_ONLY }

data class FolderVisibilityConfig(
    val mode: FolderFilterMode = FolderFilterMode.ALL,
    val whitelistedPaths: Set<String> = emptySet()
) {
    companion object { val Default = FolderVisibilityConfig() }
}
```

### PlayerUiConfig.kt

```kotlin
data class PlayerUiConfig(
    val titleConfig: TitleConfig = TitleConfig.Default,
    val controlLayoutConfig: ControlLayoutConfig = ControlLayoutConfig.Default,
    val themeConfig: ThemeConfig = ThemeConfig.Default
) {
    companion object { val Default = PlayerUiConfig() }
}
```

### Playlist.kt

```kotlin
data class PlaylistInfo(
    val id: Long,
    val name: String,
    val videoCount: Int,
    val totalDurationMs: Long,
    val createdAt: Long,
    val thumbnailPaths: List<String>    // maks 3, untuk strip thumbnail
)

data class PlaylistEntry(
    val id: Long,
    val playlistId: Long,
    val videoPath: String,
    val videoTitle: String,
    val durationMs: Long,
    val orderIndex: Int
)
```

---

## 9. Skema Storage

### Room Database (AppDatabase) — untuk data persisten terstruktur

```
Tabel: playback_state
  videoId    TEXT PRIMARY KEY     ← hash MD5 dari path
  videoPath  TEXT NOT NULL
  positionMs INTEGER NOT NULL
  updatedAt  INTEGER NOT NULL

Tabel: playlists
  id         INTEGER PRIMARY KEY AUTOINCREMENT
  name       TEXT NOT NULL
  createdAt  INTEGER NOT NULL

Tabel: playlist_entries
  id          INTEGER PRIMARY KEY AUTOINCREMENT
  playlistId  INTEGER NOT NULL REFERENCES playlists(id) ON DELETE CASCADE
  videoPath   TEXT NOT NULL
  orderIndex  INTEGER NOT NULL
```

### DataStore Preferences — untuk konfigurasi & pengaturan

```
Semua key bertipe stringPreferencesKey / intPreferencesKey / floatPreferencesKey / booleanPreferencesKey / longPreferencesKey

PREF_THEME_MODE           : stringPreferencesKey   → "SYSTEM"
PREF_ACCENT_COLOR         : longPreferencesKey     → 0xFF6655F5
PREF_USE_SYSTEM_ACCENT    : booleanPreferencesKey  → false
PREF_TITLE_FONT_SIZE      : floatPreferencesKey    → 14f
PREF_TITLE_MAX_LINES      : intPreferencesKey      → 1
PREF_TITLE_COLOR          : longPreferencesKey     → 0xFFFFFFFF
PREF_TITLE_BOLD           : booleanPreferencesKey  → false
PREF_TITLE_MARQUEE        : booleanPreferencesKey  → true
PREF_CONTROL_LAYOUT_JSON  : stringPreferencesKey   → JSON default config
PREF_FOLDER_FILTER_MODE   : stringPreferencesKey   → "ALL"
PREF_WHITELISTED_PATHS    : stringPreferencesKey   → "[]" (JSON array)
PREF_SEEK_STEP_MS         : intPreferencesKey      → 10000
PREF_ORIENTATION_MODE     : stringPreferencesKey   → "AUTO"
```

---

## 10. Logic Flow Utama

### A. Lifecycle Playback (KRITIS — Jangan Dimodifikasi)

```
[PlayerActivity.onCreate()]
  ↓
  Ambil VIDEO_PATH, VIDEO_ID, VIDEO_TITLE dari Intent
  ↓
  viewModel.initPlayer(path, id, title)
    ↓
    playbackRepository.getLastPosition(videoId) → startPositionMs
    ↓
    videoPlayerManager.prepare(videoPath, startPositionMs)
      ↓ ExoPlayer.setMediaItem(MediaItem.fromUri(path))
      ↓ ExoPlayer.seekTo(startPositionMs)
      ↓ ExoPlayer.prepare()
      ↓ ExoPlayer.playWhenReady = true
    ↓
    start collecting PlayerEventListener events → update uiState
    ↓
    start position ticker coroutine (delay 500ms loop) → update uiState.currentPositionMs

[User tekan tombol Home / minimize aplikasi]
  ↓
  PlayerActivity.onStop() dipanggil oleh Android
  ↓
  viewModel.onStop()
    ↓
    positionMs = videoPlayerManager.getCurrentPosition()
    ↓
    playbackRepository.savePosition(videoId, videoPath, positionMs)
    ↓
    videoPlayerManager.release()
      ↓ ExoPlayer.stop()
      ↓ ExoPlayer.release()

[User buka aplikasi lagi → tap video yang sama]
  ↓
  PlayerActivity.onCreate() kembali
  ↓
  playbackRepository.getLastPosition(videoId) → return positionMs yang tersimpan
  ↓
  Video lanjut dari posisi terakhir ✓
```

### B. Frame Capture (Bukan Screenshot)

```
[User tap tombol CAPTURE di PlayerControls]
  ↓
  viewModel.captureFrame()
    ↓
    videoPath = uiState.videoPath
    positionMs = videoPlayerManager.getCurrentPosition()
    ↓
    // DI COROUTINE BACKGROUND (Dispatchers.IO)
    videoFrameCapturer.captureFrame(videoPath, positionMs)
      ↓
      retriever = MediaMetadataRetriever()
      retriever.setDataSource(videoPath)
      bitmap = retriever.getFrameAtTime(
        positionMs * 1000L,          ← MICROSECONDS! Bukan milliseconds
        OPTION_CLOSEST_SYNC
      )
      retriever.release()
      ↓
      rotatedBitmap = BitmapUtils.rotateBitmapIfNeeded(bitmap, videoPath)
      return Result.success(rotatedBitmap)
    ↓
    videoFrameCapturer.saveFrameToGallery(bitmap, videoTitle)
      ↓
      contentValues = ContentValues().apply {
        put(MIME_TYPE, "image/jpeg")
        put(RELATIVE_PATH, "Pictures/AVP")     ← Android 10+
        put(DISPLAY_NAME, "avp_${currentTimeMs}.jpg")
        put(IS_PENDING, 1)
      }
      uri = contentResolver.insert(EXTERNAL_URI, contentValues)
      contentResolver.openOutputStream(uri).use { stream →
        bitmap.compress(JPEG, 95, stream)
      }
      contentValues.put(IS_PENDING, 0)
      contentResolver.update(uri, contentValues, null, null)
      return Result.success(uri)
    ↓
    // KEMBALI KE MAIN THREAD
    update uiState.captureResult = CaptureResult.Success(uri)
    ↓
    PlayerScreen.kt: tampilkan Snackbar "Frame berhasil disimpan ke Galeri"
```

### C. Whitelist Folder Filter

```
[HomeViewModel.loadFolders() dipanggil]
  ↓
  folderRepository.getVisibleFolders()  ← Flow
    ↓
    // Combine dua Flow secara paralel:
    Flow A: fileScannerDataSource.scanAllFolders()
      → MediaStore query GROUP BY BUCKET_ID
      → return List<FolderItem>
    ↓
    Flow B: settingsRepository.getFolderVisibilityConfig()
      → return FolderVisibilityConfig dari DataStore
    ↓
    combine(flowA, flowB) { allFolders, config →
      val normalizedWhitelist = config.whitelistedPaths
        .map { PathUtils.normalizePath(it) }
        .toSet()
      ↓
      val markedFolders = allFolders.map { folder →
        folder.copy(
          isWhitelisted = PathUtils.normalizePath(folder.path) in normalizedWhitelist
        )
      }
      ↓
      if (config.mode == WHITELIST_ONLY) {
        markedFolders.filter { it.isWhitelisted }
      } else {
        markedFolders   // tampilkan semua
      }
    }
  ↓
  HomeUiState.folders di-update
  ↓
  FolderGridScreen re-compose dengan list terbaru
```

### D. Gesture Detection

```
[GestureOverlay menerima touch event]

KASUS: Tap Tunggal
  → viewModel.toggleControls()
  → isControlsVisible di-toggle
  → jika visible: mulai auto-hide timer (3 detik)

KASUS: Double Tap
  zona = GestureZoneCalculator.getZone(x, screenWidth)
  → jika zona == BRIGHTNESS: viewModel.seekBackward(seekStepMs)
     tampilkan ripple animasi "⏪ 10s" di kiri
  → jika zona == VOLUME   : viewModel.seekForward(seekStepMs)
     tampilkan ripple animasi "10s ⏩" di kanan

KASUS: Drag Vertikal (onDrag callback)
  zona = GestureZoneCalculator.getZone(startX, screenWidth)
  if (zona == BRIGHTNESS):
    delta = GestureZoneCalculator.calculateBrightnessDelta(totalDy, screenHeight)
    newBrightness = (currentBrightness + delta).coerceIn(0.01f, 1f)
    → update WindowManager.LayoutParams.screenBrightness
    → update uiState.brightnessLevel
    → tampilkan GestureFeedback: ikon brightness + nilai %
  if (zona == VOLUME):
    delta = GestureZoneCalculator.calculateVolumeDelta(totalDy, screenHeight)
    → update AudioManager.setStreamVolume(STREAM_MUSIC, ...)
    → update uiState.volumeLevel
    → tampilkan GestureFeedback: ikon volume + nilai %

KASUS: Drag Horizontal (onDrag callback)
  zona = GestureZoneCalculator.getZone(startX, screenWidth)
  if (zona == SEEK):
    deltaMs = GestureZoneCalculator.calculateSeekDeltaMs(totalDx, screenWidth, durationMs)
    previewMs = (currentPositionMs + deltaMs).coerceIn(0, durationMs)
    → update uiState.seekPreviewMs = previewMs
    → SeekBarComponent menampilkan posisi preview (bukan posisi aktual)
    → GestureOverlay menampilkan teks "+00:30" atau "-00:30"

  onDragEnd:
    viewModel.seekTo(previewMs)
    uiState.seekPreviewMs = null   ← kembali ke posisi normal

CATATAN: Jika uiState.isLocked == true
  → abaikan SEMUA gesture kecuali tap di area ikon LOCK
```

### E. Control Slot Swap

```
[User membuka CustomizationActivity → tab Tombol]
  ↓
  ControlSwapEditor menampilkan semua slot dengan tombol saat ini

[User tap slot A (misalnya: PLAY_PAUSE di center[1])]
  ↓
  viewModel.onSlotSelected(SlotPosition.Center(1))
  uiState.selectedSlotA = SlotPosition.Center(1)
  ↓
  UI: highlight slot A

[User tap slot B (misalnya: CAPTURE di bottomRight[0])]
  ↓
  viewModel.swapControlSlots(
    posA = SlotPosition.Center(1),
    posB = SlotPosition.BottomRight(0)
  )
    ↓
    currentConfig = settingsRepository.getControlLayoutConfig().first()
    ↓
    slotAtA = currentConfig.getSlotAt(posA)   // = PLAY_PAUSE
    slotAtB = currentConfig.getSlotAt(posB)   // = CAPTURE
    ↓
    newConfig = currentConfig
      .setSlotAt(posA, slotAtB)   // center[1] = CAPTURE
      .setSlotAt(posB, slotAtA)   // bottomRight[0] = PLAY_PAUSE
    ↓
    settingsRepository.updateControlLayoutConfig(newConfig)
    ↓
    Flow di SettingsRepository emit nilai baru
    ↓
    CustomizationUiState.controlLayoutConfig di-update → UI refresh real-time
    uiState.selectedSlotA = null   ← reset seleksi
```

### F. Kontrol Orientasi Player (Runtime, BUKAN Kunci Manifest)

```
PRINSIP: PlayerActivity TIDAK memasang screenOrientation di Manifest.
Orientasi adalah state runtime (orientationMode di PlayerUiState) yang
diterapkan PlayerActivity sebagai requestedOrientation setiap kali berubah.

[PlayerActivity dibuka]
  → orientationMode dimuat dari SettingsRepository (bawaan AUTO)
  → Activity menerapkan requestedOrientation sesuai mode (tabel di bawah)
  → Activity membaca Settings.System.ACCELEROMETER_ROTATION (tanpa izin khusus)
    → uiState.systemAutoRotate (true = kunci rotasi HP mati)

Mode → requestedOrientation → perilaku:
  AUTO      → SCREEN_ORIENTATION_FULL_USER
              kunci rotasi HP MATI → layar mengikuti sensor (miring = berputar)
              kunci rotasi HP NYALA → layar diam mengikuti kunci HP
  LANDSCAPE → SCREEN_ORIENTATION_LANDSCAPE
              memaksa landscape, MENGALAHKAN kunci rotasi HP
  PORTRAIT  → SCREEN_ORIENTATION_PORTRAIT
              memaksa portrait, MENGALAHKAN kunci rotasi HP

[User mengetuk tombol rotasi (slot ORIENTATION)]
  → viewModel.cycleOrientationMode(): AUTO → LANDSCAPE → PORTRAIT → AUTO
  → mode baru disimpan (PREF_ORIENTATION_MODE) dan diterapkan Activity
  → tampilkan label mode sesaat ("Otomatis" / "Landscape" / "Portrait")
  → kembali ke AUTO = membuka kunci orientasi buatan aplikasi

[Kasus khusus: kunci rotasi HP NYALA + mode AUTO]
  → layar tidak akan berputar dengan sendirinya — ini perilaku yang benar
  → tombol rotasi adalah jalan keluarnya: satu ketuk masuk mode paksa
  → opsional: tampilkan petunjuk kecil "Rotasi HP terkunci — ketuk tombol rotasi"

[Saat orientasi benar-benar berubah]
  → configChanges di Manifest mencegah Activity dibuat ulang
  → ExoPlayer TIDAK di-release, posisi TIDAK reset, playback lanjut
  → hanya layout Compose yang menyesuaikan (portrait: video letterbox di atas;
    landscape: video memenuhi layar)
```

---

## 11. Urutan Pengerjaan (Milestone)

> **Aturan**: Selesaikan satu milestone sampai target terpenuhi sebelum pindah ke milestone berikutnya.
> Setiap milestone memiliki **Definition of Done** yang harus terpenuhi.

---

### Milestone 0 — Project Setup & Fondasi

**Target**: Project bisa di-build dan dijalankan sebagai blank screen.

Urutan file yang dibuat/dikonfigurasi:
1. `build.gradle.kts` (project level) — repositories dan deklarasi plugin (`apply false`): AGP, KSP, Hilt, Compose Compiler, Kotlin Serialization, Room Gradle Plugin (`androidx.room3`). Versi sesuai Bagian 3 (disarankan lewat `gradle/libs.versions.toml`); Gradle wrapper 9.5+ dan JDK 17
2. `build.gradle.kts` (app level) — apply plugin (TANPA `org.jetbrains.kotlin.android`, Kotlin sudah built-in di AGP 9), semua dependencies, blok `room3 { schemaDirectory(...) }`, compileSdk 37, targetSdk 35, minSdk 29 (rentang dukungan Android 10–15)
3. `AndroidManifest.xml` — permissions, 3 activity declarations, orientations, intent filter
4. `MainApplication.kt` — anotasi `@HiltAndroidApp`
5. `di/AppModule.kt` — shell kosong dengan anotasi saja
6. `di/PlayerModule.kt` — shell kosong
7. `utils/Constants.kt` — semua konstanta string
8. `ui/theme/Color.kt` — light/dark/AMOLED color scheme
9. `ui/theme/Typography.kt` — typography definition
10. `ui/theme/Theme.kt` — `AppTheme` composable (terima `ThemeConfig`, bangun `MaterialTheme`)

**Definition of Done**: `./gradlew assembleDebug` berhasil tanpa error.

---

### Milestone 1 — Core Playback

**Target**: Video bisa dibuka dan diputar dari `PlayerActivity` dengan path yang dikirim via Intent.

Urutan file yang dibuat:
1. `data/model/VideoItem.kt`
2. `player/core/PlayerState.kt`
3. `player/core/PlayerEventListener.kt`
4. `player/core/VideoPlayerManager.kt` — `prepare()`, `play()`, `pause()`, `seekTo()`, `release()`
5. `ui/player/PlayerUiState.kt` — versi minimal (playerState, isPlaying, positionMs, durationMs, title)
6. `ui/player/PlayerViewModel.kt` — `initPlayer()`, `play()`, `pause()`, `seekTo()`, `onStop()`
7. `ui/player/components/VideoSurface.kt` — `AndroidView` wrapping `PlayerView`
8. `ui/player/components/SeekBarComponent.kt` — seekbar sederhana
9. `ui/player/components/PlayerControls.kt` — **hardcoded** dulu (bukan slot-based), hanya play/pause/prev/next + tombol rotasi (ORIENTATION, hardcoded dulu — perilakunya mengikuti Bagian 10F)
10. `ui/player/components/TitleDisplay.kt` — static, belum baca config
11. `ui/player/components/PlayerScreen.kt` — susun semua komponen di atas
12. `ui/player/PlayerActivity.kt` — setup window flags, setContent, override `onStop()`, terapkan `requestedOrientation` dari `orientationMode` (Bagian 10F)

**Definition of Done**:
- PlayerActivity menerima `VIDEO_PATH` dari Intent
- Video `.mp4` bisa diputar, play/pause berfungsi, seekbar bergerak
- Player membuka mengikuti orientasi perangkat saat itu (TIDAK memaksa landscape); tombol rotasi berputar AUTO → LANDSCAPE → PORTRAIT → AUTO dan video tidak mulai ulang saat orientasi berubah
- Tekan Home → video berhenti (belum perlu simpan posisi)

---

### Milestone 2 — Resume Playback

**Target**: Posisi pemutaran tersimpan saat stop, dilanjutkan saat buka lagi.

Urutan file yang dibuat:
1. `data/model/PlaybackState.kt`
2. `data/db/entity/PlaybackStateEntity.kt`
3. `data/db/dao/PlaybackStateDao.kt` — fungsi `suspend`
4. `data/db/AppDatabase.kt` — versi awal, hanya `PlaybackStateEntity`; builder dengan `.setDriver(AndroidSQLiteDriver())`
5. `utils/FileUtils.kt` — `generateVideoId()`, `getFileSizeReadable()`
6. `data/source/PreferenceDataSource.kt` — hanya DataStore setup, belum semua key
7. `data/repository/PlaybackRepository.kt` — `savePosition()`, `getLastPosition()`
8. `di/AppModule.kt` — `@Provides` untuk AppDatabase, PlaybackStateDao, PlaybackRepository
9. Update `ui/player/PlayerViewModel.kt`:
   - `initPlayer()` → baca `lastPosition` dari `PlaybackRepository`
   - `onStop()` → simpan posisi sebelum `release()`

**Definition of Done**:
- Tonton video sampai 01:30 → tekan Home → buka lagi video yang sama → lanjut dari 01:30

---

### Milestone 3 — Home Screen & File Browser

**Target**: HomeActivity menampilkan daftar video dan folder, tap video → buka PlayerActivity.

Urutan file yang dibuat:
1. `data/model/FolderItem.kt`
2. `data/source/FileScannerDataSource.kt` — MediaStore query (video + folder)
3. `data/repository/VideoRepository.kt`
4. `data/repository/FolderRepository.kt` — tanpa whitelist dulu (return semua folder)
5. Update `di/AppModule.kt` — `@Provides` untuk DataSource dan Repository baru
6. `ui/home/HomeUiState.kt`
7. `ui/home/HomeViewModel.kt` — `loadVideos()`, `loadFolders()`, `onVideoClicked()`
8. `ui/home/components/ShimmerItem.kt`
9. `ui/home/components/VideoListItem.kt`
10. `ui/home/components/VideoListScreen.kt`
11. `ui/home/components/FolderGridItem.kt`
12. `ui/home/components/FolderGridScreen.kt`
13. `ui/home/components/BottomNavBar.kt`
14. `ui/home/components/SearchBar.kt`
15. `ui/home/components/HomeScreen.kt`
16. `ui/home/HomeActivity.kt` — permission request, setContent, handle navigation event

**Definition of Done**:
- HomeActivity tampil list video (dengan thumbnail, durasi, nama folder)
- Tab Folder menampilkan grid folder
- Tap video → `PlayerActivity` terbuka → video diputar
- Search video berfungsi

---

### Milestone 4 — Theme System

**Target**: App mengikuti dark/light mode sistem secara otomatis. Fondasi tema siap untuk kustomisasi.

Urutan file yang dibuat:
1. `data/model/ThemeConfig.kt`
2. Update `data/source/PreferenceDataSource.kt` — tambah theme keys (THEME_MODE, ACCENT_COLOR, USE_SYSTEM_ACCENT)
3. Update `data/repository/SettingsRepository.kt` — versi awal, hanya theme methods
4. Update `di/AppModule.kt` — `@Provides` untuk PreferenceDataSource, SettingsRepository
5. Update `ui/theme/Theme.kt` — terima `ThemeConfig`, bangun `MaterialTheme` dinamis, dukung AMOLED
6. Update `ui/home/HomeViewModel.kt` — collect `themeConfig` dari `SettingsRepository`
7. Update `ui/home/HomeActivity.kt` — pass `themeConfig` ke `AppTheme`
8. Update `ui/player/PlayerViewModel.kt` — collect `themeConfig`
9. Update `ui/player/PlayerActivity.kt` — pass `themeConfig` ke `AppTheme`

**Definition of Done**:
- App mengikuti dark/light mode HP secara otomatis
- AMOLED mode: background benar-benar hitam #000000

---

### Milestone 5 — Gesture Control

**Target**: Swipe untuk brightness, volume, dan seek berfungsi di layar player.

Urutan file yang dibuat:
1. `player/gesture/GestureZoneCalculator.kt`
2. Update `ui/player/PlayerUiState.kt` — tambah `brightnessLevel`, `volumeLevel`, `seekPreviewMs`
3. Update `ui/player/PlayerViewModel.kt` — tambah `seekForward()`, `seekBackward()`, `adjustBrightness()`, `adjustVolume()`
4. `ui/player/components/GestureOverlay.kt` — deteksi semua gesture, feedback visual
5. Update `ui/player/components/PlayerScreen.kt` — pasang `GestureOverlay` di atas `VideoSurface`
6. Update `ui/player/components/SeekBarComponent.kt` — support `seekPreviewMs` untuk tampilkan posisi preview

**Definition of Done**:
- Swipe vertikal kiri → ubah kecerahan layar
- Swipe vertikal kanan → ubah volume
- Swipe horizontal tengah → seek preview muncul, seek aktual terjadi saat dilepas
- Double tap kiri/kanan → seek backward/forward 10 detik dengan animasi ripple

---

### Milestone 6 — Frame Capture

**Target**: Tombol capture mengambil frame video (bukan screenshot) dan menyimpannya ke galeri.

Urutan file yang dibuat:
1. `utils/BitmapUtils.kt` — `rotateBitmapIfNeeded()`
2. `player/capture/VideoFrameCapturer.kt` — `captureFrame()`, `saveFrameToGallery()`
3. Update `di/AppModule.kt` — `@Provides` untuk `VideoFrameCapturer`
4. Update `ui/player/PlayerUiState.kt` — tambah `captureResult: CaptureResult?`
5. Update `ui/player/PlayerViewModel.kt` — tambah `captureFrame()`
6. Update `ui/player/components/PlayerControls.kt` — tombol CAPTURE terhubung ke `viewModel.captureFrame()`
7. Update `ui/player/components/PlayerScreen.kt` — tampilkan Snackbar berdasarkan `captureResult`

**Definition of Done**:
- Tap tombol kamera → frame video tersimpan ke `/Pictures/AVP/` di galeri
- File yang tersimpan adalah frame video murni: resolusi asli video, tanpa UI overlay, tanpa black bar letterbox
- Snackbar konfirmasi muncul setelah berhasil

---

### Milestone 7 — Player Customization (Judul & Slot Tombol)

**Target**: User bisa mengubah gaya judul dan menukar posisi tombol di player.

Urutan file yang dibuat:
1. `data/model/TitleConfig.kt`
2. `data/model/ControlSlot.kt`
3. `data/model/ControlLayoutConfig.kt`
4. `data/model/PlayerUiConfig.kt`
5. Update `data/source/PreferenceDataSource.kt` — tambah title & control layout keys
6. Update `data/repository/SettingsRepository.kt` — tambah title, control layout, dan `getPlayerUiConfig()`
7. Update `ui/player/PlayerUiState.kt` — tambah `playerUiConfig: PlayerUiConfig`
8. Update `ui/player/PlayerViewModel.kt` — tambah `loadPlayerUiConfig()`, dipanggil di `initPlayer()`
9. Update `ui/player/components/TitleDisplay.kt` — baca `TitleConfig` dari state
10. `ui/player/components/ControlButtonMap.kt` — mapping `ControlSlot` → composable tombol
11. Update `ui/player/components/PlayerControls.kt` — ganti hardcoded menjadi slot-based rendering
12. `ui/customization/CustomizationUiState.kt`
13. `ui/customization/CustomizationViewModel.kt` — termasuk `swapControlSlots()`
14. `ui/customization/components/TitleStyleEditor.kt` — slider, toggle, color picker, live preview
15. `ui/customization/components/ControlSwapEditor.kt` — visual slot, mekanisme tap-swap
16. `ui/customization/components/CustomizationScreen.kt` — root dengan tab
17. `ui/customization/CustomizationActivity.kt`
18. Update `ui/home/HomeActivity.kt` — tambah menu/tombol untuk buka `CustomizationActivity`

**Definition of Done**:
- Buka CustomizationActivity → ubah ukuran font judul → efek terlihat real-time di preview
- Swap posisi tombol via tap dua slot → layout player berubah
- Perubahan tetap tersimpan setelah app di-restart

---

### Milestone 8 — Theme Customization

**Target**: User bisa memilih mode tema dan accent color dari dalam aplikasi.

Urutan file yang dibuat:
1. Update `data/model/ThemeConfig.kt` — pastikan `accentColor` dan `useSystemAccent` sudah ada
2. Update `data/repository/SettingsRepository.kt` — lengkapi theme methods
3. `ui/customization/components/ThemeEditor.kt` — segmented button mode + color swatches + dynamic color toggle
4. Update `ui/customization/components/CustomizationScreen.kt` — tambah tab Tema

**Definition of Done**:
- Pilih mode Dark → app menjadi gelap secara instan
- Pilih AMOLED → background benar-benar hitam
- Pilih accent color → warna seekbar, tombol aktif, dan elemen primer berubah

---

### Milestone 9 — Folder Whitelist

**Target**: User bisa memfilter agar hanya folder tertentu yang muncul di tab Folder.

Urutan file yang dibuat:
1. `data/model/FolderVisibilityConfig.kt`
2. `utils/PathUtils.kt` — `normalizePath()`, `getFolderPath()`
3. Update `data/source/PreferenceDataSource.kt` — tambah `FOLDER_FILTER_MODE`, `WHITELISTED_PATHS`
4. Update `data/repository/SettingsRepository.kt` — tambah `getFolderVisibilityConfig()`, `updateFolderVisibilityConfig()`
5. Update `data/repository/FolderRepository.kt` — terapkan whitelist filter menggunakan `normalizePath()`
6. `ui/customization/components/WhitelistEditor.kt` — switch mode + checklist folder
7. Update `ui/customization/components/CustomizationScreen.kt` — tambah tab Whitelist
8. Update `ui/customization/CustomizationViewModel.kt` — load `allFolders` untuk WhitelistEditor
9. Update `ui/customization/CustomizationUiState.kt` — tambah `folderVisibilityConfig`, `allFolders`

**Definition of Done**:
- Aktifkan mode Whitelist Only + centang 3 folder → tab Folder hanya tampilkan 3 folder itu
- Matikan mode Whitelist Only → semua folder muncul kembali

---

### Milestone 10 — Manual Playlist

**Target**: User bisa membuat playlist sendiri, menambah video, mengatur urutan, dan memutar.

Urutan file yang dibuat:
1. `data/model/Playlist.kt` — `PlaylistInfo`, `PlaylistEntry`
2. `data/db/entity/PlaylistEntity.kt`
3. `data/db/entity/PlaylistEntryEntity.kt`
4. `data/db/dao/PlaylistDao.kt`
5. `data/db/dao/PlaylistEntryDao.kt`
6. Update `data/db/AppDatabase.kt` — tambah `PlaylistEntity`, `PlaylistEntryEntity`, dan DAO-nya (bump version ke 2, buat Migration 1→2 — di Room 3 migrasi memakai `SQLiteConnection`, bukan `SupportSQLiteDatabase`)
7. `data/repository/PlaylistRepository.kt`
8. Update `di/AppModule.kt` — `@Provides` untuk DAO dan Repository baru
9. Update `ui/home/HomeViewModel.kt` — `loadPlaylists()`, `createPlaylist()`, `addVideoToPlaylist()`, `removeFromPlaylist()`
10. Update `ui/home/HomeUiState.kt` — tambah `playlists: List<PlaylistInfo>`
11. `ui/home/components/PlaylistScreen.kt` — card per playlist, FAB tambah
12. `ui/home/components/PlaylistDetailScreen.kt` — list video, drag-to-reorder, Play All
13. Update `ui/home/components/HomeScreen.kt` — tab Daftar Putar terhubung ke `PlaylistScreen`

**Definition of Done**:
- Buat playlist baru dengan nama → muncul di tab Daftar Putar
- Tambahkan beberapa video → video masuk ke playlist
- Drag video untuk ubah urutan → urutan tersimpan
- Tap Play All → PlayerActivity memutar video pertama, otomatis lanjut ke berikutnya

---

## 12. Catatan Penting

### Tentang Playlist Autoplay (Milestone 10)

Untuk fitur "lanjut ke video berikutnya" dalam playlist, mekanisme yang direkomendasikan:

- Kirim ke `PlayerActivity` via Intent: selain `VIDEO_PATH`, kirim juga `PLAYLIST_ID` (Long) dan `CURRENT_INDEX` (Int)
- Di `PlayerEventListener`, saat `onPlaybackStateChanged(STATE_ENDED)`:
  - Ambil `playlistEntries` dari `PlaylistRepository`
  - Jika `currentIndex + 1 < entries.size`: prepare video berikutnya via `VideoPlayerManager.prepare()`
  - Jika sudah di akhir: tampilkan end screen atau kembali ke HomeActivity

### Tentang Permission Handling

- Cek permission di `HomeActivity.onCreate()` menggunakan Accompanist Permissions
- Jika belum ada permission: tampilkan layar khusus (bukan crash) dengan tombol "Izinkan Akses"
- Setelah permission diberikan: panggil `viewModel.loadVideos()` dan `viewModel.loadFolders()` ulang

### Tentang Thumbnail Video

- Gunakan Coil 3 dengan `VideoFrameDecoder` (`coil3.video`, artifact `io.coil-kt.coil3:coil-video`) untuk load thumbnail dari file lokal; jika tidak ter-register otomatis, tambahkan `VideoFrameDecoder.Factory()` ke `ImageLoader`
- Atau gunakan `MediaMetadataRetriever.getFrameAtTime(0)` untuk generate thumbnail manual
- Cache thumbnail agar tidak di-generate ulang setiap scroll

### Tentang Serialisasi ControlLayoutConfig ke DataStore

- Gunakan `Kotlinx Serialization` untuk serialize `ControlLayoutConfig` menjadi JSON string (wajib menerapkan plugin `org.jetbrains.kotlin.plugin.serialization`, versi sama dengan Kotlin)
- Simpan sebagai satu string di DataStore key `PREF_CONTROL_LAYOUT_JSON`
- Deserialize saat dibaca, fallback ke `ControlLayoutConfig.Default` jika JSON rusak/kosong

### Tentang Room Database Migration

- Saat `AppDatabase.kt` pertama kali dibuat (Milestone 1–2), set `version = 1`
- Saat Milestone 10 menambah tabel playlist, bump ke `version = 2` dan buat `Migration(1, 2)` (Room 3: callback migrasi menerima `SQLiteConnection` dan bersifat suspend). Alternatif: `@AutoMigration(from = 1, to = 2)` karena hanya menambah tabel, dengan schema export aktif
- Jangan gunakan `fallbackToDestructiveMigration()` — data posisi terakhir harus tetap ada

### Tentang Auto-Hide Controls

- Implementasi di `PlayerViewModel` menggunakan `Job`:
  ```
  private var autoHideJob: Job? = null

  fun resetAutoHideTimer() {
    autoHideJob?.cancel()
    autoHideJob = viewModelScope.launch {
      delay(AUTO_HIDE_CONTROLS_DELAY_MS)
      _uiState.update { it.copy(isControlsVisible = false) }
    }
  }
  ```
- Timer di-reset setiap kali ada interaksi user di `GestureOverlay`
- Timer tidak jalan saat video dalam kondisi Paused (kontrol tetap visible)

### Tentang Orientasi PlayerActivity

- PlayerActivity **tidak** memasang `screenOrientation` apa pun di Manifest. Kunci landscape di Manifest adalah perilaku lama yang DIBUANG: ia mengabaikan sensor dan kunci rotasi HP, sehingga setiap video selalu dipaksa miring.
- Orientasi diatur saat runtime dari `orientationMode` (Bagian 10F): AUTO = `SCREEN_ORIENTATION_FULL_USER` (ikuti perangkat — sensor hanya aktif bila kunci rotasi HP mati), LANDSCAPE/PORTRAIT = paksa (mengalahkan kunci rotasi HP), kembali ke AUTO = membuka kunci buatan aplikasi.
- Pertahankan `configChanges="orientation|screenSize|screenLayout|keyboardHidden|smallestScreenSize"` agar Activity tidak dibuat ulang saat rotasi — posisi dan ExoPlayer tidak boleh mulai ulang.
- Layout player wajib diuji di portrait DAN landscape. Catatan target: aplikasi ini bertarget API 35; aturan Android 16+ yang mengabaikan batasan orientasi di layar lebar (≥600dp) berlaku untuk aplikasi bertarget 36+, jadi belum diadopsi di sini — evaluasi ulang Bagian ini bila targetSdk suatu saat dinaikkan melewati 35.

---

*Blueprint versi 1.0 — selesaikan setiap milestone berurutan, jangan loncat.*