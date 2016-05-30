OpenConext-monitoring-tests
===========================

OpenConext-monitoring-tests provides a standalone Java application that performs tests on an OpenConext instance. The application can perform four different tests that can be used to monitor the availability of a OpenConext (production) instance. Scripts are provided to call the tests from Nagios.

The monitoring application contains an embedded Mujina that provides the SAML IdP and SAML SP used in the tests. Selumium WebDriver is used as webbrowser in the tests.

Available tests
===============
Four different tests are available.


Login flow
----------
Test a complete SAML authentication of a user

- Start an embedded Jetty servlet container
- Deploy Mujina IdP and Mujina SP
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
- Validate the downloaded metadata using the SAML metadata XSDs
- Verify the XML signature on the metadata
- Validate that the metadata was signed using the certificate configured in the test

API
---
- Get an access-token with client-secret for a SP that is configured with client credentials grant type
- Perform API call with non-existent person urn and verify 404 response
- Perform API call to fetch the groups for the person urn configured in monitor.properties
- Perform API call to fetch the person and compare the displayName with the configured name in monitor.properties

VOOT
---
- Get an access-token with client-secret for a SP that is configured with client credentials grant type in the authz-admin server
- Perform VOOT call with non-existent person urn and verify empty list of groups
- Perform VOOT call to fetch the groups for the person urn configured in monitor.properties and ensure the result is not empty

Installation
============
Running the application requires a Java 7 VM

Build the application from source or download a prebuild version from: [https://github.com/OpenConext/OpenConext-monitoring-tests/releases](https://github.com/OpenConext/OpenConext-monitoring-tests/releases)

The tests are run using the ```monitor-*.sh``` scripts provided in ```/src/main/deploy```

Before running the tests:

* Create a certificate and private key for use by the monitoring test. Refer to ```KEYS_CREATION.txt``` in ```/src/main/deploy```.
* Add the monitoring IdP and SP in the serviceregistry of the OpenConext instance to monitor. Refer to ```OPENCONEXT_CONFIGURATION.md``` in ```/src/main/deploy```.
* Configure ```monitor.properties``` in ```/src/main/deploy```. Refer to ```README``` in ```/src/main/deploy```.


