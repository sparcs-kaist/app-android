package org.sparcs.App.Features.TaxiPreview.Components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.sparcs.App.theme.ui.Theme
import org.sparcs.App.theme.ui.darkGray
import org.sparcs.App.theme.ui.grayBB

@Composable
fun InfoRow(
    label: String,
    value: String,
    labelColor: Color = MaterialTheme.colorScheme.grayBB,
    valueColor: Color = MaterialTheme.colorScheme.onSurface,
    trailingIcon: ImageVector? = null
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween){
        Text(
            text = label,
            color = labelColor,
            style = MaterialTheme.typography.bodyLarge
        )

        Spacer(Modifier.weight(1f))

        Row(verticalAlignment = Alignment.CenterVertically){

            Text(
                text = value,
                color = valueColor,
                style = MaterialTheme.typography.bodyLarge
            )

            trailingIcon?.let { iconName ->
                Image(
                    imageVector = trailingIcon,
                    contentDescription = null,
                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.darkGray),
                    modifier = Modifier.size(15.dp)
                )
            }
        }
    }

}

@Composable
@Preview
private fun Preview() {
    Theme { InfoRow(label = "label", value = "value",  trailingIcon= Icons.Default.Add) }
}