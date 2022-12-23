package com.livelike.demo

import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.ReactContext
import com.facebook.react.bridge.WritableMap
import com.facebook.react.modules.core.DeviceEventManagerModule
import java.util.concurrent.atomic.AtomicInteger

object DebugLog {
    lateinit var context: ReactContext
    private val debugLogSequence = AtomicInteger(0)
    var isEnabled = false

    fun log(message: String, data: WritableMap? = null) {
        if (!isEnabled) return
        val timestamp = System.currentTimeMillis()
        context
            .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter::class.java)
            .emit("onDebugLog",
                Arguments.createMap().apply {
                    putString("timestamp", "$timestamp")
                    putString("sequence", "${timestamp}android${debugLogSequence.addAndGet(1)}")
                    putString("message", message)
                    putMap("data", data)
                }
            )
    }

}