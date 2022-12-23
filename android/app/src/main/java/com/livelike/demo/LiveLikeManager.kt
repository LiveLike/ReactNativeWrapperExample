package com.livelike.demo

import android.content.Context
import android.util.Xml
import android.view.View
import com.facebook.react.bridge.*
import com.facebook.react.modules.core.DeviceEventManagerModule
import com.livelike.engagementsdk.EngagementSDK
import com.livelike.engagementsdk.LiveLikeContentSession
import com.livelike.engagementsdk.LiveLikeWidget
import com.livelike.engagementsdk.core.AccessTokenDelegate
import com.livelike.engagementsdk.publicapis.ErrorDelegate
import com.livelike.engagementsdk.publicapis.LiveLikeCallback
import com.livelike.engagementsdk.widget.LiveLikeWidgetViewFactory
import com.livelike.engagementsdk.widget.view.WidgetView
import com.livelike.engagementsdk.widget.widgetModel.*
import org.xmlpull.v1.XmlPullParser

object LiveLikeManager {

    lateinit var context: ReactApplicationContext
    lateinit var engagementSDK: EngagementSDK
    var contentSession: LiveLikeContentSession? = null
    lateinit var currentWidgetModel: QuizWidgetModel
    lateinit var currentPollWidgetModel: PollWidgetModel
    lateinit var latestResults: WritableMap
    var currentAccessToken: String? = null
    var dummyView: WidgetView? = null
    val debugLog = DebugLog

    @ReactMethod
    fun startContentSession(programId: String, promise: Promise) {
        this.contentSession = engagementSDK.createContentSession(programId)
        subscribeWidgetStream()
        val parser: XmlPullParser =
            context.resources.getXml(R.xml.ss)
        val attributes = Xml.asAttributeSet(parser)
        dummyView = WidgetView(context, attr = attributes)
        registerCustomViewModel(dummyView!!)
    }


    @ReactMethod
    fun initializeSDK(applicationContext: Context, clientId: String, accessToken: String?, promise: Promise) {
        println("Initialize with access token $currentAccessToken")
        currentAccessToken = accessToken
        engagementSDK = EngagementSDK(clientId, applicationContext, object : ErrorDelegate() {
            override fun onError(error: String) {
                println("LiveLikeApplication.onError--->$error")
                promise.reject(Throwable(error))
            }
        }, accessTokenDelegate = object : AccessTokenDelegate {
            override fun getAccessToken(): String? {
                return currentAccessToken
            }

            override fun storeAccessToken(accessToken: String?) {
                currentAccessToken = accessToken
            }
        })
    }

    private fun sendOnWidgetReceivedEvent(widget: LiveLikeWidget) {
        println("SEND EVENT")
        val options: WritableArray = Arguments.createArray()

        if (widget.kind == "text-poll") {
            widget.options?.forEach {
                options.pushMap(Arguments.createMap().apply {
                    putString("id", it?.id)
                    putString("text", it?.description)
                    putString("image", it?.imageUrl)
                    putBoolean("isCorrect", it?.isCorrect ?: false)
                    putInt("voteCount", it?.voteCount ?: 0)
                })
            }
        } else {
            widget.choices?.forEach {
                options.pushMap(Arguments.createMap().apply {
                    putString("id", it?.id)
                    putString("text", it?.description)
                    putString("image", it?.imageUrl)
                    putBoolean("isCorrect", it?.isCorrect ?: false)
                    putInt("voteCount", it?.voteCount ?: 0)
                })
            }
        }

        val map: WritableMap = Arguments.createMap().apply {
            putString("id", widget.id)
            putString("kind", widget.kind)
            putString("widgetTitle", widget.question)
            putString("text", widget.text)
            putString("createdAt", widget.createdAt)
            putString("publishedAt", widget.publishedAt)
            putString("timeout", widget.timeout)
            putString("customData", widget.customData)
            putArray("options", options)
        }

        sendEvent("onWidgetReceived", datamap = map)
    }

