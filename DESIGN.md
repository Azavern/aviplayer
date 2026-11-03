# DESIGN.md — Aturan Desain Aplikasi Android

> Aturan desain umum untuk aplikasi Android (Material Design 3, Jetpack Compose) yang dapat dipakai ulang di proyek mana pun. Nilai khusus proyek (ukuran tertentu, posisi tombol tertentu, daftar layar) tidak ada di sini; bila ada angka, ia berupa prinsip atau rentang umum. Aturan kerja agent ada di AGENTS.md.

## Daftar Isi

1. Prioritas & Tipe Aturan
2. Fidelitas terhadap Dokumen Proyek
3. Pahami Dulu, Baru Rancang
4. Fondasi: Material Design 3 & Dark-first
5. Tema
6. Warna & Token
7. Tipografi
8. Bentuk, Elevasi & Jarak
9. Tata Letak, Kepadatan & Ruang Kosong
10. Komponen Generik
11. Keadaan Layar & Keadaan Komponen
12. Gerak
13. Responsivitas: Orientasi, Ukuran Layar & Area Aman
14. Aksesibilitas
15. Konten, Bahasa & UX Writing
16. Gaya Berbasis Bayangan (Soft UI) — Opsional
17. Review & Checklist Desain
18. Catatan & Pertanyaan Terbuka

---

> **Prinsip inti:** bangun antarmuka yang nyaman dibuka orang: tepat sesuai dokumen proyek, mudah dipakai, dan terlihat rapi. Kualitas visual adalah bagian dari kebutuhan, bukan hiasan yang ditambahkan belakangan. Pola yang sudah dikenal yang dieksekusi dengan baik mengalahkan pola orisinal yang dieksekusi canggung.

## 1. Prioritas & Tipe Aturan

**Tipe aturan**
- `[HARD]` wajib terpenuhi (sengaja dibuat sedikit).
- `[DEFAULT]` pendekatan standar; menyimpang hanya bila dokumen proyek meminta, dan sebutkan dalam satu baris.
- `[CHECK]` diperiksa sebelum sebuah layar dinyatakan selesai.

**Urutan prioritas** (yang lebih tinggi selalu menang):
1. **Fidelitas terhadap dokumen proyek** (§2). `[HARD]`
2. **Lantai kegunaan dan aksesibilitas** (§13, §14). `[HARD]`
3. **Kualitas visual**: hierarki, kerapian, kenyamanan, konsistensi (§6–§10). Layar yang benar tetapi gersang, sesak, atau penuh teks mentah dianggap belum selesai.
4. **Karakter**: kepribadian visual diterima, tidak pernah mengorbankan 1–3.
5. **Minimalisme**: pilihan gaya, bukan tujuan. Polos tidak sama dengan bersih.

Estetika adalah alasan yang cukup: perlakuan visual apa pun (gradien, bayangan, ilustrasi, gerak) boleh dipakai bila membuat desain lebih baik dan tidak melanggar tingkat 1–2.

## 2. Fidelitas terhadap Dokumen Proyek `[HARD]`

- Dokumen kebutuhan produk dan dokumen teknis proyek adalah sumber kebenaran. Lapisan visual boleh sekaya apa pun, tetapi tidak boleh membengkokkan perilaku yang ditetapkan.
- Pakai istilah domain dokumen persis. Jangan mengganti nama konsep.
- Jangan menciptakan fitur, layar, opsi, atau aturan bisnis. Bila desain butuh sesuatu yang belum didefinisikan (pencarian, urutan, konfirmasi), rancang, **tandai sebagai asumsi**, dan daftarkan di Catatan Desain agar pemilik memutuskan.
- Tambahkan layar dan keadaan standar yang selalu dibutuhkan aplikasi nyata tetapi sering terlewat dokumen (tanpa izin, error, kosong, konfirmasi aksi destruktif) dan tandai sebagai *ditambahkan (tidak ada di dokumen)*.
- Presentasi (label, pengelompokan, ikon, tata letak, microcopy, polesan) adalah wilayah desain; fungsionalitas bukan.
- Bila dokumen proyek menetapkan nilai konkret (ukuran, warna bawaan, rentang), nilai itu mengalahkan bawaan di file ini.

