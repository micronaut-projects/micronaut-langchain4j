package example.micronaut.image

import dev.langchain4j.data.image.Image
import dev.langchain4j.model.image.ImageModel
import jakarta.inject.Singleton

@Singleton
class ImageGenerator {

    private final ImageModel imageModel

    ImageGenerator(ImageModel imageModel) { // <1>
        this.imageModel = imageModel
    }

    URI generate(String prompt) {
        Image image = imageModel.generate(prompt).content() // <2>
        image.url()
    }
}
