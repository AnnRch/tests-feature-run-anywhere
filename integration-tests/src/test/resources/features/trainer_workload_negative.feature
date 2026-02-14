Feature: Trainer Workload E2E - Negative Scenarios

  Background:
    Given a new trainer "lilia.levada" registers on the Gym Service
    And the trainer "lilia.levada" logs in to obtain a JWT token

  @InvalidToken
  Scenario: Attempt to view workload with an invalid JWT token
    When I request the workload for the trainer with an expired or invalid token
    Then the response status should be 403

  @AccessDenied
  Scenario: Trainer cannot delete a training session belonging to another trainer
    Given a new trainee "natali.ageeva" registers on the Gym Service
    And a new trainer "lilia.levada" registers on the Gym Service
    And a new trainer "other.trainer" registers on the Gym Service
    And the trainer "lilia.levada" logs in to obtain a JWT token
    And the "other.trainer" adds a 60-minute training
    When the trainer "lilia.levada" attempts to delete the training of "other.trainer"
    Then the response status should be 403

  @MalformedTrainingData
  Scenario: Add training with negative duration
    When the trainer "lilia.levada" adds a -30-minute training named "Time Travel"
    Then the gym service should reject the request with status 400
    And the workload for "lilia.levada" should not change