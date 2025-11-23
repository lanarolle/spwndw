package com.example.spwndw

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import com.google.android.material.appbar.MaterialToolbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.*

class ReportsFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseDatabase
    private lateinit var pieChart: PieChart
    private lateinit var barChart: BarChart

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_reports, container, false)

        auth = FirebaseAuth.getInstance()
        db = FirebaseDatabase.getInstance("https://spendw-7a319-default-rtdb.firebaseio.com/")

        val toolbar = view.findViewById<MaterialToolbar>(R.id.toolbar)
        pieChart = view.findViewById(R.id.pie_chart)
        barChart = view.findViewById(R.id.bar_chart)

        toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        fetchData()

        return view
    }

    private fun fetchData() {
        val userId = auth.currentUser?.uid ?: return

        db.getReference("users").child(userId).child("expenses")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val expenses = mutableListOf<Expense>()
                    for (childSnapshot in snapshot.children) {
                        val expense = childSnapshot.getValue(Expense::class.java)
                        if (expense != null) {
                            expenses.add(expense)
                        }
                    }
                    if (expenses.isNotEmpty()) {
                        setupPieChart(expenses)
                        setupBarChart(expenses)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(context, "Error loading reports: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun setupPieChart(expenses: List<Expense>) {
        val categoryMap = HashMap<String, Float>()
        for (expense in expenses) {
            val currentAmount = categoryMap.getOrDefault(expense.category, 0f)
            categoryMap[expense.category] = currentAmount + expense.amount.toFloat()
        }

        val entries = ArrayList<PieEntry>()
        for ((category, amount) in categoryMap) {
            entries.add(PieEntry(amount, category))
        }

        val dataSet = PieDataSet(entries, "")
        // Custom "Beautiful" Colors
        val colors = ArrayList<Int>()
        colors.add(Color.parseColor("#2563EB")) // Primary Blue
        colors.add(Color.parseColor("#9333EA")) // Purple
        colors.add(Color.parseColor("#10B981")) // Emerald Green
        colors.add(Color.parseColor("#F59E0B")) // Amber
        colors.add(Color.parseColor("#EF4444")) // Red
        colors.add(Color.parseColor("#3B82F6")) // Lighter Blue
        colors.add(Color.parseColor("#8B5CF6")) // Violet
        dataSet.colors = colors
        
        dataSet.valueTextSize = 14f
        dataSet.valueTextColor = Color.WHITE
        dataSet.sliceSpace = 3f
        dataSet.selectionShift = 5f

        val data = PieData(dataSet)
        data.setValueFormatter(object : com.github.mikephil.charting.formatter.ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                return String.format("$%.0f", value)
            }
        })

        pieChart.data = data
        pieChart.description.isEnabled = false
        pieChart.centerText = "Total\n$${String.format("%.2f", expenses.sumOf { it.amount })}"
        pieChart.setCenterTextSize(18f)
        pieChart.setCenterTextColor(Color.BLACK)
        pieChart.setEntryLabelColor(Color.WHITE)
        pieChart.setEntryLabelTextSize(12f)
        pieChart.holeRadius = 50f
        pieChart.transparentCircleRadius = 55f
        pieChart.legend.isEnabled = false // Clean look, labels are on chart
        pieChart.animateY(1400, com.github.mikephil.charting.animation.Easing.EaseInOutQuad)
        pieChart.invalidate()
    }

    private fun setupBarChart(expenses: List<Expense>) {
        // Group by Date (formatted nicely)
        val dateMap = TreeMap<String, Float>()
        // Use a map to store timestamp -> amount to sort correctly first
        val timestampMap = TreeMap<Long, Float>()
        
        // Normalize timestamps to midnight for grouping
        val cal = Calendar.getInstance()
        for (expense in expenses) {
            cal.timeInMillis = expense.date
            cal.set(Calendar.HOUR_OF_DAY, 0)
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)
            val midnight = cal.timeInMillis
            
            val currentAmount = timestampMap.getOrDefault(midnight, 0f)
            timestampMap[midnight] = currentAmount + expense.amount.toFloat()
        }

        val entries = ArrayList<BarEntry>()
        val labels = ArrayList<String>()
        var index = 0f
        val sdf = SimpleDateFormat("dd MMM", Locale.getDefault())

        for ((timestamp, amount) in timestampMap) {
            entries.add(BarEntry(index, amount))
            labels.add(sdf.format(Date(timestamp)))
            index++
        }

        val dataSet = BarDataSet(entries, "Daily Spending")
        dataSet.color = Color.parseColor("#2563EB") // Primary
        dataSet.valueTextColor = Color.BLACK
        dataSet.valueTextSize = 12f
        dataSet.highLightColor = Color.parseColor("#9333EA")

        val data = BarData(dataSet)
        data.barWidth = 0.7f // Slimmer bars

        barChart.data = data
        barChart.description.isEnabled = false
        barChart.setFitBars(true)
        
        // X-Axis Styling
        val xAxis = barChart.xAxis
        xAxis.valueFormatter = IndexAxisValueFormatter(labels)
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.granularity = 1f
        xAxis.setDrawGridLines(false)
        xAxis.textColor = Color.DKGRAY
        xAxis.textSize = 12f
        xAxis.labelRotationAngle = -45f // Slanted labels for better fit

        // Y-Axis Styling
        barChart.axisRight.isEnabled = false // Hide right axis
        barChart.axisLeft.textColor = Color.DKGRAY
        barChart.axisLeft.setDrawGridLines(true)
        
        // Zoom / Scroll
        barChart.setVisibleXRangeMaximum(7f) // Show max 7 bars at once, scroll for more
        barChart.moveViewToX(index) // Scroll to end (latest dates)
        
        barChart.animateY(1000)
        barChart.invalidate()
    }
}