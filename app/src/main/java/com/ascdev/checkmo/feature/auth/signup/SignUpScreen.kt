package com.ascdev.checkmo.feature.auth.signup

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ascdev.checkmo.R
import com.ascdev.checkmo.core.designsystem.component.PasswordTextField
import com.ascdev.checkmo.core.designsystem.theme.CheckmoTheme
import com.ascdev.checkmo.domain.model.UserRole
import com.ascdev.checkmo.domain.validation.CredentialsValidator
import com.ascdev.checkmo.feature.auth.AuthHeader
import com.ascdev.checkmo.feature.auth.message

@Composable
fun SignUpRoute(
    onBackToLogin: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SignUpViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    SignUpScreen(
        uiState = uiState,
        onRoleChange = viewModel::onRoleChange,
        onNameChange = viewModel::onNameChange,
        onEmailChange = viewModel::onEmailChange,
        onPasswordChange = viewModel::onPasswordChange,
        onInviteCodeChange = viewModel::onInviteCodeChange,
        onCompanyNameChange = viewModel::onCompanyNameChange,
        onSignUp = viewModel::signUp,
        onBackToLogin = onBackToLogin,
        modifier = modifier,
    )
}

@Composable
fun SignUpScreen(
    uiState: SignUpUiState,
    onRoleChange: (UserRole) -> Unit,
    onNameChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onInviteCodeChange: (String) -> Unit,
    onCompanyNameChange: (String) -> Unit,
    onSignUp: () -> Unit,
    onBackToLogin: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val enabled = !uiState.isLoading

    Column(
        modifier = modifier
            .fillMaxSize()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Column(
            modifier = Modifier.widthIn(max = 420.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            AuthHeader(
                title = stringResource(R.string.auth_sign_up_title),
                subtitle = stringResource(R.string.auth_sign_up_subtitle),
            )
            Spacer(Modifier.height(8.dp))

            RoleSelector(selected = uiState.role, onSelect = onRoleChange, enabled = enabled)

            OutlinedTextField(
                value = uiState.name,
                onValueChange = onNameChange,
                modifier = Modifier.fillMaxWidth(),
                enabled = enabled,
                label = { Text(stringResource(R.string.auth_name)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Words,
                    imeAction = ImeAction.Next,
                ),
            )
            OutlinedTextField(
                value = uiState.email,
                onValueChange = onEmailChange,
                modifier = Modifier.fillMaxWidth(),
                enabled = enabled,
                label = { Text(stringResource(R.string.auth_email)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
            )
            PasswordTextField(
                value = uiState.password,
                onValueChange = onPasswordChange,
                modifier = Modifier.fillMaxWidth(),
                enabled = enabled,
                supportingText = stringResource(R.string.auth_password_hint, CredentialsValidator.MIN_PASSWORD_LENGTH),
                imeAction = ImeAction.Next,
            )

            when (uiState.role) {
                UserRole.EMPLOYEE -> OutlinedTextField(
                    value = uiState.inviteCode,
                    onValueChange = onInviteCodeChange,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = enabled,
                    label = { Text(stringResource(R.string.auth_invite_code)) },
                    supportingText = { Text(stringResource(R.string.auth_invite_code_hint)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Characters,
                        imeAction = ImeAction.Done,
                    ),
                )

                UserRole.COMPANY -> OutlinedTextField(
                    value = uiState.companyName,
                    onValueChange = onCompanyNameChange,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = enabled,
                    label = { Text(stringResource(R.string.auth_company_name)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Words,
                        imeAction = ImeAction.Done,
                    ),
                )
            }

            uiState.error?.let { error ->
                Text(
                    text = error.message(),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            Button(
                onClick = onSignUp,
                enabled = enabled,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp,
                    )
                } else {
                    Text(stringResource(R.string.auth_create_account))
                }
            }
            TextButton(onClick = onBackToLogin, enabled = enabled) {
                Text(stringResource(R.string.auth_have_account))
            }
        }
    }
}

@Composable
private fun RoleSelector(selected: UserRole, onSelect: (UserRole) -> Unit, enabled: Boolean) {
    val roles = UserRole.entries
    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
        roles.forEachIndexed { index, role ->
            SegmentedButton(
                selected = role == selected,
                onClick = { onSelect(role) },
                shape = SegmentedButtonDefaults.itemShape(index = index, count = roles.size),
                enabled = enabled,
            ) {
                Text(
                    stringResource(
                        when (role) {
                            UserRole.EMPLOYEE -> R.string.role_employee
                            UserRole.COMPANY -> R.string.role_company
                        },
                    ),
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SignUpScreenPreview() {
    CheckmoTheme {
        SignUpScreen(
            uiState = SignUpUiState(role = UserRole.COMPANY),
            onRoleChange = {},
            onNameChange = {},
            onEmailChange = {},
            onPasswordChange = {},
            onInviteCodeChange = {},
            onCompanyNameChange = {},
            onSignUp = {},
            onBackToLogin = {},
        )
    }
}
