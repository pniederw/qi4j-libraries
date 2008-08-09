/*
 * Copyright (c) 2008, Rickard Öberg. All Rights Reserved.
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

package org.qi4j.library.rdf.entity;

import java.util.Collections;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import org.junit.Before;
import org.junit.Test;
import org.openrdf.model.Statement;
import org.openrdf.rio.RDFHandlerException;
import org.qi4j.bootstrap.AssemblyException;
import org.qi4j.bootstrap.ModuleAssembly;
import org.qi4j.entity.EntityBuilder;
import org.qi4j.entity.EntityComposite;
import org.qi4j.entity.UnitOfWork;
import org.qi4j.entity.association.Association;
import org.qi4j.entity.memory.MemoryEntityStoreService;
import org.qi4j.injection.scope.Service;
import org.qi4j.library.constraints.annotation.NotEmpty;
import org.qi4j.property.Property;
import org.qi4j.spi.entity.EntityState;
import org.qi4j.spi.entity.EntityStore;
import org.qi4j.spi.entity.QualifiedIdentity;
import org.qi4j.test.AbstractQi4jTest;

/**
 * TODO
 */
public class EntityParserTest
    extends AbstractQi4jTest
{
    @Service EntityStore entityStore;
    @Service EntitySerializer serializer;
    @Service EntityParser parser;

    public void assemble( ModuleAssembly module ) throws AssemblyException
    {
        module.addServices( MemoryEntityStoreService.class, EntitySerializerService.class, EntityParserService.class );
        module.addEntities( TestEntity.class );
        module.addObjects( EntityParserTest.class );
    }

    @Override @Before public void setUp() throws Exception
    {
        super.setUp();

        createDummyData();
    }

    @Test
    public void testEntityParser() throws RDFHandlerException
    {
        objectBuilderFactory.newObjectBuilder( EntityParserTest.class ).injectTo( this );

        QualifiedIdentity qualifiedIdentity = new QualifiedIdentity( "test1", TestEntity.class );
        EntityState entityState = entityStore.getEntityState( qualifiedIdentity );

        Iterable<Statement> graph = serializer.serialize( entityState );

        parser.parse( graph, entityState );

        entityStore.prepare( Collections.EMPTY_LIST, Collections.singleton( entityState ), Collections.EMPTY_LIST ).commit();

        UnitOfWork unitOfWork = unitOfWorkFactory.newUnitOfWork();
        try
        {
            TestEntity entity = unitOfWork.find( "test1", TestEntity.class );
            TestEntity entity2 = unitOfWork.find( "test2", TestEntity.class );
            assertThat( "values are ok", entity2.name().get(), equalTo( "Niclas" ) );
            assertThat( "values are ok", entity2.association().get(), equalTo( entity ) );

            unitOfWork.complete();
        }
        catch( Exception e )
        {
            unitOfWork.discard();
        }

    }

    void createDummyData()
    {
        UnitOfWork unitOfWork = unitOfWorkFactory.newUnitOfWork();
        try
        {
            EntityBuilder<TestEntity> builder = unitOfWork.newEntityBuilder( "test1", TestEntity.class );
            builder.stateOfComposite().name().set( "Rickard" );
            TestEntity testEntity = builder.newInstance();

            EntityBuilder<TestEntity> builder2 = unitOfWork.newEntityBuilder( "test2", TestEntity.class );
            builder2.stateOfComposite().name().set( "Niclas" );
            builder2.stateOfComposite().association().set( testEntity );
            TestEntity testEntity2 = builder2.newInstance();
            unitOfWork.complete();
        }
        catch( Exception e )
        {
            unitOfWork.discard();
        }

    }

    public interface TestEntity
        extends EntityComposite
    {
        @NotEmpty Property<String> name();

        Association<TestEntity> association();
    }
}