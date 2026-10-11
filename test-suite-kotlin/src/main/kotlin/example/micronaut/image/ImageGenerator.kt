package example.micronaut.image

import dev.langchain4j.model.image.ImageModel
import jakarta.inject.Singleton
import java.net.URI

@Singleton
class ImageGenerator(private val imageModel: ImageModel) { // <1>

    fun generate(prompt: String): URI? {
        val image = imageModel.generate(prompt).content() // <2>
        return image.url()
    }
}
