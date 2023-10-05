Feature: User disposer
  Check that user disposer works as intended

  Scenario: Stale user disposer is finding stale users
    Given IdAM api works fine and simulation mode is "false"
    Then it should dispose users without roles

  Scenario: Stale user disposer is in simulation mode
    Given IdAM api works fine and simulation mode is "true"
    Then it should not dispose users due to simulation mode

  Scenario: Feign client makes multiple calls on Forbidden response
    Given IdAM api responds with 401
    Then it should retry making IdAM call

  Scenario: Application raises exception on 500 error
    Given IdAM api responds with 500
    Then it should rethrow exception
