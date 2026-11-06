package io.micronaut.langchain4j.decision;

import dev.langchain4j.model.decision.DecisionModel;
import dev.langchain4j.model.decision.request.ChoiceQuestion;
import dev.langchain4j.model.decision.request.DecisionRequest;
import dev.langchain4j.model.decision.request.Question;
import dev.langchain4j.model.decision.request.YesNoQuestion;
import dev.langchain4j.model.decision.response.ChoiceAnswer;
import dev.langchain4j.model.decision.response.DecisionResponse;
import dev.langchain4j.model.decision.response.YesNoAnswer;

import java.util.Locale;
import java.util.Map;

/**
 * Answers yes when the input mentions money, and chooses the first option whose name or description shares a word
 * with the input.
 */
public final class KeywordDecisionModel implements DecisionModel {

    @Override
    public DecisionResponse doDecide(DecisionRequest request) {
        String input = String.valueOf(request.input()).toLowerCase(Locale.ROOT);
        DecisionResponse.Builder response = DecisionResponse.builder();
        for (Map.Entry<String, Question> question : request.questions().entrySet()) {
            if (question.getValue() instanceof YesNoQuestion) {
                response.answer(question.getKey(), YesNoAnswer.of(input.contains("money") ? 0.9 : 0.1));
            } else if (question.getValue() instanceof ChoiceQuestion choice) {
                String chosen = choice.options().entrySet().stream()
                    .filter(option -> matches(input, option.getKey() + " " + option.getValue()))
                    .map(Map.Entry::getKey)
                    .findFirst()
                    .orElse(choice.options().keySet().iterator().next());
                ChoiceAnswer.Builder answer = ChoiceAnswer.builder().value(chosen).options(choice.options().keySet());
                choice.options().keySet().forEach(option -> answer.probability(option, option.equals(chosen) ? 0.9 : 0.1 / (choice.options().size() - 1)));
                response.answer(question.getKey(), answer.build());
            }
        }
        return response.build();
    }

    private static boolean matches(String input, String option) {
        for (String word : option.toLowerCase(Locale.ROOT).split("\\W+")) {
            if (word.length() > 3 && input.contains(word)) {
                return true;
            }
        }
        return false;
    }
}
