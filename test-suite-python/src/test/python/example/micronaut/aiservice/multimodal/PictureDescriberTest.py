from typing import Annotated

from dev.langchain4j.data.message import AiMessage, ImageContent, PdfFileContent, TextContent
from dev.langchain4j.model.chat import ChatModel
from dev.langchain4j.model.chat.request import ChatRequest
from dev.langchain4j.model.chat.response import ChatResponse
from jakarta.inject import Inject, Singleton
from java.net import URI
from micronaut.context.annotation import Primary, Property, Requires
from micronaut.test.extensions.junit5.annotation import MicronautTest
from org.junit.jupiter.api import Test

from example.micronaut.aiservice.multimodal.PictureDescriber import PictureDescriber


@Property(name="spec.name", value="PictureDescriberTest")
@Property(name="langchain4j.ollama.enabled", value="false")
@MicronautTest(startApplication=False)
class PictureDescriberTest:
    describer: Annotated[PictureDescriber, Inject]

    @Test
    def sends_the_picture(self):
        assert self.describer.describe("What is it?", "https://example.com/cat.png") == "text:What is it?|image:https://example.com/cat.png"

    @Test
    def sends_the_document(self):
        assert self.describer.summarize("risks", URI.create("https://example.com/report.pdf")) == "text:Summarize the risks of this document|pdf:https://example.com/report.pdf"


# Answers with the contents of the user message.
@Singleton
@Primary
@Requires(property="spec.name", value="PictureDescriberTest")
class EchoChatModel(ChatModel):

    def doChat(self, request: ChatRequest) -> ChatResponse:
        messages = request.messages()
        contents = messages.get(messages.size() - 1).contents()
        described = []
        for content in contents:
            if isinstance(content, TextContent):
                described.append("text:" + content.text())
            elif isinstance(content, ImageContent):
                described.append("image:" + str(content.image().url()))
            elif isinstance(content, PdfFileContent):
                described.append("pdf:" + str(content.pdfFile().url()))
        return ChatResponse.builder().aiMessage(AiMessage.from_("|".join(described))).build()
