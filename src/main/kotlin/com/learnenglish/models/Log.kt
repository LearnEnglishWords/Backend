package com.learnenglish.models

import java.time.LocalDateTime


data class Log (
    var uuid: String? = null,
    var timestamp: LocalDateTime = LocalDateTime.now(),
    var message: String? = null
)