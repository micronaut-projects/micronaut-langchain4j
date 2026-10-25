package example.micronaut

import dev.langchain4j.data.message.ChatMessage
import dev.langchain4j.data.message.SystemMessage
import dev.langchain4j.data.message.UserMessage
import dev.langchain4j.model.chat.ChatModel
import jakarta.inject.Singleton

@Singleton
class MusicianAssistant(private val model: ChatModel) { // <1>
    fun generateTopThreeAlbums(name: String): Musician {
        val messages = generateTopThreeAlbumsMessages(name)
        val albums = model.chat(messages)
        val topThreeAlbums = albums.aiMessage().text()
        return Musician(name, topThreeAlbums)
    }

    private fun generateTopThreeAlbumsMessages(name: String): List<ChatMessage> {
        return listOf(
            SYSTEM_MSG, UserMessage.from(
                String.format("Only list the top 3 albums of %s", name)
            )
        )
    }

    companion object {
        private val SYSTEM_MSG: SystemMessage = SystemMessage.from(
            """
          You are an expert in Jazz music.
          Reply with only the names of the artists, albums, etc.
          Be very concise.
          If a list is given, separate the items with commas.
          """.trimIndent()
        )
    }
}
