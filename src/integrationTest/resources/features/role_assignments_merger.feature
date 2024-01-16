Feature: User role merger
  Check that user role merging correctly integrates

  Scenario: User Role merger merges roles
    Given lau-idam fetch users returns
    | userId | email             | first name    | last name  | deletion timestamp        |
    | 00001  | 00001@example.net | first 00001   | last 00001 | 2023-08-23T00:00:01.023Z  |
    | 00002  | 00002@example.net | first 00002   | last 00002 | 2023-08-23T00:00:02.023Z  |
    And IdAM fetch user by email returns
    | id    | email             |
    | 00002 | 00002@example.net |
    | 00003 | 00001@example.net |
    And role-assignments fetch role assignments for user "00001" returns
    | actorIdType | actorId | roleType  | roleName  | classification  | grantType | roleCategory | readOnly | beginTime               | attributes                                                                              |
    | IDAM        | 00001   | CASE      | [CREATOR] | RESTRICTED      | SPECIFIC  | CITIZEN      | false    | 2022-03-31T14:54:31.953Z| {"substantive": "N", "caseId": "12345", "jurisdiction": "SSCS", "caseType": "Benefit"}  |
    When merge service runs
    Then it should make POST request to "/am/role-assignments" with data as in "data/merge-request-single.json"

  Scenario: User Role merger accumulates multiple roles into single merge request
    Given lau-idam fetch users returns
      | userId | email             | first name    | last name  | deletion timestamp        |
      | 00001  | 00001@example.net | first 00001   | last 00001 | 2023-08-23T00:00:01.023Z  |
      | 00002  | 00002@example.net | first 00002   | last 00002 | 2023-08-23T00:00:02.023Z  |
    And IdAM fetch user by email returns
      | id    | email             |
      | 00002 | 00002@example.net |
      | 00003 | 00001@example.net |
    And role-assignments fetch role assignments for user "00001" returns
      | actorIdType | actorId | roleType  | roleName    | classification  | grantType | roleCategory | readOnly | beginTime                | attributes                                                                              |
      | IDAM        | 00001   | CASE      | [CREATOR]   | RESTRICTED      | SPECIFIC  | CITIZEN      | false    | 2022-03-29T14:54:31.953Z | {"substantive": "N", "caseId": "12345", "jurisdiction": "SSCS", "caseType": "Benefit"}  |
      | IDAM        | 00001   | CASE      | [CLAIMANT]  | RESTRICTED      | SPECIFIC  | CITIZEN      | false    | 2022-03-30T14:54:31.953Z | {"substantive": "N", "caseId": "12345", "jurisdiction": "SSCS", "caseType": "Benefit"}  |
      | IDAM        | 00001   | CASE      | [DEFENDANT] | RESTRICTED      | SPECIFIC  | CITIZEN      | false    | 2022-03-31T14:54:31.953Z | {"substantive": "N", "caseId": "12345", "jurisdiction": "SSCS", "caseType": "Benefit"}  |
    When merge service runs
    Then it should make POST request to "/am/role-assignments" with data as in "data/merge-request-multiple.json"

  Scenario: User Role merger makes different merge request if cases are different
    Given lau-idam fetch users returns
      | userId | email             | first name    | last name  | deletion timestamp        |
      | 00001  | 00001@example.net | first 00001   | last 00001 | 2023-08-23T00:00:01.023Z  |
      | 00002  | 00002@example.net | first 00002   | last 00002 | 2023-08-23T00:00:02.023Z  |
    And IdAM fetch user by email returns
      | id    | email             |
      | 00002 | 00002@example.net |
      | 00003 | 00001@example.net |
    And role-assignments fetch role assignments for user "00001" returns
      | actorIdType | actorId | roleType  | roleName    | classification  | grantType | roleCategory | readOnly | beginTime                | attributes                                                                              |
      | IDAM        | 00001   | CASE      | [CREATOR]   | RESTRICTED      | SPECIFIC  | CITIZEN      | false    | 2022-03-29T14:54:31.953Z | {"substantive": "N", "caseId": "98765", "jurisdiction": "SSCS", "caseType": "Benefit"}  |
      | IDAM        | 00001   | CASE      | [CLAIMANT]  | RESTRICTED      | SPECIFIC  | CITIZEN      | false    | 2022-03-30T14:54:31.953Z | {"substantive": "N", "caseId": "12345", "jurisdiction": "SSCS", "caseType": "Benefit"}  |
      | IDAM        | 00001   | CASE      | [DEFENDANT] | RESTRICTED      | SPECIFIC  | CITIZEN      | false    | 2022-03-31T14:54:31.953Z | {"substantive": "N", "caseId": "12345", "jurisdiction": "SSCS", "caseType": "Benefit"}  |
    When merge service runs
    Then it should make POST request to "/am/role-assignments" with data as in "data/merge-request-split-98765.json"
    And it should make POST request to "/am/role-assignments" with data as in "data/merge-request-split-12345.json"
