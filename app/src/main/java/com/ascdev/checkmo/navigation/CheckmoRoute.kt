package com.ascdev.checkmo.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

sealed interface CheckmoRoute : NavKey {

    @Serializable
    data object Home : CheckmoRoute
}
