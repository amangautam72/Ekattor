package com.creativeitem.ekattorschoolmanager;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import Adapters.StudyAndSyllabusAdapter;
import DataModels.StudyAndSyllabusModel;

public class AcademicSyllabusActivity extends ActionBarActivity implements ServerManager.ServerResponseHandler{

    private SharedPreferences preferences;

    final private int GET_ACADEMIC_SYLLABUS_REQUEST = 1000;

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_academic_syllabus);

    }

    @Override
    protected void onStart() {
        super.onStart();

        //Onback Press
        LinearLayout back = (LinearLayout) findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        preferences = getApplicationContext().getSharedPreferences("EkattorSchoolManagerPrefs", 0);
        ServerManager serverManager = new ServerManager(this);
        String authKey = preferences.getString("AUTHKEY", null);
        String loginType = preferences.getString("LOGINTYPE", "");

        progressDialog = new ProgressDialog(this);
        progressDialog.show();

        String Id="";
        Intent intent = getIntent();
        if (intent.getExtras()!=null) {
            if (loginType.equals("teacher"))
                Id = intent.getStringExtra("classid");
            if (loginType.equals("parent"))
                Id = intent.getStringExtra("studentId");
        }


        if (loginType.equals("student")){
            Id = preferences.getString("CLASSID","");
        }

        System.out.println("==========="+Id);
        serverManager.getAcademicSyllabus(authKey,loginType,Id,GET_ACADEMIC_SYLLABUS_REQUEST);
    }

    @Override
    public void requestFinished(String response, int requestTag) {

        if (progressDialog.isShowing())
            progressDialog.dismiss();

        if (requestTag == GET_ACADEMIC_SYLLABUS_REQUEST) {
            System.out.println("===========" + response);

            List<StudyAndSyllabusModel> academicSyllabusList = new ArrayList<>();

            try {
                JSONArray jsonArray = new JSONArray(response);
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject object = jsonArray.getJSONObject(i);
                    String title = object.getString("title");
                    String description = object.getString("description");
                    String subject = object.getString("subject_name");
                    String time = object.getString("time");
                    Date date = new Date(Long.parseLong(time)*1000L);
                    DateFormat format = new SimpleDateFormat("dd/MM/yyyy");
                    format.setTimeZone(TimeZone.getTimeZone("GMT+05:30"));
                    String formattedTime = format.format(date);
                    String fileUrl = object.getString("file_name");

                    academicSyllabusList.add(new StudyAndSyllabusModel(title, description, formattedTime, subject,fileUrl));
                }

                ListView listView = (ListView) findViewById(R.id.academic_syllabus);
                StudyAndSyllabusAdapter adapter = new StudyAndSyllabusAdapter(AcademicSyllabusActivity.this, academicSyllabusList);
                listView.setAdapter(adapter);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void requestFailed(String errorMessage, int requestTag) {

    }

    @Override
    public void imageDownloaded(Bitmap image, int requestTag) {

    }
}
