Feature: Trainer Workload Statistics - Negative Scenarios

  Background:
    Given the workload system is clean

  @WorkloadNotFound
  Scenario: Fail to retrieve statistics for a non-existent trainer
    When I request the workload for trainer "ghost.trainer"
    Then the workload response status should be 404

  @UnauthorizedWorkload
  Scenario: Fail to retrieve workload without authentication
    When I request the workload for trainer "lilia.levada" without credentials
    Then the workload response status should be 403

  @InvalidUsername
  Scenario: Fail to retrieve workload with missing username
    When I request the workload with a missing username parameter
    Then the workload response status should be 400