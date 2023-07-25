Feature: User disposer
  Check that user disposer works as intended

  Scenario: Stale user disposer is finding stale users
    Given User disposer runs
    Then it should collect users to dispose
