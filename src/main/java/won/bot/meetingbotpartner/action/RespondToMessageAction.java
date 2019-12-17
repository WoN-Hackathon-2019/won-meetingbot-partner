package won.bot.meetingbotpartner.action;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import won.bot.framework.eventbot.EventListenerContext;
import won.bot.framework.eventbot.action.BaseEventBotAction;
import won.bot.framework.eventbot.event.ConnectionSpecificEvent;
import won.bot.framework.eventbot.event.Event;
import won.bot.framework.eventbot.event.MessageEvent;
import won.bot.framework.eventbot.listener.EventListener;
import won.protocol.message.WonMessage;
import won.protocol.message.builder.WonMessageBuilder;
import won.protocol.util.WonRdfUtils;

import java.lang.invoke.MethodHandles;
import java.net.URI;
import java.util.Date;

/**
 * Listener that responds to open and message events with automatic messages.
 * Can be configured to apply a timeout (non-blocking) before sending messages.
 */
public class RespondToMessageAction extends BaseEventBotAction {
    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private long millisTimeoutBeforeReply = 0;
    //maps categoryName to Id;

    public RespondToMessageAction(EventListenerContext eventListenerContext) {
        super(eventListenerContext);
    }

    public RespondToMessageAction(final EventListenerContext eventListenerContext,
                                  final long millisTimeoutBeforeReply) {
        super(eventListenerContext);
        this.millisTimeoutBeforeReply = millisTimeoutBeforeReply;
    }


    @Override
    protected void doRun(final Event event, EventListener executingListener) throws Exception {
        if (event instanceof ConnectionSpecificEvent) {
            handleMessageEvent((ConnectionSpecificEvent) event);
        }
    }

    //Given a Won Message returns the Text message from it
    private String extractTextMessageFromWonMessage(WonMessage wonMessage) {
        if (wonMessage == null) return null;
        return WonRdfUtils.MessageUtils.getTextMessage(wonMessage);
    }

    private void handleMessageEvent(final ConnectionSpecificEvent messageEvent) {
        getEventListenerContext().getTaskScheduler().schedule(() -> {
            String message = null;
            if (messageEvent instanceof MessageEvent) {
                message =
                        createMessage(extractTextMessageFromWonMessage(((MessageEvent) messageEvent).getWonMessage()));
            } else {
                message = createMessage(null);
            }
            URI connectionUri = messageEvent.getConnectionURI();
            logger.debug("sending message " + message);
            URI senderSocket = messageEvent.getSocketURI();
            URI targetSocket = messageEvent.getTargetSocketURI();
            try {
                EventListenerContext ctx = getEventListenerContext();


                WonMessage wonMessage =
                        WonMessageBuilder.connectionMessage().sockets().sender(senderSocket).recipient(targetSocket).content().text(message).build();
                ctx.getWonMessageSender().prepareAndSendMessage(wonMessage);

            } catch (Exception e) {
                logger.warn("could not send message via connection {}", connectionUri, e);
            }
        }, new Date(System.currentTimeMillis() + this.millisTimeoutBeforeReply));
    }

    private String createMessage(String extractTextMessageFromWonMessage) {
        logger.info("Message: {}", extractTextMessageFromWonMessage);

        return "Hello";
    }

}