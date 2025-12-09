package com.example.spwndw

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ExpenseAdapter(
    private val expenses: List<Expense>,
    private val onItemClick: (Expense) -> Unit
) : RecyclerView.Adapter<ExpenseAdapter.ExpenseViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExpenseViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_expense, parent, false)
        return ExpenseViewHolder(view)
    }

    override fun onBindViewHolder(holder: ExpenseViewHolder, position: Int) {
        val expense = expenses[position]
        holder.bind(expense)
    }

    override fun getItemCount(): Int = expenses.size

    inner class ExpenseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ivCategoryIcon: ImageView = itemView.findViewById(R.id.iv_category_icon)
        private val tvExpenseName: TextView = itemView.findViewById(R.id.tv_expense_name)
        private val tvCategory: TextView = itemView.findViewById(R.id.tv_category)
        private val tvAmount: TextView = itemView.findViewById(R.id.tv_amount)

        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClick(expenses[position])
                }
            }
        }

        fun bind(expense: Expense) {
            tvExpenseName.text = expense.name
            tvCategory.text = expense.category
            tvAmount.text = String.format("-LKR %.2f", expense.amount)

            val icon = when (expense.category) {
                "Food" -> R.drawable.ic_food
                "Transport" -> R.drawable.ic_transport
                "Shopping" -> R.drawable.ic_shopping_cart
                "Bills" -> R.drawable.ic_bills
                "Entertainment" -> R.drawable.ic_entertainment
                else -> R.drawable.ic_other
            }
            ivCategoryIcon.setImageResource(icon)
        }
    }
}