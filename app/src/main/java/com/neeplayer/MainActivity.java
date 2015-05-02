package com.neeplayer;

import android.app.Activity;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.MediaController.MediaPlayerControl;

import java.util.ArrayList;


public class MainActivity extends Activity implements MediaPlayerControl {

    private ArrayList<Artist> artistList;
    private ListView artistView;

    private MusicService musicService;
    private Intent playIntent;
    private Boolean musicBound = false;

    private MusicController controller;

    private Boolean paused = false;
    private Boolean playbackPaused = false;


    private ServiceConnection musicConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicService.MusicBinder binder = (MusicService.MusicBinder) service;
            musicService = binder.getService();
//            musicService.setList(songList);
            musicBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            musicBound = false;
        }
    };

//    @Override
//    protected void onStart() {
//        super.onStart();
//        if (playIntent == null) {
//            playIntent = new Intent(this, MusicService.class);
//            bindService(playIntent, musicConnection, Context.BIND_AUTO_CREATE);
//            startService(playIntent);
//        }
//    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        artistView = (ListView) findViewById(R.id.artist_list);
        artistList = new ArrayList<Artist>();

        getArtistList();

        ArtistAdapter artistAdapter = new ArtistAdapter(this, artistList);
        artistView.setAdapter(artistAdapter);

        setController();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (paused) {
            setController();
            paused = false;
        }
    }

    @Override
    protected void onStop() {
        controller.hide();
        super.onStop();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void getArtistList() {
        ContentResolver resolver = getContentResolver();
        Uri uri = MediaStore.Audio.Artists.EXTERNAL_CONTENT_URI;

        //TODO: Add projection
        Cursor cursor = resolver.query(uri, null, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            String idColumnName = MediaStore.Audio.Artists._ID;
            String nameColumnName = MediaStore.Audio.Artists.ARTIST;

            int nameColumn = cursor.getColumnIndexOrThrow(nameColumnName);
            int idColumn = cursor.getColumnIndexOrThrow(idColumnName);

            do {
                Artist artist = new Artist(
                        cursor.getLong(idColumn),
                        cursor.getString(nameColumn)
                );

                artistList.add(artist);
            } while(cursor.moveToNext());

            cursor.close();
        }

    }

    public void songPicked(View view) {
        musicService.setSongPosition(Integer.parseInt(view.getTag().toString()));
        musicService.playSong();
        if (playbackPaused) {
            setController();
            playbackPaused = false;
        }
        controller.show(0);
    }

    @Override
    protected void onDestroy() {
//        stopService(playIntent);
//        musicService = null;
        super.onDestroy();
    }

    private void setController() {
        controller = new MusicController(this);
        controller.setPrevNextListeners(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playNext();
            }
        }, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playPrevious();
            }
        });

        controller.setMediaPlayer(this);
        controller.setAnchorView(findViewById(R.id.artist_list));
        controller.setEnabled(true);
    }

    private void playNext() {
        musicService.playNext();
        if (playbackPaused) {
            setController();
            playbackPaused = false;
        }
        controller.show(0);
    }

    private void playPrevious() {
        musicService.playPrevious();
        if (playbackPaused) {
            setController();
            playbackPaused = false;
        }
        controller.show(0);
    }

    @Override
    public void start() {
        musicService.start();
    }

    @Override
    public void pause() {
        playbackPaused = true;
        musicService.pausePlayer();
    }

    @Override
    public int getDuration() {
        if (musicService != null && musicBound && musicService.isPLaying()) {
            return musicService.getDuration();
        } else {
            return 0;
        }
    }

    @Override
    public int getCurrentPosition() {
        if (musicService != null && musicBound && musicService.isPLaying()) {
            return musicService.getSongPosition();
        } else {
            return 0;
        }
    }

    @Override
    public void seekTo(int pos) {
        musicService.seekTo(pos);
    }

    @Override
    public boolean isPlaying() {
        if (musicService != null && musicBound) {
            return musicService.isPLaying();
        } else {
            return false;
        }
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
