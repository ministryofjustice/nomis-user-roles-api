# Script to remove all DPS roles and then add multiple DPS roles to list of users

This script takes a list of usernames (users file) and a list of role codes (roles file), 
The script loops through the users and calls 

* the delete caseload endpoint to remove the NWEB caseload which will remove all digital roles from the user 
* the add caseload endpoint to re-add the NEWB caseload
* the add role endpoint to add each role one
at a time.

## Requirements

* bash
* curl

## Adding users and roles

Add the list of nomis usernames to the users file

Add the list of role codes to the roles file

(one per line)

## Running the script

It is normally a good idea to run the script against preprod before running it in prod

You will need to obtain a bearer token from hmpps-auth and add it the bash command as a variable
when the curl request called it has the necessary authorization to perform the action.

you Will also need to add the correct variable for the environment you are running the script against

```./add-multiple-roles-to-users-loop.bash <dev|preprod|prod> <bearer token>```

if a user already has the role we are currently trying to add the response from the 
curl request will be http 409 but the script will continue with the next role 

## output

The script will output to the terminal what it is currently doing.

It will also output the response from each curl request into a file called added.txt