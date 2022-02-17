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
    private lateinit var vibrator: Vibrator
    private val vibePatternRegex by lazy { Pattern.compile("^\\d+(,\\d+)*$") }
    private val zeroVibePatternRegex by lazy { Pattern.compile("^0+(,0+)*$") }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        init()
        setup()
    }

    private fun init() {
        prefs = Preferences(this)
        vibrator = Vibrator(this)
        binding.apply {
            vibeAtStart.isChecked = prefs.isVibeAtStart
            vibeAtEnd.isChecked = prefs.isVibeAtEnd
            filterPackageNames.isChecked = prefs.isFilterPackageNames
            vibePattern.editText?.setText(prefs.vibePattern)
        }
    }

    private fun setup() {
        binding.apply {
            vibeAtStart.setOnCheckedChangeListener { _, isChecked ->
                prefs.isVibeAtStart = isChecked
            }
            vibeAtEnd.setOnCheckedChangeListener { _, isChecked ->
                prefs.isVibeAtEnd = isChecked
            }
            filterPackageNames.setOnCheckedChangeListener { _, isChecked ->
                prefs.isFilterPackageNames = isChecked
            }
            vibePattern.editText?.doAfterTextChanged {
                val str = it?.toString() ?: ""
                if (vibePatternRegex.matcher(str).matches() &&
                    !zeroVibePatternRegex.matcher(str).matches())
                {
                    prefs.vibePattern = str
                    vibePattern.error = null
                } else {
                    vibePattern.error = getString(R.string.vibe_pattern_error)
                }
            }
            vibePattern.setEndIconOnClickListener {
                if (vibePattern.error == null) vibrator.vibrate()
            }
            gotoButton.setOnClickListener {
                startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
            }
        }
    }
}
