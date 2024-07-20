package com.odisby.kopadi.sample

import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.android.x.closestDI
import org.kodein.di.instance

abstract class BaseFragment : Fragment(), DIAware {
    override val di: DI by closestDI()

    protected val viewModelFactory: ViewModelProvider.Factory by instance()

}
