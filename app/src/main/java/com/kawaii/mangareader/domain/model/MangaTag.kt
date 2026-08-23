package com.kawaii.mangareader.domain.model

data class MangaTag(
    val id: String,
    val name: String,
    val group: String = "genre"
) {
    companion object {
        // Tag UUIDs oficiales y precisos de MangaDex
        val BOYS_LOVE = MangaTag("5920b825-4181-4a17-beeb-9918b0ff7a30", "Boys' Love (BL)", "genre")
        val ROMANCE = MangaTag("423e2eae-a7a2-4a8b-ac03-a8351462d71d", "Romance", "genre")
        val FANTASY = MangaTag("cdc58593-87dd-415e-bbc0-2ec27bf404cc", "Fantasía", "genre")
        val WEBTOON = MangaTag("e197df38-d0e7-43b5-9b09-2842d0c326dd", "Webtoon / Manhwa", "format")
        val SLICE_OF_LIFE = MangaTag("e5301a23-ebd9-49dd-a0cb-2add944c7fe9", "Recuentos de la vida", "genre")
        val DRAMA = MangaTag("b9af3a63-f058-46de-a9a0-e0c13906197a", "Drama", "genre")
        val COMEDY = MangaTag("4d32cc48-9f00-4cca-9b5a-a839f0764984", "Comedia", "genre")
        val ACTION = MangaTag("391b0423-d847-456f-aff0-8b0cfc03066b", "Acción", "genre")
        val ISEKAI = MangaTag("ace04997-f6bd-436e-b261-779182193d3d", "Isekai", "genre")
        val GIRLS_LOVE = MangaTag("a3c67850-4684-404e-9b7f-c69850ee5da6", "Girls' Love (GL)", "genre")

        val POPULAR_GENRES = listOf(
            BOYS_LOVE,
            ROMANCE,
            FANTASY,
            WEBTOON,
            SLICE_OF_LIFE,
            DRAMA,
            COMEDY,
            ACTION,
            ISEKAI,
            GIRLS_LOVE
        )
    }
}
