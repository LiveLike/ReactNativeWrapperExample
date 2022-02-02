package com.livelike.demo.ui.main

import android.content.res.Resources
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.facebook.react.bridge.UiThreadUtil.runOnUiThread
import com.livelike.demo.databinding.ChatFragmentLayoutBinding
import com.livelike.engagementsdk.EngagementSDK
import com.livelike.engagementsdk.EpochTime
import com.livelike.engagementsdk.MessageListener
import com.livelike.engagementsdk.R
import com.livelike.engagementsdk.chat.ChatView
import com.livelike.engagementsdk.chat.ChatViewDelegate
import com.livelike.engagementsdk.chat.ChatViewThemeAttributes
import com.livelike.engagementsdk.chat.ChatViewThemeAttributes.*
import com.livelike.engagementsdk.chat.data.remote.LiveLikeOrdering
import com.livelike.engagementsdk.chat.data.remote.LiveLikePagination
import com.livelike.engagementsdk.chat.data.remote.PinMessageInfo
import com.livelike.engagementsdk.publicapis.ChatMessageType
import com.livelike.engagementsdk.publicapis.ErrorDelegate
import com.livelike.engagementsdk.publicapis.LiveLikeCallback
import com.livelike.engagementsdk.publicapis.LiveLikeChatMessage
import org.json.JSONObject
import java.util.*
import kotlin.collections.ArrayList

/**
 * A placeholder fragment containing a simple view.
 */
class ChatFragment : BaseFragment() {

