/**
 *
 * Copyright 2009-2010 Streamsource AB
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.qi4j.library.eventsourcing.domain.factory;

import org.qi4j.library.eventsourcing.domain.api.DomainEventValue;

import java.util.ArrayList;
import java.util.List;

/**
 * List of eventValues for the current UnitOfWork. This will be updated by the DomainEventFactory.
 */
class UnitOfWorkEvents
{
    private List<DomainEventValue> eventValues = new ArrayList<DomainEventValue>();

    public void add( DomainEventValue eventValue )
    {
        eventValues.add( eventValue );
    }

    public List<DomainEventValue> getEventValues()
    {
        return eventValues;
    }
}
