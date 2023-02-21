package com.livelike.demo.widget

import android.os.Handler
import android.os.Message
import android.view.Choreographer
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.facebook.react.bridge.LifecycleEventListener
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.uimanager.ThemedReactContext
import com.google.gson.JsonParser
import com.livelike.demo.LiveLikeManager
import com.livelike.engagementsdk.LiveLikeContentSession
import com.livelike.engagementsdk.LiveLikeEngagementTheme
import com.livelike.engagementsdk.widget.timeline.WidgetTimeLineViewModel
import com.livelike.engagementsdk.widget.timeline.WidgetsTimeLineView
import com.reactnativewrapperexample_1.R
import java.io.IOException
import java.io.InputStream
import com.livelike.engagementsdk.core.services.network.Result;
import com.livelike.engagementsdk.widget.timeline.IntractableWidgetTimelineViewModel

class LiveLikeWidgetTimelineView(
    val context: ThemedReactContext,
    val applicationContext: ReactApplicationContext
) : FrameLayout(context), LifecycleEventListener {

    var re_render = true
    var contentSession: LiveLikeContentSession? = null
    private lateinit var containerGrp: ViewGroup
    var fallback: Choreographer.FrameCallback;

    init {
        this.applicationContext.addLifecycleEventListener(this)
        this.fallback = Choreographer.FrameCallback() {
            manuallyLayoutChildren();
            viewTreeObserver.dispatchOnGlobalLayout();
            if(re_render) {
                Choreographer.getInstance().postFrameCallbackDelayed(this!!.fallback,1500)
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
            val timeLineView = LiveLikeManager.engagementSDK?.let { sdk ->
                WidgetsTimeLineView(
                    applicationContext,
                    timeLineViewModel,
                    sdk
                )
            }

            timeLineView?.setSeparator(ContextCompat.getDrawable(applicationContext, R.drawable.white_separator))
            try {
                val inputStream: InputStream = applicationContext.assets.open("livelike_styles.json")
                val size: Int = inputStream.available()
                val buffer = ByteArray(size)
                inputStream.read(buffer)
                val theme = String(buffer)
                val result =
                    LiveLikeEngagementTheme.instanceFrom(JsonParser.parseString(theme).asJsonObject)
                if (result is Result.Success) {
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
        this.contentSession = null
    }

    fun updateContentSession(contentSession: LiveLikeContentSession) {
        this.contentSession = contentSession
        attachViewAndSession()
    }
}