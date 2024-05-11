package com.example.charvis.activity

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isGone
import androidx.core.view.isVisible
import com.example.charvis.R
import com.example.charvis.databinding.ActivitySignUpBinding
import com.example.charvis.databinding.ActivitySignUpUserDataBinding
import com.example.charvis.model.User
import com.example.charvis.utils.Extension
import com.example.charvis.utils.Extension.showMessage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase

class SignUpUserDataActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySignUpUserDataBinding
    var auth: FirebaseAuth = Firebase.auth
    private var databaseReference: DatabaseReference = FirebaseDatabase.getInstance().getReference("User")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpUserDataBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initSpinner()
        initView()
    }

    private fun initView(){
        val intent: Intent = intent
        val email = intent.getStringExtra("email")
        val password = intent.getStringExtra("password")

        binding.signUpBtn.setOnClickListener {
            if(binding.editNickName.text.toString().isNotEmpty() && binding.editInterest.text.toString().isNotEmpty()){
                signUpUser(email.toString(), password.toString(), binding.editNickName.text.toString(), binding.editInterest.text.toString() )
            }
            else{
                if(binding.editNickName.text.toString().isEmpty()) showMessage(getString(R.string.check_nickname))
                else if(binding.editInterest.text.toString().isEmpty()) showMessage(getString(R.string.check_interest))
                else showMessage(getString(R.string.error))
            }
        }

        binding.arrowBack.setOnClickListener {
            onBackPressed()
        }

        binding.charvisLogo.setOnClickListener {
            onBackPressed()
        }

        binding.userChildGenderTv.isGone = true
        binding.userChildSon.isGone = true
        binding.userChildDaughter.isGone = true

        binding.userChildGroup.setOnCheckedChangeListener { group, checkedId ->
            when(checkedId){
                binding.radioButtonYes.id ->{
                    binding.userChildGenderTv.isVisible = true
                    binding.userChildSon.isVisible = true
                    binding.userChildDaughter.isVisible = true
                }
                binding.radioButtonNo.id ->{
                    binding.userChildGenderTv.isGone = true
                    binding.userChildSon.isGone = true
                    binding.userChildDaughter.isGone = true
                }
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


    private fun signUpUser(email: String, password: String, nickname: String, interst: String){
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if(task.isSuccessful){
                    auth.currentUser?.sendEmailVerification()
                        ?.addOnCompleteListener { sendTask ->
                            if(sendTask.isSuccessful){
                                val firebaseUser = FirebaseAuth.getInstance().currentUser
                                var user = User(
                                    nickname = nickname,
                                    emailId = email,
                                    password = password,
                                    idToken = firebaseUser?.uid.toString(),
                                    interest = interst,
                                    gender = when {
                                        binding.radioButtonMale.isChecked -> 1
                                        binding.radioButtonFemale.isChecked -> 2
                                        binding.radioButtonNoChoice.isChecked -> 3
                                        else -> 0
                                    },
                                    child = when{
                                        binding.radioButtonNo.isChecked -> false
                                        binding.radioButtonYes.isChecked -> true
                                        else -> false
                                    },
                                    son = binding.userChildSon.isChecked,
                                    daughter = binding.userChildDaughter.isChecked
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