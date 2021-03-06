/*
 * Copyright (c) 2010, Rickard Öberg. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.qi4j.library.jmx;

import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.service.Activatable;
import org.qi4j.api.service.ImportedServiceDescriptor;
import org.qi4j.api.service.ServiceComposite;
import org.qi4j.api.service.ServiceReference;
import org.qi4j.api.service.qualifier.ServiceQualifier;
import org.qi4j.api.structure.Module;
import org.qi4j.api.util.Iterables;
import org.qi4j.spi.entity.EntityDescriptor;
import org.qi4j.spi.service.ServiceDescriptor;
import org.qi4j.spi.structure.*;

import javax.management.MBeanOperationInfo;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.modelmbean.ModelMBeanOperationInfo;
import javax.management.modelmbean.RequiredModelMBean;
import java.util.ArrayList;
import java.util.List;

/**
 * Expose the Qi4j app as a "tree" of MBeans.
 *
 * Other services should reuse the object names and create
 * nodes under the ones created here. For example:
 * MyApp:layer=Application,Module=MyModule,class=Service,service=MyService
 * is exported by this service, so another exporter showing some aspect related to this service should
 * use this as base for the ObjectName, and add their own properties. Example:
 * MyApp:layer=Application,Module=MyModule,class=Service,service=MyService,name=Configuration
 *
 * Use the following snippet to find the ObjectName of a service with a given identity:
 * ObjectName serviceName = Qi4jMBeans.findService(mbeanServer, applicationName, serviceId);
 */
