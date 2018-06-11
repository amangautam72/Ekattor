package com.creativeitem.ekattorschoolmanager;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import Adapters.SelectionListAdapter;
import Utility.MultipartUtility;
import Utility.PathUtil;

public class MessageActivity extends AppCompatActivity implements ServerManager.ServerResponseHandler{

    private ServerManager serverManager;
    private String authKey;
    private String loginType;
    private String loginUserId;
    private String recieverId;
    private File file;
    private String multipartResponse;

    EditText fileName;

    List<String> teacherIdList = new ArrayList<>();
    List<String> teacherNameList = new ArrayList<>();

    List<String> adminIdList = new ArrayList<>();
    List<String> adminNameList = new ArrayList<>();

    List<String> studentIdList = new ArrayList<>();
    List<String> studentNameList = new ArrayList<>();

    List<String> parentIdList = new ArrayList<>();
    List<String> parentNameList = new ArrayList<>();

    private ProgressDialog progressDialog;
    EditText messageEditText;

    LinearLayout teacherListCustomSpinner;
    LinearLayout adminListCustomSpinner;
    LinearLayout studentListCustomSpinner;
    LinearLayout parentListCustomSpinner;

    private final int GET_USER_LIST_REQUEST = 1000;
    private final int SEND_MESSAGE_REQUEST_TAG = 1001;

