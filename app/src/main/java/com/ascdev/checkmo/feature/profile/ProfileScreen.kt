package com.ascdev.checkmo.feature.profile

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ascdev.checkmo.R
import com.ascdev.checkmo.core.designsystem.component.CheckmoTopAppBar
import com.ascdev.checkmo.core.designsystem.theme.CheckmoTheme
import com.ascdev.checkmo.domain.model.Company
import com.ascdev.checkmo.domain.model.Coordinates
import com.ascdev.checkmo.domain.model.User
import com.ascdev.checkmo.domain.model.UserRole
import com.ascdev.checkmo.domain.model.Workplace

@Composable
fun ProfileRoute(
    onOpenWorkplace: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ProfileViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    uiState?.let {
        ProfileScreen(
            user = it.user,
            workplace = it.workplace,
            onOpenWorkplace = onOpenWorkplace,
            onSignOut = viewModel::signOut,
            modifier = modifier,
        )
    }
}

@Composable
fun ProfileScreen(
    user: User,
    workplace: Workplace?,
    onOpenWorkplace: () -> Unit,
    onSignOut: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var showSignOutDialog by rememberSaveable { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxSize()) {
        CheckmoTopAppBar(title = stringResource(R.string.nav_profile))
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Avatar(name = user.name)
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = user.name, style = MaterialTheme.typography.titleLarge)
                Text(
                    text = user.email,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Card(modifier = Modifier.fillMaxWidth()) {
                ProfileRow(labelRes = R.string.profile_role, value = stringResource(user.role.labelRes()))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                ProfileRow(labelRes = R.string.profile_company, value = user.company.name)
                if (user.role == UserRole.COMPANY) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    ProfileRow(labelRes = R.string.profile_invite_code, value = user.company.inviteCode)
                }
            }
            if (user.role == UserRole.COMPANY) {
                Text(
                    text = stringResource(R.string.profile_invite_code_hint),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.fillMaxWidth(),
                )
                WorkplaceRow(workplace = workplace, onClick = onOpenWorkplace)
            }

            Spacer(Modifier.size(8.dp))
            OutlinedButton(
                onClick = { showSignOutDialog = true },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_logout),
                    contentDescription = null,
                    modifier = Modifier.size(ButtonDefaults.IconSize),
                )
                Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                Text(stringResource(R.string.profile_sign_out))
            }
        }
    }

    if (showSignOutDialog) {
        AlertDialog(
            onDismissRequest = { showSignOutDialog = false },
            title = { Text(stringResource(R.string.profile_sign_out_title)) },
            text = { Text(stringResource(R.string.profile_sign_out_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showSignOutDialog = false
                        onSignOut()
                    },
                ) {
                    Text(stringResource(R.string.profile_sign_out), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showSignOutDialog = false }) {
                    Text(stringResource(R.string.action_cancel))
                }
            },
        )
    }
}

@Composable
private fun Avatar(name: String) {
    val initials = name.split(" ")
        .filter { it.isNotBlank() }
        .take(2)
        .joinToString("") { it.first().uppercase() }
    Box(
        modifier = Modifier
            .size(88.dp)
            .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = initials,
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
        )
    }
}

@Composable
private fun WorkplaceRow(workplace: Workplace?, onClick: () -> Unit) {
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_place),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
            )
            Spacer(Modifier.size(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = stringResource(R.string.workplace_title), style = MaterialTheme.typography.bodyLarge)
                Text(
                    text = workplace?.let { stringResource(R.string.workplace_radius_summary, it.radiusMeters) }
                        ?: stringResource(R.string.workplace_not_configured),
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (workplace == null) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                )
            }
            Icon(painter = painterResource(R.drawable.ic_chevron_right), contentDescription = null)
        }
    }
}

@Composable
private fun ProfileRow(@StringRes labelRes: Int, value: String) {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp)) {
        Text(
            text = stringResource(labelRes),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(text = value, style = MaterialTheme.typography.bodyLarge)
    }
}

@StringRes
private fun UserRole.labelRes(): Int = when (this) {
    UserRole.EMPLOYEE -> R.string.role_employee
    UserRole.COMPANY -> R.string.role_company
}

@Preview(showBackground = true)
@Composable
private fun ProfileScreenPreview() {
    CheckmoTheme {
        ProfileScreen(
            user = User(
                id = "1",
                name = "Laura Martínez",
                email = "laura.martinez@checkmo.test",
                role = UserRole.COMPANY,
                company = Company(id = "c1", name = "Acme", inviteCode = "KCH4Z2"),
            ),
            workplace = Workplace(Coordinates(40.41678, -3.70379), radiusMeters = 150),
            onOpenWorkplace = {},
            onSignOut = {},
        )
    }
}
