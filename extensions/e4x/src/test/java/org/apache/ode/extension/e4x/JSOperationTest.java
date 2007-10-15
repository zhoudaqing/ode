/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.ode.extension.e4x;


import org.apache.ode.test.MockExtensionContext;
import org.apache.ode.utils.DOMUtils;
import org.junit.Test;
import org.w3c.dom.Element;

/**
 * @author Tammo van Lessen (University of Stuttgart)
 */
public class JSOperationTest {

	@Test public void test() throws Exception {
		StringBuffer s = new StringBuffer();
		s.append("var request = context.readVariable('request');\n");
		s.append("request.TestPart += ' World';\n");
		s.append("context.writeVariable('request', request);\n");

		MockExtensionContext c = new MockExtensionContext();
		c.getVariables().put("request", DOMUtils.stringToDOM("<message><TestPart>Hello</TestPart></message>"));
		JSExtensionOperation jso = new JSExtensionOperation();
		Element e = DOMUtils.stringToDOM("<js:script xmlns:js=\"js\"><![CDATA[" + s + "]]></js:script>");
		jso.run(c, e);
	}
}