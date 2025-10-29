package com.example.spwndw

import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class Expense(
    val name: String = "",
    val amount: Double = 0.0,
    val category: String = "",
    @ServerTimestamp
    val date: Date? = null,
    val notes: String = ""
)