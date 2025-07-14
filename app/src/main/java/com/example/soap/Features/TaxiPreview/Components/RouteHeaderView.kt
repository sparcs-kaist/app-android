package com.example.soap.Features.TaxiPreview.Components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.soap.R
import com.example.soap.ui.theme.SoapTheme
import com.example.soap.ui.theme.soapColors

@Composable
fun RouteHeaderView(
    source: String,
    destination: String
) {
    Row(verticalAlignment = Alignment.CenterVertically){
        Text(
            text = source,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold
        )
        Icon(
            painter = painterResource(R.drawable.arrow_forward_ios),
            contentDescription = null,
            modifier = Modifier.size(15.dp),
            tint = MaterialTheme.soapColors.darkGray
        )
        Text(
            text = destination,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
@Preview
private fun Preview() {
    SoapTheme { RouteHeaderView(source = "Seoul", destination = "Busan") }
}