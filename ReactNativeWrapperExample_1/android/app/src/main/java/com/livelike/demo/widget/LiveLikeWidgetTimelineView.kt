package com.livelike.demo.widget

import android.view.Choreographer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.facebook.react.bridge.LifecycleEventListener
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContext
import com.facebook.react.uimanager.ThemedReactContext
import com.facebook.react.uimanager.events.RCTEventEmitter
import com.google.gson.JsonParser
import com.livelike.demo.LiveLikeManager
import com.livelike.engagementsdk.LiveLikeContentSession
import com.livelike.engagementsdk.LiveLikeEngagementTheme
import com.livelike.engagementsdk.LiveLikeWidget
import com.livelike.engagementsdk.chat.data.remote.LiveLikePagination
import com.livelike.engagementsdk.publicapis.LiveLikeCallback
import com.livelike.engagementsdk.widget.timeline.IntractableWidgetTimelineViewModel
import com.livelike.engagementsdk.widget.timeline.WidgetTimeLineViewModel
import com.livelike.engagementsdk.widget.timeline.WidgetsTimeLineView
import com.reactnativewrapperexample_1.R
import java.io.IOException
import java.io.InputStream

class LiveLikeWidgetTimelineView(
    val context: ThemedReactContext,
    val applicationContext: ReactApplicationContext
) : FrameLayout(context), LifecycleEventListener {

    var re_render = true
    var contentSession: LiveLikeContentSession? = null
    private lateinit var containerGrp: ViewGroup
    var fallback: Choreographer.FrameCallback;

    init {
        this.context.addLifecycleEventListener(this)
        this.fallback = Choreographer.FrameCallback() {
            manuallyLayoutChildren();
            viewTreeObserver.dispatchOnGlobalLayout();
            if(re_render) {
                Choreographer.getInstance().postFrameCallbackDelayed(this!!.fallback,800)
            }
        }
        Choreographer.getInstance().postFrameCallback(fallback)
        createView()
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

    private fun attachViewAndSession() {
        var timeLineViewModel: WidgetTimeLineViewModel
        contentSession.let { session ->
            timeLineViewModel = IntractableWidgetTimelineViewModel(session!!)
            timeLineViewModel.decideWidgetInteractivity
            val timeLineView = LiveLikeManager.engagementSDK?.let { sdk ->
                WidgetsTimeLineView(
                    applicationContext,
                    timeLineViewModel,
                    sdk
                )
            }
            timeLineView?.findViewById<View>(R.id.timeline_view)?.findViewById<View>(R.id.timeline_rv)?.findViewById<View>(R.id.timeline_snap_live)?.layoutParams?.height  = 0
            //timeLineView?.binding_field.timelineSnapLive?.layoutParams?.height  = 0
            //timeLineView?.findViewById<View>(R.id.timeline_snap_live)?.layoutParams?.height  = 0
            timeLineView?.setSeparator(ContextCompat.getDrawable(applicationContext, R.drawable.white_separator))
            try {
                val inputStream: InputStream = applicationContext.assets.open("livelike_styles.json")
                val size: Int = inputStream.available()
                val buffer = ByteArray(size)
                inputStream.read(buffer)
                val theme = String(buffer)
                val result =
                    LiveLikeEngagementTheme.instanceFrom(JsonParser.parseString(theme).asJsonObject)
                if (result is com.livelike.utils.Result.Success) {
                    timeLineView?.applyTheme(result.data)
                } else {
                    Toast.makeText(
                        context,
                        "Unable to get the theme json",
                        Toast.LENGTH_LONG
                    ).show()
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
            containerGrp.addView(timeLineView)
        }

    }

    private fun createView() {
        val parentView = LayoutInflater.from(context).inflate(R.layout.widget_timeline_view, null) as FrameLayout;
        addView(parentView)
        containerGrp = parentView.findViewById(R.id.container_view)
    }

    override fun onHostResume() {
        this.contentSession?.resume()
    }

    override fun onHostPause() {
        this.contentSession?.pause()
    }

    override fun onHostDestroy() {
        re_render = false
        Choreographer.getInstance().removeFrameCallback { this.fallback }
        contentSession?.widgetStream?.unsubscribe(this)
        this.contentSession = null
    }

    fun updateContentSession(contentSession: LiveLikeContentSession) {
        this.contentSession = contentSession
        attachViewAndSession()
        checkWidgetCount()
    }

    fun checkWidgetCount() {
        this.contentSession?.getPublishedWidgets(LiveLikePagination.FIRST, object : LiveLikeCallback<List<LiveLikeWidget>>() {
            override fun onResponse(result: List<LiveLikeWidget>?, error: String?) {
                if(result == null || result.isEmpty()){
                    fireShowEmptyListEvent()
                }
            }
        })

        contentSession?.widgetStream?.subscribe(this) {
            it?.let {
                fireHideEmptyListEvent()
                contentSession?.widgetStream?.unsubscribe(this)
            }
        }
    }

    fun fireShowEmptyListEvent(){
        val reactContext = this.getContext() as ReactContext;
        reactContext.getJSModule(RCTEventEmitter::class.java)
            .receiveEvent(this.getId(), LiveLikeWidgetViewManager.EVENT_SHOW_EMPTY_TIMELINE, null)
    }

    fun fireHideEmptyListEvent(){
        val reactContext = this.getContext() as ReactContext;
        reactContext.getJSModule(RCTEventEmitter::class.java)
            .receiveEvent(this.getId(), LiveLikeWidgetViewManager.EVENT_HIDE_EMPTY_TIMELINE, null)
    }
}