Feature: Training Types Management - Negative Scenarios

  @PublicGet
  Scenario: Successfully retrieve training types as an anonymous user
    When I request all training types without credentials
    Then the training types response status should be 200
    And the response should contain the following training types:
      | Yoga     |
      | Crossfit |
      | Boxing   |

  @MethodNotAllowed
  Scenario: Attempt to create a new training type via API
    Given I am authenticated as trainer "lilia.levada"
    When I attempt to manually create a training type named "Stretching"
    Then the training types response status should be 405