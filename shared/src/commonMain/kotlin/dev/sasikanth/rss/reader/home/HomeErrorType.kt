package dev.sasikanth.rss.reader.home

sealed interface HomeErrorType {
  object UnknownFeedType : HomeErrorType

  object FailedToParseXML : HomeErrorType

  object Timeout : HomeErrorType

  data class Unknown(val e: Exception) : HomeErrorType
}