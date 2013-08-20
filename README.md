SURFConext-monitoring-tests
===========================

A standalone application that performs some tests on a running instance of OpenConext.

Tests performed:

Login flow
-----------
- Start a(n embedded) Jetty servlet container
- Deploys Mujina IdP and Mujina SP
- Use WebDriver to test the SAML login-flow.

Metadata
------------
- Download the metadata from Engineblock:
  - IdP proxy metadata
  - SP proxy metadata
  - IdPs metadata
- Validate using XSDs
- Validate cryptographically, using the XML signature
