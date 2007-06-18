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
        <h1>Test report</h1>
        <xsl:apply-templates/>
    </xsl:template>
    <xsl:template match="property-list">
        <p><table>
            <tr bgcolor="#CCCCCC">
            <th colspan="2" align="center">Results</th></tr>
            <xsl:for-each select="property-item">
                <tr><td valign="top"><strong><xsl:value-of select="./@name"/></strong></td>
                <td>
                <xsl:variable name="htmlBR">
                    <xsl:element name="br"/>
                    <xsl:text>&#xA;</xsl:text>
                </xsl:variable>
                <xsl:choose>
                    <xsl:when test="@name='cmd'">
                        <xsl:variable name="tmp">
                            <xsl:call-template name="replace">
                                <xsl:with-param name="str" select="."/>
                                <xsl:with-param name="from" select="'&#xA;'"/>
                                <xsl:with-param name="to" select="$htmlBR"/>
                            </xsl:call-template>
                        </xsl:variable>
                        <xsl:call-template name="replace">
                            <xsl:with-param name="str" select="$tmp"/>
                            <xsl:with-param name="from" select="'&#x3b;'"/>
                            <xsl:with-param name="to" select="'&#x3b;&#x20;'"/>
                        </xsl:call-template>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:call-template name="replace">
                            <xsl:with-param name="str" select="."/>
                            <xsl:with-param name="from" select="'&#xA;'"/>
                            <xsl:with-param name="to" select="$htmlBR"/>
                        </xsl:call-template>
                    </xsl:otherwise>
                </xsl:choose>
                </td></tr>
            </xsl:for-each>
        </table></p>
    </xsl:template>
    <xsl:template match="info-list">
        <p><table>
            <tr bgcolor="#CCCCCC">
            <th colspan="2" align="center">Additional information</th></tr>
            <xsl:for-each select="info-item">
                <tr><td valign="top"><strong><xsl:value-of select="./@name"/></strong></td>
                <td><a href="{.}"><xsl:value-of select="."/></a></td>
                </tr>
            </xsl:for-each>
        </table></p>
    </xsl:template>

    <xsl:template name="replace">
        <xsl:param name="str"/>
        <xsl:param name="from"/>
        <xsl:param name="to"/>

        <xsl:choose>
            <xsl:when test="contains($str, $from)">
                <xsl:value-of select="substring-before($str, $from)"/>
                <xsl:copy-of select="$to"/>
                <xsl:call-template name="replace">
                    <xsl:with-param name="str" select="substring-after($str, $from)"/>
                    <xsl:with-param name="from" select="$from"/>
                    <xsl:with-param name="to" select="$to"/>
                </xsl:call-template>
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="$str"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
</xsl:stylesheet>