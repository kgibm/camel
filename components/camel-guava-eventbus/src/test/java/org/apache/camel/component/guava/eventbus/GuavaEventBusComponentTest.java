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
package org.apache.camel.component.guava.eventbus;

import com.google.common.eventbus.EventBus;
import org.apache.camel.BindToRegistry;
import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.test.junit5.CamelTestSupport;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class GuavaEventBusComponentTest extends CamelTestSupport {

    @BindToRegistry("eventBus")
    EventBus eventBus = new EventBus();

    @Override
    protected RouteBuilder createRouteBuilder() {
        return new RouteBuilder() {
            @Override
            public void configure() {
                from("myGuavaEventBus:eventBus").to("mock:test");
            }
        };
    }

    @Override
    protected CamelContext createCamelContext() throws Exception {
        CamelContext camelContext = super.createCamelContext();
        GuavaEventBusComponent busComponent = new GuavaEventBusComponent();
        busComponent.setEventBus(eventBus);
        camelContext.addComponent("myGuavaEventBus", busComponent);
        return camelContext;
    }

    @Test
    public void shouldForwardMessageToCustomComponent() throws InterruptedException {
        // Given
        String message = "message";

        // When
        eventBus.post(message);

        // Then
        getMockEndpoint("mock:test").setExpectedMessageCount(1);
        assertMockEndpointsSatisfied();
        assertEquals(message, getMockEndpoint("mock:test").getExchanges().get(0).getIn().getBody());
    }

}
