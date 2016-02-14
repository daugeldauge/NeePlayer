package com.neeplayer

import android.content.Context
import android.databinding.DataBindingUtil
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.neeplayer.databinding.ArtistBinding


import org.jetbrains.anko.onClick


class ArtistAdapter(private val context: Context, private val artists: List<Artist>, private val onClickListener: (Artist) -> Unit) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun getItemCount(): Int = artists.size

    class ViewHolder(view: View): RecyclerView.ViewHolder(view) {
        val binding = DataBindingUtil.bind<ArtistBinding>(view)
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): RecyclerView.ViewHolder?  {
        return ViewHolder(ArtistBinding.inflate(LayoutInflater.from(context), parent, false).root)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder?, position: Int) {
        val binding = (holder as ViewHolder).binding
        binding.artist = artists[position]
        binding.root.onClick { onClickListener(artists[position]) }
    }

}
