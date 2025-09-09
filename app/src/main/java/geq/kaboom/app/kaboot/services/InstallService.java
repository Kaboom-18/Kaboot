package geq.kaboom.app.kaboot.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import geq.kaboom.app.kaboot.R;
import geq.kaboom.app.kaboot.utils.Config;
import geq.kaboom.app.kaboot.utils.KabUtil;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class InstallService extends Service {

    public static final String EXTRA_NAME = "extra_name";
    public static final String EXTRA_URL = "extra_url";

    private static final String CHANNEL_ID = "install_channel";
    private KabUtil util;
    private static boolean running = false;
    private volatile boolean canceled = false;
    private String name;
    private String url;

    @Override
    public void onCreate() {
        super.onCreate();
        util = new KabUtil(this);
        createNotificationChannel();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null) return START_NOT_STICKY;
        name = intent.getStringExtra(EXTRA_NAME);
        url = intent.getStringExtra(EXTRA_URL);

        if ("ACTION_CANCEL".equals(intent.getAction())) {
            canceled = true;
            util.toast(String.format("%s installation canceled!", name));
            util.deleteFile(Config.getPkgDir(this, name));
            util.deleteFile(Config.getPkgTmpDir(this, name));
            util.deleteFile(new File(getCacheDir(), util.getLastPath(url)).getAbsolutePath());
            running = false;
            stopSelf();
            return START_NOT_STICKY;
        }

        startForeground(1, buildNotification("Kaboot Installer", "Installing " + name + "...", 0, 100));
        new Thread(() -> runInstaller(name, url)).start();
        return START_STICKY;
    }

    private void runInstaller(String name, String url) {
        running = true;
        canceled = false;
        InputStream input = null;
        FileOutputStream output = null;
        HttpURLConnection connection = null;

        try {
            String downloadPath = Config.getTmpDir(this);
            String pkgPath = Config.getPkgDir(this, name);
            String pkgExtPath = Config.getPkgDir(this, "."+name);

            if (util.isExistFile(pkgPath)) {
                util.toast("Package already exists: " + name);
                running = false;
                stopSelf();
                return;
            }

            util.toast("Download started in background!");

            URL link = new URL(url);
            connection = (HttpURLConnection) link.openConnection();
            connection.connect();

            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                throw new RuntimeException("Download failed!");
            }

            int fileLength = connection.getContentLength();
            File archiveFile = new File(downloadPath, util.getLastPath(url));
            input = new BufferedInputStream(connection.getInputStream());
            output = new FileOutputStream(archiveFile);

            byte[] buffer = new byte[4096];
            long total = 0;
            int count;
            int lastProgress = 0;

            while ((count = input.read(buffer)) != -1 && !canceled) {
                total += count;
                output.write(buffer, 0, count);
                int progress = (int) (total * 100 / fileLength);
                if (progress != lastProgress) {
                    updateNotification("Kaboot Downloader", String.format("Downloading %s!", name), progress, 100);
                    lastProgress = progress;
                }
            }

            if (canceled) return;

            output.close();
            input.close();
            
            util.makeDir(pkgExtPath);

            ArrayList<String> command = new ArrayList<>();
            command.add(Config.getKaboot(this));
            command.add("-l");
            command.add("tar");
            command.add("-xvzf");
            command.add(archiveFile.getAbsolutePath());
            command.add("-C");
            command.add(pkgExtPath);

            ProcessBuilder pb = new ProcessBuilder(command);
            for (String va : Config.getKabootVars(this)) {
                String[] ar = va.split("=", 2);
                pb.environment().put(ar[0], ar[1]);
            }

            Process proc = pb.start();
            proc.getErrorStream().close();

            BufferedReader reader = new BufferedReader(new InputStreamReader(proc.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null && !canceled) {
                updateNotification("Kaboom Extractor", line, 0, 100);
            }
            proc.waitFor();

            if (canceled) return;

            util.deleteFile(archiveFile.getAbsolutePath());
            if(!util.renameFolder(pkgExtPath, pkgPath)){
                throw new Exception("Couldn't rename pkgDir");
            }
            
            Config.refreshList = true;
            util.toast(String.format("%s installed successfully!", name));

        } catch (Exception e) {
            util.toast(String.format("%s installation failed!", name));
        } finally {
            try {
                if (output != null) output.close();
                if (input != null) input.close();
            } catch (Exception ignored) {}
            if (connection != null) connection.disconnect();
            stopSelf();
            running = false;
        }
    }

    private void updateNotification(String title, String message, int progress, int max) {
        Notification notif = buildNotification(title, message, progress, max);
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(1, notif);
    }

    private Notification buildNotification(String title, String message, int progress, int max) {
        Intent cancel = new Intent(this, InstallService.class);
        cancel.setAction("ACTION_CANCEL");
        cancel.putExtra(EXTRA_NAME, name);
        cancel.putExtra(EXTRA_URL, url);

        PendingIntent cancelIntent = PendingIntent.getService(
                this, name.hashCode(), cancel, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
                .setContentTitle(title)
                .addAction(R.drawable.ic_terminate, "Cancel", cancelIntent)
                .setContentText(message)
                .setSmallIcon(android.R.drawable.stat_sys_download)
                .setOngoing(true)
                .setOnlyAlertOnce(true);

        if (progress > 0 && max > 0) {
            builder.setProgress(max, progress, false);
        } else {
            builder.setProgress(0, 0, false);
        }

        return builder.build();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel chan = new NotificationChannel(
                    CHANNEL_ID, "Install Service", NotificationManager.IMPORTANCE_DEFAULT);
            chan.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(chan);
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public static boolean isRunning() {
        return running;
    }
}