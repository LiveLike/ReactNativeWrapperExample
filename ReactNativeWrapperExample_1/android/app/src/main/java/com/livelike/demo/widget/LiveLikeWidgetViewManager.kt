package com.livelike.demo.widget

import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReadableArray
import com.facebook.react.common.MapBuilder
import com.facebook.react.uimanager.ThemedReactContext
import com.facebook.react.uimanager.ViewGroupManager
import com.livelike.demo.LiveLikeManager


class LiveLikeWidgetViewManager(val applicationContext: ReactApplicationContext) :
    ViewGroupManager<LiveLikeWidgetTimelineLandscapeView>() {


    companion object {
        const val EVENT_WIDGET_READY = "widgetReady"
    }

    val REACT_CLASS = "LiveLikeWidgetView"
    val SET_PROGRAM = 1
    val SHOW_WIDGET = 2
    val HIDE_WIDGET = 3


    override fun getName(): String {
        return REACT_CLASS
    }

    /**
     * Map the "create" command to an integer
     */
    override fun getCommandsMap(): Map<String, Int>? {
        return MapBuilder.of("setProgram", SET_PROGRAM,"showWidget",SHOW_WIDGET,"hideWidget", HIDE_WIDGET)
    }

    override fun getExportedCustomDirectEventTypeConstants(): MutableMap<String, Any>? {
        var map = HashMap<String, Any>()
        map.put(EVENT_WIDGET_READY, MapBuilder.of("registrationName", "onReady"));
        return map;
    }
    /**
     * Handle "create" command (called from JS) and call createFragment method
     */
    override fun receiveCommand(
        root: LiveLikeWidgetTimelineLandscapeView,
        commandId: String,
        args: ReadableArray?
    ) {
        super.receiveCommand(root, commandId, args)
        val reactNativeViewId = args!!.getInt(0)
        val commandIdInt = commandId.toInt()
        when (commandIdInt) {
            SET_PROGRAM -> setProgramIdFrmArgs(root, args)
            HIDE_WIDGET -> hideWidget(root)
            SHOW_WIDGET -> showWidget(root)
            else -> {}
        }
    }

    private fun hideWidget(view:LiveLikeWidgetTimelineLandscapeView) {
        view.hideWidget()
    }

    private fun showWidget(view:LiveLikeWidgetTimelineLandscapeView) {
        view.displayLastWidget()
    }

    override fun createViewInstance(reactContext: ThemedReactContext): LiveLikeWidgetTimelineLandscapeView {
        return LiveLikeWidgetTimelineLandscapeView(reactContext, applicationContext);
    }

   fun setProgramIdFrmArgs(view: LiveLikeWidgetTimelineLandscapeView,args: ReadableArray?) {
        val session = args?.getString(1)?.let { LiveLikeManager.getContentSession(it) }
        session?.let {
            val configType = args?.getString(2)
            view.updateContentSession(it,configType)
        }
    }

    override fun onDropViewInstance(view: LiveLikeWidgetTimelineLandscapeView) {
        view.onHostDestroy()
        super.onDropViewInstance(view)
    }
}