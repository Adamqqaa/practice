package ci.nsu.mobile.main

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import ci.nsu.mobile.main.ui.main.MainFragment

private val colorsMap = mapOf(
    "Red" to ColorItem.Red,
    "Yellow" to ColorItem.Yellow,
    "Green" to ColorItem.Green,
    "Orange" to ColorItem.Orange,
    "Indigo" to ColorItem.Indigo,
    "Blue" to ColorItem.Blue,
    "Violet" to ColorItem.Violet
)

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.container, MainFragment.newInstance())
                .commitNow()
        }
    }
}