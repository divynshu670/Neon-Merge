package com.example.neonmerge

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.example.neonmerge.ui.NeonMergeScreen
import com.example.neonmerge.ui.theme.NeonMergeTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NeonMergeTheme {
                NeonMergeScreen()
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF090D16)
@Composable
fun NeonMergeScreenPreview() {
    NeonMergeTheme {
        NeonMergeScreen()
    }
}