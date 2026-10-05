from dataclasses import dataclass

from micronaut.jsonschema import JsonSchema
from micronaut.core.annotation import Introspected


@JsonSchema  # <1>
@Introspected
@dataclass
class TalkRecommendation:
    """A conference talk recommended to an attendee."""

    title: str
    """The title of the talk"""

    reason: str
    """Why the talk matches the interests of the attendee"""

    score: int
    """How well the talk matches the interests, from 1 to 5"""
