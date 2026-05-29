package com.example.ui

import android.text.format.DateUtils
import androidx.compose.animation.*
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.Category
import com.example.data.Note
import java.util.*

// Helper function to convert Hex String to Compose Color
fun String.toColor(isDarkTheme: Boolean = false): Color {
    return try {
        val parsed = Color(android.graphics.Color.parseColor(this))
        if (isDarkTheme && this == "#FFFFFF") {
            // In dark mode, white background changes to a dark card color
            Color(0xFF1E2638)
        } else {
            parsed
        }
    } catch (e: Exception) {
        if (isDarkTheme) Color(0xFF1E2638) else Color.White
    }
}

// Helper to determine text color based on background luminance
fun String.getTextColor(isDarkTheme: Boolean = false): Color {
    val bg = this.toColor(isDarkTheme)
    if (isDarkTheme && this == "#FFFFFF") {
        return Color.White
    }
    // Calculate simple luminance
    val r = bg.red
    val g = bg.green
    val b = bg.blue
    val luminance = 0.2126 * r + 0.7152 * g + 0.0722 * b
    return if (luminance > 0.5) Color(0xFF1E2638) else Color.White
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteAppUi(
    viewModel: NoteViewModel,
    modifier: Modifier = Modifier
) {
    val notes by viewModel.filteredNotes.collectAsStateWithLifecycle()
    val categories by viewModel.categories.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedCategoryId by viewModel.selectedCategoryId.collectAsStateWithLifecycle()
    val isGridLayout by viewModel.isGridLayout.collectAsStateWithLifecycle()
    val editingNote by viewModel.editingNote.collectAsStateWithLifecycle()

    var showAddCategoryDialog by remember { mutableStateOf(false) }
    var noteToDelete by remember { mutableStateOf<Note?>(null) }
    var selectedNoteDetails by remember { mutableStateOf<Note?>(null) }

    val isDarkTheme = isSystemInDarkTheme()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Notlarım",
                        fontWeight = FontWeight.Normal,
                        fontSize = 24.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.toggleLayout() },
                        modifier = Modifier.testTag("layout_toggle_button")
                    ) {
                        Icon(
                            imageVector = if (isGridLayout) Icons.Default.ViewList else Icons.Default.GridView,
                            contentDescription = "Görünüm Değiştir",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    // Clean Minimalist User Avatar
                    Box(
                        modifier = Modifier
                            .padding(end = 12.dp)
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Y",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        floatingActionButton = {
            // Only show FAB when not in editing mode
            if (editingNote == null) {
                FloatingActionButton(
                    onClick = { viewModel.startEditing(null) },
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .navigationBarsPadding()
                        .testTag("add_note_fab")
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Yeni Not Ekle",
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        },
        contentWindowInsets = WindowInsets.safeDrawing
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Standard state layout of the main view
            Column(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                // Search Bar Row
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.setSearchQuery(it) },
                    placeholder = { Text("Notlarda ara...", color = Color(0xFF49454F).copy(alpha = 0.7f)) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Ara",
                            tint = Color(0xFF49454F)
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Temizle",
                                    tint = Color(0xFF49454F)
                                )
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(28.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .testTag("search_bar_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFFECE6F0),
                        unfocusedContainerColor = Color(0xFFECE6F0),
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent
                    )
                )

                // Categories Row (Horizontally scrollable)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    LazyRow(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // "All" item
                        item {
                            FilterChip(
                                selected = selectedCategoryId == null,
                                onClick = { viewModel.selectCategory(null) },
                                label = { Text("Tümü") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                ),
                                shape = RoundedCornerShape(12.dp)
                            )
                        }

                        // Category items
                        items(categories, key = { it.id }) { cat ->
                            val catColor = cat.colorHex.toColor(isDarkTheme)
                            FilterChip(
                                selected = selectedCategoryId == cat.id,
                                onClick = { viewModel.selectCategory(cat.id) },
                                label = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .clip(CircleShape)
                                                .background(catColor)
                                                .border(
                                                    0.5.dp,
                                                    MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                                                    CircleShape
                                                )
                                        )
                                        Text(cat.name)
                                    }
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                ),
                                shape = RoundedCornerShape(12.dp),
                                trailingIcon = {
                                    // Custom categories can be deleted
                                    if (cat.id > 5) { // Assuming first 5 are built-in seeded
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Kategoriyi Sil",
                                            modifier = Modifier
                                                .size(16.dp)
                                                .clickable { viewModel.deleteCategory(cat.id) },
                                            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f)
                                        )
                                    }
                                }
                            )
                        }
                    }

                    // Add Category button
                    IconButton(
                        onClick = { showAddCategoryDialog = true },
                        modifier = Modifier
                            .padding(end = 16.dp)
                            .testTag("add_category_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.AddCircle,
                            contentDescription = "Kategori Ekle",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }

                // Notes Main List / Grid Layout
                if (notes.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.padding(32.dp)
                        ) {
                            Icon(
                                imageVector = if (searchQuery.isNotEmpty()) Icons.Default.SearchOff else Icons.Default.SpeakerNotes,
                                contentDescription = "Boş Not Defteri",
                                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                                modifier = Modifier.size(86.dp)
                            )
                            Text(
                                text = if (searchQuery.isNotEmpty()) "Aramanızla eşleşen not bulunamadı." else "Henüz bir not eklemediniz.",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.testTag("empty_state_text")
                            )
                            if (searchQuery.isEmpty()) {
                                Text(
                                    text = "Not almaya başlamak için sağ alttaki ekleme butonuna dokunabilirsiniz.",
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.outline,
                                    modifier = Modifier.padding(horizontal = 16.dp)
                                )
                            }
                        }
                    }
                } else {
                    val pinnedNotes = notes.filter { it.isPinned }
                    val unpinnedNotes = notes.filter { !it.isPinned }

                    LazyVerticalGrid(
                        columns = if (isGridLayout) GridCells.Fixed(2) else GridCells.Fixed(1),
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f)
                            .padding(horizontal = 12.dp),
                        contentPadding = PaddingValues(bottom = 80.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Section: Pinned Notes
                        if (pinnedNotes.isNotEmpty()) {
                            item(span = { GridItemSpan(maxLineSpan) }) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(top = 8.dp, bottom = 4.dp, start = 4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PushPin,
                                        contentDescription = "Sabitlenenler",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Sabitlenen Notlar",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }

                            items(pinnedNotes, key = { it.id }) { note ->
                                NoteCard(
                                    note = note,
                                    categories = categories,
                                    isDarkTheme = isDarkTheme,
                                    onClick = { selectedNoteDetails = note },
                                    onEditClick = { viewModel.startEditing(note) },
                                    onPinToggle = { viewModel.togglePin(note) },
                                    onDeleteClick = { noteToDelete = note }
                                )
                            }
                        }

                        // Section: Other notes
                        if (unpinnedNotes.isNotEmpty()) {
                            if (pinnedNotes.isNotEmpty()) {
                                item(span = { GridItemSpan(maxLineSpan) }) {
                                    Text(
                                        text = "Tüm Notlar",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.outline,
                                        modifier = Modifier.padding(top = 16.dp, bottom = 4.dp, start = 4.dp)
                                    )
                                }
                            }

                            items(unpinnedNotes, key = { it.id }) { note ->
                                NoteCard(
                                    note = note,
                                    categories = categories,
                                    isDarkTheme = isDarkTheme,
                                    onClick = { selectedNoteDetails = note },
                                    onEditClick = { viewModel.startEditing(note) },
                                    onPinToggle = { viewModel.togglePin(note) },
                                    onDeleteClick = { noteToDelete = note }
                                )
                            }
                        }
                    }
                }
            }

            // Edit / Add screen overlay (Animated Slide In)
            AnimatedVisibility(
                visible = editingNote != null || (editingNote == null && viewModel.editingNote.collectAsStateWithLifecycle().value != null),
                enter = slideInVertically(
                    initialOffsetY = { it },
                    animationSpec = tween(durationMillis = 280)
                ) + fadeIn(),
                exit = slideOutVertically(
                    targetOffsetY = { it },
                    animationSpec = tween(durationMillis = 250)
                ) + fadeOut()
            ) {
                // Ensure context view is present
                val currentNote = viewModel.editingNote.collectAsStateWithLifecycle().value
                NoteEditOverlay(
                    note = currentNote,
                    categories = categories,
                    isDarkTheme = isDarkTheme,
                    onSave = { title, content, catId, colHex, isPinned ->
                        viewModel.saveNote(title, content, catId, colHex, isPinned)
                    },
                    onBack = { viewModel.stopEditing() }
                )
            }
        }
    }

    // Modal Details dialog to read note clearly with nice formatting
    if (selectedNoteDetails != null) {
        val note = selectedNoteDetails!!
        val noteCategory = categories.find { it.id == note.categoryId }
        Dialog(onDismissRequest = { selectedNoteDetails = null }) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .wrapContentHeight(),
                shape = RoundedCornerShape(24.dp),
                color = note.colorHex.toColor(isDarkTheme)
            ) {
                val textColor = note.colorHex.getTextColor(isDarkTheme)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Category Label (if any)
                        if (noteCategory != null) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(noteCategory.colorHex.toColor(isDarkTheme).copy(alpha = 0.25f))
                                    .border(
                                        1.dp,
                                        noteCategory.colorHex.toColor(isDarkTheme).copy(alpha = 0.5f),
                                        RoundedCornerShape(8.dp)
                                    )
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = noteCategory.name,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = textColor.copy(alpha = 0.85f)
                                )
                            }
                        } else {
                            Spacer(modifier = Modifier.width(1.dp))
                        }

                        // Toolbar icons
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(onClick = {
                                viewModel.togglePin(note)
                                selectedNoteDetails = note.copy(isPinned = !note.isPinned)
                            }) {
                                Icon(
                                    imageVector = if (note.isPinned) Icons.Default.PushPin else Icons.Outlined.PushPin,
                                    contentDescription = "Sabitle",
                                    tint = textColor
                                )
                            }
                            IconButton(onClick = {
                                viewModel.startEditing(note)
                                selectedNoteDetails = null
                            }) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Düzenle",
                                    tint = textColor
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = note.title.ifBlank { "Başlıksız Not" },
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = textColor
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = DateUtils.getRelativeTimeSpanString(
                            note.timestamp,
                            System.currentTimeMillis(),
                            DateUtils.MINUTE_IN_MILLIS
                        ).toString(),
                        fontSize = 11.sp,
                        color = textColor.copy(alpha = 0.65f)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Divider(color = textColor.copy(alpha = 0.15f))

                    Spacer(modifier = Modifier.height(16.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 250.dp)
                    ) {
                        Text(
                            text = note.content,
                            fontSize = 14.sp,
                            lineHeight = 20.sp,
                            color = textColor.copy(alpha = 0.9f),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = { selectedNoteDetails = null },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = textColor.copy(alpha = 0.15f),
                            contentColor = textColor
                        ),
                        modifier = Modifier.align(Alignment.End),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Kapat")
                    }
                }
            }
        }
    }

    // Modal Add Category dialog
    if (showAddCategoryDialog) {
        var categoryName by remember { mutableStateOf("") }
        var selectedColorIndex by remember { mutableStateOf(0) }
        val categoryColors = listOf("#4CAF50", "#2196F3", "#FF9800", "#9C27B0", "#E91E63", "#00BCD4", "#E040FB", "#FF5252")

        Dialog(onDismissRequest = { showAddCategoryDialog = false }) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .wrapContentHeight(),
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Yeni Kategori Ekle",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    OutlinedTextField(
                        value = categoryName,
                        onValueChange = { categoryName = it },
                        placeholder = { Text("Kategori adı...") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("new_category_name_input")
                    )

                    Text(
                        text = "Renk Seçin",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // Color row selection
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        categoryColors.forEachIndexed { index, hex ->
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(Color(android.graphics.Color.parseColor(hex)))
                                    .clickable { selectedColorIndex = index }
                                    .border(
                                        width = if (selectedColorIndex == index) 3.dp else 0.dp,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        shape = CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (selectedColorIndex == index) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Seçili",
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = { showAddCategoryDialog = false }) {
                            Text("İptal")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (categoryName.isNotBlank()) {
                                    viewModel.addCategory(categoryName.trim(), categoryColors[selectedColorIndex])
                                    showAddCategoryDialog = false
                                }
                            },
                            enabled = categoryName.isNotBlank()
                        ) {
                            Text("Ekle")
                        }
                    }
                }
            }
        }
    }

    // Modal Confirm Deletion dialog
    if (noteToDelete != null) {
        AlertDialog(
            onDismissRequest = { noteToDelete = null },
            title = { Text("Notu Sil") },
            text = { Text("Bu notu silmek istediğinize emin misiniz? Bu işlem geri alınamaz.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteNote(noteToDelete!!)
                        noteToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Sil", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { noteToDelete = null }) {
                    Text("Vazgeç")
                }
            }
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun NoteCard(
    note: Note,
    categories: List<Category>,
    isDarkTheme: Boolean,
    onClick: () -> Unit,
    onEditClick: () -> Unit,
    onPinToggle: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val matchingCategory = categories.find { it.id == note.categoryId }
    val cardColor = note.colorHex.toColor(isDarkTheme)
    val textColor = note.colorHex.getTextColor(isDarkTheme)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = onEditClick
            )
            .border(
                width = 1.dp,
                color = if (note.colorHex == "#FFFFFF") {
                    if (isDarkTheme) Color(0xFF323B4F) else Color(0xFFCAC4D0)
                } else {
                    textColor.copy(alpha = 0.15f)
                },
                shape = RoundedCornerShape(20.dp)
            )
            .testTag("note_item_${note.id}"),
        colors = CardDefaults.cardColors(
            containerColor = cardColor
        ),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Title + Pin / Action Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = note.title.ifBlank { "Başlıksız Not" },
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    // Pin Button
                    Icon(
                        imageVector = if (note.isPinned) Icons.Default.PushPin else Icons.Outlined.PushPin,
                        contentDescription = "Sabitle",
                        tint = if (note.isPinned) MaterialTheme.colorScheme.primary else textColor.copy(alpha = 0.45f),
                        modifier = Modifier
                            .size(18.dp)
                            .clickable { onPinToggle() }
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    // Delete click
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Notu Sil",
                        tint = textColor.copy(alpha = 0.65f),
                        modifier = Modifier
                            .size(18.dp)
                            .clickable { onDeleteClick() }
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Short contents
            Text(
                text = note.content.ifBlank { "Gövde boş..." },
                fontSize = 12.sp,
                color = textColor.copy(alpha = 0.82f),
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 16.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Footer of the card containing Category pills & Date
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Category Tag
                if (matchingCategory != null) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(matchingCategory.colorHex.toColor(isDarkTheme).copy(alpha = 0.25f))
                            .border(
                                0.5.dp,
                                matchingCategory.colorHex.toColor(isDarkTheme).copy(alpha = 0.4f),
                                RoundedCornerShape(6.dp)
                            )
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = matchingCategory.name,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = textColor.copy(alpha = 0.9f)
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.width(1.dp))
                }

                // Date Label
                Text(
                    text = DateUtils.getRelativeTimeSpanString(
                        note.timestamp,
                        System.currentTimeMillis(),
                        DateUtils.MINUTE_IN_MILLIS
                    ).toString(),
                    fontSize = 10.sp,
                    color = textColor.copy(alpha = 0.55f)
                )
            }
        }
    }
}

