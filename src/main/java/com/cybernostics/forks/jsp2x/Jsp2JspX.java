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

import org.antlr.runtime.ANTLRFileStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.tree.CommonTreeNodeStream;

import static com.cybernostics.forks.jsp2x.JspParser.*;

import com.cybernostics.forks.jsp2x.JspTree.Attribute;
import com.cybernostics.forks.jsp2x.JspTree.Element;
import com.cybernostics.forks.jsp2x.JspTree.PcData;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.cybernostics.forks.jsp2x.JspLexer;
import com.cybernostics.forks.jsp2x.JspParser;
import com.cybernostics.forks.jsp2x.Xml;


public class Jsp2JspX extends AbstractTransformation {

    private static final String JSPX_NAMESPACE = "jspx";
    private final String inputFileName;
    private final File outputFile;
    private final Set<String> namespaces = new HashSet<String>();
    private final Map<String, String> namespaceMappings;

    protected final Resolver resolver;

    public Jsp2JspX( final String inputFileName, final File outputFile, final Resolver resolver ) {
        this( inputFileName, outputFile, resolver, new HashMap<String, String>() );
        namespaceMappings.put( null, "http://www.w3c.org/1999/xhtml" );
        namespaceMappings.put( "jsp", "http://java.sun.com/JSP/Page" );
        // TODO: command switch to override shortname
        namespaceMappings.put( JSPX_NAMESPACE, "urn:jsptagdir:/WEB-INF/tags/jspx" );
    }

    private Jsp2JspX(
        final String inputFileName,
        final File outputFile,
        final Resolver resolver,
        final Map<String, String> namespaceMappings
    ) {
        this.inputFileName = inputFileName;
        this.outputFile = outputFile;
        this.resolver = resolver;
        this.namespaceMappings = namespaceMappings;
    }

    public void convert() throws IOException, RecognitionException {
        JspTree tree = parse();
        if( tree == null ) {
            warn( "Parser didn't return any results." );
            return;
        }
        tree = transform( tree );
        wrapInRootElement( tree );
        addNamespaceToRootElement( tree );
        final CommonTreeNodeStream nodes = new CommonTreeNodeStream( tree );
        final Xml xml = new Xml( nodes );
        xml.setOut( new PrintStream( new FileOutputStream( outputFile ) ) );
        xml.document();
        if( namespaces.contains( JSPX_NAMESPACE ) ) {
            resolver.createUtilityTagFiles( JSPX_NAMESPACE );
        }
    }

    private JspTree parse() throws IOException, RecognitionException {
        final JspLexer lexer = new JspLexer( new ANTLRFileStream( inputFileName ) );
        final CommonTokenStream tokens = new CommonTokenStream( lexer );
        final JspParser parser = new JspParser( tokens );
        parser.setTreeAdaptor( new JspTree.Adaptor() );
        final JspTree tree = ( JspTree ) parser.document().getTree();
        return tree;
    }

    @Override
    public JspTree transform( JspTree tree ) {
        tree = super.transform( tree );
        tree = new TextCleanupTransformation().transform( tree );
        tree = new JspTextTransformation().transform( tree );
        return tree;
    }

    @Override
    protected JspTree transform( JspTree tree, JspTree parent, JspTree prev ) {
        tree = super.transform( tree, parent, prev );
        if( tree != null ) trackNamespacesInElement( tree );
        return tree;
    }

    private void wrapInRootElement( final JspTree tree ) {
        JspTree root;
        // TODO: this is an insufficient way to test for a file that is included
        if( inputFileName.endsWith( ".jspf" ) ) {
            root = new Element( JSPX_NAMESPACE + ":fragment" );
        } else {
            root = new Element( "jsp:root", new Attribute( "version", "2.0" ) );
        }
        trackNamespacesInElement( root );
        for( int i = 0; i < tree.size(); i++ ) {
            final JspTree child = tree.getChild( i );
            final int type = child.getType();
            if( type != PROCESSING_INSTRUCTION && type != DOCTYPE_DEFINITION ) {
                tree.deleteChild( i-- );
                root.addChild( child );
            }
        }
        tree.addChild( root );
    }

    private JspTree findRootElement( final JspTree tree ) {
        JspTree root = null;
        for( int i = 0; i < tree.size(); i++ ) {
            final JspTree child = tree.getChild( i );
            if( child.getType() == ELEMENT ) {
                if( root == null ) {
                    root = child;
                } else {
                    return null;
                }
            }
        }
        return root;
    }