## 3. Pahami Dulu, Baru Rancang `[DEFAULT]`

Sebelum bingkai pertama, tetapkan dalam 5–8 baris pendek (jadi Catatan Desain, §17):

1. **Produk dan pengguna**: apa ini, siapa memakainya, di perangkat dan situasi apa (satu tangan, cahaya terang, sambil bergerak), dan seberapa ahli.
2. **Peran layar**: tugas utama tiap layar dan prioritas informasi (apa yang dilihat pertama, kedua, ketiga).
3. **Inventaris layar dan model navigasi** dari dokumen proyek.
4. **Gaya**: sudah ditetapkan dokumen atau sistem desain? Ikuti persis. Belum? Tetapkan satu secara sengaja (§3.1).
5. **Yang dapat dipakai ulang**: komponen dan token yang ada sebelum membuat yang baru.

Bila dokumen tidak menjawab, asumsikan, tulis asumsinya satu baris, dan lanjut. Jangan menebak diam-diam.

### 3.1 Belum ada gaya? Tetapkan satu secara sengaja
Tanpa keputusan, hasil cenderung condong ke tampilan paling sering terlihat. Dengan keputusan, hasilnya kohesif dan mudah di-skin ulang.

1. Baca nada dari dokumen (domain, audiens, keseriusan).
2. Tetapkan tiga *dial* 1–10: **Kepadatan** (lega → padat), **Ekspresivitas** (konvensional → berani), **Gerak** (umpan balik saja → kaya). Aplikasi utilitas harian umumnya: Kepadatan 4–6, Ekspresivitas 4–5, Gerak 2–4.
3. Turunkan token (§6–§8): satu rona aksen, netral yang sedikit bernuansa, warna semantik, satu pasangan huruf, kepribadian radius dan bayangan.
4. Simpan sebagai token nyata dan pakai di semua layar; jangan memutuskan ulang per layar. Pergeseran gaya di sesi panjang adalah kegagalan paling umum.
5. **Preset cadangan (Modern Android)**: M3, gelap-dulu, satu aksen jenuh, permukaan tonal, radius sedang (12dp), satu keluarga huruf sans, ikon konsisten, bayangan hanya tipis.

Bila pengguna bereaksi ("terlalu polos", "terlalu ramai"), ubah **dial** dan token yang sesuai, jangan struktur.

## 4. Fondasi: Material Design 3 & Dark-first

- **Material Design 3 (M3)** adalah basis: peran warna (*color roles*), elevasi halus, sudut membulat konsisten, dan komponen M3 sebagai titik awal sebelum membuat komponen kustom.
- **Dark-first**: tema gelap dirancang **lebih dulu**; tema terang menyesuaikan dari sana. Setiap komponen diperiksa di gelap terlebih dahulu.
- **Keterbacaan di atas dekorasi.** Elemen tanpa informasi atau fungsi dibuang. Minimalis tetapi informatif.
- **Konsistensi di atas kreativitas lokal.** Pola yang sama untuk kebutuhan yang sama di seluruh aplikasi.
- Keadaan non-ideal (memuat, kosong, error, tanpa izin) adalah bagian dari desain sejak awal (§11).
- Ikuti pola Android yang dikenal pengguna (navigasi bawah, menu overflow, tombol kembali sistem, gestur sistem). Jangan menciptakan ulang yang sudah ada.

## 5. Tema

- Mode yang didukung: **Sistem**, **Terang**, **Gelap**, **AMOLED**.
  - *Sistem* mengikuti pengaturan perangkat.
  - *AMOLED* adalah tema gelap dengan **latar dan permukaan utama hitam murni (`#000000`)** untuk menghemat daya layar OLED dan memberi kontras maksimal. Tingkatan permukaan lain tetap dibedakan dengan elevasi tonal sangat halus atau garis tipis agar hierarki terbaca.
