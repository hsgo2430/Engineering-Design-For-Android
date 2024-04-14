package com.example.charvis.activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import com.example.charvis.R
import com.example.charvis.databinding.ActivityMainBinding
import com.example.charvis.databinding.ActivitySignUpBinding
import com.example.charvis.model.User
import com.example.charvis.utils.Extension.showMessage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase


class SignUpActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySignUpBinding
    var auth: FirebaseAuth = Firebase.auth
    private var databaseReference: DatabaseReference = FirebaseDatabase.getInstance().getReference("User")


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initSpinner()
        initView()
    }

    private fun initView(){
        binding.signUpBtn.setOnClickListener {
            if(binding.editPsw.text.toString() == binding.editPswCheck.text.toString()){
                signUpUser(binding.editEmail.text.toString(), binding.editPsw.text.toString())
            }
            else{
                showMessage(getString(R.string.check_password))
            }
        }
    }

    private fun initSpinner() {
        ArrayAdapter.createFromResource(
            this,
            R.array.interest_array,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.interestSpinner.adapter = adapter
        }

        binding.interestSpinner.onItemSelectedListener =
            object: AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    val selectedItem = parent?.getItemAtPosition(position).toString()

                    if (selectedItem == "직접 입력") {
                        binding.editInterest.setText("")
                        binding.editInterest.isFocusable = true
                        binding.editInterest.isFocusableInTouchMode = true
                        binding.editInterest.isEnabled = true
                    }
                    else {
                        binding.editInterest.setText(selectedItem)
                        binding.editInterest.isFocusable = false
                        binding.editInterest.isFocusableInTouchMode = false
                        binding.editInterest.isEnabled = true
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {

                }
            }
    }

    fun signUpUser(email: String, password: String){
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if(task.isSuccessful){
                   auth.currentUser?.sendEmailVerification()
                       ?.addOnCompleteListener { sendTask ->
                           if(sendTask.isSuccessful){
                               val firebaseUser = FirebaseAuth.getInstance().currentUser
                               var user = User(
                                   nickname = binding.editNickName.text.toString(),
                                   emailId = firebaseUser?.email.toString(),
                                   password = password,
                                   idToken = firebaseUser?.uid.toString(),
                                   interest = binding.editInterest.text.toString()
                               )

                               showMessage("이메일 인증을 해주신 뒤 로그인 해주세요")

                               databaseReference.child(firebaseUser?.uid.toString()).setValue(user)
                               val intent = Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.google)))
                               startActivity(intent)

                               finish()
                           }
                           else{
                               showMessage("회원 인증 실패")
                           }
                       }
                }
                else{
                    showMessage("회원 가입 실패")
                }
            }
    }
}