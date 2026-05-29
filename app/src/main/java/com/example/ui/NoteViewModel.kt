package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.Category
import com.example.data.Note
import com.example.data.NoteRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

val NoteColors = listOf(
    "#FFFFFF", // Beyaz
    "#FFF1F1", // Pastel Kırmızı
    "#FAF0E6", // Pastel Turuncu/Krem
    "#FFFDF0", // Pastel Sarı
    "#F0FFF4", // Pastel Yeşil
    "#F0F8FF", // Pastel Mavi
    "#FBF0FF", // Pastel Mor
    "#F0FFFF"  // Pastel Turkuaz
)

class NoteViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: NoteRepository

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _selectedCategoryId = MutableStateFlow<Long?>(null)
    val selectedCategoryId = _selectedCategoryId.asStateFlow()

    private val _isGridLayout = MutableStateFlow(true)
    val isGridLayout = _isGridLayout.asStateFlow()

    private val _editingNote = MutableStateFlow<Note?>(null)
    val editingNote = _editingNote.asStateFlow()

    init {
        val database = AppDatabase.getDatabase(application)
        repository = NoteRepository(database.noteDao)

        // Seed default categories if they are empty
        viewModelScope.launch {
            try {
                val list = repository.allCategories.first()
                if (list.isEmpty()) {
                    seedDefaultCategories()
                }
            } catch (e: Exception) {
                // Safe-guard database initialization errors
            }
        }
    }

    val categories: StateFlow<List<Category>> = repository.allCategories.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Combined filtered and sorted list of notes
    val filteredNotes: StateFlow<List<Note>> = combine(
        repository.allNotes,
        _searchQuery,
        _selectedCategoryId
    ) { notes, query, categoryId ->
        notes.filter { note ->
            val matchesSearch = note.title.contains(query, ignoreCase = true) ||
                    note.content.contains(query, ignoreCase = true)
            val matchesCategory = categoryId == null || note.categoryId == categoryId
            matchesSearch && matchesCategory
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun selectCategory(categoryId: Long?) {
        _selectedCategoryId.value = categoryId
    }

    fun toggleLayout() {
        _isGridLayout.value = !_isGridLayout.value
    }

    fun startEditing(note: Note?) {
        _editingNote.value = note
    }

    fun stopEditing() {
        _editingNote.value = null
    }

    fun saveNote(
        title: String,
        content: String,
        categoryId: Long?,
        colorHex: String,
        isPinned: Boolean
    ) {
        viewModelScope.launch {
            val current = _editingNote.value
            if (current != null) {
                // Update existing note
                val updatedNote = current.copy(
                    title = title,
                    content = content,
                    categoryId = categoryId,
                    colorHex = colorHex,
                    isPinned = isPinned,
                    timestamp = System.currentTimeMillis()
                )
                repository.updateNote(updatedNote)
            } else {
                // Create new note
                val newNote = Note(
                    title = title,
                    content = content,
                    categoryId = categoryId,
                    colorHex = colorHex,
                    isPinned = isPinned,
                    timestamp = System.currentTimeMillis()
                )
                repository.insertNote(newNote)
            }
            _editingNote.value = null
        }
    }

    fun togglePin(note: Note) {
        viewModelScope.launch {
            repository.updateNote(note.copy(isPinned = !note.isPinned))
        }
    }

    fun toggleFavorite(note: Note) {
        viewModelScope.launch {
            repository.updateNote(note.copy(isFavorite = !note.isFavorite))
        }
    }

    fun deleteNote(note: Note) {
        viewModelScope.launch {
            repository.deleteNote(note)
        }
    }

    fun addCategory(name: String, colorHex: String) {
        viewModelScope.launch {
            repository.insertCategory(Category(name = name, colorHex = colorHex))
        }
    }

    fun deleteCategory(categoryId: Long) {
        viewModelScope.launch {
            // Remove category correlation for notes to preserve them
            // In a more complex app, the foreign key constraints or queries would handle this
            repository.deleteCategoryById(categoryId)
            if (_selectedCategoryId.value == categoryId) {
                _selectedCategoryId.value = null
            }
        }
    }

    private suspend fun seedDefaultCategories() {
        val defaults = listOf(
            Category(name = "Kişisel", colorHex = "#4CAF50"),
            Category(name = "İş", colorHex = "#2196F3"),
            Category(name = "Fikirler", colorHex = "#FF9800"),
            Category(name = "Yapılacaklar", colorHex = "#9C27B0"),
            Category(name = "Alışveriş", colorHex = "#E91E63")
        )
        for (category in defaults) {
            repository.insertCategory(category)
        }
    }
}
