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

```json
{
  "typename": {
    <class_description>
  }
}
```

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
GET /v1/mobEditor/mobSettings/:identifier/wraptypes/:type
```

Get the current setting of a setting and the description of the setting.

#### Set the settings

```
PUT /v1/mobEditor/mobSettings/:identifier/wraptypes/:type
```

Set the current setting of a setting.

The setting should be of the format like descriped by the data description provided by the endpoint above

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

### Create node
```
PUT /v1/mobEditor/mobSettings/:identifier/behaviour/node/:type
```
Create a new node chain with a type.

### Remove last node
```
PUT /v1/mobEditor/mobSettings/:identifier/behaviour/node/:type/:id/removeLast
```
Removes the last node in a chain.
The last node cant be removed. Delete the chain instead

### Delete node chain
```
DELETE /v1/mobEditor/mobSettings/:identifier/behaviour/node/:type/:id
```
Delete the node chain.

### 

