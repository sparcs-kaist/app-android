package com.example.soap.Features.SignIn

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.soap.Domain.Helpers.Constants
import com.example.soap.ui.theme.Theme
import kotlinx.coroutines.launch

@Composable
fun SignInView(
    viewModel: SignInViewModel = hiltViewModel(),
) {
    val isLoading = viewModel.isLoading
    var showErrorDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .navigationBarsPadding()
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.weight(1f))

        Text(
            text = "SPARCS APP INTERNAL",
            style = MaterialTheme.typography.titleMedium,
        )

        TermsAndPrivacyText(context)

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = {
                coroutineScope.launch {
                    try {
                        viewModel.signIn(context as Activity)
                    } catch (e: Exception) {
                        errorMessage = e.message ?: "Unknown error"
                        showErrorDialog = true
                    }
                }
            },
            enabled = !isLoading,
            modifier = Modifier
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
                Text("Sign In with SPARCS SSO")
            }
        }

        if (showErrorDialog) {
            AlertDialog(
                onDismissRequest = { showErrorDialog = false },
                confirmButton = {
                    TextButton(onClick = { showErrorDialog = false }) {
                        Text("Okay")
                    }
                },
                title = { Text("Error") },
                text = { Text(errorMessage) }
            )
        }
    }
}


@Composable
private fun TermsAndPrivacyText(context: Context) {
    val annotatedString = buildAnnotatedString {
        append("By continuing, you agree to our ")

        pushStringAnnotation(tag = "TERMS", annotation = Constants.termsOfUseURL)
        withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.primary)) {
            append("Terms of Use")
        }
        pop()

        append(" and ")

        pushStringAnnotation(tag = "PRIVACY", annotation = Constants.privacyPolicyURL)
        withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.primary)) {
            append("Privacy Policy")
        }
        pop()

        append(".")
    }

    ClickableText(
        text = annotatedString,
        style = MaterialTheme.typography.bodySmall.copy(
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        ),
        onClick = { offset ->
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
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    )
}

@Composable
@Preview
private fun Preview() {
    Theme { SignInView() }
}