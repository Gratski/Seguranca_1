# Seguranca_1
Projecto de Seguranca

#### Compile project
mkdir bin && javac -Xlint -d bin ./src/*/*.java

#### Run server in the command line:
java -Djava.security.manager -Djava.security.policy==server.policy -cp ./bin/ server.MyWhatsServer 23456

#### Run client in the command line with some options:
java -Djava.security.manager -Djava.security.policy==client.policy -cp ./bin/ client.MyWhats simao 127.0.0.1:23456 -p pass -m joao "mensagem teste com espaços"



TODO:
- Retirar do client e do server os comandos SetProperty e colocar nos comandos da linha de comandos
- Não guardar o hash do Salt no ficheiro das passwords
- Bug onde as mensagens são enviadas sem key para quem não as pode ler
- Quando se remove um user de um grupo, temos de apagar todas as suas keys dos ficheiros!
- Criar no Proxy as constantes para as extensões de ficheiros ".key" e ".sig"
- Remover ficheiro "index" de FILES
- Fazer readme com script