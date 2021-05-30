# BloodNight Web Service

# Mob Editor

The mob editor is available on route:

```
/v1/mobEditor
```

## Sessions

In order to start working with the mob editor api you need to open a session. These sessions hold and provide data.

### Open Session

```
POST /v1/mobEditor/submit
```

Open a session.

The payload has to be a `MobEditorPayload`.

The request will return a `access token` and a `201` code if the session was created successfully.

The session will stay open as long as any interaction is made on the session in the last hour. After this the session
data gets deleted.

From now on all request have to be made with the access token in a `token` header.


<details>
<summary>Example</summary>

`POST /v1/mobEditor/submit`

```json
{
  "settingsContainer": {
    "configurations": [
      {
        "identifier": "test_mob",
        "wrapTypes": {},
        "extension": null,
        "stats": {
          "healthModifier": "DEFAULT",
          "health": 2.0,
          "damageModifier": "DEFAULT",
          "damage": 2.0
        },
        "equipment": {
          "mainHand": -1,
          "offHand": -1,
          "helmet": -1,
          "chestplate": -1,
          "leggings": -1,
          "boots": -1
        },
        "drops": {
          "minDrops": -1,
          "maxDrops": -1,
          "overrideDefaultDrops": false,
          "drops": []
        },
        "behaviour": {
          "behaviourMap": {
            "ON_HIT": [],
            "ON_EXPLOSION": [],
            "ON_PROJECTILE_SHOOT": [],
            "ON_DAMAGE": [],
            "ON_PROJECTILE_HIT": [],
            "ON_DEATH": [],
            "ON_END": [],
            "TICK": [
              {
                "clazz": "de.eldoria.bloodnight.bloodmob.nodeimpl.mapper.MoveToLocation",
                "nextNode": {
                  "clazz": "de.eldoria.bloodnight.bloodmob.nodeimpl.filter.PredicateFilter",
                  "nextNode": {
                    "clazz": "de.eldoria.bloodnight.bloodmob.nodeimpl.filter.CooldownFilter",
                    "nextNode": {
                      "clazz": "de.eldoria.bloodnight.bloodmob.nodeimpl.action.OtherPotion",
                      "type": "BLINDNESS",
                      "seconds": 20,
                      "amplifier": 2,
                      "visible": true
                    },
                    "duration": 10
                  },
                  "invert": false,
                  "predicate": {
                    "clazz": "de.eldoria.bloodnight.bloodmob.nodeimpl.predicate.HasTarget"
                  }
                },
                "source": "OLD"
              }
            ],
            "ON_EXPLOSION_PRIME": [],
            "ON_TARGET": [],
            "ON_TELEPORT": [],
            "ON_DAMAGE_BY_ENTITY": [
              {
                "clazz": "de.eldoria.bloodnight.bloodmob.nodeimpl.mapper.MoveToLocation",
                "nextNode": {
                  "clazz": "de.eldoria.bloodnight.bloodmob.nodeimpl.filter.PredicateFilter",
                  "nextNode": {
                    "clazz": "de.eldoria.bloodnight.bloodmob.nodeimpl.filter.CooldownFilter",
                    "nextNode": {
                      "clazz": "de.eldoria.bloodnight.bloodmob.nodeimpl.action.OtherPotion",
                      "type": "BLINDNESS",
                      "seconds": 20,
                      "amplifier": 2,
                      "visible": true
                    },
                    "duration": 10
                  },
                  "invert": false,
                  "predicate": {
                    "clazz": "de.eldoria.bloodnight.bloodmob.nodeimpl.predicate.HasTarget"
                  }
                },
                "source": "OLD"
              }
            ],
            "ON_KILL": [
              {
                "clazz": "de.eldoria.bloodnight.bloodmob.nodeimpl.mapper.MoveToLocation",
                "nextNode": {
                  "clazz": "de.eldoria.bloodnight.bloodmob.nodeimpl.filter.PredicateFilter",
                  "nextNode": {
                    "clazz": "de.eldoria.bloodnight.bloodmob.nodeimpl.filter.CooldownFilter",
                    "nextNode": {
                      "clazz": "de.eldoria.bloodnight.bloodmob.nodeimpl.action.OtherPotion",
                      "type": "BLINDNESS",
                      "seconds": 20,
                      "amplifier": 2,
                      "visible": true
                    },
                    "duration": 10
                  },
                  "invert": false,
                  "predicate": {
                    "clazz": "de.eldoria.bloodnight.bloodmob.nodeimpl.predicate.HasTarget"
                  }
                },
                "source": "OLD"
              }
            ]
          }
        }
      }
    ],
    "items": [
      {
        "id": 0,
        "type": "DIAMOND_BOOTS",
        "enchantment": {
          "unbreaking": 3
        },
        "lore": [],
        "displayName": null
      },
      {
        "id": 1,
        "type": "DIAMOND_CHESTPLATE",
        "enchantment": {},
        "lore": [
          "This is",
          "a nice",
          "Lore"
        ],
        "displayName": null
      }
    ],
    "globalDrops": [
      {
        "itemId": 0,
        "amount": 5,
        "weight": 10
      },
      {
        "itemId": 0,
        "amount": 2,
        "weight": 10
      },
      {
        "itemId": 2,
        "amount": 12,
        "weight": 80
      }
    ]
  }
}
```

