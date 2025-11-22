package com.example.spwndw

data class Expense(
    var id: String = "",
    val name: String = "",
    val amount: Double = 0.0,
    val category: String = "",
    val date: Long = 0L, // Storing as timestamp for Realtime Database
    val notes: String = ""
)