package example.micronaut.image

import dev.langchain4j.data.image.Image
import dev.langchain4j.model.image.ImageModel
import jakarta.inject.Singleton

@Singleton
class ImageGenerator(private val imageModel: ImageModel) { // <1>

    fun generate(prompt: String): Image =
        imageModel.generate(prompt).content() // <2>
}
