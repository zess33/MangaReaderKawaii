# MangaReader Kawaii (SweetReader) 🌸✨

Una aplicación móvil nativa para Android desarrollada con cariño en **Kotlin**, **Jetpack Compose** y **Material 3**. Diseñada como un regalo personalizado con una estética visual tierna y kawaii (tonos pastel Rosa Sakura, Lavanda, Melocotón, Menta y Modo Oscuro Pastel).

---

## 📱 Capturas y Características Principales

### 1. 🌸 Catálogo de Contenido MangaDex en Español
- **Exclusividad en Español**: Capítulos y mangas filtrados estrictamente en Español (`es-la` / `es`).
- **Géneros Favoritos Prioritarios**: Boys' Love (BL), Yaoi, Romance, Shoujo, Josei, Fantasía, Manhwa, Webtoons y Shounen Ai.
- **Filtro interactivo**: Píldoras de colores pastel para filtrar al instante por género.
- **Búsqueda en tiempo real**: Con scroll infinito y debounce de búsqueda.

### 2. 👁️ Lector Nativo de Manga y Webtoon
- **Modo Vertical Continuo**: Desplazamiento fluido tipo Webtoon oficial con ajuste automático al 100% del ancho sin deformación.
- **Zoom Fluido y Libre**: Doble toque para zoom rápido (2.5x) y pellizco libre con 2 dedos (hasta 4x).
- **Filtros de Confort Visual**:
  - ☀️ **Normal**: Colores vibrantes originales.
  - 📜 **Sepia Cálido**: Tinte ámbar suave para lectura relajada.
  - 🌙 **Tinte Nocturno**: Filtro tenue anti-fatiga para leer en la oscuridad.
- **Navegación con Botones de Volumen**: Opcional desde Ajustes.

### 3. 💾 Descargas Offline (Modo Avión)
- Botón en cada capítulo para descargarlo completo en la memoria del celular.
- Lectura 100% sin internet mediante **WorkManager** y almacenamiento privado.

### 4. 📚 Biblioteca Dulce y Gestión en Lote
- Pestañas tiernas: **"🌸 Favoritos"**, **"📖 Leyendo"**, **"⏳ Pendientes"** y **"✨ Completados"**.
- **Modo Selección Múltiple**: Elimina o mueve capítulos entre categorías en lote.
- **Historial Cronológico**: Con barra de porcentaje de lectura y botón de reanudación rápida.

### 5. 🎨 5 Temas Pastel y Dedicatoria Personalizada
- **🌸 Sakura Pink**: Cerezo en flor y dulzura (Predeterminado).
- **💜 Pastel Lavender**: Lilas soñadores y serenidad.
- **🍑 Peachy Cream**: Melocotón cálido y crema.
- **🌿 Mint Green**: Menta fresca y armonía.
- **🌙 Pastel Dark Mode**: Fondo pizarra suave con acentos pastel.
- **✨ Dedicatoria Personalizada**: *"Hecho con mucho cariño para ti por Uriel Huerta"* personalizable desde los Ajustes.

---

## 🏗️ Arquitectura Técnica

- **Lenguaje**: Kotlin 1.9+
- **UI Toolkit**: Jetpack Compose con Material 3 y Navigation Compose
- **Patrón de Diseño**: MVVM (Model-View-ViewModel) + Clean Architecture + Repository Pattern
- **Base de Datos Local**: AndroidX Room Database con Flow reactivo
- **Red / API**: Retrofit 2 + OkHttp 4 + Kotlinx Serialization
- **Carga de Imágenes**: Coil Compose con caché de memoria y disco
- **Descargas en Segundo Plano**: AndroidX WorkManager
- **Preferencias**: AndroidX DataStore Preferences

---

## 🚀 Cómo abrir y ejecutar el proyecto

1. Abre **Android Studio** (Hedgehog, Iguana, Jellyfish o posterior).
2. Selecciona **Open** y navega a la carpeta del proyecto:
   `/Users/pedrourielhuertaplantillas/.gemini/antigravity/scratch/MangaReaderKawaii`
3. Deja que Android Studio sincronice las dependencias con Gradle.
4. Conecta tu teléfono Android o inicia un Emulador.
5. Haz clic en **Run 'app'** (▶️) para compilar e instalar la app.
