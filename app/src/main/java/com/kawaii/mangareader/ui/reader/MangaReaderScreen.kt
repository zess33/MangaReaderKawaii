package com.kawaii.mangareader.ui.reader

import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import com.kawaii.mangareader.domain.model.ReadingMode
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBackIos
import androidx.compose.material.icons.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.BrightnessMedium
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.foundation.lazy.items
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
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.kawaii.mangareader.domain.model.Page
import com.kawaii.mangareader.domain.model.VisualFilter
import com.kawaii.mangareader.ui.components.LoadingHeart
import com.kawaii.mangareader.ui.theme.BottomSheetShape
import com.kawaii.mangareader.ui.theme.NightTintOverlayColor
import com.kawaii.mangareader.ui.theme.PillShape
import com.kawaii.mangareader.ui.theme.SepiaOverlayColor
import com.kawaii.mangareader.ui.theme.SquircleShape
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MangaReaderScreen(
    viewModel: MangaReaderViewModel,
    onBackClick: () -> Unit,
    onMangaInfoClick: ((String) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val readerSettings by viewModel.readerSettings.collectAsState()
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    var isFilterSheetOpen by remember { mutableStateOf(false) }

    // Zoom & Pan state
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    // Auto-Scroll Loop
    LaunchedEffect(uiState.isAutoScrolling, uiState.autoScrollSpeed) {
        if (uiState.isAutoScrolling) {
            val stepPx = when (uiState.autoScrollSpeed) {
                1f -> 3f
                2f -> 6f
                3f -> 12f
                else -> 6f
            }
            while (isActive && uiState.isAutoScrolling) {
                listState.scrollBy(stepPx)
                delay(16) // ~60fps
            }
        }
    }

    // Adjust Activity brightness
    LaunchedEffect(uiState.brightnessPercent) {
        (context as? Activity)?.let { activity ->
            val layoutParams = activity.window.attributes
            layoutParams.screenBrightness = uiState.brightnessPercent
            activity.window.attributes = layoutParams
        }
    }

    // Auto hide brightness HUD after 1.5s
    LaunchedEffect(uiState.isBrightnessHudVisible) {
        if (uiState.isBrightnessHudVisible) {
            delay(1500)
            viewModel.hideBrightnessHud()
        }
    }

    // Track scroll position to update current page
    LaunchedEffect(listState) {
        snapshotFlow { listState.firstVisibleItemIndex }
            .collect { index ->
                viewModel.onPageChanged(index)
            }
    }

    // Scroll to initial page
    LaunchedEffect(uiState.pages) {
        if (uiState.currentPageIndex > 0 && uiState.currentPageIndex < uiState.pages.size) {
            listState.scrollToItem(uiState.currentPageIndex)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                LoadingHeart(message = "Abriendo páginas...")
            }
        } else if (uiState.errorMessage != null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = uiState.errorMessage ?: "",
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { viewModel.loadChapter(uiState.chapterId) },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    shape = PillShape
                ) {
                    Text("Reintentar")
                }
            }
        } else {
            // Webtoon Vertical Canvas with Multi-Touch Zoom & Left Edge Brightness Gesture
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onTap = {
                                if (!uiState.isLocked) {
                                    viewModel.toggleHud()
                                }
                            },
                            onDoubleTap = {
                                if (!uiState.isLocked) {
                                    if (scale > 1.2f) {
                                        scale = 1f
                                        offset = Offset.Zero
                                    } else {
                                        scale = 2.5f
                                    }
                                }
                            }
                        )
                    }
                    .pointerInput(Unit) {
                        detectTransformGestures { _, pan, zoom, _ ->
                            if (!uiState.isLocked) {
                                scale = (scale * zoom).coerceIn(1f, 4f)
                                if (scale > 1f) {
                                    val maxX = (scale - 1) * 500
                                    val maxY = (scale - 1) * 800
                                    offset = Offset(
                                        x = (offset.x + pan.x).coerceIn(-maxX, maxX),
                                        y = (offset.y + pan.y).coerceIn(-maxY, maxY)
                                    )
                                } else {
                                    offset = Offset.Zero
                                }
                            }
                        }
                    }
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                        translationX = offset.x
                        translationY = offset.y
                    }
            ) {
                if (readerSettings.readingMode == ReadingMode.WEBTOON_VERTICAL) {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        itemsIndexed(uiState.pages, key = { index, _ -> "${uiState.chapterId}_$index" }) { index, page ->
                            WebtoonPageItem(
                                page = page,
                                pageIndex = index,
                                onTap = {
                                    if (!uiState.isLocked) {
                                        viewModel.toggleHud()
                                    }
                                },
                                onLongPress = {
                                    if (!uiState.isLocked) {
                                        viewModel.selectPageForAction(page)
                                    }
                                }
                            )
                        }
                    }
                } else {
                    val pagerState = rememberPagerState(pageCount = { uiState.pages.size })
                    LaunchedEffect(pagerState.currentPage) {
                        viewModel.onPageChanged(pagerState.currentPage)
                    }
                    HorizontalPager(
                        state = pagerState,
                        reverseLayout = readerSettings.readingMode == ReadingMode.PAGED_RTL,
                        modifier = Modifier.fillMaxSize()
                    ) { pageIndex ->
                        val page = uiState.pages.getOrNull(pageIndex)
                        if (page != null) {
                            WebtoonPageItem(
                                page = page,
                                pageIndex = pageIndex,
                                onTap = {
                                    if (!uiState.isLocked) {
                                        viewModel.toggleHud()
                                    }
                                },
                                onLongPress = {
                                    if (!uiState.isLocked) {
                                        viewModel.selectPageForAction(page)
                                    }
                                }
                            )
                        }
                    }
                }

                // Visual Comfort Filter Overlay
                when (uiState.currentFilter) {
                    VisualFilter.SEPIA_WARM -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(SepiaOverlayColor)
                        )
                    }
                    VisualFilter.NIGHT_TINT -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(NightTintOverlayColor)
                        )
                    }
                    VisualFilter.NORMAL -> {}
                }
            }

            // Left-Edge Brightness Gesture Strip (only active when not locked)
            if (!uiState.isLocked) {
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .width(70.dp)
                        .fillMaxSize()
                        .pointerInput(Unit) {
                            detectDragGestures { change, dragAmount ->
                                change.consume()
                                val delta = -dragAmount.y / 1000f
                                val newBrightness = (uiState.brightnessPercent + delta).coerceIn(0.05f, 1f)
                                viewModel.setBrightness(newBrightness, showHud = true)
                            }
                        }
                )
            }

            // Floating Brightness HUD Indicator
            AnimatedVisibility(
                visible = uiState.isBrightnessHudVisible,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier.align(Alignment.Center)
            ) {
                Box(
                    modifier = Modifier
                        .clip(SquircleShape)
                        .background(Color.Black.copy(alpha = 0.8f))
                        .padding(horizontal = 20.dp, vertical = 12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.BrightnessMedium,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = "Brillo: ${(uiState.brightnessPercent * 100).toInt()}%",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                }
            }

            // Top HUD Bar
            AnimatedVisibility(
                visible = uiState.isHudVisible && !uiState.isLocked,
                enter = fadeIn() + slideInVertically { -it },
                exit = fadeOut() + slideOutVertically { -it },
                modifier = Modifier.align(Alignment.TopCenter)
            ) {
                TopAppBar(
                    title = {
                        Column(
                            modifier = Modifier.clickable {
                                onMangaInfoClick?.invoke(uiState.mangaId)
                            }
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = uiState.manga?.title ?: "",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Color.White,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = "Detalles",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(15.dp)
                                )
                            }
                            Text(
                                text = uiState.currentChapter?.displayTitle ?: "",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.75f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Salir",
                                tint = Color.White
                            )
                        }
                    },
                    actions = {
                        // Botón Favorito / Seguir
                        IconButton(onClick = { viewModel.toggleFavorite() }) {
                            Icon(
                                imageVector = if (uiState.isInLibrary) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                contentDescription = if (uiState.isInLibrary) "En Favoritos" else "Seguir",
                                tint = if (uiState.isInLibrary) MaterialTheme.colorScheme.primary else Color.White
                            )
                        }
                        // Botón Ver Lista de Capítulos
                        IconButton(onClick = { viewModel.openChaptersSheet() }) {
                            Icon(
                                imageVector = Icons.Default.FormatListNumbered,
                                contentDescription = "Ver todos los capítulos",
                                tint = Color.White
                            )
                        }
                        IconButton(onClick = { viewModel.toggleLock() }) {
                            Icon(
                                imageVector = if (uiState.isLocked) Icons.Default.Lock else Icons.Default.LockOpen,
                                contentDescription = "Bloquear toques",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        IconButton(onClick = { isFilterSheetOpen = true }) {
                            Icon(
                                imageVector = Icons.Default.ColorLens,
                                contentDescription = "Filtro visual",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Black.copy(alpha = 0.88f)
                    )
                )
            }

            // Floating Candado / Unlock Button (Visible when Screen is Locked)
            AnimatedVisibility(
                visible = uiState.isLocked,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 16.dp, bottom = 24.dp)
            ) {
                Card(
                    shape = PillShape,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                    modifier = Modifier
                        .clip(PillShape)
                        .clickable { viewModel.toggleLock() }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Desbloquear toques",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Bloqueado (Toca para desbloquear)",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            // Floating Auto-Scroll Control Capsule (Center Right - visible when not locked)
            if (!uiState.isLocked) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(bottom = if (uiState.isHudVisible) 140.dp else 24.dp, end = 16.dp)
                ) {
                    Card(
                        shape = PillShape,
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            IconButton(
                                onClick = { viewModel.toggleAutoScroll() },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = if (uiState.isAutoScrolling) Icons.Default.Pause else Icons.Default.PlayArrow,
                                    contentDescription = "Auto-scroll",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }

                            if (uiState.isAutoScrolling) {
                                listOf(1f to "1x", 2f to "2x", 3f to "3x").forEach { (speed, label) ->
                                    val isSelected = uiState.autoScrollSpeed == speed
                                    Box(
                                        modifier = Modifier
                                            .clip(CircleShape)
                                            .background(if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent)
                                            .clickable { viewModel.setAutoScrollSpeed(speed) }
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = label,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Bottom HUD Bar
            AnimatedVisibility(
                visible = uiState.isHudVisible && !uiState.isLocked,
                enter = fadeIn() + slideInVertically { it },
                exit = fadeOut() + slideOutVertically { it },
                modifier = Modifier.align(Alignment.BottomCenter)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.Black.copy(alpha = 0.85f))
                        .padding(horizontal = 20.dp, vertical = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Página ${uiState.currentPageIndex + 1} de ${uiState.pages.size}",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )

                        Text(
                            text = "Filtro: ${uiState.currentFilter.title}",
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.clickable { isFilterSheetOpen = true }
                        )
                    }

                    if (uiState.pages.size > 1) {
                        Slider(
                            value = uiState.currentPageIndex.toFloat(),
                            onValueChange = { targetPage ->
                                val page = targetPage.toInt()
                                scope.launch {
                                    listState.scrollToItem(page)
                                }
                            },
                            valueRange = 0f..(uiState.pages.size - 1).toFloat(),
                            colors = SliderDefaults.colors(
                                thumbColor = MaterialTheme.colorScheme.primary,
                                activeTrackColor = MaterialTheme.colorScheme.primary,
                                inactiveTrackColor = Color.White.copy(alpha = 0.3f)
                            )
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = { viewModel.previousChapter() },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.15f)),
                            shape = PillShape
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowBackIos,
                                contentDescription = "Capítulo anterior",
                                tint = Color.White,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Anterior", color = Color.White, fontSize = 12.sp)
                        }

                        Button(
                            onClick = { viewModel.nextChapter() },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            shape = PillShape
                        ) {
                            Text("Siguiente", color = Color.White, fontSize = 12.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.ArrowForwardIos,
                                contentDescription = "Siguiente capítulo",
                                tint = Color.White,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    // Modal Bottom Sheet for Page Actions (Save & Share Viñeta)
    if (uiState.selectedPageForAction != null) {
        val page = uiState.selectedPageForAction!!
        ModalBottomSheet(
            onDismissRequest = { viewModel.selectPageForAction(null) },
            sheetState = rememberModalBottomSheetState(),
            shape = BottomSheetShape,
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "Viñeta - Página ${page.index + 1}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                // Save to gallery
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(SquircleShape)
                        .clickable {
                            scope.launch {
                                saveImageToGallery(context, page.displayUrl)
                                viewModel.selectPageForAction(null)
                            }
                        },
                    shape = SquircleShape,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Default.Download, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Guardar en la Galería de Fotos", fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                    }
                }

                // Share image
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(SquircleShape)
                        .clickable {
                            scope.launch {
                                shareImage(context, page.displayUrl)
                                viewModel.selectPageForAction(null)
                            }
                        },
                    shape = SquircleShape,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Default.Share, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Compartir Viñeta", fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    // Modal Bottom Sheet for Visual Comfort Filters
    if (isFilterSheetOpen) {
        ModalBottomSheet(
            onDismissRequest = { isFilterSheetOpen = false },
            sheetState = rememberModalBottomSheetState(),
            shape = BottomSheetShape,
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "Ajustes de Lectura",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = "📖 Modo de Lectura:",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                ReadingMode.entries.forEach { mode ->
                    val isModeSelected = readerSettings.readingMode == mode
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(SquircleShape)
                            .clickable {
                                viewModel.setReadingMode(mode)
                            },
                        shape = SquircleShape,
                        colors = CardDefaults.cardColors(
                            containerColor = if (isModeSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = mode.title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.weight(1f)
                            )
                            if (isModeSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Seleccionado",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "🎨 Filtros de Confort Visual:",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = "Ajusta el tinte de pantalla para proteger tu vista durante la lectura:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(4.dp))

                VisualFilter.entries.forEach { filter ->
                    val isSelected = uiState.currentFilter == filter
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(SquircleShape)
                            .clickable {
                                viewModel.setVisualFilter(filter)
                                isFilterSheetOpen = false
                            },
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
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = filter.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = filter.description,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Seleccionado",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    // Modal Bottom Sheet for All Chapters & Manga Info Access
    if (uiState.isChaptersSheetOpen) {
        ModalBottomSheet(
            onDismissRequest = { viewModel.closeChaptersSheet() },
            sheetState = rememberModalBottomSheetState(),
            shape = BottomSheetShape,
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Capítulos (${uiState.allChapters.size})",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = uiState.manga?.title ?: "",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    // Botón para ir a la Ficha Técnica
                    OutlinedButton(
                        onClick = {
                            viewModel.closeChaptersSheet()
                            onMangaInfoClick?.invoke(uiState.mangaId)
                        },
                        shape = PillShape
                    ) {
                        Icon(Icons.Default.Info, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Ver detalles", fontSize = 12.sp)
                    }
                }

                // Botón rápido de favoritos / seguir
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(SquircleShape)
                        .background(if (uiState.isInLibrary) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .clickable { viewModel.toggleFavorite() }
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (uiState.isInLibrary) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                            contentDescription = null,
                            tint = if (uiState.isInLibrary) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = if (uiState.isInLibrary) "En tu biblioteca" else "Agregar a biblioteca",
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 13.sp
                        )
                    }
                    Text(
                        text = if (uiState.isInLibrary) "Siguiendo" else "Seguir",
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))

                // Lista de todos los capítulos
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(360.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(uiState.allChapters, key = { it.id }) { chapter ->
                        val isCurrent = chapter.id == uiState.chapterId
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(SquircleShape)
                                .clickable { viewModel.jumpToChapter(chapter.id) },
                            shape = SquircleShape,
                            colors = CardDefaults.cardColors(
                                containerColor = if (isCurrent) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                    if (isCurrent) {
                                        Icon(
                                            imageVector = Icons.Default.PlayArrow,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                    }
                                    Column {
                                        Text(
                                            text = chapter.displayTitle,
                                            style = MaterialTheme.typography.bodyLarge,
                                            fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isCurrent) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                        )
                                        val scanGroup = chapter.scanlationGroup
                                        if (!scanGroup.isNullOrBlank()) {
                                            Text(
                                                text = scanGroup,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }

                                if (isCurrent) {
                                    Box(
                                        modifier = Modifier
                                            .clip(PillShape)
                                            .background(MaterialTheme.colorScheme.primary)
                                            .padding(horizontal = 8.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "Leyendo",
                                            color = Color.White,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                } else if (chapter.isRead) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Leído",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun WebtoonPageItem(
    page: Page,
    pageIndex: Int,
    onTap: () -> Unit,
    onLongPress: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.Black)
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { onTap() },
                    onLongPress = { onLongPress() }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(page.displayUrl)
                .crossfade(true)
                .build(),
            contentDescription = "Página ${pageIndex + 1}",
            contentScale = ContentScale.FillWidth,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

suspend fun saveImageToGallery(context: Context, urlOrPath: String) {
    try {
        val loader = ImageLoader(context)
        val req = ImageRequest.Builder(context).data(urlOrPath).build()
        val result = (loader.execute(req) as? SuccessResult)?.drawable
        val bitmap = (result as? BitmapDrawable)?.bitmap ?: return

        val filename = "MangaKawaii_${System.currentTimeMillis()}.jpg"
        var fos: OutputStream? = null

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/MangaReaderKawaii")
            }
            val uri = context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
            if (uri != null) {
                fos = context.contentResolver.openOutputStream(uri)
            }
        } else {
            val imagesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString()
            val image = File(imagesDir, filename)
            fos = FileOutputStream(image)
        }

        fos?.use {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it)
            Toast.makeText(context, "Viñeta guardada en la Galería", Toast.LENGTH_SHORT).show()
        }
    } catch (e: Exception) {
        Toast.makeText(context, "Error al guardar viñeta", Toast.LENGTH_SHORT).show()
    }
}

suspend fun shareImage(context: Context, urlOrPath: String) {
    try {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, "¡Mira esta viñeta de manga! $urlOrPath")
        }
        context.startActivity(Intent.createChooser(intent, "Compartir viñeta"))
    } catch (e: Exception) {
        Toast.makeText(context, "Error al compartir", Toast.LENGTH_SHORT).show()
    }
}
