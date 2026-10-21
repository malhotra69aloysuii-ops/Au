package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.example.ui.navigation.EpicNavGraph
import com.example.ui.theme.EpicLibraryTheme
import com.example.ui.theme.EpicVoid

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val app = application as EpicLibraryApp
        val container = app.container

        setContent {
            EpicLibraryTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = EpicVoid
                ) {
                    val navController = rememberNavController()
                    EpicNavGraph(
                        navController = navController,
                        container = container
                    )
                }
            }
        }
    }
}
