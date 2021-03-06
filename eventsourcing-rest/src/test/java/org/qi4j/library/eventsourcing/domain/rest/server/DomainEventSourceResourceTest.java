/*
 * Copyright (c) 2011, Rickard Öberg. All Rights Reserved.
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

package org.qi4j.library.eventsourcing.domain.rest.server;

import org.junit.Ignore;
import org.qi4j.api.common.UseDefaults;
import org.qi4j.api.entity.EntityComposite;
import org.qi4j.api.injection.scope.Service;
import org.qi4j.api.injection.scope.Structure;
import org.qi4j.api.mixin.Mixins;
import org.qi4j.api.property.Property;
import org.qi4j.api.unitofwork.UnitOfWork;
import org.qi4j.api.unitofwork.UnitOfWorkCompletionException;
import org.qi4j.api.unitofwork.UnitOfWorkFactory;
import org.qi4j.api.usecase.UsecaseBuilder;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ImportedServiceDeclaration;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.bootstrap.SingletonAssembler;
import org.qi4j.library.eventsourcing.domain.api.DomainEvent;
import org.qi4j.library.eventsourcing.domain.api.DomainEventValue;
import org.qi4j.library.eventsourcing.domain.api.UnitOfWorkDomainEventsValue;
import org.qi4j.library.eventsourcing.domain.factory.CurrentUserUoWPrincipal;
import org.qi4j.library.eventsourcing.domain.factory.DomainEventCreationConcern;
import org.qi4j.library.eventsourcing.domain.factory.DomainEventFactoryService;
import org.qi4j.library.eventsourcing.domain.source.EventSource;
import org.qi4j.library.eventsourcing.domain.source.memory.MemoryEventStoreService;
import org.qi4j.test.EntityTestAssembler;
import org.restlet.*;
import org.restlet.data.Protocol;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;

import java.security.Principal;

import static org.qi4j.api.service.qualifier.ServiceTags.tags;

/**
 * Start simple web server that exposes the Restlet resource. Test through browser.
 */
@Ignore
public class DomainEventSourceResourceTest
{
    public static void main( String[] args ) throws Exception
    {
        Component component = new Component();
        component.getServers().add( Protocol.HTTP, 8080 );

        SingletonAssembler assembler = new SingletonAssembler()
        {
            public void assemble( ModuleAssembly module ) throws AssemblyException
            {
                new EntityTestAssembler().assemble( module );

                module.addValues( DomainEventValue.class, UnitOfWorkDomainEventsValue.class );
                module.addServices( MemoryEventStoreService.class ).setMetaInfo( tags( "domain" ) );
                module.addServices( DomainEventFactoryService.class );
                module.importServices( CurrentUserUoWPrincipal.class ).importedBy( ImportedServiceDeclaration.NEW_OBJECT );
                module.addObjects( CurrentUserUoWPrincipal.class );

                module.addObjects( DomainEventSourceResource.class, PingResource.class );

                module.addEntities( TestEntity.class ).withConcerns( DomainEventCreationConcern.class );
            }
        };

        component.getDefaultHost().attach( "/events", new TestApplication( assembler ) );
        component.getDefaultHost().attach( "/ping", assembler.objectBuilderFactory().newObject( PingResource.class ) );
        component.start();

        generateTestData(assembler.unitOfWorkFactory());
    }

    private static void generateTestData(UnitOfWorkFactory unitOfWorkFactory) throws UnitOfWorkCompletionException
    {
        // Set principal for the UoW
        Principal administratorPrincipal = new Principal()
        {
            public String getName()
            {
                return "administrator";
            }
        };

        // Perform UoW with usecase defined
        for (int i = 0; i < 43; i++)
        {
            UnitOfWork uow = unitOfWorkFactory.newUnitOfWork( UsecaseBuilder.newUsecase( "Change description "+(i+1) ));
            uow.metaInfo().set( administratorPrincipal );

            TestEntity entity = uow.newEntity( TestEntity.class );
            entity.changedDescription( "New description" );
            uow.complete();
        }
    }

    static class TestApplication
        extends Application
    {
        private final SingletonAssembler assembler;

        TestApplication(SingletonAssembler assembler)
        {
            this.assembler = assembler;
        }

        @Override
        public Restlet createInboundRoot()
        {
            getTunnelService().setExtensionsTunnel( true );
            return assembler.objectBuilderFactory().newObject(DomainEventSourceResource.class  );
        }
    }


    @Mixins(TestEntity.Mixin.class)
    public interface TestEntity
            extends EntityComposite
    {
        @UseDefaults
        Property<String> description();

        @DomainEvent
        void changedDescription( String newName );

        abstract class Mixin
                implements TestEntity
        {
            public void changedDescription( String newName )
            {
                description().set( newName );
            }
        }
    }

    // Used to create more events
    public static class PingResource
        extends Restlet
    {
        @Structure
        UnitOfWorkFactory unitOfWorkFactory;

        @Service
        EventSource eventSource;

        @Override
        public void handle( Request request, Response response )
        {
            // Set principal for the UoW
            Principal administratorPrincipal = new Principal()
            {
                public String getName()
                {
                    return "administrator";
                }
            };

            // Perform UoW with usecase defined
            try
            {
                UnitOfWork uow = unitOfWorkFactory.newUnitOfWork( UsecaseBuilder.newUsecase( "Change description "+(eventSource.count()) ));
                uow.metaInfo().set( administratorPrincipal );

                TestEntity entity = uow.newEntity( TestEntity.class );
                entity.changedDescription( "New description" );
                uow.complete();

                response.setStatus( Status.SUCCESS_NO_CONTENT );
            } catch (UnitOfWorkCompletionException e)
            {
                throw new ResourceException(e);
            }
        }
    }
}
