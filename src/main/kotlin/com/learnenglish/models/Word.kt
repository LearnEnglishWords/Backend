package com.learnenglish.models

import com.fasterxml.jackson.databind.ObjectMapper
import com.learnenglish.parseList
import javax.validation.constraints.NotBlank
import javax.validation.constraints.Size



enum class WordState {
    IMPORT, PARSE, AUTO_PARSE, CORRECT
}

data class Word(
    var collectionId: Long? = null,
    @get:NotBlank(message = "Text is required")
    @get:Size(min = 1, max = 50)
    var text: String = "",
    @get:Size(min = 1, max = 50)
    var pronunciation: Map<String, String> = mapOf(),
    var state: WordState = WordState.IMPORT,
    var rank: Long = 0,
    var sense: List<String> = listOf(),
    var examples: List<String> = listOf()
) : BaseModel(id = null) {

    companion object {
        fun parse(map: Map<String, Any?>): Word {
            val mapper = ObjectMapper()
            return Word().apply {
                id = (map["id"] as Int).toLong()
                collectionId = (map["collection_id"] as Int?)?.toLong()
                text = map["text"] as String
                pronunciation = mapper.readValue(map["pronunciation"] as String, Map::class.java) as Map<String, String>
                state = WordState.valueOf(map["state"] as String)
                rank = (map["rank"] as Int).toLong()
                sense = (map["sense"] as String).parseList()
                examples = (map["examples"] as String).parseList()
            }
        }
    }
}