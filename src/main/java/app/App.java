package app;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.stream.Stream;

public class App {

  private static final String INPUT_JSON =
      """
      {
        "policy": "default-policy:v1.0",
        "content": {
          "title": "Hello World!",
          "body": "This is a test message."
        },
        "context": {
          "workflow": {
            "steps": [
              {
                "name": "Example Action",
                "type": "action",
                "action_id": "default-action",
                "fields": [
                  {
                    "type": "string",
                    "$ref": "/body"
                  }
                ]
              }
            ]
          }
        }
      }
      """;

  public static void main(String[] args) throws JsonMappingException, JsonProcessingException {
    ObjectMapper mapper = new ObjectMapper();
    JsonNode jsonNode = mapper.readTree(INPUT_JSON);

    Workflow workflow = mapper.treeToValue(jsonNode.at("/context/workflow"), Workflow.class);
    Step step = workflow.getStepByActionId("default-action");
    Field field = step.fields()[0];
    String value = jsonNode.get("content").at(field.ref()).asText();

    System.out.printf(
        """

          $Ref: %s, Value: '%s', Type: %s

        """,
        field.ref(), value, field.type());
  }

  public record Workflow(Step[] steps) {

    public Step getStepByActionId(String actionId) {
      return Stream.of(steps())
          .filter(o -> o.actionId().equals(actionId))
          .findFirst()
          .orElseThrow(() -> new IllegalArgumentException());
    }
  }

  public record Step(
      String name, String type, @JsonProperty("action_id") String actionId, Field[] fields) {}

  public record Field(String type, @JsonProperty("$ref") String ref) {}
}