</details>

### Close a session

```
POST /v1/mobEditor/close
```

Close the current session.

If the session is not closed yet it will be closed and a `200` code will be returned with a retreival token. This token
can be only retrieved once.

Data of a closed session will be deleted 30 minutes after the last access.

### Retrieve Data

```
GET /v1/mobEditor/retrieve/:token
```

Retrieve the settings from the web server. The `:token` has to be the token returned by the `/v1/mobEditor/close`
endpoint.

## Types

The api uses specific types for fields.

### Typelist

```
GET /v1/mobEditor/types
```

Returns all available types including a class description if available.

If no class description is available the value will be null.


<details>
<summary>Example</summary>

```json
{
  "MULTI_LIST": null,
  "DROPS": {
    "clazz": "de.eldoria.bloodnight.bloodmob.settings.Drops",
    "values": [
      {
        "field": "minDrops",
        "name": "",
        "descr": "",
        "type": "NUMBER",
        "values": {
          "min": -1,
          "max": 64
        }
      },
      {
        "field": "maxDrops",
        "name": "",
        "descr": "",
        "type": "NUMBER",
        "values": {
          "min": -1,
          "max": 64
        }
      },
      {
        "field": "overrideDefaultDrops",
        "name": "",
        "descr": "",
        "type": "BOOLEAN"
      },
      {
        "field": "drops",
        "name": "",
        "descr": "",
        "type": "LIST",
        "values": [
          {
            "field": "itemId",
            "name": "",
            "descr": "",
            "type": "ITEM"
          },
          {
            "field": "amount",
            "name": "",
            "descr": "",
            "type": "NUMBER",
            "values": {
              "min": 0,
              "max": 64
            }
          },
          {
            "field": "weight",
            "name": "",
            "descr": "",
            "type": "NUMBER",
            "values": {
              "min": 0,
              "max": 100
            }
          }
        ]
      }
    ]
  },
  "NUMBER": null,
  "NUMERIC": null,
  "BEHAVIOUR": {
    "clazz": "de.eldoria.bloodnight.bloodmob.settings.Behaviour",
    "values": []
  },
  "STATS": {
    "clazz": "de.eldoria.bloodnight.bloodmob.settings.Stats",
    "values": [
      {
        ...
      }
    ]
  },
  "PREDICATE": {
    "clazz": "de.eldoria.bloodnight.bloodmob.node.predicate.PredicateNode",
    "values": []
  },
  "EXTENSION": {
    "clazz": "de.eldoria.bloodnight.bloodmob.settings.Extension",
    "values": [
      {
        "field": "extensionType",
        "name": "",
        "descr": "",
        "type": "LIST",
        "values": [
          "BAT",
          "BLAZE",
          "CAVE_SPIDER",
          "CREEPER",
          ...
          "WOLF",
          "ZOGLIN",
          "ZOMBIE",
          "ZOMBIE_VILLAGER",
          "ZOMBIFIED_PIGLIN"
        ]
      },
      {
        "field": "extensionRole",
        "name": "",
        "descr": "",
        "type": "LIST",
        "values": [
          "CARRIER",
          "PASSENGER"
        ]
      },
      {
        "field": "equipment",
        "name": "",
        "descr": "",
        "type": "EQUIPMENT"
      },
      {
        "field": "invisible",
        "name": "",
        "descr": "",
        "type": "BOOLEAN"
      },
      {
        "field": "invulnerable",
        "name": "",
        "descr": "",
        "type": "BOOLEAN"
      },
      {
        "field": "clearEquipment",
        "name": "",
        "descr": "",
        "type": "BOOLEAN"
      }
    ]
  },
  "BOOLEAN": null,
  "DROP": {
    "clazz": "de.eldoria.bloodnight.bloodmob.drop.Drop",
    "values": [
      {
        "field": "itemId",
        "name": "",
        "descr": "",
        "type": "ITEM"
      },
      {
        "field": "amount",
        "name": "",
        "descr": "",
        "type": "NUMBER",
        "values": {
          "min": 0,
          "max": 64
        }
      },
      {
        "field": "weight",
        "name": "",
        "descr": "",
        "type": "NUMBER",
        "values": {
          "min": 0,
          "max": 100
        }
      }
    ]
  },
  "ITEM": {
    "clazz": "de.eldoria.bloodnight.bloodmob.registry.items.SimpleItem",
    "values": [
      {
        "field": "id",
        "name": "",
        "descr": "",
        "type": "NUMBER",
        "values": {
          "min": 0,
          "max": 2147483647
        }
      },
      {
        "field": "type",
        "name": "",
        "descr": "",
        "type": "STRING",
        "values": {
          "pattern": "",
          "min": 0,
          "max": 32
        }
      },
      {
        "field": "enchantment",
        "name": "",
        "descr": "",
        "type": "MAP",
        "values": {
          "keys": {
            "type": "STRING"
          },
          "value": {
            "type": "NUMBER"
          }
        }
      },
      {
        "field": "lore",
        "name": "",
        "descr": "",
        "type": "LIST",
        "values": []
      },
      {
        "field": "displayName",
        "name": "",
        "descr": "",
        "type": "STRING",
        "values": {
          "pattern": "",
          "min": 0,
          "max": 32
        }
      }
    ]
  },
  "EQUIPMENT": {
    "clazz": "de.eldoria.bloodnight.bloodmob.settings.Equipment",
    "values": [
      {
        "field": "mainHand",
        "name": "",
        "descr": "",
        "type": "ITEM"
      },
      {
        "field": "offHand",
        "name": "",
        "descr": "",
        "type": "ITEM"
      },
      {
        "field": "helmet",
        "name": "",
        "descr": "",
        "type": "ITEM"
      },
      {
        "field": "chestplate",
        "name": "",
        "descr": "",
        "type": "ITEM"
      },
      {
        "field": "leggings",
        "name": "",
        "descr": "",
        "type": "ITEM"
      },
      {
        "field": "boots",
        "name": "",
        "descr": "",
        "type": "ITEM"
      }
    ]
  },
  "COLOR": null,
  "STRING": null,
  "LIST": null,
  "MAP": null
}
```

