package at.vulkanspirit.lcarstv

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import at.vulkanspirit.lcarstv.ui.LcarsApp

class MainActivity : ComponentActivity() {
    private val vm: LcarsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.getInsetsController(window, window.decorView).apply {
            hide(WindowInsetsCompat.Type.systemBars())
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
        setContent {
            LcarsApp(
                vm = vm,
                isHomeMode = { intent?.hasCategory(Intent.CATEGORY_HOME) == true },
                onExit = { finish() },
            )
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        if (intent.hasCategory(Intent.CATEGORY_HOME)) vm.onHomeIntent()
    }

    override fun onResume() {
        super.onResume()
        vm.refreshSystem()
    }

    override fun onStop() {
        super.onStop()
        vm.onBackgrounded()
    }
}
