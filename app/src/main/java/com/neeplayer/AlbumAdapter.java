package com.neeplayer;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

public class AlbumAdapter extends BaseAdapter {
    private ArrayList<Album> albums;
    private LayoutInflater albumInf;

    public AlbumAdapter(Context context, ArrayList<Album> albums) {
        this.albums = albums;
        albumInf = LayoutInflater.from(context);
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
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = albumInf.inflate(R.layout.album, parent, false);
            holder = new ViewHolder();

            holder.title = (TextView) convertView.findViewById(R.id.album_title);
            holder.year  = (TextView) convertView.findViewById(R.id.album_year);
            holder.info  = (TextView) convertView.findViewById(R.id.album_info);
            holder.art  = (ImageView) convertView.findViewById(R.id.album_art);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Album album = albums.get(position);

        holder.title.setText(album.getTitle());
        holder.year.setText(Integer.toString(album.getYear()));
        holder.info.setText("11 songs, 56 min");

        holder.art.setImageBitmap(BitmapFactory.decodeFile(album.getArt()));

        return convertView;
    }
}
