package com.kawaii.mangareader.domain.model

enum class ReadingMode(val title: String) {
    WEBTOON_VERTICAL("Webtoon (Continuo vertical)"),
    PAGED_RTL("Paginado Manga (Derecha a Izquierda)"),
    PAGED_LTR("Paginado Horizontal (Izquierda a Derecha)")
}

enum class VisualFilter(val title: String, val description: String) {
    NORMAL("Normal", "Colores originales del autor"),
    SEPIA_WARM("Sepia Cálido", "Tinte ámbar suave para lectura relajante"),
    NIGHT_TINT("Tinte Nocturno", "Filtro suave anti-fatiga visual para la noche")
}

data class ReaderSettings(
    val readingMode: ReadingMode = ReadingMode.WEBTOON_VERTICAL,
    val visualFilter: VisualFilter = VisualFilter.NORMAL,
    val volumeKeyNavigation: Boolean = true,
    val dataSaverMode: Boolean = false,
    val keepScreenOn: Boolean = true,
    val zoomSensitivity: Float = 2.5f
)
