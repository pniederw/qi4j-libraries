/*
 * Copyright (c) 2008, Rickard Öberg. All Rights Reserved.
 * Copyright (c) 2008, Sonny Gill. All Rights Reserved.
 * Copyright (c) 2008, Niclas Hedhman. All Rights Reserved.
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
package org.qi4j.library.framework.swing;

import java.util.Iterator;
import java.awt.event.ActionEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.AdjustmentEvent;
import java.awt.geom.Point2D;
import java.awt.geom.AffineTransform;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Color;
import java.awt.Graphics2D;
import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.AbstractAction;
import javax.swing.KeyStroke;
import javax.swing.InputMap;
import javax.swing.ActionMap;
import javax.swing.Action;
import javax.swing.JScrollBar;
import javax.swing.BoundedRangeModel;
import javax.swing.DefaultBoundedRangeModel;
import static org.qi4j.library.framework.swing.GraphConstants.FIELD_TYPE;
import org.qi4j.library.framework.swing.render.ApplicationRenderer;
import org.qi4j.library.framework.swing.render.CompositeRenderer;
import org.qi4j.library.framework.swing.render.LayerRenderer;
import org.qi4j.library.framework.swing.render.ModuleRenderer;
import org.qi4j.library.framework.swing.render.VerticalEdgeRenderer;
import prefuse.Display;
import prefuse.Visualization;
import prefuse.action.ActionList;
import prefuse.action.RepaintAction;
import prefuse.action.assignment.ColorAction;
import prefuse.controls.Control;
import prefuse.data.Graph;
import prefuse.data.Node;
import prefuse.render.DefaultRendererFactory;
import prefuse.util.ColorLib;
import prefuse.util.display.DisplayLib;
import prefuse.util.display.PaintListener;
import prefuse.visual.EdgeItem;
import prefuse.visual.VisualItem;
import prefuse.visual.sort.ItemSorter;

/**
 * TODO
 */
public class ApplicationPanel extends JPanel
{
    static final int TYPE_APPLICATION = 0;
    static final int TYPE_LAYER = 1;
    static final int TYPE_MODULE = 2;
    static final int TYPE_COMPOSITE = 3;

    static final int TYPE_EDGE_HIDDEN = 100;

    private Visualization visualization;
    private Display display;
    private VisualItem applicationNodeItem;

    private Action zoomIn;
    private Action zoomOut;
    private Control compositeSelectionControl;

    private BoundedRangeModel hBarModel = new DefaultBoundedRangeModel( 0, 5, 0, 10 );
    private BoundedRangeModel vBarModel = new DefaultBoundedRangeModel( 1, 0, 0, 1 );

    public ApplicationPanel( Graph graph, Control compositeSelectionControl )
    {
        super( new BorderLayout() );
        this.compositeSelectionControl = compositeSelectionControl;

        visualization = createVisualization( graph );
        createRenderers( visualization );
        createProcessingActions( visualization );
        display = createDisplay( visualization );
        launchDisplay( visualization, display );
        createDisplayActions( display );

        Node applicationNode = graph.getNode( 0 );
        applicationNodeItem = visualization.getVisualItem( "graph.nodes", applicationNode );

        JPanel controlsPanel = new JPanel( new FlowLayout( FlowLayout.LEFT ) );
        JButton zoomInBtn = new JButton( zoomIn );
        JButton zoomOutBtn = new JButton( zoomOut );
        JButton zoomToFitBtn = new JButton( new ZoomToFitAction() );
        JButton actualSizeButton = new JButton( new ActualSizeAction() );

        controlsPanel.setBackground( Color.white );
        zoomInBtn.setBackground( Color.white );
        zoomOutBtn.setBackground( Color.white );
        zoomToFitBtn.setBackground( Color.white );
        actualSizeButton.setBackground( Color.white );

        controlsPanel.add( zoomInBtn );
        controlsPanel.add( zoomOutBtn );
        controlsPanel.add( zoomToFitBtn );
        controlsPanel.add( actualSizeButton );

/*
        final JTextField xField = new JTextField( 4 );
        final JTextField yField = new JTextField( 4 );
        JButton jButton = new JButton( "Pan" );
        jButton.addActionListener( new ActionListener()
        {
            public void actionPerformed( ActionEvent e )
            {
                double x = Double.valueOf( xField.getText() );
                double y = Double.valueOf( yField.getText() );
                display.panAbs( x, y );
                display.repaint();
            }
        } );

        for( Object o :  Arrays.asList( xField, yField, jButton ) )
        {
            controlsPanel.add( (Component) o );
        }
*/

        final JScrollBar hBar = new JScrollBar( JScrollBar.HORIZONTAL, 0, 5, 0, 10 );
        hBar.setUnitIncrement( 10 );
        hBar.setBlockIncrement( 100 );
        hBar.setModel( hBarModel );
        final JScrollBar vBar = new JScrollBar( JScrollBar.VERTICAL );
        vBar.setModel( vBarModel );

        hBar.addAdjustmentListener(
            new AdjustmentListener()
            {
                public void adjustmentValueChanged( AdjustmentEvent e )
                {
                    pan( display, hBar.getValue(), vBar.getValue() );
                }
            } );

        add( controlsPanel, BorderLayout.NORTH );
        add( display, BorderLayout.CENTER );
        add( hBar, BorderLayout.SOUTH );
        add( vBar, BorderLayout.EAST );

    }

