package com.example.data

import kotlinx.coroutines.flow.Flow

class NoteRepository(private val noteDao: NoteDao) {
    val allNotes: Flow<List<Note>> = noteDao.getAllNotes()
    val allCategories: Flow<List<Category>> = noteDao.getAllCategories()

    fun getNotesByCategory(categoryId: Long): Flow<List<Note>> {
        return noteDao.getNotesByCategory(categoryId)
    }

    suspend fun getNoteById(id: Long): Note? {
        return noteDao.getNoteById(id)
    }

    suspend fun insertNote(note: Note): Long {
        return noteDao.insertNote(note)
    }

    suspend fun updateNote(note: Note) {
        noteDao.updateNote(note)
    }

    suspend fun deleteNote(note: Note) {
        noteDao.deleteNote(note)
    }

    suspend fun deleteNoteById(id: Long) {
        noteDao.deleteNoteById(id)
    }

    suspend fun insertCategory(category: Category): Long {
        return noteDao.insertCategory(category)
    }

    suspend fun deleteCategoryById(id: Long) {
        noteDao.deleteCategoryById(id)
    }
}
