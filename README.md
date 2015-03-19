SURFConext-monitoring-tests
===========================

A standalone application that performs tests on an OpenConext instance.

Tests performed:

Login flow
-----------
- Start an embedded Jetty servlet container
- Deploy Mujina IdP and Mujina SP
- Use WebDriver to test the SAML login-flow
  - Login into a protected page on Mujina SP
  - Choose the 'SURFconext monitoring IdP' IdP from the WAYF
  - Assert that the login was successful making assertions about the username - e.g. John Doe

Metadata
------------
- Download the metadata from Engineblock:
  - IdP proxy metadata
  - SP proxy metadata
  - IdPs metadata
- Validate using XSDs
- Validate cryptographically, using the XML signature

API
------------
- Get an access-token with client-secret for a SP that is configured with client credentials grant type
- Perform API call with non-existent person urn and verify 404 response
- Perform API call to fetch the groups for the person urn configured in monitor.properties
- Perform API call to fetch the person and compare the displayName with the configured name in monitor.properties
