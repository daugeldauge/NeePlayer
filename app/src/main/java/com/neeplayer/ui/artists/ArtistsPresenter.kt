package com.neeplayer.ui.artists

import com.neeplayer.api.apotify.SpotifyApi
import com.neeplayer.api.deezer.DeezerApi
import com.neeplayer.model.Artist
import com.neeplayer.model.ArtistImagesStorage
import com.neeplayer.model.Database
import com.neeplayer.ui.BasePresenter
import com.neeplayer.ui.Router
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class ArtistsPresenter @Inject constructor(
        private val database: Database,
        private val artistImagesStorage: ArtistImagesStorage,
        private val deezer: DeezerApi,
        private val spotify: SpotifyApi,
        private val router: Router
) : BasePresenter<ArtistsView>() {

    override fun bind(view: ArtistsView) {
        super.bind(view)
        val artists = database.getArtists()
        view.showArtists(artists)
        fetchUnknownArtistImages(artists, view)
    }

    fun onArtistClicked(artist: Artist) {
        router.gotToAlbums(artist)
    }

    private fun fetchUnknownArtistImages(artists: List<Artist>, view: ArtistsView) {
        mainScope.launch {
            artists.filter { it.imageUrl == null }.forEach { artist ->
                val image = fetchImageFromSpotify(artist) ?: fetchImageFromDeezer(artist)
                if (image != null) {
                    artistImagesStorage.put(artist.name, image)
                    view.updateArtist(artist.withImage(image))
                } else {
                    Timber.w("Couldn't fetch artist image url")
                }
            }
        }
    }

    private suspend fun fetchImageFromSpotify(artist: Artist): String? {
        val response = spotify.searchArtist(artist.name).success() ?: return null

        return response.body
                .artists
                .items
                .firstOrNull()
                ?.images
                ?.run { getOrNull(1) ?: first() } // Second-quality image, if present
                ?.url
    }

    private suspend fun fetchImageFromDeezer(artist: Artist): String? {
        return deezer.searchArtist(artist.name)
                .success()
                ?.body
                ?.data
                ?.firstOrNull()
                ?.artist
                ?.pictureMedium
    }

}