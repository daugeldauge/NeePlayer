package com.neeplayer.ui

import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel

open class CoroFragment(@LayoutRes contentLayoutId: Int) : Fragment(contentLayoutId) {
    protected val viewScope = MainScope()

    override fun onDestroyView() {
        viewScope.cancel()
        super.onDestroyView()
    }
}