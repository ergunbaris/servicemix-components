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
package org.apache.servicemix.cxfse;

import java.io.File;
import java.net.URL;
import java.util.logging.Logger;

import javax.jbi.messaging.InOnly;
import javax.jbi.messaging.InOut;
import javax.naming.InitialContext;
import javax.xml.namespace.QName;

import junit.framework.TestCase;

import org.apache.cxf.common.logging.LogUtils;
import org.apache.cxf.transport.ConduitInitiatorManager;
import org.apache.cxf.transport.jbi.JBITransportFactory;
import org.apache.servicemix.client.DefaultServiceMixClient;
import org.apache.servicemix.jbi.container.JBIContainer;
import org.apache.servicemix.jbi.jaxp.SourceTransformer;
import org.apache.servicemix.jbi.jaxp.StringSource;


public class CxfSeClientProxyTest extends TestCase {

    private static final Logger LOG = LogUtils.getL7dLogger(CxfSeClientProxyTest.class);
    private DefaultServiceMixClient client;
    private InOut io;
    private InOnly inOnly;
    private JBIContainer container;
    
    protected void setUp() throws Exception {
        container = new JBIContainer();
        container.setUseMBeanServer(false);
        container.setCreateMBeanServer(false);
        container.setMonitorInstallationDirectory(false);
        container.setNamingContext(new InitialContext());
        container.setEmbedded(true);
        container.init();
        
    }
    
    public void testClientProxy() throws Exception {
        
        CxfSeComponent component = new CxfSeComponent();
        container.activateComponent(component, "CxfSeComponent");

        // Start container
        container.start();
        
        // Deploy SU
        component.getServiceUnitManager().deploy("target", getServiceUnitPath("proxytarget"));
        component.getServiceUnitManager().init("target", getServiceUnitPath("proxytarget"));
        component.getServiceUnitManager().start("target");
              
        component.getServiceUnitManager().deploy("proxy", getServiceUnitPath("proxy"));
        component.getServiceUnitManager().init("proxy", getServiceUnitPath("proxy"));
        component.getServiceUnitManager().start("proxy");
        
        //test redepoly su
        component.getServiceUnitManager().stop("target");
        component.getServiceUnitManager().shutDown("target");
        component.getServiceUnitManager().undeploy("target", getServiceUnitPath("proxytarget"));
        
        JBITransportFactory jbiTransportFactory = (JBITransportFactory) component.getBus()
            .getExtension(ConduitInitiatorManager.class)
            .getConduitInitiator(JBITransportFactory.TRANSPORT_ID);
        assertNull(jbiTransportFactory.getDeliveryChannel());
        
        
        component.getServiceUnitManager().init("target", getServiceUnitPath("proxytarget"));
        component.getServiceUnitManager().start("target");
        
        client = new DefaultServiceMixClient(container);
        io = client.createInOutExchange();
        io.setService(new QName("http://apache.org/hello_world_soap_http", "SOAPService"));
        io.setInterfaceName(new QName("http://apache.org/hello_world_soap_http", "Greeter"));
        io.setOperation(new QName("http://apache.org/hello_world_soap_http", "greetMe"));
        
        LOG.info("test clientProxy");
        io.getInMessage().setContent(new StringSource(
                "<message xmlns='http://java.sun.com/xml/ns/jbi/wsdl-11-wrapper'>"
              + "<part> "
              + "<greetMe xmlns='http://apache.org/hello_world_soap_http/types'><requestType>"
              + "ffang"
              + "</requestType></greetMe>"
              + "</part> "
              + "</message>"));
        client.sendSync(io);
        assertTrue(new SourceTransformer().contentToString(
                io.getOutMessage()).indexOf("Hello ffang 3") > 0);
        client.done(io);
        
        //      test restart component
        component.getServiceUnitManager().stop("target");
        component.getServiceUnitManager().shutDown("target");
        component.getServiceUnitManager().undeploy("target", getServiceUnitPath("proxytarget"));
        component.stop();
        
        component.start();
        component.getServiceUnitManager().init("target", getServiceUnitPath("proxytarget"));
        component.getServiceUnitManager().start("target");
        
        client = new DefaultServiceMixClient(container);
        io = client.createInOutExchange();
        io.setService(new QName("http://apache.org/hello_world_soap_http", "SOAPService"));
        io.setInterfaceName(new QName("http://apache.org/hello_world_soap_http", "Greeter"));
        io.setOperation(new QName("http://apache.org/hello_world_soap_http", "greetMe"));
        LOG.info("test clientProxy");
        io.getInMessage().setContent(new StringSource(
                "<message xmlns='http://java.sun.com/xml/ns/jbi/wsdl-11-wrapper'>"
              + "<part> "
              + "<greetMe xmlns='http://apache.org/hello_world_soap_http/types'><requestType>"
              + "fault"
              + "</requestType></greetMe>"
              + "</part> "
              + "</message>"));
        client.sendSync(io);
        assertTrue(new SourceTransformer().contentToString(
                io.getOutMessage()).indexOf("AddNumbersFault") > 0);
        client.done(io);
        
        client = new DefaultServiceMixClient(container);
        io = client.createInOutExchange();
        io.setService(new QName("http://apache.org/hello_world_soap_http", "SOAPService"));
        io.setInterfaceName(new QName("http://apache.org/hello_world_soap_http", "Greeter"));
        io.setOperation(new QName("http://apache.org/hello_world_soap_http", "greetMe"));
        LOG.info("test property get/set");
        io.getInMessage().setContent(new StringSource(
                "<message xmlns='http://java.sun.com/xml/ns/jbi/wsdl-11-wrapper'>"
              + "<part> "
              + "<greetMe xmlns='http://apache.org/hello_world_soap_http/types'><requestType>"
              + "property"
              + "</requestType></greetMe>"
              + "</part> "
              + "</message>"));
        client.sendSync(io);
        assertTrue(new SourceTransformer().contentToString(
                io.getOutMessage()).indexOf("Hello ffang") > 0);
        client.done(io);
        
        client = new DefaultServiceMixClient(container);
        inOnly = client.createInOnlyExchange();
        inOnly.setService(new QName("http://apache.org/hello_world_soap_http", "SOAPService"));
        inOnly.setInterfaceName(new QName("http://apache.org/hello_world_soap_http", "Greeter"));
        inOnly.setOperation(new QName("http://apache.org/hello_world_soap_http", "greetMeOneWay"));
        LOG.info("test clientProxy with OneWay");
        inOnly.getInMessage().setContent(new StringSource(
                "<message xmlns='http://java.sun.com/xml/ns/jbi/wsdl-11-wrapper'>"
              + "<part> "
              + "<greetMeOneWay xmlns='http://apache.org/hello_world_soap_http/types'><requestType>"
              + "oneway"
              + "</requestType></greetMeOneWay>"
              + "</part> "
              + "</message>"));
        client.sendSync(inOnly);
       
        //sleep 2sec so that done message is sent before shutdown container
        Thread.sleep(2000);
        
    }
    
    protected void tearDown() throws Exception {
        if (container != null) {
            container.shutDown();
        }
    }
    
    protected String getServiceUnitPath(String name) {
        URL url = getClass().getClassLoader().getResource("org/apache/servicemix/cxfse/" + name + "/xbean.xml");
        File path = new File(url.getFile());
        path = path.getParentFile();
        return path.getAbsolutePath();
    }
    

}