    private void addNamespaceToRootElement( final JspTree tree ) {
        final JspTree root = findRootElement( tree );
        if( root != null ) {
            final JspTree attributes = root.attributes();
            for( final String namespace : namespaces ) {
                // TODO: do research as to whether we should generate a xmlns attribute for the null
                // namespace. The rest of the code works with null namespaces. It's just that Resin
                // replicates the null namespace reference unnecessarily in the root element of
                // every tagfile.
                if( namespace != null ) {
                    attributes.addChild(
                        new Attribute(
                            "xmlns" + ( namespace == null ? "" : ":" + namespace ),
                            namespaceMappings.get( namespace )
                        )
                    );
                }
            }
        } else {
            throw new Failure( "Wasn't able to determine the root element of converted input." );
        }
    }

    private static void warn( final String message ) {
        System.err.println( "Warning:" + message );
    }

    @Override
    protected JspTree onProcessingInstr( JspTree tree, JspTree parent, JspTree prev ) {
        return isXmlDeclaration( tree ) ? null : tree;
    }

    //J-
    private static final Pattern DOCTYPE_PATTERN = Pattern.compile(
        "<!DOCTYPE\\s+" // preamble
        + "(\\w+)\\s+" // root namespace
        + "PUBLIC\\s+" // we only support public doctype (as opposed to system ones)
        + "\"([^\"]+)\"\\s+" // the PubidLiteral (yeah, I know, wtf)
        + "\"([^\"]+)\"" // the SystemLiteral (ditto)
        + "\\s*>"
    );
    //J+

    @Override
    protected JspTree onDoctypeDef( JspTree tree, JspTree parent, JspTree prev ) {
        final Matcher matcher = DOCTYPE_PATTERN.matcher( tree.getText() );
        if( matcher.matches() ) {
            return new Element(
                "jsp:output",
                new Attribute( "doctype-root-element", matcher.group( 1 ) ),
                new Attribute( "doctype-public", matcher.group( 2 ) ),
                new Attribute( "doctype-system", matcher.group( 3 ) )
            );

        }
        return tree;
    }

    @Override
    protected JspTree onComment( JspTree tree, JspTree parent, JspTree prev ) {
        tree.setType( CDATA );
        tree.setText( "<!--" + tree.getText() + "-->" );
        return tree;
    }

    @Override
    protected JspTree onJspComment( JspTree tree, JspTree parent, JspTree prev ) {
        tree.setType( COMMENT );
        return tree;
    }

    @Override
    protected JspTree onElExpression( JspTree tree, JspTree parent, JspTree prev ) {
        trackNamespacesInElExpression( tree );
        tree.setType( PCDATA );
        tree.setText( "${" + tree.getText() + "}" );
        return tree;
    }

    @Override
    protected JspTree onJspScriptlet( JspTree tree, JspTree parent, JspTree prev ) {
        return wrapInElement( tree, "jsp:scriptlet" );
    }

    @Override
    protected JspTree onJspExpression( JspTree tree, JspTree parent, JspTree prev ) {
        return wrapInElement( tree, "jsp:expression" );
    }

    @Override
    protected JspTree onJspDirective( JspTree tree, JspTree parent, JspTree prev ) {
        if( handleIncludeDirective( tree ) || resolveTaglib( tree ) ) return null;
        tree.setType( ELEMENT );
        tree.setName( "jsp:directive." + tree.name() );
        return tree;
    }

    @Override
    protected JspTree onElement( JspTree tree, JspTree parent, JspTree prev ) {
        if( false ) wrapScriptContentInCDATA( tree );
        return resolveRecursionInAttributes( tree );
    }

    private JspTree wrapInElement( final JspTree tree, final String string ) {
        tree.setType( PCDATA );
        final JspTree element = new Element( string );
        element.addChild( tree );
        return element;
    }

    private boolean resolveTaglib( final JspTree directive ) {
        if( directive.name().equals( "taglib" ) ) {
            String namespace = null;
            String uri = null;
            final JspTree attributes = directive.attributes();
            for( int i = 0; i < attributes.size(); i++ ) {
                final JspTree attribute = attributes.getChild( i );
                final String name = attribute.name();
                final String value = attribute.value();
                if( "prefix".equals( name ) ) {
                    namespace = value;
                } else if( "uri".equals( name ) ) {
                    if( value.startsWith( "/" ) ) {
                        uri = "urn:jsptld:" + value;
                    } else {
                        uri = value;
                    }
                } else if( "tagdir".equals( name ) ) {
                    uri = "urn:jsptagdir:" + value;
                } else {
                    warn( "Unknown taglib directive atribute '" + name + "'." );
                }
            }
            if( namespace != null && uri != null ) {
                namespaceMappings.put( namespace, uri );
                return true;
            }
            warn( "Incomplete taglib directive '" + directive.getText() + "'." );
        }
        return false;
    }

