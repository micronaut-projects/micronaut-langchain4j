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
package io.micronaut.langchain4j.serde;

import io.micronaut.core.annotation.Internal;
import io.micronaut.core.annotation.Introspected;
import io.micronaut.core.beans.BeanIntrospection;
import io.micronaut.core.beans.BeanIntrospector;
import io.micronaut.reflection.ReflectionBeanIntrospection;
import io.micronaut.serde.SerdeIntrospections;
import io.micronaut.serde.annotation.Serdeable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Supplies Micronaut Serialization with an introspection for the types LangChain4j maps to and from JSON that have
 * no {@link Serdeable} introspection: the introspection generated for the type when it is {@link Introspected}
 * without being {@link Serdeable}, and otherwise one built reflectively by micronaut-reflection, from the fields of
 * any visibility and the accessors of the type, as LangChain4j's Jackson codec does. A {@link Serdeable} type is left
 * to its generated introspection.
 *
 * @since 2.4.0
 */
@Internal
final class ReflectiveIntrospectionResolver implements SerdeIntrospections.RuntimeIntrospectionResolver {

    private static final Logger LOG = LoggerFactory.getLogger(ReflectiveIntrospectionResolver.class);
    private static final Set<Introspected.AccessKind> FIELDS_AND_METHODS = Set.of(Introspected.AccessKind.FIELD, Introspected.AccessKind.METHOD);
    private static final String[] PLATFORM_PACKAGES = {"java.", "javax.", "jdk.", "sun.", "com.sun.", "kotlin.", "groovy.", "scala."};

    @Override
    public Object cacheKey() {
        return ReflectiveIntrospectionResolver.class;
    }

    @Override
    public <T> Optional<BeanIntrospection<T>> resolve(SerdeIntrospections.RuntimeIntrospectionRequest<T> request) {
        Class<T> type = request.argument().getType();
        if (type.isPrimitive() || type.isArray() || type.isEnum() || Collection.class.isAssignableFrom(type)
            || Map.class.isAssignableFrom(type) || isPlatformType(type)) {
            return Optional.empty();
        }
        Optional<BeanIntrospection<T>> generated = BeanIntrospector.SHARED.findIntrospection(type);
        if (generated.isPresent()) {
            // a @Serdeable introspection is resolved by Micronaut Serialization itself, with its configuration
            return generated.get().hasStereotype(Serdeable.class) ? Optional.empty() : generated;
        }
        if (!ReflectionBeanIntrospection.isIntrospectable(type)) {
            return Optional.empty();
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("Mapping {} with a reflective introspection: it has no @Serdeable introspection", type.getName());
        }
        return Optional.of(ReflectionBeanIntrospection.of(type, FIELDS_AND_METHODS));
    }

    private static boolean isPlatformType(Class<?> type) {
        String name = type.getName();
        for (String platformPackage : PLATFORM_PACKAGES) {
            if (name.startsWith(platformPackage)) {
                return true;
            }
        }
        return false;
    }
}
