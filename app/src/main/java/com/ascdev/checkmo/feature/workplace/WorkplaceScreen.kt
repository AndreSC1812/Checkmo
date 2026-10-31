package com.ascdev.checkmo.feature.workplace

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ascdev.checkmo.R
import com.ascdev.checkmo.core.designsystem.component.CheckmoTopAppBar
import com.ascdev.checkmo.core.designsystem.theme.CheckmoTheme
import com.ascdev.checkmo.domain.model.Coordinates
import com.ascdev.checkmo.domain.model.Workplace
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun WorkplaceRoute(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: WorkplaceViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) { grants ->
        if (grants[Manifest.permission.ACCESS_FINE_LOCATION] == true) {
            viewModel.useCurrentLocation()
        } else {
            viewModel.onLocationPermissionDenied()
        }
    }

    WorkplaceScreen(
        uiState = uiState,
        onNavigateBack = onNavigateBack,
        onUseCurrentLocation = {
            // Android 12+ requires asking for coarse together with fine location.
            permissionLauncher.launch(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
            )
        },
        onRadiusChange = viewModel::onRadiusChange,
        onSave = viewModel::save,
        onMessageShown = viewModel::onMessageShown,
        modifier = modifier,
    )
}

@Composable
fun WorkplaceScreen(
    uiState: WorkplaceUiState,
    onNavigateBack: () -> Unit,
    onUseCurrentLocation: () -> Unit,
    onRadiusChange: (Float) -> Unit,
    onSave: () -> Unit,
    onMessageShown: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val resources = LocalResources.current
    LaunchedEffect(uiState.message) {
        uiState.message?.let { message ->
            snackbarHostState.showSnackbar(resources.getString(message.textRes()))
            onMessageShown()
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            CheckmoTopAppBar(title = stringResource(R.string.workplace_title), onNavigateBack = onNavigateBack)
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        contentWindowInsets = WindowInsets(0),
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = stringResource(R.string.workplace_description),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            LocationCard(
                center = uiState.center,
                accuracyMeters = uiState.accuracyMeters,
                isLocating = uiState.isLocating,
                enabled = !uiState.isLoading && !uiState.isSaving,
                onUseCurrentLocation = onUseCurrentLocation,
            )
            RadiusCard(
                radiusMeters = uiState.radiusMeters,
                enabled = !uiState.isLoading && !uiState.isSaving,
                onRadiusChange = onRadiusChange,
            )
            Button(
                onClick = onSave,
                enabled = uiState.canSave,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
            ) {
                if (uiState.isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp,
                    )
                } else {
                    Text(stringResource(R.string.workplace_save))
                }
            }
        }
    }
}

@Composable
private fun LocationCard(
    center: Coordinates?,
    accuracyMeters: Float?,
    isLocating: Boolean,
    enabled: Boolean,
    onUseCurrentLocation: () -> Unit,
) {
    val context = LocalContext.current
    val mapLabel = stringResource(R.string.workplace_map_label)

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = stringResource(R.string.workplace_location),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = painterResource(R.drawable.ic_place),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                )
                Spacer(Modifier.size(8.dp))
                Text(
                    text = center?.format() ?: stringResource(R.string.workplace_not_configured),
                    style = MaterialTheme.typography.titleMedium,
                )
            }
            accuracyMeters?.let {
                Text(
                    text = stringResource(R.string.workplace_accuracy, it.roundToInt()),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (center != null) {
                TextButton(
                    onClick = {
                        val uri = "geo:${center.latitude},${center.longitude}" +
                            "?q=${center.latitude},${center.longitude}(${Uri.encode(mapLabel)})"
                        try {
                            context.startActivity(Intent(Intent.ACTION_VIEW, uri.toUri()))
                        } catch (_: ActivityNotFoundException) {
                            // No maps app installed; nothing else to do.
                        }
                    },
                ) {
                    Text(stringResource(R.string.workplace_open_map))
                }
            }
            OutlinedButton(
                onClick = onUseCurrentLocation,
                enabled = enabled && !isLocating,
                modifier = Modifier.fillMaxWidth(),
            ) {
                if (isLocating) {
                    CircularProgressIndicator(modifier = Modifier.size(ButtonDefaults.IconSize), strokeWidth = 2.dp)
                } else {
                    Icon(
                        painter = painterResource(R.drawable.ic_my_location),
                        contentDescription = null,
                        modifier = Modifier.size(ButtonDefaults.IconSize),
                    )
                }
                Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                Text(stringResource(R.string.workplace_use_current_location))
            }
        }
    }
}

@Composable
private fun RadiusCard(radiusMeters: Int, enabled: Boolean, onRadiusChange: (Float) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = stringResource(R.string.workplace_radius),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    text = stringResource(R.string.workplace_radius_value, radiusMeters),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            Slider(
                value = radiusMeters.toFloat(),
                onValueChange = onRadiusChange,
                enabled = enabled,
                valueRange = Workplace.MIN_RADIUS_METERS.toFloat()..Workplace.MAX_RADIUS_METERS.toFloat(),
                steps = (Workplace.MAX_RADIUS_METERS - Workplace.MIN_RADIUS_METERS) / WorkplaceViewModel.RADIUS_STEP - 1,
            )
        }
    }
}

private fun Coordinates.format(): String = "%.5f, %.5f".format(Locale.ROOT, latitude, longitude)

private fun WorkplaceMessage.textRes(): Int = when (this) {
    WorkplaceMessage.SAVED -> R.string.workplace_saved
    WorkplaceMessage.SAVE_FAILED -> R.string.workplace_save_failed
    WorkplaceMessage.LOCATION_PERMISSION_DENIED -> R.string.location_permission_denied
    WorkplaceMessage.LOCATION_UNAVAILABLE -> R.string.location_unavailable
}

@Preview(showBackground = true)
@Composable
private fun WorkplaceScreenPreview() {
    CheckmoTheme {
        WorkplaceScreen(
            uiState = WorkplaceUiState(
                isLoading = false,
                center = Coordinates(40.41678, -3.70379),
                accuracyMeters = 12f,
                radiusMeters = 150,
                hasChanges = true,
            ),
            onNavigateBack = {},
            onUseCurrentLocation = {},
            onRadiusChange = {},
            onSave = {},
            onMessageShown = {},
        )
    }
}
