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

import static com.cybernostics.forks.jsp2x.JspParser.ATTRIBUTE;
import static com.cybernostics.forks.jsp2x.JspParser.ELEMENT;

import com.cybernostics.forks.jsp2x.JspTree.Element;
import com.cybernostics.forks.jsp2x.JspTree.PcData;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class JspTextTransformation extends AbstractTransformation {

    //J-
    static final Pattern INDENTED_TEXT_PATTERN = Pattern.compile(
        "^(\\s*(?:\\n\\s*)+)([^\\s].*?)(\\s*)$"
    );
    //J+

    @Override
    protected JspTree onPcData( JspTree tree, JspTree parent, JspTree prev ) {
        if(
            parent == null
            || parent.getType() != ATTRIBUTE
            || parent.getType() == ELEMENT && parent.name().equals( "script" )
        ) {
            return wrapInJspText( tree );
        } else {
            return tree;
        }
    }

    private JspTree wrapInJspText( final JspTree pcdata ) {
        final Matcher matcher = INDENTED_TEXT_PATTERN.matcher( pcdata.getText() );
        if( matcher.matches() ) {
            final JspTree jspText = new Element( "jsp:text" );
            final String leadingSpace = matcher.group( 1 );
            final String text = matcher.group( 2 );
            final String trailingSpace = matcher.group( 3 );
            jspText.addChild( new PcData( text ) );
            final JspTree list = new JspTree();
            list.addChild( new PcData( leadingSpace ) );
            list.addChild( jspText );
            if( trailingSpace.length() > 0 ) {
                list.addChild( new PcData( trailingSpace ) );
            }
            return list;
        }
        return pcdata;
    }
}
