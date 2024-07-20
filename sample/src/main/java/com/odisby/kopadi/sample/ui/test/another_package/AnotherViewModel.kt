package com.odisby.kopadi.sample.ui.test.another_package

import androidx.lifecycle.ViewModel
import com.odisby.kopadi.sample.ui.test.repository.SomeGoodRepository
import javax.inject.Inject

class AnotherViewModel @Inject constructor(
    private val someGoodRepository: SomeGoodRepository
) : ViewModel() {
    init {
        someGoodRepository.getSomething()
    }
}