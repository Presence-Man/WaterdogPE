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

import dev.waterdog.waterdogpe.event.defaults.InitialServerConnectedEvent;
import dev.waterdog.waterdogpe.event.defaults.PlayerDisconnectedEvent;
import dev.waterdog.waterdogpe.network.PacketDirection;
import org.cloudburstmc.protocol.bedrock.data.skin.ImageData;
import org.cloudburstmc.protocol.bedrock.data.skin.SerializedSkin;
import org.cloudburstmc.protocol.bedrock.packet.PlayerSkinPacket;
import xxAROX.PresenceMan.WaterdogPE.utils.Utils;

import java.nio.charset.StandardCharsets;

public final class EventListener {

    public static void InitialServerConnectedEvent(InitialServerConnectedEvent event){
        if (Utils.isFromSameHost(event.getPlayer().getAddress().getAddress())) return;
        if (!PresenceMan.enable_default) return;

        var persona = event.getPlayer().getLoginData().getClientData().get("PersonaSkin").getAsBoolean();
        if (!persona && PresenceMan.update_skin) {
            var skin = SerializedSkin.builder().skinData(ImageData.of(event.getPlayer().getLoginData().getClientData().get("SkinData").getAsString().getBytes(StandardCharsets.UTF_8))).build();
            PresenceMan.save_skin(event.getPlayer(), skin);
        }
        PresenceMan.setActivity(event.getPlayer(), PresenceMan.default_activity);


        event.getPlayer().getPluginPacketHandlers().add((bedrockPacket, packetDirection) -> {
            if (packetDirection.equals(PacketDirection.FROM_USER) && bedrockPacket instanceof PlayerSkinPacket playerSkinPacket) // TODO: cooldown 5sec
                PresenceMan.save_skin(event.getPlayer(), playerSkinPacket.getSkin());
            return null;
        });
    }

    public void PlayerQuitEvent(PlayerDisconnectedEvent event){
        if (Utils.isFromSameHost(event.getPlayer().getAddress().getAddress())) return;
        PresenceMan.presences.remove(event.getPlayer().getXuid());
        PresenceMan.offline(event.getPlayer());
    }
}
