package com.example.admin.messenger.activities

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Matrix
import android.net.Uri
import android.opengl.Visibility
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Toast
import com.daimajia.androidanimations.library.Techniques
import com.daimajia.androidanimations.library.YoYo
import com.example.admin.messenger.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_register.*
import java.util.*
import com.example.admin.messenger.R
import com.google.firebase.iid.FirebaseInstanceId
import android.graphics.Bitmap
import java.io.ByteArrayOutputStream
import android.support.v4.app.ActivityCompat
import android.content.pm.PackageManager
import android.support.annotation.MainThread
import android.support.v4.content.ContextCompat
import kotlin.collections.ArrayList


class RegisterActivity : Activity() {
    private  var selectedPhotoURI: Uri? = null
    private var selectedPhotoBitmap: Bitmap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        username_edittext_register.background.alpha = 150
        email_edittext_register.background.alpha = 150
        password_edittext_register.background.alpha = 150
        photo_button_register.background.alpha = 180
        register_button_register.background.alpha = 200
        rotate_picture.alpha = 0.7f

        register_button_register.setOnClickListener {
            startWaiting()
            performRegister()
        }


        photo_button_register.setOnClickListener {
            val i = Intent(Intent.ACTION_PICK)
            i.type = "image/*"
            startActivityForResult(i,0)
        }

        already_have_account_text_view_register.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }

        rotate_picture.setOnClickListener {
            if (selectedPhotoURI == null || selectedPhotoBitmap == null) return@setOnClickListener

            val matrix = Matrix()
            matrix.postRotate(90f)

            selectedPhotoBitmap = Bitmap.createBitmap(selectedPhotoBitmap, 0, 0, selectedPhotoBitmap!!.width, selectedPhotoBitmap!!.height, matrix, true)

            selectphoto_imageview_register.setImageBitmap(selectedPhotoBitmap)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 0 && resultCode == Activity.RESULT_OK && data != null) {
            selectedPhotoURI = data.data

            selectedPhotoBitmap = MediaStore.Images.Media.getBitmap(contentResolver,selectedPhotoURI)

            selectphoto_imageview_register.setImageBitmap(selectedPhotoBitmap)
            selectphoto_imageview_register.visibility = View.VISIBLE
            photo_button_register.alpha = 0f

            rotate_picture.visibility = View.VISIBLE
            ripple_photo_background_register.stopRippleAnimation()
        }
    }

    private fun performRegister() {
        val username = username_edittext_register.text.toString()
        val email = email_edittext_register.text.toString()
        val password = password_edittext_register.text.toString()

        if(username.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this,"Fields are empty",Toast.LENGTH_SHORT).show()
            stopWaiting()
            return
        }

        Log.d("Reg_Activity", "Email: $email")
        Log.d("Reg_Activity", "Password: $password")


        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email,password)
                .addOnCompleteListener {
                    if (!it.isSuccessful) {
                        stopWaiting()
                        return@addOnCompleteListener
                    }
                    Log.d("Reg_Activity", "Created new user with uid: ${it.result!!.user.uid}")

                    uploadImageToFirebaseStorage()
                }
                .addOnFailureListener {
                    Log.d("Reg_Activity", "Failed to crete user: ${it.message}")
                    Toast.makeText(this,"Failed to crete user: ${it.message}",Toast.LENGTH_SHORT).show()
                    stopWaiting()
                }
    }

    private fun uploadImageToFirebaseStorage() {
        val defPhotoUrl: String = "https://firebasestorage.googleapis.com/v0/b/messenger-c09ae.appspot.com/o/images%2Fdefault_user.png?alt=media&token=1a20a328-49f5-43dc-add3-e49a2c918fcc"
        if (selectedPhotoURI == null) {
            saveUserToFirebaseDatabase(defPhotoUrl)
            return
        }

        if (!checkPermissions()) {
            stopWaiting()
            Toast.makeText(this,"Need to confirm permissions !", Toast.LENGTH_LONG).show()
            return
        }

        val filename = UUID.randomUUID().toString()
        val ref = FirebaseStorage.getInstance().getReference("/images/$filename")

        ref.putFile(getImageUri(this,selectedPhotoBitmap!!))
                .addOnSuccessListener {
                    Log.d("Reg_Activity", "Successfully upload image: ${it.metadata?.path}")

                    ref.downloadUrl
                            .addOnSuccessListener {
                                Log.d("Reg_Activity", "image URL: $it")
                                saveUserToFirebaseDatabase(it.toString())
                            }
                            .addOnFailureListener {
                                Log.d("Reg_Activity", "Failed to download URL: ${it.message}")
                            }
                }
    }

    private fun saveUserToFirebaseDatabase(profileImageURL: String) {
        val uid = FirebaseAuth.getInstance().uid ?: ""
        Log.d("Reg_Activity", "Save user to firebase uid: $uid")
        val ref = FirebaseDatabase.getInstance().getReference("/users/$uid")

        val messagingToken = FirebaseInstanceId.getInstance().token

        val user = User(uid, username_edittext_register.text.toString(), profileImageURL,messagingToken!!)
        ref.setValue(user)
                .addOnSuccessListener {
                    Log.d("Reg_Activity", "Successfully save user to Firebase Database")

                    val i = Intent(this, LatestMessagesActivity::class.java)
                    i.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)

                    stopWaiting()
                    startActivity(i)
                }
                .addOnFailureListener {
                    Log.d("Reg_Activity", "Failed to save user to Firebase Database: ${it.message}")
                }
    }

    private  fun startWaiting() {
        progress_bar_register.enableIndeterminateMode(true)
        progress_bar_register.visibility = View.VISIBLE

        rotate_picture.visibility = View.INVISIBLE
        selectphoto_imageview_register.visibility = View.INVISIBLE
        photo_button_register.visibility = View.INVISIBLE
        edit_fields_ll_register.visibility = View.INVISIBLE
        ripple_photo_background_register.stopRippleAnimation()
    }

    private fun stopWaiting() {
        progress_bar_register.enableIndeterminateMode(false)
        progress_bar_register.visibility = View.INVISIBLE

        selectphoto_imageview_register.visibility = View.VISIBLE
        photo_button_register.visibility = View.VISIBLE
        edit_fields_ll_register.visibility = View.VISIBLE

        if (selectedPhotoURI != null) {
            rotate_picture.visibility = View.VISIBLE
        }
        else {
            ripple_photo_background_register.startRippleAnimation()
        }

        YoYo.with(Techniques.Tada)
                .duration(500)
                .repeat(1)
                .playOn(findViewById(R.id.edit_fields_ll_register))
    }

    fun getImageUri(inContext: Context, inImage: Bitmap): Uri {
        val bytes = ByteArrayOutputStream()
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
        val path = MediaStore.Images.Media.insertImage(inContext.contentResolver, inImage, "Title", null)
        return Uri.parse(path)
    }

     fun checkPermissions(): Boolean {
        val permissions = ArrayList<String>()
        permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        permissions.add(Manifest.permission.INTERNET)
        permissions.add(Manifest.permission.VIBRATE)

        var result: Int
        val listPermissionsNeeded = ArrayList<String>()
        for (p in permissions) {
            result = ContextCompat.checkSelfPermission(this, p)
            if (result != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(p)
            }
        }
        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this, listPermissionsNeeded.toTypedArray(), 100)
            return false
        }
        return true
    }

    override fun onStart() {
        super.onStart()

        if (selectedPhotoURI == null)
            ripple_photo_background_register.startRippleAnimation()
    }

    override fun onStop() {
        super.onStop()
        ripple_photo_background_register.stopRippleAnimation()
    }
}
