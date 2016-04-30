Projecto de Seguranca

# Regras de uso
1. O porto utilizado para a ligação cliente-servidor é o 23456, não é aceite outro
2. Operações não conhecidas serão consideradas inválidas

# Compilar o projecto, estando na root
mkdir bin && javac -Xlint -d bin ./src/*/*.java

# Criar ficheiro de teste para envio
touch foto.jpg

# SERVER
## Para que o sistema possa receber pedidos de clientes é necessário iniciar o servidor
java -Djava.security.manager -Djava.security.policy==server.policy -cp ./bin/ server.MyWhatsServer 23456

# CLIENT
# Em seguida apresentamos uma sequencia possível de execução dos comandos disponíveis
## Registo de utilizadores, será ignorada a mensagem de erro visto o utilizador de destino não existir propositadamente,
## no segundo caso, a Joana tem de inserir a sua password na linha de comandos (a password será: joanaPass)
java -Djava.security.manager -Djava.security.policy==client.policy -cp ./bin/ client.MyWhats maria 127.0.0.1:23456 -p mariaPass -m blank_user ""
java -Djava.security.manager -Djava.security.policy==client.policy -cp ./bin/ client.MyWhats joana 127.0.0.1:23456 -m blank_user ""

## Envio de mensagem: Maria --> Joana
java -Djava.security.manager -Djava.security.policy==client.policy -cp ./bin/ client.MyWhats maria 127.0.0.1:23456 -p mariaPass -m joana "Olá!"

## Envio de mensagem: Joana --> Maria
java -Djava.security.manager -Djava.security.policy==client.policy -cp ./bin/ client.MyWhats joana 127.0.0.1:23456 -p joanaPass -m maria "Oi! Como estás?"

## Registo de mais um utilizador que decidiu juntar-se á aplicação
java -Djava.security.manager -Djava.security.policy==client.policy -cp ./bin/ client.MyWhats pedro 127.0.0.1:23456 -p pedroPass -m blank_user ""

## Criação de grupo. Maria cria o grupo AmigosForever e adiciona joana e pedro ao grupo
java -Djava.security.manager -Djava.security.policy==client.policy -cp ./bin/ client.MyWhats maria 127.0.0.1:23456 -p mariaPass -a joana AmigosForever
java -Djava.security.manager -Djava.security.policy==client.policy -cp ./bin/ client.MyWhats maria 127.0.0.1:23456 -p mariaPass -a pedro AmigosForever

## Envio de mensagem para grupo: Joana --> AmigosForever
java -Djava.security.manager -Djava.security.policy==client.policy -cp ./bin/ client.MyWhats joana 127.0.0.1:23456 -p joanaPass -m AmigosForever "Olá malta, a aula de hoje foi uma seca"

## Envio de mensagem para grupo: Pedro --> AmigosForever
java -Djava.security.manager -Djava.security.policy==client.policy -cp ./bin/ client.MyWhats pedro 127.0.0.1:23456 -p pedroPass -m AmigosForever "Não me digas nada... Vou começar a não ir."

## Remoção de grupo. Maria remove Pedro do grupo AmigosForever
java -Djava.security.manager -Djava.security.policy==client.policy -cp ./bin/ client.MyWhats maria 127.0.0.1:23456 -p mariaPass -d pedro AmigosForever

## Pedro tenta enviar mensagem para grupo e não consegue por já não fazer parte do grupo
java -Djava.security.manager -Djava.security.policy==client.policy -cp ./bin/ client.MyWhats pedro 127.0.0.1:23456 -p pedroPass -m AmigosForever "Malta estão por aí..?"

## Envio de ficheiro. Maria manda foto à Joana
java -Djava.security.manager -Djava.security.policy==client.policy -cp ./bin/ client.MyWhats maria 127.0.0.1:23456 -p mariaPass -f joana foto.jpg

## Visualização de conversa. Joana visualiza conversação
java -Djava.security.manager -Djava.security.policy==client.policy -cp ./bin/ client.MyWhats joana 127.0.0.1:23456 -p joanaPass -r AmigosForever
java -Djava.security.manager -Djava.security.policy==client.policy -cp ./bin/ client.MyWhats joana 127.0.0.1:23456 -p joanaPass -r maria

## Download de ficheiro. Joana descarrega o ficheiro da Maria
java -Djava.security.manager -Djava.security.policy==client.policy -cp ./bin/ client.MyWhats joana 127.0.0.1:23456 -p joanaPass -r maria foto.jpg

## Joana quer mais privacidade e decide enviar uma conversa privada para Maria
java -Djava.security.manager -Djava.security.policy==client.policy -cp ./bin/ client.MyWhats joana 127.0.0.1:23456 -p joanaPass -m maria "Ai o pedrinho foi mesmo parvo..."

## Maria quer ver as mais recentes mensagens em todas as suas conversações
java -Djava.security.manager -Djava.security.policy==client.policy -cp ./bin/ client.MyWhats maria 127.0.0.1:23456 -p mariaPass -r
