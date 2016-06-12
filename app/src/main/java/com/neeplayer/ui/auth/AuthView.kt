package com.neeplayer.ui.auth

import android.net.Uri

interface AuthView {

    enum class MenuElement {
        SIGN_IN, SIGN_OUT, SCROBBLING
    }

    fun showAuthSuccess()
    fun showAuthError()
    fun showMenuElements(items: Set<MenuElement>)
    fun setScrobblingChecked(checked: Boolean)
    fun showAuthView(uri: Uri)

}