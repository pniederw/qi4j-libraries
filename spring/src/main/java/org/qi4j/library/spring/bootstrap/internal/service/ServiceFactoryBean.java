/*  Copyright 2008 Edward Yakop.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
* implied.
*
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package org.qi4j.library.spring.bootstrap.internal.service;

import org.qi4j.api.service.ServiceDescriptor;
import org.qi4j.api.service.ServiceReference;
import org.qi4j.spi.structure.ApplicationSPI;
import org.qi4j.api.structure.Application;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;
import static org.springframework.util.Assert.*;

/**
 * @author edward.yakop@gmail.com
 * @since 0.5
 */
public final class ServiceFactoryBean
    implements FactoryBean, DisposableBean
{
    private ServiceDescriptor serviceDescriptor;
    private ServiceReference serviceReference;

    public ServiceFactoryBean( Application anApplication, String aServiceId )
        throws IllegalArgumentException
    {
        notNull( anApplication, "Argument [anApplication] must not be [null]." );
        notNull( aServiceId, "Argument [aServiceId] must not be [null]." );

        ServiceLocator serviceLocator = new ServiceLocator( aServiceId );
        ApplicationSPI spi = (ApplicationSPI) anApplication;
        spi.visitDescriptor( serviceLocator );
        serviceReference = serviceLocator.locateService( anApplication );
        serviceDescriptor = serviceLocator.serviceDescriptor();

        if( serviceReference == null )
        {
            throw new IllegalArgumentException( "Qi4j service with id [" + aServiceId + "] is not found." );
        }
    }

    public final Object getObject()
        throws Exception
    {
        return serviceReference.get();
    }

    public final Class getObjectType()
    {
        return serviceDescriptor.type();
    }

    public final boolean isSingleton()
    {
        return false;
    }

    public final void destroy()
        throws Exception
    {
        if( serviceReference != null )
        {
            serviceReference.releaseService();
        }
    }
}