</details>

### Type

```
GET /v1/mobEditor/type/:type
```

Returns the class description of the type if available.

If no class description is available the value will be `null`.

```json
{
  <class_description>
}
```

## Moblist

```
GET /v1/mobEditor/moblist
```

Returns a list of identifiers of all available mobs

## MobSettings

Mobs can be created, retreived or deleted by the `mobSettings` endpoint:

```
/v1/mobEditor/mobSettings/:identifier
```

The `:identifier` is the identifier of the mob.

### Retrieve Mob Settings

```
GET /v1/mobEditor/mobSettings/:identifier
```

Retrieve the mob settings. Probably not needed. Use the more specific endpoint if possible.

### Create Mob Setting

```
POST /v1/mobEditor/mobSettings/:identifier
```

Create a new mob with a identifier.

Returns `201` if the mob was created.

### Delete Mob Setting

```
DELETE /v1/mobEditor/mobSettings/:identifier
```

Deletes the mob with a identifier.

Returns `202` if the mob was deleted.

## Change Settings

All changes for a specific mob start at this endpoint

```
/v1/mobEditor/mobSettings/:identifier
```

### Define mob wrapper types

Wrap types define which mobs types can be wrapped to this mob.

#### Get available types

```
GET /v1/mobEditor/mobSettings/:identifier/wraptypes/available
```

Returns all available wrap types, which are not already present as wrap type.

These types contain a class description for the wrapper type.

```json
{
  "type": {
    <description>
  }
}
```

#### Get set types

```
GET /v1/mobEditor/mobSettings/:identifier/wraptypes/available
```

Returns all wrap types which are set alreay. They will be returned as a `DataDescriptionContainer`

```json
{
  "type": {
    "data": {
      ...
    },
    "definition": {
      ...
    }
  }
}
```

#### Delete wrapp type

```
DELETE /v1/mobEditor/mobSettings/:identifier/wraptypes/:type
```

Delete the wrapper type. Returns `202` if the wrapper type was deleted.

#### Add wrap type

```
PUT /v1/mobEditor/mobSettings/:identifier/wraptypes/:type
```

Add a new wrap type. Can override currently existing types.

The body should contain an object like defined from the endpoints in the two sections above.

`PUT /v1/mobeditor/mobsetting/test_mob/wraptypes/SLIME`

```json
{
  "size": 1,
  "name": "Slime"
}
```

### Settings

Settings are provided via keys:

```
extension
extension.equipment
equipment
drops
behaviour
```

These keys represent the names of the fields returned by the settings endpoint above.

Changes on the behaviour system have an own endpoint.

#### Get the current settings

```
GET /v1/mobEditor/mobSettings/:identifier/settings/:setting
```

Get the current setting of a setting and the description of the setting.


<details>
<summary>Example</summary>

`GET /v1/mobeditor/mobsetting/test_mob/equipment`

