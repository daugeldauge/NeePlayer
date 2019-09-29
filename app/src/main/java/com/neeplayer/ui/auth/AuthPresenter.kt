package com.neeplayer.ui.auth

import android.net.Uri
import com.neeplayer.api.lastfm.LastFmApi
import com.neeplayer.di.LastFmModule
import com.neeplayer.model.Preferences
import com.neeplayer.model.Preferences.Item.BooleanItem.ScrobblingEnabled
import com.neeplayer.model.Preferences.Item.StringItem.LastFmAuthToken
import com.neeplayer.model.Preferences.Item.StringItem.SessionKey
import com.neeplayer.ui.BasePresenter
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import javax.inject.Inject

class AuthPresenter @Inject constructor(
        private val lastFm: LastFmApi,
        private val preferences: Preferences
): BasePresenter<AuthView>() {

    private val mainScope = MainScope()

    override fun bind(view: AuthView) {
        super.bind(view)
        updateMenuItems()
    }

    fun onResume() {
        val token = preferences.get(LastFmAuthToken) ?: return
        mainScope.launch {
            when (val result = lastFm.getSession(token)) {
                is LastFmApi.Result.Success<LastFmApi.GetSessionResponse> -> {
                    preferences.put(SessionKey, result.data.session.key)
                    updateMenuItems()
                    view.showAuthSuccess()
                    preferences.remove(LastFmAuthToken)
                }
                is LastFmApi.Result.Error -> view.showAuthError()
            }
        }
    }

    fun onSignInClicked() {
        mainScope.launch {
            when (val result = lastFm.getToken()) {
                is LastFmApi.Result.Success<LastFmApi.GetTokenResponse> -> {
                    val token = result.data.token
                    preferences.put(LastFmAuthToken, token)
                    view.showAuthView(Uri.parse("http://www.last.fm/api/auth/?api_key=${LastFmModule.apiKey}&token=$token"))
                }
                is LastFmApi.Result.Error -> view.showAuthError()
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