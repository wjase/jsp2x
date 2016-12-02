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
import static com.cybernostics.forks.jsp2x.JspParser.ATTRIBUTES;
import static com.cybernostics.forks.jsp2x.JspParser.ELEMENT;
import static com.cybernostics.forks.jsp2x.JspParser.EL_EXPR;
import static com.cybernostics.forks.jsp2x.JspParser.GENERIC_ID;
import static com.cybernostics.forks.jsp2x.JspParser.JSP_DIRECTIVE;
import static com.cybernostics.forks.jsp2x.JspParser.PCDATA;
import org.antlr.runtime.CommonToken;
import org.antlr.runtime.Token;
import org.antlr.runtime.tree.CommonTree;
import org.antlr.runtime.tree.CommonTreeAdaptor;


import java.util.regex.Pattern;


public class JspTree extends CommonTree {

    private JspTree( final Token t ) {
        super( t );
    }

    public JspTree() {
        super();
    }

    public JspTree( final int type, final String text, final JspTree... children ) {
        super( new CommonToken( type, text ) );
        for( final JspTree child : children ) {
            addChild( child );
        }
    }

    public final int size() {
        return getChildCount();
    }

    public void addAttribute( final JspTree attribute ) {
        if( getType() == ELEMENT || getType() == JSP_DIRECTIVE ) {
            getChild( 1 ).addChild( attribute );
        } else {
            throw new RuntimeException( "Given tree is not an element." );
        }
    }

    public JspTree attributes() {
        if( getType() == ELEMENT || getType() == JSP_DIRECTIVE ) {
            return getChild( 1 );
        } else {
            throw new RuntimeException( "Given tree is not an element." );
        }
    }

    public String name() {
        return getChild( 0 ).getText();
    }

    public JspTree treeValue() {
        if( ! ( getType() == ATTRIBUTE ) ) {
            throw new RuntimeException( "Given tree is not an attribute." );
        }
        if( ! ( size() == 2 ) ) {
            throw new RuntimeException( "Given attribute has complex value." );
        }
        return getChild( 1 );
    }

    public String value() {
        final JspTree value = this.treeValue();
        if( value.getType() == PCDATA || value.getType() == EL_EXPR ) {
            return value.getText();
        } else {
            throw new RuntimeException( "Given attribute is not of type text." );
        }
    }

    @SuppressWarnings( "unchecked" )
    public int insertChild( final int index, final JspTree child ) {
        if( child == null ) return 0;
        if( children == null ) {
            children = createChildrenList();
        }
        if( child.isNil() ) {
            final int childCount = child.size();
            for( int i = 0; i < childCount; i++ ) {
                children.add( index + i, child.getChild( i ) );
            }
            return childCount;
        } else {
            children.add( index, child );
            return 1;
        }
    }

    @Override
    public JspTree getChild( final int i ) {
        return ( JspTree ) super.getChild( i );
    }

    //J-
    private static final Pattern WHITESPACE_PATTERN = Pattern.compile( "\\s*" );
    //J+

    public boolean isEmpty() {
        return getType() == PCDATA && WHITESPACE_PATTERN.matcher( getText() ).matches();
    }

    public boolean isEmptyTree() {
        for( int i = 0; i < size(); i++ ) {
            final JspTree child = getChild( i );
            if( ! child.isEmpty() ) {
                return false;
            }
        }
        return true;
    }

    public void setName( final String name ) {
        getChild( 0 ).setText( name );
    }

    public void setText( final String string ) {
        getToken().setText( string );
    }

    @Override
    public Token getToken() {
        return super.getToken();
    }

    public void setType( final int type ) {
        getToken().setType( type );
    }

    public final static class Adaptor extends CommonTreeAdaptor {
        @Override
        public Object create( final Token payload ) {
            return new JspTree( payload );
        }
    }

    public static class Element extends JspTree {

        public Element( final String name, final JspTree... attributes ) {
            super(
                ELEMENT,
                "ELEMENT",
                new Id( name ),
                new JspTree( ATTRIBUTES, "ATTRIBUTES", attributes )
            );
        }

    }

    public static class Attribute extends JspTree {

        public Attribute( final String name, final JspTree value ) {
            super( ATTRIBUTE, "ATTRIBUTE", new Id( name ), value );
        }

        public Attribute( final String name, final String value ) {
            this( name, new PcData( value ) );
        }

    }

    public static class Id extends JspTree {

        public Id( final String id ) {
            super( GENERIC_ID, id );
        }

    }

    public static class PcData extends JspTree {

        public PcData( String value ) {
            super( PCDATA, value );
        }

    }
}
