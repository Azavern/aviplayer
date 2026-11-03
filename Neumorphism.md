# Neumorphism UI Design System — Versi Android (Terang & Gelap)

Color Palette, Typography & Visual Guidelines

Design Direction: **Neumorphism (Soft UI) dua tema — Light & Dark**
Visual Character: Soft, Elegant, Minimalist, Futuristic
Primary Tone: Lavender, Periwinkle, Soft Blue
Typography: Manrope + Inter
Primary Stack: **Android — Jetpack Compose / Material 3**
Status: Diadaptasi dari versi web (Light-only, CSS) per 3 November 2026. Palet terang asli dipertahankan; **palet gelap ditambahkan**; satuan px diganti dp/sp; variabel CSS diganti token Compose; komponen web (sidebar, tabel HTML, modal CSS) diganti padanan Android.

---

## 1. Design Philosophy

Desain menggunakan pendekatan Neumorphism (Soft UI), yaitu gaya antarmuka yang memberikan ilusi kedalaman melalui permainan cahaya dan bayangan: elemen tampak *timbul dari* atau *tenggelam ke* permukaan yang warnanya sama dengan latar.

Karakteristik utama:

- Soft shadows ganda (bayangan gelap di satu sisi, cahaya di sisi berlawanan) untuk kesan elevated dan inset.
- Rounded corners besar untuk kesan lembut.
- Low-contrast surfaces dengan pemisahan elemen yang tetap jelas.
- Pastel color accents (tema terang) / aksen diterangkan (tema gelap) sebagai identitas visual.
- Clean typography dengan hierarki yang konsisten.
- Minimal decorative elements untuk menjaga fokus pada konten.

Prinsip utama: **Soft appearance, clear functionality.**

### 1.1 Syarat & Batasan Neumorphism di Android

