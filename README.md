# Seguranca_1
Projecto de Seguranca


#### Run server in the command line:
java -Djava.security.manager -Djava.security.policy==server.policy -cp ./bin/ server.MyWhatsServer 8080

#### Run client in the command line with some options:
java -Djava.security.manager -Djava.security.policy==client.policy -cp ./bin/ client.MyWhats simao 127.0.0.1:8080 -p pass -m joao "mensagem teste com espaços"

TODO:
- Change Client flow
- Test client/server with diferent computers (to check permissions)
- Refactor printing Reply responsability
- Add Tests from Requests for each case
- Refactor Reply/NetworkMessage
- Refactor some path constants from Proxy to others
- Make executable?
- Make Javadoc
- Try to see if everything is synchronized?
- Test in FCUL Labs
- Make Report