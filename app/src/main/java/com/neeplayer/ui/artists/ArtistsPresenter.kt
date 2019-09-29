package com.neeplayer.ui.artists

import com.neeplayer.model.Artist
import com.neeplayer.model.ArtistImagesStorage
import com.neeplayer.model.Database
import com.neeplayer.ui.BasePresenter
import com.neeplayer.ui.Router
import org.json.JSONObject
import javax.inject.Inject

class ArtistsPresenter @Inject constructor(
        private val database: Database,
        private val artistImagesStorage: ArtistImagesStorage,
        private val router: Router
) : BasePresenter<ArtistsView>() {

    override fun bind(view: ArtistsView) {
        super.bind(view)
        val artists = database.getArtists()
        view.showArtists(artists)
        /* TODO: Use Deezer API to fetch artist images
        artists.filter { it.imageUrl == null }.forEach { artist ->
            subscriptions += lastFm.getArtistInfo(artist.name)
                    .subscribeOn(Schedulers.io())
                    .map { getArtistImageUrl(it) }
                    .subscribe({
                        artistImagesStorage.put(artist.name, it)
                        view.updateArtist(artist.withImage(it))
                    }, {
                        Timber.w(it, "Couldn't retrieve artist image url")
                    })
        }
        */
    }

    fun onArtistClicked(artist: Artist) {
        router.gotToAlbums(artist)
    }

    private fun getArtistImageUrl(artistJsonInfo: JSONObject): String {
        val images = artistJsonInfo.getJSONObject("artist").getJSONArray("image")

        for (i in 0 until images.length()) {
            val image = images.getJSONObject(i)
            val size = image.getString("size")
            if (size == "extralarge") {
                return image.getString("#text")
            }
        }

        throw RuntimeException("Invalid json")
    }

}