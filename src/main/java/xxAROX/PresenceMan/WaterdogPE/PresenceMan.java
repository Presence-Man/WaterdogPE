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

package xxAROX.PresenceMan.WaterdogPE;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import dev.waterdog.waterdogpe.ProxyServer;
import dev.waterdog.waterdogpe.event.defaults.InitialServerConnectedEvent;
import dev.waterdog.waterdogpe.player.ProxiedPlayer;
import dev.waterdog.waterdogpe.plugin.Plugin;
import dev.waterdog.waterdogpe.utils.config.Configuration;
import lombok.NonNull;
import org.cloudburstmc.protocol.bedrock.data.skin.SerializedSkin;
import xxAROX.PresenceMan.WaterdogPE.entity.ActivityType;
import xxAROX.PresenceMan.WaterdogPE.entity.ApiActivity;
import xxAROX.PresenceMan.WaterdogPE.entity.ApiRequest;
import xxAROX.PresenceMan.WaterdogPE.entity.Gateway;
import xxAROX.PresenceMan.WaterdogPE.tasks.async.BackendRequest;
import xxAROX.PresenceMan.WaterdogPE.tasks.async.FetchGatewayInformationTask;
import xxAROX.PresenceMan.WaterdogPE.tasks.async.PerformUpdateTask;
import xxAROX.PresenceMan.WaterdogPE.utils.SkinUtils;
import xxAROX.PresenceMan.WaterdogPE.utils.Utils;
import xxAROX.WebRequester.WebRequester;

import java.util.HashMap;
import java.util.Map;

public final class PresenceMan extends Plugin {
    public static final Gson GSON = new Gson();
    private static PresenceMan instance;
    public static PresenceMan getInstance() {
        return instance;
    }

    private static String token = "undefined";
    public static String client_id = null;
    public static String server = "undefined";
    public static Boolean enable_default = false;
    public static Boolean update_skin = false;

    public static Map<String, ApiActivity> presences = new HashMap<>();
    public static ApiActivity default_activity;

    @Override
    public void onStartup() {
        WebRequester.init(GSON, getProxy().getWorkerEventLoopGroup());
        instance = this;
        saveResource("README.md");
        saveResource("config.yml");

        Configuration config = this.getConfig();
        token = (String) Utils.getconfigvalue(config, "token");
        client_id = (String) Utils.getconfigvalue(config, "client_id", "", client_id);
        server = (String) Utils.getconfigvalue(config, "server", "", server);
        update_skin = (Boolean) Utils.getconfigvalue(config, "update_skin", "", update_skin);

        enable_default = (Boolean) Utils.getconfigvalue(config, "default_presence.enabled", "DEFAULT_ENABLED", enable_default);
        String DEFAULT_STATE = (String) Utils.getconfigvalue(config, "default_presence.state", "DEFAULT_STATE", "Playing {server} on {network}");
        String DEFAULT_DETAILS = (String) Utils.getconfigvalue(config, "default_presence.details", "DEFAULT_DETAILS", "");
        String DEFAULT_LARGE_IMAGE_KEY = (String) Utils.getconfigvalue(config, "default_presence.large_image_key", "DEFAULT_LARGE_IMAGE_KEY", "");
        String DEFAULT_LARGE_IMAGE_TEXT = (String) Utils.getconfigvalue(config, "default_presence.large_image_text", "DEFAULT_LARGE_IMAGE_TEXT", "{App.name} - v{App.version}");

        default_activity = new ApiActivity(
                ActivityType.PLAYING,
                DEFAULT_STATE,
                DEFAULT_DETAILS,
                null,
                DEFAULT_LARGE_IMAGE_KEY,
                DEFAULT_LARGE_IMAGE_TEXT
        );
    }
    public static boolean running = false;
    @Override public void onEnable() {
        getProxy().getEventManager().subscribe(InitialServerConnectedEvent.class, EventListener::InitialServerConnectedEvent);
        getProxy().getScheduler().scheduleRepeating(() -> {
            if (running) return;
            running = true;
            new PerformUpdateTask();
        }, 20 *60 *60); // NOTE: 60 minutes
        FetchGatewayInformationTask.unga_bunga();
    }
    @Override public void onDisable() {
        for (ProxiedPlayer player : ProxyServer.getInstance().getPlayers().values()) offline(player);
    }

