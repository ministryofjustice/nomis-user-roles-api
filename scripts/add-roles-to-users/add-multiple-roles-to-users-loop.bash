#!/usr/bin/env bash
set -e

SCRIPTS_BASE_DIR=`pwd`

for user in $(cat $SCRIPTS_BASE_DIR/users); do
  echo "updating user  - $user"

    for role in $(cat $SCRIPTS_BASE_DIR/roles); do

    echo "adding $role to $user"

#LOCAL
#     added=$(curl  -X 'POST' --location "http://localhost:8082/users/${user}/roles/${role}?caseloadId=NWEB" \
#    --header 'Content-Type: application/json' \
#    --header 'Authorization: Bearer <token>' \
#    )

#DEV
#     added=$(curl  -X 'POST' --location "https://nomis-user-roles-api-dev.prison.service.justice.gov.uk/users/${user}/roles/${role}?caseloadId=NWEB" \
#    --header 'Content-Type: application/json' \
#    --header 'Authorization: Bearer <token>' \
#    )

#PREPROD
#     added=$(curl  -X 'POST' --location "https://nomis-user-roles-api-preprod.prison.service.justice.gov.uk/users/${user}/roles/${role}?caseloadId=NWEB" \
#    --header 'Content-Type: application/json' \
#    --header 'Authorization: Bearer <token>' \
#    )

#PROD
    added=$(curl -X 'POST' --location "https://nomis-user-roles-api.prison.service.justice.gov.uk/users/${user}/roles/${role}?caseloadId=NWEB" \
    --header 'Content-Type: application/json' \
    --header 'Authorization: Bearer <token>' \
    )
    echo "$added"
    echo "$role added to $user"
    echo "$user and $role = $added" >> $SCRIPTS_BASE_DIR/added.txt
    done
done