- **Warna aksen dapat diganti pengguna**: preset (*swatches*) dan input HEX kustom.
- **Dynamic color** dari sistem ditawarkan hanya pada perangkat yang mendukung; pada perangkat lain opsinya **disembunyikan**, bukan ditampilkan nonaktif tanpa penjelasan.
- Tema diterapkan **instan** (tanpa memulai ulang layar) dan **konsisten** di semua layar.
- Perubahan tema hanya mengubah warna dan perlakuan elevasi, **tidak** tata letak.
- Setiap kombinasi mode × aksen (termasuk aksen pilihan pengguna yang buruk) harus tetap lulus kontras (§6, §14); bila warna pilihan pengguna merusak kontras, komponen menurunkan ke warna "on" atau tone yang aman.

## 6. Warna & Token

### 6.1 Aturan
- `[HARD]` **Tidak ada warna yang di-hardcode di komponen.** Semua warna berasal dari peran warna tema (`primary`, `onPrimary`, `surface`, `onSurface`, `surfaceVariant`, `outline`, `error`, dst.). Pengecualian yang sah (mis. warna fungsional di atas konten yang tak terkendali, seperti lencana gelap di atas gambar) diberi nama sebagai token.
- Tentukan token sekali dan tarik semua nilai darinya. Butuh nilai baru? Tambah token, jangan nilai sekali-pakai.
- Aksen dipakai hemat dan bermakna: elemen aktif, progres, fokus, aksi utama. Bukan dekorasi. Pola umum: ±60% netral, ±30% permukaan dan teks sekunder, ±10% aksen.
- Hierarki teks lewat peran warna (utama vs sekunder), bukan warna acak.
- Warna semantik (error, peringatan, sukses, info) memakai peran konsisten dan **tidak** bergantung warna saja: sertakan ikon atau teks.
- Netral sedikit bernuansa rona aksen lebih hidup daripada abu murni, tetapi periksa kontrasnya.

### 6.2 Token minimum
Warna (aksen dengan variasi terang/gelap, netral latar/permukaan/garis, teks utama/sekunder, semantik dengan latar tinted), tipografi (§7), jarak (§8), bentuk (§8), elevasi (§8), gerak (§12). Hasilnya disimpan di satu tempat (tema) dan dirujuk, bukan disalin.

## 7. Tipografi

- Gunakan **type scale M3** (display, headline, title, body, label) secara berhierarki. Jangan membuat ukuran ad-hoc di luar skala, kecuali untuk elemen yang memang dapat diatur pengguna.
- Satu peran tipografi per fungsi: judul layar, judul item, teks isi, label metadata, label tombol.
- **Keterbacaan lebih penting daripada gaya.** Teks informatif tidak lebih kecil dari ±12sp. Body 14–16sp. Tanpa bobot tipis untuk teks isi.
- Satu keluarga huruf untuk UI; keluarga kedua (mis. untuk judul) hanya bila jelas berguna. Huruf yang dibundel dengan aplikasi tidak butuh unduhan saat runtime.
- **Perilaku teks panjang** per konteks:
  - Judul item di daftar: 1–2 baris, **ellipsis** di akhir.
  - *Marquee* (teks berjalan) hanya bila dipilih eksplisit atau pada satu baris, dan tidak berjalan di banyak item sekaligus.
  - Jangan memotong tengah teks atau mengecilkan font otomatis sampai tidak terbaca.
- Hormati ukuran font sistem; tata letak tidak boleh rusak pada skala font besar.
- Angka yang berubah atau sejajar (waktu, ukuran, hitungan) lebih mudah dibaca dengan angka tabular bila tersedia.

## 8. Bentuk, Elevasi & Jarak

