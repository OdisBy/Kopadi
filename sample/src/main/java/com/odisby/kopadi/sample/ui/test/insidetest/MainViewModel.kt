package com.odisby.kopadi.sample.ui.test.insidetest

import android.util.Log
import com.odisby.kopadi.sample.BaseViewModel
import com.odisby.kopadi.sample.ui.test.repository.SomeGoodRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TestingInject @Inject constructor()

class MainViewModel @Inject constructor(
    goodRepository: SomeGoodRepository
) : BaseViewModel() {
    fun displayInjectedValue(): String {
        return "asas"
    }

    init {
        Log.d("Ruliam", goodRepository.getSomething())
    }
}
