# Script to add multiple roles to list of users

This script takes a list of usernames (users file) and a list of role codes (roles file), 
The script loops through the users and calls the add role endpoint to add each role one 
at a time.

## Requirements

* bash
* curl

## Adding users and roles

Add the list of nomis usernames to the users file

Add the list of role codes to the roles file

(one per line)

## Running the script

You will need to obtain a bearer token from hmpps-auth and add it to the script so that 
when the curl request called it has the necessary authorization to perform the action.

```./add-multiple-roles-to-users-loop.bash```

if a user already has the role we are currently trying to add the response from the 
curl request will be http 409 but the script will continue with the next role 

## output

The script will output to the terminal what it is currently doing.

It will also output the response from each curl request into a file called added.txt