package com.crc.har.statistics

import android.app.DatePickerDialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.anychart.AnyChart
import com.anychart.AnyChartView
import com.anychart.chart.common.dataentry.DataEntry
import com.anychart.chart.common.dataentry.ValueDataEntry
import com.anychart.charts.Cartesian
import com.anychart.data.Set
import com.anychart.enums.Anchor
import com.anychart.enums.MarkerType
import com.anychart.enums.TooltipPositionMode
import com.crc.har.R
import com.crc.har.base.CommonUtils
import com.crc.har.base.Constants
import com.crc.har.base.MonthlyStatisticData
import com.crc.har.database.DBTemperatureModel
import io.realm.Realm
import io.realm.RealmResults
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class StatisticTemperatureMonthFragment : Fragment()  {
    val realm: Realm = Realm.getDefaultInstance()

    private var acChartMonth : AnyChartView? = null
    private var tvDay : TextView? = null
    private var mContext : Context? = null

    private var tvMonthlyBPM : TextView? = null
    private var tvMinimumBPM : TextView? = null
    private var tvMaximumBPM : TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {

        val view: View = inflater.inflate(R.layout.fragment_statistic_month, container, false)

        mContext = container!!.context

        acChartMonth = view.findViewById(R.id.acChartMonth)
        tvMonthlyBPM = view.findViewById(R.id.tvMonthlyBPM)
        tvMinimumBPM = view.findViewById(R.id.tvMinimumBPM)
        tvMaximumBPM = view.findViewById(R.id.tvMaximumBPM)

        val calendar = Calendar.getInstance()

        val dateDialogListener = DatePickerDialog.OnDateSetListener { _view, year, month, dayOfMonth ->
            calendar.set(Calendar.YEAR, year)
            calendar.set(Calendar.MONTH, month)
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)

            val dateFormat = "yyyy/MM"
            val sdf = SimpleDateFormat(dateFormat, Locale.KOREA)
            tvDay?.text = sdf.format(calendar.time)

            Constants.curYearOfMonth = year
            Constants.curMonthOfMonth = month + 1

            acChartMonth!!.clear()
            acChartMonth!!.invalidate()

            drwaChart()

        }

        tvDay = view.findViewById(R.id.tvDay)
        tvDay?.setOnClickListener {
            DatePickerDialog(requireContext(), dateDialogListener, calendar.get(Calendar.YEAR), calendar.get(
                Calendar.MONTH), calendar.get(
                Calendar.DAY_OF_MONTH)).show()


        }

        drwaChart()

        return view
    }

    private fun drwaChart() {

        val commonUtils = CommonUtils()
        val calendar = Calendar.getInstance()
        val maxDay = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        var monthlyTemperature : Long = 0
        var maxTemperature : Int = 0
        var minTemperature : Int = 0
        //var monthlyBPMData = MonthlyStatisticData()
        val almonthlyTemperature = ArrayList<MonthlyStatisticData>()

        for(i in 1..maxDay) {
            val monthlyTemperatureData = MonthlyStatisticData()
            val monthlyResult : RealmResults<DBTemperatureModel> = realm.where(DBTemperatureModel::class.java).equalTo("year", Constants.curYearOfMonth)
                .equalTo( "month", Constants.curMonthOfMonth )
                .equalTo("day", i)
                .findAll()

            monthlyTemperature = 0
            maxTemperature = 0
            minTemperature = 300
            for(result in monthlyResult) {
                if(maxTemperature < result.temperature) {
                    maxTemperature = result.temperature
                }
                if(minTemperature > result.temperature) {
                    minTemperature = result.temperature
                }
                monthlyTemperature += result.temperature


            }

            if(monthlyTemperature > 0) {
                monthlyTemperatureData.nDay = i
                monthlyTemperatureData.nBPM = (monthlyTemperature / monthlyResult.size).toInt()
                monthlyTemperatureData.nMax = maxTemperature
                monthlyTemperatureData.nMin = minTemperature

                almonthlyTemperature.add(monthlyTemperatureData)
            }
        }


        val cartesian: Cartesian = AnyChart.line()
        cartesian.animation(true)
        cartesian.padding(10, 20, 5, 20)

        cartesian.crosshair().enabled(true)
        cartesian.crosshair()
            .yLabel(true)

        cartesian.tooltip().positionMode(TooltipPositionMode.POINT)

        //cartesian.title()

        cartesian.yAxis(0).title("Temperature")
        cartesian.xAxis(0).labels().padding(5, 5, 5, 5)

        val seriesData =  ArrayList<DataEntry>()

        if(almonthlyTemperature.size < 1) {
            seriesData.add(CustomDataEntry("0", 0, 0, 0))
        } else {
            for(temperatureData in almonthlyTemperature) {
                seriesData.add(CustomDataEntry(temperatureData.nDay.toString(), temperatureData.nBPM, temperatureData.nMin, temperatureData.nMax))
            }
        }

        val set = Set.instantiate()
        set.data(seriesData)
        val series1Mapping = set.mapAs("{ x: 'x', value: 'value' }")
        val series2Mapping = set.mapAs("{ x: 'x', value: 'value2' }")
        val series3Mapping = set.mapAs("{ x: 'x', value: 'value3' }")

        val series1 = cartesian.line(series1Mapping)
        series1.name(getString(R.string.str_statistic_temperature));
        series1.hovered().markers().enabled(true)
        series1.hovered().markers()
            .type(MarkerType.CIRCLE)
            .size(4)
        series1.tooltip()
            .position("right")
            .anchor(Anchor.LEFT_CENTER)
            .offsetX(5)
            .offsetY(5)

        val series2 = cartesian.line(series2Mapping)
        series2.name(getString(R.string.str_statistic_minimum));
        series2.hovered().markers().enabled(true)
        series2.hovered().markers()
            .type(MarkerType.CIRCLE)
            .size(4)
        series2.tooltip()
            .position("right")
            .anchor(Anchor.LEFT_CENTER)
            .offsetX(5)
            .offsetY(5)

        val series3 = cartesian.line(series3Mapping)
        series3.name(getString(R.string.str_statistic_maximum));
        series3.hovered().markers().enabled(true)
        series3.hovered().markers()
            .type(MarkerType.CIRCLE)
            .size(4)
        series3.tooltip()
            .position("right")
            .anchor(Anchor.LEFT_CENTER)
            .offsetX(5)
            .offsetY(5)

        cartesian.legend().enabled(true)
        cartesian.legend().fontSize(13)
        val padding = cartesian.legend().padding(0, 0, 10, 0)

        acChartMonth?.setChart(cartesian)
        acChartMonth!!.invalidate()

        tvDay?.text = "${Constants.curYearOfMonth}/${Constants.curMonthOfMonth}"


        //drawAvgData
        tvMonthlyBPM!!.text = commonUtils.calcMonthlyAverage(almonthlyTemperature).toString()
        tvMinimumBPM!!.text = commonUtils.calcMinAverage(almonthlyTemperature).toString()
        tvMaximumBPM!!.text = commonUtils.calcMaxAverage(almonthlyTemperature).toString()
    }

    private inner class CustomDataEntry internal constructor(x: String, value: Number, value2: Number, value3: Number) : ValueDataEntry(x, value) {
        init {
            setValue("value2", value2)
            setValue("value3", value3)
        }
    }
}