    private boolean handleIncludeDirective( final JspTree directive ) {
        if( directive.name().equals( "include" ) ) {
            try {
                final JspTree attributes = directive.attributes();
                for( int i = 0; i < attributes.size(); i++ ) {
                    final JspTree attribute = attributes.getChild( i );
                    if( attribute.name().equals( "file" ) ) {
                        final JspTree includedTree = resolveTaglibsFromInclude( attribute );
                        if( includedTree.isEmptyTree() ) {
                            return true;
                        }
                        rewriteIncludeDirective( attribute );
                        return false;
                    }
                }
            } catch( final IOException e ) {
                warn( e.getMessage() );
            } catch( final RecognitionException e ) {
                warn( e.getMessage() );
            }
        }
        return false;
    }

    private void rewriteIncludeDirective( final JspTree attribute ) {
        final String fileName = attribute.value();
        final String newFileName = resolver.rewriteLogicalPath( fileName );
        if( ! fileName.equals( newFileName ) ) {
            attribute.treeValue().setText( newFileName );
        }
    }

    private JspTree resolveTaglibsFromInclude( final JspTree attribute )
    throws IOException, RecognitionException {
        final String includeFile = resolver.physicalPathFor( attribute.value() );
        final Jsp2JspX jsp2JspX =
            new Jsp2JspX( includeFile, outputFile, resolver, namespaceMappings );
        return jsp2JspX.transform( jsp2JspX.parse() );
    }

    private void wrapScriptContentInCDATA( final JspTree element ) {
        if( element.name().equals( "script" ) ) {
            wrapCDataInPCData( element );
        }
    }

    private void wrapCDataInPCData( final JspTree element ) {
        for( int i = 2; i < element.size(); i++ ) {
            final JspTree child = element.getChild( i );
            switch( child.getType() ) {
                case PCDATA:
                    element.setChild( i, new JspTree( CDATA, child.getText() ) );
                    break;
                case ELEMENT:
                    wrapCDataInPCData( child );
                    break;
            }
        }
    }

    private JspTree resolveRecursionInAttributes( final JspTree element ) {
        if( hasRecursionInAttributes( element ) ) {
            return jspxElementFor( element );
        }
        return element;
    }

    private boolean hasRecursionInAttributes( final JspTree element ) {
        final JspTree attributes = element.attributes();
        if( attributes != null ) {
            for( int i = 0; i < attributes.size(); i++ ) {
                final JspTree attribute = attributes.getChild( i );
                if( attribute.getType() == ATTRIBUTE ) {
                    for( int j = 1; j < attribute.size(); j++ ) {
                        final int type = attribute.getChild( j ).getType();
                        if( type == JSP_EXPRESSION || type == ELEMENT || type == JSP_SCRIPTLET ) {
                            return true;
                        }
                    }
                } else {
                    return true;
                }
            }
        }
        return false;
    }

    private JspTree jspxElementFor( final JspTree element ) {
        final JspTree attributes = element.attributes();
        final String name = JSPX_NAMESPACE + ":element";
        final JspTree jspxElement = new Element( name, new Attribute( "name", element.name() ) );
        for( int i = 0; i < attributes.size(); i++ ) {
            jspxElement.addChild( jspxAttributeFor( attributes.getChild( i ) ) );
        }
        if( element.size() > 2 ) {
            final JspTree jspxBody = new Element( JSPX_NAMESPACE + ":body" );
            jspxElement.addChild( jspxBody );
            for( int i = 2; i < element.size(); i++ ) {
                jspxBody.addChild( element.getChild( i ) );
            }
        }
        return jspxElement;
    }

    private JspTree jspxAttributeFor( final JspTree tree ) {
        switch( tree.getType() ) {
            case ATTRIBUTE:
                return jspxAttributeForAttribute( tree );
            case EL_EXPR:
                return jspxAttributeForElExpr( tree );
            case ELEMENT:
                return jspxAttributeForElement( tree );
            default:
                throw new Failure( "Don't know what to do with '" + tree.getText() + "'" );
        }
    }

    private JspTree jspxAttributeForElement( final JspTree element ) {
        // Handle start tags with nested elements, e.g thinks like
        // <td <c:if test="...">foo="bar"</c:if> >...</td>
        for( int i = 2; i < element.size(); i++ ) {
            element.setChild( i, jspxAttributeFor( element.getChild( i ) ) );
        }
        return element;
    }

