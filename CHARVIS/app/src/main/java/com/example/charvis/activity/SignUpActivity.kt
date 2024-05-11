package com.example.charvis.activity

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.charvis.R
import com.example.charvis.databinding.ActivitySignUpBinding
import com.example.charvis.utils.Extension.isEmail
import com.example.charvis.utils.Extension.showMessage

class SignUpActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySignUpBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initView()
    }

    private fun initView(){
        binding.nextBtn.setOnClickListener {
            if(binding.editPsw.text.toString() == binding.editPswCheck.text.toString() && binding.editEmail.text.toString().isEmail()){
                val intent = Intent(this, SignUpUserDataActivity::class.java)
                intent.putExtra("email", binding.editEmail.text.toString())
                intent.putExtra("password", binding.editPsw.text.toString())
                startActivity(intent)
            }
            else{
                if (binding.editPsw.text.toString() != binding.editPswCheck.text.toString()) showMessage(getString(R.string.check_password))
                else if (!binding.editEmail.text.toString().isEmail()) showMessage(getString(R.string.check_email))
                else  showMessage(getString(R.string.error))
            }
        }

        binding.arrowBack.setOnClickListener {
            onBackPressed()
        }

        binding.charvisLogo.setOnClickListener {
            onBackPressed()
        }
    }
}