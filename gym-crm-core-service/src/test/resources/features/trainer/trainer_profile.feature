Feature: Trainer Profile Management
  As a Trainer
  I want to manage my profile and status
  So that my information remains up to date and my availability is accurate

  Background:
    Given the system is initialized with a trainer "lilia.levada" and password "l@pulya"
    And I am authenticated as "lilia.levada"

  @Profile
  Scenario: Successfully retrieve own profile
    When I request my trainer profile
    Then the response status should be 200
    And the profile should contain the username "lilia.levada"

  @Update
  Scenario: Successfully update profile information
    When I update my profile with the following details:
      | firstName | lastName | isActive |
      | Lilechka      | Levada    | true     |
    Then the trainer response status should be 200
    And the updated profile firstName should be "Lilechka"

  @Activation
  Scenario Outline: Change trainer activation status
    When I send a request to set my activation status to <status>
    Then the response status should be 200
    And my profile status should be <status>

    Examples:
      | status |
      | false  |
      | true   |

  @Delete
  Scenario: Delete a trainer profile
    Given I am an administrator
    When I delete the trainer profile for "lilia.levada"
    Then the response status should be 200
    And the trainer "lilia.levada" should no longer exist