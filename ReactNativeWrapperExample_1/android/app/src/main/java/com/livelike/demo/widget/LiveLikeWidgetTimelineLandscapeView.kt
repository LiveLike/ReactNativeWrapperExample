package com.livelike.demo.widget

import android.os.Handler
import android.os.Message
import android.view.Choreographer
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.Toast
import androidx.appcompat.widget.LinearLayoutCompat
import com.facebook.react.bridge.LifecycleEventListener
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContext
import com.facebook.react.modules.core.DeviceEventManagerModule
import com.facebook.react.uimanager.ThemedReactContext
import com.facebook.react.uimanager.events.RCTEventEmitter
import com.google.gson.JsonParser
import com.livelike.demo.LiveLikeManager
import com.livelike.engagementsdk.LiveLikeContentSession
import com.livelike.engagementsdk.LiveLikeEngagementTheme
import com.livelike.engagementsdk.LiveLikeWidget
import com.livelike.engagementsdk.chat.data.remote.LiveLikePagination
import com.livelike.engagementsdk.core.services.messaging.proxies.LiveLikeWidgetEntity
import com.livelike.engagementsdk.core.services.messaging.proxies.WidgetLifeCycleEventsListener
import com.livelike.engagementsdk.publicapis.LiveLikeCallback
import com.livelike.engagementsdk.widget.data.models.WidgetKind
import com.livelike.engagementsdk.widget.data.models.WidgetUserInteractionBase
import com.livelike.engagementsdk.widget.timeline.TimelineWidgetResource
import com.livelike.engagementsdk.widget.view.WidgetView
import com.livelike.engagementsdk.widget.viewModel.WidgetStates
import com.livelike.utils.Result
import com.reactnativewrapperexample_1.R
import java.io.IOException
import java.io.InputStream


class LiveLikeWidgetTimelineLandscapeView(
    val context: ThemedReactContext,
    val applicationContext: ReactApplicationContext
) : FrameLayout(context), LifecycleEventListener {

    private var contentSession: LiveLikeContentSession? = null
    lateinit var widgetView: WidgetView;
    var fallback: Choreographer.FrameCallback;
    private var renderWidget = false
    private var lastWidget: LiveLikeWidget? = null
    lateinit var configFilter:String

    init {
        this.applicationContext.addLifecycleEventListener(this)
        this.fallback = Choreographer.FrameCallback() {
            manuallyLayoutChildren();
            viewTreeObserver.dispatchOnGlobalLayout();
            if(renderWidget) {
                Choreographer.getInstance().postFrameCallbackDelayed(this!!.fallback,800)
            }
        }
        Choreographer.getInstance().postFrameCallback(fallback)
        createView()
    }

    private fun createView() {
        val parentView = LayoutInflater.from(context).inflate(R.layout.fc_widget_view, null) as LinearLayoutCompat;
        addView(parentView)
        widgetView = parentView.findViewById(R.id.widget_view)
        widgetView.showTimer = false
        widgetView.showDismissButton = true
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

    fun updateContentSession(contentSession: LiveLikeContentSession, configType: String?) {
        if (configType != null) {
            this.configFilter = configType
        }
        this.contentSession = contentSession
        this.contentSession?.widgetStream?.clear()
        findLastPostedLandscapeWidget(LiveLikePagination.FIRST)
        contentSession?.widgetStream?.subscribe(this) {
            it?.let {
                if(isWidgetValid(it)){
                    lastWidget = it
                }
            }
        }
    }

    private fun findLastPostedLandscapeWidget(page: LiveLikePagination){
        contentSession?.getPublishedWidgets(page, object : LiveLikeCallback<List<LiveLikeWidget>>() {
            override fun onResponse(result: List<LiveLikeWidget>?, error: String?) {
                var filteredWidget: LiveLikeWidget? = null
                result?.let { list ->
                    if (list.isEmpty()) {
                        return@let
                    }
                    filteredWidget = list.find { isWidgetValid(it) }
                }
                // this means that published result is finished, there are no more to display
                if ((result == null && error == null)) {
                    fireReady()
                } else if (filteredWidget == null) {
                    findLastPostedLandscapeWidget(LiveLikePagination.NEXT)
                } else {
                    lastWidget = filteredWidget
                    fireReady()
                }
            }
        })
    }

    private fun fireReady(){
        val reactContext = this.getContext() as ReactContext;
        reactContext.getJSModule(RCTEventEmitter::class.java)
            .receiveEvent(this.getId(), LiveLikeWidgetViewManager.EVENT_WIDGET_READY, null)
    }

    private fun isWidgetValid(widget: LiveLikeWidget): Boolean {
        //Seek Bar Position?
        var isLandscape = widget.widgetAttributes?.find { it.key.equals("landscape") }
        return isLandscape != null && widget.kind.equals(configFilter)
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
            this.widgetView.enableDefaultWidgetTransition = false

            LiveLikeManager.engagementSDK?.let {
                    sdk -> this.widgetView.displayWidget(sdk, it, showWithInteractionData = true)
            }

            widgetView.showDismissButton = true
            widgetView.showTimer = false
            widgetView.enableDefaultWidgetTransition = false
            widgetView.widgetLifeCycleEventsListener = object : WidgetLifeCycleEventsListener() {
                override fun onWidgetPresented(widgetData: LiveLikeWidgetEntity) {
                }

                override fun onWidgetInteractionCompleted(widgetData: LiveLikeWidgetEntity) {
                }

                override fun onWidgetDismissed(widgetData: LiveLikeWidgetEntity) {
                }

                override fun onUserInteract(widgetData: LiveLikeWidgetEntity) {
                }

                override fun onWidgetStateChange(
                    widgetStates: WidgetStates,
                    widgetData: LiveLikeWidgetEntity
                ) {
                    when (widgetStates) {
                        WidgetStates.READY -> {
                            // it is called when widget is rendered and gets ready
                            // interaction is locked in this state
                            // call moveToNextState to enter into ineraction state
                            widgetView. moveToNextState()
                        }
                        WidgetStates.INTERACTING -> {
                            lockAlreadyInteractedQuizAndEmojiSlider()
                        }
                        WidgetStates.RESULTS -> {

                        }
                        WidgetStates.FINISHED -> {

                        }
                    }
                }
            }

            mainHandler.sendEmptyMessageDelayed(1,1)
        }
    }

    private fun lockAlreadyInteractedQuizAndEmojiSlider() {
        val kind = lastWidget?.kind
        if (kind == WidgetKind.IMAGE_SLIDER.event || kind?.contains(WidgetKind.QUIZ.event) == true ||
            kind?.contains(WidgetKind.TEXT_ASK.event) == true || kind?.contains(WidgetKind.NUMBER_PREDICTION.event) == true
        ) {
            lastWidget?.id?.let {
                contentSession?.getWidgetInteraction(
                    it,lastWidget?.kind!!,lastWidget?.widgetInteractionUrl!!,
                    liveLikeCallback = object :
                        LiveLikeCallback<WidgetUserInteractionBase>() {
                        override fun onResponse(
                            result: WidgetUserInteractionBase?,
                            error: String?
                        ) {
                            result?.let {
                                widgetView.moveToNextState()
                            }
                        }
                    })
            }
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

    fun displayLastWidget() {
        lastWidget?.let { displayWidget(it) }
    }
}