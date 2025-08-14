package com.example.soap.Features.Post

import HTMLView
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import com.example.soap.Features.Post.Components.Comments
import com.example.soap.Features.Post.Components.PostBookmarkButton
import com.example.soap.Features.Post.Components.PostNavigationBar
import com.example.soap.Features.Post.Components.PostReportButton
import com.example.soap.Features.Post.Components.PostShareButton
import com.example.soap.Features.Post.Components.PostVoteBigButton
import com.example.soap.R
import com.example.soap.ui.theme.Theme
import com.example.soap.ui.theme.grayBB
import com.example.soap.ui.theme.grayF8
import com.example.soap.ui.theme.lightGray0

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
                .background(MaterialTheme.colorScheme.background)
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
                color = MaterialTheme.colorScheme.grayBB
            )

            Spacer(Modifier.padding(4.dp))

            Text(
                text = "485 views",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.grayBB
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
            color = MaterialTheme.colorScheme.lightGray0,
            modifier = Modifier.padding(vertical = 8.dp)
        )
    }
}

@Composable
private fun Content(){
    Box(Modifier
        .padding(horizontal = 16.dp)){
//        Text(
//            text = "대학원 학우 여러분 안녕하세요.\n" +
//                    "\n" +
//                    "대학원 동아리 연합회에서는 매년 2회(상반기-4월, 하반기-10월) 동아리 등록 심사 위원회(동심위)를 통해 새로운 대학원 동아리를 모집하고 심사합니다.\n" +
//                    "\n" +
//                    "올해 새로운 동아리를 모집하기 위해 22년 하반기 동아리 등록 심사 위원회가 개최됩니다.\n" +
//                    "동아리 등록 심사 위원회에 지원하실 동아리들을 모집합니다.\n" +
//                    "\n" +
//                    "대학원 동아리 연합회에는 가동아리, 정식 동아리 두 가지 동아리가 있습니다.",
//            style = MaterialTheme.typography.bodyMedium
//        )
        var htmlHeight by remember { mutableStateOf(0) }
        val contentString = """
    <p>안녕하세요! 기계동 정문 왼편에서 4년째 사랑받아 온 ‘오샐러드’입니다.<br>우리가게 오늘도 정상 영업 중입니다.<br>새롭게 선보이는 사이드 메뉴와 풍성한 할인 이벤트로 여러분을 찾아갑니다!<br><br>🎉세트 메뉴로 더 푸짐하고, 더 건강하게! 오샐러드 세트 할인 이벤트🎉<br><br>건강한 한 끼, 오샐러드에서 든든하게 즐기세요!<br>이제, 더 많은 메뉴를 더 저렴하게 즐길 수 있는 기회!<br>세트 할인 메뉴로 더욱 푸짐하고, 알차게 챙기세요! 🥑<br><br>🎁 [오샐러드] 세트 할인 메뉴 및 영양성분<br><br>닭가슴살 샐러드 세트 (칼로리: 592.7 kcal, 단백질: 31.8 g, 탄수화물: 111.4 g, 지방: 4.55 g / 파스타 기준, 쿠키 제외)<br>• 닭가슴살 샐러드(곡물 or 파스타) +단백질쿠키(더블초코 or 피넛버터) 택 1 +제로아이스티(복숭아 or 자몽) 택 1 = 정가 10,000원 할인가 7,000원<br><br>단호박 샐러드 세트 (칼로리: 661.0 kcal, 단백질: 22.2 g, 탄수화물: 120.0 g, 지방: 14.3 g / 파스타 기준)<br>• 단호박 샐러드(곡물 or 파스타) +단백질쿠키(더블초코 or 피넛버터) 택 1 +제로아이스티(복숭아 or 자몽) 택 1 = 정가 10,300원 할인가 7,300원<br><br>두부 샐러드 세트 (771.8 kcal, 단백질: 38.0 g, 탄수화물: 116.7 g, 지방: 21.7 g / 파스타 기준)<br>• 두부 샐러드(곡물 or 파스타) +단백질쿠키(더블초코 or 피넛버터) 택 1 +제로아이스티(복숭아 or 자몽) 택 1 = 정가 10,400원 할인가 7400원<br><br>콥 샐러드 세트 (803.0 kcal, 단백질: 33.9 g, 탄수화물: 114.8 g, 지방: 25.9 g / 파스타 기준)<br>• 콥 샐러드(곡물 or 파스타) +단백질쿠키(더블초코 or 피넛버터) 택 1 +제로아이스티(복숭아 or 자몽) 택 1 = 정가 10,600원 할인가 7,600원<br><br>밸런스 세트 (776.3 kcal, 단백질: 36.2 g, 탄수화물: 129.8 g, 지방: 15.8 g / 파스타 기준)<br>• 밸런스 샐러드(곡물 or 파스타) +단백질쿠키(더블초코 or 피넛버터) 택 1 +제로아이스티(복숭아 or 자몽) 택 1 = 정가 11,500원 할인가 8,500원<br><br>단백질 쿠키<br>• 더블초코 : (175kcal, 단백질 : 7g, 탄수화물 : 20g, 지방 : 8g)<br>• 피넛버터 : (165kcal, 단백질 : 9g, 탄수화물 : 20g, 지방 : 6g)<br><br>📍 위치: [대전광역시 유성구 대학로 291 N7-1, 1층 ]<br>📞 문의: [0507-1336-3599]<br>🛵 단체 주문 가능 (전화 문의)<br>건강하게, 맛있게, 부담 없이!<br>지금 바로 오샐러드에서 세트 할인 메뉴로 더욱 맛있는 한 끼를 즐겨보세요.💚<br><br>※ 본 영양성분은 샐러드 정량과 fatsecret 의 표기를 따랐으며 오차가 있을 수 있습니다.<img src=\"https://sparcs-newara.s3.amazonaws.com/files/2_sjYcz0g.png\" width=\"500\" data-attachment=\"160726\"><img src=\"https://sparcs-newara.s3.amazonaws.com/files/3_AWZYwn6.png\" width=\"500\" data-attachment=\"160729\"><img src=\"https://sparcs-newara.s3.amazonaws.com/files/4_1jTOofj.jpg\" width=\"500\" data-attachment=\"160727\"><img src=\"https://sparcs-newara.s3.amazonaws.com/files/5_WvjnOsH.jpg\" width=\"500\" data-attachment=\"160728\"></p>
    """
        HTMLView(
            htmlString = contentString,
            onContentHeightChanged = { newHeight ->
                htmlHeight = newHeight
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(htmlHeight.dp)
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
        color = MaterialTheme.colorScheme.lightGray0,
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
            .background(MaterialTheme.colorScheme.surface)
            .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ){
            Box(Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.grayF8)
                .weight(1f)
                .padding(4.dp),

            ){
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    textStyle = MaterialTheme.typography.titleLarge.copy(color = MaterialTheme.colorScheme.onSurface),
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                    decorationBox = { innerTextField ->
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            if (value.isEmpty()) {
                                Text(
                                    text = stringResource(R.string.reply_as_anonymous),
                                    color = MaterialTheme.colorScheme.grayBB,
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
                tint = MaterialTheme.colorScheme.primary
            )

        }
    }
}

@Composable
@Preview
private fun Preview(){
    Theme { PostView(rememberNavController()) }
}
