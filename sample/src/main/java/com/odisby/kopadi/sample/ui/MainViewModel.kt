package com.odisby.kopadi.sample.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import com.odisby.kopadi.annotations.Testing
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TestingInject @Inject constructor(val testString: String = "Testing")

class MainViewModel @Inject constructor(
    private val testingInject: TestingInject
) : ViewModel() {
    fun displayInjectedValue(): String {
        return testingInject.testString
    }

    init {
        Log.d("Testing", testingInject.testString)
    }
}
