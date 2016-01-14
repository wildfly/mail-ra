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

import java.io.Serializable;
import javax.resource.ResourceException;
import javax.resource.spi.Activation;
import javax.resource.spi.ActivationSpec;
import javax.resource.spi.ConfigProperty;
import javax.resource.spi.InvalidPropertyException;
import javax.resource.spi.ResourceAdapter;



/**
 * The encapsulation of the mail folder endpoint specification
 *
 * @author <a href="mailto:scott.stark@jboss.org">Scott Stark</a>
 * @author <a href="mailto:jesper.pedersen@jboss.org">Jesper Pedersen</a>
 * @author <a href="mailto:mluksa@redhat.com">Marko Luksa</a>
 */
@Activation(messageListeners = {MailListener.class})
public class MailActivationSpec implements ActivationSpec, Serializable {
    /**
     * @since 1.0
     */
    private static final long serialVersionUID = -3034364895936568423L;

    /**
     * The resource adapter
     */
    private transient ResourceAdapter ra;

    /**
     * The mail server hostname/address
     */
    @ConfigProperty
    private String mailServer = "mailhost";

    /**
     * The mail store protocol
     */
    @ConfigProperty
    private String storeProtocol = "imap";

    /**
     * The mail folder name
     */
    @ConfigProperty
    private String mailFolder;

    /**
     * The message selector
     */
    @ConfigProperty
    private String messageSelector;

    /**
     * The mail store user
     */
    @ConfigProperty
    private String userName;

    /**
     * The mail store password
     */
    @ConfigProperty(confidential = true)
    private String password;

    /**
     * The new messages check delay in MS
     */
    @ConfigProperty
    private Long pollingInterval = 60000L;

    /**
     * The maximum number of messages
     */
    @ConfigProperty
    private Integer maxMessages = 1;

    /**
     * Enable JavaMail debugging
     */
    @ConfigProperty
    private Boolean debug = false;

    /**
     * Flush - for pop3 flush the mailbox after checking
     */
    @ConfigProperty
    private Boolean flush = Boolean.TRUE;

    /**
     * Starttls - ssl
     */
    @ConfigProperty
    private Boolean starttls = false;

    /**
     * The port
     */
    @ConfigProperty
    private Integer port;

    /**
     * Constructor
     */
    public MailActivationSpec() {
    }

    /**
     * Get the mail server
     *
     * @return The value
     */
    public String getMailServer() {
        return mailServer;
    }

    /**
     * Set the mail server
     *
     * @param mailServer The value
     */
    public void setMailServer(String mailServer) {
        this.mailServer = mailServer;
    }

    /**
     * Get the store protocol
     *
     * @return The value
     */
    public String getStoreProtocol() {
        return storeProtocol;
    }

    /**
     * Set the store protocol
     *
     * @param storeProtocol The value
     */
    public void setStoreProtocol(String storeProtocol) {
        this.storeProtocol = storeProtocol;
    }

    /**
     * Get the mail folder
     *
     * @return The value
     */
    public String getMailFolder() {
        return mailFolder;
    }

    /**
     * Set the mail folder
     *
     * @param mailFolder The value
     */
    public void setMailFolder(String mailFolder) {
        this.mailFolder = mailFolder;
    }

    /**
     * Get the message selector
     *
     * @return The value
     */
    public String getMessageSelector() {
        return messageSelector;
    }

    /**
     * Set the message selector
     *
     * @param messageSelector The value
     */
    public void setMessageSelector(String messageSelector) {
        this.messageSelector = messageSelector;
    }

    /**
     * Get the user name
     *
     * @return The value
     */
    public String getUserName() {
        return userName;
    }

    /**
     * Set the user name
     *
     * @param userName The value
     */
    public void setUserName(String userName) {
        this.userName = userName;
    }

    /**
     * Get the password
     *
     * @return The value
     */
    public String getPassword() {
        return password;
    }

    /**
     * Set the password
     *
     * @param password The value
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Get the polling interval
     *
     * @return The value
     */
    public Long getPollingInterval() {
        return pollingInterval;
    }

    /**
     * Set the polling interval
     *
     * @param pollingInterval The value
     */
    public void setPollingInterval(Long pollingInterval) {
        this.pollingInterval = pollingInterval;
    }

    /**
     * Get the max messages
     *
     * @return The value
     */
    public int getMaxMessages() {
        return maxMessages;
    }

    /**
     * Set the max messages
     *
     * @param maxMessages The value
     */
    public void setMaxMessages(int maxMessages) {
        this.maxMessages = maxMessages;
    }

    /**
     * Get the max messages
     *
     * @return The value
     */
    public boolean isDebug() {
        return debug;
    }

    /**
     * Set the debug
     *
     * @param debug The value
     */
    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    /**
     * Get the port
     *
     * @return The value
     */
    public int getPort() {
        return port == null ? 0 : port;
    }

    /**
     * Set the port
     *
     * @param port The value
     */
    public void setPort(int port) {
        this.port = port;
    }

    /**
     * Get the starttls
     *
     * @return The value
     */
    public boolean isStarttls() {
        return starttls;
    }

    /**
     * Set the starttls
     *
     * @param starttls The value
     */
    public void setStarttls(boolean starttls) {
        this.starttls = starttls;
    }

    /**
     * Get the flush
     *
     * @return The value
     */
    public boolean isFlush() {
        return flush;
    }

    /**
     * Set the flush
     *
     * @param flush The value
     */
    public void setFlush(boolean flush) {
        this.flush = flush;
    }

    /**
     * Get the resource adapter
     *
     * @return The value
     */
    public ResourceAdapter getResourceAdapter() {
        return ra;
    }

    /**
     * Set the resource adapter
     *
     * @param ra The value
     * @throws javax.resource.ResourceException Thrown if an error occurs
     */
    public void setResourceAdapter(ResourceAdapter ra) throws ResourceException {
        this.ra = ra;
    }

    /**
     * Validate
     *
     * @throws javax.resource.spi.InvalidPropertyException Thrown if an error occurs
     */
    public void validate() throws InvalidPropertyException {
    }

    /**
     * String representation
     *
     * @return The value
     */
    public String toString() {
        return "MailActivationSpec(" +
                "mailServer=" +
                mailServer +
                ", storeProtocol=" +
                storeProtocol +
                ", mailFolder=" +
                mailFolder +
                ", pollingInterval=" +
                pollingInterval +
                ", messageSelector=" +
                messageSelector +
                ", userName=" +
                userName +
                ", maxMessages=" +
                maxMessages +
                ", debug=" +
                debug +
                ", starttls=" +
                starttls +
                ", port=" +
                port +
                ")";

    }
}
