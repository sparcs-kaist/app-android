package com.example.soap.Features.Post

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.tooling.preview.Preview
import com.example.soap.ui.theme.SoapTheme

@Composable
fun PostView() {
    var comment by remember { mutableStateOf("") }
//    var isWritingCommentFocusState by remember { mutableStateOf(false) }
    var isWritingComment by remember { mutableStateOf(false) }

}



@Composable
@Preview
private fun Preview(){
    SoapTheme { PostView() }
}
