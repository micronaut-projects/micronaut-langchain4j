from typing import Annotated

from jakarta.inject import Inject
from micronaut.test.extensions.junit5.annotation import MicronautTest
from org.junit.jupiter.api import Test

from example.micronaut.MusicianAssistant import MusicianAssistant


@MicronautTest(startApplication=False, environments=["ollama"])
class MusicianAssistantTest:
    assistant: Annotated[MusicianAssistant, Inject]

    @Test
    def should_generate_musician_top_three_albums(self):
        musician = self.assistant.generate_top_three_albums("Miles Davis")
        assert "kind of blue" in musician.albums.lower()
