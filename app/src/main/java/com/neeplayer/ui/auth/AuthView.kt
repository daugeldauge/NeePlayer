package com.neeplayer.ui.auth

import android.net.Uri
import kotlinx.coroutines.flow.Flow

interface AuthView {

    enum class MenuElement {
        SIGN_IN, SIGN_OUT, SCROBBLING
    }

    val signInClicks: Flow<*>
    val signOutClicks: Flow<*>
    val scrobbleToggles: Flow<*>
    val resumes: Flow<*>

    fun showAuthSuccess()
    fun showAuthError()
    fun showMenuElements(items: Set<MenuElement>)
    fun setScrobblingChecked(checked: Boolean)
    fun showAuthView(uri: Uri)
}