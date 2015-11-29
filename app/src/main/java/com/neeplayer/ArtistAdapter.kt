package com.neeplayer

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide


import kotlinx.android.synthetic.artist.view.*;


class ArtistAdapter(private val context: Context, private val artists: List<Artist>) : BaseAdapter() {
    private val songInf: LayoutInflater = LayoutInflater.from(context)

    override fun getCount(): Int = artists.size

    override fun getItem(position: Int): Any = artists.get(position)

    override fun getItemId(position: Int): Long = 0

    internal data class ViewHolder(
        var nameView: TextView,
        var descriptionView: TextView,
        var imageView: ImageView
    )

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: let {
            val view = songInf.inflate(R.layout.artist, parent, false)
            view.tag = ViewHolder(view.artist_name, view.artist_description, view.artist_image)
            view
        }

        val holder = view.tag as ViewHolder
        val artist = artists[position]

        holder.nameView.text = artist.name
        holder.descriptionView.text = "%d albums, %d songs".format(artist.numberOfAlbums, artist.numberOfSongs)

        Glide.with(context).load(artist.imageURL).into(holder.imageView)

        return view
    }

}
