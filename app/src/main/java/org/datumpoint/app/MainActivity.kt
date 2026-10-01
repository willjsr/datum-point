package org.datumpoint.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import org.datumpoint.app.ui.DatumPointApp
import org.datumpoint.app.ui.theme.DatumPointTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DatumPointTheme {
                DatumPointApp()
            }
        }
    }
}
