package com.kawaii.mangareader.ui.library

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DriveFileMove
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.PrimaryScrollableTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kawaii.mangareader.domain.model.LibraryCategory
import com.kawaii.mangareader.domain.model.Manga
import com.kawaii.mangareader.ui.components.EmptyStateKawaii
import com.kawaii.mangareader.ui.components.KawaiiTopBar
import com.kawaii.mangareader.ui.components.MangaGridCard
import com.kawaii.mangareader.ui.theme.BottomSheetShape
import com.kawaii.mangareader.ui.theme.PillShape
import com.kawaii.mangareader.ui.theme.SquircleShape

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(
    viewModel: LibraryViewModel,
    onMangaClick: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var isMoveSheetOpen by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            KawaiiTopBar(
                title = if (uiState.isMultiSelectMode) "Seleccionados (${uiState.selectedMangaIds.size})" else "Mi Biblioteca Dulce 🌸",
                navigationIcon = {
                    if (uiState.isMultiSelectMode) {
                        IconButton(onClick = { viewModel.toggleMultiSelectMode() }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Cerrar selección",
                                tint = MaterialTheme.colorScheme.onBackground
                            )
                        }
                    }
                },
                actions = {
                    if (uiState.mangas.isNotEmpty()) {
                        IconButton(onClick = { viewModel.toggleMultiSelectMode() }) {
                            Icon(
                                imageVector = Icons.Default.Checklist,
                                contentDescription = "Modo selección",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Category Tabs
            PrimaryScrollableTabRow(
                selectedTabIndex = LibraryCategory.entries.indexOf(uiState.selectedCategory),
                containerColor = MaterialTheme.colorScheme.background,
                contentColor = MaterialTheme.colorScheme.primary,
                indicator = { tabPositions ->
                    val index = LibraryCategory.entries.indexOf(uiState.selectedCategory)
                    if (index < tabPositions.size) {
                        TabRowDefaults.PrimaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[index]),
                            color = MaterialTheme.colorScheme.primary,
                            shape = PillShape
                        )
                    }
                },
                edgePadding = 16.dp,
                divider = {}
            ) {
                LibraryCategory.entries.forEach { category ->
                    val isSelected = uiState.selectedCategory == category
                    Tab(
                        selected = isSelected,
                        onClick = { viewModel.selectCategory(category) },
                        text = {
                            Text(
                                text = "${category.emoji} ${category.displayName}",
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 13.sp,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    )
                }
            }

            // Batch action bar if multi-select mode is active
            if (uiState.isMultiSelectMode) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Seleccionar todos",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable { viewModel.selectAll() }
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        IconButton(
                            onClick = { isMoveSheetOpen = true },
                            enabled = uiState.selectedMangaIds.isNotEmpty()
                        ) {
                            Icon(
                                imageVector = Icons.Default.DriveFileMove,
                                contentDescription = "Mover de categoría",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }

                        IconButton(
                            onClick = { viewModel.deleteSelectedBatch() },
                            enabled = uiState.selectedMangaIds.isNotEmpty()
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Eliminar de biblioteca",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }

            // Mangas Grid
            if (uiState.mangas.isEmpty()) {
                EmptyStateKawaii(
                    emoji = uiState.selectedCategory.emoji,
                    title = "No hay mangas en ${uiState.selectedCategory.displayName}",
                    subtitle = "Explora o busca tus historias favoritas y guárdalas aquí 🌸"
                )
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 160.dp),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(uiState.mangas, key = { it.id }) { manga ->
                        val isSelected = uiState.selectedMangaIds.contains(manga.id)

                        Box(
                            modifier = Modifier
                                .clip(SquircleShape)
                                .then(
                                    if (uiState.isMultiSelectMode && isSelected) {
                                        Modifier.border(2.dp, MaterialTheme.colorScheme.primary, SquircleShape)
                                    } else Modifier
                                )
                        ) {
                            MangaGridCard(
                                manga = manga,
                                onClick = {
                                    if (uiState.isMultiSelectMode) {
                                        viewModel.toggleMangaSelection(manga.id)
                                    } else {
                                        onMangaClick(manga.id)
                                    }
                                }
                            )

                            if (uiState.isMultiSelectMode) {
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(8.dp)
                                        .clip(PillShape)
                                        .background(Color.Black.copy(alpha = 0.5f))
                                        .padding(4.dp)
                                ) {
                                    Icon(
                                        imageVector = if (isSelected) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                                        contentDescription = null,
                                        tint = if (isSelected) MaterialTheme.colorScheme.primary else Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Move Batch Bottom Sheet
    if (isMoveSheetOpen) {
        ModalBottomSheet(
            onDismissRequest = { isMoveSheetOpen = false },
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
                    text = "Mover ${uiState.selectedMangaIds.size} mangas a...",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                LibraryCategory.entries.filter { it != uiState.selectedCategory }.forEach { cat ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(SquircleShape)
                            .clickable {
                                viewModel.moveSelectedBatchTo(cat)
                                isMoveSheetOpen = false
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
                            Text(text = cat.emoji, fontSize = 20.sp)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = cat.displayName,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}
