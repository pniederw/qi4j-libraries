/*
 * Copyright (c) 2007, Sianny Halim. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.qi4j.library.general.test;

import org.qi4j.api.Composite;
import org.qi4j.api.annotation.ImplementedBy;
import org.qi4j.api.annotation.ModifiedBy;
import org.qi4j.library.framework.properties.PropertiesMixin;
import org.qi4j.library.general.model.Descriptor;
import org.qi4j.library.general.model.DescriptorMixin;
import org.qi4j.library.general.model.DescriptorModifier;
import org.qi4j.library.general.model.Name;

public class DescriptorTest extends AbstractTest
{
    public void testDescriptorAsMixin() throws Exception
    {
        DummyComposite composite = compositeFactory.newInstance( DummyComposite.class );
        composite.setName( "Sianny" );
        String displayValue = composite.getDisplayValue();
        assertEquals( displayValue, composite.getName() );
    }

    public void testDescriptorWithModifier() throws Exception {
        DummyComposite2 composite = compositeFactory.newInstance( DummyComposite2.class );
        composite.setName( "Sianny" );
        String displayValue = composite.getDisplayValue();
        assertEquals( displayValue, "My name is " + composite.getName() );
    }

    @ImplementedBy( { DescriptorMixin.class, PropertiesMixin.class } )
    private interface DummyComposite extends Descriptor, Name, Composite
    {
    }

    @ModifiedBy( { DescriptorModifier.class } )
    @ImplementedBy( { DescriptorMixin.class, PropertiesMixin.class } )
    private interface DummyComposite2 extends Descriptor, Name, Composite
    {
    }
}