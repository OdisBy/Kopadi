package com.odisby.kopadi.sample

import android.app.Application
import androidx.lifecycle.ViewModelProvider
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.android.x.androidXModule
import org.kodein.di.bindSingleton

class App : Application(), DIAware {
    override val di = DI.lazy {
        import(androidXModule(this@App))

        bindSingleton<ViewModelProvider.Factory> { KodeinViewModelFactory(di) }
    }
}