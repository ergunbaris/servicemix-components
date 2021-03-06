/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.servicemix.eip.patterns;

import javax.jbi.management.DeploymentException;
import javax.jbi.messaging.ExchangeStatus;
import javax.jbi.messaging.Fault;
import javax.jbi.messaging.InOnly;
import javax.jbi.messaging.MessageExchange;
import javax.jbi.messaging.NormalizedMessage;
import javax.jbi.messaging.RobustInOnly;

import org.apache.servicemix.common.util.MessageUtil;
import org.apache.servicemix.eip.EIPEndpoint;
import org.apache.servicemix.eip.support.ExchangeTarget;
import org.apache.servicemix.eip.support.Predicate;

/**
 * MessageFilter allows filtering incoming JBI exchanges.
 * This component implements the  
 * <a href="http://www.enterpriseintegrationpatterns.com/Filter.html">Message Filter</a> 
 * pattern.
 *  
 * @author gnodet
 * @version $Revision: 376451 $
 * @org.apache.xbean.XBean element="message-filter"
 */
public class MessageFilter extends EIPEndpoint {

    /**
     * The main target destination which will receive the exchange
     */
    private ExchangeTarget target;
    /**
     * The filter to use on incoming messages
     */
    private Predicate filter;
    /**
     * The correlation property used by this component
     */
    //private String correlation;
    /**
     * Indicates if faults and errors from recipients should be sent
     * back to the consumer.  In such a case, only the first fault or
     * error received will be reported.
     * Note that if the consumer is synchronous, it will be blocked
     * until all recipients successfully acked the exchange, or
     * a fault or error is reported, and the exchange will be kept in the
     * store for recovery. 
     */
    private boolean reportErrors;
    
    /**
     * @return Returns the target.
     */
    public ExchangeTarget getTarget() {
        return target;
    }

    /**
     * The main target destination which will receive the exchange
     * @param target The target to set.
     */
    public void setTarget(ExchangeTarget target) {
        this.target = target;
    }

    /**
     * @return Returns the filter.
     */
    public Predicate getFilter() {
        return filter;
    }

    /**
     * The filter to use on incoming messages
     * @param filter The filter to set.
     */
    public void setFilter(Predicate filter) {
        this.filter = filter;
    }

    /**
     * @return Returns the reportErrors.
     */
    public boolean isReportErrors() {
        return reportErrors;
    }

    /**
     * Indicates if faults and errors from recipients should be sent
     * back to the consumer.  In such a case, only the first fault or
     * error received will be reported.
     * Note that if the consumer is synchronous, it will be blocked
     * until all recipients successfully acked the exchange, or
     * a fault or error is reported, and the exchange will be kept in the
     * store for recovery.
     *  
     * @param reportErrors The reportErrors to set.
     */
    public void setReportErrors(boolean reportErrors) {
        this.reportErrors = reportErrors;
    }

    /* (non-Javadoc)
     * @see org.apache.servicemix.eip.EIPEndpoint#validate()
     */
    public void validate() throws DeploymentException {
        super.validate();
        // Check target
        if (target == null) {
            throw new IllegalArgumentException("target should be set to a valid ExchangeTarget");
        }
        // Check filter
        if (filter == null) {
            throw new IllegalArgumentException("filter property should be set");
        }
        // Create correlation property
        //correlation = "MessageFilter.Correlation." + getService() + "." + getEndpoint();
    }

    /* (non-Javadoc)
     * @see org.apache.servicemix.eip.EIPEndpoint#processSync(javax.jbi.messaging.MessageExchange)
     */
    protected void processSync(MessageExchange exchange) throws Exception {
        if (!(exchange instanceof InOnly)
            && !(exchange instanceof RobustInOnly)) {
            fail(exchange, new UnsupportedOperationException("Use an InOnly or RobustInOnly MEP"));
        } else {
            NormalizedMessage in = MessageUtil.copyIn(exchange);
            MessageExchange me = getExchangeFactory().createExchange(exchange.getPattern());
            target.configureTarget(me, getContext());
            MessageUtil.transferToIn(in, me);
            if (filter.matches(me)) {
                sendSync(me);
                if (me.getStatus() == ExchangeStatus.ERROR && reportErrors) {
                    fail(exchange, me.getError());
                } else if (me.getStatus() == ExchangeStatus.DONE) {
                    done(exchange);
                } else if (me.getFault() != null && reportErrors) {
                    Fault fault = MessageUtil.copyFault(me);
                    done(me);
                    MessageUtil.transferToFault(fault, exchange);
                    sendSync(exchange);
                }
            } else {
                done(exchange);
            }
        }
    }

    /* (non-Javadoc)
     * @see org.apache.servicemix.eip.EIPEndpoint#processAsync(javax.jbi.messaging.MessageExchange)
     */
    protected void processAsync(MessageExchange exchange) throws Exception {
        // If we need to report errors, the behavior is really different,
        // as we need to keep the incoming exchange in the store until
        // all acks have been received
        if (reportErrors) {
            // TODO: implement this
            throw new UnsupportedOperationException("Not implemented");
        // We are in a simple fire-and-forget behaviour.
        // This implementation is really efficient as we do not use
        // the store at all.
        } else {
            if (exchange.getStatus() == ExchangeStatus.DONE) {
                return;
            } else if (exchange.getStatus() == ExchangeStatus.ERROR) {
                return;
            } else if (!(exchange instanceof InOnly)
                       && !(exchange instanceof RobustInOnly)) {
                fail(exchange, new UnsupportedOperationException("Use an InOnly or RobustInOnly MEP"));
            } else if (exchange.getFault() != null) {
                done(exchange);
            } else {
                NormalizedMessage in = MessageUtil.copyIn(exchange);
                MessageExchange me = getExchangeFactory().createExchange(exchange.getPattern());
                target.configureTarget(me, getContext());
                MessageUtil.transferToIn(in, me);
                if (filter.matches(me)) {
                    send(me);
                }
                done(exchange);
            }
        }
    }

}
