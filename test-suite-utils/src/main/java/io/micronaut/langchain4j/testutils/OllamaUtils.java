package io.micronaut.langchain4j.testutils;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.model.Image;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.ollama.OllamaContainer;
import org.testcontainers.utility.DockerImageName;

import java.io.IOException;
import java.util.List;

public final class OllamaUtils {
    private static Logger LOG = LoggerFactory.getLogger(OllamaUtils.class);
    private static String MODEL_NAME = "tinyllama";
    private static String IMAGE_NAME = "ollama/ollama:latest";
    private static String NEW_IMAGE_NAME = "ollama/ollama-tinyllama";
    private static OllamaContainer container;

    private OllamaUtils() {
    }

    public static void close() {
        if (container != null) {
            container.close();
            container = null;
        }
    }
    public static String ollamaContainerBaseUrl() throws IOException, InterruptedException {
        if (container == null) {
            createAndStartContainer();
            GlobalTestLifecycle.registerShutdownHook();
        }
        return String.format("http://%s:%d", container.getHost(), container.getFirstMappedPort());
    }

    private static void createAndStartContainer() throws InterruptedException, IOException {
        container = createContainer();
        container.start();
        do {
            LOG.info("Waiting for Ollama container to be ready...");
            Thread.sleep(1000);
        } while (!container.isRunning());
    }

    private static OllamaContainer createContainer() throws IOException, InterruptedException {
        LOG.info("Checking if the Ollama Docker image exists already...");
        DockerClient dockerClient = DockerClientFactory.lazyClient();
        List<Image> ollamaDockerImages = dockerClient
            .listImagesCmd()
            .withImageNameFilter(NEW_IMAGE_NAME)
            .exec();

        if (ollamaDockerImages.isEmpty()) {
            return createOllamaContainerAndPullModel(IMAGE_NAME, MODEL_NAME, NEW_IMAGE_NAME);
        } else {
            LOG.info("Using existing Ollama container with model image...");
            return new OllamaContainer(
                DockerImageName.parse(NEW_IMAGE_NAME).asCompatibleSubstituteFor("ollama/ollama"));
        }
    }

    private static OllamaContainer createOllamaContainerAndPullModel(String image, String model, String newImage) throws IOException, InterruptedException {
        LOG.info("Creating a new Ollama container...");
        OllamaContainer ollama = new OllamaContainer(image);
        ollama.start();
        LOG.info("Executing an 'ollama pull' command...");
        ollama.execInContainer("ollama", "pull", model);
        ollama.commitToImage(newImage);
        return ollama;
    }
}
