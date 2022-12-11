package homework03

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import homework03.dto.CommentsSnapshot
import homework03.dto.JsonAboutWrapper
import homework03.dto.JsonPostsWrapper
import homework03.dto.TopicSnapshot
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*

object RedditClient {
    private val httpClient = HttpClient(CIO)
    private val objectMapper = ObjectMapper()
    private val mainURL = "https://www.reddit.com/r/"
    private val commPartURL = "/comments/"
    private val jsonPartURL = "/.json"
    private val aboutURL = "/about.json"

    init {
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
    }

    suspend fun getTopic(name: String): TopicSnapshot {
        return TopicSnapshot.get(getAboutTopic(name), getPosts(name))
    }

    private suspend fun getAboutTopic(name: String): JsonAboutWrapper.TopicAbout {
        val json = httpClient.get(mainURL + name + aboutURL).body<String>()
        return objectMapper.readValue(json, JsonAboutWrapper::class.java).data
    }

    private suspend fun getPosts(name: String): JsonPostsWrapper.JsonPosts {
        val json = httpClient.get(mainURL + name + jsonPartURL).body<String>()
        return objectMapper.readValue(json, JsonPostsWrapper::class.java).data
    }

    suspend fun getComments(url: String): CommentsSnapshot {
        val json = httpClient.get(url + jsonPartURL).body<String>()
        return CommentsSnapshot.parse(objectMapper, json)
    }

    suspend fun getComments(topicName: String, threadID: String): CommentsSnapshot {
        return getComments(mainURL + topicName + commPartURL + threadID + jsonPartURL)
    }
}