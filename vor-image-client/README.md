# Vör Image Client

## A light-weight node.js client(server) installed in Raspberry PI 1 or 2 
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

### Deploying to Raspberry PI
1. Install Raspian version ^8 to Raspberry PI
2. SSH to Raspberry PI [Raspberry PI support](https://www.raspberrypi.org/documentation/remote-access/ssh/)
3. Install node.js ^5.0.0 [more info](http://blog.wia.io/installing-node-js-v4-0-0-on-a-raspberry-pi/)
    - use https://nodejs.org/dist/v5.0.0/node-v5.0.0-linux-armv6l.tar.gz for Raspberry PI 1
    - use https://nodejs.org/dist/v5.0.0/node-v5.0.0-linux-armv7l.tar.gz for Raspberry PI 2
4. Run: ```sudo apt-get update``` to update package manager
4. Run: ```sudo apt-get install fswebcam``` to install fswebcam [more info](https://www.raspberrypi.org/documentation/usage/webcams/)
5. Run: ```cd /var``` and run: ```sudo git clone https://github.com/futurice/vor.git``` to clone this repo
6. Run: ```cd /var/vor/vor-image-client``` and run:  ```sudo npm install --unsafe-perm --production ```
7. Run ```cd``` and add /var/vor.env file with content:
  ```
  SOCKET_SERVER=<vor-backend server>
  LISTEN_TYPE=<socket message type to listen>
  LISTEN_ID=<socket message id to listen>
  SEND_TYPE=<the type property of message to be sent>
  SEND_ID=<the id property of message to be sent>
  NODE_PATH=/var/vor/vor-image-client/
  NODE_ENV=production
  ```
  
7. Add /etc/systemd/system/vor.service file with content: 
  ```
  [Unit]
  Description=vor-image-client
  
  [Service]
  ExecStart=/usr/local/bin/node --harmony_destructuring --harmony_modules --harmony_array_includes /var/vor/vor-image-client/app/index.js
  EnvironmentFile=-/var/vor.env
  WorkingDirectory=/var/vor/vor-image-client
  
  [Install]
  WantedBy=multi-user.target
  ```
  
8. Run: ```sudo systemctl start vor.service``` to start service. 
  Notice! when ever you edit vor.service file:
    1. run: ```sudo systemctl daemon-reload``` to ensure updated file is used.
    2. run: ```sudo systemctl restart vor.service``` to restart service.
9. Run: ```sudo systemctl enable vor.service``` to enable service for reboot.
10. Run ```sudo systemctl status vor.service```to view the process output.

EXTRA: [More about system services](https://www.digitalocean.com/community/tutorials/how-to-use-systemctl-to-manage-systemd-services-and-units)
