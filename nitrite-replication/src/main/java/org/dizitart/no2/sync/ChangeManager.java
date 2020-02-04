package org.dizitart.no2.sync;

import org.dizitart.no2.collection.Document;
import org.dizitart.no2.collection.NitriteId;
import org.dizitart.no2.sync.crdt.LastWriteWinState;
import org.dizitart.no2.sync.message.BatchChangeContinue;
import org.dizitart.no2.sync.message.BatchChangeStart;

import java.util.*;

/**
 * @author Anindya Chatterjee
 */
public class ChangeManager {
    private LocalReplica replica;
    private Timer timer;
    private Set<NitriteId> acceptedChanges;
    private Set<NitriteId> acceptedTombstones;

    public ChangeManager(LocalReplica replica) {
        this.replica = replica;
        this.acceptedChanges = new LinkedHashSet<>();
        this.acceptedTombstones = new LinkedHashSet<>();
    }

    public void sendChanges() {
        if (replica.isConnected()) {
            Long lastSyncTime = replica.getLastSyncTime();
            String uuid = UUID.randomUUID().toString();

            MessageFactory factory = replica.getMessageFactory();
            MessageTemplate messageTemplate = replica.getMessageTemplate();

            BatchChangeStart message = createStart(factory, uuid, lastSyncTime);
            messageTemplate.sendMessage(message);

            timer = new Timer();
            timer.scheduleAtFixedRate(new TimerTask() {
                boolean hasMore = true;
                int start = replica.getConfig().getChunkSize();

                @Override
                public void run() {
                    LastWriteWinState state = replica.getCrdt().getChangesSince(lastSyncTime, start,
                        replica.getConfig().getChunkSize());
                    if (state.getChanges().size() == 0 && state.getTombstones().size() == 0) {
                        hasMore = false;
                    }

                    if (hasMore) {
                        BatchChangeContinue startMessage = factory.createChangeContinue(replica.getConfig(),
                            replica.getReplicaId(), uuid);

                        for (Document document : state.getChanges()) {
                            acceptedChanges.add(document.getId());
                        }

                        for (Map.Entry<Long, Long> entry : state.getTombstones().entrySet()) {
                            acceptedTombstones.add(NitriteId.createId(entry.getKey()));
                        }

                        start = start + replica.getConfig().getChunkSize();
                    }

                    if (!hasMore) {
                        timer.cancel();
                    }
                }
            }, 0, replica.getConfig().getDebounce());
        }
    }

    public void shutdown() {

    }

    private BatchChangeStart createStart(MessageFactory factory, String uuid, Long lastSyncTime) {
        BatchChangeStart startMessage = factory.createChangeStart(replica.getConfig(),
            replica.getReplicaId(), uuid);

        LastWriteWinState state = replica.getCrdt().getChangesSince(lastSyncTime, 0,
            replica.getConfig().getChunkSize());

        for (Document document : state.getChanges()) {
            acceptedChanges.add(document.getId());
        }

        for (Map.Entry<Long, Long> entry : state.getTombstones().entrySet()) {
            acceptedTombstones.add(NitriteId.createId(entry.getKey()));
        }

        startMessage.setState(state);
        return startMessage;
    }
}
