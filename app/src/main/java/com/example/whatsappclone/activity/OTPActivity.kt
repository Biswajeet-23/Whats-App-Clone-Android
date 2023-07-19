package com.example.whatsappclone.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.example.whatsappclone.R
import com.example.whatsappclone.databinding.ActivityOtpactivityBinding
import com.google.firebase.FirebaseException
import com.google.firebase.auth.*
import java.util.concurrent.TimeUnit

class OTPActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOtpactivityBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var dialog: AlertDialog
    private lateinit var verificatonId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOtpactivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        val builder = AlertDialog.Builder(this)
        builder.apply {
            setMessage("Please Wait")
            setTitle("Loading")
            setCancelable(false)
        }
        dialog = builder.create()
        dialog.show()
        val contact = intent.getStringExtra("number")
        val phNo = "+91$contact"

        Log.d("phone No", "$contact")
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phNo)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(this)
            .setCallbacks(object: PhoneAuthProvider.OnVerificationStateChangedCallbacks(){
                override fun onVerificationCompleted(p0: PhoneAuthCredential) {
                    Log.d("On Verification completed", "$p0")
                }

                override fun onVerificationFailed(p0: FirebaseException) {
                    dialog.dismiss()
                    Log.d("On Verification failed", "$p0")
                }

                override fun onCodeSent(p0: String, p1: PhoneAuthProvider.ForceResendingToken) {

                    dialog.dismiss()
                    verificatonId = p0
                }

            }).build()
        PhoneAuthProvider.verifyPhoneNumber(options)

        binding.continueBtn.setOnClickListener {
            if(binding.otpNo.text!!.isEmpty()){
                Toast.makeText(this, "Please enter the OTP", Toast.LENGTH_SHORT).show()
            }else{
                dialog.show()
                val credential = PhoneAuthProvider.getCredential(verificatonId, binding.otpNo.text!!.toString())
                signInWithPhoneAuthCredential(credential)
            }
        }
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    dialog.dismiss()
                    // Sign in success, update UI with the signed-in user's information
                    Log.d("on Verified", "signInWithCredential:success")
                    val user = task.result?.user
                    startActivity(Intent(this, ProfileActivity::class.java))
                    finish()
                } else {
                    dialog.dismiss()
                    // Sign in failed, display a message and update the UI
                    Log.d("on Verify failed", "signInWithCredential:failure"+task.exception)
                    if (task.exception is FirebaseAuthInvalidCredentialsException) {
                        // The verification code entered was invalid
                        Log.d("on Verify failed", "invalid OTP")
                    }
                    // Update UI
                }
            }
    }
}