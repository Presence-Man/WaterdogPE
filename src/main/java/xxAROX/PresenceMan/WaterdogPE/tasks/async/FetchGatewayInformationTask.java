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

package xxAROX.PresenceMan.WaterdogPE.tasks.async;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import dev.waterdog.waterdogpe.ProxyServer;
import dev.waterdog.waterdogpe.scheduler.Task;
import dev.waterdog.waterdogpe.utils.exceptions.PluginChangeStateException;
import xxAROX.PresenceMan.WaterdogPE.PresenceMan;
import xxAROX.PresenceMan.WaterdogPE.entity.Gateway;
import xxAROX.PresenceMan.WaterdogPE.tasks.ReconnectingTask;
import xxAROX.WebRequester.WebRequester;

import java.util.HashMap;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public final class FetchGatewayInformationTask extends Task {
    public static final String URL = "https://raw.githubusercontent.com/Presence-Man/Gateway/main/gateway.json";

    @Override
    public void onRun(int tick) {
        HashMap<String, String> headers = new HashMap<>();
        headers.put("Cache-Control", "no-cache, no-store");

        CompletableFuture<WebRequester.Result> future = WebRequester.getAsync(URL, headers);
        future.thenAccept(response -> {
            try {
                if (response == null || response.getStatus() != 200) PresenceMan.getInstance().getLogger().warn("Couldn't fetch gateway data");
                else {
                    JsonObject json = PresenceMan.GSON.fromJson(response.getBody(), JsonObject.class);
                    if (json != null) {
                        Gateway.protocol = json.get("protocol").getAsString();
                        Gateway.address = json.get("address").getAsString();
                        Gateway.port = json.has("port") && !json.get("port").isJsonNull() ? json.get("port").getAsInt() : null;
                        ping_backend(success -> {
                            if (!success) PresenceMan.getInstance().getLogger().error("Error while connecting to backend-server!");
                        });
                    }
                }
            } catch (JsonParseException e) {
                PresenceMan.getInstance().getLogger().error("Error while fetching gateway information: " + e.getMessage());
                PresenceMan.getInstance().getLogger().warn("Presence-Man backend-gateway config is not reachable, disabling..");
                try {
                    PresenceMan.getInstance().setEnabled(false);
                } catch (PluginChangeStateException ignore) {
                }
            }
        });
    }

    @Override
    public void onCancel() {
    }

    public static void ping_backend(Consumer<Boolean> callback) {
        if (ReconnectingTask.active) return;
        CompletableFuture<WebRequester.Result> future = WebRequester.getAsync(Gateway.getUrl());
        future.thenAccept(response -> {
            var code = response.getStatus();
            if (code != 200) {
                Gateway.broken = true;
                ReconnectingTask.activate();
            } else {
                ReconnectingTask.deactivate();
                PresenceMan.getInstance().getLogger().info("Presence-Man is active!");
            }
            callback.accept(code == 200);
        });
    }

    public static void unga_bunga() {
        ProxyServer.getInstance().getScheduler().scheduleAsync(new FetchGatewayInformationTask());
    }
}
