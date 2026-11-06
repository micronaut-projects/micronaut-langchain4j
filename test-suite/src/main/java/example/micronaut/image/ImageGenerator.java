package example.micronaut.image;

import dev.langchain4j.data.image.Image;
import dev.langchain4j.model.image.ImageModel;
import jakarta.inject.Singleton;

@Singleton
public class ImageGenerator {

    private final ImageModel imageModel;

    public ImageGenerator(ImageModel imageModel) { // <1>
        this.imageModel = imageModel;
    }

    public Image generate(String prompt) {
        return imageModel.generate(prompt).content(); // <2>
    }
}
