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

package xxAROX.PresenceMan.WaterdogPE.tasks;

import dev.waterdog.waterdogpe.scheduler.Task;
import dev.waterdog.waterdogpe.scheduler.TaskHandler;
import xxAROX.PresenceMan.WaterdogPE.PresenceMan;
import xxAROX.PresenceMan.WaterdogPE.tasks.async.FetchGatewayInformationTask;

public final class ReconnectingTask extends Task {
    public static boolean active = false;
    private static TaskHandler<ReconnectingTask> task = null;

    public static void activate() {
        if (active) return;
        active = true;
        task = PresenceMan.getInstance().getProxy().getScheduler().scheduleRepeating(new ReconnectingTask(), 20 * 5);
    }

    public static void deactivate() {
        if (!active) return;
        task.cancel();
        task = null;
        active = false;
    }

    @Override
    public void onRun(int tick) {
        FetchGatewayInformationTask.ping_backend(success -> {
            if (success) {
                PresenceMan.getInstance().getLogger().debug("Reconnected!");
                ReconnectingTask.deactivate();
            }
        });
    }

    @Override
    public void onCancel() {
    }
}
