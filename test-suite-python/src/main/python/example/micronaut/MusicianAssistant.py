from dev.langchain4j.data.message import SystemMessage, UserMessage
from dev.langchain4j.model.chat import ChatModel
from jakarta.inject import Singleton
from java.util import List

from .Musician import Musician

SYSTEM_MSG = SystemMessage.from_("""\
      You are an expert in Jazz music.
      Reply with only the names of the artists, albums, etc.
      Be very concise.
      If a list is given, separate the items with commas.""")


@Singleton
class MusicianAssistant:

    def __init__(self, model: ChatModel):  # <1>
        self.model = model

    def generate_top_three_albums(self, name: str) -> Musician:
        messages = generate_top_three_albums_messages(name)
        albums = self.model.chat(messages)
        top_three_albums = albums.aiMessage().text()
        return Musician(name, top_three_albums)


def generate_top_three_albums_messages(name: str):
    return List.of(SYSTEM_MSG, UserMessage.from_(
        f"Only list the top 3 albums of {name}"
    ))
