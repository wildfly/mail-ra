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

import javax.mail.NoSuchProviderException;
import javax.mail.Session;
import javax.mail.Store;

/**
 * Represents a POP3s mail folder
 *
 * @author <a href="mailto:scott.stark@jboss.org">Scott Stark</a>
 * @author <a href="mailto:jesper.pedersen@jboss.org">Jesper Pedersen</a>
 */
public class POP3sMailFolder extends POP3MailFolder {
    /**
     * Constructor
     *
     * @param spec The mail activation
     */
    public POP3sMailFolder(MailActivationSpec spec) {
        super(spec);
    }

    /**
     * Open a store
     *
     * @param session The mail session
     * @return The store
     * @throws javax.mail.NoSuchProviderException Thrown if there is no provider
     */
    protected Store openStore(Session session) throws NoSuchProviderException {
        return session.getStore("pop3s");
    }
}
