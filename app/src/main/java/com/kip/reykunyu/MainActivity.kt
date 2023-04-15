package com.kip.reykunyu

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.kip.reykunyu.ui.ReykunyuContainer
import com.kip.reykunyu.ui.theme.ReykunyuTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ReykunyuTheme {
                ReykunyuContainer()
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    ReykunyuTheme {
        ReykunyuContainer()
    }
}