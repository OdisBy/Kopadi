package com.odisby.kopadi.sample.ui.test.insidetest

import android.util.Log
import com.odisby.kopadi.sample.BaseViewModel
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TestingInject @Inject constructor()

class MainViewModel @Inject constructor(
    private val testingInject: TestingInject,
    private val testingInject2: TestingInject
) : BaseViewModel() {
    fun displayInjectedValue(): String {
        return "asas"
    }

    init {
        Log.d("Testing", "asas")
    }
}
