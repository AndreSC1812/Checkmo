package com.ascdev.checkmo.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

sealed interface CheckmoRoute : NavKey {

    // Auth
    @Serializable
    data object Login : CheckmoRoute

    @Serializable
    data object SignUp : CheckmoRoute

    // Employee
    @Serializable
    data object ClockIn : CheckmoRoute

    @Serializable
    data object History : CheckmoRoute

    // Company
    @Serializable
    data object Team : CheckmoRoute

    @Serializable
    data object Corrections : CheckmoRoute

    @Serializable
    data object Reports : CheckmoRoute

    // Shared
    @Serializable
    data object Profile : CheckmoRoute
}
