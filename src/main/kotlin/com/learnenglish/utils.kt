package com.learnenglish

fun String.parseList()
    = this.let { it.substring(2, it.length-2).split("|") }
