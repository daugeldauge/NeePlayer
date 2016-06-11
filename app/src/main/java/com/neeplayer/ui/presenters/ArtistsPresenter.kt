package com.neeplayer.ui.presenters

import com.neeplayer.LastFmService
import com.neeplayer.model.Artist
import com.neeplayer.model.ArtistImagesStorage
import com.neeplayer.model.Database
import com.neeplayer.ui.views.ArtistsView
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.warn
import org.json.JSONObject
import rx.schedulers.Schedulers
import rx.subscriptions.CompositeSubscription
import javax.inject.Inject

class ArtistsPresenter
    @Inject constructor(private val database: Database, private val lastFm: LastFmService, private val artistImagesStorage: ArtistImagesStorage)
: BasePresenter<ArtistsView>(), AnkoLogger {

    private val subscription = CompositeSubscription()

    override fun bind(view: ArtistsView) {
        super.bind(view)
        val artists = database.getArtists()
        view.showArtists(artists)
        artists.filter { it.imageUrl == null }.forEach { artist ->
            subscription.add(
                    lastFm.getArtistInfo(artist.name)
                            .subscribeOn(Schedulers.io())
                            .map { getArtistImageUrl(it) }
                            .subscribe({
                                artistImagesStorage.put(artist.name, it)
                                view.updateArtist(artist.withImage(it))
                            }, {
                                warn("Couldn't retrieve artist image url", it)
                            })
            )
        }
    }

    override fun unbind() {
        subscription.clear()
        super.unbind()
    }

    fun onArtistClicked(artist: Artist) {
        artist.toString()
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