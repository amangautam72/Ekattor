package com.creativeitem.ekattorschoolmanager;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import Adapters.NoticeboardGridAdapter;
import Adapters.SelectionListAdapter;
import DataModels.EventInfo;

public class NoticeboardActivity extends ActionBarActivity implements ServerManager.ServerResponseHandler{

    private int selectedMonth;
    private NoticeboardGridAdapter noticeboardGridAdapter;
    private List<String> dates;
    private List<Boolean> hasEvent;
    private List<EventInfo> events;
    private List<Integer> eventPositions;
    List<String> monthList;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_noticeboard);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        initValues();

    }

    // initialize values and settings
    private void initValues() {

        //onBack Press
        LinearLayout back = (LinearLayout) findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });


        events = new ArrayList<>();
        SharedPreferences sharedPreferences = getSharedPreferences("EkattorSchoolManagerPrefs", 0);
        String authKey = sharedPreferences.getString("AUTHKEY", "");
        String loginType = sharedPreferences.getString("LOGINTYPE", "");
        progressDialog = new ProgressDialog(this);
        ServerManager serverManager = new ServerManager(this);
        progressDialog.show();
        serverManager.getEvents(authKey, loginType, 0);
        selectedMonth = 0;

        int current_month = Calendar.getInstance().get(Calendar.MONTH);
        String[] months = {"Select month", "January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"};
        monthList = new ArrayList<>(Arrays.asList(months));
        final SelectionListAdapter selectionListAdapter = new SelectionListAdapter(this, monthList);
