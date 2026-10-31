package com.ascdev.checkmo.feature.corrections

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
fun CorrectionsRoute(modifier: Modifier = Modifier) {
    CorrectionsScreen(modifier = modifier)
}

@Composable
fun CorrectionsScreen(modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxSize()) {
        CheckmoTopAppBar(title = stringResource(R.string.nav_corrections))
        Box(modifier = Modifier.weight(1f).fillMaxSize(), contentAlignment = Alignment.Center) {
            EmptyState(
                iconRes = R.drawable.ic_edit,
                title = stringResource(R.string.corrections_empty_title),
                message = stringResource(R.string.corrections_empty_message),
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun CorrectionsScreenPreview() {
    CheckmoTheme {
        CorrectionsScreen()
    }
}
