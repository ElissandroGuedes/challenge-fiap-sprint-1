\# 📱 ChallengeFiap – App Android com Firebase



Este projeto é um aplicativo Android desenvolvido como parte do desafio técnico da FIAP. Ele utiliza Firebase para autenticação, banco de dados e envio de notificações push.



---



\## 🚀 Funcionalidades



\- Autenticação de usuários com Firebase Authentication

\- Listagem de campanhas mockadas

\- Envio automático de notificações via Firebase Cloud Messaging (FCM)

\- Backend com Firebase Functions para disparo de push segmentado



---



\## 🛠️ Tecnologias Utilizadas



\- Android Studio (Java/Kotlin)

\- Firebase Authentication

\- Firebase Firestore

\- Firebase Cloud Messaging (FCM)

\- Firebase Functions (Node.js)



---



\## 📦 Estrutura do Projeto



projeto\_finalizado/ ├── ChallengeFiap/        

\# Projeto Android Studio ├── push\_campaign/         

\# Funções Firebase └── app-release.apk        

\# APK gerado para instalação





---



\## 📲 Como instalar o app



1\. Baixe o arquivo `app-release.apk`

2\. Instale no seu dispositivo Android (ativando “Fontes desconhecidas” se necessário)

3\. Faça login com um usuário válido



---



\## ☁️ Como rodar as funções Firebase



1\. Instale o Firebase CLI

2\. Navegue até a pasta `push\_campaign`

3\. Execute:

&nbsp;  ```bash

&nbsp;  firebase deploy --only functions


## ⚠️ Arquivo `google-services.json`

Por motivos de segurança, o arquivo `google-services.json` **não está incluído neste repositório**. Ele contém credenciais sensíveis do Firebase e foi removido do controle de versão para evitar exposição pública.

### 🔧 Para executar o app corretamente:

- Insira o arquivo `google-services.json` na pasta `ChallengeFiap/app/` do projeto Android Studio
- Esse arquivo deve ser obtido diretamente com o autor do projeto ou via compartilhamento privado (Google Drive, e-mail, etc.)



\## Autores



Elissandro dos Santos Guedes –RM559537 

Gustavo Moraes Rego Fonseca – RM553411 

Gabriel Ferreira do Vale - RM560780 

Pedro Henrique Vieira Bispo – RM5600580

