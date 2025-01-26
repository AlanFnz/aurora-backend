package com.ixtlan.aurora.entity

import jakarta.persistence.*
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails

@Entity
@Table(name = "users")
data class User(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(unique = true)
    private val username: String,

    private var password: String,

    @Column(unique = true)
    val email: String,

    val firstName: String,

    val lastName: String,

    @Column(name = "is_enabled")
    private val isEnabled: Boolean = true,

    @Column(name = "is_account_non_expired")
    private val isAccountNonExpired: Boolean = true,

    @Column(name = "is_account_non_locked")
    private val isAccountNonLocked: Boolean = true,

    @Column(name = "is_credentials_non_expired")
    private val isCredentialsNonExpired: Boolean = true,

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_roles", joinColumns = [JoinColumn(name = "user_id")])
    @Column(name = "role")
    val roles: MutableSet<String> = mutableSetOf(),

    @OneToMany(mappedBy = "user", cascade = [CascadeType.ALL], orphanRemoval = true)
    val folders: MutableList<Folder> = mutableListOf()

) : UserDetails {

    override fun getAuthorities(): Collection<GrantedAuthority> =
        roles.map { SimpleGrantedAuthority("ROLE_$it") }

    override fun getPassword(): String = password

    override fun getUsername(): String = username

    override fun isAccountNonExpired(): Boolean = isAccountNonExpired

    override fun isAccountNonLocked(): Boolean = isAccountNonLocked

    override fun isCredentialsNonExpired(): Boolean = isCredentialsNonExpired

    override fun isEnabled(): Boolean = isEnabled

    fun updatePassword(newPassword: String) {
        password = newPassword
    }
}