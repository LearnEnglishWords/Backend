package com.learnenglish.models

import com.learnenglish.parseList
import javax.validation.constraints.NotBlank
import javax.validation.constraints.Size

data class Word(
    @get:NotBlank(message = "Text is required")
    @get:Size(min = 3, max = 50)
    var text: String = "",
    @get:NotBlank(message = "Pronunciation is required")
    @get:Size(min = 3, max = 50)
    var pronunciation: String = "",
    var sense: List<String> = listOf(),
    var examples: List<String> = listOf()
) : BaseModel(id = null) {

    companion object {
        fun parse(map: Map<String, Any?>): Word {
            return Word().apply {
                id = (map["id"] as Int).toLong()
                text = map["text"] as String
                pronunciation = map["pronunciation"] as String
                sense = (map["sense"] as String).parseList()
                examples = (map["examples"] as String).parseList()
            }
        }
    }
}