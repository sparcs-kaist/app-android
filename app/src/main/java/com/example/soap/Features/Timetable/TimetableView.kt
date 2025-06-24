package com.example.soap.Features.Timetable

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.soap.Features.Timetable.Components.CompactTimetableSelector
import com.example.soap.Features.Timetable.Components.TimetableSummary

@Composable
fun TimetableView(navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF2F2F6))
    ) {
        Column(modifier = Modifier.padding(vertical = 8.dp)){
            CompactTimetableSelector()

            TimetableSummary()
        }

    }
}

@Composable
@Preview
private fun Preview(){
    TimetableView(rememberNavController())
}