package com.livelike.demo.widget

import android.os.Handler
import android.os.Message
import android.view.Choreographer
import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.constraintlayout.widget.ConstraintLayout
import com.facebook.react.bridge.*
import com.facebook.react.uimanager.ThemedReactContext
import com.livelike.demo.LiveLikeManager
import com.livelike.demo.R
import com.livelike.engagementsdk.LiveLikeContentSession
import com.livelike.engagementsdk.LiveLikeWidget
import com.livelike.engagementsdk.widget.view.WidgetView

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
                Choreographer.getInstance().postFrameCallback(this!!.fallback)
            }
        }
        Choreographer.getInstance().postFrameCallback(fallback)
        createView()
    }

    private fun createView() {
        val parentView = LayoutInflater.from(context).inflate(R.layout.fc_widget_view, null) as ConstraintLayout;
        addView(parentView)
        widgetView = parentView.findViewById(R.id.widget_view)
    }

    override fun onHostResume() {

    }

    override fun onHostPause() {

    }

    override fun onHostDestroy() {
//        contentSession?.close()
        this.contentSession = null
    }

    fun updateContentSession(contentSession: LiveLikeContentSession) {
        this.contentSession = contentSession
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
            this.widgetView.displayWidget(LiveLikeManager.engagementSDK, it)
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