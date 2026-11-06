from dev.langchain4j.data.image import Image
from dev.langchain4j.model.image import ImageModel
from jakarta.inject import Singleton


@Singleton
class ImageGenerator:

    def __init__(self, image_model: ImageModel):  # <1>
        self.image_model = image_model

    def generate(self, prompt: str) -> Image:
        return self.image_model.generate(prompt).content()  # <2>
