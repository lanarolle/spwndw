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
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.appbar.MaterialToolbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class AddExpenseFragment : Fragment() {

    private val viewModel: MainViewModel by viewModels()
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_add_expense, container, false)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val toolbar = view.findViewById<MaterialToolbar>(R.id.toolbar)
        val etExpenseName = view.findViewById<TextInputEditText>(R.id.et_expense_name)
        val btnSmartCategory = view.findViewById<Button>(R.id.btn_smart_category)
        val etAmount = view.findViewById<TextInputEditText>(R.id.et_amount)
        val actCategory = view.findViewById<AutoCompleteTextView>(R.id.act_category)
        val etDate = view.findViewById<TextInputEditText>(R.id.et_date)
        val etNotes = view.findViewById<TextInputEditText>(R.id.et_notes)
        val btnSaveExpense = view.findViewById<Button>(R.id.btn_save_expense)

        toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        val categories = arrayOf("Food", "Transport", "Shopping", "Bills", "Entertainment", "Other")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, categories)
        actCategory.setAdapter(adapter)

        etDate.setOnClickListener {
            val datePicker = MaterialDatePicker.Builder.datePicker().build()
            datePicker.addOnPositiveButtonClickListener {
                val date = Date(it)
                val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                etDate.setText(format.format(date))
            }
            datePicker.show(childFragmentManager, "datePicker")
        }

        btnSmartCategory.setOnClickListener {
            val expenseName = etExpenseName.text.toString().trim()
            if (expenseName.isEmpty()) {
                Toast.makeText(context, "Please enter an expense name", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            viewModel.getSmartCategory(expenseName)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.categorySuggestion.collect { category ->
                if (category != null) {
                    actCategory.setText(category, false)
                }
            }
        }

        btnSaveExpense.setOnClickListener {
            val expenseName = etExpenseName.text.toString().trim()
            val amount = etAmount.text.toString().trim()
            val category = actCategory.text.toString().trim()
            val date = etDate.text.toString().trim()
            val notes = etNotes.text.toString().trim()

            if (expenseName.isEmpty() || amount.isEmpty() || category.isEmpty() || date.isEmpty()) {
                Toast.makeText(context, "Please fill all required fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val expense = Expense(
                name = expenseName,
                amount = amount.toDouble(),
                category = category,
                notes = notes,
                date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(date)
            )

            val userId = auth.currentUser?.uid ?: return@setOnClickListener

            db.collection("users").document(userId).collection("expenses")
                .add(expense)
                .addOnSuccessListener {
                    Toast.makeText(context, "Expense added successfully", Toast.LENGTH_SHORT).show()
                    findNavController().navigateUp()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(context, "Error adding expense: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }

        return view
    }
}