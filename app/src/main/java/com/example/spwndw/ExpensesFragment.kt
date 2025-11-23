package com.example.spwndw

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ExpensesFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseDatabase
    private lateinit var recyclerView: RecyclerView
    private lateinit var expenseAdapter: ExpenseAdapter
    private var expenses = mutableListOf<Expense>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_expenses, container, false)

        auth = FirebaseAuth.getInstance()
        db = FirebaseDatabase.getInstance("https://spendw-7a319-default-rtdb.firebaseio.com/")

        val toolbar = view.findViewById<MaterialToolbar>(R.id.toolbar)
        recyclerView = view.findViewById(R.id.recycler_view_expenses)

        // Navigate back to Dashboard
        toolbar.setNavigationOnClickListener {
             // We use popBackStack because we came from Dashboard.
             // But if user wants to force "redirect in dashboard", we can navigate explicitly.
             // However, navigateUp() is cleaner. I will use navigate(R.id.nav_dashboard) to be 100% sure per request.
             findNavController().navigate(R.id.nav_dashboard)
        }

        recyclerView.layoutManager = LinearLayoutManager(context)
        
        expenseAdapter = ExpenseAdapter(expenses) { expense ->
            val bundle = Bundle().apply {
                putString("expenseId", expense.id)
                putString("name", expense.name)
                putDouble("amount", expense.amount)
                putString("category", expense.category)
                putLong("date", expense.date)
                putString("notes", expense.notes)
            }
            findNavController().navigate(R.id.action_expensesFragment_to_editExpenseFragment, bundle)
        }
        recyclerView.adapter = expenseAdapter

        fetchExpenses()

        return view
    }

    private fun fetchExpenses() {
        val userId = auth.currentUser?.uid ?: return

        db.getReference("users").child(userId).child("expenses")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    expenses.clear()
                    for (childSnapshot in snapshot.children) {
                        val expense = childSnapshot.getValue(Expense::class.java)
                        if (expense != null) {
                            expenses.add(expense)
                        }
                    }
                    // Sort by date descending
                    expenses.sortByDescending { it.date }
                    expenseAdapter.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(context, "Error fetching data: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }
}