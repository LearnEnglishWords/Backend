package com.learnenglish.services

import com.learnenglish.models.Word
import it.skrape.core.htmlDocument
import it.skrape.extract
import it.skrape.extractIt
import it.skrape.selects.and
import it.skrape.selects.eachText
import it.skrape.selects.html5.a
import it.skrape.selects.html5.div
import it.skrape.selects.html5.p
import it.skrape.selects.html5.span
import it.skrape.skrape
import javax.inject.Singleton

@Singleton
class ImportService(private val wordService: WordService) {

    fun importWord(word: String) {
        val extracted = skrape {
            url = "https://dictionary.cambridge.org/dictionary/english/$word"

            extractIt<Word> {
                htmlDocument {
                    span {  withClass = "eg" and "deg"
                        findAll {
                            it.examples = eachText().subList(0,6)
                        }
                    }
                    span {  withClass = "ipa" and "dipa" and "lpr-2" and "lpl-1"
                        it.pronunciation = findFirst { text }
                    }
                    span {  withClass = "hw" and "dhw"
                        it.text = findFirst { text }
                    }
                }
            }
        }
        print(extracted)
    }
}