    private void createDisplayActions( Display display )
    {
        InputMap inputMap = getInputMap( WHEN_ANCESTOR_OF_FOCUSED_COMPONENT );
        ActionMap actionMap = getActionMap();

        zoomIn = new ZoomInAction( display );
        inputMap.put( KeyStroke.getKeyStroke( '+' ), "zoomIn" );
        actionMap.put( "zoomIn", zoomIn );

        zoomOut = new ZoomOutAction( display );
        inputMap.put( KeyStroke.getKeyStroke( '-' ), "zoomOut" );
        actionMap.put( "zoomOut", zoomOut );

/*
        Action panLeft = new PanLeftAction( display );
        inputMap.put( KeyStroke.getKeyStroke( KeyEvent.VK_LEFT, 0 ), "panLeft" );
        actionMap.put( "panLeft", panLeft );

        Action panRight = new PanRightAction( display );
        inputMap.put( KeyStroke.getKeyStroke( KeyEvent.VK_RIGHT, 0 ), "panRight" );
        actionMap.put( "panRight", panRight );

        Action panUp = new PanUpAction( display );
        inputMap.put( KeyStroke.getKeyStroke( KeyEvent.VK_UP, 0 ), "panUp" );
        actionMap.put( "panUp", panUp );

        Action panDown = new PanDownAction( display );
        inputMap.put( KeyStroke.getKeyStroke( KeyEvent.VK_DOWN, 0 ), "panDown" );
        actionMap.put( "panDown", panDown );
*/
    }

    private void launchDisplay( Visualization visualization, Display display )
    {
        visualization.run( "color" );
        visualization.run( "layout" );
        visualization.run( "hideEdges" );
        visualization.run( "repaint" );
    }

    private Display createDisplay( Visualization visualization )
    {
        Display display = new Display( visualization );

//        display.addControlListener( new PanControl() );
//        display.addControlListener( new ZoomControl() );

        display.setItemSorter( new ItemSorter()
        {
            public int score( VisualItem item )
            {
                // First draw the Application box, then the edges, then other nodes
                if( item.getInt( FIELD_TYPE ) == TYPE_APPLICATION )
                {
                    return 0;
                }
                else if( item instanceof EdgeItem )
                {
                    return 1;
                }
                else
                {
                    return 2;
                }
            }
        } );

        display.addControlListener( compositeSelectionControl );

        display.addPaintListener( new PaintListener()
        {
            double previousScale = 1.0D;

            public void prePaint( Display d, Graphics2D g )
            {
            }

            public void postPaint( Display d, Graphics2D g )
            {
                double scale = d.getScale();

                if( scale != previousScale )
                {
                    int width = (int) applicationNodeItem.getBounds().getWidth();
                    int height = d.getHeight();

                    int extent = (int) ( d.getWidth() / scale );
                    int wValue = ( width - extent ) / 2;

//                    System.out.println( "width - " + width + ", wValue - " + wValue + ", extent - " + extent );
                    hBarModel.setRangeProperties( wValue, extent, 0, width, false );
                    vBarModel.setRangeProperties( height, height, height, height, false );

                    previousScale = scale;
                }
            }
        } );
        return display;
    }

    private int prevX;
    private int prevY;

    private void pan( Display display, int x, int y )
    {
/*
        System.out.println( "prevX = " + prevX );
        System.out.println( "x = " + x );
        System.out.println( "panning x by : " + (prevX - x) );
        //todo not worrying about vertical scrolling at the moment
*/

        display.panAbs( prevX - x, 0 );
        prevX = x;
        display.repaint();
    }

    private void createProcessingActions( Visualization visualization )
    {
        ActionList color = establishColors();
        ApplicationLayout layout = new ApplicationLayout( "graph" );

        visualization.putAction( "color", color );
        visualization.putAction( "layout", layout );
        visualization.putAction( "repaint", new RepaintAction() );
        visualization.putAction( "hideEdges", new prefuse.action.Action()
        {

            public void run( double frac )
            {
                Iterator itr = m_vis.items( "graph.edges", "type=100" );
                while( itr.hasNext() )
                {
                    VisualItem item = (VisualItem) itr.next();
                    item.setVisible( false );
                }
            }
        } );
    }

