from abc import ABC, abstractmethod
from typing import Annotated

from dev.langchain4j.service import SystemMessage, UserMessage, V
from java.net import URI
from micronaut.langchain4j.annotation import AiService, ImageUrl, PdfUrl


@AiService
class PictureDescriber(ABC):

    @SystemMessage("You describe pictures for visually impaired people")
    @abstractmethod
    def describe(self, question: Annotated[str, UserMessage],
                 picture: Annotated[str, UserMessage, ImageUrl]) -> str:  # <1>
        ...

    @UserMessage("Summarize the {{topic}} of this document")
    @abstractmethod
    def summarize(self, topic: Annotated[str, V("topic")],
                  document: Annotated[URI, UserMessage, PdfUrl]) -> str:  # <2>
        ...
