OpenConext-monitoring-tests
===========================

OpenConext-monitoring-tests provides a standalone Spring Boot Java application that performs tests on an OpenConext instance. 
The application can performs different health check tests that can be used to monitor the availability of a OpenConext (production) instance. 

The health endpoint is available on 'http://localhost:9000/health' and is secured with username / password.

Available tests
===============
Four different tests are available.

Login flow
----------
Test a complete SAML authentication of a user

- Use WebDriver to test the SAML login-flow
  - Login into a protected page on Mujina SP
  - Choose the 'SURFconext monitoring IdP' IdP from the WAYF
  - Assert that the login was successful making assertions about the username - e.g. John Doe

Metadata
--------
- Download the SAML metadata published by Engineblock:
  - IdP proxy metadata
  - SP proxy metadata
  - IdPs metadata
- Validate the validUntil date's

VOOT
---
- Get an access-token with client-secret for a SP that is configured with client credentials grant type in the authz-admin server
- Perform VOOT call with non-existent person urn and verify empty list of groups
- Perform VOOT call to fetch the groups for the person urn configured in monitor.properties and ensure the result is not empty

PDP
---
- Perform pdp policy request with a spEntityId and idpEntityId

Installation
============
Running the application locally requires a Java 8 VM:

```bash
mvn clean install
mvn spring-boot:run
curl -u user:secret 'http://localhost:9000/health'
```

Before running the tests:

* Add the monitoring IdP and SP in the serviceregistry of the OpenConext instance to monitor. 
* Configure the correct keys / values using Ansible and overwrite the defaults from application.properties


