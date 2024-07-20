package com.odisby.kopadi.sample

import android.app.Application
import androidx.lifecycle.ViewModelProvider
import com.odisby.kopadi.sample.ui.test.allModules
import com.odisby.kopadi.sample.ui.test.repository.GoodApi
import com.odisby.kopadi.sample.ui.test.repository.GoodApiImpl
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.android.x.androidXModule
import org.kodein.di.bindProvider
import org.kodein.di.bindSingleton

class App : Application(), DIAware {
    override val di = DI.lazy {
        import(androidXModule(this@App))

        bindSingleton<ViewModelProvider.Factory> { KodeinViewModelFactory(di) }

        bindProvider<GoodApi> { GoodApiImpl() }

        import(allModules)
    }
}