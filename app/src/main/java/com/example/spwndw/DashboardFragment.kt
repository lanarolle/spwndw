package com.example.spwndw

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.launch

class DashboardFragment : Fragment() {

    private val viewModel: MainViewModel by viewModels()
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private lateinit var tvTotalBalance: TextView
    private lateinit var recyclerView: RecyclerView
    private lateinit var expenseAdapter: ExpenseAdapter
    private var expenses = mutableListOf<Expense>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_dashboard, container, false)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        tvTotalBalance = view.findViewById(R.id.tv_total_balance)
        recyclerView = view.findViewById(R.id.recycler_view_transactions)
        val fab = view.findViewById<FloatingActionButton>(R.id.fab)
        val bottomNav = view.findViewById<BottomNavigationView>(R.id.bottom_navigation_view)
        val insightsButton = view.findViewById<View>(R.id.btn_get_insights)

        recyclerView.layoutManager = LinearLayoutManager(context)
        expenseAdapter = ExpenseAdapter(expenses)
        recyclerView.adapter = expenseAdapter

        fab.setOnClickListener {
            findNavController().navigate(R.id.action_dashboardFragment_to_addExpenseFragment)
        }

        bottomNav.setOnItemSelectedListener {
            when(it.itemId) {
                R.id.nav_dashboard -> {
                    // Already on dashboard
                    true
                }
                R.id.nav_expenses -> {
                    // Navigate to expenses
                    true
                }
                R.id.nav_reports -> {
                    // Navigate to reports
                    true
                }
                R.id.nav_profile -> {
                    // Navigate to profile
                    true
                }
                else -> false
            }
        }
        
        insightsButton.setOnClickListener {
            val transactions = expenses.joinToString("\n") { "${it.name}: $${it.amount}" }
            viewModel.getFinancialInsights(transactions)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.financialInsights.collect { insights ->
                if (insights != null) {
                    // Display the insights in a dialog or a toast
                    Toast.makeText(context, insights, Toast.LENGTH_LONG).show()
                }
            }
        }

        fetchExpenses()

        return view
    }

    private fun fetchExpenses() {
        val userId = auth.currentUser?.uid ?: return

        db.collection("users").document(userId).collection("expenses")
            .orderBy("date", Query.Direction.DESCENDING)
            .limit(5)
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    Toast.makeText(context, "Error fetching data", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                if (snapshots != null) {
                    var total = 0.0
                    val newExpenses = snapshots.toObjects(Expense::class.java)
                    for (expense in newExpenses) {
                        total += expense.amount
                    }
                    tvTotalBalance.text = String.format("$%.2f", total)

                    expenses.clear()
                    expenses.addAll(newExpenses)
                    expenseAdapter.notifyDataSetChanged()
                }
            }
    }
}