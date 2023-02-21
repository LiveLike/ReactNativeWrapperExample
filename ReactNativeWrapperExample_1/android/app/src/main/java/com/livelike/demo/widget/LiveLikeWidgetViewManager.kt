package com.livelike.demo.widget

import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReadableArray
import com.facebook.react.common.MapBuilder
import com.facebook.react.uimanager.ThemedReactContext
import com.facebook.react.uimanager.ViewGroupManager
import com.livelike.demo.LiveLikeManager


class LiveLikeWidgetViewManager(val applicationContext: ReactApplicationContext) :
    ViewGroupManager<LiveLikeWidgetTimelineView>() {

    val REACT_CLASS = "LiveLikeWidgetView"
    val SET_PROGRAM = 1
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
        root: LiveLikeWidgetTimelineView,
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

    override fun createViewInstance(reactContext: ThemedReactContext): LiveLikeWidgetTimelineView {
        return LiveLikeWidgetTimelineView(reactContext, applicationContext);
    }

   fun setProgramIdFrmArgs(view: LiveLikeWidgetTimelineView,args: ReadableArray?) {
        val session = args?.getString(1)?.let { LiveLikeManager.getContentSession(it) }
        session?.let {
            view.updateContentSession(it)
        }

    }

    override fun onDropViewInstance(view: LiveLikeWidgetTimelineView) {
        view.onHostDestroy()
        super.onDropViewInstance(view)
    }
}