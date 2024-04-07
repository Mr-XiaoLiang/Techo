package com.lollipop.punch2.activity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.lollipop.insets.WindowInsetsHelper
import com.lollipop.punch2.ui.theme.TechoTheme
import com.lollipop.punch2.utils.ThemeHelper
import com.lollipop.punch2.utils.liveTheme

class AddFlagActivity : ComponentActivity(), ThemeHelper.OnThemeChangeCallback {

    private val themeHelper = ThemeHelper.createWith(this, this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val theme = themeHelper.liveTheme()
            TechoTheme(theme) {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Greeting(
                        name = "Android",
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }

    override fun onThemeChanged(theme: ColorScheme, isDark: Boolean) {
        WindowInsetsHelper.getController(this).apply {
            isAppearanceLightStatusBars = !isDark
            isAppearanceLightNavigationBars = !isDark
        }
    }

}


@Composable
private fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
private fun GreetingPreview() {
    TechoTheme {
        Greeting("Android")
    }
}