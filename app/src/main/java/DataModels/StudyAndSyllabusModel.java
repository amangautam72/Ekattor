package DataModels;

/**
 * Created by Aman on 3/9/2018.
 */

public class StudyAndSyllabusModel {

    private String title;
    private String description;
    private String date;
    private String subject;
    private String fileUrl;

    public StudyAndSyllabusModel(String title, String description, String date, String subject,String fileUrl) {
        this.title = title;
        this.description = description;
        this.date = date;
        this.subject = subject;
        this.fileUrl = fileUrl;
    }


    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getDate() {
        return date;
    }

    public String getSubject() {
        return subject;
    }

    public String getFileUrl() {
        return fileUrl;
    }
}
