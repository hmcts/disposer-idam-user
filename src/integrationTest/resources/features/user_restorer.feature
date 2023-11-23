Feature: User restorer
  Check that user restorer works as intended

  Scenario: User restorer restores users
    Given lau api returns a list of users to restore
    Then user restorer should call IdAM api to restore users
    And user restorer should call lau api to delete log entries of deletion

  Scenario: User restorer deletes log entry if idam returns 409 because of matching id
    Given lau api returns a list of users to restore
    And IdAM api fails to restore with status code 409 and message "id in use"
    Then user restorer should call lau api to delete log entries of deletion

  Scenario: User restorer deletes log entry if idam returns 409 because of archived matching id
    Given lau api returns a list of users to restore
    And IdAM api fails to restore with status code 409 and message "id already archived"
    Then user restorer should call lau api to delete log entries of deletion

  Scenario: User restorer backs out if restore fails
    Given lau api returns a list of users to restore
    And IdAM api fails to restore with status code 504 and message "Gateway Timeout"
    Then user restorer should NOT call lau api to delete log entries

  Scenario: User restorer backs out if user already exists
    Given lau api returns a list of users to restore
    And IdAM api fails to restore with status code 409 and message "email in use"
    Then user restorer should NOT call lau api to delete log entries

  Scenario: User restorer makes limited number of requests
    Given requests limit set to 10
    Then there should be 10 requests to lau api
