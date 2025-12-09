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
import com.github.mikephil.charting.formatter.ValueFormatter
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
        val colors = ArrayList<Int>()
        colors.add(Color.parseColor("#2563EB"))
        colors.add(Color.parseColor("#9333EA"))
        colors.add(Color.parseColor("#10B981"))
        colors.add(Color.parseColor("#F59E0B"))
        colors.add(Color.parseColor("#EF4444"))
        colors.add(Color.parseColor("#3B82F6"))
        colors.add(Color.parseColor("#8B5CF6"))
        dataSet.colors = colors
        
        dataSet.valueTextSize = 14f
        dataSet.valueTextColor = Color.WHITE
        dataSet.sliceSpace = 3f
        dataSet.selectionShift = 5f

        val data = PieData(dataSet)
        data.setValueFormatter(object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                return "LKR %.0f".format(value)
            }
        })

        pieChart.data = data
        pieChart.description.isEnabled = false
        pieChart.centerText = "Total\nLKR ${String.format("%.2f", expenses.sumOf { it.amount })}"
        pieChart.setCenterTextSize(18f)
        pieChart.setCenterTextColor(Color.BLACK)
        pieChart.setEntryLabelColor(Color.WHITE)
        pieChart.setEntryLabelTextSize(12f)
        pieChart.holeRadius = 50f
        pieChart.transparentCircleRadius = 55f
        pieChart.legend.isEnabled = false
        pieChart.animateY(1400, com.github.mikephil.charting.animation.Easing.EaseInOutQuad)
        pieChart.invalidate()
    }

    private fun setupBarChart(expenses: List<Expense>) {
        val timestampMap = TreeMap<Long, Float>()
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

        val dataSet = BarDataSet(entries, "Daily Spending (LKR)")
        dataSet.color = Color.parseColor("#2563EB")
        dataSet.valueTextColor = Color.BLACK
        dataSet.valueTextSize = 12f
        dataSet.highLightColor = Color.parseColor("#9333EA")

        val data = BarData(dataSet)
        data.setValueFormatter(object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                return "%.0f".format(value)
            }
        })
        data.barWidth = 0.7f

        barChart.data = data
        barChart.description.isEnabled = false
        barChart.setFitBars(true)
        
        val xAxis = barChart.xAxis
        xAxis.valueFormatter = IndexAxisValueFormatter(labels)
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.granularity = 1f
        xAxis.setDrawGridLines(false)
        xAxis.textColor = Color.DKGRAY
        xAxis.textSize = 12f
        xAxis.labelRotationAngle = -45f

        barChart.axisRight.isEnabled = false
        barChart.axisLeft.textColor = Color.DKGRAY
        barChart.axisLeft.setDrawGridLines(true)
        barChart.axisLeft.valueFormatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                return "LKR %.0f".format(value)
            }
        }
        
        barChart.setVisibleXRangeMaximum(7f)
        barChart.moveViewToX(index)
        
        barChart.animateY(1000)
        barChart.invalidate()
    }
}