#!/bin/bash
# make sure that the lifespan of accessToken is sufficient
RESULT=`curl --data "username=keycloak&password=k&grant_type=password&client_id=admin-cli" http://localhost:8080/auth/realms/master/protocol/openid-connect/token`

TOKEN=`echo $RESULT | sed 's/.*access_token":"//g' | sed 's/".*//g'`

echo "====    user creation   ===="
for i in {1..1000}
do
  USERNAME="test-$i"
  PAYLOAD="{\"username\":\"$USERNAME\",\"enabled\":true,\"credentials\":[{\"type\":\"password\",\"value\":\"t\"}]}"
  echo "create user $USERNAME"
  curl -H "Authorization: bearer $TOKEN" -H "Content-Type: application/json" --data "$PAYLOAD" http://localhost:8080/auth/admin/realms/test/users
done
echo "==== user creation done ===="
