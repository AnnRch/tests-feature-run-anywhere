Feature: Trainer Profile Management - Negative Scenarios
  As a User
  I want to be prevented from unauthorized profile actions
  So that trainer data remains secure and consistent

  Background:
    Given the system is initialized with a trainer "lilia.levada" and password "l@pulya"

  @ProfileUnauthorized
  Scenario: Retrieve trainer profile without authentication
    When I request my trainer profile without credentials
    Then the response status should be 401

  @UpdateForbidden
  Scenario: Update a profile belonging to another trainer
    Given the system is initialized with a trainer "other.trainer" and password "secret"
    And I am authenticated as "lilia.levada"
    When I update trainer "other.trainer" profile with the following details:
      | firstName | lastName | isActive |
      | Hacker    | Trainer  | true     |
    Then the response status should be 403

  @DeleteUnauthorized
  Scenario: Attempt to delete trainer profile without admin rights
    And I am authenticated as "lilia.levada"
    When I attempt to delete the trainer profile for "lilia.levada" as a trainer
    Then the response status should be 403

  @GetNonExistent
  Scenario: Request profile for a non-existent trainer
    When I request profile for "ghost.trainer" as an administrator
    Then the response status should be 404