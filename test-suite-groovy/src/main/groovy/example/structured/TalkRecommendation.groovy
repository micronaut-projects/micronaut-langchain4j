package example.structured

import groovy.transform.EqualsAndHashCode
import io.micronaut.jsonschema.JsonSchema

@JsonSchema(description = "A conference talk recommended to an attendee.") // <1>
@EqualsAndHashCode
class TalkRecommendation {
    String title
    String reason
    int score
}
