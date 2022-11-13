package com.sminrana.exosimplify;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import androidx.annotation.Nullable;

import com.sminrana.exosimplify.ui.ActivityControl;

/**
 * Service is being used to clean all the notifications when app gets killed
 */
public class NotificationService extends Service {
    private final Binder mBinder = new NotificationServiceBinder();
    private ActivityControl activity;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);

        activity.onAppKill();
        stopSelf();
    }

    public void setVideoActivity(ActivityControl videoActivity) {
        activity = videoActivity;
    }

    public class NotificationServiceBinder extends Binder {

        public NotificationService getService() {

            return NotificationService.this;
        }
    }
}