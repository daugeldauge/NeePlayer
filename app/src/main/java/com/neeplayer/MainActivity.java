package com.neeplayer;

import android.app.Activity;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.MediaController.MediaPlayerControl;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Scanner;


public class MainActivity extends Activity implements MediaPlayerControl {

    private ArrayList<Artist> artistList;
    private ListView artistView;

    private MusicService musicService;
    private Intent playIntent;
    private Boolean musicBound = false;

    private MusicController controller;

    SharedPreferences artistImages;

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

        artistImages = getSharedPreferences("ArtistImages", 0);

        getArtistList();

        ArtistAdapter artistAdapter = new ArtistAdapter(this, artistList);
        artistView.setAdapter(artistAdapter);
        artistView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(MainActivity.this, ArtistActivity.class);

                Artist artist = (Artist) parent.getItemAtPosition(position);
                intent.putExtra("ARTIST_NAME", artist.getName());
                intent.putExtra("ARTIST_ID", artist.getId());
                startActivity(intent);
            }
        });

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

        String idColumnName = MediaStore.Audio.Artists._ID;
        String nameColumnName = MediaStore.Audio.Artists.ARTIST;
        String numberOfSongsColumnName = MediaStore.Audio.Artists.NUMBER_OF_TRACKS;
        String numberOfAlbumsColumnName = MediaStore.Audio.Artists.NUMBER_OF_ALBUMS;

        Cursor cursor = resolver.query(
                uri,
                new String[]{idColumnName, nameColumnName, numberOfSongsColumnName, numberOfAlbumsColumnName},
                null,
                null,
                null);

        if (cursor != null && cursor.moveToFirst()) {

            int nameColumn = cursor.getColumnIndexOrThrow(nameColumnName);
            int idColumn = cursor.getColumnIndexOrThrow(idColumnName);
            int numberOfSongsColumn = cursor.getColumnIndexOrThrow(numberOfSongsColumnName);
            int numberOfAlbumsColumn = cursor.getColumnIndexOrThrow(numberOfAlbumsColumnName);

            do {
                Artist artist = new Artist(
                        cursor.getLong(idColumn),
                        cursor.getString(nameColumn),
                        cursor.getInt(numberOfSongsColumn),
                        cursor.getInt(numberOfAlbumsColumn)
                );

                String image = artistImages.getString(artist.getName(), null);
                if (image != null) {
                    artist.setImageURL(image);
                } else {
                    new RetrieveArtistImageUrl().execute(artist);
                }

                artistList.add(artist);
            } while(cursor.moveToNext());

            cursor.close();
        }

    }

    class RetrieveArtistImageUrl extends AsyncTask<Artist, Void, Void> {
        @Override
        protected Void doInBackground(Artist... params) {
            String apiKey = "76b52a83c8c82ae436524353bcea2da0";
            Artist artist = (Artist) params[0];

            try {
                URL url = new URI(
                        "http",
                        "ws.audioscrobbler.com",
                        "/2.0",
                        String.format("method=artist.getinfo&artist=%s&api_key=%s&format=json", artist.getName(), apiKey),
                        null
                ).toURL();

                InputStream response = url.openConnection().getInputStream();
                String responseString = new Scanner(response).useDelimiter("\\A").next();

                JSONObject json = new JSONObject(responseString);
                JSONObject artistInfo = json.getJSONObject("artist");
                JSONArray images = artistInfo.getJSONArray("image");

                for (int i = 0; i < images.length(); ++i) {
                    JSONObject image = images.getJSONObject(i);
                    String size = image.getString("size");
                    if (size.equals("extralarge")) {
                        String imageURL = image.getString("#text");
                        artist.setImageURL(imageURL);

                        SharedPreferences.Editor editor = artistImages.edit();
                        editor.putString(artist.getName(), imageURL);
                        editor.commit();

                        return null;
                    }
                }


            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
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
