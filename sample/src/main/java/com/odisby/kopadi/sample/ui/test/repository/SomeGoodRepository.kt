package com.odisby.kopadi.sample.ui.test.repository

import javax.inject.Inject

class SomeGoodRepository @Inject constructor(
    private val someGoodApi: GoodApi,
) {
    fun getSomething(): String {
        return someGoodApi.getSomething()
    }
}