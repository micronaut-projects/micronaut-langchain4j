package io.micronaut.langchain4j.sourcegen

import io.micronaut.annotation.processing.test.AbstractTypeElementSpec
import io.micronaut.context.ApplicationContext

class ChatLanguageModelVisitorSpec
    extends AbstractTypeElementSpec {

    void "test produce configuration"() {
        given:
        def context = buildContext('test.OllamaModule', '''
package test;

import dev.langchain4j.model.ollama.OllamaChatModel;
import io.micronaut.langchain4j.annotation.ChatLanguageModelProvider;

@ChatLanguageModelProvider(OllamaChatModel.class)
final class OllamaModule {
}

''', true, [
        'langchain4j.ollama.chat-models.default.base-url': 'whatever'
])
        expect:
        context != null
        getBean(context, 'test.OllamaChatModelConfiguration')
    }
}
