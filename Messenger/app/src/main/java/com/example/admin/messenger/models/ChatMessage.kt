package com.example.admin.messenger.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
public  class ChatMessage(val id: String,val text: String,val fromId: String,val toId: String,val timestamp: Long) : Parcelable {
    constructor(): this("","","","",-1)
}