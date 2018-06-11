package com.creativeitem.ekattorschoolmanager;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MyProfileActivity extends ActionBarActivity implements ServerManager.ServerResponseHandler{
    private ServerManager serverManager;
    private String authKey;
    private String loginType;
    private String userId;
    private String name;
    private String email;
    private String imageUrl;
    private int profileImageSize;

    private final int GET_MY_PROFILE_REQUEST = 1000;
    private final int UPDATE_PROFILE_REQUEST = 1001;
    private final int UPDATE_PASSWORD_REQUEST = 1002;
    private final int UPDATE_IMAGE_REQUEST = 1003;
    private ProgressDialog progressDialog;
    private Button updateImageCancelButton;
    private Button updateImageDoneButton;
    private int RESULT_LOAD_IMAGE = 10000;
    private Bitmap newImage;
    private Bitmap currentImage;
    private ImageView profileImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_profile);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        initValues();
    }

    public void initValues() {
        serverManager = new ServerManager(this);
        SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences("EkattorSchoolManagerPrefs", 0);
        authKey = sharedPreferences.getString("AUTHKEY", "");
        loginType = sharedPreferences.getString("LOGINTYPE", "");
        userId = sharedPreferences.getString("USERID", "");
        name = "";
        email = "";
        imageUrl = "";
        progressDialog = new ProgressDialog(this);
        progressDialog.show();
        serverManager.getMyProfile(authKey, loginType, userId, GET_MY_PROFILE_REQUEST);
        Display display = getWindowManager().getDefaultDisplay();
        DisplayMetrics displayMetrics = new DisplayMetrics();
        display.getMetrics(displayMetrics);
        profileImageSize = displayMetrics.heightPixels/8;
        profileImageView = (ImageView)findViewById(R.id.my_profile_imageView);
        profileImageView.getLayoutParams().height = profileImageSize;
        profileImageView.getLayoutParams().width = profileImageSize;
        //updateImageCancelButton = (Button)findViewById(R.id.updateImage_cancel_button);
        //updateImageDoneButton = (Button)findViewById(R.id.updateImage_done_button);
    }

    public void manageProfileButtonAction(View view) {
        Button manageProfileButton = (Button)findViewById(R.id.manage_profile_button);
        manageProfileButton.setBackgroundColor(getResources().getColor(R.color.appColor));
        manageProfileButton.setTextColor(Color.WHITE);
        Button changePasswordButton = (Button)findViewById(R.id.change_password_button);
        changePasswordButton.setBackgroundColor(Color.WHITE);
        changePasswordButton.setTextColor(Color.rgb(69, 187, 247));
        LinearLayout manageProfileLayout = (LinearLayout) findViewById(R.id.manage_profile_layout);
        manageProfileLayout.setVisibility(View.VISIBLE);
        RelativeLayout changePasswordLayout = (RelativeLayout)findViewById(R.id.change_password_layout);
        changePasswordLayout.setVisibility(View.INVISIBLE);
    }

    public void changePasswordButtonAction(View view) {
        Button manageProfileButton = (Button)findViewById(R.id.manage_profile_button);
        manageProfileButton.setBackgroundColor(Color.WHITE);
        manageProfileButton.setTextColor(Color.rgb(69, 187, 247));
        Button changePasswordButton = (Button)findViewById(R.id.change_password_button);
        changePasswordButton.setBackgroundColor(getResources().getColor(R.color.appColor));
        changePasswordButton.setTextColor(Color.WHITE);
        LinearLayout manageProfileLayout = (LinearLayout) findViewById(R.id.manage_profile_layout);
        manageProfileLayout.setVisibility(View.INVISIBLE);
        RelativeLayout changePasswordLayout = (RelativeLayout)findViewById(R.id.change_password_layout);
        changePasswordLayout.setVisibility(View.VISIBLE);
    }

    public void profileUpdateButtonAction(View view) {
        EditText nameText = (EditText)findViewById(R.id.name_editText);
        EditText emailText = (EditText)findViewById(R.id.email_editText);
        String updatedName = nameText.getText().toString().trim();
        String updatedEmail = emailText.getText().toString().trim();
        if (updatedName.isEmpty()) {
            updatedName = name;
        }
        if (updatedEmail.isEmpty()) {
            updatedEmail = email;
        }
        progressDialog.show();
        serverManager.updateProfile(authKey, loginType, userId, updatedName, updatedEmail, UPDATE_PROFILE_REQUEST);
    }

    public void passwordUpdateButtonAction(View view) {
        EditText currentPasswordText = (EditText)findViewById(R.id.current_password_editText);
        String currentPassword = currentPasswordText.getText().toString().trim();
        if (currentPassword.isEmpty()) {
            currentPasswordText.setError("Enter current password");
            return;
        }
        EditText newPasswordText = (EditText)findViewById(R.id.new_password_editText);
        String newPassword = newPasswordText.getText().toString().trim();
        if (newPassword.isEmpty()) {
            newPasswordText.setError("Enter new password");
            return;
        }
        EditText confirmNewPasswordText = (EditText)findViewById(R.id.confirm_new_password_editText);
        String confirmNewPassword = confirmNewPasswordText.getText().toString().trim();
        if (confirmNewPassword.isEmpty()) {
            confirmNewPasswordText.setError("Re type new password");
            return;
        }
        if (!newPassword.equals(confirmNewPassword)) {
            confirmNewPasswordText.setError("Re type new password correctly");
            return;
        }
        progressDialog.show();
        serverManager.updatePassword(authKey, loginType, userId, newPassword, currentPassword, UPDATE_PASSWORD_REQUEST);
    }

    /*public void updateImageButtonAction(View view) {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image*//*");
        startActivityForResult(intent, RESULT_LOAD_IMAGE);
    }

    public void updateImageDoneAction(View view) {
        progressDialog.show();
//        serverManager.updateImage(authKey, loginType, userId, newImage, UPDATE_IMAGE_REQUEST);
        updateImageDoneButton.setVisibility(View.INVISIBLE);
        updateImageCancelButton.setVisibility(View.INVISIBLE);
    }

    public void updateImageCancelAction(View view) {
        profileImageView.setImageBitmap(currentImage);
        updateImageDoneButton.setVisibility(View.INVISIBLE);
        updateImageCancelButton.setVisibility(View.INVISIBLE);
    }*/

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && data != null) {
            Uri selectedImageUri = data.getData();
            String[] filePathColumn = {MediaStore.Images.Media.DATA};
            Cursor cursor = getContentResolver().query(selectedImageUri, filePathColumn, null, null, null);
            if (cursor != null && cursor.getCount() > 0) {
                cursor.moveToFirst();
                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                String newImagePath = cursor.getString(columnIndex);
                cursor.close();
                BitmapFactory.Options options = new BitmapFactory.Options();
                newImage = BitmapFactory.decodeFile(newImagePath, options);
                profileImageView.setImageBitmap(newImage);
                updateImageDoneButton.setVisibility(View.VISIBLE);
                updateImageCancelButton.setVisibility(View.VISIBLE);
            }
        }
    }

    public void showAlert(String message) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setMessage(message);
        alertDialogBuilder.setPositiveButton("Ok", null);
        AlertDialog alert = alertDialogBuilder.create();
        alert.show();
    }

    @Override
    public void requestFinished(String response, int requestTag) {
        if (progressDialog.isShowing())
            progressDialog.dismiss();
        boolean jsonError = false;
        try {
            if (requestTag == GET_MY_PROFILE_REQUEST) {
                JSONArray jsonArray = new JSONArray(response);
                for (int i=0;i<jsonArray.length();i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    name = jsonObject.optString("name");
                    email = jsonObject.optString("email");
                    imageUrl = jsonObject.optString("image_url");
                    EditText nameText = (EditText)findViewById(R.id.name_editText);
                    nameText.setText(name);
                    EditText emailText = (EditText)findViewById(R.id.email_editText);
                    emailText.setText(email);
                    serverManager.downloadImage(imageUrl, profileImageSize, 0);
                }
            }
            else if (requestTag == UPDATE_PROFILE_REQUEST) {
                JSONObject jsonObject = new JSONObject(response);
                String updateStatus = jsonObject.optString("update_status");
                String updateMessage;
                if (updateStatus.equals("success"))
                    updateMessage = "Profile updated successfully";
                else
                    updateMessage = "Profile update failed";
                showAlert(updateMessage);
            }
            else if (requestTag == UPDATE_PASSWORD_REQUEST) {
                JSONObject jsonObject = new JSONObject(response);
                String updateStatus = jsonObject.optString("update_status");
                String updateMessage;
                if (updateStatus.equals("success"))
                    updateMessage = "Password updated successfully";
                else
                    updateMessage = "Password update failed";
                showAlert(updateMessage);
            }
            else if (requestTag == UPDATE_IMAGE_REQUEST) {
                updateImageDoneButton.setVisibility(View.INVISIBLE);
                updateImageCancelButton.setVisibility(View.INVISIBLE);
                JSONObject jsonObject = new JSONObject(response);
                String status = jsonObject.optString("update_status");
                if (status.equals("success")) {
                    currentImage = newImage;
                    profileImageView.setImageBitmap(currentImage);
                    showAlert("Image updated successfully");
                }
                else {
                    showAlert("Image update failed");
                }
            }
        }
        catch (JSONException e) {
            jsonError = true;
        }
        if (jsonError)
            showAlert("An error occurred");
    }

    @Override
    public void requestFailed(String errorMessage, int requestTag) {
        if (progressDialog.isShowing())
            progressDialog.dismiss();
        if (requestTag == UPDATE_IMAGE_REQUEST) {
            updateImageDoneButton.setVisibility(View.INVISIBLE);
            updateImageCancelButton.setVisibility(View.INVISIBLE);
        }
        String message = "";
        if (requestTag == GET_MY_PROFILE_REQUEST)
            message = "Could not profile information";
        else if (requestTag == UPDATE_PROFILE_REQUEST)
            message = "Error in updating";
        else if (requestTag == UPDATE_IMAGE_REQUEST)
            message = "Error in image update";
        showAlert(message);
    }

    @Override
    public void imageDownloaded(Bitmap image, int requestTag) {
        currentImage = image;
        profileImageView.setImageBitmap(currentImage);
    }
}
