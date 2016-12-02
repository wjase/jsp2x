/*
 * Copyright (c) 2008, Hannes Schmidt. All rights reserved.
 * 
 * This file is part of Jsp2X.
 * 
 * Jsp2X is free software: you can redistribute it and/or modify it under the terms of the GNU
 * Lesser Public License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * Jsp2X is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser
 * Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser Public License along with Jsp2X. If not, see
 * <http://www.gnu.org/licenses/>.
 * 
 * $Author$ 
 * $Date$ 
 * $Rev$
 */
/**
 *
 */
package com.cybernostics.forks.jsp2x;

import static com.cybernostics.forks.jsp2x.JspParser.PCDATA;


public class TextCleanupTransformation extends AbstractTransformation {

    @Override
    protected JspTree onPcData( JspTree pcData, JspTree parent, JspTree prev ) {
        escapeEntities( pcData );
        if( prev.getType() == PCDATA ) {
            prev.setText( prev.getText() + pcData.getText() );
            return null;
        }
        return pcData;
    }

    private void escapeEntities( final JspTree node ) {
        String text = node.getText();
        text = text.replaceAll( "&(?!amp;)", "&amp;" );
        text = text.replace( "<", "&lt;" );
        text = text.replace( ">", "&gt;" );
        node.setText( text );
    }
}
