package jp.techacademy.sugawara.shun.taskapp;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import io.realm.Realm;

public class TaskAlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context,Intent intent){
//                Log.d("TaskApp","onRecever");
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if(Build.VERSION.SDK_INT >= 26){
            NotificationChannel channel = new NotificationChannel(
                    "default","Channel name",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            channel.setDescription("Channel description");
            notificationManager.createNotificationChannel(channel);

        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context,"default");
        builder.setSmallIcon(R.drawable.small_icon);
        builder.setLargeIcon(BitmapFactory.decodeResource(context.getResources(),R.drawable.large_icon));
        builder.setWhen(System.currentTimeMillis());
        builder.setDefaults(Notification.DEFAULT_ALL);
        builder.setAutoCancel(true);

        int taskId = intent.getIntExtra(MainActivity.EXTRA_TASK,-1);
        Realm realm = Realm.getDefaultInstance();
        Task task = realm.where(Task.class).equalTo("id",taskId).findFirst();

        builder.setTicker(task.getTitle());
        builder.setContentTitle(task.getTitle());
        builder.setContentText(task.getContents());

        Intent startAppIntent = new Intent(context,MainActivity.class);
        startAppIntent.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
        PendingIntent pendingIntent =PendingIntent.getActivity(context,0,startAppIntent,0);
        builder.setContentIntent(pendingIntent);



        notificationManager.notify(task.getId(),builder.build());
        realm.close();






    }
}
