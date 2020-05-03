package com.learnenglish.models

import java.time.LocalDateTime


data class Activity (
    var uuid: String? = null,
    var timestamp: LocalDateTime = LocalDateTime.now()
)