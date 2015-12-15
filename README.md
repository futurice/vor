# [Vör](http://vor.space)

To purify your place of work, sprinkle water and say: *"By Vör be cleansed, darkness return, to darkness below"  [*](http://www.northernpaganism.org/shrines/handmaidens/vor/who-is.html)*

[Vör (homepage)](http://vor.space) is open source software and hardware for turning your open office into an open, real-time map for finding people, open work places and current events. Vör is pronounced "FOR" as in "forward".

This project is currently migrating from other repos and undergoing active development. Expect cool, but don't expect stability.

## Summary

Vör is an intranet for IoT. It is a decentralized and vendor-agnostic to find out where people are in your office, find a quiet place to work, find someone to play a game with or check air quality. The reference implementation here is all open source. We are doing things as we think they should be and hope that stimulates you to join the conversation and add your innovative ideas. Lets make the world a better place where services are created and controlled directly by the people using them for the needs they themselves define and want. Making that as easy as possible is our goal.

You can download the Node.js server, Android client, and optionally IoT sensors and actuators for providing additional information about your workspace to your employees and visitors. Parametric models for 3D printing the sensors and actuators are also provided so you can tailor the look of your office and add new "things" relatively easily. Everything is open source with permissive licenses. We try to define services based on the user experience (UX) and map that on to the many capabilities open to taking that beyond the touchscreen in your pocket. Have fun and let's play!

### Technical Summary

You create a vör.space for yourself by placing any beacons and additional Internet of Things (IoT) sensors and actuators around your place of work. Users can then choose to run an Android or iOS application to access this and provide their current indoor location triangulated off of nearby beacons. A webserver running Node.js is setup inside your WLAN for privacy and security- no cloud snooping allowed. Everything is messages. All messages are passed to the Node server which echoes them to everyone else who is currently connected and has send a "subscribe" message. No persistent records are kept for privacy reasons. We want to know what is happening now and in the recent past, but it changes the social nature of the converstaion if someone is snooping that data and keeping it around for other purposes. The Node server keeps an in-memory copy of recent messages for 15minutes (configurable).

All communication is JSON by websockets with end-to-end push. IoT nodes such as Arduino may use HTTP POST if that is easier. There are not multiple URLs like in a REST services you may be more familiar with. Instead there are different messages. You can freely define new messages by changing JSON fields- no server changes are required.

All communication is stateless from the standpoint of the client. The client, usually a mobile phone, sends messsages and forgets about them. The server sends messages which are complete by themselves. This is different from the more standard HTTP request-response.

Please have a look at the subdirectories corresponding to the different parts of this setup for additional details. The /doc director has additional information as it is available.

#### Starting points depending on your interest:

[JSON](/doc/json-schema.md) describing the different message.

[Design](/design/README.md) visual design assets

[Android](/vor-android/README.md) for the Android client to track your location indoors and interact with the map and service cards. A custom build for fixed table display helps office visitor interact with the service.

[iOS](/vor-ios/README.md) for the iPhone client to track your location indoors

[Backend](/vor-backend/README.md) concerning the Node.js server

[Arduino and Raspberry Pi 2](/vor-arduino/README.md) about the custom hardware for IoT sensors and actuators to setup your office

[3D Models](/3d-models/README.md) for the custom Arduio and Raspberry Pi 2 IoT nodes

iOS is definitely planned, starting with a simple app to share current location. Unfortunately it is not in the minimum viable product (MVP) test we have going on at this early stage.

## Contact and Additional Information

Project lead paul.houghton@futurice.com

Director, Wizardry and Mobile Development, [Futurice](http://futurice.com/)

[Blog](http://futurice.com/people/paul-houghton)

[@mobile_rat](https://twitter.com/mobile_rat/)

