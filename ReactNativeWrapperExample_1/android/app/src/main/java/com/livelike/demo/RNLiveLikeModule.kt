package com.livelike.demo

import android.app.Application
import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod
import com.livelike.engagementsdk.chat.ChatRoomInfo
import com.livelike.engagementsdk.publicapis.LiveLikeCallback
import com.livelike.engagementsdk.publicapis.LiveLikeUserApi

class RNLiveLikeModule(
    val application: Application,
    private val applicationContext: ReactApplicationContext
) : ReactContextBaseJavaModule(applicationContext) {

    override fun getName(): String {
        return "LiveLikeModule"
    }

    @ReactMethod
    fun initializeSDK(clientId: String, promise: Promise) {
        LiveLikeManager.initializeSDK(application, clientId, promise)
    }

    @ReactMethod
    fun subscribeUserStream(key: String, promise: Promise) {
        LiveLikeManager.subscribeUserStream(key, promise)
    }

    @ReactMethod
    fun destroyContentSession(promise: Promise) {
        LiveLikeManager.destroyContentSession()
        promise.resolve(true)
    }
}