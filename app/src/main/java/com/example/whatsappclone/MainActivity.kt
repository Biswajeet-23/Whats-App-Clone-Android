package com.example.whatsappclone

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.example.whatsappclone.activity.PhoneNoAuthActivity
import com.example.whatsappclone.adapter.ViewPagerAdapter
import com.example.whatsappclone.databinding.ActivityMainBinding
import com.example.whatsappclone.ui.CallFragment
import com.example.whatsappclone.ui.ChatFragment
import com.example.whatsappclone.ui.StatusFragment
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        authentication()

        val viewPager = binding.viewPager
        val tabLayout = binding.tabLayout

        val fragmentList = arrayListOf(
            ChatFragment(),
            StatusFragment(),
            CallFragment()
        )

        val pagerAdapter = ViewPagerAdapter(this, supportFragmentManager, lifecycle, fragmentList)
        binding.viewPager.adapter = pagerAdapter

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            when(position){
                0 -> tab.text = "Chats"
                1 -> tab.text = "Status"
                2 -> tab.text = "Call"
            }
        }.attach()
    }

    private fun authentication() {
        auth = FirebaseAuth.getInstance()

        if(auth.currentUser == null){
            val intent = Intent(this, PhoneNoAuthActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}