package uk.ac.open.kmi.forge.ptAnywhere.session.impl;

import uk.ac.open.kmi.forge.ptAnywhere.properties.PacketTracerInstanceProperties;
import uk.ac.open.kmi.forge.ptAnywhere.properties.PropertyFileManager;
import uk.ac.open.kmi.forge.ptAnywhere.session.SessionsManagerFactory;


public class SessionsManagerFactoryImpl {

    public static SessionsManagerFactory create(PropertyFileManager properties) {
        final PacketTracerInstanceProperties sharedInstance = properties.getSharedInstanceDetails();
        if (sharedInstance==null)
            return new MultipleSessionsManagerFactory(properties.getSessionHandlingDetails(),
                                                      properties.getMaximumSessionLength());
        else
            return new SharedSessionsManagerFactory(sharedInstance);
    }

}
