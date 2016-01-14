WildFly Mail RA 
===============

Mail inflow resource addapter for WildFly 10


Building
-------------------

Ensure you have JDK 7 (or newer) installed

> java -version

If you need Maven 3.2.5 (or newer) installed you can use it directly

> mvn install


Usage example
---------------------------


```java

....
@MessageDriven(
    activationConfig = {
        @ActivationConfigProperty(propertyName = "mailServer", propertyValue = "mail-server-host"),
        @ActivationConfigProperty(propertyName = "userName", propertyValue = "username"),
        @ActivationConfigProperty(propertyName = "password", propertyValue = "password"),
        @ActivationConfigProperty(propertyName = "storeProtocol", propertyValue = "imaps"),
        @ActivationConfigProperty(propertyName = "mailFolder", propertyValue = ""),
        @ActivationConfigProperty(propertyName = "pollingInterval", propertyValue = "5000")
    })
public class MyInboundMailMDB implements org.wildfly.mail.ra.MailListener {

    
    @Override
    public void onMessage(Message msg) {
        //process msg
    }
    
}

```

