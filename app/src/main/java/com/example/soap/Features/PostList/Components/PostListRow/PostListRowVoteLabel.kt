package com.example.soap.Features.PostList.Components.PostListRow


import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.soap.R
import com.example.soap.ui.theme.SoapTheme
import com.example.soap.ui.theme.soapColors

@Composable
fun PostListRowVoteLabel(voteCount: Int){
    if(voteCount > 0){
        //up voted
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.soapColors.grayf8)
                .padding(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(R.drawable.icon_arrowup),
                contentDescription = null,
                colorFilter = ColorFilter.tint(Color(0xFFFF4500)),
                modifier = Modifier.size(15.dp)
            )
            Spacer(Modifier.padding(2.dp))

            Text(
                text = "$voteCount",
                color = Color(0xFFFF4500),
                maxLines = 1,
                style = MaterialTheme.typography.bodySmall
            )

        }
    }
    else if (voteCount < 0){
        //down voted
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.soapColors.grayf8)
                .padding(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(R.drawable.icon_arrowdown),
                contentDescription = null,
                colorFilter = ColorFilter.tint(Color(0xFF047DFF)),
                modifier = Modifier.size(15.dp)
            )
            Spacer(Modifier.padding(2.dp))

            Text(
                text = "$voteCount",
                color = Color(0xFF047DFF),
                maxLines = 1,
                style = MaterialTheme.typography.bodySmall
            )

        }
    }
}

@Composable
@Preview
private fun Preview(){
    SoapTheme {
        Column{
            PostListRowVoteLabel(20)
            PostListRowVoteLabel(-20)
        }
    }
}