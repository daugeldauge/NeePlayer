package com.neeplayer;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
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
    private Boolean paused;

    private ImageLoader imageLoader;

    private ServiceConnection musicConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicService.MusicBinder binder = (MusicService.MusicBinder) service;
            musicService = binder.getService();

            musicService.setList(albumList);
            musicService.setPosition(albumPosition, songPosition);
            musicService.playSong();

            paused = false;
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

        IntentFilter filter = new IntentFilter();
        filter.addAction("UPDATE_CURRENT_SONG");
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, filter);

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

        updateScreen();
    }

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals("UPDATE_CURRENT_SONG")) {
                albumPosition = intent.getIntExtra("ALBUM_POSITION", 0);
                songPosition = intent.getIntExtra("SONG_POSITION", 0);
            }
            updateScreen();
        }
    };

    private void updateScreen() {
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

    public void onFastRewindPressed(View view) {
        musicService.playPrevious();
    }

    public void onFastForwardPressed(View view) {
        musicService.playNext();
    }

    public void onPlayPausePressed(View view) {
        if (musicBound) {
            ImageButton button = (ImageButton) findViewById(R.id.np_play_pause);
            int drawableId;

            if (paused) {
                musicService.start();
                drawableId = R.drawable.ic_pause_black_48dp;
            } else {
                musicService.pausePlayer();
                drawableId = R.drawable.ic_play_arrow_black_48dp;
            }

            button.setImageDrawable(getResources().getDrawable(drawableId));

            paused = !paused;
        }
    }

}
