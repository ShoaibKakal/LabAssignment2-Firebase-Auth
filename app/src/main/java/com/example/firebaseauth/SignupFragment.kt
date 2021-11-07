package com.example.firebaseauth

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.opengl.ETC1.encodeImage
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.firebaseauth.databinding.FragmentSignupBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.io.ByteArrayOutputStream
import java.io.FileNotFoundException
import java.io.InputStream

class SignupFragment : Fragment() {

    private lateinit var binding: FragmentSignupBinding
    private lateinit var auth: FirebaseAuth
    private var enCodedImage: String? = null

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




        binding.layoutImage.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            pickImage.launch(intent)

        }


        val name = binding.inputName.text
        val email = binding.inputEmail.text
        val password = binding.inputPassword.text


        binding.buttonSignUp.setOnClickListener {

            if (isValidSignUpDetails()) {
                    loading(true)
                    createAccount(
                        email.toString(),
                        password.toString(),
                        name.toString(),
                    )
                }
            else {
                Toast.makeText(requireContext(), "Some Fields are empty", Toast.LENGTH_LONG).show()
            }
            }



        binding.textSignIn.setOnClickListener {
            findNavController().navigate(R.id.action_signupFragment_to_loginFragment)
        }
    }


    private fun createAccount(email: String, pwd: String, name: String) {
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

                    saveDateInFirestore(currentUser!!, name, email)

                } else {
                    loading(false)
                    // display an error message
                    Log.d(TAG, "createUserWithEmail:failure", task.exception)
                    Toast.makeText(
                        requireContext(), "Authentication failed. Try Again",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }


    private fun saveDateInFirestore(currentUser: String, name: String, phoneNumber: String) {
        loading(false)

        val user = hashMapOf(
            "name" to name,
            "phoneNumber" to phoneNumber,
            "image" to enCodedImage
        )

        db.collection("users")
            .document(currentUser)
            .set(user)
            .addOnSuccessListener { Log.d(TAG, "saveDateInFirestore: Data Inserted") }
            .addOnFailureListener { e ->
                Log.d(TAG, "saveDateInFirestore: ${e.message}")
            }

        startActivity(Intent(requireActivity(), MainActivity::class.java))
        requireActivity().finish()
    }

    private val pickImage: ActivityResultLauncher<Intent> = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
        ActivityResultCallback {
            if (it.resultCode == RESULT_OK) {
                if (it.data != null) {
                    val imageUri: Uri = it.data!!.data!!
                    try {
                        val inputStream: InputStream =
                            requireContext().contentResolver.openInputStream(imageUri)!!
                        val bitmap: Bitmap = BitmapFactory.decodeStream(inputStream)
                        binding.imageProfile.setImageBitmap(bitmap)
                        binding.textAddImage.visibility = View.GONE
                        enCodedImage = encodeImage(bitmap)
                    } catch (e: FileNotFoundException) {
                        Log.d(TAG, "${e.printStackTrace()}: ")
                    }
                }
            }
        }
    )



    private fun encodeImage(bitmap: Bitmap): String {
        val previewWidth = 150
        val previewHeight = bitmap.height * previewWidth / bitmap.width
        val previewBitmap = Bitmap.createScaledBitmap(bitmap, previewWidth, previewHeight, false)
        val byteArrayOutputStream = ByteArrayOutputStream()
        previewBitmap.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream)
        val bytes = byteArrayOutputStream.toByteArray()
        return Base64.encodeToString(bytes, Base64.DEFAULT)
    }


    private fun isValidSignUpDetails(): Boolean {

        if (enCodedImage == null) {
            showToast("Select profile image")
            return false
        } else if (binding.inputName.text.toString().trim().isEmpty()) {
            showToast("Enter name")
            return false
        } else if (binding.inputEmail.text.toString().trim().isEmpty()) {
            showToast("Enter email")
            return false
        } else if (!Patterns.EMAIL_ADDRESS.matcher(binding.inputEmail.text.toString()).matches()) {
            showToast("Enter a valid email")
            return false
        } else if (binding.inputPassword.text.toString().trim().isEmpty()) {
            showToast("Enter password")
            return false
        } else if (binding.inputConfirmPassword.text.toString().trim().isEmpty()) {
            showToast("Confirm your password")
            return false
        } else if (binding.inputPassword.text.toString() != binding.inputConfirmPassword.text.toString()) {
            showToast("Password & confirm password must be same")
            return false
        } else {
            return true
        }
    }



    private fun showToast(msg:String)
    {
        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
    }


    private fun loading(isLoading: Boolean) {
        if (isLoading) {
            binding.buttonSignUp.visibility = View.GONE
            binding.progressBar.visibility = View.VISIBLE
        } else {
            binding.buttonSignUp.visibility = View.VISIBLE
            binding.progressBar.visibility = View.GONE

        }
    }

}