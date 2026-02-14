Feature: Trainee Profile Management - Negative Scenarios

  Background:
    Given the system is initialized with a trainee "natali.ageeva"

  @ProfileUnauthorized
  Scenario: Retrieve profile without authentication
    When I request my trainee profile without credentials
    Then the response status should be 401

  @UpdateForbidden
  Scenario: Update a profile belonging to another user
    Given the system is initialized with a trainee "other.user"
    And I am authenticated as trainee "natali.ageeva"
    When I update trainee "other.user" profile with the following details:
      | firstName | lastName | isActive |
      | Hacker    | Man      | true     |
    Then the response status should be 403

  @DeleteNonExistent
  Scenario: Delete a profile that does not exist
    When I attempt to delete a non-existent trainee "ghost.user"
    Then the response status should be 404

  @ValidationFailure
  Scenario: Update profile with invalid data
    And I am authenticated as trainee "natali.ageeva"
    When I update my trainee profile with invalid empty firstName
    Then the response status should be 400