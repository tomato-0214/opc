/*
 * Copyright (c) 2019 the Eclipse Milo Authors
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */

package org.eclipse.milo.examples.client;

import org.eclipse.milo.examples.client.reader.DataTypeDictionarySessionInitializer;
import org.eclipse.milo.opcua.binaryschema.GenericBsdParser;
import org.eclipse.milo.opcua.sdk.client.OpcUaClient;
import org.eclipse.milo.opcua.stack.core.types.builtin.DataValue;
import org.eclipse.milo.opcua.stack.core.types.builtin.ExtensionObject;
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId;
import org.eclipse.milo.opcua.stack.core.types.enumerated.TimestampsToReturn;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;

/**
 * An example that shows reading the value of a node whose DataType is a custom structure type.
 * <p>
 * Requires the Unified Automation CPP Demo server be running and the endpoint URL be pointing to it.
 */
public class UnifiedAutomationReadCustomDataTypeExample implements ClientExample {

    public static void main(String[] args) throws Exception {
        UnifiedAutomationReadCustomDataTypeExample example =
            new UnifiedAutomationReadCustomDataTypeExample();

        new ClientExampleRunner(example).run();
    }

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public void run(OpcUaClient client, CompletableFuture<OpcUaClient> future) throws Exception {
        // Decoding a struct with custom DataType requires a DataTypeManager
        // that has the codec registered with it.
        // Add a SessionInitializer that will read any DataTypeDictionary
        // nodes present in the server every time the session is activated
        // and dynamically generate codecs for custom structures.
        client.addSessionInitializer(new DataTypeDictionarySessionInitializer(new GenericBsdParser()));

        client.connect().get();

        DataValue dataValue = client.readValue(
            0.0,
            TimestampsToReturn.Neither,
            NodeId.parse("ns=2;s=Demo.Static.Scalar.WorkOrder")
        ).get();

        ExtensionObject xo = (ExtensionObject) dataValue.getValue().getValue();

        Object value = xo.decode(client.getSerializationContext());

        logger.info("value: {}", value);

        future.complete(client);
    }

    @Override
    public String getEndpointUrl() {
        // Change this if UaCPPServer is running somewhere other than localhost.
        return "com.cc1500.extension.opc.tcp://localhost:48010";
    }

}
