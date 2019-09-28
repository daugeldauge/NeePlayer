package com.neeplayer.ui.auth

import android.net.Uri
import com.neeplayer.RxSchedulers
import com.neeplayer.di.LastFmModule
import com.neeplayer.model.LastFmService
import com.neeplayer.model.Preferences
import com.neeplayer.model.Preferences.Item.BooleanItem.ScrobblingEnabled
import com.neeplayer.model.Preferences.Item.StringItem.LastFmAuthToken
import com.neeplayer.model.Preferences.Item.StringItem.SessionKey
import com.neeplayer.plusAssign
import com.neeplayer.ui.BasePresenter
import javax.inject.Inject

class AuthPresenter @Inject constructor(
        private val lastFm: LastFmService,
        private val preferences: Preferences,
        private val schedulers: RxSchedulers
): BasePresenter<AuthView>() {

    override fun bind(view: AuthView) {
        super.bind(view)
        updateMenuItems()
    }

    fun onResume() {
        subscriptions += lastFm.getSession(preferences.get(LastFmAuthToken) ?: return)
                .subscribeOn(schedulers.io())
                .observeOn(schedulers.mainThread())
                .subscribe({
                    preferences.put(SessionKey, it.getJSONObject("session").getString("key"))
                    updateMenuItems()
                    view.showAuthSuccess()
                    preferences.remove(LastFmAuthToken)
                }, {
                    view.showAuthError()
                })
    }

    fun onSignInClicked() {
        subscriptions += lastFm.getToken()
                .subscribeOn(schedulers.io())
                .observeOn(schedulers.mainThread())
                .subscribe({
                    val token = it.getString("token")
                    preferences.put(LastFmAuthToken, token)
                    view.showAuthView(Uri.parse("http://www.last.fm/api/auth/?api_key=${LastFmModule.apiKey}&token=$token"))
                }, {
                    view.showAuthError()
                })
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

    private fun updateMenuItems() {
        view.showMenuElements(if (preferences.get(SessionKey) != null) {
            setOf(AuthView.MenuElement.SCROBBLING, AuthView.MenuElement.SIGN_OUT)
        } else {
            setOf(AuthView.MenuElement.SIGN_IN)
        })
    }
}