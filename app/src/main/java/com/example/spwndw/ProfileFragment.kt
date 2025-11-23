package com.example.spwndw

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.appbar.MaterialToolbar
import com.google.firebase.auth.FirebaseAuth
import java.util.Locale

class ProfileFragment : Fragment() {

    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        auth = FirebaseAuth.getInstance()

        val toolbar = view.findViewById<MaterialToolbar>(R.id.toolbar)
        val tvName = view.findViewById<TextView>(R.id.tv_profile_name)
        val tvEmail = view.findViewById<TextView>(R.id.tv_profile_email)
        val btnLogout = view.findViewById<Button>(R.id.btn_logout)

        // Set User Info
        val user = auth.currentUser
        val name = user?.displayName ?: user?.email?.substringBefore('@') ?: "User"
        val formattedName = name.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
        
        tvName.text = formattedName
        tvEmail.text = user?.email ?: "No Email"

        // Back Button
        toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        // Logout
        btnLogout.setOnClickListener {
            auth.signOut()
            findNavController().navigate(R.id.action_global_loginFragment)
        }

        return view
    }
}