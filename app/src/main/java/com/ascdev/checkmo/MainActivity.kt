package com.ascdev.checkmo

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ascdev.checkmo.core.designsystem.theme.CheckmoTheme
import com.ascdev.checkmo.navigation.CheckmoNavDisplay
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
// FragmentActivity is required to host BiometricPrompt.
class MainActivity : FragmentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            CheckmoTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    // Keep the themed window background while the cached session loads.
                    when (val state = uiState) {
                        MainUiState.Loading -> Unit
                        MainUiState.SignedOut -> CheckmoNavDisplay(user = null)
                        is MainUiState.SignedIn -> CheckmoNavDisplay(user = state.user)
                    }
                }
            }
        }
    }
}
