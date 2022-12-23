package com.reactnativelivelike

import android.util.Log
import android.view.Choreographer
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import androidx.constraintlayout.widget.ConstraintLayout
import com.facebook.react.bridge.*
import com.facebook.react.modules.core.DeviceEventManagerModule
import com.facebook.react.uimanager.ThemedReactContext
import com.livelike.demo.DebugLog
import com.livelike.demo.LiveLikeManager
import com.livelike.demo.R

import com.livelike.engagementsdk.LiveLikeContentSession
import com.livelike.engagementsdk.LiveLikeWidget
import com.livelike.engagementsdk.WidgetListener
import com.livelike.engagementsdk.core.services.messaging.proxies.LiveLikeWidgetEntity
import com.livelike.engagementsdk.core.services.messaging.proxies.WidgetInterceptor
import com.livelike.engagementsdk.widget.LiveLikeWidgetViewFactory
import com.livelike.engagementsdk.widget.view.WidgetView
import com.livelike.engagementsdk.widget.widgetModel.*

class LiveLikeWidgetView(
    val context: ThemedReactContext,
    val applicationContext: ReactApplicationContext
) : LinearLayout(context), LifecycleEventListener {


    var contentSession: LiveLikeContentSession? = null
    lateinit var widgetView: WidgetView;
    //  var widgetDetails: LiveLikeWidget? = null
    var fallback: Choreographer.FrameCallback;
    private var renderWidget = false
    val debugLog = DebugLog

    init {
        debugLog.context = applicationContext
        LiveLikeManager.context.addLifecycleEventListener(this)
        this.fallback = Choreographer.FrameCallback() {
            manuallyLayoutChildren();
            viewTreeObserver.dispatchOnGlobalLayout();
            if(renderWidget) {
                Choreographer.getInstance().postFrameCallback(this!!.fallback)
            }
        }
        Choreographer.getInstance().postFrameCallback(fallback)
        createView()
        println("INITIALIZE")

    }

    private fun createView() {
        val parentView = LayoutInflater.from(context).inflate(R.layout.fc_widget_view, null) as ConstraintLayout;
        addView(parentView)
        widgetView = parentView.findViewById(R.id.widget_view);
    }

    override fun onHostResume() {}

    override fun onHostPause() {}

    override fun onHostDestroy() {
//    this.contentSession = null
    }

//    fun updateContentSession(contentSession: LiveLikeContentSession?) {
//        this.contentSession = contentSession;
//        println("UPDATE SESSION")
////    widgetView.setSession(contentSession)
//        subscribeWidgetStream()
//    }


    fun displayAskWidget(widgetDetails: LiveLikeWidget) {
        println("DISPLAY WIDGET")
        widgetDetails?.let {
            renderWidget = true
            this.widgetView.displayWidget(LiveLikeManager.engagementSDK, it)
//        Choreographer.getInstance().postFrameCallback(this.fallback)
        }
    }





    fun manuallyLayoutChildren() {
        for (i in 0 until getChildCount()) {
            var child = getChildAt(i);
            child.measure(
                MeasureSpec.makeMeasureSpec(getMeasuredWidth(), MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(getMeasuredHeight(), MeasureSpec.EXACTLY)
            );
            child.layout(0, 0, child.getMeasuredWidth(), child.getMeasuredHeight());
        }
    }

}
