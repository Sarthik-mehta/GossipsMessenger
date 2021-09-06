package com.MessagingApp.gossipsmessengerapp

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import com.MessagingApp.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_register.*
import java.util.*

class RegisterActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)



        register_button_register.setOnClickListener{
            performRegister()

        }

        already_have_account_text_view.setOnClickListener {
            Log.d("RegisterActivity", "Trying to go to login activity")
            val intent= Intent(this,LoginActivity::class.java)
            startActivity(intent)
        }

        selectphoto_button_register.setOnClickListener {
            val intent= Intent(Intent.ACTION_PICK)
            intent.type= "image/*"
            startActivityForResult(intent,0)

        }

    }

    var selectedPhotoUri: Uri?=null

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode==0 && resultCode== Activity.RESULT_OK && data!=null)
        {
            Log.d("RegisterActivity","Photo was selected")
            selectedPhotoUri= data.data
            val bitmap= MediaStore.Images.Media.getBitmap(contentResolver,selectedPhotoUri)
            selectphoto_imageview_register.setImageBitmap(bitmap)
            selectphoto_button_register.alpha=0f
            //val bitmapDrawable = BitmapDrawable(bitmap)
            //selectphoto_button_register.setBackgroundDrawable(bitmapDrawable)
        }
    }



    private fun performRegister() {
        val username= username_edittext_register.text.toString()
        val email = email_edittext_register.text.toString()
        val password = password_edittext_register.text.toString()
        Log.d("RegisterActivity", "Email is: $email")
        Log.d("RegisterActivity", "Password is: $password")

        if(username.isEmpty())
        {
            Toast.makeText(this,"Please fill your username.",Toast.LENGTH_SHORT).show()
            return
        }
        if(email.isEmpty())
        {
            Toast.makeText(this,"Please fill your e-mail.",Toast.LENGTH_SHORT).show()
            return
        }
        if(password.isEmpty())
        {
            Toast.makeText(this,"Please fill your password.",Toast.LENGTH_SHORT).show()
            return
        }

        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener {
                if(!it.isSuccessful) return@addOnCompleteListener

                Log.d("RegisterActivity","successfully created user with uid:${it.result!!.user!!.uid}")
                uploadImageToFirebaseStorage()
            }
            .addOnFailureListener{
                Log.d("RegisterActivity","Failed to create user :${it.message}")
                Toast.makeText(this,"Failed to create user:${it.message}",Toast.LENGTH_SHORT).show()
            }
    }

    private fun uploadImageToFirebaseStorage() {
        if(selectedPhotoUri==null) return

        val filename= UUID.randomUUID().toString()
        val ref = FirebaseStorage.getInstance().getReference("/images/$filename")

        ref.putFile(selectedPhotoUri!!)
            .addOnSuccessListener {
                Log.d("RegisterActivity","Successfully uploaded image: ${it.metadata?.path} ")

                ref.downloadUrl.addOnSuccessListener {

                    Log.d("RegisterActivity","File Location: $it")
                    saveUserToFirebaseDatabase(it.toString())
                }
                    .addOnFailureListener{

                    }
            }

    }

    private fun saveUserToFirebaseDatabase(profileImageUrl: String) {
        val uid = FirebaseAuth.getInstance().uid?:""
        val ref = FirebaseDatabase.getInstance().getReference("/users/$uid")
        val user = User(uid,username_edittext_register.text.toString(),profileImageUrl)
        ref.setValue(user)
            .addOnSuccessListener {
                Log.d("RegisterActivity","User finally saved and registered successfully to Firebase Database")
                val intent= Intent(this,LatestMessagesActivity::class.java)

                intent.flags= Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK) //when you press back button, this will not take you to register activity page, it will take you out of the app instead
                startActivity(intent)
            }
            .addOnFailureListener{
                Log.d("RegisterActivity","Something Went Wrong!")
            }
    }
}
