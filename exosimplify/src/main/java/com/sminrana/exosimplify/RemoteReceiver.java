package com.sminrana.exosimplify;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.view.KeyEvent;

/**
 * RemoteReceiver receives tap events from notification
 * It has only play and pause button
 */
public class RemoteReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            if (Intent.ACTION_MEDIA_BUTTON.equals(intent.getAction())) {
                final KeyEvent event = intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);

                if (event != null && event.getAction() == KeyEvent.ACTION_DOWN) {
                    switch (event.getKeyCode()) {
                        case KeyEvent.KEYCODE_MEDIA_PAUSE:
                            SinglePlayer.getInstance().player.pause();
                            break;
                        case KeyEvent.KEYCODE_MEDIA_PLAY:
                            SinglePlayer.getInstance().player.play();
                            break;
                    }
                }
            }

        } catch (Exception e) { /* ignore */ }
    }
}