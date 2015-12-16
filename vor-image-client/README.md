# Vör image client

## A light-weight node.js client(server) installed in Rasperry PI 2 
- connects to backend using websockets
- takes a picture on certain socket event (see production config/)

### Requirements
- Node.js 5.0.0 [node.js homepage](https://nodejs.org/en/)
- command line camera app, uses imagesnap in development and 
fswebcam in production. This can be changed in config. 

### Local setup
1. Install dependencies:```npm install```
2. Start server and watch changes: ```npm run start```
3. App runs in port 9000

### Other commands
- To lint ESlint: ```npm run eslint```
- To run tests: ```npm run test```
- To run tests continuously: ```npm run test-watch```


### Deploying to Rasperry PI

