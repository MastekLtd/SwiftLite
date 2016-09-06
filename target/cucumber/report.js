$(document).ready(function() {var formatter = new CucumberHTML.DOMFormatter($('.cucumber-report'));formatter.uri("Projects/DemoAuto1/Feature/test.feature");
formatter.feature({
  "line": 2,
  "name": "Smoke Test Feature",
  "description": "",
  "id": "smoke-test-feature",
  "keyword": "Feature"
});
formatter.before({
  "duration": 356142,
  "status": "passed"
});
formatter.scenario({
  "line": 5,
  "name": "Add Patients \u003cSC_TC1\u003e",
  "description": "",
  "id": "smoke-test-feature;add-patients-\u003csc-tc1\u003e",
  "type": "scenario",
  "keyword": "Scenario",
  "tags": [
    {
      "line": 4,
      "name": "@SmokeTest"
    }
  ]
});
formatter.step({
  "line": 7,
  "name": "User Story for Adding Patients, Login to Health page",
  "keyword": "Given "
});
formatter.step({
  "line": 8,
  "name": "do add two patients \u003cAddPatient\u003e",
  "keyword": "When "
});
formatter.step({
  "line": 9,
  "name": "verify the patients were added to table...",
  "keyword": "And "
});
formatter.step({
  "line": 10,
  "name": "Validate Web Table \u003cVerifyRanorexVIP\u003e",
  "keyword": "Then "
});
formatter.match({
  "arguments": [
    {
      "val": "User Story for Adding Patients, Login to Health page",
      "offset": 0
    }
  ],
  "location": "StepDefinitions.user_on_webpage(String)"
});
formatter.result({
  "duration": 106038912,
  "status": "passed"
});
formatter.match({
  "arguments": [
    {
      "val": "do add two patients \u003cAddPatient\u003e",
      "offset": 0
    }
  ],
  "location": "StepDefinitions.user_on_webpage(String)"
});
formatter.result({
  "duration": 7201313004,
  "status": "passed"
});
formatter.match({
  "arguments": [
    {
      "val": "verify the patients were added to table...",
      "offset": 0
    }
  ],
  "location": "StepDefinitions.user_on_webpage(String)"
});
formatter.result({
  "duration": 35922,
  "status": "passed"
});
formatter.match({
  "arguments": [
    {
      "val": "Validate Web Table \u003cVerifyRanorexVIP\u003e",
      "offset": 0
    }
  ],
  "location": "StepDefinitions.user_on_webpage(String)"
});
formatter.result({
  "duration": 333485746,
  "status": "passed"
});
});