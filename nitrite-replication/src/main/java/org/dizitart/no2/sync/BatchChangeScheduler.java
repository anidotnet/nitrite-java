package org.dizitart.no2.sync;

import org.dizitart.no2.sync.crdt.LastWriteWinState;
import org.dizitart.no2.sync.message.BatchChangeContinue;
import org.dizitart.no2.sync.message.BatchChangeEnd;
import org.dizitart.no2.sync.message.BatchChangeStart;

import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

/**
 * @author Anindya Chatterjee
 */
public class BatchChangeScheduler {
    private Timer timer;
    private ReplicationTemplate replica;
    private MessageFactory factory;
    private MessageTemplate messageTemplate;
    private FeedJournal journal;

    public BatchChangeScheduler(ReplicationTemplate replica) {
        this.replica = replica;
        this.factory = replica.getMessageFactory();
        this.messageTemplate = replica.getMessageTemplate();
        this.journal = replica.getFeedJournal();
    }

    public void schedule() {
        if (replica.isConnected()) {
            Long lastSyncTime = replica.getLastSyncTime();
            String uuid = UUID.randomUUID().toString();

            System.out.println("*****after schedule = " + messageTemplate.toString() + " websocket = " + messageTemplate.getWebSocket());

            BatchChangeStart message = createStart(factory, uuid, lastSyncTime);
            System.out.println("sending batch message");
            messageTemplate.sendMessage(message);
            journal.write(message.getFeed());

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
                        BatchChangeContinue message = factory.createChangeContinue(replica.getConfig(),
                            replica.getReplicaId(), uuid, state);

                        messageTemplate.sendMessage(message);
                        journal.write(state);
                        start = start + replica.getConfig().getChunkSize();
                    }

                    if (!hasMore) {
                        timer.cancel();
                    }
                }
            }, 0, replica.getConfig().getDebounce());

            BatchChangeEnd endMessage = factory.createChangeEnd(replica.getConfig(), replica.getReplicaId(), uuid, lastSyncTime);
            messageTemplate.sendMessage(endMessage);
        }
    }

    public void stop() {
        if (timer != null) {
            timer.cancel();
        }
    }

    private BatchChangeStart createStart(MessageFactory factory, String uuid, Long lastSyncTime) {
        BatchChangeStart startMessage = factory.createChangeStart(replica.getConfig(),
            replica.getReplicaId(), uuid);

        LastWriteWinState state = replica.getCrdt().getChangesSince(lastSyncTime, 0,
            replica.getConfig().getChunkSize());

        startMessage.setFeed(state);
        return startMessage;
    }
}
