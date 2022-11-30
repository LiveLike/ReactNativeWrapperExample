package com.livelike.demo.widget

import android.util.Log
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReadableArray
import com.facebook.react.common.MapBuilder
import com.facebook.react.uimanager.ThemedReactContext
import com.facebook.react.uimanager.ViewGroupManager
import com.facebook.react.uimanager.annotations.ReactProp
import com.livelike.demo.LiveLikeManager
import java.util.*


class LiveLikeWidgetViewManager(val applicationContext: ReactApplicationContext) :
    ViewGroupManager<LiveLikeWidgetView>() {

    val REACT_CLASS = "LiveLikeWidgetView"
    val SET_PROGRAM = 1

    companion object {
        const val EVENT_WIDGET_SHOWN = "widgetShown"
        const val EVENT_WIDGET_HIDDEN = "widgetHidden"
        const val EVENT_ANALYTICS = "analytics"
    }

    override fun getName(): String {
        return REACT_CLASS
    }

    /**
     * Map the "create" command to an integer
     */
    override fun getCommandsMap(): Map<String, Int>? {
        return MapBuilder.of("setProgram", SET_PROGRAM)
    }

    /**
     * Handle "create" command (called from JS) and call createFragment method
     */
    override fun receiveCommand(
        root: LiveLikeWidgetView,
        commandId: String,
        args: ReadableArray?
    ) {
        super.receiveCommand(root, commandId, args)
        val reactNativeViewId = args!!.getInt(0)
        val commandIdInt = commandId.toInt()
        when (commandIdInt) {
            SET_PROGRAM -> setProgramIdFrmArgs(root, args)
            else -> {}
        }
    }

    override fun createViewInstance(reactContext: ThemedReactContext): LiveLikeWidgetView {
        return LiveLikeWidgetView(reactContext, applicationContext);
    }

   fun setProgramIdFrmArgs(view: LiveLikeWidgetView,args: ReadableArray?) {
        val session = args?.getString(1)?.let { LiveLikeManager.getContentSession(it) }
        session?.let {
            view.updateContentSession(it)
        }

    }

    @ReactProp(name = "programId")
    fun setProgramId(view: LiveLikeWidgetView, programId: String) {
        val session = LiveLikeManager.getContentSession(programId)
        session?.let {
            view.updateContentSession(it)
        }

    }


    override fun onDropViewInstance(view: LiveLikeWidgetView) {
        super.onDropViewInstance(view)
    }

    override fun getExportedCustomDirectEventTypeConstants(): Map<String, Any> {
        var map = HashMap<String, Any>()
        map.put(EVENT_WIDGET_SHOWN, MapBuilder.of("registrationName", "onWidgetShown"));
        map.put(EVENT_WIDGET_HIDDEN, MapBuilder.of("registrationName", "onWidgetHidden"));
        map.put(EVENT_ANALYTICS, MapBuilder.of("registrationName", "onEvent"));
        return map;
    }
}