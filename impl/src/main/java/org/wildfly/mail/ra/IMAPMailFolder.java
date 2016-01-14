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

import javax.mail.Flags;
import javax.mail.Flags.Flag;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.search.FlagTerm;

/**
 * An IMAP mail folder
 *
 * @author <a href="mailto:scott.stark@jboss.org">Scott Stark</a>
 * @author <a href="mailto:jesper.pedersen@jboss.org">Jesper Pedersen</a>
 * @author <a href="mailto:mluksa@redhat.com">Marko Luksa</a>
 */
public class IMAPMailFolder extends MailFolder {
    /**
     * Constructor
     *
     * @param spec The mail activation spec
     */
    public IMAPMailFolder(MailActivationSpec spec) {
        super(spec);
    }

    /**
     * Get the messages from a folder
     *
     * @param folder The folder
     * @return The messages
     * @throws MessagingException Thrown if there is an error
     */
    protected Message[] getMessages(Folder folder) throws MessagingException {
        Message[] result = folder.search(new FlagTerm(new Flags(Flag.SEEN), false));

        if (result != null && result.length > 0) { return result; }

        return new Message[0];
    }

    /**
     * {@inheritDoc}
     */
    protected Store openStore(Session session) throws NoSuchProviderException {
        return session.getStore("imap");
    }

    /**
     * {@inheritDoc}
     */
    protected void markMessageSeen(Message message) throws MessagingException {
        message.setFlag(Flag.SEEN, true);
    }

    /**
     * {@inheritDoc}
     */
    protected void closeStore(boolean success, Store store, Folder folder) throws MessagingException {
        try {
            if (folder != null && folder.isOpen()) {
                folder.close(success);
            }
        } finally {
            if (store != null && store.isConnected()) {
                store.close();
            }
        }
    }
}
