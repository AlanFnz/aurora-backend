package com.ixtlan.aurora.entity

import jakarta.persistence.*

@Entity
data class Note(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    val title: String,

    @Column(columnDefinition = "TEXT")
    val content: String? = null,

    val modifiedDate: Long,

    @ManyToOne
    @JoinColumn(name = "folder_id", nullable = false)
    val folder: Folder
)
