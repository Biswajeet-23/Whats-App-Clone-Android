package com.example.whatsappclone.activity

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.example.whatsappclone.MainActivity
import com.example.whatsappclone.R
import com.example.whatsappclone.databinding.ActivityProfileBinding
import com.example.whatsappclone.model.UserModel
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.storage.FirebaseStorage
import java.util.Date

class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var storage: FirebaseStorage
    private lateinit var selectedImg: Uri
    private lateinit var dialog: AlertDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val builder = AlertDialog.Builder(this)
        builder.apply {
            setMessage("Updating Profile...")
            setCancelable(true)
        }
        dialog = builder.create()

        database = FirebaseDatabase.getInstance()
        storage = FirebaseStorage.getInstance()
        auth = FirebaseAuth.getInstance()

        binding.userLogo.setOnClickListener {
            val intent = Intent()
            intent.action = Intent.ACTION_GET_CONTENT
            intent.type = "image/*"
            startActivityForResult(intent, 1)
        }

        binding.profileContinueBtn.setOnClickListener {
            if(binding.userName.text!!.isEmpty()){
                Toast.makeText(this, "Please enter Your name", Toast.LENGTH_SHORT).show()
            }else if(selectedImg == null){
                Toast.makeText(this, "Please enter Your image", Toast.LENGTH_SHORT).show()
            }else{
                uploadData()
            }
        }
    }

    private fun uploadData() {
        val reference = storage.reference.child("Profile").child(Date().time.toString())
        reference.putFile(selectedImg).addOnCompleteListener{
            if(it.isSuccessful){
                reference.downloadUrl.addOnSuccessListener { task ->
                    getTokenAndUploadInfo(task.toString())
                }
            }
        }
    }

    private fun getTokenAndUploadInfo(imageUrl: String){
        var token: String? = null
        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if(!task.isSuccessful){
                Log.d("FCM error", "Error in FCM")
                return@OnCompleteListener
            }
            Log.d("FCM", task.result)
            token = task.result
            uploadInfo(imageUrl, token)
        })
    }

    private fun uploadInfo(imageUrl: String, token: String?) {
        val user = UserModel(auth.uid.toString(), binding.userName.text.toString(), auth.currentUser!!.phoneNumber.toString(), imageUrl, token)

        database.reference.child("users")
            .child(auth.uid.toString())
            .setValue(user)
            .addOnSuccessListener {
                Toast.makeText(this, "Data Successfully inserted", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(data!=null){
            if(data.data!=null){
                selectedImg = data.data!!
                binding.userLogo.setImageURI(selectedImg)
            }
        }
    }
}