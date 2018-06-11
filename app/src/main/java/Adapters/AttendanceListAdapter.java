package Adapters;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.creativeitem.ekattorschoolmanager.DataTransferFromAdapterToActivity;
import com.creativeitem.ekattorschoolmanager.R;
import com.creativeitem.ekattorschoolmanager.ServerManager;

import java.util.ArrayList;
import java.util.List;

import DataModels.AttendanceInfo;

/**
 * Created by creativeitem on 12/29/15.
 * attendance list adapter
 */
public class AttendanceListAdapter extends BaseAdapter{

    DataTransferFromAdapterToActivity dataTransferFromAdapterToActivity;

    private int attendanceTag;
    private Context context;
    private List<AttendanceInfo> attendanceList;
    List<String> statusList;


    public AttendanceListAdapter(Activity context,DataTransferFromAdapterToActivity dataTransferFromAdapterToActivity, List<AttendanceInfo> attendanceList,int attendanceTag) {
        this.context = context;
        this.dataTransferFromAdapterToActivity = dataTransferFromAdapterToActivity;
        this.attendanceList = attendanceList;
        statusList = new ArrayList<>();
        this.attendanceTag = attendanceTag;
    }

    @Override
    public int getCount() {
        return attendanceList.size();

    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public Object getItem(int position) {
        return attendanceList.get(position);
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater layoutInflater = (LayoutInflater)this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(R.layout.attendance_list_item, parent, false);
        }

        TextView rollText = (TextView)convertView.findViewById(R.id.roll_textView);
        TextView studentNameText = (TextView)convertView.findViewById(R.id.studentName_textView);


        final Spinner attendanceSpinner = (Spinner)convertView.findViewById(R.id.attendance);
        String list[] = {"Undefined","Present","Absent"};
        ArrayAdapter adapter = new ArrayAdapter(context,R.layout.custom_spinner,list);
        attendanceSpinner.setAdapter(adapter);


        attendanceSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                    //attendanceList.add(new AttendanceInfo(studentId,attendanceList.get(position).getStudentRoll(),attendanceList.get(position).getStudentName(),String.valueOf(adapterView.getSelectedItemPosition())));
                    statusList.set(position,""+adapterView.getSelectedItemPosition());
                    dataTransferFromAdapterToActivity.setValues(statusList);

                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {

                }
            });


        rollText.setText(attendanceList.get(position).getStudentRoll());
        studentNameText.setText(attendanceList.get(position).getStudentName());
        String status = attendanceList.get(position).getStatus();



        //   PASSING STATUS TO ACTIVITY   ///
        for (int j=0;j<1;j++){
            statusList.add(""+attendanceSpinner.getSelectedItemPosition());
            dataTransferFromAdapterToActivity.setValues(statusList);

        }
        //--------------------//

        if (attendanceTag ==1){
            attendanceSpinner.setSelection(1);

        }else {
            switch (status) {
                case "0":
                    attendanceSpinner.setSelection(0);
                    break;
                case "1":
                    attendanceSpinner.setSelection(1);
                    break;
                case "2":
                    attendanceSpinner.setSelection(2);
                    break;
                default:
                    break;

            }
        }





        return convertView;
    }

}
