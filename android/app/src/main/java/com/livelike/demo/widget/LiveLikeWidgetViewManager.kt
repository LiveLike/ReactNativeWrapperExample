package com.livelike.demo.widget

import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.uimanager.ThemedReactContext
import com.facebook.react.uimanager.ViewGroupManager
import com.reactnativelivelike.LiveLikeWidgetView


class LiveLikeWidgetViewManager(val applicationContext: ReactApplicationContext) :
    ViewGroupManager<LiveLikeWidgetView>() {

    val REACT_CLASS = "LiveLikeWidgetView"

    companion object {
        const val EVENT_WIDGET_SHOWN = "widgetShown"
        const val EVENT_WIDGET_HIDDEN = "widgetHidden"
        const val EVENT_ANALYTICS = "analytics"
    }

    override fun getName(): String {
        return REACT_CLASS
    }

    override fun createViewInstance(reactContext: ThemedReactContext): LiveLikeWidgetView {
        val view = LiveLikeWidgetView(reactContext, applicationContext);
        return view
    }
}