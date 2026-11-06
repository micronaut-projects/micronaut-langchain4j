from abc import ABC, abstractmethod

from dev.langchain4j.service import SystemMessage
from java.util.concurrent import CompletableFuture
from micronaut.langchain4j.annotation import AiService
from org.reactivestreams import Publisher


@AiService
class AsyncFriend(ABC):

    @SystemMessage("You are a good friend of mine. Answer using slang.")
    @abstractmethod
    def chat(self, user_message: str) -> CompletableFuture[str]:  # <1>
        ...

    @SystemMessage("You are a good friend of mine. Answer using slang.")
    @abstractmethod
    def stream(self, user_message: str) -> Publisher[str]:  # <2>
        ...
