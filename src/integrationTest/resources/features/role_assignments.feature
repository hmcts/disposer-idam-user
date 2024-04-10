Feature: Role assignments
  Tests scenarios related to role assignments

  Scenario: Call Role Assignment pages
    Given Role Assignments filtering called with many users
    Then Role Assignments should fetch all available pages
