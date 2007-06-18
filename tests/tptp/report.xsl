<!--
    Licensed to the Apache Software Foundation (ASF) under one or more
    contributor license agreements.  See the NOTICE file distributed with
    this work for additional information regarding copyright ownership.
    The ASF licenses this file to You under the Apache License, Version 2.0
    (the "License"); you may not use this file except in compliance with
    the License.  You may obtain a copy of the License at

         http://www.apache.org/licenses/LICENSE-2.0

     Unless required by applicable law or agreed to in writing, software
     distributed under the License is distributed on an "AS IS" BASIS,
     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
     See the License for the specific language governing permissions and
     limitations under the License.
-->

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:template match="/">
        <h1>General report</h1>
        <xsl:apply-templates/>
    </xsl:template>
    <xsl:template match="property-list">
        <p><a name="Total"></a><table>
            <tr bgcolor="#CCCCCC">
            <th colspan="2" align="center">General properties</th>          </tr>
            <xsl:for-each select="property-item">
                <tr>
                <td>
                    <xsl:if test="@name='date'">
                        <strong><xsl:value-of select="./@name"/></strong>
                    </xsl:if>
                    <xsl:if test="@name!='date'">
                        <a href="#{./@name}"><strong><xsl:value-of select="./@name"/></strong></a>
                    </xsl:if>
                </td>
                <td><xsl:value-of select="."/></td>
                </tr>
            </xsl:for-each>
        </table></p>
    </xsl:template>
    <xsl:template match="cfg-list">
        <p><a name="Config"></a><table>
            <tr bgcolor="#CCCCCC">
            <th colspan="2" align="center">Configuration</th></tr>
            <xsl:for-each select="cfg-item">
                <tr>
                <td><strong><xsl:value-of select="./@name"/></strong></td>
                <td><xsl:value-of select="."/></td>
                </tr>
            </xsl:for-each>
        </table></p>
    </xsl:template>
    <xsl:template match="passed-list">
        <p><a name="passed"></a><table>
            <tr bgcolor="#CCCCCC">
            <th colspan="2" align="center">Passed tests</th></tr>
            <xsl:for-each select="list-item">
            <xsl:sort order="ascending" select="."/>
                <tr>
                <td><b><xsl:number value="position()" format="1" /></b></td>
                <td><a href="{.}"><xsl:value-of select="."/></a></td>
                </tr>
            </xsl:for-each>
        </table></p>
    </xsl:template>
    <xsl:template match="failed-list">
        <p><a name="failed"></a><table>
            <tr bgcolor="#CCCCCC">
            <th colspan="2" align="center">Failed tests</th></tr>
            <xsl:for-each select="list-item">
            <xsl:sort order="ascending" select="."/>
                <tr>
                <td><b><xsl:number value="position()" format="1" /></b></td>
                <td><a href="{.}"><xsl:value-of select="."/></a></td>
                </tr>
            </xsl:for-each>
        </table></p>
    </xsl:template>
    <xsl:template match="error-list">
        <p><a name="error"></a><table>
            <tr bgcolor="#CCCCCC">
            <th colspan="2" align="center">Tests with error</th></tr>
            <xsl:for-each select="list-item">
            <xsl:sort order="ascending" select="."/>
                <tr>
                <td><b><xsl:number value="position()" format="1" /></b></td>
                <td><a href="{.}"><xsl:value-of select="."/></a></td>
                </tr>
            </xsl:for-each>
        </table></p>
    </xsl:template>
    <xsl:template match="modeError-list">
        <p><a name="skipped"></a><table>
            <tr bgcolor="#CCCCCC">
            <th colspan="2" align="center">Tests were skipped</th></tr>
            <xsl:for-each select="list-item">
            <xsl:sort order="ascending" select="."/>
                <tr>
                <td><b><xsl:number value="position()" format="1" /></b></td>
                <td><a href="{.}"><xsl:value-of select="."/></a></td>
                </tr>
            </xsl:for-each>
        </table></p>
    </xsl:template>
    <xsl:template match="unknown-list">
        <p><a name="unknown"></a><table>
            <tr bgcolor="#CCCCCC">
            <th colspan="2" align="center">Tests completed with exit code which is not recognized by harness as known test execution code</th></tr>
            <xsl:for-each select="list-item">
            <xsl:sort order="ascending" select="."/>
                <tr>
                <td><b><xsl:number value="position()" format="1" /></b></td>
                <td><a href="{.}"><xsl:value-of select="."/></a></td>
                </tr>
            </xsl:for-each>
        </table></p>
    </xsl:template>
</xsl:stylesheet>