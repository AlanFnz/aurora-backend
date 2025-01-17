package com.ixtlan.aurora.model

data class FolderResponse(
    val id: Long,
    val folderName: String,
    val notes: List<NoteResponse>
) {
    data class NoteResponse(
        val id: Long,
        val title: String,
        val snippet: String,
        val modifiedDate: Long
    )
}
