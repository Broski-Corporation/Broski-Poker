#!/bin/bash

SERVER_IP="104.248.45.171"
SERVER_USER="poker-server"

echo "Building server JAR..."
./gradlew serverJar

echo "Preparing server for deployment..."
ssh -i ./rc $SERVER_USER@$SERVER_IP "cd /home/poker-server/poker-server && ./deploy.sh prepare"

echo "Uploading to server..."
scp -i ./rc core/build/libs/poker-server-1.0.0.jar $SERVER_USER@$SERVER_IP:/home/poker-server/poker-server/jars/poker-server.jar

echo "Setting permissions..."
ssh -i ./rc $SERVER_USER@$SERVER_IP "chmod 755 /home/poker-server/poker-server/jars/poker-server.jar"

echo "Starting server..."
ssh -i ./rc $SERVER_USER@$SERVER_IP "cd /home/poker-server/poker-server && ./deploy.sh start"

echo "Checking server logs..."
ssh -i ./rc $SERVER_USER@$SERVER_IP "journalctl -u poker-server -n 10 --no-pager"

echo "Server status:"
ssh -i ./rc $SERVER_USER@$SERVER_IP "systemctl status poker-server"
