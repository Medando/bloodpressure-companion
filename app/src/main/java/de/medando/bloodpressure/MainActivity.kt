package de.medando.bloodpressure

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import dagger.hilt.android.AndroidEntryPoint
import de.medando.bloodpressure.ui.BloodPressureApp
import de.medando.bloodpressure.ui.disclaimer.DisclaimerGate
import de.medando.bloodpressure.ui.theme.BloodPressureTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            BloodPressureTheme {
                DisclaimerGate {
                    BloodPressureApp()
                }
            }
        }
    }
}
