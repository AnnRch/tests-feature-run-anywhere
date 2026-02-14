Feature: Training Management - Negative Scenarios

  Background:
    Given the system has a trainee "natali.ageeva"
    And the system has a trainer "lilia.levada"
    And the system has a trainer "other.trainer"

  @AddTrainingMissingField
  Scenario: Fail to create training with missing trainee
    And I am authenticated as trainer "lilia.levada"
    When I create a training session with the following details:
      | traineeUsername | trainingName | date       | duration |
      |                 | Morning Yoga | 2026-12-01 | 60       |
    Then the training response status should be 400

  @DeleteTrainingForbidden
  Scenario: Trainer cannot delete someone else's training session
    Given a training session exists between "natali.ageeva" and "other.trainer"
    And I am authenticated as trainer "lilia.levada"
    When I delete that training session
    Then the training response status should be 403

  @DeleteNonExistent
  Scenario: Delete a training session that does not exist
    And I am authenticated as trainer "lilia.levada"
    When I attempt to delete a training with ID "00000000-0000-0000-0000-000000000000"
    Then the training response status should be 403

  @UnauthorizedAccess
  Scenario: Create training session without authentication
    When I create a training session without credentials
    Then the training response status should be 401