@Mixins(ApplicationManagerService.Mixin.class)
public interface ApplicationManagerService
    extends ServiceComposite, Activatable
{
    class Mixin
        implements Activatable
    {
        @Service
        public MBeanServer server;

        @Structure
        public ApplicationSPI application;

        private List<ObjectName> mbeans = new ArrayList<ObjectName>( );

        public void activate() throws Exception
        {
            application.visitDescriptor( new DescriptorVisitor<Exception>()
            {
                LayerSPI layer;
                ModuleSPI module;

                @Override
                public void visit( ApplicationDescriptor applicationDescriptor ) throws Exception
                {
                }

                @Override
                public void visit( LayerDescriptor layerDescriptor ) throws Exception
                {
                    layer = (LayerSPI) application.findLayer( layerDescriptor.name() );

                    LayerBean layerBean = new LayerBean(layer, layerDescriptor);
                    ObjectName objectName = new ObjectName( application.name()+":layer="+layer.name() );
                    RequiredModelMBean mbean = new ModelMBeanBuilder( objectName, layerDescriptor.name(), LayerBean.class.getName()).
                            attribute( "uses", "Layer usages", String.class.getName(), "Other layers that this layer uses", "getUses", null ).
                            operation( "restart", "Restart layer", String.class.getName(), MBeanOperationInfo.ACTION_INFO ).
                            newModelMBean();

                    mbean.setManagedResource( layerBean, "ObjectReference" );

                    server.registerMBean( mbean, objectName );
                    mbeans.add( objectName );
                }

                @Override
                public void visit( ModuleDescriptor moduleDescriptor ) throws Exception
                {
                    module = (ModuleSPI) application.findModule( layer.name(), moduleDescriptor.name() );
                    ObjectName objectName = new ObjectName( application.name()+":layer="+layer.name()+",module="+moduleDescriptor.name() );
                    RequiredModelMBean mbean = new ModelMBeanBuilder( objectName, moduleDescriptor.name(), moduleDescriptor.getClass().getName()).
                            attribute( "name", "Module name", String.class.getName(), "Name of module", "name", null ).
                            newModelMBean();

                    mbean.setManagedResource( moduleDescriptor, "ObjectReference" );

                    server.registerMBean( mbean, objectName );
                    mbeans.add( objectName );
                }

                @Override
                public void visit( ServiceDescriptor serviceDescriptor ) throws Exception
                {
                    ObjectName objectName = new ObjectName( application.name()+":layer="+layer.name()+",module="+module.name()+",class=Service,service="+serviceDescriptor.identity() );
                    RequiredModelMBean mbean = new ModelMBeanBuilder( objectName, serviceDescriptor.identity(), ServiceBean.class.getName()).
                            attribute( "Id", "Service id", String.class.getName(), "Id of service", "getId", null ).
                            attribute( "Visibility", "Service visibility", String.class.getName(), "Visibility of service", "getVisibility", null ).
                            operation( "restart", "Restart service", String.class.getName(), ModelMBeanOperationInfo.ACTION_INFO).
                            newModelMBean();

                    mbean.setManagedResource( new ServiceBean(serviceDescriptor, module), "ObjectReference" );

                    server.registerMBean( mbean, objectName );
                    mbeans.add( objectName );
                }

                @Override
                public void visit( ImportedServiceDescriptor importedServiceDescriptor ) throws Exception
                {
                    ObjectName objectName = new ObjectName( application.name()+":layer="+layer.name()+",module="+module.name()+",class=Imported service,importedservice="+importedServiceDescriptor.identity() );
                    RequiredModelMBean mbean = new ModelMBeanBuilder( objectName, importedServiceDescriptor.identity(), ImportedServiceBean.class.getName()).
                            attribute( "Id", "Service id", String.class.getName(), "Id of service", "getId", null ).
                            attribute( "Visibility", "Service visibility", String.class.getName(), "Visibility of service", "getVisibility", null ).
                            newModelMBean();

                    mbean.setManagedResource( new ImportedServiceBean(importedServiceDescriptor, module), "ObjectReference" );

                    server.registerMBean( mbean, objectName );
                    mbeans.add( objectName );
                }

                @Override
                public void visit( EntityDescriptor entityDescriptor ) throws Exception
                {
                    ObjectName objectName = new ObjectName( application.name()+":layer="+layer.name()+",module="+module.name()+",class=Entity,entity="+entityDescriptor.entityType().type().name() );
                    RequiredModelMBean mbean = new ModelMBeanBuilder( objectName, entityDescriptor.entityType().type().name(), EntityBean.class.getName()).
                            attribute( "Type", "Entity type", String.class.getName(), "Type of entity", "getType", null ).
                            newModelMBean();

                    mbean.setManagedResource( new EntityBean(entityDescriptor, module), "ObjectReference" );

                    server.registerMBean( mbean, objectName );
                    mbeans.add( objectName );
                }

                
            });
        }

        public void passivate() throws Exception
        {
            for (ObjectName mbean : mbeans)
            {
                server.unregisterMBean( mbean );
            }
        }
    }

    public static class LayerBean
    {
        private final LayerSPI layer;
        private final LayerDescriptor layerDescriptor;
        private String uses;

        public LayerBean( LayerSPI layer, LayerDescriptor layerDescriptor)
        {
            this.layer = layer;
            this.layerDescriptor = layerDescriptor;

            uses = "Uses: ";
            for (LayerDescriptor usedLayer : layerDescriptor.usedLayers().layers())
            {
                uses +=usedLayer.name()+" ";
            }
        }

        public String getUses()
        {
            return uses;
        }

        public String restart() throws Exception
        {
            try
            {
                layer.passivate();
                layer.activate();
                return "Restarted layer";
            } catch (Exception e)
            {
                return "Could not restart layer:"+e.getMessage();
            }
        }
    }

    public static class ServiceBean
    {
        private final ServiceDescriptor serviceDescriptor;
        private final Module module;

        public ServiceBean( ServiceDescriptor serviceDescriptor, Module module )
        {
            this.serviceDescriptor = serviceDescriptor;
            this.module = module;
        }

        public String getId()
        {
            return serviceDescriptor.identity();
        }

        public String getVisibility()
        {
            return serviceDescriptor.visibility().name();
        }

        public String restart()
        {
            Iterable services = module.serviceFinder().findServices( Activatable.class );
            ServiceReference<Activatable> serviceRef = (ServiceReference<Activatable>) Iterables.first(Iterables.filter( ServiceQualifier.withId( serviceDescriptor.identity() ), services ));
            if (serviceRef != null)
            {
                try
                {
                    ((Activatable)serviceRef).passivate();
                    ((Activatable)serviceRef).activate();
                    return "Restarted service";
                } catch (Exception e)
                {
                    return "Could not restart service:"+e.getMessage();
                }
            } else
                return "Could not find service";
        }
    }

    public static class ImportedServiceBean
    {
        private final ImportedServiceDescriptor serviceDescriptor;
        private final Module module;

        public ImportedServiceBean( ImportedServiceDescriptor serviceDescriptor, Module module )
        {
            this.serviceDescriptor = serviceDescriptor;
            this.module = module;
        }

        public String getId()
        {
            return serviceDescriptor.identity();
        }

        public String getVisibility()
        {
            return serviceDescriptor.visibility().name();
        }
    }

    public static class EntityBean
    {
        private final EntityDescriptor entityDescriptor;
        private final Module module;

        public EntityBean( EntityDescriptor entityDescriptor, Module module )
        {
            this.entityDescriptor = entityDescriptor;
            this.module = module;
        }

        public String getType()
        {
            return entityDescriptor.entityType().type().name();
        }

        public String getVisibility()
        {
            return entityDescriptor.visibility().name();
        }
    }

}
