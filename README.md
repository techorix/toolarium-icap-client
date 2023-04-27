
# toolarium-icap-client fork 'shared-icap'

Implements an ICAP client in java compliant with [RFC 3507](https://www.ietf.org/rfc/rfc3507.txt) and is in itself
a Rest-Service that relays incoming files to an ICAP-Service.

In addition to the original client at Toolarium, this fork 
* uses the Quarkus framework 
* Testcontainers 
* Tries to keep everything deployable in a read-only environment

This is mostly meant as a prove of concept, please consider simply using your ICAP service as a HTTP proxy before considering this solution.

If you want to build it within Gitlab CI, here are some commands you could use:
```yaml
script:
    - cp src/test/resources/.testcontainers.properties ~
    - ./gradlew build -Pversion=1.0.0-${CI_COMMIT_SHORT_SHA} -Dquarkus.helm.repository.username=${ARTI_USER} -Dquarkus.helm.repository.password=${ARTI_PASS} 
    - helm upgrade --install  --set ociConfigJson.username=${ARTI_USER} --set ociConfigJson.password=${ARTI_PASS} shared-icap build/helm/kubernetes/shared-icap
```

Before you do attempt to deploy anything you should set a password in `icap-users.properties` and adjust necessary settings in the `application.properties` and `helm/values.yaml`.