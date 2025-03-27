package com.nate.autofinance.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppLargeCenteredTopBar(text : String) {
    CenterAlignedTopAppBar(
        modifier = Modifier.padding(0.dp, 16.dp, 0.dp, 0.dp),
        colors = TopAppBarColors(
            containerColor = MaterialTheme.colorScheme.background,
            scrolledContainerColor = Color.Transparent,
            navigationIconContentColor = Color.Transparent,
            titleContentColor = MaterialTheme.colorScheme.primary,
            actionIconContentColor = Color.Transparent,
        ),
        title = {
            Text(
                text = text,
                fontSize = 28.sp,
                style = TextStyle(fontWeight = FontWeight.Bold),
            )
        })
}

@Preview
@Composable
fun AppLargeTopBarPreview() {
    AppLargeCenteredTopBar("Welcome to AutoFinance")
}