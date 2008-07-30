/*
 * Copyright 2006 Niclas Hedhman.
 *
 * Licensed  under the  Apache License,  Version 2.0  (the "License");
 * you may not use  this file  except in  compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed  under the  License is distributed on an "AS IS" BASIS,
 * WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 */
package org.qi4j.log;

import java.lang.reflect.Method;
import org.qi4j.Qi4j;
import org.qi4j.composite.Composite;
import org.qi4j.injection.scope.Service;
import org.qi4j.injection.scope.Structure;
import org.qi4j.injection.scope.This;
import org.qi4j.log.service.DebuggingService;
import org.qi4j.property.ComputedProperty;
import org.qi4j.property.ComputedPropertyInstance;
import org.qi4j.property.Property;

public final class DebugConcern
    implements Debug
{
    private static final ComputedProperty<Integer> OFF_PROPERTY;

    @Structure private Qi4j api;
    @Service( optional = true ) private DebuggingService loggingService;
    @This private Composite composite;

    static
    {
        Method method;
        try
        {
            method = DebugConcern.class.getMethod( "debugLevel" );
        }
        catch( NoSuchMethodException e )
        {
            // Can not happen.
            throw new InternalError();
        }
        OFF_PROPERTY = new ComputedPropertyInstance<Integer>( method )
        {
            public Integer get()
            {
                return OFF;
            }
        };
    }

    public Property<Integer> debugLevel()
    {
        if( loggingService != null )
        {
            return loggingService.debugLevel();
        }
        return OFF_PROPERTY;
    }

    public void debug( int priority, String message )
    {
        if( loggingService == null )
        {
            return;
        }
        if( priority >= loggingService.debugLevel().get() )
        {
            loggingService.debug( api.dereference( composite ), message );
        }
    }

    public void debug( int priority, String message, Object param1 )
    {
        if( loggingService == null )
        {
            return;
        }
        if( priority >= loggingService.debugLevel().get() )
        {
            loggingService.debug( api.dereference( composite ), message, param1 );
        }
    }

    public void debug( int priority, String message, Object param1, Object param2 )
    {
        if( loggingService == null )
        {
            return;
        }
        if( priority >= loggingService.debugLevel().get() )
        {
            loggingService.debug( api.dereference( composite ), message, param1, param2 );
        }
    }

    public void debug( int priority, String message, Object... params )
    {
        if( loggingService == null )
        {
            return;
        }
        if( priority >= loggingService.debugLevel().get() )
        {
            loggingService.debug( api.dereference( composite ), message, params );
        }
    }
}