- **Sudut membulat konsisten** memakai skala bentuk M3 (kecil ≈ 4–8dp, sedang ≈ 12dp, besar ≈ 16–28dp). Elemen sejenis memakai radius sama. Radius bersarang: radius dalam = radius luar − padding.
- **Elevasi halus**: bedakan permukaan dengan warna tonal lebih dulu, bayangan hanya pelengkap tipis. Hindari bayangan tebal. Beri nama 3–5 tingkat (datar, rendah, sedang, tinggi) dan pakai resep yang sama di mana-mana.
- Jarak memakai **grid 4dp/8dp**. Margin layar dan jarak antar item seragam di layar sejenis.
- **Jarak mengikuti kedekatan**: jarak dalam kelompok < jarak antar kelompok < jarak antar bagian. Pola umum: 8–16dp dalam kelompok, 24–32dp antar kelompok.
- Tidak ada komponen yang "menempel" ke tepi layar tanpa alasan, kecuali konten imersif.
- Hormati area aman: *cutout*, bilah status dan navigasi sistem, dan zona gestur sistem. Elemen interaktif tidak boleh tertutup atau berada di zona gestur.

## 9. Tata Letak, Kepadatan & Ruang Kosong `[DEFAULT]`

- Ruang ada untuk mengelompokkan, bukan untuk mengisi kanvas. Lubang kosong, kekakuan, dan dinding teks adalah cara paling umum sebuah desain "minimalis" gagal.
- Tidak ada area lebih dari ±seperempat layar yang kosong tanpa alasan. Untuk mengatasinya, berurutan: rapatkan jarak satu langkah → susun ulang menjadi kolom → perbesar konten yang penting → tambah konten relevan yang didukung dokumen → barulah biarkan kosong.
- Layar UI bukan dokumen. Ubah paragraf menjadi struktur: ubin angka, kartu, daftar berikon, chip, tab. Paragraf di chrome UI maksimal tiga baris.
- Beri setiap layar sedikitnya satu jangkar visual non-teks yang sesuai isinya: ikon, thumbnail, grafik, indikator progres, warna status, ilustrasi.
- Variasikan ukuran dan penekanan blok. Satu elemen dominan per layar; hindari halaman demi halaman yang berisi grid identik.
- Lapisi permukaan (kanvas → kartu → terangkat) dengan nada, garis, dan elevasi dari token, bukan hanya jarak.
- Kepadatan: aplikasi daftar padat 48–56dp per baris; konten bacaan lebih lega.

## 10. Komponen Generik

### 10.1 Daftar dan kisi untuk koleksi media
- **List** untuk koleksi kaya metadata yang perlu dipindai cepat (nama panjang, durasi, ukuran, asal). **Grid** untuk koleksi visual atau wadah (folder, album), dengan jumlah kolom tetap dan terbaca (umumnya 2–3 di ponsel portrait, bertambah mengikuti lebar).
- Item list: thumbnail di kiri, blok teks di tengah, aksi sekunder (menu) di kanan. Tinggi item konsisten; target sentuh ≥ 48dp.
- Gulir mulus: thumbnail dimuat malas dan di-cache; item ringan; *placeholder* tampil sebelum gambar siap.

### 10.2 Kartu
- Kartu mengelompokkan satu entitas; aksi utamanya = mengetuk seluruh kartu. Jangan menumpuk kartu di dalam kartu. Efek **ripple** pada semua yang dapat diketuk.

### 10.3 Chip metadata
- Metadata ringkas yang informatif (asal, kategori, status): ikon kecil + label singkat, gaya *tonal* agar tidak menyaingi aksi utama. Chip informatif tidak tampil seperti tombol kecuali memang interaktif.

### 10.4 Lencana (badge) di atas thumbnail
- Informasi seperti durasi: sudut thumbnail (umumnya kanan bawah), latar gelap semi-transparan (±60–80% opak), teks putih kecil, sudut membulat kecil, padding ringkas. Kontras terpenuhi pada thumbnail terang dan gelap.
- Lencana penanda sumber/kategori diletakkan di sudut dan tidak menutupi informasi utama.

### 10.5 Bottom navigation
- Untuk navigasi utama **3–5 tujuan** setara; ikon + label singkat.
- Terpilih: **ikon terisi + warna aksen/indikator**. Tidak terpilih: **ikon garis + warna sekunder**. Status terpilih tidak boleh hanya dibedakan oleh bayangan atau warna tipis.
- Perpindahan tujuan mempertahankan posisi gulir dan state masing-masing. Pada layar medium/expanded, pertimbangkan *navigation rail* (§13).

