package com.neeplayer;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentUris;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;

public class MusicService extends Service implements MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener, MediaPlayer.OnCompletionListener {

    private MediaPlayer player;

    private ArrayList<Album> albums;

    private String artistName;
    private int songPosition;
    private int albumPosition;
    private String songTitle;
    private static final int NOTIFY_ID = 1;
    public void onCreate() {
        super.onCreate();
        songPosition = 0;
        player = new MediaPlayer();
        initMediaPLayer();
    }

    public void initMediaPLayer() {
        player.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
        player.setAudioStreamType(AudioManager.STREAM_MUSIC);

        player.setOnPreparedListener(this);
        player.setOnCompletionListener(this);
        player.setOnErrorListener(this);
    }

    public void setList(ArrayList<Album> albums) {
        this.albums = albums;
    }

    public void setArtistName(String artistName) {
        this.artistName = artistName;
    }

    public void setPosition(int albumPosition, int songPosition) {
        this.songPosition = songPosition;
        this.albumPosition = albumPosition;
    }

    public void playSong() {
        player.reset();
        Song song = albums.get(albumPosition).getSongs().get(songPosition);
        Long id = song.getId();

        songTitle = song.getTitle();

        Uri trackUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id);

        try {
            player.setDataSource(getApplicationContext(), trackUri);
            player.prepareAsync();
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), "Error on setting data source", Toast.LENGTH_SHORT).show();
        }
    }

    public class MusicBinder extends Binder {
        MusicService getService() {
            return MusicService.this;
        }
    }

    private final IBinder musicBind = new MusicBinder();

    @Override
    public IBinder onBind(Intent intent) {
        return musicBind;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        player.stop();
        player.release();
        return false;
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        if (player.getCurrentPosition() > 0) {
            mp.reset();
            playNext();
        }
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        mp.reset();
        return false;
    }

    @Override
    public void onDestroy() {
        stopForeground(true);
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        mp.start();

        Intent notIntent = new Intent(this, ArtistActivity.class);
        notIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Notification.Builder builder = new Notification.Builder(this);

        builder.setContentIntent(pendingIntent)
                .setSmallIcon(R.drawable.play)
                .setTicker(songTitle)
                .setOngoing(true)
                .setContentTitle(songTitle)
                .setContentText(artistName);

        Notification not = builder.build();

        startForeground(NOTIFY_ID, not);


    }

    public int getSongPosition() {
        return player.getCurrentPosition();
    }

    public int getDuration() {
        return player.getDuration();
    }

    public Boolean isPLaying() {
        return player.isPlaying();
    }

    public void pausePlayer() {
        player.pause();
    }

    public void seekTo(int position) {
        player.seekTo(position);
    }

    public void start() {
        player.start();
    }

    public void playPrevious() {
        --songPosition;
        if (songPosition < 0) {
            --albumPosition;
            if (albumPosition < 0) {
                albumPosition = albums.size() - 1;
            }
            songPosition = albums.get(albumPosition).getSongs().size() - 1;
        }
        playSong();
    }

    public void playNext() {
        ++songPosition;
        if (songPosition >= albums.get(albumPosition).getSongs().size()) {
            songPosition = 0;
            ++albumPosition;
            if (albumPosition >= albums.size()) {
                albumPosition = 0;
            }
        }
        playSong();
    }
}
