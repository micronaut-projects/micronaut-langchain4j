package example.micronaut;

import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.response.ChatResponse;
import jakarta.inject.Singleton;

import java.util.List;

@Singleton
public class MusicianAssistant {
    private static final SystemMessage SYSTEM_MSG = SystemMessage.from("""
      You are an expert in Jazz music.
      Reply with only the names of the artists, albums, etc.
      Be very concise.
      If a list is given, separate the items with commas.""");

    private final ChatModel model;

    public MusicianAssistant(ChatModel model) { // <1>
        this.model = model;
    }

    public Musician generateTopThreeAlbums(String name) {
        List<ChatMessage> messages = generateTopThreeAlbumsMessages(name);
        ChatResponse albums = model.chat(messages);
        String topThreeAlbums = albums.aiMessage().text();
        return new Musician(name, topThreeAlbums);
    }

    private static List<ChatMessage> generateTopThreeAlbumsMessages(String name) {
        return List.of(SYSTEM_MSG, UserMessage.from(
            String.format("Only list the top 3 albums of %s", name)
        ));
    }
}