### 10.6 Overlay kontrol untuk konten imersif
- Konten imersif (video, foto layar penuh, pemutar media) tampil **tanpa bilah sistem**; kontrol berupa **overlay transparan** di atas konten.
- Muncul/hilang dengan **fade**; hilang otomatis setelah beberapa detik tanpa interaksi (±2–5 detik) saat konten aktif, dan **tetap tampil** saat konten dijeda.
- Kontras kontrol di atas konten tak terkendali dijaga dengan *scrim* gradien gelap tipis di tepi atau latar semi-transparan pada kontrol.
- Kontrol yang paling sering dipakai ada di zona yang terjangkau ibu jari pada orientasi mana pun.
- Mode penguncian kontrol (bila ada) punya satu jalan keluar yang selalu jelas dan terjangkau.

### 10.7 Pengaturan dan kontrol penyesuaian
- Slider untuk rentang kontinu (dengan nilai terbaca), *segmented button* untuk 2–4 pilihan eksklusif, switch untuk boolean, checklist untuk pilihan jamak, pemilih warna dengan swatches plus input HEX.
- Pengaturan yang memengaruhi tampilan menyertakan **pratinjau langsung** yang bereaksi seketika.

### 10.8 Menu, dialog, dan sheet
- Menu overflow untuk aksi sekunder per item; dialog untuk keputusan yang perlu input atau konfirmasi; bottom sheet untuk pilihan dari daftar singkat. Aksi destruktif selalu dengan konfirmasi atau kemampuan urungkan.
- Dialog: judul singkat, satu kalimat penjelasan, dua aksi dengan label kata kerja yang jelas.

## 11. Keadaan Layar & Keadaan Komponen

Setiap layar yang menampilkan data wajib dirancang untuk:

- **Memuat**: *shimmer* yang menyerupai bentuk konten akhir (jumlah dan proporsi mirip), bukan spinner layar penuh. Shimmer berhenti segera setelah data siap; tidak ada shimmer tanpa batas. Bila memuat gagal, beralih ke error.
- **Kosong**: ilustrasi/ikon sederhana, judul singkat, satu kalimat penjelasan, dan aksi bila ada langkah berikutnya yang berguna. Bedakan "belum ada data" dari "tidak ada hasil" (pencarian/filter).
- **Error**: apa yang gagal dalam bahasa awam + aksi pemulihan (coba lagi/kembali).
- **Tanpa izin**: layar khusus yang menjelaskan **mengapa** izin dibutuhkan dengan tombol aksi yang jelas untuk memberi atau mengatur izin.

Layar tidak boleh tampil kosong tanpa penjelasan, dan tidak boleh crash pada keadaan-keadaan ini.

**Keadaan komponen** yang dirancang: tombol (default, ditekan, fokus, nonaktif, memuat); input (default, fokus, terisi, error, nonaktif); daftar (memuat, kosong, tak ada hasil, error dengan coba lagi, terisi, terpilih); navigasi (default, aktif); kontrol multi-nilai (satu keadaan eksplisit per nilai). Keadaan nonaktif harus tetap dapat dibedakan dari aktif tanpa bergantung pada kontras teks saja.

## 12. Gerak

> Gerak harus **menjelaskan, merespons, dan memandu**, tidak pernah mengganggu.

### 12.1 Prinsip
- Setiap animasi punya tujuan: umpan balik interaksi, menjelaskan perubahan keadaan, menunjukkan hubungan sebelum-sesudah, atau membuat perpindahan terasa wajar.
- **Mulus, halus, konsisten, responsif, bertujuan, dapat diakses.**
- **Animasi tidak boleh menghalangi interaksi**: elemen tetap dapat diketuk selama transisi, tidak ada animasi yang harus ditunggu, input tidak ditelan.
- Hindari gerak dekoratif yang berulang tanpa henti; ambient hanya untuk indikator keadaan (shimmer, spinner).
- Hindari durasi panjang untuk interaksi yang sering terjadi.

### 12.2 Token durasi

