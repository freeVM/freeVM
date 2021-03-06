<?xml version="1.0"?>
<!--********************************************************************************
 * CruiseControl, a Continuous Integration Toolkit
 * Copyright (c) 2001, ThoughtWorks, Inc.
 * 651 W Washington Ave. Suite 600
 * Chicago, IL 60661 USA
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *     + Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *
 *     + Redistributions in binary form must reproduce the above
 *       copyright notice, this list of conditions and the following
 *       disclaimer in the documentation and/or other materials provided
 *       with the distribution.
 *
 *     + Neither the name of ThoughtWorks, Inc., CruiseControl, nor the
 *       names of its contributors may be used to endorse or promote
 *       products derived from this software without specific prior
 *       written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE REGENTS OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 ********************************************************************************-->
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
<xsl:stylesheet
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

    <xsl:output method="text"/>
    <xsl:variable name="modification.list" select="cruisecontrol/modifications/modification"/>
    <xsl:variable name="urlroot" select='"/cruisecontrol/buildresults/"'/>


    <xsl:template match="/" mode="modifications">
            <!-- Modifications -->
            <xsl:text>&#10;***********************************************************&#10;</xsl:text>
            <xsl:text>Modifications since last successful build:	</xsl:text>(<xsl:value-of select="count($modification.list)"/>)
            <xsl:text>&#10;</xsl:text>

            <xsl:apply-templates select="$modification.list" mode="modifications">
                <xsl:sort select="date" order="descending" data-type="text" />
            </xsl:apply-templates>

    </xsl:template>

    <!-- user defined variables for logging into ClearQuest -->
    <xsl:variable name="cqserver">localhost</xsl:variable>
    <xsl:variable name="cqschema">2003.06.00</xsl:variable>
    <xsl:variable name="cqdb">RBPRO</xsl:variable>
    <xsl:variable name="cqlogin">admin</xsl:variable>
    <xsl:variable name="cqpasswd">password</xsl:variable>

    <xsl:template match="modification[@type='activity']" mode="modifications">
        <xsl:variable name="cqrecurl">http://<xsl:value-of select="$cqserver"/>/cqweb/main?command=GenerateMainFrame&amp;service=CQ&amp;schema=<xsl:value-of select="$cqschema"/>&amp;contextid=<xsl:value-of select="$cqdb"/>&amp;entityID=<xsl:value-of select="revision"/>&amp;entityDefName=<xsl:value-of select="crmtype"/>&amp;username=<xsl:value-of select="$cqlogin"/>&amp;password=<xsl:value-of select="$cqpasswd"/></xsl:variable>
            <xsl:attribute name="class">changelists-evenrow</xsl:attribute>
            <xsl:text> </xsl:text>
	    <a href="{$cqrecurl}" target="_blank"><xsl:value-of select="revision"/></a>
            <xsl:text> </xsl:text>
            <xsl:value-of select="crmtype"/>
            <xsl:text> </xsl:text>
            <xsl:value-of select="user"/>
            <xsl:text> </xsl:text>
            <xsl:value-of select="comment"/>
            <xsl:text>&#10;</xsl:text>
    </xsl:template>

    <xsl:template match="modification[@type='contributor']" mode="modifications">
        <xsl:variable name="cqrecurl">http://<xsl:value-of select="$cqserver"/>/cqweb/main?command=GenerateMainFrame&amp;service=CQ&amp;schema=<xsl:value-of select="$cqschema"/>&amp;contextid=<xsl:value-of select="$cqdb"/>&amp;entityID=<xsl:value-of select="revision"/>&amp;entityDefName=<xsl:value-of select="crmtype"/>&amp;username=<xsl:value-of select="$cqlogin"/>&amp;password=<xsl:value-of select="$cqpasswd"/></xsl:variable>
            <xsl:attribute name="class">changelists-oddrow</xsl:attribute>
		<a href="{$cqrecurl}" target="_blank"><xsl:value-of select="revision"/></a>
            <xsl:text> </xsl:text>
                <xsl:value-of select="crmtype"/>
            <xsl:text> </xsl:text>
                <xsl:value-of select="user"/>
            <xsl:text> </xsl:text>
                <xsl:value-of select="comment"/>
            <xsl:text>&#10;</xsl:text>
    </xsl:template>


    <xsl:template match="modification[@type='p4']" mode="modifications">
            <xsl:if test="position() mod 2=0">
                <xsl:attribute name="class">changelists-oddrow</xsl:attribute>
            </xsl:if>
            <xsl:if test="position() mod 2!=0">
                <xsl:attribute name="class">changelists-evenrow</xsl:attribute>
            </xsl:if>
            <xsl:value-of select="revision"/>
            <xsl:text> </xsl:text>
            <xsl:value-of select="user"/>
            <xsl:text> </xsl:text>
            <xsl:value-of select="client"/>
            <xsl:text> </xsl:text>
            <xsl:value-of select="date"/>
            <xsl:text> </xsl:text>
            <xsl:variable name="convertedComment">
                <xsl:call-template name="newlineToHTML">
                    <xsl:with-param name="line">
                        <xsl:value-of select="comment"/>
                    </xsl:with-param>
                </xsl:call-template>
            </xsl:variable>

            <xsl:copy-of select="$convertedComment"/>
            <xsl:text>&#10;</xsl:text>

        <xsl:if test="count(file) > 0">
                <xsl:if test="position() mod 2=0">
                    <xsl:attribute name="class">changelists-oddrow</xsl:attribute>
                </xsl:if>

                <xsl:if test="position() mod 2!=0">
                    <xsl:attribute name="class">changelists-evenrow</xsl:attribute>
                </xsl:if>

                <xsl:text>Files affected by this changelist: </xsl:text>
                (<xsl:value-of select="count(file)"/>)
                <xsl:text>&#10;</xsl:text>
                <xsl:apply-templates select="file" mode="modifications"/>
        </xsl:if>
    </xsl:template>

    <!-- used by P4 -->
    <xsl:template match="file" mode="modifications">
            <xsl:if test="position() mod 2=0">
                <xsl:attribute name="class">changelists-file-oddrow</xsl:attribute>
            </xsl:if>

            <xsl:if test="position() mod 2!=0">
                <xsl:attribute name="class">changelists-file-evenrow</xsl:attribute>
            </xsl:if>

            <xsl:value-of select="@action"/>
            <xsl:text> </xsl:text>
            <xsl:value-of select="filename"/>
            <xsl:text> </xsl:text>
            <xsl:value-of select="revision"/>
            <xsl:text> </xsl:text>
    </xsl:template>

    <!-- Modifications template for other SourceControls -->
    <xsl:template match="modification[file][@type!='p4']" mode="modifications">
            <xsl:if test="position() mod 2=0">
                <xsl:attribute name="class">modifications-oddrow</xsl:attribute>
            </xsl:if>

            <xsl:if test="position() mod 2!=0">
                <xsl:attribute name="class">modifications-evenrow</xsl:attribute>
            </xsl:if>

            <xsl:value-of select="file/@action"/>
            <xsl:text> </xsl:text>
            <xsl:value-of select="user"/>
            <xsl:text> </xsl:text>
            <xsl:if test="file/project">
                <xsl:value-of select="file/project"/>
                <xsl:value-of select="'/'"/>
            </xsl:if>
            <xsl:text> </xsl:text>
            <xsl:value-of select="file/filename"/>
            <xsl:text> </xsl:text>
            <xsl:value-of select="date"/>
            <xsl:text> </xsl:text>
            <xsl:variable name="convertedComment">
                <xsl:call-template name="newlineToHTML">
                    <xsl:with-param name="line">
                        <xsl:value-of select="comment"/>
                    </xsl:with-param>
                </xsl:call-template>
            </xsl:variable>
            <xsl:copy-of select="$convertedComment"/>
            <xsl:text>&#10;</xsl:text>
    </xsl:template>

    <xsl:template match="modification[file][@type='buildstatus']" mode="modifications">
            <xsl:if test="position() mod 2=0">
                <xsl:attribute name="class">modifications-oddrow</xsl:attribute>
            </xsl:if>

            <xsl:if test="position() mod 2!=0">
                <xsl:attribute name="class">modifications-evenrow</xsl:attribute>
            </xsl:if>

            <xsl:value-of select="file/@action"/>
            <xsl:text> </xsl:text>

            <xsl:value-of select="user"/>
            <xsl:text> </xsl:text>

            <xsl:if test="file/project">
                <xsl:value-of select="file/project"/>
                <xsl:value-of select="'/'"/>
            </xsl:if>

            <xsl:for-each select="file/filename">
                <xsl:variable name="thefile" select="substring(current(),1,string-length(current())-4)"/>
                <xsl:variable name="theproject" select="../../comment"/>
                <a href="{$urlroot}{$theproject}?log={$thefile}"><xsl:copy-of select="$thefile"/></a>
                <xsl:text> </xsl:text>
            </xsl:for-each>

            <xsl:value-of select="date"/>
            <xsl:variable name="convertedComment">
                <xsl:call-template name="newlineToHTML">
                    <xsl:with-param name="line">
                        <xsl:value-of select="comment"/>
                    </xsl:with-param>
                </xsl:call-template>
            </xsl:variable>
            <xsl:copy-of select="$convertedComment"/>
            <xsl:text>&#10;</xsl:text>
    </xsl:template>

    <!-- Up to version 2.1.6 the modification set format did not
         include the file node -->
    <xsl:template match="modification" mode="modifications">
            <xsl:if test="position() mod 2=0">
                <xsl:attribute name="class">modifications-oddrow</xsl:attribute>
            </xsl:if>
            <xsl:if test="position() mod 2!=0">
                <xsl:attribute name="class">modifications-evenrow</xsl:attribute>
            </xsl:if>

            <xsl:value-of select="@type"/>
            <xsl:text> </xsl:text>
            <xsl:value-of select="user"/>
            <xsl:text> </xsl:text>
            <xsl:if test="project">
                <xsl:value-of select="project"/>
                <xsl:value-of select="'/'"/>
            </xsl:if>
            <xsl:value-of select="filename"/>
            <xsl:text> </xsl:text>
            <xsl:value-of select="date"/>
            <xsl:text> </xsl:text>
            <xsl:variable name="convertedComment">
                <xsl:call-template name="newlineToHTML">
                    <xsl:with-param name="line">
                        <xsl:value-of select="comment"/>
                    </xsl:with-param>
                </xsl:call-template>
            </xsl:variable>
            <xsl:copy-of select="$convertedComment"/>
            <xsl:text>&#10;</xsl:text>
    </xsl:template>

    <!-- Used by CM Synergy -->
    <xsl:template match="modification[@type='ccmtask']" mode="modifications">
        <tr>
            <td class="modifications-sectionheader">Task</td>
            <td class="modifications-sectionheader">Owner</td>
            <td class="modifications-sectionheader">Release</td>
            <td class="modifications-sectionheader">Change Request(s)</td>
            <td class="modifications-sectionheader">Completion Date</td>
            <td class="modifications-sectionheader">Synopsis</td>
        </tr>
        <tr valign="top">
            <xsl:if test="position() mod 2=0">
                <xsl:attribute name="class">changelists-oddrow</xsl:attribute>
            </xsl:if>
            <xsl:if test="position() mod 2!=0">
                <xsl:attribute name="class">changelists-evenrow</xsl:attribute>
            </xsl:if>
            <td class="modifications-data">
                <b><xsl:copy-of select="task"/></b>
            </td>
            <td class="modifications-data">
                <xsl:value-of select="user"/>
            </td>
            <td class="modifications-data">
                <xsl:value-of select="revision"/>
            </td>
            <td class="modifications-data">
                <xsl:apply-templates select="ccmcr" mode="modifications"/>
            </td>
            <td class="modifications-data">
                <xsl:value-of select="date"/>
            </td>
            <td class="modifications-data">
                <xsl:variable name="convertedComment">
                    <xsl:call-template name="newlineToHTML">
                        <xsl:with-param name="line">
                            <xsl:value-of select="comment"/>
                        </xsl:with-param>
                    </xsl:call-template>
                </xsl:variable>
                <xsl:copy-of select="$convertedComment"/>
            </td>
        </tr>
        <xsl:if test="count(ccmobject) > 0">
            <tr valign="top">
                <xsl:if test="position() mod 2=0">
                    <xsl:attribute name="class">changelists-oddrow</xsl:attribute>
                </xsl:if>
                <xsl:if test="position() mod 2!=0">
                    <xsl:attribute name="class">changelists-evenrow</xsl:attribute>
                </xsl:if>
                <td class="modifications-data" colspan="6">
                    <table align="right" cellpadding="1" cellspacing="1" border="0" width="95%">
                        <tr>
                            <td class="changelists-file-header" colspan="7">
                                &#160;Objects associated with this task:&#160;
                                (<xsl:value-of select="count(ccmobject)"/>)
                            </td>
                        </tr>
                        <tr>
                            <td class="changelists-file-header">Object</td>
                            <td class="changelists-file-header">Version</td>
                            <td class="changelists-file-header">Type</td>
                            <td class="changelists-file-header">Instance</td>
                            <td class="changelists-file-header">Project</td>
                            <td class="changelists-file-header">Comment</td>
                        </tr>
                        <xsl:apply-templates select="ccmobject" mode="modifications"/>
                    </table>
                </td>
            </tr>
        </xsl:if>
    </xsl:template>
    <xsl:template match="ccmobject" mode="modifications">
        <tr valign="top" >
            <xsl:if test="position() mod 2=0">
                <xsl:attribute name="class">changelists-file-oddrow</xsl:attribute>
            </xsl:if>
            <xsl:if test="position() mod 2!=0">
                <xsl:attribute name="class">changelists-file-evenrow</xsl:attribute>
            </xsl:if>
            <td class="modifications-data"><b><xsl:value-of select="name"/></b></td>
            <td class="modifications-data"><xsl:value-of select="version"/></td>
            <td class="modifications-data"><xsl:value-of select="type"/></td>
            <td class="modifications-data"><xsl:value-of select="instance"/></td>
            <td class="modifications-data"><xsl:value-of select="project"/></td>
            <td class="modifications-data">
                <xsl:variable name="convertedComment">
                    <xsl:call-template name="newlineToHTML">
                        <xsl:with-param name="line">
                            <xsl:value-of select="comment"/>
                        </xsl:with-param>
                    </xsl:call-template>
                </xsl:variable>
                <xsl:copy-of select="$convertedComment"/>
            </td>
        </tr>
    </xsl:template>
    <xsl:template match="ccmcr" mode="modifications">
        <xsl:if test="position() != 1">
            ,
        </xsl:if>
        <xsl:copy-of select="*"/>
    </xsl:template>

    <xsl:template match="/">
        <xsl:apply-templates select="." mode="modifications"/>
    </xsl:template>

    <xsl:template name="newlineToHTML">
        <xsl:param name="line"/>
        <xsl:choose>
            <xsl:when test="contains($line, '&#xA;')">
                <xsl:value-of select="substring-before($line, '&#xA;')"/>
                <br/>
                <xsl:call-template name="newlineToHTML">
                    <xsl:with-param name="line">
                        <xsl:value-of select="substring-after($line, '&#xA;')"/>
                    </xsl:with-param>
                </xsl:call-template>
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="$line"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
</xsl:stylesheet>
