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

import javax.resource.spi.work.Work;
import javax.resource.spi.work.WorkEvent;
import javax.resource.spi.work.WorkException;
import javax.resource.spi.work.WorkListener;
import javax.resource.spi.work.WorkManager;
import java.util.concurrent.PriorityBlockingQueue;

/**
 * Handles new messages
 *
 * @author <a href="mailto:scott.stark@jboss.org">Scott Stark</a>
 * @author <a href="mailto:jesper.pedersen@jboss.org">Jesper Pedersen</a>
 * @author <a href="mailto:mluksa@redhat.com">Marko Luksa</a>
 */
public class NewMsgsWorker implements Work, WorkListener {
    private static Logger log = Logger.getLogger(NewMsgsWorker.class);

    private boolean released;

    private WorkManager mgr;

    private PriorityBlockingQueue<MailActivation> pollQueue;

    /**
     * Constructor
     *
     * @param mgr       The work manager
     * @param queueSize The queue size
     */
    public NewMsgsWorker(WorkManager mgr, Integer queueSize) {
        this.mgr = mgr;
        this.pollQueue = new PriorityBlockingQueue<>(queueSize);
    }

    /**
     * Watch an activation
     *
     * @param activation The activation
     * @throws InterruptedException Thrown if the queue is interrupted
     */
    public void watch(MailActivation activation) throws InterruptedException {
        activation.updateNextNewMsgCheckTime(System.currentTimeMillis());

        pollQueue.put(activation);
    }

    /**
     * Release
     */
    public void release() {
        released = true;

        log.tracef("released");
    }

    /**
     * Run
     */
    public void run() {
        log.tracef("Begin run");

        while (!released) {
            try {
                MailActivation ma = pollQueue.take();

                // Wait until its time to check for new msgs
                long now = System.currentTimeMillis();
                long nextTime = ma.getNextNewMsgCheckTime();
                long sleepMS = nextTime - now;

                if (sleepMS > 0) { Thread.sleep(sleepMS); }

                if (released) { break; }

                // This has to go after the sleep otherwise we can get into an inconsistent state
                if (ma.isReleased()) { continue; }

                // Now schedule excecution of the new msg check
                mgr.scheduleWork(ma, WorkManager.INDEFINITE, null, this);
            } catch (InterruptedException e) {
                log.debug("Interrupted waiting for new msg check. NewMsgsWorker will stop checking for new messages.");
                Thread.currentThread().interrupt();
            } catch (WorkException e) {
                log.warn("Failed to schedule new msg check", e);
            }
        }

        log.tracef("End run");
    }

    /**
     * Work accepted
     *
     * @param e The event
     */
    public void workAccepted(WorkEvent e) {
        log.tracef("workAccepted: e=%s", e);
    }

    /**
     * Work rejected
     *
     * @param e The event
     */
    public void workRejected(WorkEvent e) {
        log.tracef("workRejected: e=%s", e);
    }

    /**
     * Work started
     *
     * @param e The event
     */
    public void workStarted(WorkEvent e) {
        log.tracef("workStarted: e=%s", e);
    }

    /**
     * Work completed
     *
     * @param e The event
     */
    public void workCompleted(WorkEvent e) {
        log.tracef("workCompleted: e=%s", e);

        MailActivation activation = (MailActivation) e.getWork();
        try {
            activation.unrelease();
            watch(activation);
        } catch (InterruptedException ex) {
            log.warn("Failed to reschedule new msg check", ex);
        }
    }
}