| Token | Durasi | Pemakaian |
|---|---|---|
| Instant | 100 ms | Umpan balik tekan, mikro-interaksi |
| Fast | 150 ms | Perubahan ikon/keadaan kecil, keluar menu |
| Normal | 200 ms | Menu, switch, fade kontrol, animasi ukuran kecil |
| Moderate | 300 ms | Dialog, crossfade konten, perubahan layar kecil |
| Relaxed | 450 ms | Panel besar, masuknya layar |

Batas wajar: transisi kecil 150–300 ms; transisi besar tidak lebih dari ±450 ms. Pemuat dan indikator berulang (shimmer ≈ 1500 ms, spinner ≈ 700 ms) adalah pengecualian dan harus berhenti saat tidak dibutuhkan.

### 12.3 Token easing

| Token | Kurva | Pemakaian |
|---|---|---|
| Standard | `cubic-bezier(0.2, 0.8, 0.2, 1)` | Transisi umum |
| Enter | `cubic-bezier(0, 0, 0.2, 1)` | Elemen masuk (melambat di akhir) |
| Exit | `cubic-bezier(0.4, 0, 1, 1)` | Elemen keluar (mempercepat) |
| Linear | linear | Pemuat dan progres |

### 12.4 Pola yang konsisten
- **Fade** untuk muncul/hilang; **crossfade** untuk pergantian konten di tempat yang sama; **animasi ukuran** untuk elemen yang memuai/menyusut.
- Menu: fade + geser kecil + skala 0.95→1 (masuk ±200 ms, keluar ±150 ms).
- Dialog: scrim fade ±200 ms; panel fade + skala 0.95→1 + geser kecil ±250 ms.
- Tekan: skala sedikit mengecil (≈0.97) dan/atau ripple, 100–150 ms.
- Switch: knob dan warna track berubah bersamaan, 150–200 ms.
- Umpan balik gestur bersifat sesaat, ringan, dan menghilang sendiri.

### 12.5 Aksesibilitas dan kinerja gerak
- **Hormati pengaturan sistem untuk mengurangi/menghapus animasi.** Ganti gerak dengan perubahan instan atau fade minimal; shimmer dan teks berjalan berhenti menjadi tampilan statis. Informasi tetap tersampaikan tanpa gerak.
- Jangan menjadikan gerak satu-satunya penanda perubahan.
- Prioritaskan properti murah (transformasi, alpha). Batasi animasi yang memicu tata letak ulang di daftar panjang dan bayangan besar yang berubah terus-menerus.
- Jangan memakai animasi sebagai pengganti keadaan memuat yang sebenarnya.
- Interaksi khas web (hover, scroll reveal, tooltip hover, transisi halaman penuh) tidak ada padanan langsung di layar sentuh; jangan menirunya mentah-mentah.

## 13. Responsivitas: Orientasi, Ukuran Layar & Area Aman

- `[HARD]` **Rancang portrait dan landscape sejak awal, bersama-sama.** Dilarang merancang satu orientasi lalu "menyesuaikan" belakangan. Untuk tiap layar tentukan apa yang berpindah, tetap, dan menyesuaikan di kedua orientasi.
- Perubahan orientasi tidak mengubah **makna** atau **urutan prioritas** informasi, dan tidak memicu animasi yang mengganggu atau hilangnya konteks (posisi gulir, seleksi, input).
- **Konten berasio lebar di layar portrait**: tampilkan dengan rasio aslinya (*letterbox*) pada lebar penuh, tidak dipotong atau diregangkan; ruang kosong dibiarkan polos/hitam, dan kontrol tetap terjangkau.
- **Konten berasio tinggi di layar landscape**: tampilkan utuh dengan *pillarbox* atau penuh sesuai pilihan pengguna; jangan memotong tanpa sepengetahuan pengguna.
- Gunakan tata letak adaptif berdasarkan **kelas ukuran jendela** (compact < 600dp, medium 600–839dp, expanded ≥ 840dp), bukan asumsi satu ukuran perangkat. Jumlah kolom grid dan lebar maksimum konten menyesuaikan; navigasi bawah dapat berubah menjadi rail pada medium/expanded.
- Kontrol, teks, dan target sentuh tidak bertabrakan dengan area tidak aman pada orientasi mana pun (cutout di sisi saat landscape, bilah gestur di bawah). Konten mengisi layar penuh (*edge-to-edge*) dengan inset ditangani, bukan diabaikan.
- Layar yang dinyatakan satu orientasi oleh dokumen proyek tetap harus tahan terhadap perubahan ukuran jendela (mode multi-jendela, layar lipat) tanpa rusak.
- Skala font besar dan layar kecil (±360dp lebar) adalah kasus uji, bukan pengecualian.

