package com.invisionapp.projects.musicdemo;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.ContentUris;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.session.MediaSession;
import android.media.session.MediaSessionManager;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.provider.MediaStore;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v7.graphics.Palette;
import android.text.TextUtils;

import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;

public class MusicService extends Service implements MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener, MediaPlayer.OnCompletionListener {


    public final String PREVIOUS_ACTION = "previous_action";
    public final String STOP_ACTION = "stop_action";
    public final String PAUSE_ACTION = "pause_action";
    public final String NEXT_ACTION = "next_action";
    public final String TOGGLEPAUSE_ACTION = "togglepause_action";
    private MediaPlayer player;

    private MediaSessionCompat mSession;
    private NotificationManagerCompat mNotificationManager;
    private long mNotificationPostTime = 0;

    private ArrayList<Song> listSongs;
    private int songPosition;
    private static final int NOTIFI_ID = 1;
    private Song currentSong;

    private final IBinder musicBind = new MusicBinder();


    @Override
    public void onCreate() {
        super.onCreate();

        mNotificationManager = NotificationManagerCompat.from(this);
        setUpMediaSession();

        songPosition = 0;
        player = new MediaPlayer();
        initMusicPlayer();


    }


    public void initMusicPlayer() {
        player.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);//music play background and sleep
        player.setAudioStreamType(AudioManager.STREAM_MUSIC);
        player.setOnPreparedListener(this);
        player.setOnCompletionListener(this);
        player.setOnErrorListener(this);
    }

    public void setListSongs(ArrayList<Song> listSongs) {
        this.listSongs = listSongs;
    }


    public void setSong(int songPos) {
        songPosition = songPos;
    }

    public MusicService() {
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopForeground(true);
        mSession.release();
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.

        return musicBind;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        player.stop();
        player.release();
        return false;

    }


    private void setUpMediaSession() {
        mSession = new MediaSessionCompat(this, "Timber");
        mSession.setCallback(new MediaSessionCompat.Callback() {
            @Override
            public void onPause() {
                pausePlayer();
            }

            @Override
            public void onPlay() {
                startPlay();
            }

            @Override
            public void onSeekTo(long pos) {

                seekTo((int) pos);
            }

            @Override
            public void onSkipToNext() {
                playNext();
            }

            @Override
            public void onSkipToPrevious() {
                playPrev();
            }

            @Override
            public void onStop() {
                pausePlayer();

            }
        });
        mSession.setFlags(MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);
    }

    public void playSong() {
        player.reset();
        Song playSong = listSongs.get(songPosition);
        currentSong = playSong;
        long currSong = playSong.getId();
        Uri trackUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, currSong);

        try {
            player.setDataSource(getApplicationContext(), trackUri);
        } catch (Exception e) {
            e.printStackTrace();
        }
        player.prepareAsync();
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        mp.start();
//        String songTitle = listSongs.get(songPosition).getTitle();
//        Intent noti = new Intent(this,MainActivity.class);
//        noti.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(),0,noti,PendingIntent.FLAG_UPDATE_CURRENT);
//        Notification.Builder builder = new Notification.Builder(this);
//        builder.setContentIntent(pendingIntent).setSmallIcon(R.mipmap.ic_launcher).setTicker(songTitle).setOngoing(true).setContentTitle("playing").
//                setContentText(songTitle);
//
//        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
//            Notification notification = builder.build();
//            startForeground(NOTIFI_ID,notification);
//        }
        startForeground(NOTIFI_ID, buildNotification());


    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        mp.reset();
        return false;
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        if (mp.getCurrentPosition() > 0) {
            mp.reset();
            playNext();
        }
    }


    /**
     * build notification on top bar
     */

    private Notification buildNotification() {
        if (currentSong != null)
            currentSong = listSongs.get(songPosition);
        final String albumName = currentSong.getTitle();
        final String artistName = currentSong.getArtist();
        final boolean isPlaying = isPlaying();
        String text = TextUtils.isEmpty(albumName)
                ? artistName : artistName + " - " + albumName;

        int playButtonResId = isPlaying
                ? R.drawable.ic_pause_white_36dp : R.drawable.ic_play_white_36dp;

        Intent nowPlayingIntent = NavigationUtils.getNowPlayingIntent(this);
        PendingIntent clickIntent = PendingIntent.getActivity(this, 0, nowPlayingIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        Bitmap artwork = null;
//        artwork = ImageLoader.getInstance().loadImageSync(TimberUtils.getAlbumArtUri(getAlbumId()).toString());

        if (artwork == null) {
            artwork = BitmapFactory.decodeResource(getApplication().getResources(),
                    R.drawable.ic_empty_music2);
//            Uri uri = Uri.parse("android.resource://com.invisionapp.projects.musicdemo/drawable/ic_empty_music2");
//            artwork = ImageLoader.getInstance().loadImageSync(uri.toString());
        }

//        if (mNotificationPostTime == 0) {
//            mNotificationPostTime = System.currentTimeMillis();
//        }

        mNotificationPostTime = System.currentTimeMillis();
        android.support.v4.app.NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_notification)
                .setLargeIcon(artwork)
                .setContentIntent(clickIntent)
                .setContentTitle(albumName)
                .setContentText(artistName)
                .setWhen(mNotificationPostTime)
                .addAction(R.drawable.ic_skip_previous_white_36dp,
                        "",
                        retrievePlaybackAction(PREVIOUS_ACTION))
                .addAction(playButtonResId, "",
                        retrievePlaybackAction(TOGGLEPAUSE_ACTION))
                .addAction(R.drawable.ic_skip_next_white_36dp,
                        "",
                        retrievePlaybackAction(NEXT_ACTION));

        if (AppUtils.isJellyBeanMR1()) {
            builder.setShowWhen(false);
        }
        if (AppUtils.isLollipop()) {
            builder.setVisibility(Notification.VISIBILITY_PUBLIC);
            android.support.v7.app.NotificationCompat.MediaStyle style = new android.support.v7.app.NotificationCompat.MediaStyle()
                    .setMediaSession(mSession.getSessionToken())
                    .setShowActionsInCompactView(0, 1, 2, 3);
            builder.setStyle(style);
        }
        if (artwork != null && AppUtils.isLollipop())
            builder.setColor(Palette.from(artwork).generate().getVibrantColor(Color.parseColor("#403f4d")));
        Notification n = builder.build();


        return n;
    }


    private final PendingIntent retrievePlaybackAction(final String action) {
        final ComponentName serviceName = new ComponentName(this, MusicService.class);
        Intent intent = new Intent(action);
        intent.setComponent(serviceName);

        return PendingIntent.getService(this, 0, intent, 0);
    }


    public class MusicBinder extends Binder {
        MusicService getService() {
            return MusicService.this;
        }
    }

    public int getPos() {
        return player.getCurrentPosition();
    }

    public int getDur() {
        return player.getDuration();
    }

    public boolean isPlaying() {
        return player.isPlaying();
    }

    public void pausePlayer() {
        player.pause();
    }

    public void seekTo(int pos) {
        player.seekTo(pos);
    }

    public void startPlay() {
        player.start();
    }

    public void playNext() {
        songPosition++;
        if (songPosition == listSongs.size())
            songPosition = 0;
        playSong();
    }

    public void playPrev() {
        songPosition--;
        if (songPosition == 0)
            songPosition = listSongs.size() - 1;
        playSong();
    }

}
