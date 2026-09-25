package com.minhabateria.app

import android.app.Activity
import android.os.Bundle
import com.minhabateria.app.monitoring.LocalBatteryMonitorHub
import com.minhabateria.app.monitoring.MonitoringState
import com.minhabateria.app.monitoring.MonitoringStateStore
import com.minhabateria.app.source.SourceProfileStore
import com.minhabateria.app.ui.BottomTab
import com.minhabateria.app.ui.BottomTabsBinder
import com.minhabateria.app.ui.SessionScreenRenderer
import com.minhabateria.app.ui.SystemBars

class SessionActivity : Activity() {
    private lateinit var renderer: SessionScreenRenderer
    private lateinit var sourceStore: SourceProfileStore

    private val stateListener: (MonitoringState) -> Unit = { state ->
        runOnUiThread {
            renderer.render(state.session)
            LocalBatteryMonitorHub.sync(this)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_session)
        SystemBars.apply(this, findViewById(R.id.sessionRoot))
        renderer = SessionScreenRenderer(this)
        sourceStore = SourceProfileStore(this)

        BottomTabsBinder(this).bind(
            active = BottomTab.SESSION,
            openNow = { finish() },
            openSession = {}
        )
        renderer.renderSource(sourceStore.getProfile())
        renderer.render(MonitoringStateStore.current().session)
    }

    override fun onStart() {
        super.onStart()
        MonitoringStateStore.addListener(stateListener)
        LocalBatteryMonitorHub.attach(this, this)
    }

    override fun onResume() {
        super.onResume()
        renderer.renderSource(sourceStore.getProfile())
        renderer.render(MonitoringStateStore.current().session)
    }

    override fun onStop() {
        LocalBatteryMonitorHub.detach(this)
        MonitoringStateStore.removeListener(stateListener)
        super.onStop()
    }
}
