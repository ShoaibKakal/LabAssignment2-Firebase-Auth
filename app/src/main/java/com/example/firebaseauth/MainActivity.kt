package com.example.firebaseauth

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.firebaseauth.databinding.ActivityMainBinding
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

const val TAG = "testTag"

class MainActivity : AppCompatActivity() {

    private val db = Firebase.firestore
    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        val currentUserId = intent.getStringExtra("currentUserId")

        Log.d(TAG, "onCreate: current User Id: $currentUserId")
        db.collection("users")
            .document(currentUserId!!)
            .get()
            .addOnSuccessListener {
                Log.d(TAG, "onCreate: ${it.data}")
                binding.progressBar.visibility = View.GONE
                binding.nameTV.text = it["name"].toString()
                binding.phoneNumberTV.text = it["phoneNumber"].toString()
            }
    }


}