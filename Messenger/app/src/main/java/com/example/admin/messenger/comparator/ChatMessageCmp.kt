package com.example.admin.messenger.comparator

import com.example.admin.messenger.models.ChatMessage

class ChatMessageCmp: Comparator<ChatMessage> {
    override fun compare(o1: ChatMessage, o2: ChatMessage): Int {
        return (o1.timestamp - o2.timestamp).toInt()
    }
}