//        Spinner monthSpinner = (Spinner)findViewById(R.id.noticeboard_month_spinner);
//        monthSpinner.setAdapter(selectionListAdapter);
//        monthSpinner.setSelection(0);

        //Custom Month Spinner
        LinearLayout monthListCustomSpinner = (LinearLayout)findViewById(R.id.month_custom_list_spinner);
        monthListCustomSpinner.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(NoticeboardActivity.this);
                LayoutInflater inflater = NoticeboardActivity.this.getLayoutInflater();
                final View dialogView = inflater.inflate(R.layout.custom_spinner_dialog, null);
                dialogBuilder.setView(dialogView);

                ListView month_list = (ListView)dialogView.findViewById(R.id.custom_list_spinner_dialog);
                month_list.setAdapter(selectionListAdapter);
                month_list.setSelection(0);

                final AlertDialog b = dialogBuilder.create();
                b.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                b.show();

                month_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                        selectedMonth = position;
                        if (selectedMonth > 0)
                            monthSelected();

                        TextView monthText = (TextView)findViewById(R.id.month);
                        String month = String.valueOf(adapterView.getItemAtPosition(position));
                        monthText.setText(month);
                        b.cancel();
                    }
                });
            }
        });



    }

    // setting up the month view after selecting a month
    private void monthSelected() {
        eventPositions = new ArrayList<>();
        Calendar calendar = new GregorianCalendar();
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        calendar.set(Calendar.MONTH, selectedMonth - 1);
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        calendar.set(Calendar.YEAR, currentYear);
        int startDay = calendar.get(Calendar.DAY_OF_WEEK);
        int numberOfDays = calendar.getActualMaximum(Calendar.DATE);
        dates.clear();
        hasEvent.clear();
        for (int i=1;i<startDay;i++) {
            dates.add("");
            hasEvent.add(false);
            eventPositions.add(-1);
        }
        for (int i=1;i<=numberOfDays;i++) {
            Integer eventPosition = -1;
            dates.add(String.valueOf(i));
            Boolean eventFlag = false;
            for (int j=0;j<events.size();j++) {
                Calendar eventCalender = Calendar.getInstance();
                eventCalender.setTime(events.get(j).getEventDate());
                int day = eventCalender.get(Calendar.DATE);
                int month = eventCalender.get(Calendar.MONTH);
                int year = eventCalender.get(Calendar.YEAR);
                if (day == i && month == (selectedMonth-1) && year == currentYear) {
                    eventFlag = true;
                    eventPosition = j;
                }
            }

            hasEvent.add(eventFlag);
            eventPositions.add(eventPosition);
        }
        int remaining = 42 - dates.size();
        for (int i=0;i<remaining;i++) {
            dates.add("");
            hasEvent.add(false);
            eventPositions.add(-1);
        }

        noticeboardGridAdapter.notifyDataSetChanged();
    }

    // shows details of an event
    public void showEventDetails(View view) {
        int tag = (int)view.getTag();
        int eventPosition = eventPositions.get(tag);

        if(eventPosition == -1)
            return;

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle(events.get(eventPosition).getNoticeTitle());
        alertDialogBuilder.setMessage(events.get(eventPosition).getNoticeDescription());
        alertDialogBuilder.setPositiveButton("Ok", null);
        AlertDialog alert = alertDialogBuilder.create();
        alert.show();
    }

    // shows an alert
    void showAlert(String alertMessage) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setMessage(alertMessage);
        alertDialogBuilder.setPositiveButton("Ok", null);
        AlertDialog alert = alertDialogBuilder.create();
        alert.show();
    }

    // server response methods
    @Override
    public void requestFinished(String response, int requestTag) {
        if (progressDialog.isShowing())
            progressDialog.dismiss();
        boolean jsonError = false;
        try {
            JSONArray jsonArray = new JSONArray(response);
            for (int i=0;i<jsonArray.length();i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                String title = jsonObject.optString("notice_title");
                String description = jsonObject.optString("notice");
                String timeStamp = jsonObject.optString("create_timestamp");
                long timeStampValue = Long.parseLong(timeStamp)*1000;
                Date eventDate = new Date(timeStampValue);
                events.add(new EventInfo(title, description, timeStamp, eventDate));

                System.out.println("EVENTS : "+events);
            }
        }
        catch (JSONException e) {
            jsonError = true;
        }

        //setting current month to custom spinner
        Calendar eventCalender = Calendar.getInstance();
        int month = eventCalender.get(Calendar.MONTH);

        selectedMonth = month+1;
        String monthString = monthList.get(month+1);
        TextView monthText = (TextView)findViewById(R.id.month);
        monthText.setText(monthString);

        //----------------------------//


        dates = new ArrayList<>();
        hasEvent = new ArrayList<>();
        noticeboardGridAdapter = new NoticeboardGridAdapter(this, dates, hasEvent);
        GridView gridView = (GridView) findViewById(R.id.noticeboard_gridView);
        gridView.setAdapter(noticeboardGridAdapter);


        eventPositions = new ArrayList<>();
        Calendar calendar = new GregorianCalendar();
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        calendar.set(Calendar.MONTH, selectedMonth - 1);
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        calendar.set(Calendar.YEAR, currentYear);
        int startDay = calendar.get(Calendar.DAY_OF_WEEK);
        int numberOfDays = calendar.getActualMaximum(Calendar.DATE);
        dates.clear();
        hasEvent.clear();
        for (int i=1;i<startDay;i++) {
            dates.add("");
            hasEvent.add(false);
            eventPositions.add(-1);
        }
        for (int i=1;i<=numberOfDays;i++) {

            Integer eventPosition = -1;
            dates.add(String.valueOf(i));
            Boolean eventFlag = false;
            for (int j=0;j<events.size();j++) {
                Calendar eventalender = Calendar.getInstance();
                eventalender.setTime(events.get(j).getEventDate());
                int day = eventalender.get(Calendar.DATE);
                int monh = eventalender.get(Calendar.MONTH);
                int year = eventalender.get(Calendar.YEAR);
                if (day == i && monh == (selectedMonth-1) && year == currentYear) {
                    eventFlag = true;
                    eventPosition = j;
                }
            }

            hasEvent.add(eventFlag);
            eventPositions.add(eventPosition);
        }
        int remaining = 42 - dates.size();
        for (int i=0;i<remaining;i++) {
            dates.add("");
            hasEvent.add(false);
            eventPositions.add(-1);
        }
        noticeboardGridAdapter.notifyDataSetChanged();

        if (jsonError) {
            showAlert("An error occurred");
        }
    }

    @Override
    public void requestFailed(String errorMessage, int requestTag) {
        if (progressDialog.isShowing())
            progressDialog.dismiss();
        showAlert("Failed to load event information");
    }

    @Override
    public void imageDownloaded(Bitmap image, int requestTag) {

    }
}
