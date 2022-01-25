package me.lucky.vibe

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.appcompat.app.AppCompatActivity

import me.lucky.vibe.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var prefs: Preferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        init()
        setup()
    }

    private fun init() {
        prefs = Preferences(this)
        binding.apply {
            filterPackageNames.isChecked = prefs.isFilterPackageNames
        }
    }

    private fun setup() {
        binding.apply {
            filterPackageNames.setOnCheckedChangeListener { _, isChecked ->
                prefs.isFilterPackageNames = isChecked
            }
            gotoButton.setOnClickListener {
                startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
            }
        }
    }
}
