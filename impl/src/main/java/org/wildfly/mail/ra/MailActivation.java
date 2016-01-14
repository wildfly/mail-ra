/*
 * JBoss, Home of Professional Open Source
 * Copyright 2016, Red Hat, Inc., and individual contributors as indicated
 * by the @authors tag.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wildfly.mail.ra;

import java.lang.reflect.Method;
import javax.mail.Message;
import javax.resource.spi.endpoint.MessageEndpoint;
import javax.resource.spi.endpoint.MessageEndpointFactory;
import javax.resource.spi.work.Work;

import org.jboss.logging.Logger;

/**
 * The MailActivation encapsulates a MailResourceAdapter#endpointActivation
 *
 * @author <a href="mailto:scott.stark@jboss.org">Scott Stark</a>
 * @author <a href="mailto:jesper.pedersen@jboss.org">Jesper Pedersen</a>
 * @author <a href="mailto:mluksa@redhat.com">Marko Luksa</a>
 */
public class MailActivation implements Comparable, Work {
    /**
     * The logger
     */
    private static final Logger log = Logger.getLogger(MailActivation.class);

    /**
     * The MailListener.onMessage method
     */
    public static final Method ON_MESSAGE;

    /**
     * A flag indicated if the unit of work has been released
     */
    private boolean released;

    /**
     * The time at which the next new msgs check should be performed
     */
    private long nextNewMsgCheckTime;

    /**
     * The activation spec for the mail folder
     */
    protected MailActivationSpec spec;

    /**
     * The message endpoint factory
     */
    protected MessageEndpointFactory endpointFactory;

    static {
        try {
            ON_MESSAGE = MailListener.class.getMethod("onMessage", Message.class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Constructor
     *
     * @param endpointFactory The message endpoint factory
     * @param spec            The mail activation spec
     */
    public MailActivation(MessageEndpointFactory endpointFactory, MailActivationSpec spec) {
        this.endpointFactory = endpointFactory;
        this.spec = spec;
    }

    /**
     * Get the next message check time
     *
     * @return The value
     */
    public long getNextNewMsgCheckTime() {
        return nextNewMsgCheckTime;
    }

    /**
     * Update the next message check time
     *
     * @param now The current time
     */
    public void updateNextNewMsgCheckTime(long now) {
        nextNewMsgCheckTime = now + spec.getPollingInterval();
    }

    /**
     * {@inheritDoc}
     */
    public int compareTo(Object obj) {
        MailActivation ma = (MailActivation) obj;

        return (int) (nextNewMsgCheckTime - ma.getNextNewMsgCheckTime());
    }

    /**
     * Is the activation released ?
     *
     * @return True if released; otherwise false
     */
    public boolean isReleased() {
        return released;
    }

    /**
     * Release the activation
     */
    public void release() {
        released = true;

        log.tracef("released");
    }

    /**
     * {@inheritDoc}
     */
    public void run() {
        released = false;

        log.tracef("Begin new msgs check");

        try {
            MailFolder mailFolder = MailFolder.getInstance(spec);
            mailFolder.open();

            while (mailFolder.hasNext()) {
                Message msg = (Message) mailFolder.next();
                deliverMsg(msg);
            }

            mailFolder.close();
        } catch (Exception e) {
            log.error("Failed to execute folder check, spec=" + spec);
        }

        log.tracef("End new msgs check");
    }

    /**
     * Deliver the message
     *
     * @param msg The message
     */
    private void deliverMsg(Message msg) {
        MessageEndpoint endpoint = null;
        try {
            endpoint = endpointFactory.createEndpoint(null);
            if (endpoint != null && endpoint instanceof MailListener) {
                log.tracef("deliverMsg: msg subject=", msg.getSubject());

                MailListener listener = (MailListener) endpoint;
                listener.onMessage(msg);
            }
        } catch (Throwable e) {
            log.debug("onMessage delivery failure", e);
        } finally {
            if (endpoint != null) {
                endpoint.release();
            }
        }
    }

    public void unrelease() {
        released = false;
    }
}
