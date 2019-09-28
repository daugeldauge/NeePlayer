package com.neeplayer.di

import android.content.Context
import androidx.fragment.app.Fragment
import com.neeplayer.NeePlayerApp
import com.neeplayer.ui.MainActivity

val androidx.fragment.app.Fragment.component: ActivityComponent
    get() = (activity as MainActivity).activityComponent

val Context.component: AppComponent
    get() = (applicationContext as NeePlayerApp).appComponent