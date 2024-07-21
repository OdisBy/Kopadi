package com.odisby.kopadi.sample.ui.test.another_package

import androidx.lifecycle.ViewModel
import com.odisby.kopadi.sample.ui.test.repository.AnotherGoodRepository
import com.odisby.kopadi.sample.ui.test.repository.SomeGoodRepository
import javax.inject.Inject

class AnotherViewModel @Inject constructor(
    someGoodRepository4: SomeGoodRepository,
    anotherGoodRepository: AnotherGoodRepository
) : ViewModel() {
    init {
        someGoodRepository4.getSomething()
        anotherGoodRepository.getSomething()
    }
}