    private final int SELECTION_REQUEST_CODE = 1;
    private static int recieverNameTag=0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);

        messageEditText = (EditText)findViewById(R.id.message);
        fileName = (EditText)findViewById(R.id.file_name);
        //Progress Dialog initializing and showing
        progressDialog = new ProgressDialog(this);
        progressDialog.show();


        //calling Api//
        serverManager = new ServerManager(this);
        SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences("EkattorSchoolManagerPrefs", 0);
        authKey = sharedPreferences.getString("AUTHKEY", "");
        loginUserId = sharedPreferences.getString("USERID","");
        loginType = sharedPreferences.getString("LOGINTYPE", "");
        serverManager.getMessageUserList(authKey,loginType,GET_USER_LIST_REQUEST);

        //Custom Spinners//
        teacherListCustomSpinner = (LinearLayout)findViewById(R.id.teacher_list_custom_spinner);
        adminListCustomSpinner = (LinearLayout)findViewById(R.id.admin_list_custom_spinner);
        studentListCustomSpinner = (LinearLayout)findViewById(R.id.student_list_custom_spinner);
        parentListCustomSpinner = (LinearLayout)findViewById(R.id.parent_list_custom_spinner);

        //checking usertypes//
        if (loginType.equals("admin")){
            adminListCustomSpinner.setVisibility(View.GONE);
        }else if (loginType.equals("teacher")){
            teacherListCustomSpinner.setVisibility(View.GONE);
        }else if (loginType.equals("student")){
            studentListCustomSpinner.setVisibility(View.GONE);
            parentListCustomSpinner.setVisibility(View.GONE);
        }else if (loginType.equals("parent")){
            parentListCustomSpinner.setVisibility(View.GONE);
            studentListCustomSpinner.setVisibility(View.GONE);
        }

        teacherListCustomSpinner.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(MessageActivity.this);
                LayoutInflater inflater = MessageActivity.this.getLayoutInflater();
                final View dialogView = inflater.inflate(R.layout.custom_spinner_dialog, null);
                dialogBuilder.setView(dialogView);

                ListView teacherListView = (ListView)dialogView.findViewById(R.id.custom_list_spinner_dialog);
                SelectionListAdapter userListSelectionAdapter = new SelectionListAdapter(MessageActivity.this, teacherNameList);
                teacherListView.setAdapter(userListSelectionAdapter);

                final AlertDialog b = dialogBuilder.create();
                b.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                b.show();

                teacherListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        recieverId = teacherIdList.get(position);

                        if (Integer.parseInt(recieverId)>0){
//                                Toast.makeText(MessageActivity.this, "message can be sent to one user only", Toast.LENGTH_SHORT).show();
                            recieverNameTag = 1;//for teacher
                            studentListCustomSpinner.setEnabled(false);
                            parentListCustomSpinner.setEnabled(false);
                            adminListCustomSpinner.setEnabled(false);
                        }else {
                            recieverNameTag = 0;
                            studentListCustomSpinner.setEnabled(true);
                            parentListCustomSpinner.setEnabled(true);
                            adminListCustomSpinner.setEnabled(true);
                        }

                        String teacher = String.valueOf(parent.getItemAtPosition(position));
                        TextView teacherText = (TextView)findViewById(R.id.teacher);
                        teacherText.setText(teacher);

                        b.cancel();

                    }
                });
            }
        });


        adminListCustomSpinner.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(MessageActivity.this);
                LayoutInflater inflater = MessageActivity.this.getLayoutInflater();
                final View dialogView = inflater.inflate(R.layout.custom_spinner_dialog, null);
                dialogBuilder.setView(dialogView);

                ListView adminListView = (ListView)dialogView.findViewById(R.id.custom_list_spinner_dialog);
                SelectionListAdapter userListSelectionAdapter = new SelectionListAdapter(MessageActivity.this, adminNameList);
                adminListView.setAdapter(userListSelectionAdapter);

                final AlertDialog b = dialogBuilder.create();
                b.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                b.show();

                adminListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        recieverId = adminIdList.get(position);

                        if (Integer.parseInt(recieverId)>0){
//                                Toast.makeText(MessageActivity.this, "message can be sent to one user only", Toast.LENGTH_SHORT).show();
                            recieverNameTag = 4;//for admin
                            studentListCustomSpinner.setEnabled(false);
                            parentListCustomSpinner.setEnabled(false);
                            teacherListCustomSpinner.setEnabled(false);
                        }else {
                            recieverNameTag = 0;
                            studentListCustomSpinner.setEnabled(true);
                            parentListCustomSpinner.setEnabled(true);
                            teacherListCustomSpinner.setEnabled(true);
                        }

                        String admin = String.valueOf(parent.getItemAtPosition(position));
                        TextView adminText = (TextView)findViewById(R.id.admin);
                        adminText.setText(admin);

                        b.cancel();
                    }
                });

            }
        });


        studentListCustomSpinner.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(MessageActivity.this);
                LayoutInflater inflater = MessageActivity.this.getLayoutInflater();
                final View dialogView = inflater.inflate(R.layout.custom_spinner_dialog, null);
                dialogBuilder.setView(dialogView);

                ListView studentListView = (ListView)dialogView.findViewById(R.id.custom_list_spinner_dialog);
                SelectionListAdapter userListSelectionAdapter = new SelectionListAdapter(MessageActivity.this, studentNameList);
                studentListView.setAdapter(userListSelectionAdapter);

                final AlertDialog b = dialogBuilder.create();
                b.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                b.show();

                studentListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        recieverId = studentIdList.get(position);

                        if (Integer.parseInt(recieverId)>0){
//                                Toast.makeText(MessageActivity.this, "message can be sent to one user only", Toast.LENGTH_SHORT).show();
                            recieverNameTag = 2;//for student
                            teacherListCustomSpinner.setEnabled(false);
                            adminListCustomSpinner.setEnabled(false);
                            parentListCustomSpinner.setEnabled(false);
                        }else {
                            recieverNameTag = 0;
                            teacherListCustomSpinner.setEnabled(true);
                            adminListCustomSpinner.setEnabled(true);
                            parentListCustomSpinner.setEnabled(true);
                        }

                        String student = String.valueOf(parent.getItemAtPosition(position));
                        TextView studentText = (TextView)findViewById(R.id.student);
                        studentText.setText(student);

                        b.cancel();
                    }
                });
            }
        });


        parentListCustomSpinner.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(MessageActivity.this);
                LayoutInflater inflater = MessageActivity.this.getLayoutInflater();
                final View dialogView = inflater.inflate(R.layout.custom_spinner_dialog, null);
                dialogBuilder.setView(dialogView);

                ListView parentListView = (ListView)dialogView.findViewById(R.id.custom_list_spinner_dialog);
                SelectionListAdapter userListSelectionAdapter = new SelectionListAdapter(MessageActivity.this, parentNameList);
                parentListView.setAdapter(userListSelectionAdapter);

                final AlertDialog b = dialogBuilder.create();
                b.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                b.show();

                parentListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                        recieverId = parentIdList.get(position);

                        if (Integer.parseInt(recieverId)>0){
//                                Toast.makeText(MessageActivity.this, "message can be sent to one user only", Toast.LENGTH_SHORT).show();
                            recieverNameTag = 3;//for parent
                            teacherListCustomSpinner.setEnabled(false);
                            adminListCustomSpinner.setEnabled(false);
                            studentListCustomSpinner.setEnabled(false);
                        }else {
                            recieverNameTag = 0;
                            teacherListCustomSpinner.setEnabled(true);
                            adminListCustomSpinner.setEnabled(true);
                            studentListCustomSpinner.setEnabled(true);
                        }

                        String parent = String.valueOf(adapterView.getItemAtPosition(position));
                        TextView parentText = (TextView)findViewById(R.id.parent);
                        parentText.setText(parent);
                        b.cancel();
                    }
                });
            }
        });



        TextView upload = (TextView) findViewById(R.id.browse);
        upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("*/*");
                startActivityForResult(intent, SELECTION_REQUEST_CODE);
            }
        });
    }




    public void sendMessage(View view){
//        System.out.println("DATA : "+authKey+":"+loginType+":"+loginUserId+":"+messageEditText.getText().toString()+":"+recieverId+" :"+SEND_MESSAGE_REQUEST_TAG);
//        serverManager.sendNewMessage(authKey,loginType,loginUserId,"",messageEditText.getText().toString(),recieverId,SEND_MESSAGE_REQUEST_TAG);
        new SendMessage().execute();
    }

    @Override
    public void requestFinished(String response, int requestTag) {
        if (progressDialog.isShowing())
            progressDialog.dismiss();

        if (requestTag == GET_USER_LIST_REQUEST) {
            System.out.println("RESPONSE : "+response);

            try {
                JSONObject jsonObject = new JSONObject(response);

                if (!loginType.equals("teacher")) {
                    JSONArray teachersArray = jsonObject.getJSONArray("teachers");

                    teacherIdList.add("0");
                    teacherNameList.add("Select Teacher");
                    for (int i = 0; i < teachersArray.length(); i++) {
                        JSONObject data = teachersArray.getJSONObject(i);
                        String teacherId = data.getString("teacher_id");
                        String teacherName = data.getString("teacher_name");
                        teacherIdList.add(teacherId);
                        teacherNameList.add(teacherName);
                    }
                }

                if (!loginType.equals("admin")) {
                    adminIdList.add("0");
                    adminNameList.add("Select Admin");
                    JSONArray adminArray = jsonObject.getJSONArray("admin");
                    for (int j = 0; j < adminArray.length(); j++) {
                        JSONObject data = adminArray.getJSONObject(j);
                        String adminId = data.getString("admin_id");
                        String adminName = data.getString("admin_name");
                        adminIdList.add(adminId);
                        adminNameList.add(adminName);
                    }
                }

                if (!loginType.equals("student")||!loginType.equals("parent")) {
                    studentIdList.add("0");
                    studentNameList.add("Select Student");
                    JSONArray studentArray = jsonObject.getJSONArray("students");
                    for (int j = 0; j < studentArray.length(); j++) {
                        JSONObject data = studentArray.getJSONObject(j);
                        String studentId = data.getString("student_id");
                        String studentName = data.getString("student_name");
                        studentIdList.add(studentId);
                        studentNameList.add(studentName);
                    }
                }

                if (!loginType.equals("parent")||!loginType.equals("student"))
                parentIdList.add("0");
                parentNameList.add("Select Parent");
                JSONArray parentArray = jsonObject.getJSONArray("parent");
                for (int j=0;j<parentArray.length();j++){
                    JSONObject data = parentArray.getJSONObject(j);
                    String parentId = data.getString("parent_id");
                    String parentName = data.getString("parent_name");
                    parentIdList.add(parentId);
                    parentNameList.add(parentName);
                }

                System.out.println("LISTS : "+adminNameList+" : "+studentNameList+" : "+parentNameList);

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }


    }

    @Override
    public void requestFailed(String errorMessage, int requestTag) {
        if (progressDialog.isShowing())
            progressDialog.dismiss();

        if (requestTag == GET_USER_LIST_REQUEST){
            Toast.makeText(this, "Something went wrong", Toast.LENGTH_SHORT).show();
        }

        if (requestTag == SEND_MESSAGE_REQUEST_TAG){
            Toast.makeText(this, "Something went wrong", Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public void imageDownloaded(Bitmap image, int requestTag) {

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        try {
            if (resultCode == Activity.RESULT_OK) {
                if (requestCode == SELECTION_REQUEST_CODE) {
                    Uri uri = data.getData();

                    if (uri != null){
                        String path = PathUtil.getPath(MessageActivity.this,uri);
                        System.out.println("PATH : " + path);

                        fileName.setText(path);
                    }

                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    class SendMessage extends AsyncTask<Void,Void,Void>{

        int code;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            progressDialog.setMessage("Sending...");
            progressDialog.show();
        }


        @Override
        protected Void doInBackground(Void... voids) {


            ServerManager s = new ServerManager(MessageActivity.this);
            String api = s.sendMessageApi;

           // String api = "http://www.nexusjobz.com/school/index.php?mobile/send_new_message";
            System.out.println("URL : "+api);
            try {
                MultipartUtility multipartUtility= new MultipartUtility(api,"UTF-8");

                System.out.println("===="+file+":"+authKey+":"+loginType+":"+messageEditText.getText().toString()+":"+recieverId+":"+loginUserId);
                if (!TextUtils.isEmpty(fileName.getText().toString())) {
                    file = new File(fileName.getText().toString());
                    multipartUtility.addFilePart("file", file);
                }


                multipartUtility.addFormField("authentication_key", authKey);
                multipartUtility.addFormField("user_type", loginType);
                multipartUtility.addFormField("message", messageEditText.getText().toString());
                if (recieverNameTag == 1){
                    multipartUtility.addFormField("reciever", "teacher"+"-"+recieverId);
                }
                else if (recieverNameTag == 2){
                    multipartUtility.addFormField("reciever", "student"+"-"+recieverId);
                }
                else if (recieverNameTag == 3){
                    multipartUtility.addFormField("reciever", "parent"+"-"+recieverId);
                }
                else if (recieverNameTag == 4){
                    multipartUtility.addFormField("reciever", "admin"+"-"+recieverId);
                }

                multipartUtility.addFormField("login_id", loginUserId);



                List<String> response = multipartUtility.finish();

                for (String line : response) {
                    System.out.println("response============== "+line);
                    multipartResponse = line;
                }

                try {
                    JSONObject jsonObject = new JSONObject(multipartResponse);
                    JSONObject data = jsonObject.getJSONObject("response");
                    code = data.getInt("code");


                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            if (code == 200) {

                messageEditText.setText("");
                fileName.setText("");
                Toast.makeText(MessageActivity.this, "Successfully Sent", Toast.LENGTH_SHORT).show();
            }
            if (progressDialog.isShowing()){
                progressDialog.dismiss();
            }
        }
    }
}