```json
{
  "data": {
    "mainHand": -1,
    "offHand": -1,
    "helmet": -1,
    "chestplate": -1,
    "leggings": -1,
    "boots": -1
  },
  "definition": {
    "clazz": "de.eldoria.bloodnight.bloodmob.settings.Equipment",
    "values": [
      {
        "field": "mainHand",
        "name": "",
        "descr": "",
        "type": "ITEM"
      },
      {
        "field": "offHand",
        "name": "",
        "descr": "",
        "type": "ITEM"
      },
      {
        "field": "helmet",
        "name": "",
        "descr": "",
        "type": "ITEM"
      },
      {
        "field": "chestplate",
        "name": "",
        "descr": "",
        "type": "ITEM"
      },
      {
        "field": "leggings",
        "name": "",
        "descr": "",
        "type": "ITEM"
      },
      {
        "field": "boots",
        "name": "",
        "descr": "",
        "type": "ITEM"
      }
    ]
  }
}

```

</details>

#### Set the settings

```
PUT /v1/mobEditor/mobSettings/:identifier/settings/:setting
```

Set the current setting of a setting.

The setting should be of the format like descriped by the data description provided by the endpoint above


<details>
<summary>Example</summary>

`http://localhost:8888/v1/mobeditor/mobsetting/test_mob/equipment`

```json
{
  "mainHand": 1,
  "offHand": 2,
  "helmet": 3,
  "chestplate": 4,
  "leggings": 5,
  "boots": 6
}
```

</details>

## Behaviour

The behaviour system is defined at the endpoint:

```
/v1/mobEditor/mobSettings/:identifier/behaviour/node/:type
```

Nodes are sorted by types. Each type has a list of node chains. A node chain can be accessed by its index.

### Get next Nodes

```
GET /v1/mobEditor/mobSettings/:identifier/behaviour/node/:type/nextNodes
```

Returns possible nodes for this specific type as class description

```
GET /v1/mobEditor/mobSettings/:identifier/behaviour/node/:type/:id/nextNodes
```

Returns possible nodes for the node chain with `id` of `type` as class description

### Add node

```
PUT /v1/mobEditor/mobSettings/:identifier/behaviour/node/:type/:id
```

Add a new node to the node chain


<details>
<summary>Example</summary>

`PUT /v1/mobeditor/mobsetting/test_mob/behaviour/node/TICK/1`

```json
{
  "clazz": "de.eldoria.bloodnight.bloodmob.nodeimpl.action.LaunchProjectileOnTarget",
  "projectileType": "LARGE_FIREBALL",
  "speed": 5
}
```

</details>

Returns 201 when the node was created.

Returns 304 if the chain is already closed.

### Create node

```
PUT /v1/mobEditor/mobSettings/:identifier/behaviour/node/:type
```

Create a new node chain with a type.


<details>
<summary>Example</summary>

`PUT /v1/mobeditor/mobsetting/test_mob/behaviour/node/TICK`

```json
{
  "clazz": "de.eldoria.bloodnight.bloodmob.nodeimpl.filter.CooldownFilter",
  "duration": 10
}
```

</details>

Returns 201 and the id of the created chain

### Remove last node

```
DELETE /v1/mobEditor/mobSettings/:identifier/behaviour/node/:type/:id/last
```

Removes the last node in a chain. The last node cant be removed. Delete the chain instead

### Delete node chain

```
DELETE /v1/mobEditor/mobSettings/:identifier/behaviour/node/:type/:id
```

Delete the node chain.

### Get types

```
GET /v1/mobeditor/mobsetting/:identifier/behaviour/node/types
```

Get available event types

### Get Chain

```
GET /v1/mobeditor/mobsetting/:identifier/behaviour/node/:type/:id
```

Get the current chain with description

### Get Nodes

```
GET /v1/mobeditor/mobsetting/:identifier/behaviour/node/nodes
```

Get the node types with nodes

### Get Nodes

```
GET /v1/mobeditor/mobsetting/:identifier/behaviour/node/nodes
```

Get Nodes of a specific type.

# Global Drops

## Get Drops
```
GET /v1/mobeditor/globaldrops
```

Get a list of global drops

## Delete Drop
```
DELETE /v1/mobeditor/globaldrops
```

Delete a drop by sending the drop to delete in the body.

Returns 202 if the drop was removed or 304 if the drop does not exists.

# Class definitions

Class definitions are used very often. They describe the class and the fields in it:

```json
{
  "type": "fully qualified name of clazz",
  "name": "Human readable translated name of clazz",
  "description": "description of clazz",
  "values": [
    {
      "field": "name of field",
      "name": "human readable name of field",
      "description": "Description of field",
      "type": "type of the field",
      "values": {
        "Depends on the type. May be a integer range, a list of valid values or other stuff. Can be empty as well"
      }
    }
  ]
}
```