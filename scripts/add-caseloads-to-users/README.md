# Script to add single or multiple caseloads to list of users

This script takes a list of usernames (users file) and a list of caseload codes (caseloads file), 
The script loops through the users and calls the add caseload endpoint to add each caseload one 
at a time.

## Requirements

* bash
* curl

## Adding users and caseloads

Add the list of nomis usernames to the users file

Add the list of caseload codes to the caseloads file

(one per line, with additional line at end otherwise last line will not be included)

## Running the script

It is normally a good idea to run the script against preprod before running it in prod

You will need to obtain a bearer token from hmpps-auth and add it the bash command as a variable
when the curl request called it has the necessary authorization to perform the action.

you will also need to add the correct variable for the environment you are running the script against

```./add-multiple-caseloads-to-users-loop.bash <dev|preprod|prod> <bearer token>```

if a user already has the caseload we are currently trying to add the response from the 
curl request will be http 409 but the script will continue with the next caseload 

## output

The script will output to the terminal what it is currently doing.

It will also output the response from each curl request into a file called added.txt