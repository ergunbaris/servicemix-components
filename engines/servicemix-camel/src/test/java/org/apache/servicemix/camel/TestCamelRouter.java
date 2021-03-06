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
package org.apache.servicemix.camel;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.CamelTestSupport;

/**
 * @version $Revision: 563665 $
 */
public class TestCamelRouter extends CamelTestSupport {
    private MockEndpoint a;
    private MockEndpoint b;
    private MockEndpoint c;

    public void testWithOut() throws Exception {
        a.whenExchangeReceived(1, new Processor() {
            public void process(Exchange exchange) throws Exception {
                exchange.getOut().setBody("out");
            }
        });
        a.expectedMessageCount(1);
        b.expectedMessageCount(1);
        b.expectedBodiesReceived("out");
        c.expectedMessageCount(0);

        template.sendBody("direct:start", "in");

        MockEndpoint.assertIsSatisfied(a, b, c);
    }

    public void testWithFault() throws Exception {
        a.whenExchangeReceived(1, new Processor() {
            public void process(Exchange exchange) throws Exception {
                exchange.getOut().setBody("fault");
                exchange.getOut().setFault(true);
            }
        });
        a.expectedMessageCount(1);
        b.expectedMessageCount(0);
        c.expectedMessageCount(1);
        c.expectedBodiesReceived("fault");

        template.sendBody("direct:start", "in");

        //MockEndpoint.assertIsSatisfied(a, b, c);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        a = resolveMandatoryEndpoint("mock:a", MockEndpoint.class);
        b = resolveMandatoryEndpoint("mock:b", MockEndpoint.class);
        c = resolveMandatoryEndpoint("mock:c", MockEndpoint.class);
    }

    @Override
    protected RouteBuilder createRouteBuilder() throws Exception {
        return new RouteBuilder() {
            @Override
            public void configure() {
                from("direct:start")
                    .to("mock:a")
                    .choice()
                    .when(faultBody().isNull())
                    .to("mock:b")
                    .otherwise()
                    .to("mock:c");
            }
        };
    }

}