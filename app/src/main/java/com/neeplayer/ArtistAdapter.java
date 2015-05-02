package com.neeplayer;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

public class ArtistAdapter extends BaseAdapter{

    private ArrayList<Artist> artists;
    private LayoutInflater songInf;

    public ArtistAdapter(Context context, ArrayList<Artist> artists) {
        this.artists = artists;
        songInf= LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return artists.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LinearLayout artistLayout = (LinearLayout) songInf.inflate(R.layout.artist, parent, false);

        TextView nameView = (TextView) artistLayout.findViewById(R.id.artist_name);

        Artist currSong = artists.get(position);

        nameView.setText(currSong.getName());

        artistLayout.setTag(position);

        return artistLayout;
    }
}
