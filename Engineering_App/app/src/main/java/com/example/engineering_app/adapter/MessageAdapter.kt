package com.example.engineering_app.adapter

import android.R
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.example.engineering_app.databinding.ChatItemBinding
import com.example.engineering_app.model.Message


class MessageAdapter(private var messageList: ArrayList<Message>) : RecyclerView.Adapter<MessageAdapter.ViewHolder>() {

    class ViewHolder(private val binding: ChatItemBinding): RecyclerView.ViewHolder(binding.root){
        val leftChatView = binding.leftChatView
        val rightChatView = binding.rightChatView

        val leftChatTv = binding.leftChatTv
        val rightChatTv = binding.rightChatTv
        // 뷰 바인딩을 사용
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ChatItemBinding.inflate(inflater, parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return messageList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        with(holder){
            val message: Message = messageList[position]
            if (message.sendBy == message.sendByMe){
                leftChatView.visibility = View.GONE
                rightChatView.visibility = View.VISIBLE
                rightChatTv.text = message.message
                // 보낸 사람이 사용자라면 오른쪽 말풍선만 보이도록 하고 말풍선에 메시지 출력
            }
            else {
                rightChatView.visibility= View.GONE
                leftChatView.visibility = View.VISIBLE
                leftChatTv.text = message.message
                // 보낸 사람이 봇이라면 왼쪽  말풍선만 보이도록하고 메시지 출력
            }
        }
    }
}