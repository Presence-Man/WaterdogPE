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

package xxAROX.PresenceMan.WaterdogPE.utils;

import dev.waterdog.waterdogpe.ProxyServer;
import dev.waterdog.waterdogpe.utils.config.Configuration;
import lombok.AllArgsConstructor;
import xxAROX.PresenceMan.WaterdogPE.PresenceMan;
import xxAROX.WebRequester.WebRequester;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public final class Utils {
    public static Object getconfigvalue(Configuration config, String key, String env, Object defaultValue){
        if (env.isEmpty()) env = key.toUpperCase(Locale.ROOT);
        if (!env.startsWith("PRESENCE_MAN_")) env = "PRESENCE_MAN_" + env;
        String val = System.getenv(env);
        if (val == null || val.isEmpty()) return config.get(key, defaultValue);
        else return val;
    }
    public static Object getconfigvalue(Configuration config, String key, String env){
        return getconfigvalue(config, key, env, null);
    }
    public static Object getconfigvalue(Configuration config, String key){
        return getconfigvalue(config, key, "", null);
    }
    public static boolean isFromSameHost(InetAddress address) {
        return address.isSiteLocalAddress() || address.isLoopbackAddress() || address.isAnyLocalAddress();
    }
    public static boolean isFromSameHost(String ip) {
        try {
            InetAddress address = InetAddress.getByName(InetAddress.getByName(ip).getHostAddress());
            return address.isSiteLocalAddress() || address.isLoopbackAddress() || address.isAnyLocalAddress();
        } catch (UnknownHostException e) {
            return false;
        }
    }

    @AllArgsConstructor
    public static class PlayerDataRow {
        protected String xuid;
        protected String ip;
    }

    public static class VersionComparison {
        public static Version parse(String versionString) {
            String[] parts = versionString.split("\\.");
            int[] versionNumbers = new int[parts.length];
            for (int i = 0; i < parts.length; i++) {
                versionNumbers[i] = Integer.parseInt(parts[i]);
            }
            return new Version(versionNumbers);
        }
        public static class Version implements Comparable<Version> {
            private final int[] versionNumbers;
            public Version(int[] versionNumbers) {
                this.versionNumbers = versionNumbers;
            }
            @Override
            public int compareTo(Version other) {
                for (int i = 0; i < Math.min(versionNumbers.length, other.versionNumbers.length); i++) {
                    int result = Integer.compare(versionNumbers[i], other.versionNumbers[i]);
                    if (result != 0) return result;
                }
                return Integer.compare(versionNumbers.length, other.versionNumbers.length);
            }
        }
    }


    public static class WebUtils {
        public static CompletableFuture<WebRequester.Result> get(String url) {return get(url, new HashMap<>());}
        public static CompletableFuture<WebRequester.Result> get(String url, Map<String, String> headers) {
            WebRequester.init(PresenceMan.GSON, ProxyServer.getInstance().getWorkerEventLoopGroup());
            return WebRequester.getAsync(url, headers);
        }
        public static CompletableFuture<WebRequester.Result> post(String url) {return post(url, new HashMap<>(), new HashMap<>());}
        public static CompletableFuture<WebRequester.Result> post(String url, Map<String, String> headers) {return post(url, headers, new HashMap<>());}
        public static CompletableFuture<WebRequester.Result> post(String url, Map<String, String> headers, Map<String, String> body) {
            WebRequester.init(PresenceMan.GSON, ProxyServer.getInstance().getWorkerEventLoopGroup());
            return WebRequester.postAsync(url, headers, body);
        }
    }
}
