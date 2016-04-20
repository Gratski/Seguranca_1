# Seguranca_1
Projecto de Seguranca

#### Compile project
mkdir bin && javac -Xlint -d bin ./src/*/*.java

#### Run server in the command line:
java -Djava.security.manager -Djava.security.policy==server.policy -cp ./bin/ server.MyWhatsServer 23456

#### Run client in the command line with some options:
java -Djava.security.manager -Djava.security.policy==client.policy -cp ./bin/ client.MyWhats simao 127.0.0.1:23456 -p pass -m joao "mensagem teste com espaços"




recebe pedido
envia contacts e certificates para o cliente
recebe assinatura digital, guardar em .sig
recebe as chaves cifradas a guardar para cada user
