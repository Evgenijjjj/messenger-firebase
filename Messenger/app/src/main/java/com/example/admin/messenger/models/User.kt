package com.example.admin.messenger.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
public class User(val uid: String, val username: String, val profileImageURL: String, val messagingToken: String) : Parcelable {
    constructor() : this("","","","")
}