package com.livelike.demo

import android.app.Application
import com.facebook.react.bridge.*
import com.facebook.react.modules.core.DeviceEventManagerModule
import com.livelike.engagementsdk.EngagementSDK
import com.livelike.engagementsdk.EpochTime
import com.livelike.engagementsdk.MessageListener
import com.livelike.engagementsdk.chat.LiveLikeChatSession
import com.livelike.engagementsdk.chat.data.remote.ChatRoomMembership
import com.livelike.engagementsdk.chat.data.remote.PinMessageInfo
import com.livelike.engagementsdk.core.data.models.LeaderBoard
import com.livelike.engagementsdk.publicapis.LiveLikeCallback
import com.livelike.engagementsdk.publicapis.LiveLikeChatMessage

    class RNLiveLikeModule(application: Application, reactContext: ReactApplicationContext) : ReactContextBaseJavaModule(reactContext) {
    lateinit var chatSession: LiveLikeChatSession
    lateinit var producerChatSession: LiveLikeChatSession
    lateinit var profileId: String
    var context = reactContext
    val debugLog = DebugLog

    override fun getName(): String {
        return "Livelike"
    }

    @ReactMethod
    fun initialize(clientId: String, accessToken: String?, promise: Promise) {
        debugLog.context = context
        LiveLikeManager.context = context
        LiveLikeManager.initializeSDK(context, clientId, accessToken, promise)

        LiveLikeManager.engagementSDK.userStream.subscribe("_") {
            it?.let {
                profileId = it.userId
                val params = Arguments.createMap().apply {
                    putString("accessToken", it.accessToken)
                    putString("profileID", it.userId)
                }

                promise?.resolve(params)
                LiveLikeManager.engagementSDK.userStream.unsubscribe("_")
            }

        }
    }

    @ReactMethod
    fun setIsDebugLoggingEnabled(isEnabled: Boolean) {
        debugLog.isEnabled = isEnabled
    }

    @ReactMethod
    fun startContentSession(programID: String, promise: Promise) {
        if (LiveLikeManager.contentSession != null) {
            debugLog.log("Closing previous content session $programID")
            LiveLikeManager.contentSession?.close()
        }
        debugLog.log("startContentSession $programID")
        LiveLikeManager.startContentSession(programID, promise)
        promise.resolve(true)
    }

    @ReactMethod
    fun closeContentSession(promise: Promise) {
        LiveLikeManager.contentSession?.close()
        promise.resolve(true)
    }

    @ReactMethod
    fun joinProducerChatRoom(roomID: String, promise: Promise) {
        debugLog.log("joinProducerChatRoom $roomID")
        if (::producerChatSession.isInitialized) {
            debugLog.log("Closing previous producer chat session ${producerChatSession.getCurrentChatRoom()}")
            producerChatSession.close()
        }
        debugLog.log("createChatSession")
        producerChatSession = LiveLikeManager.engagementSDK.createChatSession(object : EngagementSDK.TimecodeGetter {
            override fun getTimecode(): EpochTime {
                return EpochTime(0)
            }
        })
        debugLog.log("connectToChatRoom")
        producerChatSession.connectToChatRoom(roomID, object : LiveLikeCallback<Unit>() {
            override fun onResponse(result: Unit?, error: String?) {
                if (error == null) {
                    debugLog.log("connectToChatRoom success")
                    producerChatSession.setMessageListener(object : MessageListener {
                        override fun onDeleteMessage(messageId: String) {
                            print(messageId)
                        }

                        override fun onHistoryMessage(messages: List<LiveLikeChatMessage>) {
                            print(messages)
                        }

                        override fun onNewMessage(message: LiveLikeChatMessage) {
                            debugLog.log("onNewMessage",
                                Arguments.createMap().apply {
                                    putString("message", message.message)
                                    putString("timestamp", message.timestamp)

                                    putString("custom_data", message.custom_data)
                                }
                            )

                            val list = ArrayList<LiveLikeChatMessage>()
                            list.add(message)
                            sendOnMessageReceivedEvent(list, producerChatSession.getCurrentChatRoom())
                        }

                        override fun onPinMessage(message: PinMessageInfo) {
                            // TODO("Not yet implemented")
                        }

                        override fun onUnPinMessage(pinMessageId: String) {
                            // TODO("Not yet implemented")
                        }
                    })

                    promise.resolve(roomID)
                } else {
                    debugLog.log("connectToChatRoom error: $error")
                }
            }
        })
    }

    @ReactMethod
    fun joinChatRoom(roomID: String, avatarURL: String?, promise: Promise) {
        if (::chatSession.isInitialized) chatSession.close()
        LiveLikeManager.engagementSDK.chat().addCurrentUserToChatRoom(roomID, object: LiveLikeCallback<ChatRoomMembership>() {
            override fun onResponse(result: ChatRoomMembership?, error: String?) {
                if (error == null) println("Added current user to chatroom $roomID")
                else println("Error adding current user to chatroom $roomID: $error")
            }
        })
        chatSession = LiveLikeManager.engagementSDK.createChatSession(object : EngagementSDK.TimecodeGetter {
            override fun getTimecode(): EpochTime {
                return EpochTime(0)
            }
        })
        chatSession.avatarUrl = avatarURL
        chatSession.connectToChatRoom(roomID, object : LiveLikeCallback<Unit>() {
            override fun onResponse(result: Unit?, error: String?) {
                if (error == null) {
                    chatSession.setMessageListener(object : MessageListener {
                        override fun onDeleteMessage(messageId: String) {
                            println("\n>>> DELETE $messageId")
                            sendOnMessageDeletedEvent(messageId)
                        }

                        override fun onHistoryMessage(messages: List<LiveLikeChatMessage>) {
                            print(messages)
                            sendOnMessageReceivedEvent(ArrayList(messages), chatSession.getCurrentChatRoom())
                        }

                        override fun onNewMessage(message: LiveLikeChatMessage) {
                            val list = ArrayList<LiveLikeChatMessage>()
                            list.add(message)
                            sendOnMessageReceivedEvent(list, chatSession.getCurrentChatRoom())
                        }

                        override fun onPinMessage(message: PinMessageInfo) {
                            // TODO("Not yet implemented")
                        }

                        override fun onUnPinMessage(pinMessageId: String) {
                            // TODO("Not yet implemented")
                        }
                    })

                    promise.resolve(roomID)
                }
            }
        })
    }

    @ReactMethod
    fun sendMessage(message: String) {

        chatSession.sendChatMessage(
            message,
            null,
            null,
            null,
            object : LiveLikeCallback<LiveLikeChatMessage>() {
                override fun onResponse(result: LiveLikeChatMessage?, error: String?) {
                    if (error != null) {
                        print(error)
                    } else {
                        print(result?.id)
                    }
                }
            })
    }

    @ReactMethod
    fun sendEvent(event: String) {
        producerChatSession.sendCustomChatMessage(
            event,
            object : LiveLikeCallback<LiveLikeChatMessage>() {
                override fun onResponse(result: LiveLikeChatMessage?, error: String?) {
                    if (error != null) {
                        print(error)
                    } else {
                        print(result?.id)
                    }
                }
            })
    }

    private fun sendOnMessageReceivedEvent(messages: ArrayList<LiveLikeChatMessage>, roomID: String) {
        val array: WritableArray = Arguments.createArray()
        messages.forEach {
            array.pushMap(Arguments.createMap().apply {
                putString("id", it.id)
                putString("text", it.message)
                putString("photoURL", it.userPic)
                putString("customData", it.custom_data)
                putString("senderNickname", it.nickname)
                putString("senderID", it.senderId)
                putBoolean("isMine", it.senderId == profileId)
                putString("timestamp", it.timestamp)
            })
        }


        val params = Arguments.createMap().apply {
            putString("roomID", roomID)
            putArray("messages", array)
        }

        context
            .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter::class.java)
            .emit("onMessageReceived", params)
    }

    private fun sendOnMessageDeletedEvent(messageId: String) {
        context
            .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter::class.java)
            .emit("onMessageDeleted", messageId)
    }

    @ReactMethod
    fun getResults(widgetID: String, promise: Promise) {
        LiveLikeManager.getResults(widgetID, promise)
    }

    @ReactMethod
    fun getProgramLeaderboards(programID: String, promise: Promise) {
        // The replacement for the deprecated method autocompletes,
        // but then shows 'Unresolved reference: getLeaderBoardsForProgram' error
        // https://docs.livelike.com/changelog/android-sdk-24
        // LiveLikeLeaderBoardClient.getLeaderBoardsForProgram()

        LiveLikeManager.engagementSDK.getLeaderBoardsForProgram(
            programID, object : LiveLikeCallback<List<LeaderBoard>>() {
                override fun onResponse(result: List<LeaderBoard>?, error: String?) {
                    result?.let {
                        val array: WritableArray = Arguments.createArray()
                        result.forEach {
                            array.pushMap(Arguments.createMap().apply {
                                putString("id", it.id)
                                putString("name", it.name)
                                putMap("rewardItem", Arguments.createMap().apply {
                                    putString("id", it.rewardItem.id)
                                    putString("name", it.rewardItem.name)
                                })
                            })
                        }
                        promise.resolve(array)
                    }
                    error?.let {
                        promise.reject(Throwable(error))
                    }
                }
            }
        )
    }

    @ReactMethod
    fun lockInAnswer(_unused: String, choiceID: String, promise: Promise) {
        LiveLikeManager.lockInAnswer(choiceID, promise)
    }

    @ReactMethod
    fun submitVote(_unused: String, choiceID: String, promise: Promise) {
        LiveLikeManager.submitVote(choiceID, promise)
    }
}