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
1. SSH to Rasberry PI [Rasberry PI support](https://www.raspberrypi.org/documentation/remote-access/ssh/)
2. Install node.js ^5.0.0 [more info](http://elinux.org/Node.js_on_RPi)
3. Install fswebcam [more info](https://www.raspberrypi.org/documentation/usage/webcams/)
4. Clone this repo to var folder and cd vor/vor-image-client/
5. Run ```npm install``` with sudo if needed
6. Run ```crontab -u pi -e``` and select preferred editor
5. Add a command to run on Rasberry PI start:
  ```
    @reboot
    cd /var/vor/vor-image-client/
     /usr/bin/sudo -u pi -H 
     SOCKET_SERVER=<socket-server-url> 
     LISTEN_TYPE=<socket-event-type> 
     LISTEN_ID=<event-emitter-id>  S
     END_TYPE=<event-type> 
     SEND_ID=<image-client-id> 
     /usr/local/bin/npm run serve
  ```
  
6. Restart Rasberry PI
