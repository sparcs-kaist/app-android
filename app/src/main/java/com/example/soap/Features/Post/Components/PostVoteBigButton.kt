package com.example.soap.Features.Post.Components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.soap.R
import com.example.soap.ui.theme.SoapTheme
import com.example.soap.ui.theme.soapColors

@Composable
fun PostVoteBigButton(modifier: Modifier){
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.soapColors.gray0Border)
            .padding(vertical = 4.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(R.drawable.icon_arrowdown),
            contentDescription = null,
            tint = MaterialTheme.soapColors.gray64Button,
            modifier = Modifier.size(25.dp)
        )

        Spacer(Modifier.padding(4.dp))

        Text(
            text = "+13",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.soapColors.gray64Button
        )

        Spacer(Modifier.padding(4.dp))

        Icon(
            painter = painterResource(R.drawable.icon_arrowup),
            contentDescription = null,
            tint = MaterialTheme.soapColors.gray64Button,
            modifier = Modifier.size(25.dp)
        )
    }
}

@Composable
@Preview
private fun Preview(){
    SoapTheme { PostVoteBigButton(Modifier) }
}