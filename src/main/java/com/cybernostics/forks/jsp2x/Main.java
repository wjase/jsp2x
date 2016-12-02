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

import com.martiansoftware.jsap.FlaggedOption;
import com.martiansoftware.jsap.JSAP;
import com.martiansoftware.jsap.JSAPException;
import com.martiansoftware.jsap.JSAPResult;
import com.martiansoftware.jsap.Parameter;
import com.martiansoftware.jsap.SimpleJSAP;
import com.martiansoftware.jsap.Switch;
import com.martiansoftware.jsap.UnflaggedOption;

import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;


public class Main implements Resolver {

    private static final String FS = File.separator;

    private final String outputDir;
    private final Set<String> files;
    private final boolean clobber;
    private boolean utilityTagFilesCreated;

    public Main( String[] args ) throws JSAPException, IOException {

        SimpleJSAP jsap =
            new SimpleJSAP(
                "Jsp2JspX",
                "Converts JSP pages to JSP documents (well-formed XML files with JSP tags).",
                new Parameter[] {
                    new Switch(
                        "clobber",
                        'c',
                        "clobber",
                        "Overwrite output files even if they already exist."
                    ),
                    new FlaggedOption(
                        "output",
                        JSAP.STRING_PARSER,
                        JSAP.NO_DEFAULT,
                        JSAP.NOT_REQUIRED,
                        'o',
                        "output",
                        "The path to the output folder. By default output files and logs are "
                        + "created in the same directory as the input file."
                    ),
                    new UnflaggedOption(
                        "file",
                        JSAP.STRING_PARSER,
                        JSAP.NO_DEFAULT,
                        JSAP.REQUIRED,
                        JSAP.GREEDY,
                        "One or more paths to JSP files. Should not be absolute paths."
                    )
                }
            );

        JSAPResult config = jsap.parse( args );
        if( jsap.messagePrinted() ) System.exit( 1 );

        this.clobber = config.getBoolean( "clobber" );

        String outputDir = config.getString( "output" );
        if( outputDir == null ) {
            outputDir = "";
        } else {
            if( ! outputDir.endsWith( FS ) ) {
                outputDir += FS;
            }
        }
        this.outputDir = outputDir;

        final String[] files = config.getStringArray( "file" );
        rewritePaths( files, outputDir != null );

        this.files = Collections.unmodifiableSet( new HashSet<String>( Arrays.asList( files ) ) );

        checkForRewriteCollisions();
    }

    private void checkForRewriteCollisions() {
        Set<String> rewrittenFiles = new HashSet<String>();
        for( String file : files ) {
            String rewrittenFile = rewritePath( file );
            if( ! rewrittenFiles.add( rewrittenFile ) ) {
                System.err.println(
                    "Error: The output file name for argument ' + file + ' collides with the "
                    + "output file name for another argument. This is ususally caused by two "
                    + "arguments that only differ in their file extensions '.jsp' and '.jspf'. "
                    + "Note that both '.jspf' and '.jsp' are mapped to '.jspx'."
                );
                System.exit( 1 );
            }
        }
    }

    private void rewritePaths( String[] files, boolean enforce ) {
        for( int i = 0; i < files.length; i++ ) {
            if( new File( files[i] ).isAbsolute() ) {
                System.err.println(
                    "Warning: All arguments " + ( enforce ? "should" : "must" )
                    + " be relative paths. The argument '" + files[i] + "' is absolute."
                );
                if( enforce ) System.exit( 1 );
            }
            files[i] = files[i].replace( "/", FS );
            final String prefix = "." + FS;
            if( files[i].startsWith( prefix ) ) {
                files[i] = files[i].substring( prefix.length() );
            }

        }
    }

    public static void main( String[] args ) {
        try {
            new Main( args ).process();
        } catch( JSAPException e ) {
            System.err.println( e.getMessage() );
            System.exit( 1 );
        } catch( IOException e ) {
            System.err.println( e.getMessage() );
            System.exit( 1 );
        }

    }

