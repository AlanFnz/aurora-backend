package com.ixtlan.aurora.entity

import jakarta.persistence.*

@Entity
data class Folder(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) val id: Long = 0,
    val folderName: String,
    @OneToMany(
        mappedBy = "folder",
        cascade = [CascadeType.ALL],
        orphanRemoval = true
    ) var notes: MutableList<Note> = mutableListOf(),
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "user_id") val user: User
)
