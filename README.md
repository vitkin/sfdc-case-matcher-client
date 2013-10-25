# SFDC Case Matcher Client

Client JAR for the SFDC Case Matcher Server Web Service. 

## Downloading the WSDL automatically
The WSDL file can be automatically downloaded by using the profile
`download-wsdl`.  
The existing WSDL file will be overwritten by the downloaded one.

To use that profile make sure you've created a `build.properties` file located
under the base directory.  
That `build.properties` has to contain at least your login and password as in
the hereunder example:
```INI
sfdc.username = user.name@domain.tld
sfdc.password = 123456
```

If you want to download the WSDL file from the sandbox environment
(`https://test.salesforce.com`), then also add the property:
```INI
sfdc.useSandbox = true
```

## Write Application Code
The following sample illustrates creating a Partner Web Service connection and
retrieving the session id from that connection and using it in a new Case
Matcher Web Service connection without the need to provide a second time the
username and password.   
Logging in is automatically handled by the Connector.

```java
import com.sforce.soap.CaseMatcher.wsc.*;
import com.sforce.soap.partner.wsc.HandledEmailMessage;
import com.sforce.ws.*;

public class Main
{
	private static final String REGEX_TOO_COMPLICATED = "Regex too complicated";

	public static void main(String args) throws ConnectionException
	{
		// Emails for which we are looking for cases (Not filled in that sample)
		HandledEmailMessage[] handledEmailMessages = new HandledEmailMessage[10];

		// Connecting using the Partner Web Service API -- Note that instead we can
		// connect directly using the Connector from the Case Matcher Web Service
		// API
		ConnectorConfig partnerConfig = new ConnectorConfig();
		partnerConfig.setUsername("username");
		partnerConfig.setPassword("password");

		com.sforce.soap.partner.wsc.PartnerConnection connection 
			= com.sforce.soap.partner.wsc.Connector.newConnection(partnerConfig);

		// Include the session id obtained from the partner API -- Note that instead
		// we could have connected directly using the Connector from the Case
		// Matcher API
		ConnectorConfig config = new ConnectorConfig();
		config.setSessionId(partnerConfig.getSessionId());

		// Retrieve the Service Endpoint -- Note that this is required if the URL
		// of the resource server doesn't match the one specified in the WSDL.
		// Doing so permits to use the same resource server than the one used to
		// connect to the Partner API
		String endpoint = config.getServiceEndpoint();
		endpoint = endpoint.split("/Soap/", 2)[0] + "/Soap/class/CaseMatcher";
		config.setAuthEndpoint(endpoint);
		config.setServiceEndpoint(endpoint);

		// Create a new connection to the Case Matcher Web Service
		SoapConnection conn = Connector.newConnection(config);

		final Email[] emails = new Email[handledEmailMessages.length];
		String[] caseThreadIds = new String[handledEmailMessages.length];

		// Convert Partner API based emails to Case Matcher API based ones
		for (int i = 0; i < handledEmailMessages.length; i++)
		{
			final Email email = new Email();
			email.setHtmlBody(handledEmailMessages[i].getHtmlBody());
			email.setSubject(handledEmailMessages[i].getSubject());
			email.setTextBody(handledEmailMessages[i].getTextBody());

			emails[i] = email;
		}

		try
		{
			// Try to filter all e-mails at once
			caseThreadIds = conn.findCaseThreadIds(emails);
		}
		catch (ConnectionException ex1)
		{
			if (ex1.getMessage().contains(REGEX_TOO_COMPLICATED))
			{
				// In case of failure, try to filter e-mails one by one
				for (int i = 0; i < handledEmailMessages.length; i++)
				{
					try
					{
						caseThreadIds[i] = conn.findCaseThreadIds(new Email[]
						{emails[i]})[0];
					}
					catch (ConnectionException ex2)
					{
						// In case of error related to complicated regex, skip the e-mail
						if (ex2.getMessage().contains(REGEX_TOO_COMPLICATED))
						{
							caseThreadIds[i] = "";
						}
						else
						{
							throw ex2;
						}
					}
				}
			}
			else
			{
				throw ex1;
			}
		}

		// Append the Salesforce case thread id to the end of the e-mail body
		for (int i = 0; i < handledEmailMessages.length; i++)
		{
			if (!caseThreadIds[i].isEmpty())
			{
				if (!isBlank(handledEmailMessages[i].getTextBody()) || 
						(isBlank(handledEmailMessages[i].getHtmlBody())))
				{
					handledEmailMessages[i].setTextBody(handledEmailMessages[i].getTextBody()
						+ "\n\n" + caseThreadIds[i]);
				}
				if (!isBlank(handledEmailMessages[i].getHtmlBody()) || 
						(isBlank(handledEmailMessages[i].getTextBody())))
				{
					handledEmailMessages[i].setHtmlBody(handledEmailMessages[i].getHtmlBody()
						+ "<br><br>" + caseThreadIds[i]);
				}
			}
		}
	}

	private static boolean isBlank(String s)
	{
		return s == null || s.trim().length() == 0;
	}
}
```
