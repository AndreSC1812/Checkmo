package com.ascdev.checkmo.feature.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ascdev.checkmo.R
import com.ascdev.checkmo.core.designsystem.theme.CheckmoTheme
import com.ascdev.checkmo.domain.model.Company
import com.ascdev.checkmo.domain.model.User
import com.ascdev.checkmo.domain.model.UserRole

@Composable
fun HomeRoute(
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val user by viewModel.user.collectAsStateWithLifecycle()
    user?.let { HomeScreen(user = it, onSignOut = viewModel::signOut, modifier = modifier) }
}

// Temporary home until each role gets its own screens.
@Composable
fun HomeScreen(user: User, onSignOut: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
    ) {
        Text(
            text = stringResource(R.string.home_greeting, user.name),
            style = MaterialTheme.typography.headlineMedium,
        )
        Text(
            text = "${stringResource(user.role.labelRes())} · ${user.company.name}",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        if (user.role == UserRole.COMPANY) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = stringResource(R.string.home_invite_code),
                        style = MaterialTheme.typography.labelLarge,
                    )
                    Text(
                        text = user.company.inviteCode,
                        style = MaterialTheme.typography.displaySmall,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }
        }
        OutlinedButton(onClick = onSignOut) {
            Text(stringResource(R.string.home_sign_out))
        }
    }
}

private fun UserRole.labelRes(): Int = when (this) {
    UserRole.EMPLOYEE -> R.string.role_employee
    UserRole.COMPANY -> R.string.role_company
}

@Preview(showBackground = true)
@Composable
private fun HomeScreenPreview() {
    CheckmoTheme {
        HomeScreen(
            user = User(
                id = "1",
                name = "Ana García",
                email = "ana@empresa.es",
                role = UserRole.COMPANY,
                company = Company(id = "c1", name = "Acme S.L.", inviteCode = "K7P2QX"),
            ),
            onSignOut = {},
        )
    }
}
