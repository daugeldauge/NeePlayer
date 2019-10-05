package com.neeplayer.ui.auth

import android.net.Uri
import com.neeplayer.network.Response
import com.neeplayer.network.lastfm.LastFmApi
import com.neeplayer.model.Preferences
import com.neeplayer.model.Preferences.Item.BooleanItem.ScrobblingEnabled
import com.neeplayer.model.Preferences.Item.StringItem.LastFmAuthToken
import com.neeplayer.model.Preferences.Item.StringItem.SessionKey
import com.neeplayer.ui.BasePresenter
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import javax.inject.Inject

class AuthPresenter @Inject constructor(
        private val lastFm: LastFmApi,
        private val preferences: Preferences
): BasePresenter<AuthView>() {

    override fun bind(view: AuthView) {
        super.bind(view)
        updateMenuItems()
    }

    fun onResume() {
        val token = preferences.get(LastFmAuthToken) ?: return
        mainScope.launch {
            when (val result = lastFm.getSession(token)) {
                is Response.Success<LastFmApi.GetSessionBody> -> {
                    preferences.put(SessionKey, result.body.session.key)
                    updateMenuItems()
                    view.showAuthSuccess()
                    preferences.remove(LastFmAuthToken)
                }
                is Response.Error -> view.showAuthError()
            }
        }
    }

    fun onSignInClicked() {
        mainScope.launch {
            when (val result = lastFm.getToken()) {
                is Response.Success<LastFmApi.GetTokenBody> -> {
                    val token = result.body.token
                    preferences.put(LastFmAuthToken, token)
                    view.showAuthView(Uri.parse("http://www.last.fm/api/auth/?api_key=${LastFmApi.apiKey}&token=$token"))
                }
                is Response.Error -> view.showAuthError()
            }
        }
    }

    fun onSignOutClicked() {
        preferences.remove(SessionKey)
        updateMenuItems()
    }

    fun onScrobblingToggled() {
        val scrobbling = !preferences.getOrDefault(ScrobblingEnabled)
        preferences.put(ScrobblingEnabled, scrobbling)
        view.setScrobblingChecked(scrobbling)
    }

    override fun unbind() {
        super.unbind()
        mainScope.cancel()
    }

    private fun updateMenuItems() {
        view.showMenuElements(if (preferences.get(SessionKey) != null) {
            setOf(AuthView.MenuElement.SCROBBLING, AuthView.MenuElement.SIGN_OUT)
        } else {
            setOf(AuthView.MenuElement.SIGN_IN)
        })
    }
}