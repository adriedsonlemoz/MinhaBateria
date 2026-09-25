package com.minhabateria.app

import android.app.Activity
import android.os.Bundle
import android.view.WindowManager
import com.minhabateria.app.battery.BatteryMonitor
import com.minhabateria.app.battery.BatteryStatusReader
import com.minhabateria.app.battery.ChargingSession
import com.minhabateria.app.ui.MainScreenRenderer

class MainActivity : Activity() {
    private lateinit var monitor: BatteryMonitor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        val renderer = MainScreenRenderer(this)
        monitor = BatteryMonitor(
            reader = BatteryStatusReader(this),
            session = ChargingSession(),
            onUpdate = renderer::render
        )
    }

    override fun onStart() {
        super.onStart()
        monitor.start()
    }

    override fun onStop() {
        monitor.stop()
        super.onStop()
    }
}
