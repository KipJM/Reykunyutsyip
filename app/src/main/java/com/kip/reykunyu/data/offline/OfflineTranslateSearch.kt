package com.kip.reykunyu.data.offline

import com.kip.reykunyu.data.dict.*
import me.xdrop.fuzzywuzzy.FuzzySearch
import kotlin.math.abs


const val relevanceLimit = 85
class OfflineTranslateSearch : TranslateSearchProvider {


    override suspend fun search(query: String, language: Language): TranslateSearchResult {
        if (OfflineDictionary.dictionary == null) {
            //Cancels search if dictionary is not here. This (hopefully) should be unreachable code!
            return TranslateSearchResult(SearchResultStatus.Error, emptyList(), emptyList())
        }
        val dictionary = OfflineDictionary.safeDictionary

        //Split query if Na'vi sentence
        val naviWords = splitNaviSentence(query)

        val fromNaviResults = naviWords.map{ word ->
            Pair(word, naviSearch(dictionary, word))
        }

        // =>Na'vi search
        val toNaviSearch = FuzzySearch.extractSorted(
            query,
            dictionary.indexedTranslations,
            { it.first },
            relevanceLimit
        ).map { it.referent.second }.distinct()

        val toNaviResults = toNaviSearch.map { dictionary.indexedNavi[it].toNavi() }

        return TranslateSearchResult(SearchResultStatus.Success, fromNaviResults, toNaviResults)
    }

    private fun splitNaviSentence(query: String): List<String> {
        val splitSentecne = query.split(' ')

        val wordList = mutableListOf<String>()

        splitSentecne.forEachIndexed{ index, element ->
            //"... si" is a single word
            //TODO: ADD MORE
            if (index > 0 && element == "si" && splitSentecne[index - 1] != "si")
            {
                wordList[wordList.size - 1] = wordList[wordList.size - 1] + " " + element
            }
            else
            {
                wordList.add(element)
            }
        }

        return wordList
    }


    fun naviSearch(dictionary: NaviDictionary, query: String): List<Navi> {
        // Na'vi=> search
        var fromNaviSearch = FuzzySearch.extractSorted(
            query,
            dictionary.indexedNavi.map { it.word },
            relevanceLimit
        ).map { Pair(it, it.score) }.toMutableList()

        // Adjusted with weights for word length, and starting character.
        fromNaviSearch.forEachIndexed() { indexed, it ->
            var newScore = it.second
            //Word length
            newScore -= abs(it.first.string.length - query.length) * 2

            //Starting characters
            var index = 0
            for (character in query) {
                if (it.first.string.length - 1 < index) {
                    break
                }
                if (it.first.string[index] != character){
                    break
                }
                index ++
            }
            newScore += index * 6 //More weight!

            fromNaviSearch[indexed] = Pair(it.first, newScore)
        }
        fromNaviSearch = fromNaviSearch.sortedByDescending { it.second }.toMutableList()


        return fromNaviSearch.map { dictionary.indexedNavi[it.first.index].toNavi() }
    }

}