package me.lucky.vibe

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
import java.util.regex.Pattern

import me.lucky.vibe.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var prefs: Preferences
    private val vibePatternRegex by lazy { Pattern.compile("^\\d+(,\\d+)*$") }

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
            vibePattern.editText?.setText(prefs.vibePattern)
        }
    }

    private fun setup() {
        binding.apply {
            filterPackageNames.setOnCheckedChangeListener { _, isChecked ->
                prefs.isFilterPackageNames = isChecked
            }
            vibePattern.editText?.doAfterTextChanged { text ->
                val str = text.toString()
                if (vibePatternRegex.matcher(str).matches())
                    prefs.vibePattern = str
                else
                    vibePattern.editText?.error = getString(R.string.vibe_pattern_error)
            }
            gotoButton.setOnClickListener {
                startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
            }
        }
    }
}
