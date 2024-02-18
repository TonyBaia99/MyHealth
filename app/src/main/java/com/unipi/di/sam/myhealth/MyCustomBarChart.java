package com.unipi.di.sam.myhealth;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatEditText;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class MyCustomBarChart extends View {
    private ArrayList<Integer> dataSet=new ArrayList<>();
    private ArrayList<String> dataList =new ArrayList<>();

    private ArrayList<Integer> dummyDataSet= new ArrayList<>();
    private ArrayList<String> dummyDataList= new ArrayList<>();

    private int maxValY=20000;
    private String typeDataSet=null;
    private Paint rectColor=null;
    private Paint axes;
    private Paint text;
    private Paint dataText;
    private int dimRect;


    private String data;

    public MyCustomBarChart(Context context) throws ParseException {
        super(context);
        init(context, null, 0);
    }

    public MyCustomBarChart(Context context, AttributeSet attrs) throws ParseException {
        super(context, attrs);
        init(context, attrs, 0);
    }

    private void init(Context context, AttributeSet attributeSet, int def) throws ParseException {
        TypedArray a= context.getTheme().obtainStyledAttributes(attributeSet, R.styleable.MyCustomBarChart, def, 0);

        try {
            dimRect= a.getInteger(R.styleable.MyCustomBarChart_dimRect, 50);
        }
        finally {
            a.recycle();
        }

        axes= new Paint();
        axes.setColor(getResources().getColor(R.color.axesColor));
        axes.setStrokeWidth(5);

        text=new Paint();
        text.setColor(getResources().getColor(R.color.textColor));
        text.setTextSize(30);
        text.setFakeBoldText(true);

        dataText= new Paint();
        dataText.setTextSize(24);
        dataText.setColor(getResources().getColor(R.color.textColor));

        LocalDateTime localDateTime= new Date().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
        int year= localDateTime.getYear();
        int month= localDateTime.getMonthValue();
        int day= localDateTime.getDayOfMonth();
        data= year+"/"+month+"/"+day;

        for( int i=0; i<7; i++){
            dataSet.add(i, 0);
            dataList.add(i, generatePreviousDay(i));
        }

        setTypeDataSet("steps");
    }

    private   String generatePreviousDay(int i) throws ParseException {
        LocalDateTime localDateTime= new Date().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
        int year= localDateTime.getYear();
        int month= localDateTime.getMonthValue();
        int day= localDateTime.getDayOfMonth();
        String data1= year+"/"+month+"/"+day;

        SimpleDateFormat sdf= new SimpleDateFormat("yyyy/MM/dd");
        Calendar c= Calendar.getInstance();
        c.setTime(sdf.parse(data));
        c.add(Calendar.DATE, -i);

        return sdf.format(c.getTime());
    }

    public void setDataSet(ArrayList<Integer> dataSet, ArrayList<String> datalist) {
        this.dataSet=dataSet;
        this.dataList= datalist;
    }

    public void setMaxValY(int maxValY) {
        this.maxValY= maxValY;
    }

    public void setTypeDataSet(String typeDataSet) {
        rectColor= new Paint();
        rectColor.setStyle(Paint.Style.FILL);
        switch (typeDataSet) {
            case "steps": {
                rectColor.setColor(getResources().getColor(R.color.green));
                this.typeDataSet= "Passi";
            } break;

            case "activityDuration": {
                rectColor.setColor(getResources().getColor(R.color.blue));
                this.typeDataSet= "Durata Attività";
            } break;

            case "calories": {
                rectColor.setColor(getResources().getColor(R.color.calories_color));
                this.typeDataSet= "Calorie";
            } break;

            default: {
                rectColor.setColor(getResources().getColor(R.color.black));
            }
        }
        invalidate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        int width= getWidth();
        int height= getHeight();

        //asse y
        canvas.drawLine(width-110, 50, width-110, height-30, axes);
        //Dicitura asse y
        canvas.drawText(typeDataSet, width-130, 45, text);
        //Max on axe y
        canvas.drawText(String.valueOf(maxValY), width-100, 80, text);

        //asse x
        canvas.drawLine(40, height-80, width-30, height-80, axes);

        int i=0;
        for(int x=width-160; x>0; x-=130) {
            if(i<7) {

                if(dataSet.get(i)>=maxValY) {
                    canvas.drawRect(x-dimRect, 80, x, height-82, rectColor);
                    canvas.drawText(String.valueOf(maxValY)+">", x-dimRect , 72, dataText);
                    canvas.drawText(dataList.get(i), x-dimRect-30, height-55, dataText);
                    i++;
                }
                else {
                    int tmp= (((height - 160) * dataSet.get(i)) / maxValY);
                    canvas.drawRect(x-dimRect, (height-80) - tmp, x, height-82, rectColor);
                    canvas.drawText(String.valueOf(dataSet.get(i)), x-dimRect -5, (height-80) - tmp -10, dataText);
                    canvas.drawText(dataList.get(i), x-dimRect-30, height-55, dataText);
                    i++;
                }
            }
        }
    }
}
