/*
 * Copyright 2017-2026 original authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.micronaut.langchain4j.processor;

import dev.langchain4j.agent.tool.CompensateFor;
import io.micronaut.context.annotation.Executable;
import io.micronaut.core.annotation.AnnotationValue;
import io.micronaut.core.annotation.Internal;
import io.micronaut.inject.annotation.TypedAnnotationTransformer;
import io.micronaut.inject.visitor.VisitorContext;

import java.util.List;

/**
 * Makes the {@link CompensateFor} methods executable, so that the tool registry finds them without reflection and
 * reports that their compensating actions are not registered.
 *
 * @since 2.4.0
 */
@Internal
public class CompensateForAnnotationTransformer implements TypedAnnotationTransformer<CompensateFor> {
    @Override
    public Class<CompensateFor> annotationType() {
        return CompensateFor.class;
    }

    @Override
    public List<AnnotationValue<?>> transform(AnnotationValue<CompensateFor> annotation, VisitorContext visitorContext) {
        return List.of(AnnotationValue.builder(annotation)
            .stereotype(AnnotationValue.builder(Executable.class).build())
            .build());
    }
}
