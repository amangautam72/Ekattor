package Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.creativeitem.ekattorschoolmanager.R;

import java.util.List;

import DataModels.StudyAndSyllabusModel;

/**
 * Created by Aman on 3/9/2018.
 */

public class AcademicSyllabusAdapter extends BaseAdapter {

    private Context context;
    private List<StudyAndSyllabusModel> academicSyllabusList;
    public AcademicSyllabusAdapter(Context context, List<StudyAndSyllabusModel> academicSyllabusListt) {
        this.context = context;
        this.academicSyllabusList = academicSyllabusList;
    }
    @Override
    public int getCount() {
        return 1;
    }

    @Override
    public Object getItem(int i) {
        return 0;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int i, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater layoutInflater = (LayoutInflater)this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(R.layout.study_material_and_syllabus_item_list, parent, false);
        }

        return convertView;
    }
}
