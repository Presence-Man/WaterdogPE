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

import dev.waterdog.waterdogpe.ProxyServer;
import dev.waterdog.waterdogpe.event.defaults.InitialServerConnectedEvent;
import dev.waterdog.waterdogpe.event.defaults.PlayerDisconnectedEvent;
import dev.waterdog.waterdogpe.event.defaults.TransferCompleteEvent;
import dev.waterdog.waterdogpe.player.ProxiedPlayer;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.cloudburstmc.protocol.bedrock.PacketDirection;
import org.cloudburstmc.protocol.bedrock.data.skin.ImageData;
import org.cloudburstmc.protocol.bedrock.data.skin.SerializedSkin;
import org.cloudburstmc.protocol.bedrock.packet.PlayerSkinPacket;
import xxAROX.PresenceMan.WaterdogPE.utils.Utils;

import java.util.Base64;
import java.util.HashMap;

public final class EventListener {
    private static boolean registered = false;
    public static void register() {
        if (registered) return;
        registered = true;
        ProxyServer.getInstance().getEventManager().subscribe(InitialServerConnectedEvent.class, EventListener::InitialServerConnectedEvent);
        ProxyServer.getInstance().getEventManager().subscribe(TransferCompleteEvent.class, EventListener::TransferCompleteEvent);
        ProxyServer.getInstance().getEventManager().subscribe(PlayerDisconnectedEvent.class, EventListener::PlayerDisconnectedEvent);
    }

    private static final HashMap<String, Long> cooldowns = new HashMap<>();
    private static void InitialServerConnectedEvent(InitialServerConnectedEvent event){
        if (Utils.isFromSameHost(event.getPlayer().getAddress().getAddress())) return;
        if (!event.getPlayer().getLoginData().getClientData().get("PersonaSkin").getAsBoolean()) {
            var bytes = event.getPlayer().getLoginData().getClientData().get("SkinData").getAsString();
            PresenceMan.save_skin(event.getPlayer(),
                    SerializedSkin.builder()
                            .skinData(ImageData.of(Base64.getDecoder().decode(bytes)))
                            .animations(new ObjectArrayList<>())
                            .personaPieces(new ObjectArrayList<>())
                            .tintColors(new ObjectArrayList<>())
                            .build()
            );
        }
        PresenceMan.setActivity(event.getPlayer(), event.getServerInfo());

        var cooldown = 5;
        event.getPlayer().getPluginPacketHandlers().add((bedrockPacket, packetDirection) -> {
            ProxiedPlayer player = event.getPlayer();
            if (packetDirection.equals(PacketDirection.SERVER_BOUND) && bedrockPacket instanceof PlayerSkinPacket playerSkinPacket) {
                if (cooldowns.containsKey(player.getXuid()) && cooldowns.get(player.getXuid()) <= System.currentTimeMillis()) cooldowns.remove(player.getXuid());
                if (!cooldowns.containsKey(player.getXuid())) {
                    cooldowns.put(player.getXuid(), System.currentTimeMillis() +(1000 *cooldown));
                    PresenceMan.save_skin(player, playerSkinPacket.getSkin());
                }
            }
            return null;
        });
    }
    private static void TransferCompleteEvent(TransferCompleteEvent event){
        if (Utils.isFromSameHost(event.getPlayer().getAddress().getAddress())) return;
        if (event.getTargetServer() != null) PresenceMan.setActivity(event.getPlayer(), event.getTargetServer());
    }

    private static void PlayerDisconnectedEvent(PlayerDisconnectedEvent event){
        System.out.println("isFromSameHost: " + Utils.isFromSameHost(event.getPlayer().getAddress().getAddress()));
        if (Utils.isFromSameHost(event.getPlayer().getAddress().getAddress())) return;
        PresenceMan.offline(event.getPlayer());
        System.out.println("Should work!");
    }
}
