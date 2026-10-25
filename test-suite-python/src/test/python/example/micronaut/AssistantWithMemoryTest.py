from typing import Annotated

from jakarta.inject import Inject
from micronaut.context.annotation import Property
from micronaut.test.extensions.junit5.annotation import MicronautTest
from org.junit.jupiter.api import Test

from example.micronaut.AssistantWithMemory import AssistantWithMemory


# TODO(python): a Python test class cannot extend a Python base class, so unlike the Java suite (which runs this
# test against the in-memory, Redis, Neo4j and Cassandra chat memory stores) the Python test only covers the in-memory store.
@Property(name="langchain4j.chat-memory-store.neo4j.enabled", value="false")
@Property(name="langchain4j.chat-memory-store.cassandra.enabled", value="false")
@Property(name="langchain4j.chat-memory-store.redis.enabled", value="false")
@MicronautTest(startApplication=False, environments=["ollama"])
class AssistantWithMemoryTest:
    assistant: Annotated[AssistantWithMemory, Inject]

    # tag::test[]
    @Test
    def chat_with_memory(self):
        john_conversation = self.assistant.chat("Let me introduce myself. My name is John")
        john_conversation_id = john_conversation.memory_id
        assert john_conversation_id is not None
        aegon_conversation = self.assistant.chat("Let me introduce myself. My name is Dan")
        aegon_conversation_id = aegon_conversation.memory_id
        assert aegon_conversation_id is not None
        answer = self.assistant.chat("What's my name?", john_conversation_id)
        assert "john" in answer.response.lower(), answer.response
        answer = self.assistant.chat("What's my name?", aegon_conversation_id)
        assert "dan" in answer.response.lower(), answer.response
    # end::test[]
