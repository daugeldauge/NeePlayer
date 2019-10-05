package com.neeplayer.ui.auth

import android.net.Uri
import com.neeplayer.model.Preferences
import com.neeplayer.model.Preferences.Item.BooleanItem.ScrobblingEnabled
import com.neeplayer.model.Preferences.Item.StringItem.LastFmAuthToken
import com.neeplayer.model.Preferences.Item.StringItem.SessionKey
import com.neeplayer.network.Response
import com.neeplayer.network.lastfm.LastFmApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

class AuthPresenter @Inject constructor(
        private val lastFm: LastFmApi,
        private val preferences: Preferences
) {

    fun bind(scope: CoroutineScope, view: AuthView) {
        with(scope) {
            launch { view.signInClicks.collect { requestLastFmToken(view) } }
            launch { view.signOutClicks.collect { signOut(view) } }
            launch { view.resumes.collect { tryToCompleteAuthorization(view) } }
            launch { view.scrobbleToggles.collect { toggleScrobbling(view) } }
        }

        updateMenuItems(view)
    }

    private suspend fun tryToCompleteAuthorization(view: AuthView) {
        val token = preferences.get(LastFmAuthToken) ?: return
        when (val result = lastFm.getSession(token)) {
            is Response.Success<LastFmApi.GetSessionBody> -> {
                preferences.put(SessionKey, result.body.session.key)
                updateMenuItems(view)
                view.showAuthSuccess()
                preferences.remove(LastFmAuthToken)
            }
            is Response.Error -> view.showAuthError()
        }
    }

    private suspend fun requestLastFmToken(view: AuthView) {
        when (val result = lastFm.getToken()) {
            is Response.Success<LastFmApi.GetTokenBody> -> {
                val token = result.body.token
                preferences.put(LastFmAuthToken, token)
                view.showAuthView(Uri.parse("http://www.last.fm/api/auth/?api_key=${LastFmApi.apiKey}&token=$token"))
            }
            is Response.Error -> view.showAuthError()
        }
    }

    private fun signOut(view: AuthView) {
        preferences.remove(SessionKey)
        updateMenuItems(view)
    }

    private fun toggleScrobbling(view: AuthView) {
        val scrobbling = !preferences.getOrDefault(ScrobblingEnabled)
        preferences.put(ScrobblingEnabled, scrobbling)
        view.setScrobblingChecked(scrobbling)
    }

    private fun updateMenuItems(view: AuthView) {
        view.showMenuElements(if (preferences.get(SessionKey) != null) {
            setOf(AuthView.MenuElement.SCROBBLING, AuthView.MenuElement.SIGN_OUT)
        } else {
            setOf(AuthView.MenuElement.SIGN_IN)
        })
    }
}