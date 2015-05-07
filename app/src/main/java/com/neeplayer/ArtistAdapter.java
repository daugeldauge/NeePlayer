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

    static class ViewHolder {
        TextView nameView;
        TextView descriptionView;
        ImageView imageView;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = songInf.inflate(R.layout.artist, parent, false);
            holder = new ViewHolder();

            holder.nameView = (TextView) convertView.findViewById(R.id.artist_name);
            holder.descriptionView = (TextView) convertView.findViewById(R.id.artist_description);
            holder.imageView = (ImageView) convertView.findViewById(R.id.artist_image);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Artist artist = artists.get(position);

        holder.nameView.setText(artist.getName());
        holder.descriptionView.setText(String.format("%d albums, %d songs", artist.getNumberOfAlbums(), artist.getNumberOfSongs()));

        imageLoader.displayImage(artist.getImageURL(), holder.imageView);

        return convertView;
    }

}
