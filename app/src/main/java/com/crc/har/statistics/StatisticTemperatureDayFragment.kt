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
import com.crc.har.base.AvgData
import com.crc.har.base.CommonUtils
import com.crc.har.base.Constants
import com.crc.har.database.DBTemperatureModel
import io.realm.Realm
import io.realm.RealmResults
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class StatisticTemperatureDayFragment : Fragment()  {

    val realm: Realm = Realm.getDefaultInstance()

    private var acChartDay : AnyChartView? = null
    private var tvDay : TextView? = null
    private var mContext : Context? = null

    private var tvMorningBPM : TextView? = null
    private var tvAfternoonBPM : TextView? = null
    private var tvDayBPM : TextView? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val view: View = inflater!!.inflate(R.layout.fragment_stastics_day, container, false)

        mContext = container!!.context

        acChartDay = view.findViewById(R.id.acChartDay)
        tvMorningBPM = view.findViewById(R.id.tvMorningBPM)
        tvAfternoonBPM = view.findViewById(R.id.tvAfternoonBPM)
        tvDayBPM = view.findViewById(R.id.tvDayBPM)

//        Log.e("eleutheria", realm.path)
        var calendar = Calendar.getInstance()

        val dateDialogListener = DatePickerDialog.OnDateSetListener { view, year, month, dayOfMonth ->
            calendar.set(Calendar.YEAR, year)
            calendar.set(Calendar.MONTH, month)
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)

            val dateFormat = "yyyy/MM/dd"
            val sdf = SimpleDateFormat(dateFormat, Locale.KOREA)
            tvDay?.text = sdf.format(calendar.time)

            Constants.curYearOfDay = year
            Constants.curMonthOfDay = month + 1
            Constants.curDayOfDay = dayOfMonth

            acChartDay!!.clear()
            acChartDay!!.invalidate()
            drawDayData()

        }

        tvDay = view.findViewById(R.id.tvDay)
        tvDay?.setOnClickListener {
            DatePickerDialog(requireContext(), dateDialogListener, calendar.get(Calendar.YEAR), calendar.get(
                Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
        }

        drawDayData()

        return view
    }

    private fun drawDayData() {
        // get realm data

        var commonUtils = CommonUtils()


//        var nDay : Int = 2
        var temperatureDataResult : RealmResults<DBTemperatureModel> = realm.where(DBTemperatureModel::class.java).equalTo("year", Constants.curYearOfDay)
            .equalTo( "month", Constants.curMonthOfDay )
            .equalTo("day", Constants.curDayOfDay)
            .findAll()

        var morningAvgResult : RealmResults<DBTemperatureModel> = realm.where(DBTemperatureModel::class.java).equalTo("year", Constants.curYearOfDay)
            .equalTo( "month", Constants.curMonthOfDay )
            .equalTo("day", Constants.curDayOfDay)
            .lessThan("hour", 13)
            .findAll()

        var afternoonAvgResult : RealmResults<DBTemperatureModel> = realm.where(DBTemperatureModel::class.java).equalTo("year", Constants.curYearOfDay)
            .equalTo( "month", Constants.curMonthOfDay )
            .equalTo("day", Constants.curDayOfDay)
            .greaterThan("hour", 12)
            .findAll()

        var TemperatureDataAvgResult : Double = 0.0
        var arTemperatureAvgResult : ArrayList<AvgData> = ArrayList<AvgData>()
        var nMinSecond = 0
        var nMaxSecond = 19

        for(nHour in 0 until 24) {
            for(nMinute in 0 until 60) {
                for(nAgain in 0 until 3) {
                    var TemperatureAvgData: AvgData = AvgData()

                    if(nAgain == 0) {
                        nMinSecond = 0
                        nMaxSecond = 19
                    } else if(nAgain == 0) {
                        nMinSecond = 20
                        nMaxSecond = 39
                    } else {
                        nMinSecond = 40
                        nMaxSecond = 59
                    }

                    TemperatureDataAvgResult = realm.where(DBTemperatureModel::class.java)
                        .equalTo("year", Constants.curYearOfDay)
                        .equalTo("month", Constants.curMonthOfDay)
                        .equalTo("day", Constants.curDayOfDay)
                        .equalTo("hour", nHour)
                        .equalTo("minute", nMinute)
                        .greaterThanOrEqualTo("second", nMinSecond)
                        .lessThanOrEqualTo("second", nMaxSecond)
                        .average("temperature")

                    TemperatureAvgData.strLabel = nHour.toString() + "_$nAgain"
                    TemperatureAvgData.dValue = TemperatureDataAvgResult
                    arTemperatureAvgResult.add(TemperatureAvgData)
                }
            }

        }

        //drawGraph

        var cartesian: Cartesian = AnyChart.line()

        cartesian.animation(true)
        cartesian.padding(10, 20, 5, 20)

        cartesian.crosshair().enabled(true)
        cartesian.crosshair()
            .yLabel(true)

        cartesian.tooltip().positionMode(TooltipPositionMode.POINT)

        //cartesian.title()

        cartesian.yAxis(0).title("Temperature")
        cartesian.xAxis(0).labels().padding(5, 5, 5, 5)

        var seriesData =  ArrayList<DataEntry>()

        if(arTemperatureAvgResult.size < 1) {
            seriesData.add(ValueDataEntry("0", 0))
        } else {
            for(temperatureData in arTemperatureAvgResult) {
                seriesData.add(ValueDataEntry(temperatureData.strLabel, temperatureData.dValue))
            }
        }

        val set = Set.instantiate()
        set.data(seriesData)
        var series1Mapping = set.mapAs("{ x: 'x', value: 'value' }");

        var series1 = cartesian.line(series1Mapping)
        series1.name("Temperature");
        series1.hovered().markers().enabled(true)
        series1.hovered().markers()
            .type(MarkerType.CIRCLE)
            .size(4)
        series1.tooltip()
            .position("right")
            .anchor(Anchor.LEFT_CENTER)
            .offsetX(5)
            .offsetY(5)


        cartesian.legend().enabled(true)
        cartesian.legend().fontSize(13)
        val padding = cartesian.legend().padding(0, 0, 10, 0)

        acChartDay?.setChart(cartesian)
        acChartDay!!.invalidate()

        tvDay?.text = "${Constants.curYearOfDay}/${Constants.curMonthOfDay}/${Constants.curDayOfDay}"

        //drawAvgData

        tvMorningBPM!!.text = commonUtils.calcTemperatureAverage(morningAvgResult).toString()
        tvAfternoonBPM!!.text = commonUtils.calcTemperatureAverage(afternoonAvgResult).toString()
        tvDayBPM!!.text = commonUtils.calcTemperatureAverage(temperatureDataResult).toString()
    }
}