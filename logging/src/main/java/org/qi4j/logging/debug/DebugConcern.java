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
package org.qi4j.logging.debug;

import java.lang.reflect.Method;
import java.io.Serializable;
import org.qi4j.Qi4j;
import org.qi4j.logging.debug.service.DebuggingService;
import org.qi4j.composite.Composite;
import org.qi4j.injection.scope.Service;
import org.qi4j.injection.scope.Structure;
import org.qi4j.injection.scope.This;
import org.qi4j.property.ComputedPropertyInstance;
import org.qi4j.property.Property;

public final class DebugConcern
    implements Debug
{
    @Structure private Qi4j api;
    @Service( optional = true ) private DebuggingService loggingService;
    @This private Composite composite;

    public Integer debugLevel()
    {
        if( loggingService != null )
        {
            return loggingService.debugLevel();
        }
        return OFF;
    }

    public void debug( int priority, String message )
    {
        if( loggingService == null )
        {
            return;
        }
        if( priority >= loggingService.debugLevel() )
        {
            loggingService.debug( api.dereference( composite ), message );
        }
    }

    public void debug( int priority, String message, Serializable param1 )
    {
        if( loggingService == null )
        {
            return;
        }
        if( priority >= loggingService.debugLevel() )
        {
            loggingService.debug( api.dereference( composite ), message, param1 );
        }
    }

    public void debug( int priority, String message, Serializable param1, Serializable param2 )
    {
        if( loggingService == null )
        {
            return;
        }
        if( priority >= loggingService.debugLevel() )
        {
            loggingService.debug( api.dereference( composite ), message, param1, param2 );
        }
    }

    public void debug( int priority, String message, Serializable... params )
    {
        if( loggingService == null )
        {
            return;
        }
        if( priority >= loggingService.debugLevel() )
        {
            loggingService.debug( api.dereference( composite ), message, params );
        }
    }
}