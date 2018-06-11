package DataModels;

import com.creativeitem.ekattorschoolmanager.ServerManager;

/**
 * Created by creativeitem on 12/29/15.
 * stores attendance information of a student. student roll, student name and attendance status
 */
public class AttendanceInfo {
    private String studentId;
    private String attendanceId;
    private String studentRoll;
    private String studentName;
    private String status;
    public AttendanceInfo(String studentId,String attendanceId,String studentRoll, String studentName, String status) {
        this.studentId = studentId;
        this.attendanceId = attendanceId;
        this.studentRoll = studentRoll;
        this.studentName = studentName;
        this.status = status;
    }

    public String getStudentId() {
        return studentId;
    }

    public String getAttendanceId() {
        return attendanceId;
    }

    public String getStudentRoll() {
        return studentRoll;
    }

    public String getStudentName() {
        return studentName;
    }

    public String getStatus() {
        return status;
    }
}
