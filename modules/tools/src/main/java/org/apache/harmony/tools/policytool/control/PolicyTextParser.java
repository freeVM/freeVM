/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.harmony.tools.policytool.control;

import java.util.ArrayList;
import java.util.List;

import org.apache.harmony.tools.policytool.model.CommentEntry;
import org.apache.harmony.tools.policytool.model.KeystoreEntry;
import org.apache.harmony.tools.policytool.model.KeystorePasswordURLEntry;
import org.apache.harmony.tools.policytool.model.PolicyEntry;

/**
 * Policy text parser.
 *
 */
public class PolicyTextParser {

    /** String containing the white spaces. */
    private static final String WHITE_SPACES  = " \t\n\r";

    /**
     * Parses a policy text and creates an equivalent list of policy entries from it.
     * @param policyText policy text to be parsed
     * @return an equivalent list of policy entries created from the policy text, equivalent to policy text
     * @throws InvalidPolicyTextException thrown if policyText is invalid
     */
    public static List< PolicyEntry > parsePolicyText( String policyText ) throws InvalidPolicyTextException {
        policyText = policyText.replace( "\r", "" ); // I only want to handle a unified line terminator

        final List< PolicyEntry > policyEntryList = new ArrayList< PolicyEntry >();

        final char[] policyTextChars = policyText.toCharArray();

        int index = 0;
        int[] firstLastTokenIndices;
        while ( ( firstLastTokenIndices = peekTokenAhead( policyTextChars, index ) ) != null ) {

            final String nextToken        = policyText.substring( firstLastTokenIndices[ 0 ], firstLastTokenIndices[ 1 ] );
            final String loweredNextToken = nextToken.toLowerCase();

            int newIndex = -1;

            // Line comment
            if ( nextToken.startsWith( "//" ) ) {
                newIndex = policyText.indexOf( '\n', firstLastTokenIndices[ 1 ] );
                if ( newIndex < 0 ) // This is the last line
                    newIndex = policyTextChars.length;
                policyEntryList.add( new CommentEntry( policyText.substring( index, newIndex ) ) );
            }

            // Block comment
            else if ( nextToken.startsWith( "/*" ) ) {
                newIndex = policyText.indexOf( "*/", firstLastTokenIndices[ 1 ] );
                if ( newIndex < 0 ) // No closing bracket
                    throw new InvalidPolicyTextException( "No closing bracket for block comment!" );
                newIndex += 2;  // length of "*/"
                policyEntryList.add( new CommentEntry( policyText.substring( index, newIndex ) ) );
            }

            // Keystore entry
            else if ( loweredNextToken.equals( KeystoreEntry.LOWERED_KEYWORD ) ) {
                final int[] firstLastKeystoreURLIndices = peekQuotedStringAhead( policyTextChars, firstLastTokenIndices[ 1 ] );
                if ( firstLastKeystoreURLIndices == null )
                    throw new InvalidPolicyTextException( "Incomplete keystore entry, found no quoted string for keystore URL!" );

                int[] keystoreTypeIndices     = null;
                int[] keystoreProviderIndices = null;

                char nextChar = peekNextNonWhiteSpaceChar( policyTextChars, firstLastKeystoreURLIndices[ 1 ] );
                if ( nextChar != PolicyEntry.TERMINATOR_CHAR ) {
                    if ( nextChar != ',' )
                        throw new InvalidPolicyTextException( "Was expecting semicolon but found something else!" );
                    keystoreTypeIndices = peekQuotedStringAhead( policyTextChars, policyText.indexOf( ',', firstLastKeystoreURLIndices[ 1 ] ) + 1 );
                    if ( keystoreTypeIndices == null )
                        throw new InvalidPolicyTextException( "Incomplete keystore entry, found no quoted string for keystore type!" );

                    nextChar = peekNextNonWhiteSpaceChar( policyTextChars, keystoreTypeIndices[ 1 ] );
                    if ( nextChar != PolicyEntry.TERMINATOR_CHAR ) {
                        if ( nextChar != ',' )
                            throw new InvalidPolicyTextException( "Was expecting semicolon but found something else!" );
                        keystoreProviderIndices = peekQuotedStringAhead( policyTextChars, policyText.indexOf( ',', keystoreTypeIndices[ 1 ] ) + 1 );
                        if ( keystoreProviderIndices == null )
                            throw new InvalidPolicyTextException( "Incomplete keystore entry, found no quoted string for keystore provider!" );

                        if ( peekNextNonWhiteSpaceChar( policyTextChars, keystoreProviderIndices[ 1 ] ) != PolicyEntry.TERMINATOR_CHAR )
                            throw new InvalidPolicyTextException( "Was expecting semicolon but found something else!" );
                        else
                            newIndex = policyText.indexOf( PolicyEntry.TERMINATOR_CHAR, keystoreProviderIndices[ 1 ] ) + 1;
                    }
                    else
                        newIndex = policyText.indexOf( PolicyEntry.TERMINATOR_CHAR, keystoreTypeIndices[ 1 ] ) + 1;
                }
                else
                    newIndex = policyText.indexOf( PolicyEntry.TERMINATOR_CHAR, firstLastKeystoreURLIndices[ 1 ] ) + 1;

                final KeystoreEntry keystoreEntry = new KeystoreEntry();
                keystoreEntry.setUrl( policyText.substring( firstLastKeystoreURLIndices[ 0 ] + 1, firstLastKeystoreURLIndices[ 1 ] - 1 ) );
                if ( keystoreTypeIndices != null )
                    keystoreEntry.setType( policyText.substring( keystoreTypeIndices[ 0 ] + 1, keystoreTypeIndices[ 1 ] - 1 ) );
                if ( keystoreProviderIndices != null )
                    keystoreEntry.setProvider( policyText.substring( keystoreProviderIndices[ 0 ] + 1, keystoreProviderIndices[ 1 ] - 1 ) );
                policyEntryList.add( keystoreEntry );
            }

            // Keystore password URL entry
            else if ( loweredNextToken.equals( KeystorePasswordURLEntry.LOWERED_KEYWORD ) ) {
                final int[] firstLastKeystorePasswordURLIndices = peekQuotedStringAhead( policyTextChars, firstLastTokenIndices[ 1 ] );
                if ( firstLastKeystorePasswordURLIndices == null )
                    throw new InvalidPolicyTextException( "Incomplete keystore password URL entry, found no quoted string for keystore password URL!" );

                final char nextChar = peekNextNonWhiteSpaceChar( policyTextChars, firstLastKeystorePasswordURLIndices[ 1 ] );
                if ( nextChar != PolicyEntry.TERMINATOR_CHAR )
                    throw new InvalidPolicyTextException( "Was expecting semicolon but found something else!" );
                else
                    newIndex = policyText.indexOf( PolicyEntry.TERMINATOR_CHAR, firstLastKeystorePasswordURLIndices[ 1 ] ) + 1;

                final KeystorePasswordURLEntry keystorePasswordURLEntry = new KeystorePasswordURLEntry();
                keystorePasswordURLEntry.setUrl( policyText.substring( firstLastKeystorePasswordURLIndices[ 0 ] + 1, firstLastKeystorePasswordURLIndices[ 1 ] - 1 ) );
                policyEntryList.add( keystorePasswordURLEntry );
            }

            if ( newIndex >= 0 && newIndex < policyTextChars.length && policyTextChars[ newIndex ] == '\n' )
                newIndex++;

            index = newIndex;
        }

        return policyEntryList;
    }

