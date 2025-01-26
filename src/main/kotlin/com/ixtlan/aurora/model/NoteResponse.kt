package com.ixtlan.aurora.model

data class NoteResponse(
    val id: Long,
    val title: String,
    val content: String?,
    val folderId: Long?,
    val modifiedDate: Long
)
