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
import dev.waterdog.waterdogpe.ProxyServer;
import dev.waterdog.waterdogpe.scheduler.CallbackTask;
import xxAROX.PresenceMan.WaterdogPE.PresenceMan;
import xxAROX.PresenceMan.WaterdogPE.entity.ApiRequest;
import xxAROX.PresenceMan.WaterdogPE.entity.Gateway;
import xxAROX.WebRequester.WebRequester;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

public final class BackendRequest {
    private final String request;
    private final Consumer<JsonObject> onResponse;
    private final Consumer<JsonObject> onError;
    private final String url;

    public BackendRequest(String request, Consumer<JsonObject> onResponse, Consumer<JsonObject> onError) {
        this.url = Gateway.getUrl();
        this.request = request;
        this.onResponse = onResponse;
        this.onError = onError;
    }

    public void unga_bunga(boolean async) {
        if (async) ProxyServer.getInstance().getScheduler().scheduleAsync(new CallbackTask<>(this::onRun, this::onCompletion));
        else onCompletion(onRun());
    }

    private String onRun() {
        ApiRequest apiRequest = ApiRequest.deserialize(request);
        Map<String, String> headers = apiRequest.getHeaders();

        CompletableFuture<WebRequester.Result> future = null;
        if (apiRequest.isPostMethod()) future = WebRequester.postAsync(url + apiRequest.getUri(), headers, (HashMap<String, String>) PresenceMan.GSON.fromJson(apiRequest.getBody(), HashMap.class));
        else future = WebRequester.getAsync(url + apiRequest.getUri(), headers);


        try {
            WebRequester.Result response = future.get();
            JsonObject json = new JsonObject();
            json.addProperty("body", response.getBody());
            json.addProperty("status", response.getStatus());
            return json.toString();
        } catch (InterruptedException | ExecutionException e) {
            PresenceMan.getInstance().getLogger().error("" + e);
            return null;
        }
    }

    private void onCompletion(String str) {
        ApiRequest request = ApiRequest.deserialize(this.request);
        JsonObject results = PresenceMan.GSON.fromJson(str, JsonObject.class);
        System.out.println(results);

        if (results != null) {
            int code = results.get("status").getAsInt();
            JsonObject body = PresenceMan.GSON.fromJson(results.get("body").getAsString(), JsonObject.class);

            if (code >= 100 && code <= 399) { // Good
                if (onResponse != null) onResponse.accept(body);
            } else if (code >= 400 && code <= 499) { // Client-Errors
                PresenceMan.getInstance().getLogger().error("[CLIENT-ERROR] [" + request.getUri() + "]: " + body.toString());
                if (onError != null) onError.accept(body);
            } else if (code >= 500 && code <= 599) { // Server-Errors
                if (!body.toString().contains("<html>")) PresenceMan.getInstance().getLogger().error("[API-ERROR] [" + request.getUri() + "]: " + body);
                if (onError != null) onError.accept(body);
            }
        }
    }
}
