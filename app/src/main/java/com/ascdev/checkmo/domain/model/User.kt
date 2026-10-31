package com.ascdev.checkmo.domain.model

data class User(
    val id: String,
    val name: String,
    val email: String,
    val role: UserRole,
    val company: Company,
)
