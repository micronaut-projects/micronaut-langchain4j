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
package io.micronaut.langchain4j.aiservices;

import dev.langchain4j.service.tool.ToolProvider;
import io.micronaut.context.BeanContext;
import io.micronaut.context.exceptions.ConfigurationException;
import io.micronaut.core.reflect.ClassUtils;
import java.util.List;

/**
 * Creates the {@link ToolProvider} of the MCP clients selected with
 * {@link io.micronaut.langchain4j.annotation.AiService#mcpClients()}.
 *
 * <p>{@code langchain4j-mcp} is an optional dependency: the MCP types are only referenced by {@link McpSupport},
 * which is loaded once its presence has been checked.</p>
 */
final class McpToolProviders {

    private static final String MCP_CLIENT = "dev.langchain4j.mcp.client.McpClient";

    private McpToolProviders() {
    }

    static ToolProvider create(BeanContext beanContext, Class<?> service, List<String> clientNames) {
        if (!ClassUtils.isPresent(MCP_CLIENT, McpToolProviders.class.getClassLoader())) {
            throw new ConfigurationException("AI service " + service.getName() + " declares mcpClients " + clientNames
                + " but LangChain4j MCP is not on the classpath. Add the dependency io.micronaut.mcp:micronaut-mcp-client-langchain4j or dev.langchain4j:langchain4j-mcp");
        }
        return McpSupport.toolProvider(beanContext, clientNames);
    }
}
