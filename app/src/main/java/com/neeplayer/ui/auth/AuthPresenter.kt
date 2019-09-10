package com.neeplayer.ui.auth

import android.net.Uri
import com.neeplayer.RxSchedulers
import com.neeplayer.di.LastFmModule
import com.neeplayer.model.LastFmService
import com.neeplayer.model.Preferences
import com.neeplayer.model.Preferences.Item.BooleanItem.SCROBBLING
import com.neeplayer.model.Preferences.Item.StringItem.SESSION_KEY
import com.neeplayer.plusAssign
import com.neeplayer.ui.BasePresenter
import icepick.State
import javax.inject.Inject

class AuthPresenter @Inject constructor(
        private val lastFm: LastFmService,
        private val preferences: Preferences,
        private val schedulers: RxSchedulers
): BasePresenter<AuthView>() {

    @State
    @JvmField
    internal var token: String? = null

    override fun bind(view: AuthView) {
        super.bind(view)
        updateMenuItems()
    }

    fun onResume() {
        subscriptions += lastFm.getSession(token ?: return)
                .subscribeOn(schedulers.io())
                .observeOn(schedulers.mainThread())
                .subscribe({
                    preferences.put(SESSION_KEY, it.getJSONObject("session").getString("key"))
                    updateMenuItems()
                    view.showAuthSuccess()
                    token = null
                }, {
                    view.showAuthError()
                })
    }

    fun onSignInClicked() {
        subscriptions += lastFm.getToken()
                .subscribeOn(schedulers.io())
                .observeOn(schedulers.mainThread())
                .subscribe({
                    token = it.getString("token")
                    view.showAuthView(Uri.parse("http://www.last.fm/api/auth/?api_key=${LastFmModule.apiKey}&token=$token"))
                }, {
                    view.showAuthError()
                })
    }

    fun onSignOutClicked() {
        preferences.remove(SESSION_KEY)
        updateMenuItems()
    }

    fun onScrobblingToggled() {
        val scrobbling = !preferences.getOrDefault(SCROBBLING)
        preferences.put(SCROBBLING, scrobbling)
        view.setScrobblingChecked(scrobbling)
    }

    private fun updateMenuItems() {
        view.showMenuElements(if (preferences.get(SESSION_KEY) != null) {
            setOf(AuthView.MenuElement.SCROBBLING, AuthView.MenuElement.SIGN_OUT)
        } else {
            setOf(AuthView.MenuElement.SIGN_IN)
        })
    }
}