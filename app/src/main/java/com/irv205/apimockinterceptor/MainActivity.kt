package com.irv205.apimockinterceptor

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.irv205.apimockinterceptor.ui.navigation.AppNavHost
import com.irv205.apimockinterceptor.ui.theme.ApiMockInterceptorTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ApiMockInterceptorTheme {
                AppNavHost()
            }
        }
    }
}
