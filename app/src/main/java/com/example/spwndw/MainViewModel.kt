package com.example.spwndw

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.ai.client.generativeai.GenerativeModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {

    // Initialize the GenerativeModel
    private val generativeModel = GenerativeModel(
        modelName = "gemini-2.5-flash-preview-09-2025",
        // DO NOT hardcode your API key. Use a secure method.
        // For testing, you can pass it here.
        apiKey = "YOUR_API_KEY_HERE"
    )

    private val _categorySuggestion = MutableStateFlow<String?>(null)
    val categorySuggestion: StateFlow<String?> = _categorySuggestion.asStateFlow()

    private val _financialInsights = MutableStateFlow<String?>(null)
    val financialInsights: StateFlow<String?> = _financialInsights.asStateFlow()

    fun getSmartCategory(expenseName: String) {
        viewModelScope.launch {
            try {
                val prompt = "Categorize this expense: '$expenseName'. " +
                             "Respond with only one category: Food, Transport, Shopping, Bills, Entertainment, Other."

                val response = generativeModel.generateContent(prompt)
                _categorySuggestion.value = response.text
            } catch (e: Exception) {
                _categorySuggestion.value = "Error: ${e.message}"
            }
        }
    }

    fun getFinancialInsights(transactions: String) {
        viewModelScope.launch {
             try {
                val systemInstruction = "You are a friendly financial advisor for the SpendWise app. Provide concise, helpful, and encouraging financial advice in a few bullet points."
                val prompt = "Here are my recent transactions:\n$transactions\n\nProvide some actionable financial insights."

                val response = generativeModel.generateContent(prompt)
                _financialInsights.value = response.text
            } catch (e: Exception) {
                _financialInsights.value = "Error: ${e.message}"
            }
        }
    }
}