package ru.mgvk.prostoege.ui.statistic;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import ru.mgvk.prostoege.MainActivity;
import ru.mgvk.prostoege.fragments.RepetitionFragmentLeft;
import ru.mgvk.prostoege.ui.UI;

import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

/**
 * Created by mike on 24.07.17.
 */
public class StatisticPlot extends LinearLayout
        implements MainActivity.OnConfigurationUpdateListener {


    private Surface surface;
    private int width  = -1;
    private int height = UI.calcSize(142);
    private Context context;

    public StatisticPlot(Context context) {
        super(context);

        this.context = context;
        setBackgroundColor(Color.TRANSPARENT);
        setLayoutParams(new LayoutParams(width, height));
        setOrientation(HORIZONTAL);
        surface = new Surface(context, height);
        addView(surface);
        ((MainActivity) context).addOnConfigurationUpdateListener(this);
    }

    void addColumn(RepetitionFragmentLeft.Result result) {
        surface.addColumn(result);
    }

    @Override
    public void onConfigurationUpdate(Configuration configuration) {
        if (surface != null) {
            surface.initSizesForced();
        }
    }

    interface OnPlotScaleChangeListener {
        void onChange();
    }

    private static class Surface extends View {


        private final int COLUMN_UNCHOOSED_COLOR = Color.WHITE;
        private final int COLUMN_CHOOSED_COLOR   = Color.parseColor("#FFFFFD72");
        private final int BACKGROUND_COLOR       = Color.TRANSPARENT;
        private final int AXIS_COLOR             = Color.parseColor("#77ffffff"); //todo 50% alpha


        String[]          month              =
                {"Янв", "Фев", "Мар", "Апр", "Мая", "Июн",
                 "Июл", "Авг", "Сен", "Окт", "Ноя", "Дек"};
        DateFormatSymbols symbols            = new DateFormatSymbols(new Locale("ru", "RU"));
        int               currentColumnIndex = -1;//default all are unchoosed;
        Paint currentColumnPaint;
        private int               axisStartX            = 0;
        private int               axisLabelStartX       = UI.calcSize(5);
        private int               axisLabelMarginBottom = UI.calcSize(5);
        private int               axisLabelMarginTop    = UI.calcSize(10);
        private int               height                = 0;
        private double            columnDX              = 0;
        private double            columnDxCommon        = 0;
        private boolean           barTouched            = false;
        private int               width                 = 0;
        private String            TAG                   = "StatisticPlot";
        private ArrayList<Column> columnsList           = new ArrayList<>();
        private boolean           commonView            = false;
        private boolean           emptyPlaceTouched     = false;
        private int               plotHeight            = 0;
        private float             threshold             = 0;
        private int               columnWidth           = 10;
        private int               indexOffset           = 0;
        private double            heightRatio           = 5 / 6.0;
        private Paint   axisPaint;
        private Paint   axisLabelPaint;
        private Paint   axis0Paint;
        private Paint   columnPaintUnchoosed;
        private Paint   columnLabelPaint;
        private Context context;
        private float   thresholdCommon;
        private Paint   columnPaintChoosed;
        private Paint   backgroundPaint;

        public Surface(Context context, int height) {
            super(context);

            this.plotHeight = (int) (heightRatio * (this.height = height));
            this.context = context;
            init();
        }

        void init() {
            symbols.setMonths(month);


            setBackgroundColor(BACKGROUND_COLOR);

            initSizes();

            setLayoutParams(new ViewGroup.LayoutParams(-1, height));
            initPaints();

            MainStatisticView.setOnScaleButtonClickListener(
                    new MainStatisticView.OnScaleButtonClickListener() {
                        @Override
                        public void onClick(Button scaleButton) {
                            try {
                                commonView = (Boolean) scaleButton.getTag();
                                postInvalidate();
                            } catch (Exception ignored) {
                            }
                        }
                    });

        }

        void initSizesForced() {
            plotHeight = (int) (heightRatio * getHeight());
            width = getWidth();
            axisLabelStartX = width - UI.calcSize(8);

            columnDxCommon = (axisLabelStartX) / (double) (columnsList.size() + 1);

            //так как добавление всегда последовательно, то мы определяем размер только пока
            // кол-во < 8, а потом просто используем старое значение, которое было при кол-ве == 7
            if (columnsList.size() < 8) {
                columnDX = columnDxCommon;
                threshold = (float) ((columnDX + columnWidth / 2) % columnDX) * 2;
                indexOffset = 0;
            } else {
                indexOffset = columnsList.size() - 7;
            }
            thresholdCommon = (float) ((columnDX + columnWidth / 2) % columnDxCommon) /* * 2*/;

//            Log.d(TAG, "initSizesForced: " + width + " " + height);


        }

        void initSizes() {
            if (plotHeight == 0) {
                plotHeight = (int) (heightRatio * getHeight());
            }
            if (width == 0) {
                width = getWidth();
                axisLabelStartX = width - UI.calcSize(8);
            }
        }

        void initPaints() {
            axisPaint = new Paint();
            axis0Paint = new Paint();
            axisLabelPaint = new Paint();
            columnPaintUnchoosed = new Paint();
            columnPaintChoosed = new Paint();
            columnLabelPaint = new Paint();
            backgroundPaint = new Paint();

            axisPaint.setColor(AXIS_COLOR);
            axis0Paint.setColor(Color.WHITE);
            axisLabelPaint.setColor(Color.WHITE);
            columnPaintUnchoosed.setColor(COLUMN_UNCHOOSED_COLOR);
            columnPaintChoosed.setColor(COLUMN_CHOOSED_COLOR);
            columnLabelPaint.setColor(Color.WHITE);
            backgroundPaint.setColor(BACKGROUND_COLOR);

            axis0Paint.setStrokeWidth(2);
            axisLabelPaint.setTextSize(18);
            axisLabelPaint.setTextAlign(Paint.Align.RIGHT);
            columnPaintUnchoosed.setStrokeWidth(columnWidth);
            columnPaintChoosed.setStrokeWidth(columnWidth);
            columnLabelPaint.setTextSize(18);
            columnLabelPaint.setTextAlign(Paint.Align.CENTER);
            backgroundPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        }

        @Override
        protected void onDraw(Canvas canvas) {

            drawBackground(canvas);

            initSizesForced();

            drawAXIS(canvas);

            drawColumns(canvas);

        }

        private void drawBackground(Canvas canvas) {
//            canvas.drawColor(BACKGROUND_COLOR, PorterDuff.Mode.OVERLAY);
//            canvas.drawCircle(50,50,100,backgroundPaint);
//            canvas.draw
        }

        private void drawColumns(Canvas canvas) {
            int x = 0;

            if (!commonView) {


                for (int i = columnsList.size() < 8 ? 0 : columnsList.size() - 7;
                     i < columnsList.size();
                     i++) {
                    x += columnDX;

                    currentColumnPaint = columnsList.get(i).isChoosed()
                            ? columnPaintChoosed : columnPaintUnchoosed;

                    float stopY = plotHeight -
                                  axisLabelMarginBottom - columnsList.get(i).getHeight();
                    canvas.drawCircle(x, plotHeight - axisLabelMarginBottom, columnWidth / 2,
                            currentColumnPaint);
                    canvas.drawLine(x, plotHeight - axisLabelMarginBottom, x, stopY,
                            currentColumnPaint);
                    canvas.drawCircle(x, stopY, columnWidth / 2, currentColumnPaint);

                    canvas.drawText(columnsList.get(i).getDate(), x,
                            height - axisLabelMarginBottom * 2,
                            columnLabelPaint);
                }

            } else {

                for (Column column : columnsList) {
                    x += columnDxCommon;
                    currentColumnPaint = column.isChoosed()
                            ? columnPaintChoosed : columnPaintUnchoosed;

                    float stopY = plotHeight -
                                  axisLabelMarginBottom - column.getHeight();
                    canvas.drawCircle(x, plotHeight - axisLabelMarginBottom, columnWidth / 2,
                            currentColumnPaint);
                    canvas.drawLine(x, plotHeight - axisLabelMarginBottom, x, stopY,
                            currentColumnPaint);
                    canvas.drawCircle(x, stopY, columnWidth / 2, currentColumnPaint);

                }
            }
        }

        private void drawAXIS(Canvas canvas) {

            for (int h = 0, i = 4; h < plotHeight; h += (plotHeight / 4.0), i--) {
                canvas.drawLine(axisStartX, h, width, h, axisPaint);
            }
            canvas.drawLine(axisStartX, plotHeight - 1, width, plotHeight - 1, axis0Paint);

            drawAxisLabels(canvas);

        }

//        void addColumn(String date, int plotHeight) {
//
//            columnsList.add(new Column(date,
//                    (float) (plotHeight / 100.0 * (plotHeight - 2 * axisLabelMarginBottom))));
//
//            columnDX = (axisLabelStartX) / (double) (columnsList.size() + 1);
//            threshold = (float) ((columnDX - columnWidth / 2) % columnDX);
//            postInvalidate();
//        }

        private void drawAxisLabels(Canvas canvas) {

            canvas.drawText("0", axisLabelStartX, plotHeight - axisLabelMarginBottom,
                    axisLabelPaint);
            canvas.drawText("100", axisLabelStartX, axisLabelMarginTop, axisLabelPaint);

        }

        void addColumn(String date, RepetitionFragmentLeft.Result result) {

            columnsList.add(new Column(date, result,
                    (float) (result.getScoreSecondary() / 100.0 * (plotHeight
                                                                   - 2 * axisLabelMarginBottom))));

            initSizesForced();
//            Log.d(TAG, "addColumn: " + columnDX + " " + threshold);
            postInvalidate();
        }

        void addColumn(RepetitionFragmentLeft.Result result) {

            addColumn(new SimpleDateFormat("d MMMM", symbols)
                    .format(result.getDate(context)), result);

        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {

            ((MainActivity) context).ui.mainScroll.setScrollEnabled(false);
            ((MainActivity) context).ui.taskListFragment.taskScroll.setScrollEnabled(false);
            ((MainActivity) context).ui.taskListFragment.myCoordinatorLayout
                    .setScrollEnabled(false);

            float  currentThreshold = commonView ? thresholdCommon : threshold;
            double currentColumnDX  = commonView ? columnDxCommon : columnDX;

            if (event.getAction() == MotionEvent.ACTION_DOWN || event.getAction() == MotionEvent
                    .ACTION_MOVE) {

                double curr = event.getX() % currentColumnDX;

                if (curr <= currentThreshold || currentColumnDX - curr <= currentThreshold) {
                    //click on column
                    barTouched = true;

//                    Log.d(TAG, "onTouchEvent: " +
                    currentColumnIndex = (commonView ? 0 :
                            indexOffset) + round(event.getX() / currentColumnDX, 0,
                            columnsList.size() - 1);
//                    );

                    if (currentColumnIndex < columnsList.size() && currentColumnIndex >= 0) {
                        columnsList.get(currentColumnIndex).setChoosed(true);
                    }
                    postInvalidate();


                } else {

                    //click on empty place
                    emptyPlaceTouched = true;
                    if (currentColumnIndex >= 0
                        && currentColumnIndex < columnsList.size()) {
                        columnsList.get(currentColumnIndex).setChoosed(false);
                        barTouched = false;
                        currentColumnIndex = -1;
                    }
                    postInvalidate();

                }
                return true;
            }
            if (event.getAction() == MotionEvent.ACTION_UP) {
                if (barTouched) {
                    columnsList.get(currentColumnIndex).onClick(context);
                    columnsList.get(currentColumnIndex)
                            .setChoosed(false);
                    barTouched = false;
                    currentColumnIndex = -1;
                    postInvalidate();
                }
                if (emptyPlaceTouched) {
                    if (currentColumnIndex != -1) {
                        columnsList.get(currentColumnIndex).setChoosed(false);
                    }

                    emptyPlaceTouched = false;
                }

                ((MainActivity) context).ui.mainScroll.setScrollEnabled(true);
                ((MainActivity) context).ui.taskListFragment.taskScroll.setScrollEnabled(true);
                ((MainActivity) context).ui.taskListFragment.myCoordinatorLayout.setScrollEnabled
                        (true);
                return true;

            }

//            return super.onTouchEvent(event);
            return false;
        }


        /**
         * @return Math.round in [min,max];
         */
        int round(double d, int min, int max) {
            double res = Math.round(d) - 1;
            return (res = (res < min ? min : (int) res)) > max ? max : (int) res;
        }

        void changeScale() {
            if (commonView) {

                commonView = false;
            } else {
                commonView = true;
            }

            postInvalidate();

        }
    }

    static class Column {
        private String date   = "";
        private float  height = 0;
        private RepetitionFragmentLeft.Result result;
        private boolean choosed = false;

        public Column(String date, RepetitionFragmentLeft.Result result) {
            this.date = date;
            this.result = result;
        }

        public Column(String date, RepetitionFragmentLeft.Result result, float height) {
            this.date = date;
            this.height = height;
            this.result = result;
        }

        public String getDate() {
            return date;
        }

        public float getHeight() {
            return height;
        }

        public float getPrimaryScore() {
            return getResult().getScorePrimary();
        }

        public float getSecondaryScore() {
            return getResult().getScoreSecondary();
        }

        public RepetitionFragmentLeft.Result getResult() {
            return result;
        }


        public boolean isChoosed() {
            return choosed;
        }

        void setChoosed(boolean choosed) {

            this.choosed = choosed;
        }

        public void onClick(Context context) {
            ((MainActivity) context).ui.openRepetitionResultWindow(result);
        }
    }


}