## 14. Aksesibilitas `[HARD]`

- **Target sentuh minimum 48×48dp** untuk semua kontrol interaktif (area sentuh boleh lebih besar dari ikon visual); jarak antar target cukup agar tidak salah ketuk.
- **Kontras**: teks biasa ≥ **4,5:1** terhadap latar; teks besar, ikon fungsional, dan batas komponen yang menjadi satu-satunya penanda (mis. tepi input) ≥ **3:1**. Periksa di semua mode tema (termasuk AMOLED) dan di atas konten gambar/video.
- **Label ikon**: setiap ikon kontrol punya `contentDescription` yang bermakna (menyebut aksi, bukan bentuk; menyebut status bila berubah, mis. "kunci aktif"). Ikon dekoratif ditandai agar diabaikan.
- **Jangan mengandalkan warna saja** untuk status; sertakan ikon, teks, atau bentuk. Jangan mengandalkan bayangan saja untuk menandai elemen interaktif atau keadaan terpilih.
- Urutan fokus dan navigasi TalkBack/keyboard logis; kontrol yang muncul sebagai overlay dapat dijangkau dan diumumkan.
- Teks mengikuti skala font sistem dan tata letak bertahan pada skala besar.
- Perubahan keadaan sesaat (mis. pergantian mode) juga diumumkan secara aksesibel, bukan hanya visual.
- Hormati pengaturan pengurangan animasi (§12.5).

## 15. Konten, Bahasa & UX Writing `[DEFAULT]`

- **Konten realistis dan spesifik domain.** Isi setiap daftar, kartu, dan pratinjau dengan data contoh yang masuk akal (nama panjang dan pendek, nol dan angka besar). Layar kosong atau placeholder lorem ipsum menyembunyikan masalah tata letak.
- **Bahasa dan lokal.** Teks UI dalam bahasa yang ditetapkan dokumen proyek; format angka, tanggal, ukuran, dan durasi mengikuti lokal tersebut (mis. untuk Indonesia, pemisah desimal koma: "215,94 MB").
- **Label menamai aksi dengan tepat** ("Simpan perubahan", "Buat daftar putar"), dalam kalimat aktif dan huruf kapital kalimat. Satu aksi mempertahankan namanya sepanjang alur.
- **Error dan keadaan kosong** menyebut apa yang terjadi dan apa yang bisa dilakukan, dengan nada produk, tanpa menyalahkan dan tanpa basa-basi ("Ups!"). Keadaan kosong adalah undangan dengan satu aksi yang jelas.
- Kata biasa dan spesifik. Sebut hal seperti pengguna menyebutnya, bukan seperti sistem dibangun.

## 16. Gaya Berbasis Bayangan (Soft UI) — Opsional

Bila sebuah proyek memilih gaya *soft UI/neumorphism* (kedalaman lewat bayangan ganda terang-gelap pada permukaan berwarna hampir sama dengan latar), terapkan di atas fondasi M3 dengan batasan berikut:

- **Karakter**: permukaan hampir selaras dengan latar; bayangan gelap di satu sisi dan sorotan terang di sisi lawan memberi kesan terangkat; **bayangan cekung (inset)** untuk input, area pencarian, trek progres, dan kontrol terpilih; keadaan ditekan = bayangan inset.
- **Kelemahan bawaan**: kontras rendah. Karena itu:
  1. Jangan jadikan bayangan satu-satunya penanda interaksi atau keadaan terpilih; tambahkan isi warna, ikon, atau garis.
  2. Teks memenuhi 4,5:1; batas komponen yang menjadi satu-satunya penanda memenuhi 3:1.
  3. Bedakan keadaan nonaktif, ditekan, dan terpilih tanpa bergantung pada bayangan saja.
  4. Jangan memakai warna "redup" untuk teks informatif; periksa **setiap** token teks terhadap latar sebenarnya.
