package com.ascdev.checkmo.domain.model

sealed interface SignUpRequest {
    val name: String
    val email: String
    val password: String

    data class Employee(
        override val name: String,
        override val email: String,
        override val password: String,
        val inviteCode: String,
    ) : SignUpRequest

    data class Company(
        override val name: String,
        override val email: String,
        override val password: String,
        val companyName: String,
    ) : SignUpRequest
}
