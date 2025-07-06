package com.example.soap.Features.TaxiPreview.Components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.soap.R
import com.example.soap.ui.theme.SoapTheme
import com.example.soap.ui.theme.soapColors

@Composable
fun InfoRow(
    label: String,
    value: String,
    labelColor: Color = MaterialTheme.soapColors.grayBB,
    valueColor: Color = MaterialTheme.soapColors.onSurface,
    trailingIcon: Int? = null
) {
    Row(verticalAlignment = Alignment.CenterVertically){
        Text(
            text = label,
            color = labelColor,
            style = MaterialTheme.typography.bodySmall
        )

        Spacer(Modifier.padding(4.dp))

        Row(verticalAlignment = Alignment.CenterVertically){

            Text(
                text = value,
                color = valueColor,
                style = MaterialTheme.typography.bodySmall
            )

            trailingIcon?.let { iconName ->
                Image(
                    painter = painterResource(trailingIcon),
                    contentDescription = null,
                    colorFilter = ColorFilter.tint(MaterialTheme.soapColors.darkGray),
                    modifier = Modifier.size(15.dp)
                )
            }
        }
    }

}

@Composable
@Preview
private fun Preview() {
    SoapTheme { InfoRow(label = "label", value = "value",  trailingIcon= R.drawable.arrow_forward_ios) }
}