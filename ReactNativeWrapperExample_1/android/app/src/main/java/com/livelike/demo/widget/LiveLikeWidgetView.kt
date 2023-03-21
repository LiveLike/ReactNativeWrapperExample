package com.livelike.demo.widget

import android.os.Handler
import android.os.Message
import android.util.Log
import android.view.Choreographer
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.widget.LinearLayoutCompat
import com.facebook.react.bridge.LifecycleEventListener
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.uimanager.ThemedReactContext
import com.google.gson.JsonParser
import com.livelike.demo.LiveLikeManager
import com.livelike.engagementsdk.LiveLikeContentSession
import com.livelike.engagementsdk.LiveLikeEngagementTheme
import com.livelike.engagementsdk.LiveLikeWidget
import com.livelike.engagementsdk.core.services.messaging.proxies.LiveLikeWidgetEntity
import com.livelike.engagementsdk.core.services.messaging.proxies.WidgetLifeCycleEventsListener
import com.livelike.engagementsdk.widget.view.WidgetView
import com.livelike.engagementsdk.widget.viewModel.WidgetStates
import com.reactnativewrapperexample_1.R
import java.io.IOException
import java.io.InputStream
import com.livelike.utils.Result

class LiveLikeWidgetView(
    val context: ThemedReactContext,
    val applicationContext: ReactApplicationContext
) : LinearLayout(context), LifecycleEventListener {


    var contentSession: LiveLikeContentSession? = null
    lateinit var widgetView: WidgetView;
    var fallback: Choreographer.FrameCallback;
    private var renderWidget = false

    init {
        this.applicationContext.addLifecycleEventListener(this)
        this.fallback = Choreographer.FrameCallback() {
            manuallyLayoutChildren();
            viewTreeObserver.dispatchOnGlobalLayout();
            if(renderWidget) {
                Choreographer.getInstance().postFrameCallbackDelayed(this!!.fallback,1200)
            }
        }
        Choreographer.getInstance().postFrameCallback(fallback)
        createView()
    }

    private fun createView() {
        val parentView = LayoutInflater.from(context).inflate(R.layout.fc_widget_view, null) as LinearLayoutCompat;
        addView(parentView)
        widgetView = parentView.findViewById(R.id.widget_view)
        widgetView.widgetLifeCycleEventsListener = object : WidgetLifeCycleEventsListener() {
            override fun onUserInteract(widgetData: LiveLikeWidgetEntity) {
            }

            override fun onWidgetDismissed(widgetData: LiveLikeWidgetEntity) {
                renderWidget = false
            }

            override fun onWidgetInteractionCompleted(widgetData: LiveLikeWidgetEntity) {
            }

            override fun onWidgetPresented(widgetData: LiveLikeWidgetEntity) {
            }

            override fun onWidgetStateChange(
                state: WidgetStates,
                widgetData: LiveLikeWidgetEntity
            ) {

            }

        }
        try {
            val inputStream: InputStream = applicationContext.assets.open("livelike_styles.json")
            val size: Int = inputStream.available()
            val buffer = ByteArray(size)
            inputStream.read(buffer)
            val theme = String(buffer)
            val result =
                LiveLikeEngagementTheme.instanceFrom(JsonParser.parseString(theme).asJsonObject)
            if (result is Result.Success) {
                widgetView?.applyTheme(result.data)
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
    }

    override fun onHostResume() {
        this.contentSession?.resume()
    }

    override fun onHostPause() {
        this.contentSession?.pause()
    }

    override fun onHostDestroy() {
        this.contentSession?.widgetStream?.unsubscribe(this)
        this.contentSession = null
    }

    fun updateContentSession(contentSession: LiveLikeContentSession) {
        this.contentSession = contentSession
        this.contentSession?.widgetStream?.clear()
        contentSession?.widgetStream?.subscribe(this) {
            it?.let {
                this.displayWidget(it)
            }
        }
    }

    fun hideWidget() {
        this.renderWidget = false
        contentSession?.widgetInterceptor?.dismissWidget()
        widgetView.clearWidget()
    }


    var mainHandler = object : Handler(){
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            if (msg!!.what ==1){
                render()
            }
        }
    }

    private fun render() {
        Choreographer.getInstance().postFrameCallback(this.fallback)
    }

    fun displayWidget(widgetDetails : LiveLikeWidget) {
        widgetDetails.let {
            renderWidget = true
            LiveLikeManager.engagementSDK?.let { it1 -> this.widgetView.displayWidget(it1, it) }
            mainHandler.sendEmptyMessageDelayed(1,1)
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