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


public class NowPlayingActivity extends Activity implements MediaController.MediaPlayerControl {
    ArrayList<Album> albumList;
    String artistName;

    int albumPosition;
    int songPosition;

    private MusicService musicService;
    private Intent playIntent;

    private MusicController controller;

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

        setController();
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

    private void setController() {
        controller = new MusicController(this);
        controller.setMediaPlayer(this);
        controller.setAnchorView(findViewById(R.id.np_layout));
        controller.setEnabled(true);
    }

    private void playNext() {
        musicService.playNext();
        controller.show(0);
    }

    private void playPrevious() {
        musicService.playPrevious();
        controller.show(0);
    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }

    @Override
    public void start() {
        musicService.start();
    }

    @Override
    public void pause() {
        musicService.pausePlayer();
    }

    @Override
    public int getDuration() {
        if (musicService != null && musicBound && musicService.isPlaying()) {
            return musicService.getDuration();
        } else {
            return 0;
        }
    }

    @Override
    public int getCurrentPosition() {
        if (musicService != null && musicBound && musicService.isPlaying()) {
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
            return musicService.isPlaying();
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
