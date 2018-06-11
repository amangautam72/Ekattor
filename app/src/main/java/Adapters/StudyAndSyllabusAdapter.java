package Adapters;

import android.Manifest;
import android.app.Activity;
import android.app.DownloadManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.creativeitem.ekattorschoolmanager.R;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.security.Permission;
import java.util.List;

import DataModels.StudyAndSyllabusModel;

import static android.content.ContentValues.TAG;

/**
 * Created by Aman on 3/9/2018.
 */

public class StudyAndSyllabusAdapter extends BaseAdapter {

    private Context context;
    private List<StudyAndSyllabusModel> List;
    ProgressDialog pDialog;
    String[] extension   = { ".txt", ".jpg", ".jpeg", ".mpeg",".pdf",".DOCX",".html",".java",".png",".exif",".tiff",".gif",".bmp",".ppm",".webp",".svg",".cgm",".log",".msg",".pages",".rtf",".tex",".wpd",".wps" };
    String encodedUrl;

    public StudyAndSyllabusAdapter(Context context, List<StudyAndSyllabusModel> List) {
        this.context = context;
        this.List = List;

        pDialog = new ProgressDialog(context);
    }

    @Override
    public int getCount() {
        return List.size();
    }

    @Override
    public Object getItem(int position) {
        return List.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater layoutInflater = (LayoutInflater)this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(R.layout.study_material_and_syllabus_item_list, parent, false);
        }

        TextView titleText = (TextView)convertView.findViewById(R.id.title);
        titleText.setText(List.get(position).getTitle());
        TextView descriptionText = (TextView)convertView.findViewById(R.id.description);
        descriptionText.setText(List.get(position).getDescription());
        TextView subjectText = (TextView)convertView.findViewById(R.id.subject);
        subjectText.setText(List.get(position).getSubject());
        TextView uploadedOn = (TextView)convertView.findViewById(R.id.uploaded_on);
        uploadedOn.setText("Uploaded on "+List.get(position).getDate());
        TextView downloadFile = (TextView)convertView.findViewById(R.id.download_file);
        downloadFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String url = List.get(position).getFileUrl();
                encodedUrl = url.replaceAll(" ","%20");


                System.out.println("URL : "+List.get(position).getFileUrl());
                System.out.println("ENCODED URL : "+encodedUrl);
                new DownloadFileFromURL().execute(encodedUrl);

            }
        });

        return convertView;
    }

    /**
     * Background Async Task to download file
     * */
    class DownloadFileFromURL extends AsyncTask<String, String, String> {

        /**
         * Before starting background thread Show Progress Bar Dialog
         * */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog.setMax(100);
            pDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            pDialog.show();
        }

        /**
         * Downloading file in background thread
         * */
        @Override
        protected String doInBackground(String... f_url) {
            String fileUrl=f_url[0].toString();
            String fileName = fileUrl.substring(fileUrl.lastIndexOf("/")+1);
            System.out.println("FILE : "+fileName.substring(fileName.lastIndexOf("/")+1));
            int count;
            OutputStream output = null;
            try {
                URL url = new URL(f_url[0]);

                URLConnection conection = url.openConnection();
                conection.connect();


//                making new folder to save file into it
//                File direct=new File(Environment.getExternalStorageDirectory().getAbsolutePath()+"/SMS");
//                if(!direct.exists()){
//                    direct.mkdir();
//                }
                // this will be useful so that you can show a tipical 0-100%
                // progress bar
                int lenghtOfFile = conection.getContentLength();

                // download the file
                InputStream input = new BufferedInputStream(url.openStream(),
                        8192);


                for(int i =0; i<extension.length;i++){
                    String s = extension[i];
//                    int j = i+1;


                    if(encodedUrl.endsWith(s)) {
                        output = new FileOutputStream("/sdcard/Download/"+fileName);
                    }

                }
                byte data[] = new byte[1024];

                long total = 0;

                while ((count = input.read(data)) != -1) {
                    total += count;
                    // publishing the progress....
                    // After this onProgressUpdate will be called
                    publishProgress("" + (int) ((total * 100) / lenghtOfFile));

                    // writing data to file
                    output.write(data, 0, count);


                }

                File k = new File(Environment.getExternalStorageDirectory().getPath()+"/download/"+fileName);
                if (output != null) {
                    String extension = fileName.substring(fileName.lastIndexOf(".") );
                    System.out.println("EXTENSION : "+extension);


                    // String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(MimeTypeMap.getFileExtensionFromUrl(fileName));
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.fromFile(k));
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

                    if (extension.equals(".jpg") || extension.equals(".jpeg") || extension.equals(".png")){
                        intent.setType("image/*");
                    }

                    PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);


                    NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context)
                            .setSmallIcon(R.mipmap.ic_launcher)
                            .setContentTitle(fileName)
                            .setContentText("Download Completed")
                            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                            .setContentIntent(pendingIntent)
                            .setAutoCancel(true);


                    NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);

                    // notificationId is a unique int for each notification that you must define
                    notificationManager.notify(1, mBuilder.build());
                }else{
                    NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context)
                            .setSmallIcon(R.mipmap.ic_launcher)
                            .setContentTitle(fileName)
                            .setContentText("Download Failed")
                            .setPriority(NotificationCompat.PRIORITY_DEFAULT);


                    NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);

                    // notificationId is a unique int for each notification that you must define
                    notificationManager.notify(1, mBuilder.build());
                }


                // flushing output
                output.flush();

                // closing streams
                output.close();
                input.close();

            } catch (Exception e) {
                Log.e("Error: ", e.getMessage());
            }

            return null;
        }

        /**
         * Updating progress bar
         * */
        protected void onProgressUpdate(String... progress) {
            // setting progress percentage

            pDialog.setProgress(Integer.parseInt(progress[0]));



        }

        /**
         * After completing background task Dismiss the progress dialog
         * **/
        @Override
        protected void onPostExecute(String file_url) {
            // dismiss the dialog after the file was downloaded

            if (pDialog.isShowing()){
                pDialog.dismiss();
            }

//            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context)
//                    .setSmallIcon(R.mipmap.ic_launcher)
//                    .setContentTitle("File")
//                    .setContentText("Download Completed")
//                    .setPriority(NotificationCompat.PRIORITY_DEFAULT);
//
//
//            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
//
//            // notificationId is a unique int for each notification that you must define
//            notificationManager.notify(1, mBuilder.build());


        }

    }

}
