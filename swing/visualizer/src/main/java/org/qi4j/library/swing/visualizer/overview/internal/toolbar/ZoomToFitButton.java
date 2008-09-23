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
package org.qi4j.library.swing.visualizer.overview.internal.toolbar;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.JButton;
import static org.qi4j.composite.NullArgumentException.validateNotNull;
import org.qi4j.library.swing.visualizer.overview.internal.visualization.Qi4jApplicationDisplay;

/**
 * TODO: Localization
 * TODO: icon
 *
 * @author edward.yakop@gmail.com
 * @since 0.5
 */
final class ZoomToFitButton extends JButton
{
    ZoomToFitButton( Qi4jApplicationDisplay aDisplay )
        throws IllegalArgumentException
    {
        validateNotNull( "aDisplay", aDisplay );

        addActionListener( new ZoomToFitAction( aDisplay ) );
        setText( "Zoom to fit" );
    }

    private static final class ZoomToFitAction extends AbstractAction
    {
        private static final long serialVersionUID = 1L;

        private final Qi4jApplicationDisplay display;

        private ZoomToFitAction( Qi4jApplicationDisplay aDisplay )
        {
            display = aDisplay;
        }

        public final void actionPerformed( ActionEvent e )
        {
            display.zoomToFitContainer();
        }
    }
}