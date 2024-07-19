package com.odisby.kopadi.annotations

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
@MustBeDocumented
annotation class RetrofitInjector(val importOnce: Boolean = false)