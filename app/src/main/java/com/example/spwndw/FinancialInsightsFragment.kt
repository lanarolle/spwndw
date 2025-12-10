package com.example.spwndw

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.ProgressBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.appbar.MaterialToolbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.launch

class FinancialInsightsFragment : Fragment() {

    private val viewModel: MainViewModel by activityViewModels() // Use activityViewModels to share with Dashboard if needed
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseDatabase

    private lateinit var tvDetailedAnalysis: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var btnGenerate: View
    private val expenses = mutableListOf<Expense>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_financial_insights, container, false)

        auth = FirebaseAuth.getInstance()
        db = FirebaseDatabase.getInstance("https://spendw-7a319-default-rtdb.firebaseio.com/")

        tvDetailedAnalysis = view.findViewById(R.id.tv_detailed_analysis)
        progressBar = view.findViewById(R.id.progressBar)
        btnGenerate = view.findViewById(R.id.btn_generate_insights)
        val toolbar = view.findViewById<MaterialToolbar>(R.id.toolbar)

        toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        btnGenerate.setOnClickListener {
            if (expenses.isEmpty()) {
                Toast.makeText(context, "No transactions found to analyze.", Toast.LENGTH_SHORT).show()
                fetchExpenses() // Try fetching if empty
            } else {
                generateInsights()
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.financialInsights.collect { insights ->
                if (insights != null) {
                    progressBar.visibility = View.GONE
                    tvDetailedAnalysis.text = insights
                    if (insights.startsWith("Error")) {
                         Toast.makeText(context, "Failed to generate insights.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        fetchExpenses()

        return view
    }

    private fun fetchExpenses() {
        val userId = auth.currentUser?.uid ?: return

        db.getReference("users").child(userId).child("expenses")
            .limitToLast(20) // Analyze last 20 transactions
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    expenses.clear()
                    for (childSnapshot in snapshot.children) {
                        val expense = childSnapshot.getValue(Expense::class.java)
                        if (expense != null) {
                            expenses.add(expense)
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(context, "Error fetching data: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun generateInsights() {
        progressBar.visibility = View.VISIBLE
        tvDetailedAnalysis.text = "Generating analysis..."
        
        val transactions = expenses.joinToString("\n") { "${it.name}: ${it.amount} (${it.category})" }
        viewModel.getFinancialInsights(transactions)
    }
}