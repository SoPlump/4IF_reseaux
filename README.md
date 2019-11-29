# README

This project is composed of two sub-projects :
* building socket-based distributed systems (concretely it's a chat)
* making a http server

## Launch the Chat

### Objectives

* Building distributed systems
* Synchronous network communication systems
* Introduction to java sockets

You first need to launch the server before connecting to it with clients. To do it easily, you can do it with gradle (open the root build.gradle file, and then after building with gradle; run the wanted app : [client||server] > Tasks > application > run).

Tools :
* connect to a server
* connect with your account
* register a new user
* connect to any group (if you're in the whitelist)
* create a new group
* add user to a group (when you're on the group)
* send a message on a group
* see all existing groups
* see all users in a group
* disconnect
* quit

## Launch the HTTP Server

### Objectives

* Building distributed systems
* Synchronous network communication systems
* Introduction to HTTP protocol and HTTP servers

Open the build.gradle project and run it. 
We recommend you to use Postman (and not our bad looking html page).

We implemented the **GET**, **POST**, **PUT**, **HEAD**, **DELETE**.

## Environments

java 11
IntelliJ
Gradle
JavaFX