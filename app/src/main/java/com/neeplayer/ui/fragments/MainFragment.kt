package com.neeplayer.ui.fragments

import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.*
import com.neeplayer.LastFmService
import com.neeplayer.NeePlayerApp
import com.neeplayer.Preferences
import com.neeplayer.R
import com.neeplayer.model.Artist
import com.neeplayer.model.Database
import com.neeplayer.ui.activities.MainActivity
import com.neeplayer.ui.adapters.ArtistAdapter
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.toast
import org.jetbrains.anko.warn
import org.json.JSONObject
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import javax.inject.Inject

// TODO Refactor to MVP
class MainFragment : Fragment(), AnkoLogger {
    private val artistImages = mutableMapOf<Artist, String>()

    private var token: String? = null

    private val savedArtistImages: SharedPreferences by lazy {
        activity.getSharedPreferences("ArtistImages", 0)
    }

    @Inject
    lateinit internal var lastFm: LastFmService

    @Inject
    lateinit internal var preferences: Preferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        NeePlayerApp.component!!.inject(this)

        if (preferences.get(Preferences.Item.SESSION_KEY) != null) {
            return
        }

        lastFm.getToken().subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    token = it.getString("token")

                    val intent = Intent(Intent.ACTION_VIEW)
                    intent.data = Uri.parse("http://www.last.fm/api/auth/?api_key=${LastFmService.apiKey}&token=$token")
                    startActivity(intent)
                }, {
                    context.toast(R.string.last_fm_auth_error)
                })

    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater?.inflate(R.layout.fragment_main, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val recyclerView = view as RecyclerView

        recyclerView.adapter = ArtistAdapter(activity, getArtistList(), artistImages) {
            (activity as MainActivity).navigateToArtistFragment(it)
        }
        recyclerView.layoutManager = LinearLayoutManager(activity)

    }

    override fun onResume() {
        super.onResume()
        val token = token ?: return

        lastFm.getSession(token)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                preferences.put(Preferences.Item.SESSION_KEY, it.getJSONObject("session").getString("key"))
                context.toast(R.string.last_fm_auth_success)
            }, {
                context.toast(R.string.last_fm_auth_error)
            })
    }

    private fun getArtistList(): List<Artist> {

        val list = Database.getArtists()

        list.forEach { artist ->
            val image = savedArtistImages.getString(artist.name, null)
            if (image.isNullOrEmpty()) {
                lastFm.getArtistInfo(artist.name)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .map {
                            getArtistImageUrl(it)
                        }
                        .subscribe({
                            val retrievedImage = it ?: return@subscribe

                            artistImages.put(artist, retrievedImage)
                            savedArtistImages.edit().putString(artist.name, retrievedImage).apply()
                        }, {
                            warn("Couldn't retrieve artist image url", it)
                        })
            } else {
                artistImages.put(artist, image)
            }
        }

        return list
    }

    private fun getArtistImageUrl(artistJsonInfo: JSONObject): String? {
        val images = artistJsonInfo.getJSONObject("artist").getJSONArray("image")

        for (i in 0 until images.length()) {
            val image = images.getJSONObject(i)
            val size = image.getString("size")
            if (size == "extralarge") {
                return image.getString("#text")
            }
        }

        warn("Invalid json")
        return null
    }

}