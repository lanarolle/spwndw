package com.example.spwndw

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth

class LoginFragment : Fragment() {

    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_login, container, false)

        auth = FirebaseAuth.getInstance()

        val etEmail = view.findViewById<EditText>(R.id.et_email)
        val etPassword = view.findViewById<EditText>(R.id.et_password)
        val btnLogin = view.findViewById<Button>(R.id.btn_login)
        val tvGoToRegister = view.findViewById<TextView>(R.id.tv_go_to_register)

        // Check if user is already logged in
        if (auth.currentUser != null) {
            // User is already logged in, navigate to Dashboard
            findNavController().navigate(R.id.action_loginFragment_to_dashboardFragment)
        }

        btnLogin.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(context, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        // Login success, navigate to Dashboard
                        Toast.makeText(context, "Login Successful", Toast.LENGTH_SHORT).show()
                        findNavController().navigate(R.id.action_loginFragment_to_dashboardFragment)
                    } else {
                        // Login failed
                        Toast.makeText(context, "Login Failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                    }
                }
        }

        tvGoToRegister.setOnClickListener {
            // Navigate to Register page
            findNavController().navigate(R.id.action_loginFragment_to_registerFragment)
        }

        return view
    }
}