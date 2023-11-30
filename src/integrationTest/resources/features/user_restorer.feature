Feature: User restorer
  Check that user restorer works as intended

  Scenario: User restorer restores users
    Given lau api returns a list of users to restore
    When restore service runs
    Then there should be 1 calls to fetch deleted users
    And user restorer should call IdAM api to restore 4 users
    And summary should have successful restore of size 4 and failed of size 0

  Scenario: User restorer fetches paged results
    Given lau api returns paged results
    When restore service runs
    Then there should be 2 calls to fetch deleted users
    And user restorer should call IdAM api to restore 3 users
    And summary should have successful restore of size 3 and failed of size 0

  Scenario: Summary records successful and failed restore actions
    Given lau api returns a list of users to restore
    And IdAM api responds with 400 error to user restore call
    When restore service runs
    Then summary should have successful restore of size 0 and failed of size 4

  Scenario: User restorer makes limited number of requests
    Given requests limit set to 10
    When restore service runs
    Then there should be 10 requests to lau api
