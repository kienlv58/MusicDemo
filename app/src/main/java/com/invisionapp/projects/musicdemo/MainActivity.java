package com.invisionapp.projects.musicdemo;

import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.net.Uri;
import android.os.IBinder;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.MediaController;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class MainActivity extends AppCompatActivity implements MediaController.MediaPlayerControl {


    private MusicService musicService;
    private Intent playIntent;
    private boolean musicBound = false;
    ArrayList<Song> listSongs = new ArrayList<>();
    ListView listView;
    ListSongAdapter adapter;
    MusicController musicController;
    private boolean pause = false;
    private boolean playbackPause = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getSongList();

        Collections.sort(listSongs, new Comparator<Song>() {
            public int compare(Song a, Song b) {
                return a.getTitle().compareTo(b.getTitle());
            }
        });
        listView = (ListView) findViewById(R.id.listview);
        adapter = new ListSongAdapter(this, listSongs);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                int pos = (int) view.getTag();
                Log.d("pos: ", pos + "");
                musicService.setSong(pos);
                musicService.playSong();
                if(playbackPause){
                    setController();
                    playbackPause = false;
                }
                musicController.show(3);
            }
        });

        setController();


    }

    public void setController() {
        musicController = new MusicController(this);
        musicController.setPrevNextListeners(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playNext();

            }
        }, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playPrev();
            }
        });
        musicController.setMediaPlayer(this);
        musicController.setAnchorView(findViewById(R.id.playerview));
        musicController.setEnabled(true);
    }

    public void playNext() {
        musicService.playNext();
        if(playbackPause){
            setController();
            playbackPause = false;
        }
        musicController.show(0);
    }

    public void playPrev() {
        musicService.playPrev();
        if(playbackPause){
            setController();
            playbackPause = false;
        }
        musicController.show(0);
    }

    @Override
    protected void onPause() {
        super.onPause();
            pause = true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(pause){
            setController();
            pause = false;
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        musicController.hide();

    }

    @Override
    protected void onDestroy() {
        stopService(playIntent);
        musicService = null;
        super.onDestroy();
    }

    public void getSongList() {
        ContentResolver musicResolver = getContentResolver();
        Uri musicUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Cursor musicCursor = musicResolver.query(musicUri, null, null, null, null);
        if (musicCursor != null && musicCursor.moveToFirst()) {
            int titleColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media.TITLE);
            int idColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media._ID);
            int artistColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media.ARTIST);

            do {
                long id = musicCursor.getLong(idColumn);
                String title = musicCursor.getString(titleColumn);
                String artist = musicCursor.getString(artistColumn);
                listSongs.add(new Song(id, title, artist));
            } while (musicCursor.moveToNext());
        }
    }


    private ServiceConnection musicConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicService.MusicBinder binder = (MusicService.MusicBinder) service;
            musicService = binder.getService();
            musicService.setListSongs(listSongs);
            musicBound = true;

        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            musicBound = false;
        }
    };


    @Override
    protected void onStart() {
        super.onStart();
        if (playIntent == null) {
            playIntent = new Intent(this, MusicService.class);
            bindService(playIntent, musicConnection, Context.BIND_AUTO_CREATE);
        }
    }

    @Override
    public void start() {
        musicService.startPlay();
        musicController.show(3);

    }

    @Override
    public void pause() {
        if (musicService != null && musicBound && musicService.isPlaying())
            musicService.pausePlayer();
        playbackPause = true;

    }

    @Override
    public int getDuration() {
        if (musicService != null && musicBound && musicService.isPlaying())
            return musicService.getDur();
        return 0;
    }

    @Override
    public int getCurrentPosition() {
        if (musicService != null && musicBound && musicService.isPlaying())
            return musicService.getPos();
        return 0;
    }

    @Override
    public void seekTo(int pos) {

        musicService.seekTo(pos);

    }

    @Override
    public boolean isPlaying() {
        if (musicService != null && musicBound)
            return musicService.isPlaying();
        return false;
    }

    @Override
    public int getBufferPercentage() {
        return 0;
    }

    @Override
    public boolean canPause() {
        return true;
    }

    @Override
    public boolean canSeekBackward() {
        return true;
    }

    @Override
    public boolean canSeekForward() {
        return true;
    }

    @Override
    public int getAudioSessionId() {
        return 0;
    }
}
