package com.neeplayer;

import android.content.Context;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Scanner;

public class ArtistAdapter extends BaseAdapter{

    private ArrayList<Artist> artists;
    private LayoutInflater songInf;
    private ImageLoader imageLoader;

    public ArtistAdapter(Context context, ArrayList<Artist> artists) {
        this.artists = artists;
        songInf= LayoutInflater.from(context);
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(context).build();
        imageLoader = ImageLoader.getInstance();
        imageLoader.init(config);
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
        TextView descriptionView = (TextView) artistLayout.findViewById(R.id.artist_description);
        ImageView imageView = (ImageView) artistLayout.findViewById(R.id.artist_image);

        Artist artist = artists.get(position);

        nameView.setText(artist.getName());
        descriptionView.setText(String.format("%d albums, %d songs", artist.getNumberOfAlbums(), artist.getNumberOfSongs()));

        imageLoader.displayImage(artist.getImageURL(), imageView);

        artistLayout.setTag(position);

        return artistLayout;
    }

}
