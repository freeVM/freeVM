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
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0"
    xmlns:lxslt="http://xml.apache.org/xslt">

    <xsl:output method="text"/>

    <xsl:variable name="testsuite.list" select="//testsuite"/>
    <xsl:variable name="testsuite.error.count" select="count($testsuite.list/error)"/>
    <xsl:variable name="testcase.list" select="$testsuite.list/testcase"/>
    <xsl:variable name="testcase.error.list" select="$testcase.list/error"/>
    <xsl:variable name="testcase.failure.list" select="$testcase.list/failure"/>
    <xsl:variable name="totalErrorsAndFailures" select="count($testcase.error.list) + count($testcase.failure.list) + $testsuite.error.count"/>

    <xsl:template match="/" mode="unittests">
            <!-- Unit Tests -->
        <xsl:text>&#10;***********************************************************&#10;</xsl:text>
        <xsl:text>Unit Tests: </xsl:text>(<xsl:value-of select="count($testcase.list)"/>)
        <xsl:text>&#10;</xsl:text>

            <xsl:choose>
                <xsl:when test="count($testsuite.list) = 0">
                     <xsl:text>No Tests Run: This project doesn't have any tests&#10;</xsl:text>
                </xsl:when>

                <xsl:when test="$totalErrorsAndFailures = 0">
                    <xsl:text>All Tests Passed&#10;</xsl:text>
                </xsl:when>
            </xsl:choose>

            <xsl:apply-templates select="$testcase.error.list" mode="unittests"/>
            <xsl:apply-templates select="$testcase.failure.list" mode="unittests"/>

            <xsl:if test="$totalErrorsAndFailures > 0">

                <xsl:text>Unit Test Error Details:	</xsl:text>(<xsl:value-of select="$totalErrorsAndFailures"/>)
                <xsl:text>&#10;</xsl:text>

                <!-- (PENDING) Why doesn't this work if set up as variables up top? -->
                <xsl:call-template name="testdetail">
                    <xsl:with-param name="detailnodes" select="//testsuite/testcase[.//error]"/>
                </xsl:call-template>

                <xsl:call-template name="testdetail">
                    <xsl:with-param name="detailnodes" select="//testsuite/testcase[.//failure]"/>
                </xsl:call-template>
            </xsl:if>
    </xsl:template>

    <!-- UnitTest Errors -->
    <xsl:template match="error" mode="unittests">
        <xsl:if test="position() mod 2 = 0">
            <xsl:attribute name="class">unittests-oddrow</xsl:attribute>
        </xsl:if>

        <xsl:text>error: </xsl:text>
        <xsl:value-of select="../@name"/>
        <xsl:text> </xsl:text>
        <xsl:value-of select="..//..//@name"/>
        <xsl:text>&#10;</xsl:text>
    </xsl:template>

    <!-- UnitTest Failures -->
    <xsl:template match="failure" mode="unittests">
        <xsl:if test="($testsuite.error.count + position()) mod 2 = 0">
            <xsl:attribute name="class">unittests-oddrow</xsl:attribute>
        </xsl:if>

        <xsl:text>failure: </xsl:text>
        <xsl:value-of select="../@name"/>
        <xsl:text> </xsl:text>
        <xsl:value-of select="..//..//@name"/>
        <xsl:text>&#10;</xsl:text>
    </xsl:template>

    <!-- UnitTest Errors And Failures Detail Template -->
    <xsl:template name="testdetail">
        <xsl:param name="detailnodes"/>
        <xsl:for-each select="$detailnodes">
            <xsl:text>Test: </xsl:text>
            <xsl:value-of select="@name"/>
            <xsl:text>Class: </xsl:text>
            <xsl:value-of select="..//@name"/>

            <xsl:if test="error">
                <xsl:call-template name="test-data">
                    <xsl:with-param name="word" select="error"/>
                    <xsl:with-param name="type" select="'error'"/>
                </xsl:call-template>
            </xsl:if>

            <xsl:if test="failure">
                <xsl:call-template name="test-data">
                    <xsl:with-param name="word" select="failure"/>
                    <xsl:with-param name="type" select="'failure'"/>
                </xsl:call-template>
            </xsl:if>

        </xsl:for-each>
    </xsl:template>

    <xsl:template name="test-data">
        <xsl:param name="word"/>
        <xsl:param name="type"/>
        <xsl:call-template name="stack-trace">
            <xsl:with-param name="word" select="$word"/>
            <xsl:with-param name="type" select="$type"/>
        </xsl:call-template>
    </xsl:template>

    <xsl:template name="stack-trace">
        <xsl:param name="word"/>
        <xsl:param name="type"/>
        <xsl:call-template name="br-replace">
            <xsl:with-param name="word" select="$word"/>
            <xsl:with-param name="type" select="$type"/>
            <xsl:with-param name="count" select="0"/>
        </xsl:call-template>
    </xsl:template>

    <xsl:template name="br-replace">
        <xsl:param name="word"/>
        <xsl:param name="type"/>
        <xsl:param name="count"/>
        <xsl:variable name="stackstart"><xsl:text>	at</xsl:text></xsl:variable>
        <xsl:variable name="cr"><xsl:text>
</xsl:text></xsl:variable>
        <xsl:choose>
            <xsl:when test="contains($word,$cr)">
                <xsl:attribute name="class">unittests-<xsl:value-of select="$type"/></xsl:attribute>
                <xsl:if test="$count mod 2 != 0">
                    <xsl:attribute name="bgcolor">#EEEEEE</xsl:attribute>
                </xsl:if>

                <xsl:if test="$count != 0 and starts-with($word,$stackstart)">
                    <xsl:value-of select="substring-before($word,$cr)"/>
                    <xsl:text>&#10;</xsl:text>
                </xsl:if>

                <xsl:if test="$count != 0 and not(starts-with($word,$stackstart))">
                    <xsl:value-of select="substring-before($word,$cr)"/>
                    <xsl:text>&#10;</xsl:text>
                </xsl:if>

                <xsl:if test="$count = 0">
                    <xsl:value-of select="substring-before($word,$cr)"/>
                    <xsl:text>&#10;</xsl:text>
                </xsl:if>
                <xsl:call-template name="br-replace">
                    <xsl:with-param name="word" select="substring-after($word,$cr)"/>
                    <xsl:with-param name="type" select="$type"/>
                    <xsl:with-param name="count" select="$count + 1"/>
                </xsl:call-template>
            </xsl:when>
            <xsl:otherwise>
                <xsl:attribute name="class">unittests-<xsl:value-of select="$type"/></xsl:attribute>
                <xsl:if test="$count mod 2 != 0">
                    <xsl:attribute name="bgcolor">#EEEEEE</xsl:attribute>
                </xsl:if>
                <xsl:value-of select="$word"/>
                <xsl:text>&#10;</xsl:text>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template match="/">
        <xsl:apply-templates select="." mode="unittests"/>
    </xsl:template>
</xsl:stylesheet>