    private void createRenderers( Visualization visualization )
    {

        DefaultRendererFactory rendererFactory = new DefaultRendererFactory();

        rendererFactory.add( "type = 0", new ApplicationRenderer() );
        rendererFactory.add( "type = 1", new LayerRenderer() );
        rendererFactory.add( "type = 2", new ModuleRenderer() );
        rendererFactory.add( "type = 3", new CompositeRenderer() );

        rendererFactory.setDefaultEdgeRenderer( new VerticalEdgeRenderer() );
        visualization.setRendererFactory( rendererFactory );
    }

    private Visualization createVisualization( Graph graph )
    {
        // add the graph to the visualization as the data group "graph"
        // nodes and edges are accessible as "graph.nodes" and "graph.edges"
        Visualization visualization = new Visualization();
        visualization.add( "graph", graph );
        return visualization;
    }

    private ActionList establishColors()
    {
        // color for edges
        ColorAction edgesStroke = new ColorAction( "graph.edges", VisualItem.STROKECOLOR, ColorLib.gray( 100 ) );
        ColorAction edgesFill = new ColorAction( "graph.edges", VisualItem.FILLCOLOR, ColorLib.gray( 100 ) );

        // an action list containing all color assignments
        ActionList color = new ActionList();
        color.add( edgesStroke );
        color.add( edgesFill );
        return color;
    }

    public void graphShown()
    {
        zoomToFit();
    }

    private Point2D getDisplayCenter()
    {
        return new Point2D.Float( display.getWidth() / 2, display.getHeight() / 2 );
    }

    private void zoomToFit()
    {
        DisplayLib.fitViewToBounds( display, applicationNodeItem.getBounds(), 2000 );
    }

    private class ZoomToFitAction extends AbstractAction
    {
        private ZoomToFitAction()
        {
            super( "Zoom To Fit" );
        }

        public void actionPerformed( ActionEvent e )
        {
            zoomToFit();
        }
    }

    private class ActualSizeAction extends AbstractAction
    {
        private ActualSizeAction()
        {
            super( "Actual Size" );
        }

        public void actionPerformed( ActionEvent e )
        {
            display.animateZoom( getDisplayCenter(), 1 / display.getScale(), 2000 );
        }
    }

    private class ZoomInAction extends AbstractAction
    {
        private Display display;

        private ZoomInAction( Display display )
        {
            super( "Zoom In" );
            this.display = display;
        }

        public void actionPerformed( ActionEvent e )
        {
            Point2D p = getDisplayCenter();
            display.animateZoom( p, 1.1, 1000 );
        }

    }

    private class ZoomOutAction extends AbstractAction
    {
        private Display display;

        private ZoomOutAction( Display display )
        {
            super( "Zoom Out" );
            this.display = display;
        }

        public void actionPerformed( ActionEvent e )
        {
            Point2D p = new Point2D.Float( display.getWidth() / 2, display.getHeight() / 2 );
            display.animateZoom( p, 0.91, 1000 );
        }
    }

    private int panMovement = 10;

    private class PanLeftAction extends AbstractAction
    {
        private Display display;

        private PanLeftAction( Display display )
        {
            super( "Pan Left" );
            this.display = display;
        }

        public void actionPerformed( ActionEvent e )
        {
            AffineTransform at = display.getTransform();
            display.pan( at.getShearX() - panMovement, at.getShearY() );
            display.repaint();
        }
    }

    private class PanRightAction extends AbstractAction
    {
        private Display display;

        private PanRightAction( Display display )
        {
            super( "Pan Right" );
            this.display = display;
        }

        public void actionPerformed( ActionEvent e )
        {
            AffineTransform at = display.getTransform();
            display.pan( at.getShearX() + panMovement, at.getShearY() );
            display.repaint();
        }
    }

    private class PanUpAction extends AbstractAction
    {
        private Display display;

        private PanUpAction( Display display )
        {
            super( "Pan Up" );
            this.display = display;
        }

        public void actionPerformed( ActionEvent e )
        {
            AffineTransform at = display.getTransform();
            display.pan( at.getShearX(), at.getShearY() - panMovement );
            display.repaint();
        }
    }

    private class PanDownAction extends AbstractAction
    {
        private Display display;

        private PanDownAction( Display display )
        {
            super( "Pan Down" );
            this.display = display;
        }

        public void actionPerformed( ActionEvent e )
        {
            AffineTransform at = display.getTransform();
            display.pan( at.getShearX(), at.getShearY() + panMovement );
            display.repaint();
        }
    }

}