package com.neeplayer;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import java.util.ArrayList;


public class NowPlayingActivity extends Activity {
    ArrayList<Album> albumList;
    String artistName;

    int albumPosition;
    int songPosition;

    private MusicService musicService;
    private Intent playIntent;

    private Boolean musicBound = false;

    private ImageLoader imageLoader;

    private ServiceConnection musicConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicService.MusicBinder binder = (MusicService.MusicBinder) service;
            musicService = binder.getService();

            musicService.setList(albumList);
            musicService.setPosition(albumPosition, songPosition);
            musicService.playSong();

            musicBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            musicBound = false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_now_playing);

        imageLoader = ImageLoader.getInstance();

        onNewIntent(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        albumPosition = intent.getIntExtra("ALBUM_POSITION", 0);
        songPosition = intent.getIntExtra("SONG_POSITION", 0);
        albumList = (ArrayList<Album>) intent.getSerializableExtra("ALBUM_LIST");
        artistName = intent.getStringExtra("ARTIST_NAME");

        if (playIntent != null) {
            unbindService(musicConnection);
            stopService(playIntent);
        }

        playIntent = new Intent(this, MusicService.class);
        bindService(playIntent, musicConnection, Context.BIND_AUTO_CREATE);
        startService(playIntent);

        Album album = albumList.get(albumPosition);
        Song song = album.getSongs().get(songPosition);

        TextView songTitleView = (TextView)findViewById(R.id.np_song_title);
        songTitleView.setText(song.getTitle());

        TextView albumArtistView = (TextView)findViewById(R.id.np_artist_and_album);
        albumArtistView.setText(String.format("%s — %s", artistName, album.getTitle()));

        ImageView artView = (ImageView)findViewById(R.id.np_album_art);
        imageLoader.displayImage("file://" + album.getArt(), artView);

    }

    @Override
    protected void onDestroy() {
        unbindService(musicConnection);
        stopService(playIntent);
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_now_playing, menu);
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

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }

}
