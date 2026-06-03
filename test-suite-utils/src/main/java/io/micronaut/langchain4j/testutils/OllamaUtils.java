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
    public static final String CHAT_MODEL_NAME = "tinyllama";
    public static final String TOOL_MODEL_NAME = "qwen2.5:0.5b";
    public static final String EMBEDDING_MODEL_NAME = "all-minilm";
    private static final String IMAGE_NAME = "ollama/ollama:latest";
    private static final String NEW_IMAGE_NAME = "ollama/ollama-tinyllama-qwen2.5-0.5b-all-minilm";
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

    public static String ollamaEmbeddingModelName() {
        return EMBEDDING_MODEL_NAME;
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
            .withFilter("reference", List.of(NEW_IMAGE_NAME))
            .exec();

        if (ollamaDockerImages.isEmpty()) {
            return createOllamaContainerAndPullModels(IMAGE_NAME, NEW_IMAGE_NAME, CHAT_MODEL_NAME, TOOL_MODEL_NAME, EMBEDDING_MODEL_NAME);
        } else {
            LOG.info("Using existing Ollama container with model image...");
            return new OllamaContainer(
                DockerImageName.parse(NEW_IMAGE_NAME).asCompatibleSubstituteFor("ollama/ollama"));
        }
    }

    private static OllamaContainer createOllamaContainerAndPullModels(String image, String newImage, String... models) throws IOException, InterruptedException {
        LOG.info("Creating a new Ollama container...");
        OllamaContainer ollama = new OllamaContainer(image);
        ollama.start();
        for (String model : models) {
            LOG.info("Executing an 'ollama pull' command for model '{}'...", model);
            ollama.execInContainer("ollama", "pull", model);
        }
        ollama.commitToImage(newImage);
        return ollama;
    }
}
