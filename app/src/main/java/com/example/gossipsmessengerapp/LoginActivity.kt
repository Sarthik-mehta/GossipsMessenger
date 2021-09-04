package com.example.gossipsmessengerapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_login.*


class LoginActivity:AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        login_button_login.setOnClickListener{
            performLogin()
            }
        back_to_register_textview.setOnClickListener {
            val intent= Intent(this,RegisterActivity::class.java)
            startActivity(intent)
        }

        }

    private fun performLogin() {
        val email = email_edittext_login.text.toString()
        val password = password_edittext_login.text.toString()
        if(email.isEmpty() || password.isEmpty())
        {
            Toast.makeText(this,"Please fill all the fields properly", Toast.LENGTH_SHORT).show()
            return
        }

        FirebaseAuth.getInstance().signInWithEmailAndPassword(email,password)
            .addOnSuccessListener {
                val intent= Intent(this,LatestMessagesActivity::class.java)
                intent.flags= Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK) //when you press back button, this will not take you to register activity page, it will take you out of the app instead
                startActivity(intent)
            }
            .addOnFailureListener{
                Toast.makeText(this,it.suppressedExceptions!!.toString(), Toast.LENGTH_SHORT).show()
            }

}
}