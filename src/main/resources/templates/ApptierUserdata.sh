#!/bin/bash
sudo apt update -y
sudo apt install git -y
sudo apt-get install openjdk-11-jdk -y
sudo su - ubuntu
cd /
cd home/ubuntu/classifier
git clone https://github.com/rithvikmerugu98/CSE546Assignment1AppServer.git
cd CSE546Assignment1AppServer
./mvnw spring-boot:run