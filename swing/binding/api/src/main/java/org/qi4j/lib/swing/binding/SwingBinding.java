/*
 * Copyright 2008 Niclas Hedhman. All rights Reserved.
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
package org.qi4j.lib.swing.binding;

import javax.swing.JComponent;
import org.qi4j.library.constraints.annotation.NotNull;

public interface SwingBinding<T>
{

    /**
     * Bind the specific component to this binding.
     *
     * @param aComponent The component. This argument must not be {@code null}.
     * @return this.
     * @throws IllegalBindingException Thrown if adapter for the specified {@code aComponent} is not found.
     */
    SwingBinding<T> to( @NotNull JComponent aComponent )
        throws IllegalBindingException;

    /**
     * @return The state model for this binding.
     */
    StateModel<T> stateModel();
}
