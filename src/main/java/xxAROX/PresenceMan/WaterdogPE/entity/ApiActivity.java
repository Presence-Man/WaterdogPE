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

import com.google.gson.JsonObject;
import lombok.*;
import lombok.experimental.Accessors;
import xxAROX.PresenceMan.WaterdogPE.PresenceMan;

@NoArgsConstructor @AllArgsConstructor
@ToString
@Getter @Setter @Accessors(chain = true)
public class ApiActivity {
    private Long client_id;
    public ActivityType type;
    public String state;
    public String details;
    public Long end = null;
    public String large_icon_key = null;
    public String large_icon_text = null;
    public Integer party_max_player_count = null;
    public Integer party_player_count = null;

    public ApiActivity(ActivityType type, String state, String details, Long end, String large_icon_key, String large_icon_text) {
        this.type = type;
        this.state = state;
        this.details = details;
        this.end = end;
        this.large_icon_key = large_icon_key;
        this.large_icon_text = large_icon_text;
    }

    public JsonObject serialize(){
        JsonObject json = new JsonObject();
        json.addProperty("client_id", String.valueOf(client_id));
        json.addProperty("type", type.toString());
        json.addProperty("state", state);
        json.addProperty("details", details);
        json.addProperty("end", end);
        json.addProperty("large_icon_key", large_icon_key);
        json.addProperty("large_icon_text", large_icon_text);
        json.addProperty("party_max_player_count", party_max_player_count);
        json.addProperty("party_player_count", party_player_count);
        return json;
    }

    public final static class defaults {
        public static ApiActivity activity(){
            return new ApiActivity(
                    PresenceMan.client_id,
                    ActivityType.PLAYING,
                    ServerPresence.getDefault_presence().getState(),
                    ServerPresence.getDefault_presence().getDetails(),
                    null,
                    ServerPresence.getDefault_presence().getLarge_image_key(),
                    ServerPresence.getDefault_presence().getLarge_image_text(),
                    null,
                    null
            );
        }

        /**
         * time = System.currentTimeMillis(): long
         * null|ApiActivity base
         */
        public static ApiActivity ends_in(long time, ApiActivity base){
            if (base == null) base = activity();
            base.end = time;
            return base;
        }
        public static ApiActivity ends_in(long time){
            return ends_in(time, null);
        }
        public static ApiActivity players_left(int current_players, int max_players, ApiActivity base){
            if (base == null) base = activity();
            base.party_player_count = current_players;
            base.party_max_player_count = max_players;
            return base;
        }
        public static ApiActivity players_left(int current_players, int max_players){
            return players_left(current_players, max_players, null);
        }
    }
}
