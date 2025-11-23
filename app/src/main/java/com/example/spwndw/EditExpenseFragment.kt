package com.example.spwndw

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import java.text.SimpleDateFormat
import java.util.*

class EditExpenseFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseDatabase
    private var expenseId: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_edit_expense, container, false)

        auth = FirebaseAuth.getInstance()
        db = FirebaseDatabase.getInstance("https://spendw-7a319-default-rtdb.firebaseio.com/")

        val toolbar = view.findViewById<MaterialToolbar>(R.id.toolbar)
        val etExpenseName = view.findViewById<TextInputEditText>(R.id.et_expense_name)
        val etAmount = view.findViewById<TextInputEditText>(R.id.et_amount)
        val actCategory = view.findViewById<AutoCompleteTextView>(R.id.act_category)
        val etDate = view.findViewById<TextInputEditText>(R.id.et_date)
        val etNotes = view.findViewById<TextInputEditText>(R.id.et_notes)
        val btnUpdate = view.findViewById<Button>(R.id.btn_update)
        val btnDelete = view.findViewById<Button>(R.id.btn_delete)

        // Back Function - Redirect to Dashboard/Previous
        toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        // Setup Category Dropdown
        val categories = arrayOf("Food", "Transport", "Shopping", "Bills", "Entertainment", "Other")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, categories)
        actCategory.setAdapter(adapter)

        // Date Picker
        etDate.setOnClickListener {
            val datePicker = MaterialDatePicker.Builder.datePicker().build()
            datePicker.addOnPositiveButtonClickListener {
                val date = Date(it)
                val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                etDate.setText(format.format(date))
            }
            datePicker.show(childFragmentManager, "datePicker")
        }

        // Populate Data
        arguments?.let {
            expenseId = it.getString("expenseId")
            etExpenseName.setText(it.getString("name"))
            etAmount.setText(it.getDouble("amount").toString())
            actCategory.setText(it.getString("category"), false)
            val dateMillis = it.getLong("date")
            if (dateMillis > 0) {
                val date = Date(dateMillis)
                val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                etDate.setText(format.format(date))
            }
            etNotes.setText(it.getString("notes"))
        }

        // Update Logic
        btnUpdate.setOnClickListener {
            val name = etExpenseName.text.toString().trim()
            val amountStr = etAmount.text.toString().trim()
            val category = actCategory.text.toString().trim()
            val dateStr = etDate.text.toString().trim()
            val notes = etNotes.text.toString().trim()

            if (name.isEmpty() || amountStr.isEmpty() || category.isEmpty() || dateStr.isEmpty()) {
                Toast.makeText(context, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val userId = auth.currentUser?.uid ?: return@setOnClickListener
            val id = expenseId ?: return@setOnClickListener

            val dateMillis = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(dateStr)?.time ?: System.currentTimeMillis()

            val expense = Expense(
                id = id,
                name = name,
                amount = amountStr.toDouble(),
                category = category,
                date = dateMillis,
                notes = notes
            )

            db.getReference("users").child(userId).child("expenses").child(id)
                .setValue(expense)
                .addOnSuccessListener {
                    Toast.makeText(context, "Expense updated", Toast.LENGTH_SHORT).show()
                    findNavController().navigateUp()
                }
                .addOnFailureListener {
                    Toast.makeText(context, "Failed to update", Toast.LENGTH_SHORT).show()
                }
        }

        // Delete Logic
        btnDelete.setOnClickListener {
            val userId = auth.currentUser?.uid ?: return@setOnClickListener
            val id = expenseId ?: return@setOnClickListener

            db.getReference("users").child(userId).child("expenses").child(id)
                .removeValue()
                .addOnSuccessListener {
                    Toast.makeText(context, "Expense deleted", Toast.LENGTH_SHORT).show()
                    findNavController().navigateUp()
                }
                .addOnFailureListener {
                    Toast.makeText(context, "Failed to delete", Toast.LENGTH_SHORT).show()
                }
        }

        return view
    }
}