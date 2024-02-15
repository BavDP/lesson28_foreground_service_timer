package com.example.foregroundservicetimer

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.foregroundservicetimer.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var _binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(_binding.root)

        _binding.startBtn.setOnClickListener {
            TimerService.startForegroundService(this)
        }

        _binding.stopBtn.setOnClickListener {
            TimerService.stopForegroundService(this)
        }
    }
}