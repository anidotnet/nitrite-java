package org.dizitart.no2.sync.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.dizitart.no2.common.event.NitriteEventBus;
import org.dizitart.no2.common.util.StringUtils;
import org.dizitart.no2.sync.ReplicationException;
import org.dizitart.no2.sync.message.DataGateMessage;

/**
 * @author Anindya Chatterjee
 */
public class ReplicationEventBus extends NitriteEventBus<ReplicationEvent, ReplicationEventListener> {
    private MessageTransformer messageTransformer;
    private MessageRouter messageRouter;

    public ReplicationEventBus() {
        messageTransformer = new MessageTransformer();
        messageRouter = new MessageRouter();
    }

    @Override
    public void post(ReplicationEvent replicationEvent) {
        messageRouter.dispatch(replicationEvent);
    }

    public void handleMessage(ObjectMapper objectMapper, String message) {
        DataGateMessage msg = messageTransformer.transform(objectMapper, message);
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

        if (message.getMessageHeader() == null) {
            throw new ReplicationException("message received from server does not have message info");
        }

        if (StringUtils.isNullOrEmpty(message.getMessageHeader().getCollection())) {
            throw new ReplicationException("message received from server does not have collection name");
        }
    }
}
