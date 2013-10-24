/*
 * #%L
 * SFDC Case Matcher Client
 * %%
 * Copyright (C) 2013 Victor Itkin
 * %%
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
 * #L%
 */

package com.sforce.soap.schemas._class.CaseMatcher;

import javax.xml.rpc.ServiceException;
import junit.framework.AssertionFailedError;
import junit.framework.TestCase;

public class CaseMatcherServiceTestCase extends TestCase
{

  public CaseMatcherServiceTestCase(String name)
  {
    super(name);
  }

  public void testCaseMatcherFindCaseThreadIds() throws Exception
  {
    CaseMatcherBindingStub binding;

    try
    {
      binding = (CaseMatcherBindingStub) new CaseMatcherServiceLocator().getCaseMatcher();
    }
    catch (ServiceException jre)
    {
      if (jre.getLinkedCause() != null)
      {
        jre.getLinkedCause().printStackTrace();
      }

      throw new AssertionFailedError("JAX-RPC ServiceException caught: " + jre);
    }

    assertNotNull("binding is null", binding);

    // Time out after a minute
    binding.setTimeout(60000);

    // Test operation
    //String[] value = binding.findCaseThreadIds(new Email[0]);
    // TBD - validate results
  }
}
