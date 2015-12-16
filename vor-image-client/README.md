# Vör Image Client

## A light-weight node.js client(server) installed in Rasperry PI 2 
- connects to backend using websockets
- takes a picture on socket event

### Requirements
- Node.js 5.0.0 [node.js homepage](https://nodejs.org/en/)
- command line camera app, uses [ImageSnap](http://iharder.sourceforge.net/current/macosx/imagesnap/) in development and 
[fswebcam](http://manpages.ubuntu.com/manpages/lucid/man1/fswebcam.1.html) in production. These can be changed in configs. 

### Local setup
1. Install dependencies:```npm install```
2. Start server and watch changes: ```npm run start```
3. App runs in port 9000

### Other commands
- To lint ESlint: ```npm run eslint```
- To run tests: ```npm run test```
- To run tests continuously: ```npm run test-watch```

### Deploying to Rasperry PI
1. Install node.js ^5.0.0
2. SSH to Rasperry PI [Rasperry PI support](https://www.raspberrypi.org/documentation/remote-access/ssh/)
3. Run ```npm install``` with sudo if needed
4. Run ```npm run serve``` with sudo if needed
