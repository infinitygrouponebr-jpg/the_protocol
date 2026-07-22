package Infinitygroup.the_protocol.client;

import Infinitygroup.the_protocol.network.ParkourRollSyncPayload;

public final class ParkourRollClientSync {
    private ParkourRollClientSync() {
    }

    public static void handle(ParkourRollSyncPayload payload) {
        ParkourRollClient.handleSync(payload);
    }
}
