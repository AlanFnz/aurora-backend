package com.ixtlan.aurora.model

data class NoteRequest(
    val title: String,
    val content: String?,
    val folderId: Long
)