- **Tema gelap dan AMOLED**: bayangan terang tidak terlihat pada hitam. Gantikan dengan elevasi tonal dan garis tipis; jangan memaksa bayangan ganda di sana.
- **Cakupan**: gunakan pada permukaan kontrol dan kartu; hindari pada konten padat atau data tabular (baris daftar sebaiknya datar) dan pada overlay di atas konten gambar/video.
- **Kinerja**: bayangan blur besar pada banyak item di daftar panjang mahal; pakai tingkat bayangan lebih kecil atau permukaan datar pada item yang berulang.
- **Gerak**: umpan balik tekan lewat skala kecil dan inset; durasi dari token §12.2.
- Nilai dan palet konkret gaya ini milik dokumen spesifikasi visual proyek, bukan file ini.

## 17. Review & Checklist Desain `[CHECK]`

Tinjau setiap layar pada tiga lebar (±360, ±600, ±840dp) dan dua orientasi dengan data realistis sebelum menyatakannya selesai.

- [ ] **Dokumen** — setiap entitas, aksi, dan keadaan di dokumen proyek muncul; istilah sesuai; tak ada yang dibuat tanpa ditandai.
- [ ] **Titik fokus** — satu elemen utama per layar; hierarki terbaca saat disipitkan.
- [ ] **Tanpa lubang** — tak ada area kosong besar tanpa tujuan.
- [ ] **Tanpa dinding teks** — setiap layar punya jangkar visual; prosa terstruktur.
- [ ] **Konsistensi** — satu kepribadian radius, satu resep elevasi, satu keluarga ikon; jarak dan warna hanya dari token; tak ada warna/ukuran yang di-hardcode.
- [ ] **Gelap-dulu** — tema gelap diperiksa dulu; terang dan AMOLED diperiksa.
- [ ] **Tipografi** — skala M3, teks panjang punya aturan ellipsis/marquee, skala font besar aman.
- [ ] **Keadaan** — memuat/kosong/error/tanpa izin dirancang untuk setiap layar data; keadaan komponen terlihat.
- [ ] **Responsif** — portrait dan landscape dirancang bersama; letterbox/pillarbox diatur; kelas ukuran jendela diperiksa; area aman dihormati.
- [ ] **Gerak** — token dipakai, singkat, tidak menghalangi interaksi; pengurangan animasi dihormati.
- [ ] **Aksesibilitas** — target ≥ 48dp, kontras terpenuhi, label ikon lengkap, status tidak hanya warna/bayangan.
- [ ] **Konten** — realistis, bahasa dan lokal benar; tanpa lorem ipsum.
- [ ] **Dipercaya?** — bandingkan dengan aplikasi matang sekategori. Bila tampak seperti wireframe, tambah polesan; bila ramai, kurangi dekorasi yang paling tidak berguna lebih dulu.

**Sertakan Catatan Desain** (maksimal 10 baris, di balasan atau dokumen):

```
Catatan Desain
- Peran & gaya: ...
- Dial: Kepadatan x · Ekspresivitas y · Gerak z
- Token: aksen ..., netral ..., huruf ..., radius ..., elevasi ...
- Asumsi: ...
- Layar/keadaan ditambahkan (tidak ada di dokumen): ...
- Kebutuhan dari sisi logika/data: ...
- Untuk mengubah tampilan: ubah token, bukan struktur.
```

## 18. Catatan & Pertanyaan Terbuka

Tidak ada item khusus proyek. File ini sengaja memuat prinsip, bukan keputusan proyek. Angka berupa rentang adalah titik awal; nilai final per proyek ditetapkan di dokumen spesifikasi visual proyek yang bersangkutan.
