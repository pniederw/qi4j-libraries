/*  Copyright 2009 Tonny Kohar.
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
package org.qi4j.library.swing.envisage.graph;

import javax.swing.JPanel;
import org.qi4j.library.swing.envisage.model.descriptor.ApplicationDetailDescriptor;
import org.qi4j.api.structure.Application;
import prefuse.data.Graph;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Robot;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseEvent;

/**
 * Just a simple wrapper for ApplicationModelDisplay
 *
 * @author Tonny Kohar (tonny.kohar@gmail.com)
 */
public class GraphPane extends JPanel
{
    private GraphDisplay display;

    protected Application application;
    protected ApplicationDetailDescriptor descriptor;

    public GraphPane()
    {
        display = new GraphDisplay();
        setBackground(display.getBackground());
        setForeground(display.getForeground());
        add(display, BorderLayout.CENTER);

        this.addComponentListener( new ComponentAdapter()
        {
            public void componentResized( ComponentEvent evt)
            {
                Component comp = evt.getComponent();
                Dimension size = comp.getSize();
                display.setSize( size.width, size.height );
            }
        });
    }

    public void initQi4J(Application application, ApplicationDetailDescriptor descriptor)
    {
        this.application = application;
        this.descriptor = descriptor;

        Graph graph = GraphBuilder.buildGraph( descriptor );
        display.run(graph);
    }

    public void refresh()
    {
        display.run();
    }

}