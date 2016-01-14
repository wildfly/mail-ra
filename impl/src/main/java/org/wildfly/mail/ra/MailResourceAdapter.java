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


import org.jboss.logging.Logger;

import javax.resource.NotSupportedException;
import javax.resource.ResourceException;
import javax.resource.spi.ActivationSpec;
import javax.resource.spi.BootstrapContext;
import javax.resource.spi.Connector;
import javax.resource.spi.ResourceAdapter;
import javax.resource.spi.ResourceAdapterInternalException;
import javax.resource.spi.endpoint.MessageEndpointFactory;
import javax.resource.spi.work.WorkException;
import javax.resource.spi.work.WorkManager;
import javax.transaction.xa.XAResource;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The mail resource adapter
 *
 * @author <a href="mailto:scott.stark@jboss.org">Scott Stark</a>
 * @author <a href="mailto:jesper.pedersen@jboss.org">Jesper Pedersen</a>
 */
@Connector(
    displayName = "JavaMail Adapter",
    description = "WildFly JavaMail Resource Adapter",
    vendorName = "Red Hat, Inc.",
    eisType = "JavaMail Adapter",
    licenseRequired = true,
    licenseDescription = "JBoss, Home of Professional Open Source.\n" +
        "Copyright 2014, Red Hat, Inc., and individual contributors\n" +
        "as indicated by the @author tags. See the copyright.txt file in the\n" +
        "distribution for a full listing of individual contributors.\n" +
        "\n" +
        "This is free software; you can redistribute it and/or modify it\n" +
        "under the terms of the GNU Lesser General Public License as\n" +
        "published by the Free Software Foundation; either version 2.1 of\n" +
        "the License, or (at your option) any later version.\n" +
        "\n" +
        "This software is distributed in the hope that it will be useful,\n" +
        "but WITHOUT ANY WARRANTY; without even the implied warranty of\n" +
        "MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU\n" +
        "Lesser General Public License for more details.\n" +
        "\n" +
        "You should have received a copy of the GNU Lesser General Public\n" +
        "License along with this software; if not, write to the Free\n" +
        "Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA\n" +
        "02110-1301 USA, or see the FSF site: http://www.fsf.org.\n"
)
public class MailResourceAdapter implements ResourceAdapter {
    /**
     * The logger
     */
    private static Logger log = Logger.getLogger(MailResourceAdapter.class);

    /**
     * The bootstrap context
     */
    private BootstrapContext ctx;

    /**
     * The activations by activation spec
     */
    private ConcurrentHashMap<MailActivationSpec, MailActivation> activations;

    /**
     * The new message worker
     */
    private NewMsgsWorker newMsgsWorker;

    /**
     * Queue size
     */
    private Integer queueSize;

    /**
     * Constructor
     */
    public MailResourceAdapter() {
        this.ctx = null;
        this.activations = new ConcurrentHashMap<>();
        this.newMsgsWorker = null;
        this.queueSize = 1024;
    }

    /**
     * Get the queue size
     *
     * @return The value
     */
    public Integer getQueueSize() {
        return queueSize;
    }

    /**
     * Set the queue size
     *
     * @param v The value
     */
    public void setQueueSize(Integer v) {
        if (v != null && v > 0) { queueSize = v; }
    }

    /**
     * {@inheritDoc}
     */
    public void start(BootstrapContext ctx) throws ResourceAdapterInternalException {
        log.debugf("start");

        this.ctx = ctx;

        WorkManager mgr = ctx.getWorkManager();
        newMsgsWorker = new NewMsgsWorker(mgr, queueSize);

        try {
            mgr.scheduleWork(newMsgsWorker);
        } catch (WorkException e) {
            throw new ResourceAdapterInternalException(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void stop() {
        log.debugf("stop");

        newMsgsWorker.release();
    }

    /**
     * {@inheritDoc}
     */
    public void endpointActivation(MessageEndpointFactory endpointFactory, ActivationSpec spec)
        throws ResourceException {
        log.debugf("endpointActivation: endpointFactory=%s,spec=%s", endpointFactory, spec);

        if (spec == null) { throw new NotSupportedException("Null MailActivationSpec instance"); }

        if (!(spec instanceof MailActivationSpec)) {
            throw new NotSupportedException("Not a MailActivationSpec instance" + spec.getClass().getName());
        }

        MailActivationSpec mailSpec = (MailActivationSpec) spec;
        MailActivation activation = new MailActivation(endpointFactory, mailSpec);

        try {
            newMsgsWorker.watch(activation);
            activations.put(mailSpec, activation);
        } catch (InterruptedException e) {
            throw new ResourceException("Failed to schedule new msg check", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void endpointDeactivation(MessageEndpointFactory endpointFactory, ActivationSpec spec) {
        log.debugf("endpointDeactivation: endpointFactory=%s,spec=%s", endpointFactory, spec);

        if (spec != null && spec instanceof MailActivationSpec) {
            MailActivation activation = activations.remove(spec);

            if (activation != null) { activation.release(); }
        }
    }

    /**
     * {@inheritDoc}
     */
    public XAResource[] getXAResources(ActivationSpec[] specs) throws ResourceException {
        return new XAResource[0];
    }

    /**
     * {@inheritDoc}
     */
    public int hashCode() {
        return super.hashCode();
    }

    /**
     * {@inheritDoc}
     */
    public boolean equals(Object other) {
        return super.equals(other);
    }
}
