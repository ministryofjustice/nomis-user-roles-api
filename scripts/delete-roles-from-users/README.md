# Script to remove single or multiple roles from list of users

This script takes a list of usernames (users file) and a list of role codes (roles file), 
The script loops through the users and calls the remove role endpoint to remove each role one 
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

you will also need to add the correct variable for the environment you are running the script against

```./delete-multiple-roles-from-users-loop.bash <dev|preprod|prod> <bearer token>```

if a user doesn't have the role we are currently trying to remove the response from the 
curl request will be http 404, and a user"userMessage":"Role not found: Role xxx is not assigned to this user"

## output

The script will output to the terminal what it is currently doing.

It will also output the response from each curl request into a file called removed.txt