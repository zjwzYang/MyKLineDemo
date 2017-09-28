package com.example.yangjie.myklinedemo;

import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.github.mikephil.charting.charts.Chart;
import com.github.mikephil.charting.charts.CombinedChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.CandleData;
import com.github.mikephil.charting.data.CandleDataSet;
import com.github.mikephil.charting.data.CandleEntry;
import com.github.mikephil.charting.data.CombinedData;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.Utils;
import com.github.mikephil.charting.utils.ViewPortHandler;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private CombinedChart candlerChart;
    private CombinedChart volumeChart;
    private List<CandleEntry> mlists;
    private List<CandleEntry> mCandleEntries; // k线数据
    private List<BarEntry> mBarEntries; // 成交量数控
    private List<String> xVals;
    private CombinedData combinedData;
    private CandleData candleData;
    private List<StockListBean.StockBean> stockBeans;
    private LineData lineData;
    private XAxis xAxisVolume;
    private YAxis axisLeftVolume;
    private YAxis axisRightVolume;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        candlerChart = (CombinedChart) findViewById(R.id.candler_chart);
        volumeChart = (CombinedChart) findViewById(R.id.kline_chart_volume);

        setChart();
        loadData();

        initChartEvent();

        candlerChart.moveViewToX(stockBeans.size() - 1);
        volumeChart.moveViewToX(stockBeans.size() - 1);
    }

    private void setChart() {
        candlerChart.setDescription(null);
        candlerChart.setDrawBorders(false);
        candlerChart.setDragEnabled(true);
        candlerChart.setScaleYEnabled(false);
        candlerChart.setAutoScaleMinMaxEnabled(true);
        candlerChart.setNoDataText("加载中...");

        XAxis xAxis =candlerChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(true);
        xAxis.setGridColor(Color.BLUE);//X轴刻度线颜色
        xAxis.setTextColor(Color.BLUE);//X轴文字颜色
        xAxis.setAvoidFirstLastClipping(true);//设置首尾的值是否自动调整，避免被遮挡

        YAxis leftAxis = candlerChart.getAxisLeft();
        leftAxis.setEnabled(true);
        leftAxis.setLabelCount(7,false);
        leftAxis.setDrawGridLines(true);
        leftAxis.setDrawAxisLine(false);
        leftAxis.setGridColor(Color.BLUE);
        leftAxis.setTextColor(Color.BLUE);

        YAxis rightAxis =candlerChart.getAxisRight();
        rightAxis.setEnabled(false);

        mlists = new ArrayList<>();

        // 对图标颜色设置
        CandleDataSet set = new CandleDataSet(mlists, "Data Set");
        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        set.setShadowColor(Color.DKGRAY);//影线颜色
        set.setShadowColorSameAsCandle(true);//影线颜色与实体一致
        set.setShadowWidth(0.7f);//影线
        set.setDecreasingColor(Color.RED);
        set.setDecreasingPaintStyle(Paint.Style.FILL);//红涨，实体
        set.setIncreasingColor(Color.GREEN);
        set.setIncreasingPaintStyle(Paint.Style.STROKE);//绿跌，空心
        set.setNeutralColor(Color.RED);//当天价格不涨不跌（一字线）颜色
        set.setHighlightLineWidth(1f);//选中蜡烛时的线宽
        set.setDrawValues(false);//在图表中的元素上面是否显示数值
        set.setLabel("label");

        Legend lineChartLegend = candlerChart.getLegend();
        lineChartLegend.setEnabled(false);//是否绘制 Legend 图例
        lineChartLegend.setPosition(Legend.LegendPosition.ABOVE_CHART_RIGHT);

        candlerChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, int dataSetIndex, Highlight h) {

            }

            @Override
            public void onNothingSelected() {
                candlerChart.highlightValue(null);
            }
        });

        // 成交量 x y轴
        xAxisVolume = volumeChart.getXAxis();
        xAxisVolume.setEnabled(false);

        axisLeftVolume = volumeChart.getAxisLeft();
        axisLeftVolume.setAxisMinValue(0);//设置Y轴坐标最小为多少
        axisLeftVolume.setDrawGridLines(true);
        axisLeftVolume.setDrawAxisLine(false);
        axisLeftVolume.setDrawLabels(true);
        axisLeftVolume.enableGridDashedLine(10f, 10f, 0f);
        axisLeftVolume.setTextColor(Color.BLACK);
        axisLeftVolume.setPosition(YAxis.YAxisLabelPosition.INSIDE_CHART);
        axisLeftVolume.setLabelCount(1, false); //第一个参数是Y轴坐标的个数，第二个参数是 是否不均匀分布，true是不均匀分布
        axisLeftVolume.setSpaceTop(0);//距离顶部留白

        axisRightVolume = volumeChart.getAxisRight();
        axisRightVolume.setDrawLabels(false);
        axisRightVolume.setDrawGridLines(false);
        axisRightVolume.setDrawAxisLine(false);

        volumeChart.setDragDecelerationEnabled(true);
        volumeChart.setDragDecelerationFrictionCoef(0.2f);

        candlerChart.animateX(2000);

        // 成交量标的初始化设置
        volumeChart.setDrawBorders(true); // 边框是否显示
        volumeChart.setBorderWidth(1); // 边框宽度
        volumeChart.setBorderColor(Color.BLUE);
        volumeChart.setDescription("");
        volumeChart.setDragEnabled(true); // 是否可以拖拽
        volumeChart.setScaleYEnabled(false); // y轴方向是否可以缩放
        volumeChart.setExtraOffsets(0f, 0f, 0f, 5f);

        Legend combinedchartLegend = volumeChart.getLegend(); // 设置比例图标示，就是那个一组y的value的
        combinedchartLegend.setEnabled(false);//是否绘制比例图

        volumeChart.animateX(2000);
    }

    private void loadData() {
        candlerChart.resetTracking();

        mCandleEntries = Model.getCandleEntries();
        mBarEntries = Model.getBarEntries();
        stockBeans = Model.getData();
        xVals = new ArrayList<>();
        for (int i = 0; i < mCandleEntries.size(); i++) {
            xVals.add(stockBeans.get(i).getDate());
        }

        combinedData = new CombinedData(xVals);

        // k
        candleData = generateCandleData();
        combinedData.setData(candleData);

        /*ma5*/
        ArrayList<Entry> ma5Entries = new ArrayList<Entry>();
        for (int index = 0; index < mCandleEntries.size(); index++) {
            ma5Entries.add(new Entry(stockBeans.get(index).getMa5(), index));
        }

        lineData = generateMultiLineData(generateLineDataSet(ma5Entries, Color.YELLOW, "ma5"));
        combinedData.setData(lineData);
        candlerChart.setData(combinedData);
        setHandler(candlerChart);
        candlerChart.invalidate();

        // 加载成交量的数据
        String unit = getVolUnit(Integer.parseInt(stockBeans.get(0).getVolume()));
        int u = 1;
        if ("万手".equals(unit)) {
            u = 4;
        } else if ("亿手".equals(unit)) {
            u = 8;
        }
        volumeChart.getAxisLeft().setValueFormatter(new VolFormatter((int) Math.pow(10, u)));
        BarData barData = generatevolumeData();

        CombinedData barCombinedData = new CombinedData(xVals);
        barCombinedData.setData(barData);
        volumeChart.setData(barCombinedData);
        setHandler(volumeChart);
    }

    private void initChartEvent() {

        // 将k线的滑动事件传递给成交量控件
        candlerChart.setOnChartGestureListener(new CoupleChartGestureListener(candlerChart, new Chart[]{volumeChart}));
        // 将成交量控件的滑动事件传递给k线控件
        volumeChart.setOnChartGestureListener(new CoupleChartGestureListener(volumeChart, new Chart[]{candlerChart}));

        candlerChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, int dataSetIndex, Highlight h) {
                volumeChart.highlightValue(new Highlight(h.getXIndex(), 0));
            }

            @Override
            public void onNothingSelected() {
                volumeChart.highlightValue(null);
            }
        });
        volumeChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, int dataSetIndex, Highlight h) {
                candlerChart.highlightValue(new Highlight(h.getXIndex(), 0));
            }

            @Override
            public void onNothingSelected() {
                candlerChart.highlightValue(null);
            }
        });
        setOffset();
    }

    /*设置量表对齐*/
    private void setOffset() {
        float lineLeft = candlerChart.getViewPortHandler().offsetLeft();
        float kbLeft = volumeChart.getViewPortHandler().offsetLeft();
        float lineRight = candlerChart.getViewPortHandler().offsetRight();
        float kbRight = volumeChart.getViewPortHandler().offsetRight();
        float kbBottom = volumeChart.getViewPortHandler().offsetBottom();
        float offsetLeft, offsetRight;
        float transLeft = 0, transRight = 0;
 /*注：setExtraLeft...函数是针对图表相对位置计算，比如A表offLeftA=20dp,B表offLeftB=30dp,则A.setExtraLeftOffset(10),并不是30，还有注意单位转换*/
        if (kbLeft < lineLeft) {
           /* offsetLeft = Utils.convertPixelsToDp(lineLeft - barLeft);
            barChart.setExtraLeftOffset(offsetLeft);*/
            transLeft = lineLeft;
        } else {
            offsetLeft = Utils.convertPixelsToDp(kbLeft - lineLeft);
            candlerChart.setExtraLeftOffset(offsetLeft);
            volumeChart.setExtraLeftOffset(offsetLeft);
            transLeft = kbLeft;
        }
  /*注：setExtraRight...函数是针对图表绝对位置计算，比如A表offRightA=20dp,B表offRightB=30dp,则A.setExtraLeftOffset(30),并不是10，还有注意单位转换*/
        if (kbRight < lineRight) {
          /*  offsetRight = Utils.convertPixelsToDp(lineRight);
            barChart.setExtraRightOffset(offsetRight);*/
            transRight = lineRight;
        } else {
            offsetRight = Utils.convertPixelsToDp(kbRight);
            candlerChart.setExtraRightOffset(offsetRight);
            transRight = kbRight;
        }
        volumeChart.setViewPortOffsets(transLeft, 15, transRight, kbBottom);
    }

    private LineDataSet generateLineDataSet(List<Entry> entries, int color, String label) {
        LineDataSet set = new LineDataSet(entries, label);
        set.setColor(color);
        set.setLineWidth(1f);
        set.setDrawCubic(true);//圆滑曲线
        set.setDrawCircles(false);
        set.setDrawCircleHole(false);
        set.setDrawValues(false);
        set.setHighlightEnabled(false);
        set.setAxisDependency(YAxis.AxisDependency.LEFT);

        return set;
    }

    private LineData generateMultiLineData(LineDataSet... lineDataSets) {
        List<ILineDataSet> dataSets = new ArrayList<>();
        for (int i = 0; i < lineDataSets.length; i++) {
            dataSets.add(lineDataSets[i]);
        }

        List<String> xVals = new ArrayList<String>();
        for (int i = 0; i < mCandleEntries.size(); i++) {
            xVals.add("" + (1990 + i));
        }

        LineData data = new LineData(xVals, dataSets);

        return data;
    }

    private BarData generatevolumeData() {

        BarDataSet set = new BarDataSet(mBarEntries, "");
        set.setHighlightEnabled(true);
        set.setHighLightColor(Color.BLACK);
        set.setDrawValues(false);
        List<Integer> list = new ArrayList<>();
        list.add(getResources().getColor(R.color.increasing_color));
        list.add(getResources().getColor(R.color.decreasing_color));
        set.setColors(list);

        BarData barData = new BarData(xVals, set);
        return barData;
    }

    private CandleData generateCandleData() {

        CandleDataSet set = new CandleDataSet(mCandleEntries, "");
        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        set.setShadowWidth(0.7f);
        set.setDecreasingColor(Color.RED);
        set.setDecreasingPaintStyle(Paint.Style.FILL);
        set.setIncreasingColor(Color.GREEN);
        set.setIncreasingPaintStyle(Paint.Style.STROKE);
        set.setNeutralColor(Color.RED);
        set.setShadowColorSameAsCandle(true);
        set.setHighlightLineWidth(0.5f);
        set.setHighLightColor(Color.BLACK);
        set.setDrawValues(false);

        CandleData candleData = new CandleData(xVals);
        candleData.addDataSet(set);

        return candleData;
    }

    private void setHandler(CombinedChart combinedChart) {
        final ViewPortHandler viewPortHandlerBar = combinedChart.getViewPortHandler();
        viewPortHandlerBar.setMaximumScaleX(culcMaxscale(stockBeans.size()));
        Matrix touchmatrix = viewPortHandlerBar.getMatrixTouch();
        final float xscale = 3;
        touchmatrix.postScale(xscale, 1f);
    }

    private float culcMaxscale(float count) {
        float max = 1;
        max = count / 127 * 5;
        return max;
    }

    public String getVolUnit(float num) {

        int e = (int) Math.floor(Math.log10(num));

        if (e >= 8) {
            return "亿手";
        } else if (e >= 4) {
            return "万手";
        } else {
            return "手";
        }
    }
}
