package DataModels;

/**
 * Created by Aman on 2/15/2018.
 */

public class NotificationModel {
    private String title;
    private String notification;
    private String date;
    private String dueAmount;
    public NotificationModel(String title, String notification, String date) {
        this.title = title;
        this.notification =notification;
        this.dueAmount = dueAmount;
        this.date = date;
    }

    public String getTitle() {
        return title;
    }

    public String getNotification() {
        return notification;
    }

    public String getDate() {
        return date;
    }
}
