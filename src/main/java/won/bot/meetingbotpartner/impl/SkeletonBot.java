package won.bot.meetingbotpartner.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import won.bot.framework.bot.base.EventBot;
import won.bot.framework.eventbot.EventListenerContext;
import won.bot.framework.eventbot.action.BaseEventBotAction;
import won.bot.framework.eventbot.behaviour.ExecuteWonMessageCommandBehaviour;
import won.bot.framework.eventbot.bus.EventBus;
import won.bot.framework.eventbot.event.Event;
import won.bot.framework.eventbot.event.impl.command.connect.ConnectCommandEvent;
import won.bot.framework.eventbot.event.impl.command.create.CreateAtomCommandEvent;
import won.bot.framework.eventbot.event.impl.command.create.CreateAtomCommandSuccessEvent;
import won.bot.framework.eventbot.event.impl.wonmessage.ConnectFromOtherAtomEvent;
import won.bot.framework.eventbot.event.impl.wonmessage.MessageFromOtherAtomEvent;
import won.bot.framework.eventbot.listener.EventListener;
import won.bot.meetingbotpartner.context.SkeletonBotContextWrapper;
import won.protocol.message.WonMessage;
import won.protocol.message.builder.WonMessageBuilder;
import won.protocol.model.ConnectionState;
import won.protocol.util.DefaultAtomModelWrapper;
import won.protocol.util.WonRdfUtils;
import won.protocol.vocabulary.WXCHAT;

import java.lang.invoke.MethodHandles;
import java.net.URI;

public class SkeletonBot extends EventBot {
    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private static final String API_TAG = "meetingapi";
    private int registrationMatcherRetryInterval;

    //Given a Won Message returns the Text message from it
    private String extractTextMessageFromWonMessage(WonMessage wonMessage) {
        if (wonMessage == null) return null;
        return WonRdfUtils.MessageUtils.getTextMessage(wonMessage);
    }

    @Override
    protected void initializeEventListeners() {
        EventListenerContext ctx = getEventListenerContext();
        if (!(getBotContextWrapper() instanceof SkeletonBotContextWrapper)) {
            logger.error(getBotContextWrapper().getBotName() + " does not work without a SkeletonBotContextWrapper");
            throw new IllegalStateException(getBotContextWrapper().getBotName() + " does not work without a " +
                    "SkeletonBotContextWrapper");
        }
        EventBus bus = getEventBus();
        SkeletonBotContextWrapper botContextWrapper = (SkeletonBotContextWrapper) getBotContextWrapper();
        // register listeners for event.impl.command events used to tell the bot to send
        // messages
        ExecuteWonMessageCommandBehaviour wonMessageCommandBehaviour = new ExecuteWonMessageCommandBehaviour(ctx);
        wonMessageCommandBehaviour.activate();

        ctx.getEventBus().subscribe(CreateAtomCommandSuccessEvent.class, new BaseEventBotAction(ctx) {
            @Override
            protected void doRun(Event event, EventListener eventListener) throws Exception {
                CreateAtomCommandSuccessEvent e = (CreateAtomCommandSuccessEvent) event;

                logger.info("Created atom {}: {}", e.isSuccess() ? "YES" : "NO", e.getAtomURI());
            }
        });

        bus.subscribe(ConnectFromOtherAtomEvent.class, new BaseEventBotAction(ctx) {
            @Override
            protected void doRun(Event event, EventListener eventListener) {

                ConnectFromOtherAtomEvent e = (ConnectFromOtherAtomEvent) event;
                EventListenerContext ctx = getEventListenerContext();

                // If the connection is not CONNECTED, we send a connect message
                logger.info("Con: {}, state: {}", e.getCon(), e.getCon().getState());
                if (e.getCon().getState() != ConnectionState.CONNECTED) {

                    ConnectFromOtherAtomEvent con = (ConnectFromOtherAtomEvent) event;
                    ConnectCommandEvent connectCommandEvent = new ConnectCommandEvent(con.getRecipientSocket(),
                            con.getSenderSocket(), "connecting...");
                    ctx.getEventBus().publish(connectCommandEvent);
                    return;
                }

                String request = "48.210159,16.355502;48.202251,16.361638/Metro Station"; // should result into
                // Volkstheater
                WonMessage wonMessage =
                        WonMessageBuilder.connectionMessage().sockets().sender(e.getSocketURI()).recipient(e.getTargetSocketURI()).content().text(request).build();
                getEventListenerContext().getWonMessageSender().prepareAndSendMessage(wonMessage);

            }

        });

        bus.subscribe(MessageFromOtherAtomEvent.class, new BaseEventBotAction(ctx) {
            @Override
            protected void doRun(Event event, EventListener eventListener) throws Exception {
                MessageFromOtherAtomEvent e = (MessageFromOtherAtomEvent) event;
                logger.info("Got message: '{}'", extractTextMessageFromWonMessage(e.getWonMessage()));
            }
        });

        // Create a new atom URI
        URI wonNodeUri = ctx.getNodeURISource().getNodeURI();
        URI atomURI = ctx.getWonNodeInformationService().generateAtomURI(wonNodeUri);

        // Set atom data - here only shown for commonly used (hence 'default') properties
        DefaultAtomModelWrapper atomWrapper = new DefaultAtomModelWrapper(atomURI);
        atomWrapper.setTitle("Searching for meetingbot");
        atomWrapper.setDescription("Contact me for all things Cthulhu, Yogge-Sothothe and R'lyeh");
        atomWrapper.addTag(API_TAG);
        //an atom must have at least one socket
        atomWrapper.addSocket("#chatSocket", WXCHAT.ChatSocketString);
        //publish command
        CreateAtomCommandEvent createCommand = new CreateAtomCommandEvent(atomWrapper.getDataset(), "atom_uris");
        ctx.getEventBus().publish(createCommand);

    }

    // bean setter, used by spring
    public void setRegistrationMatcherRetryInterval(final int registrationMatcherRetryInterval) {
        this.registrationMatcherRetryInterval = registrationMatcherRetryInterval;
    }
}
