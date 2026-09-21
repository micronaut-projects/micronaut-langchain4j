from dataclasses import dataclass

from micronaut.core.annotation import Introspected


@Introspected
@dataclass(frozen=True)
class MemoryIdAndResponse:
    memory_id: str
    response: str
