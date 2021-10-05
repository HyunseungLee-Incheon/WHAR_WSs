package com.crc.har.statistics

import android.app.DatePickerDialog
import android.content.Context
import android.os.Bundle
import android.util.Log
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
import com.crc.har.database.DBHeartBeatModel
import io.realm.Realm
import io.realm.RealmResults
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class StasticDayFragment : Fragment() {

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

        val view: View = inflater.inflate(R.layout.fragment_stastics_day, container, false)

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

//        val sdf = SimpleDateFormat("yyyy/MM/dd hh:mm:ss")
//        val currentDate = sdf.format(Date())
//
//        Log.e("eleutheria", "currentDate : ${currentDate}")
//
//
//        val nId: Int = 1
//        var restoreData: DBHeartBeatModel? = realm.where(DBHeartBeatModel::class.java).equalTo("id", nId).findFirst()
//        Log.e("eleutheria", "restoreData : ${restoreData?.year}/${restoreData?.month}/${restoreData?.day} ${restoreData?.hour}:${restoreData?.minute}:${restoreData?.second}, bpm : ${restoreData?.heartbeat}, Status : ${restoreData?.status}")

        return view
    }

    private fun drawDayData() {
        // get realm data

        val commonUtils = CommonUtils()


//        var nDay : Int = 2
        val heartBeatDataResult : RealmResults<DBHeartBeatModel> = realm.where(DBHeartBeatModel::class.java).equalTo("year", Constants.curYearOfDay)
            .equalTo( "month", Constants.curMonthOfDay )
            .equalTo("day", Constants.curDayOfDay)
            .findAll()

        val morningAvgResult : RealmResults<DBHeartBeatModel> = realm.where(DBHeartBeatModel::class.java).equalTo("year", Constants.curYearOfDay)
            .equalTo( "month", Constants.curMonthOfDay )
            .equalTo("day", Constants.curDayOfDay)
            .lessThan("hour", 13)
            .findAll()

        val afternoonAvgResult : RealmResults<DBHeartBeatModel> = realm.where(DBHeartBeatModel::class.java).equalTo("year", Constants.curYearOfDay)
            .equalTo( "month", Constants.curMonthOfDay )
            .equalTo("day", Constants.curDayOfDay)
            .greaterThan("hour", 12)
            .findAll()

//        var heartBeatDataAvgResult : DBHeartBeatModel = DBHeartBeatModel()
        var heartBeatDataAvgResult : Double = 0.0
        var arHBAvgResult : ArrayList<AvgData> = ArrayList<AvgData>()

        for(i in 0 until 24) {
            var HBAvgData: AvgData = AvgData()

            heartBeatDataAvgResult = realm.where(DBHeartBeatModel::class.java).equalTo("year", Constants.curYearOfDay)
                .equalTo( "month", Constants.curMonthOfDay )
                .equalTo("day", Constants.curDayOfDay)
                .equalTo("hour", i)
                .average("heartbeat")

            HBAvgData.strLabel = i.toString()
            HBAvgData.dValue = heartBeatDataAvgResult
            arHBAvgResult.add(HBAvgData)

        }


        //drawGraph

        val cartesian: Cartesian = AnyChart.line()

        cartesian.animation(true)
        cartesian.padding(10, 20, 5, 20)

        cartesian.crosshair().enabled(true)
        cartesian.crosshair()
            .yLabel(true)

        cartesian.tooltip().positionMode(TooltipPositionMode.POINT)

        //cartesian.title()

        cartesian.yAxis(0).title("BPM")
        cartesian.xAxis(0).labels().padding(5, 5, 5, 5)

        var seriesData =  ArrayList<DataEntry>()

        if(arHBAvgResult.size < 1) {
            seriesData.add(ValueDataEntry("0", 0))
        } else {
            for(bpmData in arHBAvgResult!!) {
                Log.e("eleutheria", "bpmData - Label : ${bpmData.strLabel}, value : ${bpmData.dValue}")
                seriesData.add(ValueDataEntry(bpmData.strLabel, bpmData.dValue))
            }
        }

        val set = Set.instantiate()
        set.data(seriesData)
        var series1Mapping = set.mapAs("{ x: 'x', value: 'value' }");

        var series1 = cartesian.line(series1Mapping)
        series1.name("BPM");
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

        tvMorningBPM!!.text = commonUtils.calcAverage(morningAvgResult).toString()
        tvAfternoonBPM!!.text = commonUtils.calcAverage(afternoonAvgResult).toString()
        tvDayBPM!!.text = commonUtils.calcAverage(heartBeatDataResult).toString()
    }
}