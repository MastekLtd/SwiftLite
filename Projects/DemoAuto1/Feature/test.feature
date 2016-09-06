
Feature: Smoke Test Feature

@SmokeTest
Scenario: Add Patients <SC_TC1>

Given User Story for Adding Patients, Login to Health page
When  do add two patients <AddPatient>
And   verify the patients were added to table... 
Then  Validate Web Table <VerifyRanorexVIP>