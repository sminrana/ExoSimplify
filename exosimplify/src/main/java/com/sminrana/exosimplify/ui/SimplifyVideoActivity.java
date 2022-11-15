package com.sminrana.exosimplify.ui;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.provider.Settings;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.webkit.URLUtil;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.PlaybackException;
import com.google.android.exoplayer2.analytics.AnalyticsListener;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSource;
import com.sminrana.exosimplify.NotificationBuilder;
import com.sminrana.exosimplify.NotificationService;
import com.sminrana.exosimplify.R;
import com.sminrana.exosimplify.RemoteReceiver;
import com.sminrana.exosimplify.SinglePlayer;
import com.sminrana.exosimplify.player.PlayerControl;
import com.sminrana.exosimplify.player.PlayerState;
import com.sminrana.exosimplify.player.PlayerView;

public abstract class SimplifyVideoActivity extends AppCompatActivity implements ActivityControl, PlayerControl {
    private SinglePlayer singlePlayer = SinglePlayer.getInstance();
    private static final float PLAYBACK_RATE = 1.0f;
    private static long playerCurrentPosition = 0;
    private static String actionBarColor = "";
    private static String title = "";
    private static String url = "";
    
    private LinearLayout llContainer;
    private PlayerView videoView;
    private SimplifyVideoActivity activity;
    private ImageView fullScreenButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_exo);
        activity = this;

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            if (extras.getString("actionBarColor") != null) {
                actionBarColor = extras.getString("actionBarColor");
            }

            title = extras.getString("title");
            url = extras.getString("url");
        }

        if (actionBarColor.isEmpty()) {
            actionBarColor = "#000000";
        }

        ColorDrawable bgColor =  new ColorDrawable(Color.parseColor(actionBarColor));
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(title);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setBackgroundDrawable(bgColor);

        llContainer = findViewById(R.id.ll_video_view_container);
        videoView =  findViewById(R.id.styledPlayerView);

        fullScreenButton = videoView.findViewById(R.id.exo_fullscreen_icon);
        fullScreenButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleOrientation();
            }
        });

        ImageView playButton = videoView.findViewById(R.id.exo_play);
        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                play();
            }
        });

        ImageView pauseButton = videoView.findViewById(R.id.exo_pause);
        pauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pause();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {
        super.onStart();

        boolean fromNotification = getIntent().getBooleanExtra("from_notification", false);
        initializePlayer(fromNotification);
    }

    @Override
    protected void onPause() {
        super.onPause();
        this.finish();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {

            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.hide();
            }

            fullScreenButton.setImageResource(R.drawable.ic_fullscreen_close);

            llContainer.setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {

            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.show();
            }

            fullScreenButton.setImageResource(R.drawable.ic_fullscreen_open);

            llContainer.setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
        }
    }

    private void toggleOrientation() {
        int orientation = getResources().getConfiguration().orientation;
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }

        if (Settings.System.getInt(getContentResolver(),
                Settings.System.ACCELEROMETER_ROTATION, 0) == 1) {
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
                }
            }, 2000);
        }
    }

    /**
     * ---------------------------------------------------------------------------------------------
     * Exo singlePlayer.player
     * ---------------------------------------------------------------------------------------------
     */
    private void initializePlayer(boolean fromNotification) {
        playerCurrentPosition = 0;

        if (fromNotification) {
            videoView.setPlayer(singlePlayer.player);
        } else {
            if (singlePlayer.player != null) {
                if (singlePlayer.player.isPlaying()) {
                    singlePlayer.player.stop();
                    singlePlayer.player.clearMediaItems();
                    cleanPlayerNotification();
                }
            }

            if (singlePlayer.player == null) {
                singlePlayer.player = new ExoPlayer.Builder(getApplicationContext()).build();
                singlePlayer.player.addAnalyticsListener(new PlayerAnalyticsEventsListener());
            }

            videoView.setPlayer(singlePlayer.player);

            updateMediaSource();

            //singlePlayer.player.setRepeatMode(singlePlayer.player.REPEAT_MODE_ONE);
            singlePlayer.player.prepare();
            singlePlayer.player.setPlayWhenReady(true);

            bindNotificationService();
        }
    }

    private void updateMediaSource() {
        DataSource.Factory dataSourceFactory = new DefaultDataSource.Factory(getApplicationContext());

        /* This is the MediaSource representing the media to be played. */
        MediaSource videoSource;

        /*
         * Check for HLS playlist file extension ( .m3u8 or .m3u )
         * https://tools.ietf.org/html/rfc8216
         */
        if(url.contains(".m3u8") || url.contains(".m3u")) {
            videoSource = new HlsMediaSource.Factory(dataSourceFactory).createMediaSource(MediaItem.fromUri(Uri.parse(url)));
        } else {
            videoSource = new ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(MediaItem.fromUri(Uri.parse(url)));
        }

        singlePlayer.player.setMediaSource(videoSource);
    }

    @Override
    public void onAppKill() {
        unBindNotificationService();
        cleanPlayerNotification();
    }

    @Override
    public void ended() {

    }

    @Override
    public void pause() {
        if (singlePlayer.player != null && singlePlayer.player.isPlaying()) {
            singlePlayer.player.setPlayWhenReady(false);
        }
    }

    @Override
    public void play() {
        if (singlePlayer.player != null && !singlePlayer.player.isPlaying()) {
            singlePlayer.player.setPlayWhenReady(true);
        }
    }

    @Override
    public void seekTo(long position) {
        if (singlePlayer.player != null) {
            singlePlayer.player.seekTo(position);
        }
    }

    @Override
    public void error(String error) {

    }

    class PlayerAnalyticsEventsListener implements AnalyticsListener {

        @Override
        public void onPlaybackStateChanged(EventTime eventTime, int playbackState) {
            if (playbackState == singlePlayer.player.STATE_READY) {
                try {
                    playerCurrentPosition = singlePlayer.player.getDuration();

                    // Set seekbar position
                    setupMediaSession();
                } catch (Exception e) {
                    e.printStackTrace();
                }

            } else if (playbackState == singlePlayer.player.STATE_ENDED) {
                try {
                    updatePlaybackState(PlayerState.COMPLETE);
                    ended();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        public void onIsPlayingChanged(EventTime eventTime, boolean isPlaying) {
            if (isPlaying) {
                updatePlaybackState(PlayerState.PLAYING);
            } else {
                updatePlaybackState(PlayerState.PAUSED);
            }
        }

        @Override
        public void onPlayerError(EventTime eventTime, PlaybackException error) {
            if (error != null) {
                error(error.getErrorCodeName());
            }
        }
    }

    /**
     * ---------------------------------------------------------------------------------------------
     * Notification and MediaSession
     * ---------------------------------------------------------------------------------------------
     */
    private MediaSessionCompat mMediaSessionCompat;
    public static final String mNotificationChannelId = "NotificationBarController";
    private static final int NOTIFICATION_ID = 0;

    private final class MediaSessionCallback extends MediaSessionCompat.Callback {

        @Override
        public void onPause() {
            pause();
        }

        @Override
        public void onPlay() {
            play();
        }

        @Override
        public void onSeekTo(long pos) {
            seekTo(pos);
        }

        @Override
        public void onStop() {
            pause();
        }
    }

    private void setupMediaSession() {
        if (mMediaSessionCompat == null) {
            ComponentName receiver = new ComponentName(this.getPackageName(),
                    RemoteReceiver.class.getName());

            mMediaSessionCompat = new MediaSessionCompat(this, "mm_player", receiver, null);
            mMediaSessionCompat.setCallback(new MediaSessionCallback());
            mMediaSessionCompat.setActive(true);
        }

        // Update video title on the notification panel
        MediaMetadataCompat metadata = new MediaMetadataCompat.Builder()
                .putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_TITLE, title)
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, title)
                .putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_SUBTITLE, "")
                .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, playerCurrentPosition)
                .build();

        if (mMediaSessionCompat != null) {
            mMediaSessionCompat.setMetadata(metadata);
        }

        updatePlaybackState(PlayerState.PLAYING);
    }

    private PlaybackStateCompat.Builder getPlaybackStateBuilder() {
        PlaybackStateCompat playbackState = mMediaSessionCompat.getController().getPlaybackState();

        return playbackState == null
                ? new PlaybackStateCompat.Builder()
                : new PlaybackStateCompat.Builder(playbackState);
    }

    private void updatePlaybackState(PlayerState playerState) {
        if (mMediaSessionCompat == null) return;

        PlaybackStateCompat.Builder newPlaybackState = getPlaybackStateBuilder();

        long capabilities = getCapabilities(playerState);
        newPlaybackState.setActions(capabilities);

        int playbackStateCompat = PlaybackStateCompat.STATE_NONE;
        switch (playerState) {
            case PLAYING:
                playbackStateCompat = PlaybackStateCompat.STATE_PLAYING;
                break;
            case PAUSED:
                playbackStateCompat = PlaybackStateCompat.STATE_PAUSED;
                break;
            case BUFFERING:
                playbackStateCompat = PlaybackStateCompat.STATE_BUFFERING;
                break;
            case IDLE:
                playbackStateCompat = PlaybackStateCompat.STATE_STOPPED;
                break;
        }

        newPlaybackState.setState(playbackStateCompat, singlePlayer.player.getCurrentPosition(), PLAYBACK_RATE);
        mMediaSessionCompat.setPlaybackState(newPlaybackState.build());

        updateNotification(capabilities);
    }

    private NotificationChannel newChannel;

    private void updateNotification(long capabilities) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel();
        }

        NotificationCompat.Builder notificationBuilder = NotificationBuilder.from(
                this, getApplicationContext(), mMediaSessionCompat, mNotificationChannelId);

        if ((capabilities & PlaybackStateCompat.ACTION_PAUSE) != 0) {
            notificationBuilder.addAction(R.drawable.ic_pause, "Pause",
                    NotificationBuilder.getActionIntent(getApplicationContext(), KeyEvent.KEYCODE_MEDIA_PAUSE));
        }

        if ((capabilities & PlaybackStateCompat.ACTION_PLAY) != 0) {
            notificationBuilder.addAction(R.drawable.ic_play, "Play",
                    NotificationBuilder.getActionIntent(getApplicationContext(), KeyEvent.KEYCODE_MEDIA_PLAY));
        }

        NotificationManager notificationManager = getNotificationManager();
        if (notificationManager != null) {
            notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build());
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private void createNotificationChannel() {
        CharSequence channelNameDisplayedToUser = "Creation Kit Notification";
        int importance = NotificationManager.IMPORTANCE_LOW;

        if (newChannel == null) {
            newChannel = new NotificationChannel(
                    mNotificationChannelId, channelNameDisplayedToUser, importance);
            newChannel.setDescription("Creation Kit Notification");
            newChannel.setShowBadge(false);
            newChannel.setLockscreenVisibility(android.app.Notification.VISIBILITY_PUBLIC);
        }

        NotificationManager notificationManager = getNotificationManager();
        if (newChannel != null) {
            notificationManager.createNotificationChannel(newChannel);
        }
    }

    private void cleanPlayerNotification() {
        NotificationManager notificationManager = getNotificationManager();

        if (notificationManager != null) {
            notificationManager.cancel(NOTIFICATION_ID);
        }
    }

    private NotificationManager getNotificationManager() {
        return (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
    }

    private long getCapabilities(PlayerState playerState) {
        long capabilities = 0;

        switch (playerState) {
            case PLAYING:
            case BUFFERING:
                capabilities |= PlaybackStateCompat.ACTION_PAUSE
                        | PlaybackStateCompat.ACTION_STOP;
                break;
            case PAUSED:
                capabilities |= PlaybackStateCompat.ACTION_PLAY
                        | PlaybackStateCompat.ACTION_STOP;
                break;
            case IDLE:
                capabilities |= PlaybackStateCompat.ACTION_PLAY;
                break;
        }

        return capabilities;
    }

    /**
     * ---------------------------------------------------------------------------------------------
     * Notification Service
     * ---------------------------------------------------------------------------------------------
     */
    private NotificationService notificationService;
    private boolean notificationServiceIsActive;
    private ServiceConnection notificationServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            notificationService = ((NotificationService.NotificationServiceBinder) service).getService();
            notificationService.setVideoActivity(activity);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            notificationService = null;
        }
    };

    private void bindNotificationService() {
        Intent service = new Intent(this,
                NotificationService.class);
        this.bindService(service, notificationServiceConnection, Context.BIND_AUTO_CREATE);
        this.startService(service);
        notificationServiceIsActive = true;
    }

    private void unBindNotificationService() {
        if (notificationServiceIsActive) {
            this.unbindService(notificationServiceConnection);
            notificationServiceIsActive = false;
        }
    }
}