    /**
     * Skips the following whtie spaces starting from the specified index,
     * and returns the next non-white space index or -1 if end of string reached.
     *
     * @param stringChars char array of the string to use
     * @param index index to start from
     * @return the next non-white space index or -1 if end of string reached
     */
    private static int skipWhiteSpaces( final char[] stringChars, int index ) {
        try {
            while ( WHITE_SPACES.indexOf( stringChars[ index ] ) >= 0 )
                index++;

            return index;
        } catch ( final ArrayIndexOutOfBoundsException aioobe ) {
            return -1;
        }
    }

    /**
     * Returns the first non-whitespace character starting from a given index or -1 if all the remaining characters are white spaces.
     * @param stringChars char array of the string to use
     * @param index index to start from
     * @return the first non-whitespace character starting from a given index or -1 if all the remaining characters are white spaces
     */
    private static char peekNextNonWhiteSpaceChar( final char[] stringChars, int index ) {
        index = skipWhiteSpaces( stringChars, index );
        if ( index < 0 )
            return (char) -1;
        else
            return stringChars[ index ];
    }

    /**
     * Returns the first (inclusive) and last index (exclusive) of the next token or null if no more tokens.
     * @param stringChars char array of the string to use
     * @param index index to start from
     * @return the first (inclusive) and last index (exclusive) of the next token or null if no more tokens
     */
    private static int[] peekTokenAhead ( final char[] stringChars, int index ) {
        index = skipWhiteSpaces( stringChars, index );
        if ( index < 0 )
            return null;

        final int firstIndex = index;

        while ( index++ < stringChars.length )
            if ( index == stringChars.length || WHITE_SPACES.indexOf( stringChars[ index ] ) >= 0  )
                break;

        return new int[] { firstIndex, index };
    }

    /**
     * Returns the first (inclusive) and last index (exclusive) of the next quoted string or null if found something else.<br>
     * The string denoted by the returned indices includes the quotes in the beginning and in the end of the quoted string.
     *
     * @param stringChars char array of the string to use
     * @param index index to start from
     * @return the first (inclusive) and last index (exclusive) of the next quoted string or null if found something else
     * @throws InvalidPolicyTextException thrown if no quoted string found
     */
    private static int[] peekQuotedStringAhead ( final char[] stringChars, int index ) throws InvalidPolicyTextException {
        try {
            index = skipWhiteSpaces( stringChars, index );
            if ( index < 0 )
                return null;

            if ( stringChars[ index ] != '"' )
                throw new InvalidPolicyTextException( "Could not find expected quoted string (missing opener quotation mark)!" );

            final int firstIndex = index;

            while ( ++index <= stringChars.length ) {
                if ( index == stringChars.length )
                    throw new InvalidPolicyTextException( "Could not find expected quoted string (missing closer quotation mark)!" );

                if ( stringChars[ index ] == '"' )
                    break;
            }

            return new int[] { firstIndex, index+1 }; // +1 because end index must be exclusive

        } catch ( final ArrayIndexOutOfBoundsException aioobe ) {
            return null;
        }
    }

}