- Ilusi neumorphism hanya bekerja bila **warna permukaan elemen sama (atau nyaris sama) dengan warna latar di belakangnya**. Latar harus polos/seragam — neumorphism **tidak** diterapkan di atas foto, thumbnail, video, atau gradient ramai. Untuk layar pemutar media: permukaan di atas video tetap flat transparan gelap, bukan neumorphic.
- Tema **AMOLED (hitam murni #000000) tidak kompatibel** dengan neumorphism: di atas hitam pekat, bayangan gelap tidak terlihat dan ilusi kedalaman runtuh. Pada mode AMOLED gunakan *varian flat*: permukaan diganti nuansa abu sangat gelap dengan border halus 1 dp sebagai pemisah, tanpa bayangan ganda (lihat §3.4).
- Neumorphism adalah gaya permukaan, bukan pengganti hierarki: teks, ikon, dan state interaktif tetap wajib jelas tanpa mengandalkan bayangan (lihat §9 Aksesibilitas).

---

## 2. Color Palette

Palet dirancang dengan dominasi lavender dan cool blue, dipadukan netral sedikit kebiruan. Setiap token memiliki nilai **Light** dan **Dark**; nama token tidak berubah antar tema.

### 2.1 Primary Colors

| Token | Light | Dark | Penggunaan |
|---|---|---|---|
| Primary | `#6655F5` | `#8B7FFF` | Tombol utama, CTA, aksen aktif (di Dark diterangkan agar kontras di atas latar gelap) |
| Primary Pressed | `#5745E8` | `#9A8FFF` | Keadaan ditekan (Android tidak memakai hover; state ini menggantikannya) |
| Primary Active | `#4938D4` | `#7A68F5` | Keadaan aktif/sesaat setelah tekan |
| Primary Light | `#E8E5FF` | `#332E63` | Highlight dan selected state (di Dark menjadi ungu gelap) |
| Primary Soft | `#F0EEFF` | `#26224A` | Background aksen |
| Primary Dark | `#39318F` | `#CFC9FF` | Teks/ikon beraksen yang butuh kontras di tema masing-masing |

Catatan tombol utama: isian tombol primer boleh memakai `#6655F5` yang sama di kedua tema (teks putih di atasnya); yang berubah antar tema adalah warna aksen untuk ikon/teks/garis di atas permukaan.

### 2.2 Background & Surface Colors

| Token | Light | Dark | Penggunaan |
|---|---|---|---|
| Background | `#E9EBF8` | `#191B2B` | Background utama |
| Background Soft | `#EFF0FA` | `#1E2033` | Area sekunder |
| Surface | `#EAECF8` | `#1C1E30` | Card dan panel (sengaja ≈ Background — syarat ilusi neumorphism) |
| Surface Light | `#F4F5FC` | `#262842` | Area elevated / sisi cahaya |
| Surface Dark | `#DDE1F1` | `#12131F` | Area inset / sisi gelap |
| Surface Pressed | `#E4E7F6` | `#23253E` | Permukaan saat ditekan |

### 2.3 Neutral Colors

| Token | Light | Dark | Penggunaan |
|---|---|---|---|
| Text Primary | `#171A36` | `#F1F2FA` | Judul dan teks utama |
| Text Secondary | `#515A8C` | `#B4B9D4` | Deskripsi dan informasi |
| Text Muted | `#858CAD` | `#7E84A6` | Placeholder dan metadata |
| Text Disabled | `#AEB3CA` | `#565A78` | Teks nonaktif |
| Border Subtle | `#DCE0F0` | `#2C2F4D` | Pemisah elemen |
| Divider | `#D7DBED` | `#272A44` | Garis pemisah |

### 2.4 Accent Colors

Digunakan untuk membedakan kategori informasi, status, dan data visualisasi. Di tema Dark, warna aksen diterangkan dan warna latar aksen digelapkan.

| Token | Light | Light BG | Dark | Dark BG | Penggunaan |
|---|---|---|---|---|---|
| Blue | `#438BFA` | `#DCEBFF` | `#6EA8FF` | `#22365C` | Informasi dan aktivitas |
| Purple | `#8875FA` | — | `#A08FFF` | — | Highlight sekunder |
| Cyan | `#55C9D5` | — | `#6FD8E2` | — | Data dan indikator |
| Green | `#25B993` | `#D9F5EC` | `#43D3AB` | `#173D33` | Success dan positive trends |
| Orange | `#F5A443` | `#FFF0DA` | `#FFB65C` | `#46331B` | Warning dan perhatian |
| Red | `#E76B82` | `#FCE4E9` | `#FF8FA3` | `#47222C` | Error dan destructive action |

### 2.5 Gradient Palette

Gradient digunakan sebagai aksen, bukan sebagai background seluruh halaman/layar. Berlaku sama di kedua tema kecuali Lavender Gradient yang hanya untuk tema terang.

| Nama | Gradient |
|---|---|
| Primary Gradient | `#8172FF → #5946EF` (135°) |
| Lavender Gradient (Light saja) | `#F0EEFF → #DCD9FF` |
| Blue Gradient | `#E8F1FF → #D5E5FF` (Light) / `#2A3D66 → #22365C` (Dark) |
| Purple Gradient | `#EDE6FF → #D9CCFF` (Light) / `#372F66 → #2A2452` (Dark) |
| Cyan Gradient | `#E1F8F7 → #C9F0EF` (Light) / `#173D3F → #122E33` (Dark) |

Di Compose, gradient dinyatakan sebagai `Brush.linearGradient(...)` dengan daftar warna token di atas.

### 2.6 Pemetaan ke Material 3 Color Roles

Agar selaras dengan tema Material 3: `primary` ← Primary, `background` ← Background, `surface` ← Surface, `onBackground` ← Text Primary, `onSurface` ← Text Primary, `onSurfaceVariant` ← Text Secondary, `outlineVariant` ← Divider. Skema terang dan gelap masing-masing dibangun dari kolomnya (lightColorScheme / darkColorScheme), sehingga komponen Material standar otomatis mengikuti gaya ini.

---

## 3. Neumorphism Shadow System

Shadow adalah elemen terpenting desain ini. Setiap bayangan selalu **berpasangan**: bayangan gelap bergeser ke kanan-bawah, bayangan terang (cahaya) bergeser ke kiri-atas dengan jarak & blur yang sama. Di tema Light, warna cahaya adalah putih; di tema Dark, warna cahaya adalah Surface Light yang diterangkan, dan bayangan gelapnya lebih pekat.

### 3.1 Standard Shadows (Raised)

| Token | Offset / Blur | Light (gelap / cahaya) | Dark (gelap / cahaya) | Penggunaan |
|---|---|---|---|---|
| Shadow XS | 3 dp / 6 dp | `#D2D6E7` / `#FFFFFF` | `#101222` / `#2A2E4C` | Elemen kecil |
| Shadow SM | 5 dp / 10 dp | `#D1D5E7` / `#FFFFFF` | `#101222` / `#2A2E4C` | Tombol |
| Shadow MD | 8 dp / 16 dp | `#CDD2E5` / `#FFFFFF` | `#0E1020` / `#2C3050` | Card |
| Shadow LG | 12 dp / 24 dp | `#CBD0E3` / `#FFFFFF` | `#0E1020` / `#2C3050` | Panel besar, dialog |

Arah selalu konsisten: gelap di (+x, +y), cahaya di (−x, −y), seolah cahaya datang dari kiri-atas di semua layar dan tema.

### 3.2 Inset Shadows (Pressed / Cekung)

Kesankan permukaan tenggelam dengan pasangan bayangan yang sama tetapi digambar di **dalam** tepi elemen:

- Spesifikasi: inset 4 dp offset / 8 dp blur, memakai pasangan warna tema yang sama seperti Shadow SM.
- Penggunaan: input field, search bar, area form, kontrol terpilih (selected), track slider/progress, tombol yang sedang ditekan.

### 3.3 Button States (kedua tema)

| State | Perlakuan |
|---|---|
| Default | Shadow SM (raised) |
| Ditekan (pressed) | Beralih ke **inset** Shadow XS–SM + latar Surface Pressed; transisi mengikuti Motion.md (Instant–Fast) |
| Terpilih / aktif menetap | Inset ringan menetap atau isian Primary Light tema, sesuai konteks |
| Fokus (keyboard/D-pad) | Outline Primary 2 dp **ditambahkan** di luar bayangan — bayangan saja tidak cukup sebagai penanda fokus |
| Disabled | Tanpa bayangan (flat), teks Text Disabled, tidak merespons sentuhan |

Catatan Android: tidak ada state hover pada sentuhan; urutan state yang berlaku adalah default → ditekan → dilepas. Pada perangkat berpenunjuk (mouse/stylus) keadaan hover boleh meminjam perlakuan "ditekan ringan" (Shadow XS), tetapi tidak boleh diandalkan.

### 3.4 Varian Flat untuk AMOLED

Pada latar hitam murni, ganti seluruh bayangan ganda dengan: latar `#000000`, permukaan `#0B0B10`, pemisah Border Subtle versi AMOLED `#1E1E28` setebal 1 dp, dan state ditekan ditandai perubahan latar (bukan bayangan). Token warna teks & aksen mengikuti kolom Dark.

---

## 4. Border Radius System

| Token | Value | Penggunaan |
|---|---|---|
| Radius XS | 6 dp | Small elements |
| Radius SM | 10 dp | Badge |
| Radius MD | 14 dp | Input dan button |
| Radius LG | 20 dp | Cards |
| Radius XL | 24 dp | Large panels, dialog, bottom sheet (sudut atas) |
| Radius 2XL | 30 dp | Panel hero / penekanan khusus |
| Radius Full | 50% (CircleShape) | Pills dan avatar |

## 5. Spacing System

Menggunakan kelipatan 4 dp sebagai basis (mengikuti grid 4 dp Material):

| Token | Value |
|---|---|
| 1 | 4 dp |
| 2 | 8 dp |
| 3 | 12 dp |
| 4 | 16 dp |
| 5 | 20 dp |
| 6 | 24 dp |
| 8 | 32 dp |
| 10 | 40 dp |
| 12 | 48 dp |
| 16 | 64 dp |

Rekomendasi: padding kartu 20–24 dp; jarak antar-seksi 32–48 dp; jarak antar-field form 16 dp; spacing internal komponen 8–16 dp. Target sentuh minimum semua elemen interaktif: **48 × 48 dp**.

---

## 6. Typography System

### 6.1 Font Families di Android

Menggunakan kombinasi dua keluarga font:

- **Primary: Manrope** — heading, judul layar, judul kartu, label navigasi, angka statistik yang ditonjolkan. Karakter: modern geometric sans-serif, bersih, elegan.
- **Secondary: Inter** — body text, label form, isi input, deskripsi dan metadata. Karakter: sangat mudah dibaca, dirancang untuk antarmuka digital, konsisten di berbagai ukuran.

Penyediaan font di Android (pilih satu, konsisten):

1. **Bundled font:** sertakan berkas font (TTF/OTF, atau variable font) di `res/font` / aset aplikasi — paling andal karena aplikasi dapat bekerja offline penuh.
2. **Downloadable fonts (Google Fonts):** Compose dapat mengambil Manrope & Inter lewat penyedia Google Fonts; unduhan terjadi sekali dan butuh jaringan saat pertama kali — untuk aplikasi offline-only, cara ini hanya boleh sebagai *fallback* dengan font sistem sebagai bawaan sampai unduhan tersedia.

Fallback wajib: bila kedua font belum tersedia, gunakan font sistem (sans-serif) tanpa mengubah tata letak secara berarti. Weight 750 hanya dipakai bila variable font mendukungnya; jika tidak, gunakan 700 atau 800.

### 6.2 Typography Scale (sp)

Ukuran dalam **sp** (bukan dp/px) agar mengikuti skala font aksesibilitas pengguna.

| Elemen | Font | Size | Weight | Line Height |
|---|---|---|---|---|
| Display | Manrope | 40 sp | 800 | 1.15 |
| H1 | Manrope | 32 sp | 800 | 1.2 |
| H2 | Manrope | 26 sp | 750 | 1.25 |
| H3 | Manrope | 21 sp | 700 | 1.3 |
| H4 | Manrope | 18 sp | 700 | 1.35 |
| Body Large | Inter | 16 sp | 400 | 1.6 |
| Body | Inter | 14 sp | 400 | 1.6 |
| Body Small | Inter | 13 sp | 400 | 1.5 |
| Caption | Inter | 12 sp | 400 | 1.4 |
| Button | Manrope | 14 sp | 700 | 1.2 |
| Statistic | Manrope | 28 sp | 800 | 1.2 |

Skala ini dipetakan ke `Typography` Material 3 (display*/headline*/title*/body*/label*) agar komponen standar ikut konsisten.

### 6.3 Typography Color

| Elemen | Light | Dark |
|---|---|---|
| Main Heading | `#171A36` | `#F1F2FA` |
| Body Text | `#515A8C` | `#B4B9D4` |
| Secondary Text | `#737B9F` | `#9AA0BE` |
| Placeholder | `#9299B6` | `#6E7394` |
| Link | `#6655F5` | `#8B7FFF` |
| Disabled | `#AEB3CA` | `#565A78` |

---

## 7. Component Design Guidelines

### 7.1 Cards

- Border radius: 20–24 dp (Radius LG–XL). Background: Surface. Shadow: MD (kedua tema). Padding: 20–24 dp.
- Hindari border yang kontras; pada tema Dark, bila bayangan kurang terbaca di perangkat tertentu, tambahkan Divider 1 dp sebagai pengaman.

### 7.2 Buttons

- Border radius: 14–18 dp. Tinggi: 48 dp (target sentuh).
- Primary button: Primary Gradient atau Primary polos dengan teks putih. Secondary button: Surface dengan Shadow SM.
- State mengikuti §3.3 (ditekan = inset). Label memakai gaya Button pada skala tipografi.

### 7.3 Input Fields & Search Bar

- Border radius: 14–16 dp. Background: Surface. **Inset shadow** selalu. Tinggi: 48 dp.
- Focus state: outline Primary 2 dp yang terlihat jelas (kedua tema) ditambah kursor aksen; jangan hanya mengubah bayangan.

### 7.4 Dialog & Bottom Sheet (pengganti Modal web)

- Dialog: radius 24 dp, Background Surface, Shadow LG, scrim gelap transparan secukupnya (sama di kedua tema).
- Bottom sheet: radius XL di dua sudut atas, Shadow LG, latar mengikuti Background tema.

### 7.5 Navigation (Navigation Bar / Rail)

- Item aktif: latar Primary Light tema, ikon Primary tema, label Text Primary.
- Wadah navigasi: Surface dengan shadow lembut terbalik arahnya (cahaya dari bawah diperbolehkan khusus untuk bar bawah) atau Divider atas 1 dp pada tema Dark.

### 7.6 Switch, Slider & Chips (komponen khas Android)

- **Switch:** track dalam keadaan mati memakai inset; kenop raised (Shadow XS). Keadaan menyala: track Primary Soft/Primary Light tema, kenop tetap raised dengan ikon/aksen Primary.
- **Slider/Seek:** track memakai **inset** (cekung); thumb raised (Shadow SM) berbentuk lingkaran; nilai aktif track memakai Primary.
- **Chips:** tidak terpilih = raised ringan; terpilih = inset + Primary Light tema + teks/ikon Primary tema.
- **FAB:** Shadow LG, isian Primary Gradient/Primary; ditekan menyusut ke Shadow MD.

### 7.7 Lists (pengganti Tables web)

Untuk data padat (daftar berkas/item), jangan menerapkan neumorphism berlebihan:

- Latar bersih mengikuti Background; pemisah Divider halus.
- Baris interaktif: feedback ditekan berupa ripple tipis atau perubahan latar Surface Pressed — tanpa mengangkat bayangan per baris.
- Lencana status (badge) memakai Accent Colors tema; teks baris memakai Text Primary/Secondary agar kontras terjaga.

---

## 8. Iconography

Rekomendasi Android: **Material Symbols / Material Icons** bergaya outline (keluarga bawaan platform, konsisten dengan Material 3). Bila proyek memilih Lucide, gunakan port Android-nya secara konsisten — jangan mencampurkan dua gaya ikon dalam satu aplikasi.

Panduan (dp):

- Default size: 20 dp. Small icon: 16 dp. Large icon: 24 dp.
- Ketebalan goresan setara 1.8–2 dp, konsisten di seluruh aplikasi.
- Warna: Primary tema atau Text Secondary, mengikuti keadaan (aktif/nonaktif).

---

## 9. Motion & Interaction

Seluruh durasi, easing, dan perilaku gerak diatur di **Motion.md** (versi Android) dan tidak diduplikasi di sini. Khusus permukaan neumorphic, yang berubah saat interaksi hanyalah parameter bayangannya:

| Interaction | Duration | Behavior |
|---|---|---|
| Button Press | 100 ms | Raised → inset shadow |
| Pilihan/chip berubah | 150 ms | Inset + warna selected |
| Dialog / Bottom Sheet | 200–300 ms | Fade + scale/slide (per Motion.md) |
| Card ditekan | 200 ms | Skala 0.98 + bayangan menguat sesaat |

Hormati pengaturan animasi sistem pengguna (lihat Motion.md Bagian 8): bila animasi dimatikan sistem, perubahan bayangan terjadi seketika.

---

## 10. Accessibility Guidelines

Neumorphism memiliki kelemahan alami dalam hal kontras karena background, surface, dan shadow cenderung berdekatan — **di tema terang maupun gelap**. Untuk mengatasinya:

1. Jangan menggunakan shadow sebagai satu-satunya penanda interaksi.
2. Pastikan teks memenuhi standar kontras WCAG AA (lihat target di bawah) — periksa di **kedua** tema, terutama Text Muted di atas Surface.
3. Gunakan outline yang jelas pada fokus keyboard/D-pad.
4. Bedakan disabled, ditekan, aktif, dan selected state secara bentuk/warna, bukan hanya bayangan.
5. Jangan hanya mengandalkan warna untuk menunjukkan status.
6. Pastikan input memiliki label yang terlihat.
7. Hindari teks kecil di atas background pastel (terang) atau permukaan gelap berkontras rendah (gelap).

Target kontras:

- Teks normal: minimal 4.5:1.
- Teks besar: minimal 3:1.
- Komponen UI dan indikator grafis penting: minimal 3:1 terhadap warna di sekitarnya.

---

## 11. Design Tokens (Compose)

Implementasi awal token sebagai konstanta Compose — satu sumber untuk kedua tema (struktur contoh, nama mengikuti token dokumen ini):

```kotlin
// Warna (Color(0xFF......)) — contoh sebagian
object NeuColors {
    // Light
    val PrimaryLightTheme = Color(0xFF6655F5)
    val BackgroundLight   = Color(0xFFE9EBF8)
    val SurfaceLight      = Color(0xFFEAECF8)
    val TextPrimaryLight  = Color(0xFF171A36)
    // Dark
    val PrimaryDarkTheme  = Color(0xFF8B7FFF)
    val BackgroundDark    = Color(0xFF191B2B)
    val SurfaceDark       = Color(0xFF1C1E30)
    val TextPrimaryDark   = Color(0xFFF1F2FA)
}

// Spesifikasi bayangan ganda per tema
data class NeuShadowSpec(
    val offsetDp: Float, val blurDp: Float,
    val darkColor: Color, val lightColor: Color
)
// Light MD: offset 8, blur 16, gelap #CDD2E5, cahaya #FFFFFF
// Dark  MD: offset 8, blur 16, gelap #0E1020, cahaya #2C3050
```

Catatan implementasi bayangan: Compose bawaan hanya menyediakan satu bayangan elevasi per elemen. Bayangan ganda neumorphic (raised maupun inset) memerlukan Modifier kustom — gambar dua bentuk rounded di belakang/di dalam elemen dengan warna & offset token di atas, diburamkan (blur render effect tersedia API 31+; untuk API 29–30 gunakan pendekatan gambar berlapis alpha atau terima bayangan yang sedikit lebih tegas). Karena permukaan harus sewarna latar, Modifier ini hanya dipakai pada latar polos berwarna Background/Surface. Bayangan statis dihitung sekali per keadaan — jangan dihitung ulang setiap frame (lihat Motion.md Bagian 9).

---

## 12. Recommended Design Direction

| Category | Selection |
|---|---|
| Main Style | Neumorphism (Soft UI) — **Light & Dark** |
| Secondary Style | Minimalism |
| Primary Color | Lavender / Periwinkle (`#6655F5`, diterangkan di tema gelap) |
| Supporting Colors | Blue, Cyan, Purple |
| Background | Cool Lavender Gray (terang) / Deep Indigo Charcoal (gelap) |
| Heading Font | Manrope |
| Body Font | Inter |
| Icon Style | Material Symbols outline (konsisten satu gaya) |
| Shadow Style | Soft Dual Shadow (pasangan warna per tema) |
| Border Radius | 14–24 dp |
| Motion | Subtle & Smooth (per Motion.md versi Android) |
| AMOLED | Varian flat tanpa bayangan (§3.4) |

Final Design Principle:

> «A soft, elegant, and modern interface that balances visual depth with functional clarity — in light and in dark.»
