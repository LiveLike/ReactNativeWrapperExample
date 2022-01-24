package com.livelike.demo.ui.main

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.livelike.demo.R
import com.livelike.demo.databinding.ChatFragmentLayoutBinding
import com.livelike.engagementsdk.EngagementSDK
import com.livelike.engagementsdk.EpochTime
import com.livelike.engagementsdk.chat.ChatView
import com.livelike.engagementsdk.chat.ChatViewDelegate
import com.livelike.engagementsdk.chat.ChatViewThemeAttributes
import com.livelike.engagementsdk.publicapis.ChatMessageType
import com.livelike.engagementsdk.publicapis.ErrorDelegate
import com.livelike.engagementsdk.publicapis.LiveLikeCallback
import com.livelike.engagementsdk.publicapis.LiveLikeChatMessage
import org.json.JSONObject

/**
 * A placeholder fragment containing a simple view.
 */
class ChatFragment : BaseFragment() {

    private lateinit var pageViewModel: PageViewModel
    private var programId = ""
    private var isChatInputVisible = true
    private var _binding : ChatFragmentLayoutBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        pageViewModel = ViewModelProvider(requireActivity()).get(PageViewModel::class.java)
        setProgramId(arguments?.getString(ARG_SECTION_NUMBER) ?: "fbfd021c-a4ea-4088-9a07-568f7c947e33")
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
        _binding = ChatFragmentLayoutBinding.bind(inflater.inflate(R.layout.chat_fragment_layout, container, false))
        val chatView: ChatView = _binding!!.root.findViewById(R.id.chat_view)
        initChatSession(chatView)
        return _binding!!.root
    }

    private fun initChatSession(chat_view: ChatView) {
        pageViewModel.chatFrag= this
        val chatSession = pageViewModel.engagementSDK.createChatSession(object : EngagementSDK.TimecodeGetter {
            override fun getTimecode(): EpochTime {
                return EpochTime(0)
            }

        }, errorDelegate = object : ErrorDelegate() {
            override fun onError(error: String) {
                Log.e("TEST", error)
            }
        })

        if (chatSession != null) {
            chatSession.connectToChatRoom(this.programId, callback = object : LiveLikeCallback<Unit>() {
                override fun onResponse(result: Unit?, error: String?) {
                    if (error != null) {
                        Log.e("TEST", error)
                    }
                }
            })
            chat_view.allowMediaFromKeyboard = true
            chat_view.isChatInputVisible = false
            chat_view.setSession(chatSession)

            chatSession.avatarUrl
            _binding?.customChatMessageSendBtn?.setOnClickListener {
                val url = _binding?.urlInput?.text
                url?.let {
                    chatSession?.sendCustomChatMessage("{" +
                            "\"custom_message\": \""+url+"\"" +
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
                    println("ExoPlayerActivity.onBindView>> ${holder is MyCustomMsgViewHolder}")
                    chatViewThemeAttributes.chatBubbleBackgroundRes?.let {
                        val jsonObject = JSONObject(liveLikeChatMessage.custom_data)
                        val url= jsonObject.get("custom_message").toString()
                        (holder as MyCustomMsgViewHolder).videoUrl = url
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
                    putBoolean(ARG_CHAT_INPUT_VISIBILITY,isChatInputVisible)
                }
            }
        }
    }
}