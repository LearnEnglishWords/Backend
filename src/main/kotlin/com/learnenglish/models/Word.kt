package com.learnenglish.models

import com.learnenglish.parseList
import javax.validation.constraints.NotBlank
import javax.validation.constraints.Size


enum class WordState {
    IMPORT, PARSE, CORRECT
}

data class Word(
    @get:NotBlank(message = "Text is required")
    @get:Size(min = 1, max = 50)
    var text: String = "",
    @get:NotBlank(message = "Pronunciation is required")
    @get:Size(min = 1, max = 50)
    var pronunciation: String = "",
    var state: WordState = WordState.IMPORT,
    var sense: List<String> = listOf(),
    var examples: List<String> = listOf()
) : BaseModel(id = null) {

    companion object {
        fun parse(map: Map<String, Any?>): Word {
            return Word().apply {
                id = (map["id"] as Int).toLong()
                text = map["text"] as String
                pronunciation = map["pronunciation"] as String
                state = WordState.valueOf(map["state"] as String)
                sense = (map["sense"] as String).parseList()
                examples = (map["examples"] as String).parseList()
            }
        }
    }
}