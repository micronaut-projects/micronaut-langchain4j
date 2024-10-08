/*
 * Copyright 2017-2024 original authors
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

import dev.langchain4j.agent.tool.Tool;
import io.micronaut.context.annotation.Executable;
import io.micronaut.core.annotation.AnnotationValue;
import io.micronaut.core.annotation.Internal;
import io.micronaut.core.annotation.ReflectiveAccess;
import io.micronaut.inject.annotation.TypedAnnotationTransformer;
import io.micronaut.inject.visitor.VisitorContext;
import java.util.List;

@Internal
public class ToolAnnotationTransformer implements TypedAnnotationTransformer<Tool> {
    @Override
    public Class<Tool> annotationType() {
        return Tool.class;
    }

    @Override
    public List<AnnotationValue<?>> transform(AnnotationValue<Tool> annotation, VisitorContext visitorContext) {
        return List.of(AnnotationValue.builder(annotation)
            .stereotype(AnnotationValue.builder(Executable.class)
                .member("processOnStartup", true)
                .build()
            )
            .stereotype(AnnotationValue.builder(ReflectiveAccess.class).build())
            .build());
    }
}
