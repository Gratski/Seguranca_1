# Java Security Project
A WhatsApp like app has been developed in order to implement secure communications and to guarantee the 4 essencial security properties, which are Availability, Confidentiality, Integrity and Authenticity.

All user keys, key stores, as well as trust stores are already created
So we can only make use of three users
User passwords are the same both for application usage and to the corresponding
key stores access
username: aligator 	| password: uiuiui
username: joao		| password: yoyoyo
username: simao		| password: aiaiai

## Compile project
mkdir bin && javac -Xlint -d bin ./src/*/*.java

## All user key stores are stored in
keys/clients/

## Server key store is stored in
keys/server

## Clients trust store is stored in
keys/certificates.trustStore 

## Run server in the command line:
java -Djava.security.manager -Djava.security.policy==server.policy -cp ./bin/ server.MyWhatsServer 23456


## What follows next is an interaction of three users using MyWhats application
### Lets add all users by trying to retrieve all last messages
### NOTE: user passwords must be the same as their keystore password
#### Adding joao
java -Djava.security.manager -Djava.security.policy==client.policy -cp ./bin/ client.MyWhats joao 127.0.0.1:23456 -p yoyoyo -r

#### Adding simao
java -Djava.security.manager -Djava.security.policy==client.policy -cp ./bin/ client.MyWhats simao 127.0.0.1:23456 -p aiaiai -r

#### Adding aligator
java -Djava.security.manager -Djava.security.policy==client.policy -cp ./bin/ client.MyWhats aligator 127.0.0.1:23456 -p uiuiui -r

#### Now joao is going to say hello to simao
java -Djava.security.manager -Djava.security.policy==client.policy -cp ./bin/ client.MyWhats joao 127.0.0.1:23456 -p yoyoyo -m simao "Hello"

#### Simao replies very happy
java -Djava.security.manager -Djava.security.policy==client.policy -cp ./bin/ client.MyWhats simao 127.0.0.1:23456 -p aiaiai -m joao "Hey :)"

#### Its been 4 hours since they talked and simao wants to know what have been said so far
#### So he retrieves all messages from their conversation
java -Djava.security.manager -Djava.security.policy==client.policy -cp ./bin/ client.MyWhats simao 127.0.0.1:23456 -p aiaiai -r joao

#### Simao decided to add billy aligator junior to the conversation
#### And for that he creates a group and adds both joao and aligator to the new group
java -Djava.security.manager -Djava.security.policy==client.policy -cp ./bin/ client.MyWhats simao 127.0.0.1:23456 -p aiaiai -a joao GATORFCUL

java -Djava.security.manager -Djava.security.policy==client.policy -cp ./bin/ client.MyWhats simao 127.0.0.1:23456 -p aiaiai -a aligator GATORFCUL

#### Simao welcome both joao and aligator
java -Djava.security.manager -Djava.security.policy==client.policy -cp ./bin/ client.MyWhats simao 127.0.0.1:23456 -p aiaiai -m GATORFCUL "Hey guys!! :D "

#### Aligator replies to group
java -Djava.security.manager -Djava.security.policy==client.policy -cp ./bin/ client.MyWhats aligator 127.0.0.1:23456 -p uiuiui -m GATORFCUL "Whats up"

#### joao asks if someone wants to do some 24h hackaton
java -Djava.security.manager -Djava.security.policy==client.policy -cp ./bin/ client.MyWhats joao 127.0.0.1:23456 -p yoyoyo -m GATORFCUL "24H Hackaton, anyone? "

#### aligator replies to group
java -Djava.security.manager -Djava.security.policy==client.policy -cp ./bin/ client.MyWhats aligator 127.0.0.1:23456 -p uiuiui -m GATORFCUL "What? no way! "

#### joao obtains all group messages
java -Djava.security.manager -Djava.security.policy==client.policy -cp ./bin/ client.MyWhats joao 127.0.0.1:23456 -p yoyoyo -r GATORFCUL

#### joao tries to kick aligator out of group
java -Djava.security.manager -Djava.security.policy==client.policy -cp ./bin/ client.MyWhats joao 127.0.0.1:23456 -p yoyoyo -d aligator GATORFCUL

#### But he can't because he is not the group owner!!!
#### So joao decided to encourage simao to kick aligator
java -Djava.security.manager -Djava.security.policy==client.policy -cp ./bin/ client.MyWhats joao 127.0.0.1:23456 -p yoyoyo -m simao "Hey jamas! Kick aligator from GATORFCUL. He doesn't like to code -.-' "

#### simao totally agrees with joao and imediatly kicks aligator from GATORFCUL
java -Djava.security.manager -Djava.security.policy==client.policy -cp ./bin/ client.MyWhats simao 127.0.0.1:23456 -p aiaiai -d aligator GATORFCUL

#### aligator tries to say something more to group
java -Djava.security.manager -Djava.security.policy==client.policy -cp ./bin/ client.MyWhats aligator 127.0.0.1:23456 -p uiuiui -m GATORFCUL "Are you guys going..? "

#### But he can't because he is no longer part of the conversation
#### Hum... aligator tries to retrieve all conversation messages
java -Djava.security.manager -Djava.security.policy==client.policy -cp ./bin/ client.MyWhats aligator 127.0.0.1:23456 -p uiuiui -r GATORFCUL

#### Oh wait he can't too...

#### joao sends a picture of last years hackaton to simao so he can feel it!
java -Djava.security.manager -Djava.security.policy==client.policy -cp ./bin/ client.MyWhats joao 127.0.0.1:23456 -p yoyoyo -f simao myFile.txt

#### simao checks if joao said anything else
java -Djava.security.manager -Djava.security.policy==client.policy -cp ./bin/ client.MyWhats simao 127.0.0.1:23456 -p aiaiai -r joao

#### OMG! A picture of last years hackaton? no way... Gotta have it!
#### simao downloads the newly received file upload
java -Djava.security.manager -Djava.security.policy==client.policy -cp ./bin/ client.MyWhats simao 127.0.0.1:23456 -p aiaiai -r joao myFile.txt

#### simao figured out they need a third member in order to contest
#### simao imediatly adds gator to GATORFCUL group again and sends him a message
java -Djava.security.manager -Djava.security.policy==client.policy -cp ./bin/ client.MyWhats simao 127.0.0.1:23456 -p aiaiai -a aligator GATORFCUL

java -Djava.security.manager -Djava.security.policy==client.policy -cp ./bin/ client.MyWhats simao 127.0.0.1:23456 -p aiaiai -m GATORFCUL "Hey Gator you know what.. we know you don't like to code but... can you please be physically there with us? They have meetballs for you to eat xD "

#### aligator them checks what have been said on group so far (again)
java -Djava.security.manager -Djava.security.policy==client.policy -cp ./bin/ client.MyWhats aligator 127.0.0.1:23456 -p uiuiui -r GATORFCUL

#### And this time he can retrieve "all" messages again because he is a GATORFCUL member again

#### joao wants to check all the latest messages of his conversations
java -Djava.security.manager -Djava.security.policy==client.policy -cp ./bin/ client.MyWhats joao 127.0.0.1:23456 -p yoyoyo -r

#### joao wants to know more about GATORFCUL chat
java -Djava.security.manager -Djava.security.policy==client.policy -cp ./bin/ client.MyWhats joao 127.0.0.1:23456 -p yoyoyo -r GATORFCUL


## This conversations flow show how this application can be used and how it can not.
