# Vör app

## Backend for sensors and clients
- Uses websockets for communication
- Caches some messages for short period

### Requirements
- Node.js 4.1 [node.js homepage](https://nodejs.org/en/)
- Redis [Redis quickstart](http://redis.io/topics/quickstart)

### Local setup
1. Run Redis ```redis-server``` 
2. Install dependencies:```npm install```
3. Start server and watch changes: ```npm run watch```
4. Visit [http://localhost:8080/](http://localhost:8080/)  to test different events

### Other commands
- To lint (ESlint and JSCS): ```npm run lint```
- To run tests: ```npm run test```
- To run tests continuously: ```npm run test-watch```


### Making a deploy - using [Dokku](http://dokku.viewdocs.io/dokku/) 
1. SSH to your host server
2. On host server, add ssh key for dokku - [help](https://www.digitalocean.com/community/questions/dokku-add-new-ssh-key)
3. On your machine, add a remote repo to your local repository: <br/>```git remote add dokku dokku@<your host domain/ip>:<app name>```
4. To push local vor-backend to Dokku, run in the project root folder (vor):<br/>``` git push dokku `git subtree split --prefix vor-backend <your branch>`:master```
