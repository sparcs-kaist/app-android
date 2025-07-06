package com.example.soap.Features.Post.Components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.soap.R
import com.example.soap.ui.theme.SoapTheme
import com.example.soap.ui.theme.soapColors

@Composable
fun Comments(
   // image: Painter, AsyncImage처리
    comment: String
){
    Column(Modifier.padding(horizontal = 16.dp)){
        Row(verticalAlignment = Alignment.CenterVertically) {

            Image(//프로필
                painter = painterResource(R.drawable.ic_launcher_background),
                contentDescription = null,
                modifier = Modifier
                    .clip(CircleShape)
                    .size(20.dp)
            )

            Spacer(Modifier.padding(2.dp))

            Text(
                text = "Anonymous",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )

            Spacer(Modifier.padding(4.dp))

            Text(
                text = "22 May 17:44",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.soapColors.grayBB
            )

            Spacer(Modifier.weight(1f))

            Icon(
                painter = painterResource(R.drawable.more_horiz),
                contentDescription = "more",
                tint = MaterialTheme.soapColors.grayBB
            )
        }

        Text(
            text = comment,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier
                .padding(start = 22.dp)
                .padding(vertical = 4.dp)
        )

        Row(Modifier.padding(start = 22.dp)){
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.soapColors.background)
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                PostVoteButton()

                Spacer(Modifier.padding(4.dp))

                PostCommentButton()
            }
        }
    }



}



@Composable
@Preview
private fun Preview(){
    SoapTheme { Comments("댓글") }
}
