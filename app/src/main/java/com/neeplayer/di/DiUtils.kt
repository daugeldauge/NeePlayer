package com.neeplayer.di

import android.content.Context
import android.support.v4.app.Fragment
import com.neeplayer.NeePlayerApp
import com.neeplayer.ui.MainActivity

val Fragment.component: ActivityComponent
    get() = (activity as MainActivity).activityComponent

val Context.component: AppComponent
    get() = (applicationContext as NeePlayerApp).appComponent