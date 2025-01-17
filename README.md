# WaterdogPE plugin configuration

![GitHub releases](https://img.shields.io/github/downloads/Presence-Man/WaterdogPE/total?color=violet&label=Downloads&style=flat-square)



## config.yml

> *Environment variables overwrite the config.yml value.*

| Key | Variable name | Type | Description |
|:----:|:-----:|:----:|:----:|
| token                             | `PRESENCE_MAN_TOKEN`                    | String  | The token you obtain from our discord bot. |
| client_id                         | `PRESENCE_MAN_CLIENT_ID`                | String  | Discord application id |

---

## server-presences.json

> We added an [Schema](./server-presences.schema.json) for easier configuration of the presences for each server.

### For what is the `disabled` array?

> The `disabled` array is for defining which servers won't show a presence.

### For what is the `default` object?

> The `default` object is for defining the base presence, that the others in `servers` are extending.

### For what is the `servers` object?

> The `servers` object is used for the downstream servers from WaterdogPE. The name of these servers are listed as
> keys in your WaterdogPE config.yml in a property named `servers`.
>
> The keys from that property are the `server-name` and will be use in our `server-presences.schema.json` file
> under `servers` you can insert a server's presence.

#### For example here are hardcoded presences for following servers:

<details>
  <summary>Presence-Man's <code>server-presences.json</code></summary>

```json
{
	"disabled": [
		"citybuild-?\\d*"
	],
	"default": {
		"state": "Playing {server} on {network}",
		"details": "",
		"large_image_key": "presence-man",
		"large_image_text": "{App.name} - v{App.version}"
	},
	"servers": {
		"hub-?\\d*": {
			"server": "Lobby",
			"state": "Chilling in {server} on {network}",
			"large_image_key": "hub"
		},
		"development-server": {
			"server": "Dev Test Server",
			"large_image_key": "in-dev"
		},
		"bedwars-2x1-?\\d*": {
			"server": "Bedwars 2x1",
			"large_image_key": "bw-2x1"
		}
	}
}
```

</details>


<details>
  <summary>WaterdogPE's <code>config.yml</code></summary>

```yaml
servers:
  citybuild-1:
    address: 0.0.0.0:21001
    server_type: bedrock
  hub-1:
    address: 0.0.0.0:20001
    server_type: bedrock
  hub-2:
    address: 0.0.0.0:20002
    server_type: bedrock
  hub-3:
    address: 0.0.0.0:20003
    server_type: bedrock
  development-server:
    address: 0.0.0.0:20004
    server_type: bedrock
  bedwars-2x1-1:
    address: 0.0.0.0:20005
    server_type: bedrock
  bedwars-2x1-2:
    address: 0.0.0.0:20006
    server_type: bedrock
  bedwars-2x1-3:
    address: 0.0.0.0:20007
    server_type: bedrock
  bedwars-2x1-4:
    address: 0.0.0.0:20007
    server_type: bedrock
```

</details>

> *Now for any server that matches the key will be presence'd with its data from the object!*

---
