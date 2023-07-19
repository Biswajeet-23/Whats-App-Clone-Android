package com.example.whatsappclone.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.whatsappclone.MainActivity
import com.example.whatsappclone.databinding.ActivityPhoneNoAuthBinding
import com.google.firebase.auth.FirebaseAuth

class PhoneNoAuthActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPhoneNoAuthBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPhoneNoAuthBinding.inflate(layoutInflater)
        setContentView(binding.root)

        authentication()

        binding.continueBtn.setOnClickListener {
            if(binding.phoneNo.text!!.isEmpty()){
                Toast.makeText(this, "Please enter your phone number!!", Toast.LENGTH_SHORT).show()
            }
            else{
                val intent = Intent(this, OTPActivity::class.java)
                intent.putExtra("number", binding.phoneNo.text.toString())
                startActivity(intent)
            }
        }
    }

    private fun authentication() {
        auth = FirebaseAuth.getInstance()

        if(auth.currentUser != null){
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}