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

import dev.langchain4j.mcp.McpToolProvider;
import dev.langchain4j.mcp.client.McpClient;
import dev.langchain4j.service.tool.ToolProvider;
import io.micronaut.context.BeanContext;
import io.micronaut.inject.qualifiers.Qualifiers;
import java.util.List;

/**
 * Builds an {@link McpToolProvider} from named {@link McpClient} beans. Only loaded when LangChain4j MCP is present.
 */
final class McpSupport {

    private McpSupport() {
    }

    static ToolProvider toolProvider(BeanContext beanContext, List<String> clientNames) {
        List<McpClient> clients = clientNames.stream()
            .map(clientName -> beanContext.getBean(McpClient.class, Qualifiers.byName(clientName)))
            .toList();
        return McpToolProvider.builder()
            .mcpClients(clients)
            .build();
    }
}
