from dev.langchain4j.data.message import UserMessage
from dev.langchain4j.memory.chat import MessageWindowChatMemory
from dev.langchain4j.model.chat import ChatModel
from jakarta.inject import Singleton
from java.util import UUID
from java.util.concurrent import ConcurrentHashMap

from .MemoryIdAndResponse import MemoryIdAndResponse


@Singleton
class AssistantWithMemory:

    def __init__(self, message_window_chat_memory_builder: MessageWindowChatMemory.Builder,
                 model: ChatModel):
        self.conversations = ConcurrentHashMap()
        self.message_window_chat_memory_builder = message_window_chat_memory_builder
        self.model = model

    def chat(self, message: str, conversation_id: str | None = None) -> MemoryIdAndResponse:
        if conversation_id is None:
            conversation_id = self.start_conversation()
        chat_memory = self.conversations.get(conversation_id)
        if chat_memory is None:
            raise ValueError(f"Unknown conversation: {conversation_id}")
        chat_memory.add(UserMessage.from_(message))
        chat_response = self.model.chat(chat_memory.messages())
        answer = chat_response.aiMessage()
        chat_memory.add(answer)
        return MemoryIdAndResponse(conversation_id, answer.text())

    def start_conversation(self) -> str:
        memory_id = self.generate_chat_memory_id()
        chat_memory = self.generate_chat_memory(memory_id)
        self.conversations.putIfAbsent(memory_id, chat_memory)
        return memory_id

    def generate_chat_memory_id(self) -> str:
        return UUID.randomUUID().toString()

    def generate_chat_memory(self, memory_id: str):
        return (self.message_window_chat_memory_builder
                .id(memory_id)
                .build())
