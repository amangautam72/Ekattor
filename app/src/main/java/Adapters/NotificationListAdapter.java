package Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.creativeitem.ekattorschoolmanager.R;

import java.util.List;

import DataModels.NotificationModel;

/**
 * Created by Aman on 2/15/2018.
 */

public class NotificationListAdapter extends BaseAdapter {

    private Context context;
    private List<NotificationModel> notificationList;
    public NotificationListAdapter(Context context, List<NotificationModel> notificationList) {
        this.context = context;
        this.notificationList = notificationList;
    }

    @Override
    public int getCount() {
        return notificationList.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public Object getItem(int position) {
        return notificationList.get(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater layoutInflater = (LayoutInflater)this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(R.layout.notification_list_item, parent, false);
        }

        TextView titleText = (TextView)convertView.findViewById(R.id.notification_title);
        titleText.setText(notificationList.get(position).getTitle());
        TextView notificationText = (TextView)convertView.findViewById(R.id.notifications);
        notificationText.setText(notificationList.get(position).getNotification());
        TextView dateText = (TextView)convertView.findViewById(R.id.notification_date);
        dateText.setText(":"+notificationList.get(position).getDate());

        return convertView;
    }
}
