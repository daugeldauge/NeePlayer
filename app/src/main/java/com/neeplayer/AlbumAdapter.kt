package com.neeplayer

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.bumptech.glide.Glide


import java.util.concurrent.TimeUnit

import kotlinx.android.synthetic.album.view.*;


class AlbumAdapter(private val context: Context, private val albums: List<Album>) : BaseAdapter() {
    private val inflater = LayoutInflater.from(context)


    override fun getCount(): Int = albums.size

    override fun getItem(position: Int): Any? = null

    override fun getItemId(position: Int): Long = 0

    internal data class ViewHolder(
            val title: TextView,
            val year: TextView,
            val info: TextView,
            val art: ImageView,
            val songs: LinearLayout
    )

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View? {

        val view = convertView ?: let {
            val view = inflater.inflate(R.layout.album, parent, false)
            view.tag = ViewHolder(view.album_title, view.album_year, view.album_info, view.album_art, view.song_list)
            view
        }

        val holder = view.tag as ViewHolder

        val album = albums[position]

        holder.title.text = album.title
        holder.year.text = album.year.toString()

        Glide.with(context).load("file://" + album.art).dontAnimate().into(holder.art)

        var albumDuration: Long = 0
        holder.songs.removeAllViews()

        album.songs.forEachIndexed { i, song ->
            val songView = inflater.inflate(R.layout.song, holder.songs, false) as LinearLayout

            val track = songView.findViewById(R.id.song_track) as TextView
            val title = songView.findViewById(R.id.song_title) as TextView
            val duration = songView.findViewById(R.id.song_duration) as TextView


            track.text = song.track?.mod(1000).toString()

            title.text = song.title

            val ms = song.duration
            albumDuration += ms
            val min = TimeUnit.MILLISECONDS.toMinutes(ms)
            val sec = TimeUnit.MILLISECONDS.toSeconds(ms) - TimeUnit.MINUTES.toSeconds(min)
            duration.text = "%d:%02d".format(min, sec)

            songView.setTag(R.id.ALBUM_POSITION, position)
            songView.setTag(R.id.SONG_POSITION, i)

            holder.songs.addView(songView)
        }

        holder.info.text = "%d songs, %d min".format(album.songs.size, TimeUnit.MILLISECONDS.toMinutes(albumDuration))

        return view
    }
}