    private void process() {
        int succeeded = 0;
        for( String file : files ) {
            if( process( file ) ) {
                succeeded++;
            }
        }

        System.err.println(
            "Sucessfully processed " + succeeded + " file(s) of a total of " + files.size() + "."
        );

        if( succeeded != files.size() ) {
            System.err.println(
                "See *.log files for more information regarding individual files."
            );
        }
    }

    private boolean process( String inputFileName ) {
        boolean success = false;
        try {
            final File logFile = outputFile( inputFileName + ".log" );
            final PrintStream logStream = new PrintStream( logFile );
            try {
                PrintStream stdout = System.out;
                PrintStream stderr = System.err;
                try {
                    System.setOut( logStream );
                    System.setErr( logStream );
                    try {
                        new Jsp2JspX(
                            inputFileName,
                            outputFile( rewritePath( inputFileName ) ),
                            this
                        ).convert();
                    } catch( Throwable t ) {
                        handleThrowable( t );
                    }
                } finally {
                    System.setErr( stderr );
                    System.setOut( stdout );
                }
            } finally {
                logStream.close();
                if( logFile.length() == 0 ) {
                    success = true;
                    logFile.delete();
                }
            }
        } catch( Throwable t ) {
            handleThrowable( t );
        }
        return success;
    }

    private void handleThrowable( Throwable t ) {
        if( t instanceof Failure ) {
            System.err.println( t.getMessage() );
        } else {
            t.printStackTrace();
        }
    }

    private File outputFile( String fileName ) throws Failure, IOException {
        final File file = new File( outputDir + fileName );
        final File dir = file.getParentFile();
        if( dir != null && ! dir.exists() ) {
            if( ! dir.mkdirs() ) {
                throw new Failure( "Failed to create output directory." );
            }
        }
        checkFile( file );
        return file;
    }

    private void checkFile( final File logFile ) throws IOException, Failure {
        if( ! clobber ) {
            if( ! logFile.createNewFile() ) {
                throw new Failure(
                    "The file '" + logFile.getPath()
                    + "' already exists. Use --clobber to force me to overwrite it."
                );
            }
        }
    }

    @Override
    public String physicalPathFor( String path ) {
        if( path.startsWith( "/" ) ) path = path.substring( 1 );
        path = path.replace( "/", FS );
        return path;
    }

    public String rewriteLogicalPath( String path ) {
        String physPath = physicalPathFor( path );
        if( files.contains( physPath ) ) path = rewritePath( path );
        return path;
    }

    //J-
    private static final Pattern INPUT_FILE_PATTERN = Pattern.compile( "\\.(?:(jsp)(f?)|(tag))$" );
    //J+

    private String rewritePath( String path ) {
        String newPath = INPUT_FILE_PATTERN.matcher( path ).replaceFirst( ".$1$3x" );
        if( newPath.equals( path ) ) newPath = newPath + ".xml";
        return newPath;
    }

    //J-
    private static final String[] TAG_NAMES =
        new String[] { "attribute", "body", "element", "fragment" };
    private static final String WEBINF_DIR = "WEB-INF";
    private static final String TAG_FILE_PATH = WEBINF_DIR + "/tags/%s/%s.tagx";
    //J+

    @Override
    public void createUtilityTagFiles( String namespace ) throws IOException {
        if( utilityTagFilesCreated ) return;
        if( outputDir.length() > 0 || new File( WEBINF_DIR ).isDirectory() ) {
            for( String tagName : TAG_NAMES ) {
                String path = "/" + String.format( TAG_FILE_PATH, "jspx", tagName );
                final InputStream input = getClass().getResourceAsStream( path );
                if( input == null ) {
                    throw new Failure(
                        "Could obtain resource stream for utility tag file '" + tagName + "'"
                    );
                }
                try {
                    path = String.format( TAG_FILE_PATH, namespace, tagName ).replace( "/", FS );
                    final FileOutputStream output = new FileOutputStream( outputFile( path ) );
                    try {
                        IOUtils.copy( input, output );
                    } finally {
                        output.close();
                    }
                } finally {
                    input.close();
                }
            }
            utilityTagFilesCreated = true;
        } else {
            throw new Failure(
                "Cannot find WEB-INF directory needed to create necessary tag files."
            );
        }
    }

}