    private lateinit var pageViewModel: PageViewModel
    private var programId = ""
    private var isChatInputVisible = true
    private var _binding: ChatFragmentLayoutBinding? = null
    private var pinMessageAdapter = PinMessageAdapter(ArrayList())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        pageViewModel = ViewModelProvider(requireActivity()).get(PageViewModel::class.java)
        setProgramId(
            arguments?.getString(ARG_SECTION_NUMBER) ?: "fbfd021c-a4ea-4088-9a07-568f7c947e33"
        )
        //setProgramId("3ebd6f09-2f16-4171-b94a-c9335154d672")
        isChatInputVisible = true//arguments?.getBoolean(ARG_CHAT_INPUT_VISIBILITY) ?: false

    }

    private fun setProgramId(programId: String) {
        this.programId = programId

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = ChatFragmentLayoutBinding.bind(
            inflater.inflate(
                com.livelike.demo.R.layout.chat_fragment_layout,
                container,
                false
            )
        )
        val chatView: ChatView = _binding!!.root.findViewById(com.livelike.demo.R.id.chat_view)
        _binding!!.pinnedMessageList.adapter = pinMessageAdapter
        initChatSession(chatView)
        return _binding!!.root
    }


    private fun initChatSession(chat_view: ChatView) {
        pageViewModel.chatFrag = this

        val chatSession =
            pageViewModel.engagementSDK.createChatSession(object : EngagementSDK.TimecodeGetter {
                override fun getTimecode(): EpochTime {
                    return EpochTime(0)
                }

            }, errorDelegate = object : ErrorDelegate() {
                override fun onError(error: String) {
                    Log.e("TEST", error)
                }
            })

        if (chatSession != null) {
            chatSession.connectToChatRoom(
                this.programId,
                callback = object : LiveLikeCallback<Unit>() {
                    override fun onResponse(result: Unit?, error: String?) {
                        if (result != null) {

                        }
                    }
                })
            chat_view.allowMediaFromKeyboard = true
            chat_view.isChatInputVisible = true
            chat_view.setSession(chatSession)

            pageViewModel.engagementSDK.chat().getPinMessageInfoList(
                programId!!,
                LiveLikeOrdering.ASC,
                LiveLikePagination.FIRST,
                object : LiveLikeCallback<List<PinMessageInfo>>() {
                    override fun onResponse(
                        result: List<PinMessageInfo>?,
                        error: String?
                    ) {
                        result?.let {
                            pinMessageAdapter.updateData(it as ArrayList<PinMessageInfo>)

                        }
                    }
                })

            chatSession.setMessageListener(object : MessageListener {
                override fun onDeleteMessage(messageId: String) {
                    // TODO("Not yet implemented")
                }

                override fun onHistoryMessage(messages: List<LiveLikeChatMessage>) {
                    //TODO("Not yet implemented")
                }

                override fun onNewMessage(message: LiveLikeChatMessage) {
                    // TODO("Not yet implemented")
                }

                override fun onPinMessage(message: PinMessageInfo) {
                    runOnUiThread {
                        pinMessageAdapter.addMessageToList(message)
                    }
                }

                override fun onUnPinMessage(pinMessageId: String) {
                    runOnUiThread {
                        pinMessageAdapter.removeMessageToList(pinMessageId)
                    }
                }

            })

            _binding?.customChatMessageSendBtn?.setOnClickListener {
                val url = _binding?.urlInput?.text
                url?.let {
                    chatSession?.sendCustomChatMessage("{" +
                            "\"custom_message\": \"" + url + "\"" +
                            "}", object : LiveLikeCallback<LiveLikeChatMessage>() {
                        override fun onResponse(result: LiveLikeChatMessage?, error: String?) {
                            result?.let {
                                println("ExoPlayerActivity.onResponse> ${it.id}")
                            }
                            error?.let {
                                //Toast.makeText(applicationContext, error, Toast.LENGTH_SHORT).show()
                            }
                        }
                    })
                }

            }
            chat_view.chatViewDelegate = object : ChatViewDelegate {
                override fun onCreateViewHolder(
                    parent: ViewGroup,
                    viewType: ChatMessageType
                ): RecyclerView.ViewHolder {
                    return MyCustomMsgViewHolder(VideoView(parent.context))
                }

                override fun onBindViewHolder(
                    holder: RecyclerView.ViewHolder,
                    liveLikeChatMessage: LiveLikeChatMessage,
                    chatViewThemeAttributes: ChatViewThemeAttributes,
                    showChatAvatar: Boolean
                ) {

                    chatViewThemeAttributes.apply {
                        (holder.itemView as VideoView)._binding?.let {

                            if (true) {
                                it.chatNickname.setTextColor(chatNickNameColor)
                                it.chatNickname.text =
                                    liveLikeChatMessage.nickname
                            } else {
                                it.chatNickname.setTextColor(chatOtherNickNameColor)
                                it.chatNickname.text = liveLikeChatMessage.nickname
                            }
                            it.chatNickname.setTextSize(
                                TypedValue.COMPLEX_UNIT_PX,
                                chatUserNameTextSize
                            )
                            it.chatNickname.isAllCaps = chatUserNameTextAllCaps


                            val layoutParam =
                                it.chatBackground!!.layoutParams as ConstraintLayout.LayoutParams
                            it.chatBubbleBackground.setBackgroundResource(R.drawable.ic_chat_message_bubble_rounded_rectangle)
                            layoutParam.setMargins(
                                chatMarginLeft,
                                chatMarginTop + dpToPx(6),
                                chatMarginRight,
                                chatMarginBottom
                            )
                            layoutParam.width = ViewGroup.LayoutParams.MATCH_PARENT
                            it.chatBackground.layoutParams = layoutParam
                            it.chatBubbleBackground.setPadding(
                                chatBubblePaddingLeft,
                                chatBubblePaddingTop,
                                chatBubblePaddingRight,
                                chatBubblePaddingBottom
                            )
                            val layoutParam1: LinearLayout.LayoutParams =
                                it.chatBubbleBackground.layoutParams as LinearLayout.LayoutParams
                            layoutParam1.setMargins(
                                chatBubbleMarginLeft,
                                chatBubbleMarginTop,
                                chatBubbleMarginRight,
                                chatBubbleMarginBottom
                            )
                            layoutParam1.width = ViewGroup.LayoutParams.MATCH_PARENT
                            it.chatBubbleBackground.layoutParams = layoutParam1
                            it.imgChatAvatar.visibility =
                                when (showChatAvatar) {
                                    true -> View.VISIBLE
                                    else -> View.GONE
                                }
                            val layoutParamAvatar = LinearLayout.LayoutParams(
                                chatAvatarWidth,
                                chatAvatarHeight
                            )
                            layoutParamAvatar.setMargins(
                                chatAvatarMarginLeft,
                                chatAvatarMarginTop,
                                chatAvatarMarginRight,
                                chatAvatarMarginBottom
                            )
                            layoutParamAvatar.gravity = chatAvatarGravity
                            it.imgChatAvatar.layoutParams = layoutParamAvatar

                            val options = RequestOptions()
                            if (chatAvatarCircle) {
                                options.optionalCircleCrop()
                            }
                            if (chatAvatarRadius > 0) {
                                options.transform(
                                    CenterCrop(),
                                    RoundedCorners(chatAvatarRadius)
                                )
                            }
                            if (liveLikeChatMessage.userPic.isNullOrEmpty()) {
                                // load local image
                                Glide.with(holder.itemView.context.applicationContext)
                                    .load(R.drawable.default_avatar)
                                    //.apply(options)
                                    .placeholder(chatUserPicDrawable)
                                    .into(it.imgChatAvatar)
                            } else {
                                Glide.with(holder.itemView.context.applicationContext)
                                    .load("https://www.gstatic.com/webp/gallery/1.jpg")
                                    //.apply(options)
                                    .placeholder(chatUserPicDrawable)
                                    .error(chatUserPicDrawable)
                                    .into(it.imgChatAvatar)
                            }
                            //chatMessage.tag = message.id

                            val jsonObject = JSONObject(liveLikeChatMessage.custom_data)
                            val url = jsonObject.get("custom_message").toString()
                            (holder as MyCustomMsgViewHolder).videoUrl = url
                        }

                    }
                }
            }

            //this.chat_view = chat_view
        }
    }

    class MyCustomMsgViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var videoUrl: String? = null
            set(value) {
                field = value
                (itemView as VideoView).videoUrl = value
            }
    }

    companion object {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private const val ARG_SECTION_NUMBER = "section_number"
        private const val ARG_CHAT_INPUT_VISIBILITY = "chat_input_visibility"

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        @JvmStatic
        fun newInstance(sectionNumber: String, isChatInputVisible: Boolean): ChatFragment {
            return ChatFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_SECTION_NUMBER, sectionNumber)
                    putBoolean(ARG_CHAT_INPUT_VISIBILITY, isChatInputVisible)
                }
            }
        }
    }

    fun dpToPx(dp: Int): Int {
        val scale = Resources.getSystem().displayMetrics.density
        return (dp * scale + 0.5f).toInt()
    }
}

class PinMessageAdapter(private val messageList: ArrayList<PinMessageInfo>) :
    RecyclerView.Adapter<PinMessageAdapter.ViewHolder>() {

    // holder class to hold reference
    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        //get view reference
        var messageTextView: TextView =
            view.findViewById(com.livelike.demo.R.id.message) as TextView
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // create view holder to hold reference
        return ViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(com.livelike.demo.R.layout.pin_message_item, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        //set values
        holder.messageTextView.text = messageList[position].messagePayload?.message
    }

    override fun getItemCount(): Int {
        return messageList.size
    }

    fun addMessageToList(newMessage: PinMessageInfo) {
        messageList.add(newMessage)
        notifyDataSetChanged()
    }

    fun removeMessageToList(messageId: String) {
        val index = messageList.indexOfFirst { it.id == messageId }
        if (index != -1) {
            messageList.removeAt(index)
            notifyDataSetChanged()
        }

    }

    // update your data
    fun updateData(newMessageList: ArrayList<PinMessageInfo>) {
        messageList.clear()
        notifyDataSetChanged()
        messageList.addAll(newMessageList)
        notifyDataSetChanged()

    }
}