package com.example.soap.Features.Timetable.Components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.soap.R
import com.example.soap.Utilities.Extensions.noRippleClickable
import com.example.soap.ui.theme.SoapTheme

@Composable
fun CompactTimetableSelector() {
    Row(
        modifier = Modifier
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.shadow(4.dp, RoundedCornerShape(25.dp))){
            
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(25.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {

                Icon(
                    painter = painterResource(R.drawable.arrow_back_ios),
                    contentDescription = "Select Previous Semester",
                    tint = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .noRippleClickable {  }
                )


                Text(
                    text = "Autumn 2024",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold
                )

                Icon(
                    painter = painterResource(R.drawable.arrow_forward_ios),
                    contentDescription = "Select Next Semester",
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .noRippleClickable {  }
                )

            }
        }
        Spacer(Modifier.weight(1f))

        Box(Modifier.shadow(4.dp, RoundedCornerShape(25.dp))){
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(25.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.my_table),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )

                Icon(
                    painter = painterResource(R.drawable.more_horiz),
                    contentDescription = "Select Next Semester",
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .noRippleClickable {  }
                )
            }
        }
    }
}

@Composable
@Preview
private fun Preview(){
    SoapTheme { CompactTimetableSelector() }
}