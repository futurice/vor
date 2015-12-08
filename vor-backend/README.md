# Futu_2 Node.js app

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
