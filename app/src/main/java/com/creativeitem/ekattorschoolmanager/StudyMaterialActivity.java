package com.creativeitem.ekattorschoolmanager;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.test.mock.MockPackageManager;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

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

public class StudyMaterialActivity extends AppCompatActivity implements ServerManager.ServerResponseHandler{

    private SharedPreferences preferences;

    final private int GET_STUDY_MATERIAL_REQUEST = 1000;
    private static final int REQUEST_CODE_PERMISSION = 2;

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_study_material);



    }

    @Override
    protected void onStart() {
        super.onStart();


        //checking storage permissions.
        String[] mPermission = {Manifest.permission.WRITE_EXTERNAL_STORAGE};
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                if (ActivityCompat.checkSelfPermission(this, mPermission[0])
                        != MockPackageManager.PERMISSION_GRANTED) {

                    ActivityCompat.requestPermissions(this,
                            mPermission, REQUEST_CODE_PERMISSION);

                    // If any permission aboe not allowed by user, this condition will execute every tim, else your else part will work
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

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
        serverManager.getStudyMaterial(authKey,loginType,Id,GET_STUDY_MATERIAL_REQUEST);
    }

    @Override
    public void requestFinished(String response, int requestTag) {

        if (progressDialog.isShowing())
            progressDialog.dismiss();

        if (requestTag == GET_STUDY_MATERIAL_REQUEST){
            System.out.println("==========="+response);
            List<StudyAndSyllabusModel> studyMaterialList = new ArrayList<>();

            try {
                JSONArray jsonArray = new JSONArray(response);
                for (int i=0;i<jsonArray.length();i++){
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

                    studyMaterialList.add(new StudyAndSyllabusModel(title,description,formattedTime,subject,fileUrl));
                }

                ListView listView = (ListView)findViewById(R.id.study_materials);
                StudyAndSyllabusAdapter adapter = new StudyAndSyllabusAdapter(StudyMaterialActivity.this,studyMaterialList);
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


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        Log.e("Req Code", "" + requestCode);
        System.out.println("========"+grantResults);
        if (requestCode == REQUEST_CODE_PERMISSION) {
            if (grantResults[0] == MockPackageManager.PERMISSION_GRANTED)  {

                // Success Stuff here

                Toast.makeText(this, "granted", Toast.LENGTH_SHORT).show();

            }
        }

    }
}
