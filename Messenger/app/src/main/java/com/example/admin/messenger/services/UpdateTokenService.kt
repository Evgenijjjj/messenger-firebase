@file:Suppress("DEPRECATION")

package com.example.admin.messenger.services

import android.util.Log
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.iid.FirebaseInstanceIdService
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase


class UpdateTokenService: FirebaseInstanceIdService() {


    override fun onTokenRefresh() {
        changeTokenInDB(FirebaseInstanceId.getInstance().token!!)
    }

    private fun changeTokenInDB(token: String) {
        Log.d("TOKEN_update", token)
        val reference = FirebaseDatabase.getInstance().reference
        reference.child("/users/")
                .child(FirebaseAuth.getInstance().currentUser!!.uid)
                .child("messagingToken")
                .setValue(token)
    }

}