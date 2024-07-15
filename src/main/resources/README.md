# WaterdogPE plugin configuration



## Developer API

<details id="set-custom-presence">
<summary>Set custom presence:</summary>

```java
import dev.waterdog.waterdogpe.player.ProxiedPlayer;
import xxAROX.PresenceMan.WaterdogPE.entity.ApiActivity;
import xxAROX.PresenceMan.WaterdogPE.PresenceMan;

final class Documentation {
    public void some_method_name(ProxiedPlayer player) {
        ApiActivity normal = ApiActivity.defaults.activity();
        normal.setState("Playing on {network}");
        normal.setDetails("A secret game");
        PresenceMan.setActivity(player, normal);

        // Set a presence that displays: Ends in 15 minutes
        ApiActivity ends_in_15min = ApiActivity.defaults.ends_in(System.currentTimeMillis() + (1000 * 60 * 15));
        PresenceMan.setActivity(player, ends_in_15min);
        // After these 15 minutes YOU have to send a new presence to that user.

        // Set a presence with player count
        ApiActivity players_left_and_ends_in_15min = ApiActivity.defaults.players_left(8, 12, ends_in_15min);
        PresenceMan.setActivity(player, players_left_and_ends_in_15min);
        // After these 15 minutes YOU have to send a new presence to that user.
    }
}
```

</details>

<details id="set-custom-presence-for-half-dynamic servers">
<summary>Set custom presence for half-dynamic servers:</summary>

```java
import dev.waterdog.waterdogpe.ProxyServer;
import dev.waterdog.waterdogpe.network.serverinfo.BedrockServerInfo;
import dev.waterdog.waterdogpe.player.ProxiedPlayer;
import jline.internal.Nullable;
import xxAROX.PresenceMan.WaterdogPE.entity.ApiActivity;
import xxAROX.PresenceMan.WaterdogPE.PresenceMan;
import xxAROX.PresenceMan.WaterdogPE.entity.ServerPresence;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Locale;

final class Documentation {
    public void registerServer(String name, InetSocketAddress address) {
        registerServer(name, address, null);
    }

    public static void registerServer(String name, InetSocketAddress address, @Nullable InetSocketAddress public_address) {
        BedrockServerInfo info = new BedrockServerInfo(name, address, public_address);
        ServerPresence presence = new ServerPresence();
        presence.setPattern(info.getServerName());
        presence.setLarge_image_key(name.split("-")[0]);
        presence.setLarge_image_text("{server} on {network}");
        // Set state if needed
        // Set details if needed

        PresenceMan.server_presences.add(presence);
        ProxyServer.getInstance().registerServerInfo(info);
        // When the player joins this server, his presence will automatically get updated
    }
}
```

</details>

<details id="get-head-url">
<summary>Get head url: (only works for Presence-Man players)</summary>

```java
import dev.waterdog.waterdogpe.player.ProxiedPlayer;
import xxAROX.PresenceMan.WaterdogPE.PresenceMan;

final class Documentation {
    public void some_method_name(ProxiedPlayer player) {
        String skin = PresenceMan.getSkinURL(player.getXuid());             // -> normal player skin url
        String img_1 = PresenceMan.getHeadURL(player.getXuid());            // -> normal player head url
        String img_2 = PresenceMan.getHeadURL(player.getXuid(), true);      // -> gray player head url
        String img_3 = PresenceMan.getHeadURL(player.getXuid(), 256);       // -> normal player head url in scale of 256x256
        String img_4 = PresenceMan.getHeadURL(player.getXuid(), true, 256); // -> gray player head url in scale of 256x256
    }
}
```

</details>

---

## config.yml

> Environment variables overwrite the config.yml value.

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
