from dataclasses import dataclass

from micronaut.core.annotation import Introspected


@Introspected
@dataclass(frozen=True)
class Musician:
    name: str
    albums: str