@Composable
fun NoteEditOverlay(
    note: Note?,
    categories: List<Category>,
    isDarkTheme: Boolean,
    onSave: (title: String, content: String, categoryId: Long?, colorHex: String, isPinned: Boolean) -> Unit,
    onBack: () -> Unit
) {
    var title by remember(note) { mutableStateOf(note?.title ?: "") }
    var content by remember(note) { mutableStateOf(note?.content ?: "") }
    var selectedCategoryId by remember(note) { mutableStateOf(note?.categoryId) }
    var selectedColorHex by remember(note) { mutableStateOf(note?.colorHex ?: NoteColors[0]) }
    var isPinned by remember(note) { mutableStateOf(note?.isPinned ?: false) }

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Overlay Navigation Headers
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("edit_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Geri"
                        )
                    }
                    Text(
                        text = if (note == null) "Yeni Not Yaz" else "Notu Düzenle",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Pin toggle
                    IconButton(onClick = { isPinned = !isPinned }) {
                        Icon(
                            imageVector = if (isPinned) Icons.Default.PushPin else Icons.Outlined.PushPin,
                            contentDescription = "Sabitle",
                            tint = if (isPinned) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Done/Save Check
                    Button(
                        onClick = {
                            onSave(title, content, selectedCategoryId, selectedColorHex, isPinned)
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("save_note_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Kaydet",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Kaydet")
                    }
                }
            }

            Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            Spacer(modifier = Modifier.height(12.dp))

            // Note Inputs
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                placeholder = { Text("Başlık", fontSize = 18.sp) },
                singleLine = true,
                textStyle = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
                    .testTag("note_title_input"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent
                )
            )

            // Category Selection inside Editing Mode
            Text(
                text = "Kategori",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 6.dp, end = 6.dp, bottom = 6.dp)
            )

            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                item {
                    FilterChip(
                        selected = selectedCategoryId == null,
                        onClick = { selectedCategoryId = null },
                        label = { Text("Yok") },
                        shape = RoundedCornerShape(10.dp)
                    )
                }

                items(categories, key = { it.id }) { cat ->
                    val color = cat.colorHex.toColor(isDarkTheme)
                    FilterChip(
                        selected = selectedCategoryId == cat.id,
                        onClick = { selectedCategoryId = cat.id },
                        label = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(color)
                                )
                                Text(cat.name)
                            }
                        },
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            }

            // Note Card Background Color selection
            Text(
                text = "Not Kartı Rengi",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 6.dp, end = 6.dp, bottom = 8.dp)
            )

            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                items(NoteColors) { hex ->
                    val displayColor = hex.toColor(isDarkTheme)
                    val isSelected = selectedColorHex == hex
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(displayColor)
                            .clickable { selectedColorHex = hex }
                            .border(
                                width = if (isSelected) 3.dp else 1.dp,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Seçildi",
                                tint = hex.getTextColor(isDarkTheme),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            Text(
                text = "İçerik",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 6.dp, end = 6.dp, bottom = 6.dp)
            )

            // Content body input
            OutlinedTextField(
                value = content,
                onValueChange = { content = it },
                placeholder = { Text("Notunuzu yazmaya başlayın...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .testTag("note_content_input"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent
                )
            )
        }
    }
}
