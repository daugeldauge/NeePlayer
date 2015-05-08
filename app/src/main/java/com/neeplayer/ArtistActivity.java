package com.neeplayer;

import android.app.Activity;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.net.Uri;
import android.view.View;
import android.widget.ListView;

import java.util.ArrayList;


public class ArtistActivity extends Activity {

    String artistName;
    Long artistId;
    ArrayList<Album> albumList;
    private ListView albumView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_artist);

        Intent intent = getIntent();
        artistName  = intent.getStringExtra("ARTIST_NAME");
        artistId = intent.getLongExtra("ARTIST_ID", 0);

        setTitle(artistName);

        albumView = (ListView) findViewById(R.id.album_list);
        albumList = new ArrayList<Album>();

        getAlbumList();

        AlbumAdapter albumAdapter = new AlbumAdapter(this, albumList);
        albumView.setAdapter(albumAdapter);

    }

    private void getAlbumList() {
        Uri uri = MediaStore.Audio.Artists.Albums.getContentUri("external", artistId);

        ContentResolver resolver = getContentResolver();

        String idColumnName = MediaStore.Audio.Albums._ID;
        String titleColumnName = MediaStore.Audio.Artists.Albums.ALBUM;
        String artColumnName = MediaStore.Audio.Artists.Albums.ALBUM_ART;
        String yearColumnName = MediaStore.Audio.Artists.Albums.FIRST_YEAR;

        Cursor cursor = resolver.query(
                uri,
                new String[]{idColumnName, titleColumnName, artColumnName, yearColumnName},
                null,
                null,
                yearColumnName);

        if (cursor != null && cursor.moveToFirst()) {

            int idColumn = cursor.getColumnIndexOrThrow(idColumnName);
            int titleColumn = cursor.getColumnIndexOrThrow(titleColumnName);
            int artColumn = cursor.getColumnIndexOrThrow(artColumnName);
            int yearColumn = cursor.getColumnIndexOrThrow(yearColumnName);

            do {
                Long id = cursor.getLong(idColumn);

                Album album = new Album(
                        id,
                        cursor.getString(titleColumn),
                        cursor.getInt(yearColumn),
                        cursor.getString(artColumn),
                        getAlbumSongs(id)
                );

                albumList.add(album);
            } while(cursor.moveToNext());

            cursor.close();
        }


    }

    private ArrayList<Song> getAlbumSongs(Long albumId) {
        ContentResolver resolver = getContentResolver();
        ArrayList<Song> list = new ArrayList<Song>();

        String idColumnName = MediaStore.Audio.Media._ID;
        String titleColumnName = MediaStore.Audio.Media.TITLE;
        String durationColumnName = MediaStore.Audio.Media.DURATION;
        String trackColumnName = MediaStore.Audio.Media.TRACK;

        Cursor cursor = resolver.query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                new String[] { idColumnName, titleColumnName, durationColumnName, trackColumnName },
                MediaStore.Audio.Media.ALBUM_ID + "=?",
                new String[] { albumId.toString() },
                MediaStore.Audio.Media.TRACK);


        if (cursor != null && cursor.moveToFirst()) {

            do {
                list.add(new Song(
                        cursor.getLong(0),
                        cursor.getString(1),
                        cursor.getLong(2),
                        cursor.getInt(3)
                ));
            } while(cursor.moveToNext());

            cursor.close();
        }

        return list;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_artist, menu);
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

    private MusicService musicService;
    private Intent playIntent;

    private Boolean musicBound = false;

    private ServiceConnection musicConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MusicService.MusicBinder binder = (MusicService.MusicBinder) service;
            musicService = binder.getService();
            musicService.setList(albumList);
            musicService.setArtistName(artistName);
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
            startService(playIntent);
        }
    }


    public void songPicked(View view) {
        musicService.setPosition((int) view.getTag(R.id.ALBUM_POSITION), (int) view.getTag(R.id.SONG_POSITION));
        musicService.playSong();
    }
}
