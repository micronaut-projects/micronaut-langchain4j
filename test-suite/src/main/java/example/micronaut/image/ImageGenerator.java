package example.micronaut.image;

import dev.langchain4j.data.image.Image;
import dev.langchain4j.model.image.ImageModel;
import jakarta.inject.Singleton;

import java.net.URI;

@Singleton
public class ImageGenerator {

    private final ImageModel imageModel;

    public ImageGenerator(ImageModel imageModel) { // <1>
        this.imageModel = imageModel;
    }

    public URI generate(String prompt) {
        Image image = imageModel.generate(prompt).content(); // <2>
        return image.url();
    }
}
