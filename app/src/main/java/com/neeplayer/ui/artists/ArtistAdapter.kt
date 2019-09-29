package com.neeplayer.ui.artists


import android.content.Context
import androidx.databinding.DataBindingUtil
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.neeplayer.databinding.ArtistBinding
import com.neeplayer.model.Artist


class ArtistAdapter(
        private val context: Context,
        private val onClickListener: (Artist) -> Unit
) : RecyclerView.Adapter<ViewHolder>() {

    private var artists: MutableList<Artist> = mutableListOf()

    override fun getItemCount(): Int = artists.size

    class ViewHolder(view: View): RecyclerView.ViewHolder(view) {
        val binding = DataBindingUtil.bind<ArtistBinding>(view)!!
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder  {
        return ViewHolder(ArtistBinding.inflate(LayoutInflater.from(context), parent, false).root)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val binding = (holder as ViewHolder).binding
        val artist = artists[position]
        binding.artist = artist
        binding.root.setOnClickListener { onClickListener(artist) }
    }

    fun setArtists(artists: List<Artist>) {
        this.artists = artists.toMutableList()
        notifyDataSetChanged()
    }

    fun updateArtist(updatedArtist: Artist) {
        artists.mapIndexed { i, artist -> if (artist == updatedArtist) i else null }
                .filterNotNull()
                .forEach {
                    artists[it] = updatedArtist
                    notifyItemChanged(it)
                }
    }

}
