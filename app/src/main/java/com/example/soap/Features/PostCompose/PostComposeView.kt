package com.example.soap.Features.PostCompose

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActionScope
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.soap.Features.PostCompose.Components.FlairSelector
import com.example.soap.Features.PostList.PostListViewModel
import com.example.soap.R

@Composable
fun PostComposeView(postListViewModel: PostListViewModel) {

//    @Environment(PostListViewModel.self) private var viewModel
//  @Environment(\.dismiss) private var dismiss
    val titleFocusRequester = remember { FocusRequester() }
    val descriptionFocusRequester = remember { FocusRequester() } //title to description

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    var writeAsAnonymous by remember { mutableStateOf(true) }
    var isNSFW by remember { mutableStateOf(false) }
    var isPolitical by remember { mutableStateOf(false) }

    val isDoneEnabled = title.isNotBlank() && description.isNotBlank()
//    var showBottomSheet by remember { mutableStateOf(false) }

    Column(Modifier
        .background(Color.White)
        .fillMaxSize()
        .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Text(
                text = "Write",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.Center)
            )
            Text(
                text = "Done",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Normal,
                color = if (isDoneEnabled) Color(0xFF6157CD) else Color.Gray,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .clickable {
                        if (isDoneEnabled) {
                            //Todo : Post, Dismiss Button
                        }
                    }
            )
        }
        FlairSelector(postListViewModel)

        Spacer(Modifier.padding(8.dp))

        TextField(
            value = title,
            onValueChange = {title = it},
            placeholderText = "Please enter the title",
            singleLine = true,
            textStyle = MaterialTheme.typography.titleLarge,
            nextFocus = {
                descriptionFocusRequester.requestFocus()
            },
            focusRequester = titleFocusRequester
        )

        HorizontalDivider()

        Spacer(Modifier.padding(4.dp))

        TextField(
            value = description,
            onValueChange = {description = it},
            placeholderText = "What's happening?",
            singleLine = false,
            textStyle = MaterialTheme.typography.bodyMedium,
            nextFocus = {},
            focusRequester = descriptionFocusRequester
        )

        Box(Modifier.align(Alignment.End)){ TermsOfUseButton() }

        Spacer(Modifier.weight(1f))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.background(Color.White)
        ){
            Icon(
                painter = painterResource(R.drawable.add_photo_alternate),
                contentDescription = "Add photo",
                modifier = Modifier.clickable {  }
            )

            Spacer(Modifier.weight(1f))

            CheckBoxText(
                text = "Anonymous",
                isChecked = writeAsAnonymous,
                onCheckedChange = { writeAsAnonymous = !writeAsAnonymous }
            )

            CheckBoxText(
                text = "NSFW",
                isChecked = isNSFW,
                onCheckedChange = { isNSFW = !isNSFW }
            )

            CheckBoxText(
                text = "Political",
                isChecked = isPolitical,
                onCheckedChange = { isPolitical = !isPolitical}
            )
        }
    }

}

@Composable
fun CheckBoxText(
    text: String, 
    isChecked: Boolean,
    onCheckedChange:((Boolean) -> Unit)?
){
    Row(
        verticalAlignment = Alignment.CenterVertically
    ){
        Checkbox(
            checked = isChecked,
            onCheckedChange = onCheckedChange,
            colors = CheckboxDefaults.colors(Color.DarkGray)
        )

        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
fun TextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholderText: String,
    singleLine: Boolean,
    textStyle: TextStyle,
    nextFocus: (KeyboardActionScope.() -> Unit)?,
    focusRequester: FocusRequester
){
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = singleLine,
        textStyle = textStyle.copy(color = Color.Black),
        cursorBrush = SolidColor(Color(0xFF6157CD)),
        keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next),
        keyboardActions = KeyboardActions(
            onNext = nextFocus
        ),
        modifier = Modifier.focusRequester(focusRequester),
        decorationBox = { innerTextField ->
            if (value.isEmpty()) {
                Text(
                    text = placeholderText,
                    color = Color.Gray,
                    style = textStyle
                )
            }
            innerTextField()
        }
    )
}

@Composable
private fun TermsOfUseButton(){
    Text(
        text = "terms of use",
        style = MaterialTheme.typography.bodySmall,
        textDecoration = TextDecoration.Underline,
        color = Color.DarkGray
    )
}

@Composable
@Preview
private fun Preview(){
    PostComposeView(viewModel())
}