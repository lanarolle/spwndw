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
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.launch
import java.util.Locale

class DashboardFragment : Fragment() {

    private val viewModel: MainViewModel by viewModels()
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseDatabase

    private lateinit var tvTotalBalance: TextView
    private lateinit var tvWelcome: TextView
    private lateinit var recyclerView: RecyclerView
    private lateinit var expenseAdapter: ExpenseAdapter
    private var expenses = mutableListOf<Expense>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_dashboard, container, false)

        auth = FirebaseAuth.getInstance()
        db = FirebaseDatabase.getInstance("https://spendw-7a319-default-rtdb.firebaseio.com/")

        tvTotalBalance = view.findViewById(R.id.tv_total_balance)
        tvWelcome = view.findViewById(R.id.tv_welcome)
        recyclerView = view.findViewById(R.id.recycler_view_transactions)
        val fab = view.findViewById<FloatingActionButton>(R.id.fab)
        val bottomNav = view.findViewById<BottomNavigationView>(R.id.bottom_navigation_view)
        val insightsButton = view.findViewById<View>(R.id.btn_get_insights)
        val toolbar = view.findViewById<com.google.android.material.appbar.MaterialToolbar>(R.id.toolbar)

        // Set User Name
        val user = auth.currentUser
        val name = user?.displayName ?: user?.email?.substringBefore('@') ?: "User"
        // Capitalize first letter if it came from email
        val formattedName = name.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
        
        tvWelcome.text = getString(R.string.welcome_user_format, formattedName)

        recyclerView.layoutManager = LinearLayoutManager(context)
        expenseAdapter = ExpenseAdapter(expenses)
        recyclerView.adapter = expenseAdapter

        fab.setOnClickListener {
            findNavController().navigate(R.id.action_dashboardFragment_to_addExpenseFragment)
        }
        
        // Handle Logout
        toolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_logout -> {
                    auth.signOut()
                    findNavController().navigate(R.id.action_global_loginFragment)
                    true
                }
                else -> false
            }
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

        db.getReference("users").child(userId).child("expenses")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    var total = 0.0
                    val newExpenses = mutableListOf<Expense>()
                    
                    for (childSnapshot in snapshot.children) {
                        val expense = childSnapshot.getValue(Expense::class.java)
                        if (expense != null) {
                            newExpenses.add(expense)
                            total += expense.amount
                        }
                    }

                    // Sort by date descending
                    newExpenses.sortByDescending { it.date }

                    tvTotalBalance.text = String.format("$%.2f", total)

                    expenses.clear()
                    expenses.addAll(newExpenses)
                    expenseAdapter.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(context, "Error fetching data: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }
}