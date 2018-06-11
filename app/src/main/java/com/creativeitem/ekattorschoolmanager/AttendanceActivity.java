package com.creativeitem.ekattorschoolmanager;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import Adapters.AttendanceListAdapter;
import Adapters.SelectionListAdapter;
import DataModels.AttendanceInfo;
import DataModels.ClassInfo;
import DataModels.SectionInfo;

public class AttendanceActivity extends ActionBarActivity implements ServerManager.ServerResponseHandler,DataTransferFromAdapterToActivity{

    private SelectionListAdapter classListAdapter;
    private List<String> classNames;
    private ServerManager serverManager;
    private String authKey;
    private String loginType;
    private String[] monthNames;
    private int selectedDay;
    private int selectedMonth;
    private int selectedYear;
    private int selectedClass;
    private int selectedSection;
    private List<ClassInfo> classList;
    private List<SectionInfo> sectionList;
    private List<String> sectionNames;
    private List<String> sectionIDsOfClass;
    private List<AttendanceInfo> attendanceList;
    ListView attendanceListView;
    private AttendanceListAdapter attendanceListAdapter;
    private ProgressDialog progressDialog;

    private String selectedClassId;
    private String selectedSectionID;
    private String timestamp;

    private int attendanceTag = 0;
    private final int GET_CLASS_LIST_REQUEST = 1000;
    private final int GET_ATTENDANCE_REQUEST = 1001;
    public final int GET_ATTENDANCE_RESPONSE = 1003;

    private List<String> statusList;

    CheckBox markAll;

    TextView sectionText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attendance);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        initValues();

    }

    // initialize values and settings
    public void initValues() {
        sectionText = (TextView)findViewById(R.id.section);
        monthNames = new String[]{"January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"};
        selectedDay = -1;
        selectedMonth = -1;
        selectedYear = -1;
        selectedClass = -1;
        selectedSection = -1;
        serverManager = new ServerManager(this);
        SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences("EkattorSchoolManagerPrefs", 0);
        authKey = sharedPreferences.getString("AUTHKEY", "");
        loginType = sharedPreferences.getString("LOGINTYPE", "");
        attendanceList = new ArrayList<>();
        statusList = new ArrayList<>();
        //attendanceListAdapter = new AttendanceListAdapter(this, attendanceList,selectedClass,selectedSection);
        attendanceListView = (ListView)findViewById(R.id.attendance_listView);
       // attendanceListView.setAdapter(attendanceListAdapter);
        setUpClassPicker();
        progressDialog = new ProgressDialog(this);


        //onBack Press
        LinearLayout back = (LinearLayout) findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });



        markAll = (CheckBox)findViewById(R.id.mark_all);
        markAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (markAll.isChecked()){
                    attendanceTag = 1;

                    if (selectedDay != -1 && selectedMonth != -1 && selectedYear != -1 && selectedClass != -1 && selectedSection !=-1) {
                        progressDialog.show();
                        serverManager.getAttendance(authKey, loginType, selectedDay, selectedMonth, selectedYear, classList.get(selectedClass).getClassId(),sectionIDsOfClass.get(selectedSection), GET_ATTENDANCE_REQUEST);

                    }


                }else {
                    attendanceTag = 0;
                    if (selectedDay != -1 && selectedMonth != -1 && selectedYear != -1 && selectedClass != -1 && selectedSection !=-1) {
                        progressDialog.show();

                        serverManager.getAttendance(authKey, loginType, selectedDay, selectedMonth, selectedYear, classList.get(selectedClass).getClassId(),sectionIDsOfClass.get(selectedSection), GET_ATTENDANCE_REQUEST);

                    }
                }
            }
        });


    }




    // configuring the class spinner
    public void setUpClassPicker() {
        classNames = new ArrayList<>();
        classNames.add("Select class");
        classListAdapter = new SelectionListAdapter(this, classNames);
//        Spinner classSpinner = (Spinner)findViewById(R.id.attendanceClassSpinner);
//        classSpinner.setAdapter(classListAdapter);


        sectionNames = new ArrayList<>();
        sectionNames.add("Select Section");
        sectionIDsOfClass = new ArrayList<>();
        final SelectionListAdapter sectionListAdapter = new SelectionListAdapter(this, sectionNames);

        //Custom Class Spinner
        LinearLayout classListCustomSpinner = (LinearLayout)findViewById(R.id.class_list_custom_spinner);

        classListCustomSpinner.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(AttendanceActivity.this);
                LayoutInflater inflater = AttendanceActivity.this.getLayoutInflater();
                final View dialogView = inflater.inflate(R.layout.custom_spinner_dialog, null);
                dialogBuilder.setView(dialogView);

                ListView class_list = (ListView)dialogView.findViewById(R.id.custom_list_spinner_dialog);
                class_list.setAdapter(classListAdapter);

                final AlertDialog b = dialogBuilder.create();
                b.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                b.show();

                class_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                        int choice=position-1;

                        if (choice!=selectedClass){
                            attendanceListView.setAdapter(null);
                            sectionText.setText("A");
                            selectedSection = 0;
                        }
                        selectedClass = position - 1;

                        String Class = String.valueOf(adapterView.getItemAtPosition(position));
                        TextView classText = (TextView)findViewById(R.id.Class);
                        classText.setText(Class);

                        if (selectedClass >= 0) {
                            String selectedClassId = classList.get(selectedClass).getClassId();
                            sectionNames.clear();
                            sectionIDsOfClass.clear();
                            if (sectionIDsOfClass.size() > 0)
                                sectionIDsOfClass.clear();
                            sectionNames.add("Select Section");

                            for (int i=0;i<sectionList.size();i++) {
                                if (sectionList.get(i).getClassID().equals(selectedClassId)) {
                                    sectionNames.add(sectionList.get(i).getSectionName());
                                    sectionIDsOfClass.add(sectionList.get(i).getSectionID());
                                }
                            }

                            System.out.println("SECTION LIST : "+sectionNames+" - "+sectionIDsOfClass );
                            sectionListAdapter.notifyDataSetChanged();
                        }

                        b.cancel();
                    }
                });
            }
        });


        //Section Custom Spinner
        LinearLayout sectionCustomListSpinner = (LinearLayout)findViewById(R.id.section_custom_list_spinner);
        sectionCustomListSpinner.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(AttendanceActivity.this);
                LayoutInflater inflater = AttendanceActivity.this.getLayoutInflater();
                final View dialogView = inflater.inflate(R.layout.custom_spinner_dialog, null);
                dialogBuilder.setView(dialogView);

                ListView sectionList = (ListView)dialogView.findViewById(R.id.custom_list_spinner_dialog);
                sectionList.setAdapter(sectionListAdapter);

                final AlertDialog b = dialogBuilder.create();
                b.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                b.show();

                sectionList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {

                        int choice=position-1;

                        if (choice!=selectedClass){
                            attendanceListView.setAdapter(null);
                        }
                        selectedSection = position-1;

                        String section = String.valueOf(adapterView.getItemAtPosition(position));
                        sectionText.setText(section);

                        b.cancel();
                    }
                });
            }
        });

