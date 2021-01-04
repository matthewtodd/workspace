package org.matthewtodd.wake.test

@Target(AnnotationTarget.FUNCTION)
annotation class Test

@Target(AnnotationTarget.FUNCTION)
annotation class Ignore(val message: String = "Ignored.")
