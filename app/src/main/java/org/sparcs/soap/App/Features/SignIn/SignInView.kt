package org.sparcs.soap.App.Features.SignIn

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.launch
import org.sparcs.soap.App.Domain.Error.Auth.AuthUseCaseError
import org.sparcs.soap.App.Domain.Helpers.Constants
import org.sparcs.soap.App.Shared.ViewModelMocks.MockSignInViewModel
import org.sparcs.soap.App.theme.ui.Theme
import org.sparcs.soap.App.theme.ui.isDark
import org.sparcs.soap.R

@Composable
fun SignInView(
    viewModel: SignInViewModelProtocol = hiltViewModel<SignInViewModel>(),
) {
    val isLoading = viewModel.isLoading
    var showErrorDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    val mobisInlineLogoRes = if (MaterialTheme.colorScheme.isDark()) {
        R.drawable.ic_mobis_inline_logo_night
    } else {
        R.drawable.ic_mobis_inline_logo
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .navigationBarsPadding()
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.weight(1f))

        Image(
            painter = painterResource(id = R.drawable.ic_buddy_icon),
            modifier = Modifier.size(100.dp),
            contentDescription = null
        )

        Spacer(modifier = Modifier.weight(1f))

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Sponsored by",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.width(8.dp))

            Image(
                painter = painterResource(mobisInlineLogoRes),
                contentDescription = null,
                modifier = Modifier.height(24.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        TermsAndPrivacyText(context)

        Button(
            onClick = {
                coroutineScope.launch {
                    try {
                        viewModel.signIn(context as Activity)
                    } catch (e: Exception) {
                        errorMessage = when (e) {
                            is AuthUseCaseError -> e.message(context)
                            else -> e.localizedMessage ?: "Unknown error"
                        }
                        showErrorDialog = true
                    }
                }
            }, enabled = !isLoading, modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp,
                    modifier = Modifier.size(24.dp)
                )
            } else {
                Text(
                    text = stringResource(R.string.sign_in_sso)
                )
            }
        }

        if (showErrorDialog) {
            AlertDialog(onDismissRequest = { showErrorDialog = false }, confirmButton = {
                TextButton(onClick = { showErrorDialog = false }) {
                    Text(stringResource(R.string.ok))
                }
            }, title = { Text(stringResource(R.string.error)) }, text = { Text(errorMessage) })
        }
    }
}


@Composable
private fun TermsAndPrivacyText(context: Context) {
    val annotatedString = buildAnnotatedString {
        append(stringResource(R.string.terms_and_privacy_prefix) + " ")

        pushStringAnnotation(tag = "TERMS", annotation = Constants.termsOfUseURL)
        withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.primary)) {
            append(stringResource(R.string.terms_of_use) + " ")
        }
        pop()

        append(stringResource(R.string.and_text) + " ")

        pushStringAnnotation(tag = "PRIVACY", annotation = Constants.privacyPolicyURL)
        withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.primary)) {
            append(stringResource(R.string.privacy_policy) + " ")
        }
        pop()

        append(stringResource(R.string.terms_and_privacy_suffix))
    }

    ClickableText(
        text = annotatedString, style = MaterialTheme.typography.bodySmall.copy(
            color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center
        ), onClick = { offset ->
            annotatedString.getStringAnnotations(tag = "TERMS", start = offset, end = offset)
                .firstOrNull()?.let { uri ->
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri.item))
                    context.startActivity(intent)
                }
            annotatedString.getStringAnnotations(tag = "PRIVACY", start = offset, end = offset)
                .firstOrNull()?.let { uri ->
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri.item))
                    context.startActivity(intent)
                }
        }, modifier = Modifier.fillMaxWidth()
    )
}

@Composable
@Preview
private fun Preview() {
    Theme { SignInView(MockSignInViewModel()) }
}