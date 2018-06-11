package com.creativeitem.ekattorschoolmanager;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.view.Gravity;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import Adapters.NavigationDrawerListAdapter;
import Adapters.NotificationListAdapter;
import DataModels.ChildInfo;
import DataModels.ClassInfo;
import DataModels.NotificationModel;

public class HomeActivity extends ActionBarActivity implements ServerManager.ServerResponseHandler {

    private DrawerLayout navigationDrawer;
    private NavigationDrawerListAdapter adapter;

    ExpandableListView menuList;
    private List<String> headerMenuTitles;
    private HashMap<String, List<String>> childMenuTitles;
    private SharedPreferences preferences;
    private String userType = "";
    private List<ClassInfo> classList;
    private List<ChildInfo> childList;

    final private int GET_SUMMARY_REQUEST = 1000;
    final private int GET_CLASS_LIST_REQUEST = 1001;
    final private int GET_CHILD_LIST_REQUEST = 1002;
    final private int GET_NOTICE_LIST_REQUEST = 1003;

    private ProgressDialog progressDialog;


    LinearLayout firstCategory;
    LinearLayout secondCategory;
    LinearLayout thirdCategory;
    LinearLayout fourthCategory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        initValues();

        firstCategory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if ((userType.equals("admin") || userType.equals("teacher"))) {
                    navigationDrawer.openDrawer(Gravity.START);

                    if (!menuList.isGroupExpanded(1)){
                        menuList.expandGroup(1);
                    }
                }else if (userType.equals("parent")){
                    navigationDrawer.openDrawer(Gravity.START);

                    if (!menuList.isGroupExpanded(2)){
                        menuList.expandGroup(2);
                    }
                    if (menuList.isGroupExpanded(5)){
                        menuList.collapseGroup(5);
                    }
                    if (menuList.isGroupExpanded(3)){
                        menuList.collapseGroup(3);
                    }
                }else if ((userType.equals("student"))){
                    Intent intent = new Intent(HomeActivity.this,ClassRoutineActivity.class);
                    startActivity(intent);
                }



            }
        });

        secondCategory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (userType.equals("teacher")){
                    Intent intent = new Intent(HomeActivity.this, NoticeboardActivity.class);
                    startActivity(intent);
                }else {
                    Intent intent = new Intent(HomeActivity.this, UserListActivity.class);
                    String usertype = "Teachers";
                    intent.putExtra("usertype", usertype);
                    startActivity(intent);
                }
            }
        });


        thirdCategory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (userType.equals("parent")){
                    navigationDrawer.openDrawer(Gravity.START);

                    menuList.expandGroup(5);
//                    if (!menuList.isGroupExpanded(5)){
//                        menuList.expandGroup(5);
//                    }
//                    if (menuList.isGroupExpanded(2)){
//                        menuList.collapseGroup(2);
//                    }
//                    if (menuList.isGroupExpanded(5)){
//                        menuList.collapseGroup(5);
//                    }

                } else if ((userType.equals("student"))){
                    Intent intent = new Intent(HomeActivity.this,StudentExamMarksActivity.class);
                    startActivity(intent);
                } else {
                    Intent intent = new Intent(HomeActivity.this, UserListActivity.class);
                    String usertype = "Parents";
                    intent.putExtra("usertype", usertype);
                    startActivity(intent);
                }

            }
        });

        fourthCategory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if ((userType.equals("student") || userType.equals("parent"))){
                    Intent intent = new Intent(HomeActivity.this,NoticeboardActivity.class);
                    startActivity(intent);
                }else {
                    Intent intent = new Intent(HomeActivity.this, AttendanceActivity.class);
                    startActivity(intent);
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM dd, yyyy", Locale.US);
        ((TextView)findViewById(R.id.date_textView)).setText(dateFormat.format(new Date()));
        preferences = getApplicationContext().getSharedPreferences("EkattorSchoolManagerPrefs", 0);
        ServerManager serverManager = new ServerManager(this);
        String authKey = preferences.getString("AUTHKEY", null);
        String loginType = preferences.getString("LOGINTYPE", "");
        progressDialog = new ProgressDialog(this);
        progressDialog.show();
        serverManager.getNotices(authKey,loginType,GET_NOTICE_LIST_REQUEST);
        serverManager.getSummary(authKey, loginType, GET_SUMMARY_REQUEST);
        serverManager.getClassList(authKey, loginType, GET_CLASS_LIST_REQUEST);
        if (loginType.equals("parent")) {
            String parentId = preferences.getString("USERID", "");
            serverManager.getChildList(authKey, loginType, parentId, GET_CHILD_LIST_REQUEST);
        }
    }

    // initialize values and settings
    public void initValues() {
        preferences = getApplicationContext().getSharedPreferences("EkattorSchoolManagerPrefs", 0);
        String userName = preferences.getString("USERNAME", null);
        ((TextView)findViewById(R.id.username_textView)).setText(userName);

        ImageView firstSection = (ImageView)findViewById(R.id.first_section);
        TextView firstSectionText = (TextView)findViewById(R.id.first_section_text);
        ImageView secondSection = (ImageView)findViewById(R.id.second_section);
        TextView secondSectionText = (TextView)findViewById(R.id.second_section_text);
        ImageView thirdSection = (ImageView)findViewById(R.id.third_section);
        TextView thirdSectionText = (TextView)findViewById(R.id.third_section_text);
        ImageView fourthSection = (ImageView)findViewById(R.id.fourth_section);
        TextView fourthSectionText = (TextView)findViewById(R.id.fourth_section_text);
        firstCategory = (LinearLayout)findViewById(R.id.students);
        secondCategory = (LinearLayout)findViewById(R.id.teachers);
        thirdCategory = (LinearLayout)findViewById(R.id.parents);
        fourthCategory = (LinearLayout)findViewById(R.id.present_today);
        headerMenuTitles = new ArrayList<>();
        childMenuTitles = new HashMap<>();
        classList = new ArrayList<>();
        userType = preferences.getString("LOGINTYPE", "");

        System.out.println("============"+userType);

        if ((userType=="parent"||userType.equals("parent"))||(userType=="student"||userType.equals("student"))){
            firstSection.setImageResource(R.drawable.calendar);
            firstSectionText.setText("Class Routine");
            ((TextView)findViewById(R.id.total_student_textView)).setVisibility(View.GONE);

            thirdSection.setImageResource(R.drawable.ic_003_checklist);
            thirdSectionText.setText("Exam Marks");
            ((TextView)findViewById(R.id.total_parent_textView)).setVisibility(View.GONE);

            fourthSection.setImageResource(R.drawable.calendar);
            fourthSectionText.setText("Notice Board");
            ((TextView)findViewById(R.id.attendance_textView)).setVisibility(View.GONE);
        }
        if (userType=="teacher"||userType.equals("teacher")){
            secondSection.setImageResource(R.drawable.calendar);
            secondSectionText.setText("Notice Board");
            ((TextView)findViewById(R.id.total_teacher_textView)).setVisibility(View.GONE);

        }
        switch (userType) {
            case "admin":
                String[] adminTitles = {"Home", "Student", "Teacher", "Parent", "Subject", "Class Routine","Study Material","Academic Syllabus", "Daily Attendance", "Exam Marks", "Noticeboard", "Message", "Accounting", "Profile", "Log out"};
                headerMenuTitles.addAll(Arrays.asList(adminTitles));
                break;
            case "teacher":
                String[] teacherTitles = {"Home", "Student", "Subject", "Class Routine","Study Material","Academic Syllabus", "Daily Attendance", "Exam Marks", "Noticeboard","Message", "Profile", "Log out"};
                headerMenuTitles.addAll(Arrays.asList(teacherTitles));
                break;
            case "student":
                String [] studentTitles = {"Home", "Teacher", "Subject", "Class Routine","Study Material","Academic Syllabus", "Exam Marks", "Noticeboard","Message", "Accounting", "Profile", "Log out"};
                headerMenuTitles.addAll(Arrays.asList(studentTitles));
                break;
            case "parent":
                String[] parentTitles = {"Home", "Teacher", "Class Routine","Study Material","Academic Syllabus", "Exam Marks", "Noticeboard","Message", "Accounting", "Profile", "Log out"};
                headerMenuTitles.addAll(Arrays.asList(parentTitles));
                break;
            default:
                break;
        }
        navigationDrawer = (DrawerLayout)findViewById(R.id.navigationDrawer);
        menuList = (ExpandableListView)findViewById(R.id.menu_listView);
        adapter = new NavigationDrawerListAdapter(this, headerMenuTitles, childMenuTitles);

        menuList.setAdapter(adapter);
        menuList.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
            @Override
            public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
                headerMenuItemSelected(groupPosition);
                if ((userType.equals("admin") || userType.equals("teacher")) && groupPosition == 1 && classList.size() > 0) {

                    return false;
                }
                if (userType.equals("teacher") && groupPosition == 4 && classList.size()>0){
                    return false;
                }
                if (userType.equals("teacher") && groupPosition == 5 && classList.size()>0){
                    return false;
                }

                if (userType.equals("parent") && (groupPosition == 2 || groupPosition == 3 || groupPosition == 4 || groupPosition == 5||groupPosition==8) && childList.size() > 0){
                    return false;
                }



                return true;
            }
        });
        menuList.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                childMenuItemSelected(groupPosition, childPosition);
                return false;
            }
        });
    }

    // navigation menu selection method
    public void headerMenuItemSelected(final int position) {

        if (position == 0) {
            navigationDrawer.closeDrawer(Gravity.START);
        }
        else if (position == headerMenuTitles.size() - 1) {
            logOutAction();
        }
        else if (headerMenuTitles.get(position).equals("Teacher") || headerMenuTitles.get(position).equals("Parent")) {
            Intent intent = new Intent(this, UserListActivity.class);
            String usertype = headerMenuTitles.get(position) + "s";
            System.out.println("============="+usertype);
            intent.putExtra("usertype", usertype);
            startActivity(intent);
        }
        else if (headerMenuTitles.get(position).equals("Subject")) {
            Intent intent = new Intent(this, SubjectsActivity.class);
            startActivity(intent);
        }
        else if (headerMenuTitles.get(position).equals("Class Routine")) {

            if (!userType.equals("parent")) {
                Intent intent = new Intent(this, ClassRoutineActivity.class);
                if (userType.equals("student"))
                    intent.putExtra("studentId", preferences.getString("USERID", ""));
                startActivity(intent);
            }
        }
        else if (headerMenuTitles.get(position).equals("Study Material")){
            if (!userType.equals("teacher")&&!userType.equals("parent")) {
                Intent intent = new Intent(this, StudyMaterialActivity.class);
                startActivity(intent);
            }
        }
        else if (headerMenuTitles.get(position).equals("Academic Syllabus")){
            if (!userType.equals("teacher")&&!userType.equals("parent")) {
                Intent intent = new Intent(this, AcademicSyllabusActivity.class);
                startActivity(intent);
            }
        }
        else if (headerMenuTitles.get(position).equals("Academic Syllabus")){
            Intent intent = new Intent(this, AcademicSyllabusActivity.class);
            startActivity(intent);
        }
        else if (headerMenuTitles.get(position).equals("Daily Attendance")) {
            Intent intent = new Intent(this, AttendanceActivity.class);
            startActivity(intent);
        }
        else if (headerMenuTitles.get(position).equals("Exam Marks")) {
            if (userType.equals("student")) {
                Intent intent = new Intent(this, StudentExamMarksActivity.class);
                startActivity(intent);
            }
            else if (!userType.equals("parent")) {
                Intent intent = new Intent(this, ExamMarksActivity.class);
                startActivity(intent);
            }
        }
        else if (headerMenuTitles.get(position).equals("Noticeboard")) {
            Intent intent = new Intent(this, NoticeboardActivity.class);
            startActivity(intent);
        }
        else if (headerMenuTitles.get(position).equals("Message")) {
            Intent intent = new Intent(this, MessageActivity.class);
            startActivity(intent);
        }
        else if (headerMenuTitles.get(position).equals("Accounting")) {
                if (userType.equals("student")) {
                    Intent intent = new Intent(this, StudentAccountingActivity.class);
                    startActivity(intent);
                } else if (!userType.equals("parent")) {
                    Intent intent = new Intent(this, AccountingActivity.class);
                    startActivity(intent);
                }

        }
        else if (headerMenuTitles.get(position).equals("Profile")) {
            Intent intent = new Intent(this, MyProfileActivity.class);
            startActivity(intent);
        }
    }

    // navigation menu selection method
    public void childMenuItemSelected(int headerPosition, int childPosition) {
        if ((userType.equals("admin") || userType.equals("teacher")) && headerPosition == 1) {
            Intent intent = new Intent(this, UserListActivity.class);
            intent.putExtra("usertype", "Students");
            intent.putExtra("classid", classList.get(childPosition).getClassId());
            intent.putExtra("classname", classList.get(childPosition).getClassName());
            startActivity(intent);
        }
        else if (userType.equals("teacher")&&headerPosition == 4){
            Intent intent = new Intent(this, StudyMaterialActivity.class);
            intent.putExtra("classid", classList.get(childPosition).getClassId());
            startActivity(intent);
        }
        else if (userType.equals("teacher")&&headerPosition == 5){
            Intent intent = new Intent(this, AcademicSyllabusActivity.class);
            intent.putExtra("classid", classList.get(childPosition).getClassId());
            startActivity(intent);
        }
        else if (userType.equals("parent") && headerPosition == 2) {
            Intent intent = new Intent(this, ClassRoutineActivity.class);
            intent.putExtra("studentId", childList.get(childPosition).getChildId());
            startActivity(intent);
        }
        else if (userType.equals("parent") && headerPosition == 3) {
            Intent intent = new Intent(this, StudyMaterialActivity.class);
            System.out.println("STUDENTID : "+childList.get(childPosition).getChildId());
            intent.putExtra("studentId", childList.get(childPosition).getChildId());
            startActivity(intent);
        }
        else if (userType.equals("parent") && headerPosition == 4) {
            Intent intent = new Intent(this, AcademicSyllabusActivity.class);
            System.out.println("STUDENTID : "+childList.get(childPosition).getChildId());
            intent.putExtra("studentId", childList.get(childPosition).getChildId());
            startActivity(intent);
        }
        else if (userType.equals("parent") && headerPosition == 5) {
            Intent intent = new Intent(this, StudentExamMarksActivity.class);
            intent.putExtra("childId", childList.get(childPosition).getChildId());
            startActivity(intent);
        }
        else if (userType.equals("parent") && headerPosition == 8) {
            Intent intent = new Intent(this, StudentAccountingActivity.class);
            intent.putExtra("childId", childList.get(childPosition).getChildId());
            startActivity(intent);
        }
    }


    // show navigation menu
    public void showMenuAction(View view) {
        navigationDrawer.openDrawer(Gravity.START);

        for (int i=0;i<headerMenuTitles.size();i++){
            if (menuList.isGroupExpanded(i)){
                menuList.collapseGroup(i);
            }
        }
    }

    // log out action
    public void logOutAction() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setMessage("Confirm Sign Out?");
        alertDialogBuilder.setPositiveButton("ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                SharedPreferences.Editor editor = preferences.edit();
                editor.clear();
                editor.apply();
                finish();
            }
        });
        alertDialogBuilder.setNegativeButton("cancel", null);
        AlertDialog alert = alertDialogBuilder.create();
        alert.show();
    }

    // server response methods
    @Override
    public void onBackPressed() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setMessage("Are you sure you want to exit?");
        alertDialogBuilder.setPositiveButton("ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                moveTaskToBack(true);
                android.os.Process.killProcess(android.os.Process.myPid());
                System.exit(1);
            }
        });
        alertDialogBuilder.setNegativeButton("cancel", null);
        AlertDialog alert = alertDialogBuilder.create();
        alert.show();
    }

    @Override
    public void requestFinished(String response, int requestTag) {

        if (progressDialog.isShowing())
            progressDialog.dismiss();
        boolean jsonError = false;

        if (requestTag == GET_NOTICE_LIST_REQUEST)
        {
            List<NotificationModel> notificationList = new ArrayList<>();
            try {
                JSONArray jsonArray = new JSONArray(response);
                for (int i=0;i<jsonArray.length();i++)
                {
                    JSONObject object = jsonArray.getJSONObject(i);
                    String title = object.getString("notice_title");
                    String notification = object.getString("notice");
                    String date = object.getString("date");
                    notificationList.add(new NotificationModel(title,notification,date));

                }
                ListView listView = (ListView)findViewById(R.id.notifications);
                NotificationListAdapter adapter = new NotificationListAdapter(HomeActivity.this,notificationList);
                listView.setAdapter(adapter);

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        if (requestTag == GET_SUMMARY_REQUEST) {
            try {
                JSONObject responseJson = new JSONObject(response);
                int totalStudent = responseJson.optInt("total_student");
                int totalTeacher = responseJson.optInt("total_teacher");
                int totalParent = responseJson.optInt("total_parent");
                int totalAttendance = responseJson.optInt("total_present_today");
                String totalStudentText = "("+ String.valueOf(totalStudent) + ")";
//                if (totalStudent > 1)
//                    totalStudentText = totalStudentText + "s";
                String totalTeacherText = "("+String.valueOf(totalTeacher)+")";
//                if (totalTeacher > 1)
//                    totalTeacherText = totalTeacherText + "s";
                String totalParentText = "("+String.valueOf(totalParent)+")";
//                if (totalParent > 1)
//                    totalParentText = totalParentText + "s";
                String totalAttendanceText = "("+String.valueOf(totalAttendance)+")";
                ((TextView) findViewById(R.id.total_student_textView)).setText(totalStudentText);
                ((TextView)findViewById(R.id.total_teacher_textView)).setText(totalTeacherText);
                ((TextView)findViewById(R.id.total_parent_textView)).setText(totalParentText);
                ((TextView)findViewById(R.id.attendance_textView)).setText(totalAttendanceText);
            }
            catch (JSONException e) {
                jsonError = true;
                System.out.println("JSON : "+jsonError);
            }
        }
        else if (requestTag == GET_CLASS_LIST_REQUEST) {
            System.out.println("CLASS : "+response);
            try {
                classList = new ArrayList<>();
                List<String> classNames = new ArrayList<>();
                JSONArray jsonArray = new JSONArray(response);
                for (int i=0;i<jsonArray.length();i++) {
                    JSONObject jsonObj = jsonArray.getJSONObject(i);
                    String classId = jsonObj.optString("class_id");
                    String className = jsonObj.optString("name");
                    classNames.add(className);
                    classList.add(new ClassInfo(classId, className));
                }
                childMenuTitles.put(headerMenuTitles.get(1), classNames);
                if(userType.equals("teacher")) {
                    childMenuTitles.put(headerMenuTitles.get(4), classNames);
                    childMenuTitles.put(headerMenuTitles.get(5), classNames);
                }
                adapter.notifyDataSetChanged();
            }

            catch (JSONException e) {
                jsonError = true;
            }
        }
        else if (requestTag == GET_CHILD_LIST_REQUEST) {
            System.out.println("CHILDLIST : "+response);
            try {
                childList = new ArrayList<>();
                List<String> childNames = new ArrayList<>();
                JSONObject jsonObject = new JSONObject(response);
                JSONArray jsonArray = jsonObject.getJSONArray("children");
                for (int i=0;i<jsonArray.length();i++) {
                    JSONObject jsonObj = jsonArray.getJSONObject(i);
                    String childId = jsonObj.optString("student_id");
                    String childName = jsonObj.optString("name");
                    String classId = jsonObj.optString("class_id");
                    childNames.add(childName);
                    childList.add(new ChildInfo(childId, childName, classId));
                }

                childMenuTitles.put(headerMenuTitles.get(2), childNames);
                childMenuTitles.put(headerMenuTitles.get(3), childNames);
                childMenuTitles.put(headerMenuTitles.get(4), childNames);
                childMenuTitles.put(headerMenuTitles.get(5), childNames);
                childMenuTitles.put(headerMenuTitles.get(8), childNames);
                adapter.notifyDataSetChanged();
            }
            catch (JSONException e) {
                jsonError = true;
            }
        }
        if (jsonError) {
            showAlert("An error occurred");
        }
    }

    @Override
    public void requestFailed(String errorMessage, int requestTag) {
        if (progressDialog.isShowing())
            progressDialog.dismiss();
        String alertMessage = "";
        if (requestTag == GET_SUMMARY_REQUEST)
            alertMessage = "Could not load summury information";
        else if (requestTag == GET_CLASS_LIST_REQUEST)
            alertMessage = "Could not load class list";
        else if (requestTag == GET_CHILD_LIST_REQUEST)
            alertMessage = "Could not load child list";
        showAlert(alertMessage);
    }

    @Override
    public void imageDownloaded(Bitmap image, int requestTag) {

    }

    // shows an alert
    void showAlert(String alertMessage) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setMessage(alertMessage);
        alertDialogBuilder.setPositiveButton("Ok", null);
        AlertDialog alert = alertDialogBuilder.create();
        alert.show();
    }
}
