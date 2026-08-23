package com.kawaii.mangareader.ui.explore

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIos
import androidx.compose.material.icons.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kawaii.mangareader.domain.model.Manga
import com.kawaii.mangareader.domain.model.MangaTag
import com.kawaii.mangareader.ui.components.DedicationBadge
import com.kawaii.mangareader.ui.components.GenrePill
import com.kawaii.mangareader.ui.components.KawaiiTopBar
import com.kawaii.mangareader.ui.components.LoadingHeart
import com.kawaii.mangareader.ui.components.MangaCoverImage
import com.kawaii.mangareader.ui.components.MangaGridCard
import com.kawaii.mangareader.ui.components.RomanticWelcomeDialog
import com.kawaii.mangareader.ui.theme.BottomSheetShape
import com.kawaii.mangareader.ui.theme.PillShape
import com.kawaii.mangareader.ui.theme.SquircleShape
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ExploreScreen(
    viewModel: ExploreViewModel,
    onMangaClick: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val appUpdateInfo by viewModel.appUpdateInfo.collectAsState()
    val dedicationMessage by viewModel.dedicationMessage.collectAsState()
    val isFirstLaunch by viewModel.isFirstLaunch.collectAsState()
    val allowBlYaoi by viewModel.allowBlYaoi.collectAsState()
    val gridState = rememberLazyGridState()

    // Listen for random manga pick event
    LaunchedEffect(Unit) {
        viewModel.randomMangaEvent.collectLatest { randomMangaId ->
            onMangaClick(randomMangaId)
        }
    }

    // Scroll to top when page changes
    LaunchedEffect(uiState.currentPage) {
        gridState.animateScrollToItem(0)
    }

    val infiniteTransition = rememberInfiniteTransition(label = "dice_wobble")
    val diceRotation by infiniteTransition.animateFloat(
        initialValue = -12f,
        targetValue = 12f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dice"
    )

    Scaffold(
        topBar = {
            KawaiiTopBar(
                title = "MangaReader Kawaii 🌸",
                actions = {
                    IconButton(
                        onClick = { viewModel.pickRandomManga { randomMangaId -> onMangaClick(randomMangaId) } }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Casino,
                            contentDescription = "Sorpréndeme",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .size(26.dp)
                                .rotate(if (uiState.isPickingRandom) diceRotation else 0f)
                        )
                    }
                }
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        if (uiState.isLoading && uiState.popularMangas.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                LoadingHeart(message = "Buscando historias hermosas...")
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 160.dp),
                state = gridState,
                contentPadding = PaddingValues(
                    start = 16.dp,
                    end = 16.dp,
                    top = paddingValues.calculateTopPadding() + 8.dp,
                    bottom = paddingValues.calculateBottomPadding() + 24.dp
                ),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                // 1. Dedication Badge
                item(span = { GridItemSpan(maxLineSpan) }) {
                    DedicationBadge(message = dedicationMessage)
                }

                // 2. Featured Carousel (Hero)
                if (uiState.featuredMangas.isNotEmpty() && uiState.selectedGenre == null && uiState.currentPage == 1) {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        Column {
                            Text(
                                text = "✨ Destacados para Ti",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            FeaturedMangaCarousel(
                                featuredMangas = uiState.featuredMangas,
                                onMangaClick = onMangaClick
                            )
                        }
                    }
                }

                // 3. Publication Status Filter
                item(span = { GridItemSpan(maxLineSpan) }) {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(vertical = 2.dp)
                    ) {
                        item {
                            GenrePill(
                                text = "🌸 Todos",
                                isSelected = uiState.selectedStatus == null,
                                onClick = { viewModel.selectStatus(null) }
                            )
                        }
                        item {
                            GenrePill(
                                text = "🔥 En Emisión",
                                isSelected = uiState.selectedStatus == "ongoing",
                                onClick = { viewModel.selectStatus("ongoing") }
                            )
                        }
                        item {
                            GenrePill(
                                text = "✨ Finalizados",
                                isSelected = uiState.selectedStatus == "completed",
                                onClick = { viewModel.selectStatus("completed") }
                            )
                        }
                    }
                }

                // 4. Genre Pills (Filter + Ver Todos + BL/Yaoi)
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "🌸 Géneros y Categorías",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )

                            Text(
                                text = "Ver todos (+40) 🔍",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.clickable { viewModel.openAllGenresSheet() }
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            contentPadding = PaddingValues(vertical = 4.dp)
                        ) {
                            item {
                                GenrePill(
                                    text = "✨ Ver todos (+40)",
                                    isSelected = uiState.isAllGenresSheetOpen,
                                    onClick = { viewModel.openAllGenresSheet() }
                                )
                            }
                            if (allowBlYaoi) {
                                item {
                                    GenrePill(
                                        text = "💜 BL / Yaoi",
                                        isSelected = uiState.selectedGenre?.id == MangaTag.BOYS_LOVE.id,
                                        onClick = { viewModel.selectGenre(MangaTag.BOYS_LOVE) }
                                    )
                                }
                            }
                            items(uiState.availableGenres) { genre ->
                                if (genre.id != MangaTag.BOYS_LOVE.id) {
                                    GenrePill(
                                        text = genre.name,
                                        isSelected = uiState.selectedGenre?.id == genre.id,
                                        onClick = { viewModel.selectGenre(genre) }
                                    )
                                }
                            }
                        }
                    }
                }

                // 5. Section Title
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (uiState.selectedGenre != null) "🌸 ${uiState.selectedGenre?.name} (Pág. ${uiState.currentPage})" else "Recomendados en Español (Pág. ${uiState.currentPage})",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )

                        if (uiState.selectedGenre != null) {
                            Text(
                                text = "Limpiar filtro ✕",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.clickable { viewModel.selectGenre(null) }
                            )
                        }
                    }
                }

                // 6. Popular / Filtered Manga Cards
                items(uiState.popularMangas, key = { it.id }) { manga ->
                    MangaGridCard(
                        manga = manga,
                        onClick = { onMangaClick(manga.id) }
                    )
                }

                // 7. Pagination Controls
                if (uiState.popularMangas.isNotEmpty()) {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedButton(
                                onClick = { viewModel.previousPage() },
                                enabled = uiState.currentPage > 1 && !uiState.isLoading,
                                shape = PillShape
                            ) {
                                Icon(Icons.Default.ArrowBackIos, contentDescription = null, modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Anterior", fontSize = 12.sp)
                            }

                            Text(
                                text = "Página ${uiState.currentPage}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )

                            Button(
                                onClick = { viewModel.nextPage() },
                                enabled = uiState.hasMorePages && !uiState.isLoading,
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                shape = PillShape
                            ) {
                                Text("Siguiente", color = Color.White, fontSize = 12.sp)
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(Icons.Default.ArrowForwardIos, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
                            }
                        }
                    }
                }

                // Error / retry item
                if (uiState.errorMessage != null) {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = uiState.errorMessage ?: "",
                                color = MaterialTheme.colorScheme.error,
                                fontSize = 13.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = { viewModel.loadInitialData() },
                                shape = PillShape,
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                            ) {
                                Text("Reintentar 🌸", color = Color.White)
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal Bottom Sheet for All 40+ Genres
    if (uiState.isAllGenresSheetOpen) {
        ModalBottomSheet(
            onDismissRequest = { viewModel.closeAllGenresSheet() },
            sheetState = rememberModalBottomSheetState(),
            shape = BottomSheetShape,
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "✨ Todos los Géneros (+40)",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Toca cualquier género para explorar su catálogo completo:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(4.dp))

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    GenrePill(
                        text = "💜 BL / Yaoi",
                        isSelected = uiState.selectedGenre?.id == MangaTag.BOYS_LOVE.id,
                        onClick = { viewModel.selectGenre(MangaTag.BOYS_LOVE) }
                    )

                    uiState.allTags.forEach { tag ->
                        if (tag.id != MangaTag.BOYS_LOVE.id) {
                            GenrePill(
                                text = tag.name,
                                isSelected = uiState.selectedGenre?.id == tag.id,
                                onClick = { viewModel.selectGenre(tag) }
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    if (isFirstLaunch) {
        RomanticWelcomeDialog(
            onDismiss = { viewModel.dismissFirstLaunchDialog() }
        )
    }

    if (appUpdateInfo != null && appUpdateInfo!!.hasUpdate) {
        com.kawaii.mangareader.ui.components.AppUpdateDialog(
            updateInfo = appUpdateInfo!!,
            onDismiss = { viewModel.dismissUpdateDialog() }
        )
    }
}

@Composable
fun FeaturedMangaCarousel(
    featuredMangas: List<Manga>,
    onMangaClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(horizontal = 2.dp)
    ) {
        items(featuredMangas, key = { it.id }) { manga ->
            Card(
                modifier = Modifier
                    .width(260.dp)
                    .height(160.dp)
                    .clip(SquircleShape)
                    .clickable { onMangaClick(manga.id) },
                shape = SquircleShape,
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    MangaCoverImage(
                        imageUrl = manga.coverUrl,
                        contentDescription = manga.title,
                        modifier = Modifier.fillMaxSize()
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.85f)),
                                    startY = 50f
                                )
                            )
                    )

                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(12.dp)
                    ) {
                        Text(
                            text = manga.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = manga.tags.take(2).joinToString(" • ") { it.name },
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.8f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}
