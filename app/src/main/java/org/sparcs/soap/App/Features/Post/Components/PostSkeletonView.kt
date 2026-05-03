package org.sparcs.soap.App.Features.Post.Components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun PostViewSkeleton() {
    LazyColumn(
        modifier = Modifier
            .padding(16.dp)
    ) {
        item { HeaderSkeleton() }
        item { ContentSkeleton() }
        item { FooterSkeleton() }
        items(2) { CommentSkeleton() }
    }
}

@Composable
fun HeaderSkeleton() {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Box(
            Modifier
                .height(14.dp)
                .width(80.dp)
                .background(
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                    RoundedCornerShape(4.dp)
                )
        )
        Box(
            Modifier
                .height(14.dp)
                .width(60.dp)
                .background(
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                    RoundedCornerShape(4.dp)
                )
        )
        Box(
            Modifier
                .height(22.dp)
                .fillMaxWidth(0.8f)
                .background(
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                    RoundedCornerShape(4.dp)
                )
        )

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(
                Modifier
                    .height(14.dp)
                    .width(80.dp)
                    .background(
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                        RoundedCornerShape(4.dp)
                    )
            )
            Box(
                Modifier
                    .height(14.dp)
                    .width(60.dp)
                    .background(
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                        RoundedCornerShape(4.dp)
                    )
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier
                    .size(28.dp)
                    .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f), CircleShape)
            )
            Spacer(Modifier.width(8.dp))
            Box(
                Modifier
                    .height(16.dp)
                    .width(100.dp)
                    .background(
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                        RoundedCornerShape(4.dp)
                    )
            )
        }

        HorizontalDivider()
    }
}

@Composable
private fun ContentSkeleton() {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        repeat(5) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(18.dp)
                    .background(
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                        RoundedCornerShape(4.dp)
                    )
            )
        }
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
fun FooterSkeleton() {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(3) {
            Box(
                Modifier
                    .size(32.dp)
                    .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f), CircleShape)
            )
            Spacer(Modifier.width(8.dp))
        }
        Spacer(Modifier.weight(1f))
        Box(
            Modifier
                .size(32.dp)
                .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f), CircleShape)
        )
    }
}

@Composable
fun CommentSkeleton() {
    Column(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier
                    .size(28.dp)
                    .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f), CircleShape)
            )
            Spacer(Modifier.width(8.dp))
            Box(
                Modifier
                    .height(16.dp)
                    .width(120.dp)
                    .background(
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                        RoundedCornerShape(4.dp)
                    )
            )
        }

        Spacer(Modifier.height(8.dp))

        repeat(2) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(16.dp)
                    .background(
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                        RoundedCornerShape(4.dp)
                    )
            )
            Spacer(Modifier.height(4.dp))
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewSkeleton() {
    PostViewSkeleton()
}
