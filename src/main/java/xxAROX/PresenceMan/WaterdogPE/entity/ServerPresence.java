/*
 * Copyright (c) 2024. By Jan-Michael Sohn also known as @xxAROX.
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package xxAROX.PresenceMan.WaterdogPE.entity;


import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.waterdog.waterdogpe.network.serverinfo.ServerInfo;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

@NoArgsConstructor
@Getter @Setter @ToString
public final class ServerPresence {
    @Getter private static ServerPresence default_presence = new ServerPresence();
    private String display_name = null;
    private Pattern pattern = null;
    private boolean enabled = true;
    private String state = null;
    private String details = null;
    private String large_image_key = null;
    private String large_image_text = null;

    private ServerPresence(String regex, JsonObject json) {
        pattern = Pattern.compile(regex);
        display_name = !json.has("server") ? null : json.get("server").getAsString();
        enabled = !json.has("enabled") || json.get("enabled").getAsBoolean();
        state = !json.has("state") ? default_presence.state : json.get("state").getAsString();
        details = !json.has("details") ? default_presence.details : json.get("details").getAsString();
        large_image_key = !json.has("large_image_key") ? default_presence.large_image_key : json.get("large_image_key").getAsString();
        large_image_text = !json.has("large_image_text") ? default_presence.large_image_text : json.get("large_image_text").getAsString();
    }

    public boolean matchServerInfo(ServerInfo serverInfo) {
        return pattern.matcher(serverInfo.getServerName().toLowerCase(Locale.ROOT)).matches();
    }

    public void setPattern(String regex) {
        pattern = Pattern.compile(regex);
    }

    public static List<ServerPresence> load(JsonObject jsonRoot) {
        List<ServerPresence> list = new ArrayList<>();
        default_presence = new ServerPresence("default", jsonRoot.get("default").getAsJsonObject());
        for (Map.Entry<String, JsonElement> entry : jsonRoot.get("servers").getAsJsonObject().entrySet()) list.add(new ServerPresence(entry.getKey(), entry.getValue().getAsJsonObject()));
        return list;
    }
}