    //J-
    private static final Pattern CONDITIONAL_ATTRIBUTE_PATTERN = Pattern.compile(
        "^" // anchor at beginning of string
        + "\\s*" // allow leading whitespace
        + "([^?]+?)" // the condition, non-greedy
        + "\\s*\\?\\s*" // the ? operator
        + "'" // start first alternative
        + "(?:" // start non-capturing group
        + "(\\w+)" // the attribute name
        + "\\s*=\\s*" // the equals sign
        + "\"([^\"]*)\"\\s*" // the attribute value
        + ")?" // end non-capturing group, first alternative is optional
        + "'" // end first alternative
        + "\\s*:\\s*" // the : operator
        + "'" // start second alternative
        + "(?:" // start non-capturing group
        + "(\\w+)" // the attribute name
        + "\\s*=\\s*" // the equals sign
        + "\"([^\"]*)\"\\s*" // the attribute value
        + ")?" // end non-capturing group, second alternative is optional
        + "'" // end second alternative
        + "\\s*" // allow trailing white
        + "$" // anchor at end of string
    );
    //J+

    private JspTree jspxAttributeForElExpr( final JspTree elExpr ) {
        final String expression = elExpr.getText();
        final Matcher matcher = CONDITIONAL_ATTRIBUTE_PATTERN.matcher( expression );
        if( matcher.matches() ) {
            final String condition = matcher.group( 1 );
            final String nameTrue = matcher.group( 2 );
            final String valueTrue = matcher.group( 3 );
            final String nameFalse = matcher.group( 4 );
            final String valueFalse = matcher.group( 5 );
            if( nameTrue != null && nameFalse != null ) {
                final JspTree cChoose = new Element( "c:choose" );
                cChoose.addChild(
                    conditionalForAttribute( "c:when", condition, nameTrue, valueTrue )
                );
                cChoose.addChild(
                    conditionalForAttribute( "c:otherwise", null, nameFalse, valueFalse )
                );
                return cChoose;
            } else if( nameTrue != null ) {
                return conditionalForAttribute( "c:if", condition, nameTrue, valueTrue );
            } else if( nameFalse != null ) {
                return conditionalForAttribute(
                    "c:if",
                    "! ( " + condition + " )",
                    nameFalse,
                    valueFalse
                );
            }
        }
        warn( "Wasn't able to do my magic on EL expression '" + expression + "'." );
        return new Element( JSPX_NAMESPACE + ":attribute", new Attribute( "unknown", elExpr ) );
    }

    private JspTree conditionalForAttribute(
        final String elementName,
        final String test,
        final String attributeName,
        final String attributeValue
    ) {
        final JspTree cIf = new Element( elementName );
        if( test != null ) {
            cIf.addAttribute( new Attribute( "test", new JspTree( EL_EXPR, test ) ) );
        }
        final JspTree jspxAttribute =
            new Element( JSPX_NAMESPACE + ":attribute", new Attribute( "name", attributeName ) );
        jspxAttribute.addChild( new PcData( attributeValue ) );
        cIf.addChild( jspxAttribute );
        return cIf;
    }

    private JspTree jspxAttributeForAttribute( final JspTree attribute ) {
        final JspTree jspxAttribute =
            new Element( JSPX_NAMESPACE + ":attribute", new Attribute( "name", attribute.name() ) );
        for( int i = 1; i < attribute.size(); i++ ) {
            jspxAttribute.addChild( attribute.getChild( i ) );
        }
        return jspxAttribute;
    }

    //J-
    private static final Pattern EL_FUNCTION_INVOCATION_PATTERN = Pattern.compile(
        "(\\w+):\\w+\\("
    );
    //J+

    private void trackNamespacesInElExpression( final JspTree el ) {
        final Matcher matcher = EL_FUNCTION_INVOCATION_PATTERN.matcher( el.getText() );
        while( matcher.find() ) {
            namespaces.add( matcher.group( 1 ) );
        }
    }

    //J-
    private static final Pattern NAMESPACE_PATTERN = Pattern.compile(
        "^(?:([-.\\w]+):)?[-.\\w]+$"
    );
    //J+

    protected void trackNamespacesInElement( final JspTree element ) {
        if( element.getType() == ELEMENT ) {
            final Matcher matcher = Jsp2JspX.NAMESPACE_PATTERN.matcher( element.name() );
            if( matcher.matches() ) {
                namespaces.add( matcher.group( 1 ) );
            }
        }
    }

    //J-
    private static final Pattern XML_DECLARATION_PATTERN = Pattern.compile( "^\\s*xml\\s+" );
    //J+

    private boolean isXmlDeclaration( final JspTree processingInstruction ) {
        return XML_DECLARATION_PATTERN.matcher( processingInstruction.getText() ).lookingAt();
    }

}
