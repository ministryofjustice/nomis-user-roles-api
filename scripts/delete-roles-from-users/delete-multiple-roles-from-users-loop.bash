#!/usr/bin/env bash
set -e

SCRIPTS_BASE_DIR=`pwd`

ENV=${1}
BEARER_TOKEN=${2}

enforce_var_set() {
  if [[ ! -v $1 || -z $(eval "echo \$$1") ]]; then
    echo "$1 environment variable not set or is empty.  Please see README.md."
    exit 1
  fi
}

enforce_var_set ENV
enforce_var_set BEARER_TOKEN

if [ "$ENV" == "local" ]; then
  BASE_URL="http://localhost:8082"
elif [ "$ENV" == "dev" ]; then
  BASE_URL="https://nomis-user-roles-api-dev.prison.service.justice.gov.uk"
elif [ "$ENV" == "preprod" ]; then
  BASE_URL="https://nomis-user-roles-api-preprod.prison.service.justice.gov.uk"
elif [ "$ENV" == "prod" ]; then
  BASE_URL="https://nomis-user-roles-api.prison.service.justice.gov.uk"
else
  echo "Invalid environment specified"
  exit 1
fi

for user in $(cat $SCRIPTS_BASE_DIR/users); do
  user=$(echo "$user" | tr '[:lower:]' '[:upper:]')
  echo "updating user  - $user"

    for role in $(cat $SCRIPTS_BASE_DIR/roles); do

    echo "removing $role from $user"

    removed=$(curl -X 'DELETE' --location "$BASE_URL/users/${user}/roles/${role}?caseloadId=NWEB" \
    --header 'Content-Type: application/json' \
    --header "Authorization: Bearer $BEARER_TOKEN" \
    )
    echo $removed
    echo "$role removed from $user"
    echo "$user and $role = $removed" >> $SCRIPTS_BASE_DIR/removed.txt
    done
done