//        classSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//            @Override
//            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//                selectedClass = position - 1;
//            }
//
//            @Override
//            public void onNothingSelected(AdapterView<?> parent) {
//
//            }
//        });
        serverManager.getClassList(authKey, loginType, GET_CLASS_LIST_REQUEST);
    }

    // shows an date picker on press
    public void datePickerButtonAction(View view) {
        Calendar calendar = Calendar.getInstance();
        final int day = calendar.get(Calendar.DAY_OF_MONTH);
        final int month = calendar.get(Calendar.MONTH);
        int year = calendar.get(Calendar.YEAR);
        DatePickerDialog datePickerDialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                String dateString = monthNames[monthOfYear] + " " + String.valueOf(dayOfMonth) + ", " + String.valueOf(year);
                TextView dateButton = (TextView) findViewById(R.id.datePicker_button);
                dateButton.setText(dateString);
                selectedDay = dayOfMonth;
                selectedMonth = monthOfYear+1;
                selectedYear = year;
            }
        }, year, month, day);
        datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
        datePickerDialog.show();
    }

    // views attendance information fetched from server
    public void viewButtonAction(View view) {

        if (markAll.isChecked()){
            markAll.setChecked(false);
        }

        if (selectedDay==-1)
            Toast.makeText(this, "Please select date", Toast.LENGTH_SHORT).show();


        if (selectedDay != -1 && selectedMonth != -1 && selectedYear != -1 && selectedClass != -1 && selectedSection !=-1 ) {
            progressDialog.show();
            LinearLayout header = (LinearLayout)findViewById(R.id.header);
            header.setVisibility(View.VISIBLE);
            Toast.makeText(this, "Class ID : "+selectedClass+"Section ID : "+selectedSection, Toast.LENGTH_SHORT).show();
            System.out.println("POST : "+authKey+" "+loginType+" "+selectedDay+" "+selectedMonth+" "+selectedYear+" "+classList.get(selectedClass).getClassId()+" "+sectionIDsOfClass.get(selectedSection)+" "+sectionList.get(selectedSection).getSectionName());
            serverManager.getAttendance(authKey, loginType, selectedDay, selectedMonth, selectedYear, classList.get(selectedClass).getClassId(),sectionIDsOfClass.get(selectedSection), GET_ATTENDANCE_REQUEST);

        }
    }

    // shows an alert
    void showAlert(String alertMessage) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setMessage(alertMessage);
        alertDialogBuilder.setPositiveButton("Ok", null);
        AlertDialog alert = alertDialogBuilder.create();
        alert.show();
    }

    public void onSaveChange(View view){

        progressDialog.show();

        JSONArray jsonArray = new JSONArray();

        for (int i=0;i<attendanceList.size();i++){

            try {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("student_id",attendanceList.get(i).getStudentId());
                jsonObject.put("attendance_id",attendanceList.get(i).getAttendanceId());
                jsonObject.put("class_id",selectedClassId);
                jsonObject.put("section_id",selectedSectionID);
                jsonObject.put("timestamp",timestamp);
                jsonObject.put("status",statusList.get(i));
                jsonArray.put(i,jsonObject);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        System.out.println("======="+authKey+" "+loginType+" "+jsonArray);
        serverManager.attendanceUpdate(authKey,loginType,jsonArray,GET_ATTENDANCE_RESPONSE);
    }

    // server response methods
    @Override
    public void requestFinished(String response, int requestTag) {

        System.out.println("RESPONSE : "+response);
        if (progressDialog.isShowing())
            progressDialog.dismiss();
        boolean jsonError = false;
        try {
            if (requestTag == GET_CLASS_LIST_REQUEST) {
                JSONArray jsonArray = new JSONArray(response);
                System.out.println("CLASS LIST : "+jsonArray);
                classList = new ArrayList<>();
                sectionList = new ArrayList<>();
                for (int i=0;i<jsonArray.length();i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    String classId = jsonObject.optString("class_id");
                    String className = jsonObject.optString("name");
                    classNames.add(className);
                    classList.add(new ClassInfo(classId, className));

                    JSONArray sectionArray = jsonObject.getJSONArray("sections");
                    for (int j=0;j<sectionArray.length();j++) {
                        JSONObject sectionObject = sectionArray.getJSONObject(j);
                        String sectionId = sectionObject.optString("section_id");
                        String sectionName = sectionObject.optString("name");
                        String associatedClassId = sectionObject.optString("class_id");
                        sectionList.add(new SectionInfo(sectionId, sectionName, associatedClassId));

                    }

                    System.out.println("SECTION LIST : "+sectionList);
                }
                classListAdapter.notifyDataSetChanged();

            }
            else if (requestTag == GET_ATTENDANCE_REQUEST) {
                attendanceList.clear();
                JSONArray jsonArray = new JSONArray(response);
                for (int i=0;i<jsonArray.length();i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    String studentId = jsonObject.optString("student_id");
                    String attendanceId = jsonObject.optString("attendance_id");
                    String roll = jsonObject.optString("roll");
                    String name = jsonObject.optString("name");
                    String status = jsonObject.optString("status");
                    attendanceList.add(new AttendanceInfo(studentId,attendanceId,roll, name, status));
                }

                String month=null;
                if (selectedMonth<10){
                    month = "0"+selectedMonth;
                }

                selectedClassId = classList.get(selectedClass).getClassId();
                selectedSectionID = sectionList.get(selectedSection).getSectionID();
                timestamp = selectedDay+"-"+month+"-"+selectedYear;


                attendanceListAdapter = new AttendanceListAdapter(AttendanceActivity.this,this, attendanceList,attendanceTag);
                attendanceListView.setAdapter(attendanceListAdapter);

                attendanceListAdapter.notifyDataSetChanged();


            }
            else if (requestTag == GET_ATTENDANCE_RESPONSE){

                try{
                    JSONObject jsonObject = new JSONObject(response);
                    String status = jsonObject.getString("response");

                    if (status=="200"|status.equals("200")){
                        Toast.makeText(this, "Attendance Updated", Toast.LENGTH_SHORT).show();
                    }else{
                        Toast.makeText(this, "Something went wrong", Toast.LENGTH_SHORT).show();
                    }



                }catch (JSONException j){

                }




            }
        }
        catch (JSONException e) {
            jsonError = true;
        }
        if (jsonError) {
            showAlert("An error occurred");
        }
    }

    @Override
    public void requestFailed(String errorMessage, int requestTag) {
        if (progressDialog.isShowing())
            progressDialog.dismiss();
        String message = "";
        if (requestTag == GET_CLASS_LIST_REQUEST)
            message = "Failed to load class list";
        else if (requestTag == GET_ATTENDANCE_REQUEST)
            message = "Failed to load attendance information";
        showAlert(message);
    }

    @Override
    public void imageDownloaded(Bitmap image, int requestTag) {

    }

    @Override
    public void setValues(List<String> status) {
        statusList = status;

        System.out.println("============================="+statusList);
    }
}
