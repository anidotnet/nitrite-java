package org.dizitart.no2.sync.event;

import org.dizitart.no2.common.concurrent.ExecutorServiceManager;
import org.dizitart.no2.common.event.NitriteEventBus;
import org.dizitart.no2.common.util.StringUtils;
import org.dizitart.no2.sync.ReplicationException;
import org.dizitart.no2.sync.message.DataGateMessage;

import java.util.concurrent.ExecutorService;

/**
 * @author Anindya Chatterjee
 */
public class ReplicationEventBus extends NitriteEventBus<ReplicationEvent, ReplicationEventListener> {
    private static final ReplicationEventBus instance = new ReplicationEventBus();

    private MessageTransformer messageTransformer;
    private MessageRouter messageRouter;

    private ReplicationEventBus() {
        messageTransformer = new MessageTransformer();
        messageRouter = new MessageRouter(getEventExecutor());
    }

    public static ReplicationEventBus getInstance() {
        return instance;
    }

    @Override
    public void post(ReplicationEvent replicationEvent) {
        messageRouter.dispatch(replicationEvent);
    }

    @Override
    protected ExecutorService getEventExecutor() {
        return ExecutorServiceManager.syncExecutor();
    }

    public void handleMessage(String message) {
        DataGateMessage msg = messageTransformer.transform(message);
        validateMessage(msg);
        ReplicationEvent event = new ReplicationEvent(msg);
        post(event);
    }

    @Override
    public void register(ReplicationEventListener replicationEventListener) {
        messageRouter.addListener(replicationEventListener);
    }

    @Override
    public void deregister(ReplicationEventListener replicationEventListener) {
        messageRouter.removeListener(replicationEventListener);
    }

    private void validateMessage(DataGateMessage message) {
        if (message == null) {
            throw new ReplicationException("empty message received from server");
        }

        if (message.getMessageInfo() == null) {
            throw new ReplicationException("message received from server does not have message info");
        }

        if (StringUtils.isNullOrEmpty(message.getMessageInfo().getCollection())) {
            throw new ReplicationException("message received from server does not have collection name");
        }
    }
}
