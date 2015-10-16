package com.neeplayer;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class AlbumAdapter extends BaseAdapter {
    private ArrayList<Album> albums;
    private LayoutInflater inflater;
    private ImageLoader imageLoader;

    public AlbumAdapter(Context context, ArrayList<Album> albums) {
        this.albums = albums;
        inflater = LayoutInflater.from(context);

        imageLoader = ImageLoader.getInstance();
    }

    @Override
    public int getCount() {
        return albums.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    static class ViewHolder {
        TextView title;
        TextView year;
        TextView info;
        ImageView art;
        LinearLayout songs;
    }

    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.album, parent, false);
            holder = new ViewHolder();

            holder.title = (TextView) convertView.findViewById(R.id.album_title);
            holder.year  = (TextView) convertView.findViewById(R.id.album_year);
            holder.info  = (TextView) convertView.findViewById(R.id.album_info);
            holder.art  = (ImageView) convertView.findViewById(R.id.album_art);
            holder.songs = (LinearLayout) convertView.findViewById(R.id.song_list);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Album album = albums.get(position);

        holder.title.setText(album.getTitle());
        holder.year.setText(Integer.toString(album.getYear()));


        imageLoader.displayImage("file://" + album.getArt(), holder.art);

        long albumDuration = 0;
        holder.songs.removeAllViews();
        for (int i = 0; i < album.getSongs().size(); ++i) {
            Song song = album.getSongs().get(i);

            LinearLayout songView = (LinearLayout)inflater.inflate(R.layout.song, holder.songs, false);

            TextView track =    (TextView) songView.findViewById(R.id.song_track);
            TextView title =    (TextView) songView.findViewById(R.id.song_title);
            TextView duration = (TextView) songView.findViewById(R.id.song_duration);

            int songTrack = song.getTrack();
            if (songTrack >= 1000) {
                songTrack = songTrack % 1000;
            }

            track.setText(Integer.toString(songTrack));
            title.setText(song.getTitle());

            long ms = song.getDuration();
            albumDuration += ms;
            long min = TimeUnit.MILLISECONDS.toMinutes(ms);
            long sec = TimeUnit.MILLISECONDS.toSeconds(ms) - TimeUnit.MINUTES.toSeconds(min);
            duration.setText(String.format("%d:%02d", min, sec));

            songView.setTag(R.id.ALBUM_POSITION, position);
            songView.setTag(R.id.SONG_POSITION, i);

            holder.songs.addView(songView);
        }

        holder.info.setText(String.format(
                "%d songs, %d min",
                album.getSongs().size(),
                TimeUnit.MILLISECONDS.toMinutes(albumDuration)));

        return convertView;
    }
}
