package com.example.charvis

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.example.charvis.activity.BluetoothActivity
import com.example.charvis.activity.SignUpActivity
import com.example.charvis.databinding.ActivityMainBinding
import com.example.charvis.utils.Extension.showMessage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var auth: FirebaseAuth? = Firebase.auth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initView()
    }

    private fun initView(){
        binding.signUpBtn.setOnClickListener {
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
        }

        binding.loginBtn.setOnClickListener {
            loginUser(binding.editId.text.toString(), binding.editPsw.text.toString())
        }
    }

    private fun loginUser(email: String, password: String){
        if (email.isNotEmpty() && password.isNotEmpty()) {
            auth?.signInWithEmailAndPassword(email, password)?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val intent = Intent(this,BluetoothActivity::class.java)
                    intent.putExtra("uid", auth?.currentUser?.uid)
                    startActivity(intent)

                } else {
                    showMessage("로그인에 실패하였습니다.")
                }
            }
        }
    }
}