package example.structured

import io.micronaut.jsonschema.JsonSchema

/**
 * A conference talk recommended to an attendee.
 */
@JsonSchema // <1>
data class TalkRecommendation(
    /** The title of the talk */
    var title: String = "",
    /** Why the talk matches the interests of the attendee */
    var reason: String = "",
    /** How well the talk matches the interests, from 1 to 5 */
    var score: Int = 0,
)
