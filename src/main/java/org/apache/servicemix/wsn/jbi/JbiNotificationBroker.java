/*
 * Copyright 2005-2006 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.servicemix.wsn.jbi;

import javax.jbi.component.ComponentContext;

import org.apache.servicemix.wsn.jms.JmsNotificationBroker;
import org.apache.servicemix.wsn.jms.JmsPublisher;
import org.apache.servicemix.wsn.jms.JmsSubscription;

public class JbiNotificationBroker extends JmsNotificationBroker {

	private ComponentContext context;
	
	public JbiNotificationBroker(String name) {
		super(name);
	}
	
	@Override
	protected JmsSubscription createJmsSubscription(String name) {
		JbiSubscription subscription = new JbiSubscription(name);
		subscription.setContext(context);
		return subscription;
	}

	@Override
	protected JmsPublisher createJmsPublisher(String name) {
		JbiPublisher publisher = new JbiPublisher(name);
		publisher.setContext(context);
		publisher.setNotificationBrokerAddress(address);
		return publisher;
	}

	public ComponentContext getContext() {
		return context;
	}

	public void setContext(ComponentContext context) {
		this.context = context;
	}

}
