Before running the SAML authentication the mujiina IdP and SP used by the test must be added to OpenConext. The instructions below assume you are using the OpenConext servieregistry to add the IdP and the SP.

Monitor SP
==========
1) Add a new SP Connection with entityID (Connection ID) ```https://monitoring-sp```

Edit the connection.

2) Set State to ```Production```

3) In the Metadata tab set:

* Set ```description:*```, ```name:*```, ```displayName:*``` to ```OpenConext monitoring SP```.
* ```AssertionConsumerService:0:Binding``` to ```urn:oasis:names:tc:SAML:2.0:bindings:HTTP-POST```
* ```AssertionConsumerService:0:Location``` to ```https://localhost:8443/sp/```
* Set ```coin:no_consent_required``` to true. This prevents the consent screen from being shown in the test.
* Set ```coin:ss:idp_visible_only``` to true. This prevents SP from being visible in the OpenConext Dashboard.
* The the other attributes (contact infomation, logo, ...) as desired

4) In the ARP tab allow the attrubute:

* cn
* displayName
* givenName
* mail
* schacHomeOrganization
* sn
* uid

5) Set the ACL in the Indentity Provider (IdP) tab to ```Allow all```

6) Save


Monitor IdP
===========

1) Add a new IdP Connection with entityID (Connection ID) ```https://monitoring-idp```

2) Set State to ```Production```

3) In the Metadata tab set:

* Set ```description:*```, ```name:*```, ```displayName:*``` to ```OpenConext monitoring IdP```. This string is used by the test to find the IdP in the WAYF, so this must match exaclty.
* Set ```SingleSignOnService:0``` to ```urn:oasis:names:tc:SAML:2.0:bindings:HTTP-POST``` (note: not HTTP-Redirect. This is not supported by the Mujina SP).
* Set ```SingleSignOnService:0:Location``` to ```https://localhost:8443/idp/SingleSignOnService```
* Set ```cert``` to the certificate you generated for the monitoring test.

5) Set the ACL in the Service Provider (SP) tab to allow:
* ```https://monitoring-sp```
* ```https://api.surfconext.nl/```. Required for the API test. Replace "surfconext.nl" with the domain of the OpenConext instance.

7) Save


One last step
=============

You must allow one more other production IdP access to the Monitor SP, otherwise no WAYF is shown during the test. This second IdP does not have to be functional, it will not be used in the test.
