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
import android.widget.Button;
import android.widget.ImageView;
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
import java.util.List;

import Adapters.AccountingListAdapter;
import Adapters.SelectionListAdapter;
import DataModels.AccountingInfo;

public class AccountingActivity extends ActionBarActivity implements ServerManager.ServerResponseHandler{
    private String[] months = {"Select month", "January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"};
    private List<String> years;
    private int selectedMonth;
    private int selectedYear;
    private ServerManager serverManager;
    private String authKey;
    private String loginType;
    private List<AccountingInfo> accountingInfoList;
    private AccountingListAdapter accountingListAdapter;
    private ProgressDialog progressDialog;
    private final int INCOME_INFO_REQUEST = 1000;
    private final int EXPENSE_INFO_REQUEST = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accounting);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        initValues();
    }

    // initialize values and settings
    public void initValues() {
        setMonthSpinner();
        setYearSpinner();
        serverManager = new ServerManager(this);
        SharedPreferences sharedPreferences = getSharedPreferences("EkattorSchoolManagerPrefs", 0);
        authKey = sharedPreferences.getString("AUTHKEY", "");
        loginType = sharedPreferences.getString("LOGINTYPE", "");
        accountingInfoList = new ArrayList<>();
        accountingListAdapter = new AccountingListAdapter(this, accountingInfoList);
        ListView accountingListView = (ListView)findViewById(R.id.accounting_listView);
        accountingListView.setAdapter(accountingListAdapter);
        progressDialog = new ProgressDialog(this);

        LinearLayout back = (LinearLayout) findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
    }

    // configuring month spinner
    private void setMonthSpinner() {
        selectedMonth = 0;
        ArrayList<String> monthList = new ArrayList<>(Arrays.asList(months));
        final SelectionListAdapter monthSpinnerAdapter = new SelectionListAdapter(this, monthList);
//        Spinner monthSpinner = (Spinner)findViewById(R.id.accounting_month_spinner);
//        monthSpinner.setAdapter(monthSpinnerAdapter);
//        monthSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//            @Override
//            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//                selectedMonth = position;
//            }
//
//            @Override
//            public void onNothingSelected(AdapterView<?> parent) {
//
//            }
//        });

        //Custom Month Spinner
        LinearLayout monthListCustomSpinner = (LinearLayout)findViewById(R.id.month_custom_list_spinner);
        monthListCustomSpinner.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(AccountingActivity.this);
                LayoutInflater inflater = AccountingActivity.this.getLayoutInflater();
                final View dialogView = inflater.inflate(R.layout.custom_spinner_dialog, null);
                dialogBuilder.setView(dialogView);

                ListView month_list = (ListView)dialogView.findViewById(R.id.custom_list_spinner_dialog);
                month_list.setAdapter(monthSpinnerAdapter);

                final AlertDialog b = dialogBuilder.create();
                b.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                b.show();

                month_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                        selectedMonth = position;

                        TextView monthText = (TextView)findViewById(R.id.month);
                        String month = String.valueOf(adapterView.getItemAtPosition(position));
                        monthText.setText(month);

                        b.cancel();
                    }
                });
            }
        });

    }

    // configuring year spinner
    private void setYearSpinner() {
        selectedYear = 0;
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        years = new ArrayList<>();
        years.add("Select year");
        years.add(String.valueOf(year-2));
        years.add(String.valueOf(year-1));
        years.add(String.valueOf(year));

//        Spinner yearSpinner = (Spinner)findViewById(R.id.accounting_year_spinner);
//        yearSpinner.setAdapter(yearSpinnerAdapter);
//        yearSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//            @Override
//            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//                selectedYear = position;
//            }
//
//            @Override
//            public void onNothingSelected(AdapterView<?> parent) {
//
//            }
//        });

        //Custom Year Spinner
        LinearLayout yearListCustomSpinner = (LinearLayout)findViewById(R.id.year_custom_list_spinner);
        yearListCustomSpinner.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(AccountingActivity.this);
                LayoutInflater inflater = AccountingActivity.this.getLayoutInflater();
                final View dialogView = inflater.inflate(R.layout.custom_spinner_dialog, null);
                dialogBuilder.setView(dialogView);

                SelectionListAdapter yearSpinnerAdapter = new SelectionListAdapter(AccountingActivity.this, years);
                ListView year_list = (ListView)dialogView.findViewById(R.id.custom_list_spinner_dialog);
                year_list.setAdapter(yearSpinnerAdapter);

                final AlertDialog b = dialogBuilder.create();
                b.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                b.show();

                year_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                        selectedYear = position;

                        TextView yearText = (TextView)findViewById(R.id.year);
                        String year = String.valueOf(adapterView.getItemAtPosition(position));
                        yearText.setText(year);

                        b.cancel();
                    }
                });
            }
        });
    }

    // on press shows income information of the selected month
    public void incomeButtonAction(View view) {
        Button incomeButton = (Button)findViewById(R.id.income_button);
        incomeButton.setBackgroundColor(getResources().getColor(R.color.appColor));
        incomeButton.setTextColor(Color.WHITE);
        Button expenseButton = (Button)findViewById(R.id.expense_button);
        expenseButton.setBackgroundColor(Color.WHITE);
        expenseButton.setTextColor(Color.rgb(69, 187, 247));
        if (selectedMonth>0 && selectedYear>0) {
            progressDialog.show();
            serverManager.getAccountingInfo(authKey, loginType, selectedMonth, years.get(selectedYear), "income", INCOME_INFO_REQUEST);
        }
    }

    // on press shows expense information of selected month
    public void expenseButtonAction(View view) {
        Button expenseButton = (Button)findViewById(R.id.expense_button);
        expenseButton.setBackgroundColor(getResources().getColor(R.color.appColor));
        expenseButton.setTextColor(Color.WHITE);
        Button incomeButton = (Button)findViewById(R.id.income_button);
        incomeButton.setBackgroundColor(Color.WHITE);
        incomeButton.setTextColor(Color.rgb(69, 187, 247));
        if (selectedMonth>0 && selectedYear>0) {
            progressDialog.show();
            serverManager.getAccountingInfo(authKey, loginType, selectedMonth, years.get(selectedYear), "expense", EXPENSE_INFO_REQUEST);
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

    // server response methods
    @Override
    public void requestFinished(String response, int requestTag) {
        if (progressDialog.isShowing())
            progressDialog.dismiss();
        boolean jsonError = false;
        try {
            JSONArray jsonArray = new JSONArray(response);
            accountingInfoList.clear();
            accountingInfoList.add(new AccountingInfo("Title", "Amount", "", "Date"));
            for (int i=0;i<jsonArray.length();i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                String title = jsonObject.optString("title");
                String amount = jsonObject.optString("amount");
                String timeStamp = jsonObject.optString("timestamp");
                accountingInfoList.add(new AccountingInfo(title, amount, "", timeStamp));
            }
            accountingListAdapter.notifyDataSetChanged();
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
        String alertMessage = "";
        if (requestTag == INCOME_INFO_REQUEST)
            alertMessage = "Could not load income information";
        else if (requestTag == EXPENSE_INFO_REQUEST)
            alertMessage = "Could not load expense information";
        showAlert(alertMessage);
    }

    @Override
    public void imageDownloaded(Bitmap image, int requestTag) {

    }
}
