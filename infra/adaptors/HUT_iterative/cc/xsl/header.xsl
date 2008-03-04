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
    Copyright 2006-2007 The Apache Software Foundation or its licensors, as applicable
     
     Licensed under the Apache License, Version 2.0 (the "License");
     you may not use this file except in compliance with the License.
     You may obtain a copy of the License at
     
         http://www.apache.org/licenses/LICENSE-2.0
     
     Unless required by applicable law or agreed to in writing, software
     distributed under the License is distributed on an "AS IS" BASIS,
     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
     See the License for the specific language governing permissions and
     limitations under the License. 
-->
<xsl:stylesheet
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0"
    xmlns:lxslt="http://xml.apache.org/xslt">

    <xsl:output method="text"/>

    <xsl:template match="/" mode="header">
        <xsl:variable name="modification.list" select="cruisecontrol/modifications/modification"/>

            <xsl:if test="cruisecontrol/build/@error">
                <xsl:text>BUILD FAILED:&#10;	Ant Error Message:&#10;</xsl:text>
                <xsl:value-of select="cruisecontrol/build/@error"/>
		<xsl:text>&#10;</xsl:text>
            </xsl:if>

            <xsl:if test="not (cruisecontrol/build/@error)">
                <xsl:text>BUILD COMPLETE:	</xsl:text>
                    <xsl:value-of select="cruisecontrol/info/property[@name='label']/@value"/>
                <xsl:text>&#10;</xsl:text>
            </xsl:if>

            <xsl:text>&#10;</xsl:text>
            <xsl:text>Date of build:	</xsl:text>
            <xsl:value-of select="cruisecontrol/info/property[@name='builddate']/@value"/>
            <xsl:text>&#10;</xsl:text>

            <xsl:text>Time to build:	</xsl:text>
            <xsl:value-of select="cruisecontrol/build/@time"/>
            <xsl:text>&#10;</xsl:text>

            <xsl:apply-templates select="$modification.list" mode="header">
                <xsl:sort select="date" order="descending" data-type="text" />
            </xsl:apply-templates>
    </xsl:template>

    <!-- Last Modification template -->
    <xsl:template match="modification" mode="header">
        <xsl:if test="position() = 1">
            <xsl:text>Last changed:	</xsl:text>
            <xsl:value-of select="date"/>
            <xsl:text>&#10;</xsl:text>

            <xsl:text>Last log entry:	</xsl:text>
            <xsl:value-of select="comment"/>
            <xsl:text>&#10;</xsl:text>

        </xsl:if>
    </xsl:template>

    <xsl:template match="/">
        <xsl:apply-templates select="." mode="header"/>
    </xsl:template>
</xsl:stylesheet>
