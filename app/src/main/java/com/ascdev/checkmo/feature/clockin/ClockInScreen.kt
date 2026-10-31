package com.ascdev.checkmo.feature.clockin

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ascdev.checkmo.R
import com.ascdev.checkmo.core.designsystem.component.CheckmoTopAppBar
import com.ascdev.checkmo.core.designsystem.theme.CheckmoTheme
import com.ascdev.checkmo.core.ui.TimeFormat
import com.ascdev.checkmo.domain.model.SyncStatus
import com.ascdev.checkmo.domain.model.TimeEntry
import com.ascdev.checkmo.domain.model.TimeEntryType
import com.ascdev.checkmo.domain.model.WorkStatus
import com.ascdev.checkmo.domain.model.Workday
import java.time.Duration
import java.time.LocalDate
import java.time.ZoneId

@Composable
fun ClockInRoute(
    modifier: Modifier = Modifier,
    viewModel: ClockInViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    ClockInScreen(
        uiState = uiState,
        onRegister = viewModel::register,
        onErrorShown = viewModel::onErrorShown,
        modifier = modifier,
    )
}

@Composable
fun ClockInScreen(
    uiState: ClockInUiState,
    onRegister: (TimeEntryType) -> Unit,
    onErrorShown: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val error = (uiState as? ClockInUiState.Ready)?.error
    LaunchedEffect(error) {
        if (error != null) {
            snackbarHostState.showSnackbar(context.getString(error.messageRes()))
            onErrorShown()
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = { CheckmoTopAppBar(title = stringResource(R.string.nav_clock_in)) },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        // The shell's bottom bar already handles the navigation bar inset.
        contentWindowInsets = WindowInsets(0),
    ) { innerPadding ->
        when (uiState) {
            ClockInUiState.Loading -> Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }

            is ClockInUiState.Ready -> ClockInContent(
                workday = uiState.workday,
                isRegistering = uiState.isRegistering,
                onRegister = onRegister,
                contentPadding = innerPadding,
            )
        }
    }
}

@Composable
private fun ClockInContent(
    workday: Workday,
    isRegistering: Boolean,
    onRegister: (TimeEntryType) -> Unit,
    contentPadding: PaddingValues,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = contentPadding.calculateTopPadding(),
            bottom = contentPadding.calculateBottomPadding() + 24.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            Text(
                text = TimeFormat.fullDate(workday.date),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        item { StatusCard(workday) }
        item {
            ClockActions(
                actions = workday.status.allowedEntries,
                enabled = !isRegistering,
                onRegister = onRegister,
            )
        }
        item { Summary(worked = workday.worked, onBreak = workday.onBreak) }
        item {
            Text(
                text = stringResource(R.string.clock_in_today_entries),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(top = 8.dp),
            )
        }
        if (workday.entries.isEmpty()) {
            item {
                Text(
                    text = stringResource(R.string.clock_in_no_entries),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        } else {
            items(workday.entries.asReversed(), key = { it.id }) { entry -> EntryRow(entry) }
        }
    }
}

@Composable
private fun StatusCard(workday: Workday) {
    val colors = CheckmoTheme.extendedColors
    val (targetContainer, targetContent) = when (workday.status) {
        WorkStatus.OFF_DUTY -> MaterialTheme.colorScheme.surfaceContainerHigh to MaterialTheme.colorScheme.onSurface
        WorkStatus.WORKING -> colors.clockInContainer to colors.onClockInContainer
        WorkStatus.ON_BREAK -> colors.pauseContainer to colors.onPauseContainer
    }
    val container by animateColorAsState(targetContainer, label = "statusContainer")
    val content by animateColorAsState(targetContent, label = "statusContent")

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = container, contentColor = content),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(text = stringResource(workday.status.labelRes()), style = MaterialTheme.typography.titleMedium)
            workday.statusSince?.let { since ->
                Text(
                    text = stringResource(R.string.work_status_since, TimeFormat.time(since)),
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
            Spacer(Modifier.height(16.dp))
            Text(text = TimeFormat.clock(workday.worked), style = MaterialTheme.typography.displayMedium)
            Text(text = stringResource(R.string.clock_in_worked_today), style = MaterialTheme.typography.labelLarge)
        }
    }
}

@Composable
private fun ClockActions(
    actions: List<TimeEntryType>,
    enabled: Boolean,
    onRegister: (TimeEntryType) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        actions.forEach { type ->
            val (container, content) = type.colors()
            Button(
                onClick = { onRegister(type) },
                enabled = enabled,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp),
                colors = ButtonDefaults.buttonColors(containerColor = container, contentColor = content),
            ) {
                Icon(painterResource(type.iconRes()), contentDescription = null)
                Spacer(Modifier.size(12.dp))
                Text(text = stringResource(type.actionRes()), style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}

@Composable
private fun Summary(worked: Duration, onBreak: Duration) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        SummaryTile(
            label = stringResource(R.string.clock_in_summary_worked),
            value = TimeFormat.hoursMinutes(worked),
            accent = CheckmoTheme.extendedColors.clockIn,
            modifier = Modifier.weight(1f),
        )
        SummaryTile(
            label = stringResource(R.string.clock_in_summary_breaks),
            value = TimeFormat.hoursMinutes(onBreak),
            accent = CheckmoTheme.extendedColors.pause,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun SummaryTile(label: String, value: String, accent: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier
                        .size(8.dp)
                        .background(accent, CircleShape),
                )
                Spacer(Modifier.size(8.dp))
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Spacer(Modifier.height(4.dp))
            Text(text = value, style = MaterialTheme.typography.titleLarge)
        }
    }
}

@Composable
private fun EntryRow(entry: TimeEntry) {
    val (accent, onAccent) = entry.type.colors()
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(accent, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(entry.type.iconRes()),
                contentDescription = null,
                tint = onAccent,
                modifier = Modifier.size(18.dp),
            )
        }
        Spacer(Modifier.size(12.dp))
        Text(
            text = stringResource(entry.type.labelRes()),
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f),
        )
        Text(text = TimeFormat.time(entry.timestamp), style = MaterialTheme.typography.titleMedium)
    }
}

@Preview(showBackground = true, heightDp = 900)
@Composable
private fun ClockInScreenPreview() {
    val zone = ZoneId.systemDefault()
    val date = LocalDate.of(2026, 10, 30)
    fun entry(type: TimeEntryType, hour: Int) = TimeEntry(
        id = "$type",
        userId = "u",
        companyId = "c",
        type = type,
        timestamp = date.atTime(hour, 0).atZone(zone).toInstant(),
        syncStatus = SyncStatus.PENDING,
    )
    CheckmoTheme {
        ClockInScreen(
            uiState = ClockInUiState.Ready(
                workday = Workday(
                    date = date,
                    status = WorkStatus.WORKING,
                    statusSince = date.atTime(14, 0).atZone(zone).toInstant(),
                    worked = Duration.ofMinutes(325).plusSeconds(17),
                    onBreak = Duration.ofMinutes(60),
                    entries = listOf(
                        entry(TimeEntryType.CLOCK_IN, 9),
                        entry(TimeEntryType.BREAK_START, 13),
                        entry(TimeEntryType.BREAK_END, 14),
                    ),
                ),
            ),
            onRegister = {},
            onErrorShown = {},
        )
    }
}
