package example.structured;

import io.micronaut.jsonschema.JsonSchema;

/**
 * A conference talk recommended to an attendee.
 *
 * @param title The title of the talk
 * @param reason Why the talk matches the interests of the attendee
 * @param score How well the talk matches the interests, from 1 to 5
 */
@JsonSchema // <1>
public record TalkRecommendation(String title, String reason, int score) {
}
