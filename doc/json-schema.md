# JSON messaging schemas and examples

- [Room beacon message](#room-beacon-message) - [example message](#room-beacon-example)
- [Location message](#location-message) - [example message](#location-example)
- [Room message](#room-message) - [example message](#room-example)
- [Toilet message](#toilet-message) - [example message](#toilet-example)
- [Button message](#button-message) - [example message](#button-example)
- [Pool message](#pool-message) - [example message](#pool-example)
- [Cake message](#cake-message) - [example message](#cake-example)

<h2 name="room-beacon-message">Room beacon message</h2>

####Schema:

```
{
    "$schema": "http://json-schema.org/draft-04/schema#",  
    "title": "Room beacon message",  
    "description": "Client pusblishes room beacon data",  
    "type": "object",  
    "properties": { 
        "email": {  
            "description": "A label for client", 
            "type": "string"  
        },
        "id": {  
            "description": "An unique identifier for a beacon",  
            "type": "string"  
        },
        "type": {  
            "description": "The type of the message",  
            "constant": "beacon"  
        },
        "floor": {  
            "description": "The floor where beacon locates",  
            "type": "integer"  
        },
        "distance": {  
            "description": "The distance of the client from beacon",  
            "type": "float"  
        },
        "temperature": {
            "description": "The temperature measured by beacon",
            "type": "float"  
        }
    },  
    "required": ["email", "id", "type", "floor", "distance", "temperature"]
```

<h4 name="room-beacon-example">Example:</h4>

```
{
    "email": "user@mail.com",
    "id": "futu-b1-32095-19454",
    "type": "beacon",
    "floor": 7,
    "distance": 8.132,
    "temperature": 19.4
}
```


<h2 id="location-message">Location message</h2>

####Schema:
```
{
    "$schema": "http://json-schema.org/draft-04/schema#",  
    "title": "Location message",  
    "description": "Server publishes client's indoor location",  
    "type": "object",  
    "properties": {  
        "email": {  
            "description": "A label for client", 
            "type": "string"  
        },
        "type": {  
            "description": "The type of the message",  
            "constant": "location"  
        },
        "x": {  
            "description": "The calculated x-axis position of the client",  
            "type": "float"  
        },
        "y": {  
            "description": "The calculated y-axis position of the client",  
            "type": "float"  
        }
    },  
    "required": ["email", "type", "x", "y"]
}
```

<h4 name="location-example">Example:</h4>

```
{
    "email": "user@mail.com",
    "type": "location",
    "x": 2.453, 
    "y": 1.224
}
```


<h2 id="room-message">Room message</h2>

####Schema:
```
{
    "$schema": "http://json-schema.org/draft-04/schema#",  
    "title": "Room message",  
    "description": "Information of room's conditions",  
    "type": "object",  
    "properties": {  
         "id": {  
            "description": "An unique identifier for a room",  
            "type": "string"  
        },
        "type": {  
            "description": "The type of the message",  
            "type": "room"  
        },
        "reserved": {  
            "description": "Current reservation status of the room",  
            "type": "boolean"  
        },
        "temperature": {  
            "description": "Room temperature(C°)",  
            "type": "float"  
        },
        "light": {  
            "description": "Amount of light(lumen) in the room",  
            "type": "integer"  
        },
        "dioxide": {  
            "description": "Amount of dioxide((percentage of CO2²) in the room ",  
            "type": "float"  
        },
        "noise": {  
            "description": "Amount of noise(dB) in the room",  
            "type": "float"  
        }
    },  
    "required": ["id", "type", "reserved", "temperature", "light", "dioxide", "noise"]
}
```

<h4 name="room-example">Example:</h4>

```
{
    "id": "room-1a",
    "type": "room"
    "reserved": true, 
    "temperature": 20.1,
    "light": 1002,
    "dioxide": 20.2,
    "noise":45.2
}
```


<h2 id="toilet-message">Toilet message</h2>

####Schema:

```
{
    "$schema": "http://json-schema.org/draft-04/schema#",  
    "title": "Toilet message",  
    "description": "Information of toilet's conditions",  
    "type": "object",  
    "properties": {  
         "id": {  
            "description": "An unique identifier for a toilet",  
            "type": "string"  
        },
        "type": {  
            "description": "The type of the message",  
            "constant": "toilet"  
        },
        "reserved": {  
            "description": "Current reservation status of the toilet",  
            "type": "boolean"  
        },
        "methane": {  
            "description": "Amount of methane(%) in the room",  
            "type": "float"  
        }
    },  
    "required": ["id", "type", "reserved", "temperature", "light", "dioxide", "noise"]
}
```

<h4 name="toilet-example">Example:</h4>

```
{
    "id": "toilet-1a",
    "type": "toilet",
    "reserved": true, 
    "methane": 0.2
}
```


<h2 id="button-message">Button message</h2>

####Schema:

```
{
    "$schema": "http://json-schema.org/draft-04/schema#",  
    "title": "Button message",  
    "description": "Button publishes on button up event",  
    "type": "object",  
    "properties": {  
         "id": {  
            "description": "An unique identifier for a button",  
            "type": "string"  
        },
        "type": {  
            "description": "The type of the message",  
            "constant": "button"  
        }
    },  
    "required": ["id", "type"]
}
```

<h4 name="button-example">Example:</h4>

```
{
    "id": "button-1",
    "type": "button"
}
```


<h2 id="pool-message">Pool message</h2>

####Schema:

```
{
    "$schema": "http://json-schema.org/draft-04/schema#",  
    "title": "Pool message",  
    "description": "Information of toilet's conditions",  
    "type": "object",  
    "properties": {  
         "id": {  
            "description": "An unique identifier for a pool",  
            "type": "string"  
        },
        "type": {  
            "description": "The type of the message",  
            "constant": "pool"  
        },
        "image": {  
            "description": "Image of the cake as base64",  
            "type": "string"  
        }
    },  
    "required": ["id", "type", "image"]
}
```

<h4 name="pool-example">Example:</h4>

```
{
    "id": "pool",
    "type": "pool",
    "image": "/9j/4AAQSkZJRgABAQEASABIAAD/4QCAR.."
}
```

<h2 id="cake-message">Cake message</h2>

####Schema:

```
{
    "$schema": "http://json-schema.org/draft-04/schema#",  
    "title": "Cake message",  
    "description": "Information of toilet's conditions",  
    "type": "object",  
    "properties": {  
         "id": {  
            "description": "An unique identifier for a toilet",  
            "type": "string"  
        },
        "type": {  
            "description": "The type of the message",  
            "constant": "cake"  
        },
        "image": {  
            "description": "Image of the cake as base64",  
            "type": "string"  
        }
    },  
    "required": ["id", "type", "image"]
}
```

<h4 name="cake-example">Example:</h4>

```
{
    "id": "cake",
    "type": "cake",
    "image": "/9j/4AAQSkZJRgABAQEASABIAAD/4QCAR.."
}
```

