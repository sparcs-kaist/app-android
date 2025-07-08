package com.example.soap.Features.Post

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.soap.Features.NavigationBar.Components.PostNavigationBar
import com.example.soap.Features.Post.Components.Comments
import com.example.soap.Features.Post.Components.PostBookmarkButton
import com.example.soap.Features.Post.Components.PostReportButton
import com.example.soap.Features.Post.Components.PostShareButton
import com.example.soap.Features.Post.Components.PostVoteBigButton
import com.example.soap.R
import com.example.soap.ui.theme.SoapTheme
import com.example.soap.ui.theme.soapColors

@Composable
fun PostView(navController: NavController) {
    var comment by remember { mutableStateOf("") }
//    var isWritingCommentFocusState by remember { mutableStateOf(false) }
    var isWritingComment by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            PostNavigationBar(navController= navController)
        },
        bottomBar = {
            ReplyTextField(
                value = comment,
                onValueChange = {comment = it}
            )
        }
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.soapColors.surface)
                .verticalScroll(scrollState)
                .padding(innerPadding)) {

            Header()

            Content()

            Footer()

            Comments("test")


        }

    }
}

@Composable
private fun Header(){
    Column(Modifier.padding(vertical = 8.dp, horizontal = 16.dp)) {
        Text(
            text ="[대학원 동아리 연합회] 대학원 동아리 연합회 소속이 되실 동아리를 모집합니다! (feat. 2022년 하반기 동아리 등록 심사 위원회)",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(Modifier.padding(4.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text= "22 May 2025 16:22",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.soapColors.grayBB
            )

            Spacer(Modifier.padding(4.dp))

            Text(
                text = "485 views",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.soapColors.grayBB
            )
        }

        Spacer(Modifier.padding(4.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = painterResource(R.drawable.ic_launcher_background),
                contentDescription = "profile image",
                modifier = Modifier
                    .clip(CircleShape)
                    .size(28.dp)
            )

            Spacer(Modifier.padding(4.dp))

            Text(
                text = "류형욱(전산학부)",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.offset(y = -1.dp)
            )

            Icon(
                painter = painterResource(R.drawable.arrow_forward_ios),
                contentDescription = "View user's post",
                modifier = Modifier.size(15.dp)
            )
        }
        HorizontalDivider(
            color = MaterialTheme.soapColors.gray0Border,
            modifier = Modifier.padding(vertical = 8.dp)
        )
    }
}

@Composable
private fun Content(){
    Box(Modifier
        .padding(horizontal = 16.dp)){
        Text(
            text = "대학원 학우 여러분 안녕하세요.\n" +
                    "\n" +
                    "대학원 동아리 연합회에서는 매년 2회(상반기-4월, 하반기-10월) 동아리 등록 심사 위원회(동심위)를 통해 새로운 대학원 동아리를 모집하고 심사합니다.\n" +
                    "\n" +
                    "올해 새로운 동아리를 모집하기 위해 22년 하반기 동아리 등록 심사 위원회가 개최됩니다.\n" +
                    "동아리 등록 심사 위원회에 지원하실 동아리들을 모집합니다.\n" +
                    "\n" +
                    "대학원 동아리 연합회에는 가동아리, 정식 동아리 두 가지 동아리가 있습니다.",
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
private fun Footer(){
    Column(Modifier
        .padding(horizontal = 16.dp)
        .padding(top = 16.dp, bottom = 8.dp)){

        PostVoteBigButton(Modifier.align(Alignment.CenterHorizontally))

        Spacer(Modifier.padding(8.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {

            PostBookmarkButton()

            Spacer(Modifier.padding(4.dp))

            PostShareButton()

            Spacer(Modifier.weight(1f))

            PostReportButton()

        }
    }
    HorizontalDivider(
        color = MaterialTheme.soapColors.gray0Border,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    )

    Text(
        text = "1개의 댓글",
        style = MaterialTheme.typography.bodyLarge,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(vertical = 8.dp, horizontal = 16.dp)
    )
}

@Composable
private fun ReplyTextField(
    value: String,
    onValueChange: (String) -> Unit

){
    Box(
        Modifier
            .fillMaxWidth()
            .background(Color.Transparent)
            .navigationBarsPadding()
            .shadow(
                elevation = 8.dp
            )
    ) {
        Row(
            modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.soapColors.surface)
            .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ){
            Box(Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.soapColors.grayf8)
                .weight(1f)
                .padding(4.dp),

            ){
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    textStyle = MaterialTheme.typography.titleLarge.copy(color = MaterialTheme.soapColors.onSurface),
                    cursorBrush = SolidColor(MaterialTheme.soapColors.primary),
                    decorationBox = { innerTextField ->
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            if (value.isEmpty()) {
                                Text(
                                    text = stringResource(R.string.reply_as_anonymous),
                                    color = MaterialTheme.soapColors.grayBB,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                            innerTextField()
                        }
                    }
                )
            }

            Spacer(Modifier.padding(4.dp))

            Icon(
                painter = painterResource(R.drawable.paperplane),
                contentDescription = "Send Button",
                tint = MaterialTheme.soapColors.primary
            )

        }
    }
}

@Composable
@Preview
private fun Preview(){
    SoapTheme { PostView(rememberNavController()) }
}
