package io.micronaut.langchain4j.testutils;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.model.Image;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.containers.Container.ExecResult;
import org.testcontainers.images.builder.Transferable;
import org.testcontainers.ollama.OllamaContainer;
import org.testcontainers.utility.DockerImageName;

import java.io.IOException;
import java.util.List;

public final class OllamaUtils {
    private static final Logger LOG = LoggerFactory.getLogger(OllamaUtils.class);
    public static final String CHAT_MODEL_NAME = "tinyllama";
    public static final String TOOL_MODEL_NAME = "qwen2.5:0.5b";
    public static final String EMBEDDING_MODEL_NAME = "all-minilm";
    // Pinned: server defaults change between releases and alter what the small test models answer.
    private static final String OLLAMA_VERSION = "0.34.0";
    private static final String IMAGE_NAME = "ollama/ollama:" + OLLAMA_VERSION;
    // Ollama sizes the llama.cpp thread pool from the CPUs the Docker VM exposes and ignores cgroup
    // limits, so a cpuset or CPU quota does not help. With one spin-waiting thread per core on a
    // many-core host (24 on an M2 Ultra), any host activity stalls every step and tinyllama drops
    // below one token per second; the same model with 8 threads generates 100+ tokens per second.
    // The model's num_thread parameter is the only knob Ollama honours, so it is baked into the
    // cached models. Small CI runners keep their core count, which is what Ollama would pick anyway.
    private static final int MAX_NUM_THREAD = 8;
    private static final String MODELFILE_PATH = "/tmp/Modelfile";
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
        int numThread = numThread(dockerClient);
        // The cached image includes the Ollama server binary as well as the downloaded models,
        // configured with num_thread, so the thread count is part of the name.
        String cachedImageName = "ollama/ollama-" + OLLAMA_VERSION + "-tinyllama-qwen2.5-0.5b-all-minilm-t" + numThread;
        List<Image> ollamaDockerImages = dockerClient
            .listImagesCmd()
            .withFilter("reference", List.of(cachedImageName))
            .exec();

        if (ollamaDockerImages.isEmpty()) {
            return createOllamaContainerAndPullModels(IMAGE_NAME, cachedImageName, numThread, CHAT_MODEL_NAME, TOOL_MODEL_NAME, EMBEDDING_MODEL_NAME);
        } else {
            LOG.info("Using existing Ollama container with model image...");
            return new OllamaContainer(
                DockerImageName.parse(cachedImageName).asCompatibleSubstituteFor("ollama/ollama"));
        }
    }

    private static int numThread(DockerClient dockerClient) {
        Integer dockerCpus = dockerClient.infoCmd().exec().getNCPU();
        int cpus = dockerCpus != null && dockerCpus > 0 ? dockerCpus : Runtime.getRuntime().availableProcessors();
        return Math.max(1, Math.min(MAX_NUM_THREAD, cpus));
    }

    private static OllamaContainer createOllamaContainerAndPullModels(String image, String newImage, int numThread, String... models) throws IOException, InterruptedException {
        LOG.info("Creating a new Ollama container...");
        OllamaContainer ollama = new OllamaContainer(image);
        ollama.start();
        for (String model : models) {
            LOG.info("Executing an 'ollama pull' command for model '{}'...", model);
            exec(ollama, "ollama", "pull", model);
            LOG.info("Setting num_thread={} on model '{}'...", numThread, model);
            ollama.copyFileToContainer(
                Transferable.of("FROM " + model + "\nPARAMETER num_thread " + numThread + "\n"),
                MODELFILE_PATH);
            exec(ollama, "ollama", "create", model, "-f", MODELFILE_PATH);
        }
        ollama.commitToImage(newImage);
        ollama.stop();
        return new OllamaContainer(
            DockerImageName.parse(newImage).asCompatibleSubstituteFor("ollama/ollama"));
    }

    private static void exec(OllamaContainer ollama, String... command) throws IOException, InterruptedException {
        ExecResult result = ollama.execInContainer(command);
        if (result.getExitCode() != 0) {
            throw new IllegalStateException("Command " + String.join(" ", command) + " failed with exit code "
                + result.getExitCode() + ": " + result.getStderr());
        }
    }
}