    fun sendEvent(eventName : String, datamap:WritableMap) {
        context
            .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter::class.java)
            .emit(eventName, datamap)
    }

    private fun subscribeWidgetStream() {
        println("SUBSCRIBE WIDGET STREAM")
        contentSession?.widgetStream?.subscribe(this) {
            it?.let {
                println("GOT WIDGET")
                debugLog.log("Widget received",
                    Arguments.createMap().apply {
                        putString("question", it.question)
                        putString("scheduledAt", it.scheduledAt)
                        putString("createdAt", it.createdAt)
                        putString("publishedAt", it.publishedAt)
                    }
                )
                dummyView?.displayWidget(engagementSDK,it,true)
                sendOnWidgetReceivedEvent(it)
            }
        }
    }

    @ReactMethod
    fun getResults(widgetID: String, promise: Promise) {
        engagementSDK.fetchWidgetDetails(widgetID,"text-quiz", object: LiveLikeCallback<LiveLikeWidget>() {
            override fun onResponse(result: LiveLikeWidget?, error: String?) {
                engagementSDK.let {
                    if (result != null) {
                        val array: WritableArray = Arguments.createArray()
                        val totalVoteCount = result?.choices?.sumOf { it?.answerCount ?: 0 }

                        result?.choices?.forEach {
                            array.pushMap(Arguments.createMap().apply {
                                putString("id", it?.id)
                                putBoolean("isCorrect", it?.isCorrect ?: false)
                                putInt("answerCount", it?.answerCount ?: 0)
                                if(it?.answerCount is Int && totalVoteCount is Int && totalVoteCount != 0){
                                    putInt("percentage", it.answerCount!! /totalVoteCount*100)
                                }else{
                                    putInt("percentage", 0)
                                }
                                putInt("correctNumber", it?.correctNumber ?: 0)
                                putInt("number", it?.number ?: 0)
                                putDouble("mergedVoteCount", (it?.voteCount ?: 0).toDouble())
                            })
                        }
                        val params = Arguments.createMap().apply {
                            putInt("totalVoteCount", totalVoteCount?:0)
                            putArray("options", array)
                        }
                        this@LiveLikeManager.latestResults = params
                        promise.resolve(params.copy())
                    }
                }
            }
        })
    }

    private fun registerCustomViewModel(widgetView: WidgetView) {
        println("REGISTER FACTORY")
        widgetView.widgetViewFactory = object : LiveLikeWidgetViewFactory {

            override fun createAlertWidgetView(alertWidgetModel: AlertWidgetModel): View? {
                print("ALERT WIDGET MODEL");
                return null
            }

            override fun createCheerMeterView(cheerMeterWidgetModel: CheerMeterWidgetmodel): View? {
                print("CHEER WIDGET MODEL");
                return null
            }

            override fun createImageSliderWidgetView(imageSliderWidgetModel: ImageSliderWidgetModel): View? {
                print("SLIDER WIDGET MODEL");
                return null
            }

            override fun createNumberPredictionFollowupWidgetView(
                followUpWidgetViewModel: NumberPredictionFollowUpWidgetModel,
                isImage: Boolean
            ): View? {
                print("NUMBER PREDICTION FOLLOW UP WIDGET MODEL");
                return null
            }

            override fun createNumberPredictionWidgetView(
                numberPredictionWidgetModel: NumberPredictionWidgetModel,
                isImage: Boolean
            ): View? {
                print("NUMBER PREDICTION WIDGET MODEL");
                return null
            }

            override fun createPollWidgetView(
                pollWidgetModel: PollWidgetModel,
                isImage: Boolean
            ): View? {
                print("POLL WIDGET MODEL");
                currentPollWidgetModel = pollWidgetModel
                val array: WritableArray = Arguments.createArray()
                pollWidgetModel?.voteResults?.subscribe(this) { result ->
                    val array: WritableArray = Arguments.createArray()
                    val totalVoteCount = result?.choices?.sumOf { it?.vote_count ?: 0 }
                    result?.choices?.forEach {
                        array.pushMap(Arguments.createMap().apply {
                            putString("id", it.id)
                            putInt("answerCount", it?.vote_count ?: 0)
                        })
                    }
                    val params = Arguments.createMap().apply {
                        putInt("totalVoteCount", totalVoteCount ?: 0)
                        putArray("options", array)
                    }
                    sendOnPollVotesChangeEvent(params.copy())
                    latestResults = params

                }
                return null
            }

            override fun createPredictionFollowupWidgetView(
                followUpWidgetViewModel: FollowUpWidgetViewModel,
                isImage: Boolean
            ): View? {
                print("PREDICTION FOLLOW UP WIDGET MODEL");
                return null
            }

            override fun createPredictionWidgetView(
                predictionViewModel: PredictionWidgetViewModel,
                isImage: Boolean
            ): View? {
                print("NUMBER PREDICTION WIDGET MODEL");
                return null
            }

            override fun createQuizWidgetView(
                quizWidgetModel: QuizWidgetModel,
                isImage: Boolean
            ): View? {
                println(quizWidgetModel.widgetData.kind)
                currentWidgetModel = quizWidgetModel
                return null
            }

            override fun createSocialEmbedWidgetView(socialEmbedWidgetModel: SocialEmbedWidgetModel): View? {
                print("SOCIAL EMBED WIDGET MODEL");
                return null
            }

            override fun createTextAskWidgetView(textAskWidgetModel: TextAskWidgetModel): View? {
                println(textAskWidgetModel.widgetData.kind);
                print("TEXT ASK WIDGET MODEL");
                return null
            }

            override fun createVideoAlertWidgetView(videoAlertWidgetModel: VideoAlertWidgetModel): View? {
                print("VIDEO ALERT WIDGET MODEL");
                return null
            }
        }
    }

    private fun sendOnPollVotesChangeEvent(params: WritableMap) {
        sendEvent("onPollVotesChange", params)
    }

    @ReactMethod
    fun lockInAnswer(choiceID: String, promise: Promise) {
        currentWidgetModel.lockInAnswer(choiceID)
        promise.resolve(true)
    }

    @ReactMethod
    fun submitVote(choiceID: String, promise: Promise) {
        currentPollWidgetModel.submitVote(choiceID)
        println("SUBMIT VOTE WITH CHOICE "+ choiceID)
        promise.resolve(true)
    }
}