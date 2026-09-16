# Python Docs Disabled Test Inventory

This file tracks Python docs examples of Micronaut LangChain4j that are present but disabled, or that deviate from the
Java example because the direct port currently fails compilation or at runtime. Use it as the bug-fixing task list
for the final migration wave.

## Reconciliation

- Last generated active `@Disabled` count: 8.
- Last generated command: `rg -n "@Disabled\\(" test-suite-python/src/test/python`.
- Last full-suite command: `./gradlew :test-suite-python:test -Ppython-ci` (needs a container runtime for the Ollama test container).
- Last full-suite result: see the pull request description.

## Migration Rules

- Do not define local copies of Micronaut annotation helpers or custom annotation shims in docs snippets. Standard
  Micronaut and LangChain4j annotations are generated from imports (`from micronaut.langchain4j.annotation import AiService`,
  `from dev.langchain4j.service import SystemMessage, UserMessage, V`, `from dev.langchain4j.agent.tool import Tool`, ...).
- `@AiService` / `@AgenticService` interfaces are abstract classes (`ABC`) whose abstract methods have `...` bodies;
  `@Tool` methods live on plain `@Singleton` classes. Parameter annotations use `Annotated[str, V("name")]`.
- Do not add Java-style getters or setters to Python docs models. Prefer `@Introspected @dataclass(frozen=True)` models
  (`Musician`, `MemoryIdAndResponse`).
- Methods that implement or override a Java interface keep the Java (camelCase) name (`onCreated`, `doChat`); other
  methods are snake_case.
- Java classes are imported like Python modules (`from java.util import List, UUID`,
  `from java.util.concurrent import ConcurrentHashMap, Executor, ForkJoinPool`,
  `from dev.langchain4j.agentic.agent import AgentBuilder`); `java.type(...)` is not used. Nested Java types are
  attributes of the imported outer type (`OllamaChatModel.OllamaChatModelBuilder`, `MessageWindowChatMemory.Builder`).
- Method names that are Python keywords use the generated keyword-safe alias: `SystemMessage.from_(...)`,
  `UserMessage.from_(...)`, `AiMessage.from_(...)`, `EvaluationRequest.from_(...)`.
- `src/main/python` (the `source="main"` snippets) and `src/test/python` are compiled together (see `build.gradle.kts`),
  but a *relative* import of a class living in the other source root (`from .Friend import Friend` in a test) resolves
  to `java.lang.Object` in the generated injection point (`No bean of type [java.lang.Object] exists`): tests import
  main classes with absolute imports (`from example.micronaut.aiservice.Friend import Friend`).
- Prefer `@MicronautTest(environments=["ollama"])` with injected beans. The `langchain4j.ollama.base-url` of the shared
  Ollama test container is supplied by the Java `example.micronaut.OllamaTestConfigurer` (`@ContextConfigurer`) of
  this project (see below).

## Active `@Disabled` Tests

| Test | Reason |
| --- | --- |
| `example.micronaut.aiservice.AiServiceTest`, `example.micronaut.aiservice.evaluation.AiServiceEvaluationExample`, `example.micronaut.aiservice.tools.OllamaToolTest`, `example.micronaut.aiservice.tools.OpenAiToolTest` | A Python `@AiService` compiles to a Micronaut introduction bean (`$Friend$RuntimeProxy$Definition`), but `AiServiceInterceptor` hands the generated Java class to `AiServices.builder(Class)`, which builds the implementation reflectively from a Java *interface*: `IllegalConfigurationException: The type implemented by the AI Service must be an interface, found 'example.micronaut.aiservice.Friend'`. The Java stub the Python compiler generates for a Python class is a concrete class that only carries the JUnit annotations, so the LangChain4j method annotations (`@SystemMessage`, `@UserMessage`, ...) are not visible to `Method.getAnnotation` either. The same applies to `@Tool` methods of a Python bean: LangChain4j scans the tool object's Java class reflectively and finds no `@Tool` methods on the stub (the Micronaut side works: the generated `$LegalDocumentTools$Definition$Exec` carries `@Tool` with `@Executable(processOnStartup=true)` and `CompanyBot`'s `tools` member references `LegalDocumentTools`). |
| `example.micronaut.agentic.AgenticServiceTest`, `example.micronaut.agentic.EveningPlannerAgentTest`, `example.micronaut.agentic.LoopingPlannerAgentTest`, `example.micronaut.agentic.ParallelPlanningAgentTest` | Same root cause for `@AgenticService`: `AgenticServices.createAgenticSystem(Class)` fails with `No agent method found in class: example.micronaut.agentic.GreeterAgent` because the `@Agent`/`@UserMessage`/`@SequenceAgent`/... annotations of the Python class are not present on the generated Java stub. |

## Commented Unsupported Snippet Ports

None.

## Workarounds Kept In Snippets

| Target | Reason |
| --- | --- |
| `example.micronaut.OllamaTestConfigurer` (Java, `src/test/java`) | `TestPropertyProvider.getProperties()` is called by Micronaut Test before the application context, and with it the GraalPy runtime, exists, so a Python test class cannot implement `OllamaTestPropertyProvider`; the Java `@ContextConfigurer` adds the container base URL as a property source of the tests run with the `ollama` environment (in `configure(ApplicationContext)`, because the builder is configured before `@MicronautTest` selects the environments). |
| `example.micronaut.AssistantWithMemoryTest` | Python test classes cannot extend Python base classes, so the abstract Java test with its in-memory/Redis/Neo4j/Cassandra subclasses is a single concrete test of the in-memory chat memory store. |
| `example.micronaut.AssistantWithMemory` | Python has no method overloading: `chat(conversationId, message)` and `chat(message)` are a single `chat(message, conversation_id=None)` method. |
| `example.micronaut.agentic.SupportAgentBuilderListener` | `BeanCreatedEventListener[AgentBuilder]` cannot be compiled: the self-referential type bound of `AgentBuilder<T, B extends AgentBuilder<T, ?>>` makes the Python compiler stub generator fail with a `StackOverflowError` (also with a `java.type(...)` alias), so the listener implements the raw `BeanCreatedEventListener` and filters the created bean with `java.instanceof(bean, AgentBuilder)` on the imported class (`from dev.langchain4j.agentic.agent import AgentBuilder` compiles and works at runtime). `BeanCreatedEventListener[LoopAgentService]` (`LoopAgentServiceListener`, `LoopBuilderListener`) compiles. |
| `example.micronaut.agentic.LoopingPlannerAgent`, `example.micronaut.agentic.ParallelPlanningAgentTest` | The nested interfaces/classes of the Java examples (`TranslatorAgent`, `LoopBuilderListener`, `MusicPlanner`, `DinnerPlanner`, `EveningPlanner`) are top-level classes of the same module. The `@ParallelExecutor`/`@Output` static interface methods are `@staticmethod`s of the abstract class. |
| `example.micronaut.agentic.AgenticTestChatModel` (Java, `src/test/java`) | The mock `ChatModel` of the `agentic-test` environment is written in Java: a Python implementation (`@Executable doChat(...)`) is injected into other Python beans (`MusicianAssistant`) as the raw Python object, which does not have the `chat(...)` default methods of the Java interface (`AttributeError: 'AgenticTestChatModel' object has no attribute 'chat'`). |

## Intentionally Unsupported Snippet Targets

None.
