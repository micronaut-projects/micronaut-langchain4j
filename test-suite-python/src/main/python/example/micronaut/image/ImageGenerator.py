from dev.langchain4j.model.image import ImageModel
from jakarta.inject import Singleton
from java.net import URI


@Singleton
class ImageGenerator:

    def __init__(self, image_model: ImageModel):  # <1>
        self.image_model = image_model

    def generate(self, prompt: str) -> URI:
        image = self.image_model.generate(prompt).content()  # <2>
        return image.url()
