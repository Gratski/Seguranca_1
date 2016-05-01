# Seguranca_1
Projecto de Seguranca

## Compile project
mkdir bin && javac -Xlint -d bin ./src/*/*.java

## All user key stores are stored in
keys/clients/

## Server key store is stored in
keys/server

## Clients trust store is stored in
keys/certificates.trustStore 

## Run server in the command line:
java -Djava.security.manager -Djava.security.policy==server.policy -cp ./bin/ server.MyWhatsServer 23456 segredo


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
java -Djava.security.manager -Djava.security.policy==client.policy -cp ./bin/ client.MyWhats joao 127.0.0.1:23456 -p yoyoyo -m simao "Hello!"

#### Simao replies very happy
java -Djava.security.manager -Djava.security.policy==client.policy -cp ./bin/ client.MyWhats simao 127.0.0.1:23456 -p aiaiai -m joao "Hey!!!"

#### Its been 4 hours since they talked and simao wants to know what have been said so far
#### So he retrieves all messages from their conversation
java -Djava.security.manager -Djava.security.policy==client.policy -cp ./bin/ client.MyWhats simao 127.0.0.1:23456 -p aiaiai -r joao

#### Simao decided to add billy aligator junior to the conversation
#### And for that he creates a group and adds both joao and aligator to the new group
java -Djava.security.manager -Djava.security.policy==client.policy -cp ./bin/ client.MyWhats simao 127.0.0.1:23456 -p aiaiai -a joao GATORFCUL

java -Djava.security.manager -Djava.security.policy==client.policy -cp ./bin/ client.MyWhats simao 127.0.0.1:23456 -p aiaiai -a aligator GATORFCUL

#### Simao welcome both joao and aligator
java -Djava.security.manager -Djava.security.policy==client.policy -cp ./bin/ client.MyWhats simao 127.0.0.1:23456 -p aiaiai -m GATORFCUL "Hey guys!! :D"

#### Aligator replies to group
java -Djava.security.manager -Djava.security.policy==client.policy -cp ./bin/ client.MyWhats aligator 127.0.0.1:23456 -p uiuiui -m GATORFCUL "Whats up!"

#### joao asks if someone wants to do some 24h hackaton
java -Djava.security.manager -Djava.security.policy==client.policy -cp ./bin/ client.MyWhats joao 127.0.0.1:23456 -p yoyoyo -m GATORFCUL "24H Hackaton, anyone!? :D"

#### aligator replies to group
java -Djava.security.manager -Djava.security.policy==client.policy -cp ./bin/ client.MyWhats aligator 127.0.0.1:23456 -p uiuiui -m GATORFCUL "Wtf!? no way!"

#### joao tries to kick aligator out of group
java -Djava.security.manager -Djava.security.policy==client.policy -cp ./bin/ client.MyWhats joao 127.0.0.1:23456 -p yoyoyo -d aligator GATORFCUL

#### But he can't because he is not the group owner!!!
#### So joao decided to encourage simao to kick aligator
java -Djava.security.manager -Djava.security.policy==client.policy -cp ./bin/ client.MyWhats joao 127.0.0.1:23456 -p yoyoyo -m simao "Hey jamas! Kick aligator from GATORFCUL. He doesn't like to code -.-'"

#### simao totally agrees with joao and imediatly kicks aligator from GATORFCUL
java -Djava.security.manager -Djava.security.policy==client.policy -cp ./bin/ client.MyWhats simao 127.0.0.1:23456 -p aiaiai -d aligator GATORFCUL

#### aligator tries to say something more to group
java -Djava.security.manager -Djava.security.policy==client.policy -cp ./bin/ client.MyWhats aligator 127.0.0.1:23456 -p uiuiui -m GATORFCUL "Are you guys going..?"

#### But he can't because he is no longer part of the conversation
#### Hum... aligator tries to retrieve all conversation messages
java -Djava.security.manager -Djava.security.policy==client.policy -cp ./bin/ client.MyWhats aligator 127.0.0.1:23456 -p uiuiui -r GATORFCUL

#### Oh wait he can't too...

#### joao sends a picture of last years hackaton to simao so he can feel it!
java -Djava.security.manager -Djava.security.policy==client.policy -cp ./bin/ client.MyWhats joao 127.0.0.1:23456 -p yoyoyo -m simao myFile.txt

#### simao downloads the newly received file upload
java -Djava.security.manager -Djava.security.policy==client.policy -cp ./bin/ client.MyWhats simao 127.0.0.1:23456 -p aiaiai -r joao myFile.txt


## This conversations flow show how this application can be used and how it can not.


TODO:
==============
- Retirar do client e do server os comandos SetProperty e colocar nos comandos da linha de comandos
- Não guardar o hash do Salt no ficheiro das passwords
- Bug onde as mensagens são enviadas sem key para quem não as pode ler
- Quando se remove um user de um grupo, temos de apagar todas as suas keys dos ficheiros!
- Alterar ficheiros "user.key" para "message.key.user"
- Criar no Proxy as constantes para as extensões de ficheiros ".key" e ".sig"
- Remover ficheiro "index" de FILES
