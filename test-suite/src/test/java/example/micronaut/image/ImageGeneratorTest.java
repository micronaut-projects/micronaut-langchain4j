package example.micronaut.image;

import dev.langchain4j.data.image.Image;
import dev.langchain4j.model.output.Response;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class ImageGeneratorTest {

    @Test
    void keepsTheBase64DataOfTheImage() {
        ImageGenerator generator = new ImageGenerator(prompt -> Response.from(
            Image.builder().base64Data("aW1hZ2U=").mimeType("image/png").build()));

        Image image = generator.generate("A cat");

        assertEquals("aW1hZ2U=", image.base64Data());
        assertEquals("image/png", image.mimeType());
        assertNull(image.url());
    }
}
