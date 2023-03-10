package com.livelike.demo.widget

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import androidx.appcompat.widget.LinearLayoutCompat
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.uimanager.ThemedReactContext
import com.livelike.demo.LiveLikeManager
import com.livelike.engagementsdk.LiveLikeWidget
import com.livelike.engagementsdk.publicapis.LiveLikeCallback
import com.livelike.engagementsdk.widget.LiveLikeWidgetViewFactory
import com.livelike.engagementsdk.widget.view.WidgetView
import com.livelike.engagementsdk.widget.widgetModel.*
import com.reactnativewrapperexample_1.R


class VZWidgetView(
    val context: ThemedReactContext,
    val applicationContext: ReactApplicationContext
) : FrameLayout(context) {

    lateinit var widgetView: WidgetView;

    init {

        createView()
    }

    private fun createView() {
        val parentView = LayoutInflater.from(context)
            .inflate(R.layout.fc_widget_view, null) as LinearLayoutCompat;
        addView(parentView)
        widgetView = parentView.findViewById(R.id.widget_view)
        widgetView.widgetViewFactory = object : LiveLikeWidgetViewFactory {
            override fun createAlertWidgetView(alertWidgetModel: AlertWidgetModel): View? {
                return null
            }

            override fun createCheerMeterView(cheerMeterWidgetModel: CheerMeterWidgetmodel): View? {
                cheerMeterWidgetModel.voteResults?.subscribe(this.hashCode()) { widgetResult ->
                    val op1 = widgetResult?.choices?.get(0)
                    val op2 = widgetResult?.choices?.get(1)
                    val vt1 = op1?.vote_count ?: 0
                    val vt2 = op2?.vote_count ?: 0
                    Log.d("Vote", "onUser vote 1st user  ${vt1}")
                    Log.d("Vote", "onUser vote 1st user  ${vt2}")

                    val total = vt1 + vt2
                    if (total > 0) {
                        val perVt1 = (vt1.toFloat() / total.toFloat()) * 100
                        val perVt2 = (vt2.toFloat() / total.toFloat()) * 100
                        Log.d("Vote", "onUser percent 1st user  ${perVt1}")
                        Log.d("Vote", "onUser percent 2nd user  ${perVt2}")
                    }
                }
                return null
            }

            override fun createImageSliderWidgetView(imageSliderWidgetModel: ImageSliderWidgetModel): View? = null

            override fun createNumberPredictionFollowupWidgetView(
                followUpWidgetViewModel: NumberPredictionFollowUpWidgetModel,
                isImage: Boolean
            ): View? = null

            override fun createNumberPredictionWidgetView(
                numberPredictionWidgetModel: NumberPredictionWidgetModel,
                isImage: Boolean
            ): View?= null

            override fun createPollWidgetView(
                pollWidgetModel: PollWidgetModel,
                isImage: Boolean
            ): View? = null

            override fun createPredictionFollowupWidgetView(
                followUpWidgetViewModel: FollowUpWidgetViewModel,
                isImage: Boolean
            ): View? = null

            override fun createPredictionWidgetView(
                predictionViewModel: PredictionWidgetViewModel,
                isImage: Boolean
            ): View?= null
            override fun createQuizWidgetView(
                quizWidgetModel: QuizWidgetModel,
                isImage: Boolean
            ): View? {
                return null
            }

            override fun createSocialEmbedWidgetView(socialEmbedWidgetModel: SocialEmbedWidgetModel): View? {
                return null
            }

            override fun createTextAskWidgetView(imageSliderWidgetModel: TextAskWidgetModel): View? {
                return null
            }

            override fun createVideoAlertWidgetView(videoAlertWidgetModel: VideoAlertWidgetModel): View? {
                return null
            }

        }
    }

    fun fetchWidget() {
        LiveLikeManager.engagementSDK?.fetchWidgetDetails(
            "31efc73b-95d2-4395-bc3a-6b8040a5010b",
            "cheer-meter",
            object : LiveLikeCallback<LiveLikeWidget>() {
                override fun onResponse(livelikeWidget: LiveLikeWidget?, error: String?) {
                    livelikeWidget?.let {
                        //widget detail
                        displayWidget(livelikeWidget)
                    }
                    error?.let {
                    }
                }
            })
    }

    fun hideWidget() {
        widgetView.clearWidget()
    }

    fun displayWidget(widgetDetails: LiveLikeWidget) {
        widgetDetails.let {
            LiveLikeManager.engagementSDK?.let { sdk ->
                this.widgetView.displayWidget(sdk, it, showWithInteractionData = true)
            }
        }
    }
}