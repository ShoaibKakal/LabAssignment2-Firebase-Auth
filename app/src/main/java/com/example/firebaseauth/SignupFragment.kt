package com.example.firebaseauth

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.firebaseauth.databinding.FragmentSignupBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class SignupFragment : Fragment() {

    private lateinit var binding: FragmentSignupBinding
    private lateinit var auth: FirebaseAuth

    private val db = Firebase.firestore
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {


        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        auth = Firebase.auth
        binding = FragmentSignupBinding.inflate(inflater)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val name = binding.fnameEt.text
        val phoneNumber = binding.phoneEt.text
        val email = binding.emailEt.text
        val password = binding.pwdEt.text


        binding.SignUpBtn.setOnClickListener {

            if (name.isNotEmpty() || phoneNumber.isNotEmpty() || email.isNotEmpty() || password.isNotEmpty()) {
                if (password.length < 8) {
                    Toast.makeText(
                        requireContext(),
                        "Password should be greater than 8 chars",
                        Toast.LENGTH_LONG
                    ).show()

                } else {
                    createAccount(
                        email.toString(),
                        password.toString(),
                        name.toString(),
                        phoneNumber.toString()
                    )
                }
            } else {
                Toast.makeText(requireContext(), "Some Fields are empty", Toast.LENGTH_LONG).show()
            }

        }

        binding.alreadyAccountTv.setOnClickListener {
            findNavController().navigate(R.id.action_signupFragment_to_loginFragment)
        }
    }


    private fun createAccount(email: String, pwd: String, name: String, phoneNumber: String) {
        auth.createUserWithEmailAndPassword(email, pwd)
            .addOnCompleteListener(requireActivity()) { task ->

                if (task.isSuccessful) {
                    // Sign in is successfull, update the UI accourdinly.
                    Log.d(TAG, "createAccount: Success")
                    Toast.makeText(
                        requireContext(), "Authentication Successfull.",
                        Toast.LENGTH_SHORT
                    ).show()
                    val currentUser = auth.currentUser?.uid

                    saveDateInFirestore(currentUser!!, name, phoneNumber)

                } else {
                    // display an error message
                    Log.d(TAG, "createUserWithEmail:failure", task.exception)
                    Toast.makeText(
                        requireContext(), "Authentication failed.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }


    private fun saveDateInFirestore(currentUser: String, name: String, phoneNumber: String) {
        val user = hashMapOf(
            "name" to name,
            "phoneNumber" to phoneNumber
        )

        db.collection("users")
            .document(currentUser)
            .set(user)
            .addOnSuccessListener { Log.d(TAG, "saveDateInFirestore: Data Inserted") }
            .addOnFailureListener { e ->
                Log.d(TAG, "saveDateInFirestore: ${e.message}")
            }


    }


}