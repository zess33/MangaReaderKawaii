package com.kawaii.mangareader.ui.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.DownloadDone
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kawaii.mangareader.domain.model.Chapter
import com.kawaii.mangareader.domain.model.LibraryCategory
import com.kawaii.mangareader.ui.components.GenrePill
import com.kawaii.mangareader.ui.components.KawaiiTopBar
import com.kawaii.mangareader.ui.components.LoadingHeart
import com.kawaii.mangareader.ui.components.MangaCoverImage
import com.kawaii.mangareader.ui.theme.BottomSheetShape
import com.kawaii.mangareader.ui.theme.PillShape
import com.kawaii.mangareader.ui.theme.SquircleShape

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun MangaDetailScreen(
    viewModel: MangaDetailViewModel,
    onBackClick: () -> Unit,
    onChapterClick: (String, String) -> Unit, // mangaId, chapterId
    onWebReaderClick: ((String, String) -> Unit)? = null
) {
    val uiState by viewModel.uiState.collectAsState()
    var isSynopsisExpanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            KawaiiTopBar(
                title = uiState.manga?.title ?: "Detalles",
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Volver",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.openCategorySheet() }) {
                        Icon(
                            imageVector = if (uiState.isInLibrary) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                            contentDescription = "Guardar en biblioteca",
                            tint = if (uiState.isInLibrary) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground
                        )
                    }
                }
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        if (uiState.isLoading && uiState.manga == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                LoadingHeart(message = "Cargando historia mágica...")
            }
        } else if (uiState.manga != null) {
            val manga = uiState.manga!!

            LazyColumn(
                contentPadding = PaddingValues(
                    top = paddingValues.calculateTopPadding() + 8.dp,
                    bottom = paddingValues.calculateBottomPadding() + 24.dp,
                    start = 16.dp,
                    end = 16.dp
                ),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                // 1. Header Information
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        MangaCoverImage(
                            imageUrl = manga.coverUrl,
                            contentDescription = manga.title,
                            modifier = Modifier
                                .width(130.dp)
                                .clip(SquircleShape)
                        )

                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = manga.title,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )

                            if (manga.author.isNotBlank()) {
                                Text(
                                    text = "✍️ Autor: ${manga.author}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .clip(PillShape)
                                        .background(MaterialTheme.colorScheme.primaryContainer)
                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                                ) {
                                    Text(
                                        text = manga.originBadge,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .clip(PillShape)
                                        .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f))
                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                                ) {
                                    Text(
                                        text = "Estado: ${manga.status}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onBackground
                                    )
                                }
                            }

                            if (uiState.isInLibrary && uiState.currentCategory != null) {
                                Text(
                                    text = "${uiState.currentCategory?.emoji} En ${uiState.currentCategory?.displayName}",
                                    color = MaterialTheme.colorScheme.primary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                // 2. Action Buttons (Read / Add to Library / Web Reader)
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            val hasChapters = uiState.allChapters.isNotEmpty()
                            val lastReadNum = manga.lastReadChapterNum
                            val lastReadId = manga.lastReadChapterId

                            Button(
                                onClick = {
                                    if (hasChapters) {
                                        val targetChapter = if (lastReadId != null) {
                                            uiState.allChapters.find { it.id == lastReadId }
                                        } else if (lastReadNum != null) {
                                            uiState.allChapters.find { it.chapterNumber == lastReadNum }
                                        } else null

                                        val chapterToOpen = targetChapter
                                            ?: uiState.allChapters.minByOrNull { it.normalizedNumber }
                                            ?: uiState.allChapters.firstOrNull()

                                        if (chapterToOpen != null) {
                                            onChapterClick(manga.id, chapterToOpen.id)
                                        }
                                    } else {
                                        val encodedTitle = java.net.URLEncoder.encode(manga.title, "UTF-8")
                                        onWebReaderClick?.invoke(manga.title, "https://visortmo.com/search?query=$encodedTitle")
                                    }
                                },
                                shape = PillShape,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary
                                ),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp),
                                    tint = Color.White
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (hasChapters) {
                                        if (lastReadNum != null) "Reanudar Cap. $lastReadNum" else "Empezar a Leer"
                                    } else {
                                        "Leer en Visor Espejo 🌸"
                                    },
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }

                            FilledTonalButton(
                                onClick = { viewModel.openCategorySheet() },
                                shape = PillShape
                            ) {
                                Icon(
                                    imageVector = if (uiState.isInLibrary) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }

                // 3. Tags Chips
                if (manga.tags.isNotEmpty()) {
                    item {
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            manga.tags.forEach { tag ->
                                Box(
                                    modifier = Modifier
                                        .clip(PillShape)
                                        .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
                                        .padding(horizontal = 10.dp, vertical = 5.dp)
                                ) {
                                    Text(
                                        text = tag.name,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                            }
                        }
                    }
                }

                // 4. Synopsis
                if (manga.description.isNotBlank()) {
                    item {
                        Card(
                            shape = SquircleShape,
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp)
                            ) {
                                Text(
                                    text = "Sinopsis",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = manga.description,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = if (isSynopsisExpanded) Int.MAX_VALUE else 3,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = if (isSynopsisExpanded) "Mostrar menos ⬆️" else "Leer más ⬇️",
                                    color = MaterialTheme.colorScheme.primary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.clickable { isSynopsisExpanded = !isSynopsisExpanded }
                                )
                            }
                        }
                    }
                }

                // 5. Scanlation Groups Selector Pills
                if (uiState.availableScans.isNotEmpty()) {
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "🔀 Servidores y Grupos de Traducción",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                contentPadding = PaddingValues(vertical = 4.dp)
                            ) {
                                item {
                                    GenrePill(
                                        text = "✨ Todos (Desduplicado)",
                                        isSelected = uiState.selectedScan == null,
                                        onClick = { viewModel.selectScan(null) }
                                    )
                                }
                                items(uiState.availableScans) { scan ->
                                    GenrePill(
                                        text = scan,
                                        isSelected = uiState.selectedScan == scan,
                                        onClick = { viewModel.selectScan(scan) }
                                    )
                                }
                            }
                        }
                    }
                }

                // 6. Chapters Header & Search Mirror Button
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "📖 Capítulos (${uiState.filteredChapters.size})",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Filtro Orden Ascendente / Descendente
                                Box(
                                    modifier = Modifier
                                        .clip(PillShape)
                                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                                        .clickable { viewModel.toggleSortOrder() }
                                        .padding(horizontal = 10.dp, vertical = 5.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (uiState.isSortDescending) Icons.Default.ArrowDownward else Icons.Default.ArrowUpward,
                                            contentDescription = "Ordenar capítulos",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Text(
                                            text = if (uiState.isSortDescending) "Más recientes" else "Primeros caps",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.primary,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }

                                if (uiState.filteredChapters.isNotEmpty()) {
                                    Text(
                                        text = "Descargar todo ⬇️",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.clickable { viewModel.downloadAllChapters() }
                                    )
                                }
                            }
                        }

                        // Search More Chapters on Mirror Servers Button
                        OutlinedButton(
                            onClick = { viewModel.searchMirrorChapters() },
                            enabled = !uiState.isSearchingMirrors,
                            shape = PillShape,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            if (uiState.isSearchingMirrors) {
                                Text(
                                    text = "Buscando en servidores espejo... ⏳",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            } else {
                                Text(
                                    text = "⚡ Buscar más capítulos en servidores espejo",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }

                // Range Selector Pills (e.g. 1 - 25, 26 - 50)
                if (uiState.chapterRanges.isNotEmpty()) {
                    item {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            contentPadding = PaddingValues(vertical = 4.dp)
                        ) {
                            item {
                                GenrePill(
                                    text = "✨ Todos (${uiState.filteredChapters.size})",
                                    isSelected = uiState.selectedRangeIndex == null,
                                    onClick = { viewModel.selectRange(null) }
                                )
                            }
                            itemsIndexed(uiState.chapterRanges) { idx, range ->
                                val (start, end) = range
                                GenrePill(
                                    text = "Cap. $start - $end",
                                    isSelected = uiState.selectedRangeIndex == idx,
                                    onClick = { viewModel.selectRange(idx) }
                                )
                            }
                        }
                    }
                }

                // 7. Chapter items or Empty State with mirror actions
                if (uiState.filteredChapters.isEmpty()) {
                    item {
                        Card(
                            shape = SquircleShape,
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Text(
                                    text = "🌸 Leer en Visor Integrado",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Este título está disponible en los servidores espejo en español. Toca cualquiera para leer directamente dentro de la app:",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(4.dp))

                                val encodedTitle = java.net.URLEncoder.encode(manga.title, "UTF-8")

                                Button(
                                    onClick = {
                                        onWebReaderClick?.invoke(manga.title, "https://visortmo.com/search?query=$encodedTitle")
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                    shape = PillShape,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("📖 Leer en TuMangaOnline (TMO) 🌸", color = Color.White, fontWeight = FontWeight.Bold)
                                }

                                OutlinedButton(
                                    onClick = {
                                        onWebReaderClick?.invoke(manga.title, "https://lectormanga.com/search?query=$encodedTitle")
                                    },
                                    shape = PillShape,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("📖 Leer en Lectormanga 🌸", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                                }

                                OutlinedButton(
                                    onClick = {
                                        onWebReaderClick?.invoke(manga.title, "https://inmanga.com/search/result?query=$encodedTitle")
                                    },
                                    shape = PillShape,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("📖 Leer en InManga 🌸", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                } else {
                    items(uiState.displayedChapters, key = { it.id }) { chapter ->
                        val isLastRead = chapter.id == manga.lastReadChapterId ||
                                (manga.lastReadChapterNum != null && chapter.chapterNumber == manga.lastReadChapterNum)
                        val pageToDisplay = if (isLastRead && manga.lastReadPage > 0) manga.lastReadPage else chapter.lastReadPage
                        ChapterItemRow(
                            chapter = chapter,
                            isLastRead = isLastRead,
                            lastReadPage = pageToDisplay,
                            onClick = { onChapterClick(manga.id, chapter.id) },
                            onDownloadClick = { viewModel.downloadChapter(chapter) }
                        )
                    }
                }
            }
        }
    }

    // Modal Bottom Sheet for Library Category Selection
    if (uiState.isCategorySheetOpen) {
        ModalBottomSheet(
            onDismissRequest = { viewModel.closeCategorySheet() },
            sheetState = rememberModalBottomSheetState(),
            shape = BottomSheetShape,
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "🌸 Guardar en Biblioteca",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Elige una pestaña tierna para organizar este manga:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(6.dp))

                LibraryCategory.entries.forEach { category ->
                    val isSelected = uiState.currentCategory == category
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(SquircleShape)
                            .clickable { viewModel.setLibraryCategory(category) },
                        shape = SquircleShape,
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = category.emoji, fontSize = 22.sp)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = category.displayName,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.weight(1f)
                            )
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Seleccionado",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }

                if (uiState.isInLibrary) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Button(
                        onClick = { viewModel.removeFromLibrary() },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                        shape = PillShape,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Eliminar de la biblioteca 🗑️",
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun ChapterItemRow(
    chapter: Chapter,
    isLastRead: Boolean = false,
    lastReadPage: Int = 0,
    onClick: () -> Unit,
    onDownloadClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(SquircleShape)
            .clickable(onClick = onClick),
        shape = SquircleShape,
        colors = CardDefaults.cardColors(
            containerColor = when {
                isLastRead -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                chapter.isRead -> MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)
                else -> MaterialTheme.colorScheme.surface
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isLastRead) 2.dp else 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${chapter.languageFlag} ${chapter.displayTitle}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = if (isLastRead) FontWeight.ExtraBold else if (chapter.isRead) FontWeight.Normal else FontWeight.Bold,
                        color = if (isLastRead) MaterialTheme.colorScheme.primary else if (chapter.isRead) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(3.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (isLastRead) {
                        Surface(
                            shape = PillShape,
                            color = MaterialTheme.colorScheme.primary
                        ) {
                            Text(
                                text = if (lastReadPage > 0) "📍 Leyendo • Pág. ${lastReadPage + 1}" else "📍 Leyendo",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }

                    if (!chapter.scanlationGroup.isNullOrBlank()) {
                        Text(
                            text = chapter.scanlationGroup ?: "",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            maxLines = 1
                        )
                    }
                    if (chapter.isDownloaded) {
                        Text(
                            text = "• Descargado 💾",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.tertiary
                        )
                    }
                }
            }

            IconButton(onClick = onDownloadClick) {
                Icon(
                    imageVector = if (chapter.isDownloaded) Icons.Default.DownloadDone else Icons.Default.Download,
                    contentDescription = "Descargar offline",
                    tint = if (chapter.isDownloaded) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
