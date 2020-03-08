package com.learnenglish

import io.micronaut.runtime.Micronaut

object Application {

    @JvmStatic
    fun main(args: Array<String>) {
        Micronaut.build()
                .packages("com.learnenglish")
                .mainClass(Application.javaClass)
                .start()
    }
}