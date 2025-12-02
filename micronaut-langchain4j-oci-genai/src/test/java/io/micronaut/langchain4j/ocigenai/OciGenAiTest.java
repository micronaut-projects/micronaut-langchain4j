package io.micronaut.langchain4j.ocigenai;

import com.oracle.bmc.auth.BasicAuthenticationDetailsProvider;
import com.oracle.bmc.auth.SimpleAuthenticationDetailsProvider;
import com.oracle.bmc.generativeaiinference.GenerativeAiInference;
import com.oracle.bmc.generativeaiinference.GenerativeAiInferenceClient;
import com.oracle.bmc.generativeaiinference.model.BaseChatRequest;
import com.oracle.bmc.generativeaiinference.model.BaseChatResponse;
import com.oracle.bmc.generativeaiinference.model.ChatChoice;
import com.oracle.bmc.generativeaiinference.model.ChatContent;
import com.oracle.bmc.generativeaiinference.model.ChatResult;
import com.oracle.bmc.generativeaiinference.model.GenericChatRequest;
import com.oracle.bmc.generativeaiinference.model.GenericChatResponse;
import com.oracle.bmc.generativeaiinference.model.Message;
import com.oracle.bmc.generativeaiinference.model.TextContent;
import com.oracle.bmc.generativeaiinference.model.UserMessage;
import dev.langchain4j.community.model.oracle.oci.genai.OciGenAiChatModel;
import dev.langchain4j.model.chat.ChatModel;
import io.micronaut.context.annotation.Property;
import io.micronaut.context.annotation.Prototype;
import io.micronaut.context.event.BeanCreatedEvent;
import io.micronaut.context.event.BeanCreatedEventListener;
import org.jspecify.annotations.NonNull;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Post;
import io.micronaut.oraclecloud.core.TenancyIdProvider;
import io.micronaut.runtime.server.EmbeddedServer;
import io.micronaut.test.context.TestContext;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Singleton;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.List;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@MicronautTest
@Property(name = "oci.config.path", value = "") // disable local config
@Property(name = "langchain4j.oci-gen-ai.chat-model.model-name", value = "orca-mini")
@Property(name = "langchain4j.oci-gen-ai.compartment-id", value = "test")
@Property(name = "micronaut.server.port", value = "${random.port}")
public class OciGenAiTest {

    @Test
    void testClient(GenerativeAiInference generativeAiInference) {
        assertNotNull(generativeAiInference);
    }


    @Test
    void testDefaultConfig(DefaultOciGenAiChatModelConfiguration configuration) {
        assertNotNull(configuration);
    }

    @Test
    void testChatModel(ChatModel chatModel) {
        assertNotNull(chatModel);
        Assertions.assertInstanceOf(OciGenAiChatModel.class, chatModel);
        String response = chatModel.chat("Tell me a joke about Java?");
        assertNotNull(response);
    }

    @Controller
    static class TestChat {
        @Post("/20231130/actions/chat")
        ChatResult chat(@Body com.oracle.bmc.generativeaiinference.model.ChatDetails chatDetails) {
            assertNotNull(chatDetails);
            BaseChatRequest chatRequest = chatDetails.getChatRequest();
            assertInstanceOf(GenericChatRequest.class, chatRequest);
            List<Message> messages = ((GenericChatRequest) chatRequest).getMessages();
            assertEquals(1, messages.size());
            assertInstanceOf(UserMessage.class, messages.get(0));
            UserMessage userMessage = (UserMessage) messages.get(0);
            List<ChatContent> content = userMessage.getContent();
            assertEquals(1, content.size());
            assertInstanceOf(TextContent.class, content.get(0));
            TextContent textContent = (TextContent) content.get(0);
            assertEquals("Tell me a joke about Java?", textContent.getText());
            UserMessage whatever = UserMessage.builder().content(List.of(TextContent.builder().text("whatever").build())).build();
            return ChatResult.builder().chatResponse(GenericChatResponse.builder().choices(
                List.of(ChatChoice.builder().message(whatever).build())
            ).build()).build();
        }
    }

    @Prototype
    BeanCreatedEventListener<GenerativeAiInferenceClient.Builder> clientBuilder(@Property(name = "micronaut.server.port") int port) {
        return event -> {
            GenerativeAiInferenceClient.Builder builder = event.getBean();
            builder.endpoint("http://localhost:" + port);
            return builder;
        };
    }


    @Singleton
    BasicAuthenticationDetailsProvider basicAuthenticationDetailsProvider() {
        return SimpleAuthenticationDetailsProvider.builder()
            .tenantId("ocid1.tenancy.oc1.." + OciGenAiTest.class.getCanonicalName())
            .userId("ocid1.user.oc1.." + OciGenAiTest.class.getCanonicalName())
            .privateKeySupplier(createPrivateKeySupplier()).build();
    }

    @Singleton
    TenancyIdProvider tenancyIdProvider() {
        return () -> "test";
    }

    private static Supplier<InputStream> createPrivateKeySupplier() {
        KeyPairGenerator kpg;
        try {
            kpg = KeyPairGenerator.getInstance("RSA");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }

        kpg.initialize(2048);
        KeyPair pk = kpg.generateKeyPair();
        return () -> createPem(pk);
    }

    private static InputStream createPem(KeyPair keyPair) {
        String encodedKey = Base64.getEncoder().encodeToString(keyPair.getPrivate().getEncoded());
        String pem = "-----BEGIN PRIVATE KEY-----\n" + encodedKey + "\n-----END PRIVATE KEY-----\n";
        return new ByteArrayInputStream(pem.getBytes());
    }
}
