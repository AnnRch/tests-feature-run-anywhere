Feature: Authentication and Registration - Negative Scenarios

  @TraineeRegistrationError
  Scenario: Register a trainee with missing required fields
    When I register a trainee with the following invalid details:
      | firstName | lastName |
      |           | Ageeva   |
    Then the auth response status should be 400

  @LoginFailure
  Scenario: Login with incorrect credentials
    Given a trainee "faulty.user" exists with password "correctPass123"
    When I login with username "faulty.user" and password "wrongPassword"
    Then the auth response status should be 401

  @ChangePasswordUnauthorized
  Scenario: Change password without being authenticated
    When I attempt to change password for "natali.ageeva" from "old" to "new" without authentication
    Then the auth response status should be 401

  @ChangePasswordInvalidOld
  Scenario: Change password with incorrect old password
    Given a trainee "natali.ageeva" exists with password "p@ssword123"
    When I change password for "natali.ageeva" from "WRONG_OLD_PASS" to "new.P@ssword77"
    Then the auth response status should be 401