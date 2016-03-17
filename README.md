# Seguranca_1
Projecto de Seguranca


#### Run server in the command line:
java -Djava.security.manager -Djava.security.policy==server.policy -cp ./bin/ server.MyWhatsServer 23456

#### Run client in the command line with some options:
java -Djava.security.manager -Djava.security.policy==client.policy -cp ./bin/ client.MyWhats simao 127.0.0.1:23456 -p pass -m joao "mensagem teste com espaços"

TODO:
- Test client/server with diferent computers (to check permissions)
- Do Txt with how to run the project
- Test in FCUL Labs
- Make Report