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

package com.cybernostics.forks.jsp2x;

import static com.cybernostics.forks.jsp2x.JspParser.ATTRIBUTE;
import static com.cybernostics.forks.jsp2x.JspParser.COMMENT;
import static com.cybernostics.forks.jsp2x.JspParser.DOCTYPE_DEFINITION;
import static com.cybernostics.forks.jsp2x.JspParser.ELEMENT;
import static com.cybernostics.forks.jsp2x.JspParser.EL_EXPR;
import static com.cybernostics.forks.jsp2x.JspParser.JSP_COMMENT;
import static com.cybernostics.forks.jsp2x.JspParser.JSP_DIRECTIVE;
import static com.cybernostics.forks.jsp2x.JspParser.JSP_EXPRESSION;
import static com.cybernostics.forks.jsp2x.JspParser.JSP_SCRIPTLET;
import static com.cybernostics.forks.jsp2x.JspParser.PCDATA;
import static com.cybernostics.forks.jsp2x.JspParser.PROCESSING_INSTRUCTION;

import org.antlr.runtime.tree.Tree;


public abstract class AbstractTransformation {

    /**
     * Transform the given tree.
     *
     * @param   tree    The tree to be transformed. Can be nil (see {@link Tree#isNil()}) but must
     *                  not be null.
     * @param   parent  The parent of this tree.
     * @param   prev    The tree that immediately precedes the given tree in the parents list of
     *                  children.
     *
     * @return  The transformed tree or null if argument tree should be removed from its parent. The
     *          return value is not necessarily a new tree instance, it could be identical (==) to
     *          the argument. If the returned tree is nil (see {@link Tree#isNil()}) the tree
     *          argument was transformed into a list of trees. In that case the returned tree is
     *          supposed to be removed from its parent and be replaced with the children of the
     *          returned tree.
     */

    protected JspTree transform( JspTree tree, final JspTree parent, final JspTree prev ) {
        switch( tree.getType() ) {
            case ELEMENT:
                tree = onElement( tree, parent, prev );
                break;
            case JSP_DIRECTIVE:
                tree = onJspDirective( tree, parent, prev );
                break;
            case JSP_EXPRESSION:
                tree = onJspExpression( tree, parent, prev );
                break;
            case JSP_SCRIPTLET:
                tree = onJspScriptlet( tree, parent, prev );
                break;
            case EL_EXPR:
                tree = onElExpression( tree, parent, prev );
                break;
            case JSP_COMMENT:
                tree = onJspComment( tree, parent, prev );
                break;
            case COMMENT:
                tree = onComment( tree, parent, prev );
                break;
            case PCDATA:
                tree = onPcData( tree, parent, prev );
                break;
            case DOCTYPE_DEFINITION:
                tree = onDoctypeDef( tree, parent, prev );
                break;
            case PROCESSING_INSTRUCTION:
                tree = onProcessingInstr( tree, parent, prev );
                break;
            case ATTRIBUTE:
                tree = onAttribute( tree, parent, prev );
                break;
        }
        if( tree != null ) {
            transformChildren( tree );
        }
        return tree;
    }

    public JspTree transform( JspTree tree ) {
        return transform( tree, null, null );
    }

    protected final void transformChildren( final JspTree tree ) {
        JspTree prev = null;
        for( int i = 0; i < tree.size(); i++ ) {
            final JspTree child = tree.getChild( i );
            final JspTree transformedChild = transform( child, tree, prev );
            if( child != transformedChild ) {
                if( transformedChild != null ) {
                    if( transformedChild.isNil() ) {
                        tree.deleteChild( i );
                        i += tree.insertChild( i, transformedChild ) - 1;
                        prev = i > 0 ? tree.getChild( i ) : null;
                    } else {
                        tree.setChild( i, prev = transformedChild );
                    }
                } else {
                    tree.deleteChild( i );
                    while( i < tree.size() && ( tree.getChild( i ) ).isEmpty() ) {
                        tree.deleteChild( i );
                    }
                    i--;
                }
            } else {
                prev = child;
            }
        }
    }

    protected JspTree onAttribute( JspTree tree, JspTree parent, JspTree prev ) {
        return tree;
    }

    protected JspTree onProcessingInstr( JspTree tree, JspTree parent, JspTree prev ) {
        return tree;
    }

    protected JspTree onDoctypeDef( JspTree tree, JspTree parent, JspTree prev ) {
        return tree;
    }

    protected JspTree onPcData( JspTree tree, JspTree parent, JspTree prev ) {
        return tree;
    }

    protected JspTree onJspComment( JspTree tree, JspTree parent, JspTree prev ) {
        return tree;
    }

    protected JspTree onElExpression( JspTree tree, JspTree parent, JspTree prev ) {
        return tree;
    }

    protected JspTree onJspScriptlet( JspTree tree, JspTree parent, JspTree prev ) {
        return tree;
    }

    protected JspTree onJspExpression( JspTree tree, JspTree parent, JspTree prev ) {
        return tree;
    }

    protected JspTree onJspDirective( JspTree tree, JspTree parent, JspTree prev ) {
        return tree;
    }

    protected JspTree onElement( JspTree tree, JspTree parent, JspTree prev ) {
        return tree;
    }

    protected JspTree onComment( JspTree tree, JspTree parent, JspTree prev ) {
        return tree;
    }
}
