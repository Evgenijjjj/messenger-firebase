package com.example.admin.messenger.activities

import android.app.Activity
import android.content.Intent
import android.inputmethodservice.Keyboard
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.daimajia.androidanimations.library.Techniques
import com.daimajia.androidanimations.library.YoYo
import com.example.admin.messenger.R
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        email_edittext_login.background.alpha = 150
        password_edittext_login.background.alpha = 150
        login_button_login.background.alpha = 200

        login_button_login.setOnClickListener {
            email_edittext_login.clearFocus()
            password_edittext_login.clearFocus()
            login_button_login.clearFocus()
            startWaiting()
            if (email_edittext_login.text.toString().isEmpty() || password_edittext_login.text.toString().isEmpty()) {
                Toast.makeText(this,"Fields are empty", Toast.LENGTH_SHORT).show()
                stopWaiting()
                return@setOnClickListener
            }

            FirebaseAuth.getInstance().signInWithEmailAndPassword(email_edittext_login.text.toString(), password_edittext_login.text.toString())
                    .addOnSuccessListener {
                        val i = Intent(this, LatestMessagesActivity::class.java)
                        i.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)

                        stopWaiting()
                        startActivity(i)
                    }
                    .addOnFailureListener {
                        Toast.makeText(this,"Failed to login: ${it.message}",Toast.LENGTH_SHORT).show()
                        stopWaiting()
                    }
        }

        back_to_registartion_text_view_login.setOnClickListener {
            finish()
        }
    }

    override fun onPause() {
        super.onPause()
        if (isFinishing) {
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
        }
    }

    private  fun startWaiting() {
        progress_bar_login.enableIndeterminateMode(true)
        progress_bar_login.visibility = View.VISIBLE

        edit_fields_ll_login.visibility = View.INVISIBLE
    }

    private fun stopWaiting() {
        progress_bar_login.enableIndeterminateMode(false)
        progress_bar_login.visibility = View.INVISIBLE

        edit_fields_ll_login.visibility = View.VISIBLE

        YoYo.with(Techniques.Tada)
                .duration(500)
                .repeat(1)
                .playOn(findViewById(R.id.edit_fields_ll_login))
    }
}