    private static void runTask(BackendRequest task){
        if (!ProxyServer.getInstance().isRunning()) task.unga_bunga(false);
        else task.unga_bunga(true);
    }

    public static String getHeadURL(String xuid, boolean gray, Integer size) {
        size = size != null ? Math.min(512, Math.max(16, size)) : null;
        String url = ApiRequest.URI_GET_HEAD + xuid;
        if (size != null) url += "?size=" + size;
        if (gray) url += size != null ? "&gray" : "?gray";
        return Gateway.getUrl() + url;
    }
    public static String getHeadURL(String xuid, boolean gray){
        return getHeadURL(xuid, gray, null);
    }
    public static String getHeadURL(String xuid, Integer size){
        return getHeadURL(xuid, false, size);
    }
    public static String getHeadURL(String xuid){
        return getHeadURL(xuid, false, null);
    }

    public static String getSkinURL(String xuid){
        return Gateway.getUrl() + ApiRequest.URI_GET_SKIN + xuid;
    }

    public static void setActivity(@NonNull ProxiedPlayer player, ApiActivity activity) {
        if (Utils.isFromSameHost(player.getAddress().getAddress())) return;
        if (!ProxyServer.getInstance().isRunning()) return;
        if (!player.isConnected()) return;
        if (player.getXuid().isEmpty()) return;

        JsonObject body = new JsonObject();
        new HashMap<String, String>(){{
            put("ip", player.getAddress().getHostName());
            put("xuid", player.getXuid());
            put("server", PresenceMan.server);
        }}.forEach(body::addProperty);
        if (activity != null) activity.setClient_id(Long.getLong(client_id));
        if (activity == null) body.addProperty("api_activity", (String)null);
        else body.add("api_activity", activity.serialize());

        ApiRequest request = new ApiRequest(ApiRequest.URI_UPDATE_PRESENCE, body, true);
        request.header("Token", token);

        PresenceMan.runTask(new BackendRequest(
                request.serialize(),
                response -> {
                    if (response.has("status") && response.get("status").getAsInt() == 200) PresenceMan.presences.put(player.getXuid(), activity);
                    else PresenceMan.getInstance().getLogger().error("Failed to update presence for " + player.getName() + ": " + response.get("message").getAsString());
                },
                error -> {}
        ));
    }





    /**
     * @hidden
     */
    public static void offline(ProxiedPlayer player) {
        if (!ProxyServer.getInstance().isRunning()) return;
        if (!player.isConnected()) return;
        if (player.getXuid().isEmpty()) return;
        JsonObject body = new JsonObject();
        new HashMap<String, String>(){{
            put("ip", player.getAddress().getHostName());
            put("xuid", player.getXuid());
        }}.forEach(body::addProperty);

        ApiRequest request = new ApiRequest(ApiRequest.URI_UPDATE_OFFLINE, body, true);
        request.header("Token", token);
        runTask(new BackendRequest(
                request.serialize(),
                response -> PresenceMan.presences.remove(player.getXuid()),
                error -> {}
        ));
    }
    /**
     * @hidden
     */
    public static void save_skin(ProxiedPlayer player, SerializedSkin skin) {
        if (!ProxyServer.getInstance().isRunning()) return;
        if (!player.isConnected()) return;
        if (player.getXuid().isEmpty()) return;
        String content = SkinUtils.convertSkinToBased64File(skin);

        if (content != null) {
            JsonObject body = new JsonObject();
            new HashMap<String, String>(){{
                put("ip", player.getAddress().getHostName());
                put("xuid", player.getXuid());
                put("skin", content);
            }}.forEach(body::addProperty);
            ApiRequest request = new ApiRequest(ApiRequest.URI_UPDATE_SKIN, body, true);
            request.header("Token", token);
            runTask(new BackendRequest(request.serialize(), response -> {}, error -> {}));
        }
    }
}
