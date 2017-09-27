/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.siddhi.extension.string;

import junit.framework.Assert;
import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.wso2.siddhi.core.ExecutionPlanRuntime;
import org.wso2.siddhi.core.SiddhiManager;
import org.wso2.siddhi.core.event.Event;
import org.wso2.siddhi.core.query.output.callback.QueryCallback;
import org.wso2.siddhi.core.stream.input.InputHandler;
import org.wso2.siddhi.core.util.EventPrinter;
import org.wso2.siddhi.extension.string.test.util.SiddhiTestHelper;
import org.wso2.siddhi.query.api.exception.ExecutionPlanValidationException;

import java.util.concurrent.atomic.AtomicInteger;

public class ContainsFunctionExtensionTestCase {

    static final Logger log = Logger.getLogger(ContainsFunctionExtensionTestCase.class);
    private AtomicInteger count = new AtomicInteger(0);
    private volatile boolean eventArrived;

    @Before
    public void init() {
        count.set(0);
        eventArrived = false;
    }

    @Test
    public void testContainsFunctionExtension() throws InterruptedException {
        log.info("ContainsFunctionExtensionTestCase TestCase");
        SiddhiManager siddhiManager = new SiddhiManager();

        String inStreamDefinition = "define stream inputStream (symbol string, price long, " +
                "volume long);";
        String query = ("@info(name = 'query1') " +
                "from inputStream " +
                "select symbol , str:contains(symbol, 'WSO2') as isContains " +
                "insert into outputStream;");
        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(inStreamDefinition +
                query);

        executionPlanRuntime.addCallback("query1", new QueryCallback() {
            @Override
            public void receive(long timeStamp, Event[] inEvents, Event[] removeEvents) {
                EventPrinter.print(timeStamp, inEvents, removeEvents);
                for (Event inEvent : inEvents) {
                    count.incrementAndGet();
                    if (count.get() == 1) {
                        Assert.assertEquals(false, inEvent.getData(1));
                    }
                    if (count.get() == 2) {
                        Assert.assertEquals(true, inEvent.getData(1));
                    }
                    if (count.get() == 3) {
                        Assert.assertEquals(true, inEvent.getData(1));
                    }
                    eventArrived = true;
                }
            }
        });

        InputHandler inputHandler = executionPlanRuntime.getInputHandler("inputStream");
        executionPlanRuntime.start();
        inputHandler.send(new Object[]{"IBM", 700f, 100l});
        inputHandler.send(new Object[]{"WSO2", 60.5f, 200l});
        inputHandler.send(new Object[]{"One of the best middleware is from WSO2.", 60.5f, 200l});
        SiddhiTestHelper.waitForEvents(100, 3, count, 60000);
        Assert.assertEquals(3, count.get());
        Assert.assertTrue(eventArrived);
        executionPlanRuntime.shutdown();
    }

    @Test (expected = ExecutionPlanValidationException.class)
    public void testContainsFunctionExtensionWithOneArgument() throws InterruptedException {
        log.info("ContainsFunctionExtensionTestCase TestCase");
        SiddhiManager siddhiManager = new SiddhiManager();

        String inStreamDefinition = "define stream inputStream (symbol string, price long, " +
                "volume long);";
        String query = ("@info(name = 'query1') " +
                "from inputStream " +
                "select symbol , str:contains(symbol) as isContains " +
                "insert into outputStream;");
        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(inStreamDefinition +
                query);
        executionPlanRuntime.start();
        executionPlanRuntime.shutdown();
    }

    @Test (expected = ExecutionPlanValidationException.class)
    public void testContainsFunctionExtensionWithInvalidDataType() throws InterruptedException {
        log.info("ContainsFunctionExtensionTestCase TestCase with invalid datatype");
        SiddhiManager siddhiManager = new SiddhiManager();

        String inStreamDefinition = "define stream inputStream (symbol string, price long, " +
                "volume long);";
        String query = ("@info(name = 'query1') " +
                "from inputStream " +
                "select symbol , str:contains(price, 'WSO2') as isContains " +
                "insert into outputStream;");
        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(inStreamDefinition +
                query);
        executionPlanRuntime.start();
        executionPlanRuntime.shutdown();
    }

    @Test (expected = ExecutionPlanValidationException.class)
    public void testContainsFunctionExtensionWithInvalidDataType1() throws InterruptedException {
        log.info("ContainsFunctionExtensionTestCase TestCase with invalid datatype");
        SiddhiManager siddhiManager = new SiddhiManager();

        String inStreamDefinition = "define stream inputStream (symbol string, price long, " +
                "volume long);";
        String query = ("@info(name = 'query1') " +
                "from inputStream " +
                "select symbol , str:contains(symbol, 123) as isContains " +
                "insert into outputStream;");
        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(inStreamDefinition +
                query);
        executionPlanRuntime.start();
        executionPlanRuntime.shutdown();
    }

    @Test
    public void testContainsFunctionExtensionWithNullValue() throws InterruptedException {
        log.info("ContainsFunctionExtensionTestCase TestCase with null value");
        SiddhiManager siddhiManager = new SiddhiManager();

        String inStreamDefinition = "define stream inputStream (symbol string, price long, " +
                "volume long);";
        String query = ("@info(name = 'query1') " +
                "from inputStream " +
                "select symbol , str:contains(symbol, 'WSO2') as isContains " +
                "insert into outputStream;");
        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(inStreamDefinition +
                query);
        InputHandler inputHandler = executionPlanRuntime.getInputHandler("inputStream");
        executionPlanRuntime.start();
        inputHandler.send(new Object[]{null, 700f, 100l, 1});
        executionPlanRuntime.shutdown();
    }

    @Test
    public void testContainsFunctionExtensionWithNullValue1() throws InterruptedException {
        log.info("ContainsFunctionExtensionTestCase TestCase with null value");
        SiddhiManager siddhiManager = new SiddhiManager();

        String inStreamDefinition = "define stream inputStream (symbol1 string, price long, " +
                "volume long, symbol2 string);";
        String query = ("@info(name = 'query1') " +
                "from inputStream " +
                "select symbol1 , str:contains(symbol1, symbol2) as isContains " +
                "insert into outputStream;");
        ExecutionPlanRuntime executionPlanRuntime = siddhiManager.createExecutionPlanRuntime(inStreamDefinition +
                query);
        InputHandler inputHandler = executionPlanRuntime.getInputHandler("inputStream");
        executionPlanRuntime.start();
        inputHandler.send(new Object[]{"IBM", 700f, 100l, null});
        executionPlanRuntime.shutdown();
    }
}
