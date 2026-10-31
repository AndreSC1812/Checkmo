package com.ascdev.checkmo.feature.reports

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.ascdev.checkmo.R
import com.ascdev.checkmo.core.designsystem.component.CheckmoTopAppBar
import com.ascdev.checkmo.core.designsystem.component.EmptyState
import com.ascdev.checkmo.core.designsystem.theme.CheckmoTheme

@Composable
fun ReportsRoute(modifier: Modifier = Modifier) {
    ReportsScreen(modifier = modifier)
}

@Composable
fun ReportsScreen(modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxSize()) {
        CheckmoTopAppBar(title = stringResource(R.string.nav_reports))
        Box(modifier = Modifier.weight(1f).fillMaxSize(), contentAlignment = Alignment.Center) {
            EmptyState(
                iconRes = R.drawable.ic_description,
                title = stringResource(R.string.reports_empty_title),
                message = stringResource(R.string.reports_empty_message),
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ReportsScreenPreview() {
    CheckmoTheme {
        ReportsScreen()
    }
}
