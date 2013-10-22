/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

Name "Apache Harmony"
SetCompressor lzma

# Defines
!define REGKEY "SOFTWARE\$(^Name)"
!define VERSION "Snapshot Version"
!define COMPANY "Apache Software Foundation"
!define URL http://incubator.apache.org/harmony
!define JAVA_HOME_VAR "JAVA_HOME"

# MUI defines
!define MUI_ICON "harmony.ico"
!define MUI_FINISHPAGE_NOAUTOCLOSE
!define MUI_LICENSEPAGE_CHECKBOX
!define MUI_STARTMENUPAGE_REGISTRY_ROOT HKLM
!define MUI_STARTMENUPAGE_NODISABLE
!define MUI_STARTMENUPAGE_REGISTRY_KEY "Software\Apache Harmony"
!define MUI_STARTMENUPAGE_REGISTRY_VALUENAME StartMenuGroup
!define MUI_STARTMENUPAGE_DEFAULT_FOLDER "Apache Harmony"
!define MUI_FINISHPAGE_LINK "Visit the Apache Harmony website."
!define MUI_FINISHPAGE_LINK_LOCATION "http://incubator.apache.org/harmony"
#!define MUI_FINISHPAGE_SHOWREADME $INSTDIR\README.txt
!define MUI_UNICON "${NSISDIR}\Contrib\Graphics\Icons\orange-uninstall.ico"
!define MUI_UNFINISHPAGE_NOAUTOCLOSE

# Included files
!include Sections.nsh
!include MUI.nsh
!include "WriteEnvStr.nsh"
!include "AddToPath.nsh"
    
# Reserved Files
ReserveFile "${NSISDIR}\Plugins\AdvSplash.dll"

# Variables
Var StartMenuGroup

# Installer pages
!insertmacro MUI_PAGE_WELCOME
!insertmacro MUI_PAGE_LICENSE harmony\LICENSE
!insertmacro MUI_PAGE_COMPONENTS
!insertmacro MUI_PAGE_DIRECTORY
!insertmacro MUI_PAGE_STARTMENU Application $StartMenuGroup
!insertmacro MUI_PAGE_INSTFILES
!insertmacro MUI_PAGE_FINISH
!insertmacro MUI_UNPAGE_CONFIRM
!insertmacro MUI_UNPAGE_INSTFILES

# Installer languages
!insertmacro MUI_LANGUAGE English

# Installer attributes
OutFile setup.exe
InstallDir "$PROGRAMFILES\Apache Harmony"
CRCCheck on
XPStyle on
ShowInstDetails hide
VIProductVersion 0.0.0.0
VIAddVersionKey ProductName "Apache Harmony"
VIAddVersionKey ProductVersion "${VERSION}"
VIAddVersionKey CompanyName "${COMPANY}"
VIAddVersionKey CompanyWebsite "${URL}"
VIAddVersionKey FileVersion ""
VIAddVersionKey FileDescription ""
VIAddVersionKey LegalCopyright ""
InstallDirRegKey HKLM "${REGKEY}" Path
ShowUninstDetails show

# These files are required -- they are license files etc. that are always installed
Section "-Required Files" SEC0000
    SetOverwrite on

    SetOutPath $INSTDIR
    File harmony\COPYRIGHT
    File harmony\INCUBATOR_NOTICE.txt
    File harmony\LICENSE
    File harmony\NOTICE
    File harmony\THIRD_PARTY_NOTICES.txt

    MessageBox MB_OK "Apache Harmony is an effort undergoing incubation \
at the Apache Software Foundation (ASF). Incubation is \
required of all newly accepted projects until a further \
review indicates that the infrastructure, communications, \
and decision making process have stabilized in a manner \
consistent with other successful ASF projects. While \
incubation status is not necessarily a reflection of the \
completeness or stability of the code, it does indicate \
that the project has yet to be fully endorsed by the ASF."

    WriteRegStr HKLM "${REGKEY}\Components" "Required Files" 1
SectionEnd

###################
##   JRE definition
###################
SectionGroup "Runtime Environment" SECGRP0000

# These files are not optional -- they are part of the JRE that is always installed
    Section "Base Runtime Files (required)" SEC0001
        SectionIn RO
        SetOverwrite on

        SetDetailsPrint textOnly
        DetailPrint "Installing runtime environment..."
        SetDetailsPrint listonly

        #  Take all of jre/bin, recursively
        SetOutPath $INSTDIR\jdk\jre\bin
        File /r harmony\jdk\jre\bin\*

        #  Take all files in jre/lib except source JARs
        SetOutPath $INSTDIR\jdk\jre\lib
        File /r /x *-src.jar /x *-stubs.jar harmony\jdk\jre\lib\*
         
        WriteRegStr HKLM "${REGKEY}\Components" "Required Runtime Files" 1
    SectionEnd
    LangString DESC_SEC0001 ${LANG_ENGLISH} "The basic runtime environment."

SectionGroupEnd


###################
##   JDK definition
###################
SectionGroup "Development Environment" SECGRP0001

    Section "Java development environment" SEC0003
        SetOverwrite on

        SetDetailsPrint textOnly
        DetailPrint "Installing Java development environment..."
        SetDetailsPrint listonly

        # JDK include files
        SetOutPath $INSTDIR\jdk\include
        File /r harmony\jdk\include\*

        # JDK lib files
        SetOutPath $INSTDIR\jdk\lib
        File /r harmony\jdk\lib\*
        
        # Compile-against stub JARs
        SetOutPath $INSTDIR\jdk\jre\lib\boot
        File harmony\jdk\jre\lib\boot\*-stubs.jar
        
        # Doc dir
        SetOutPath $INSTDIR\jdk\jre\doc
        File /r harmony\jdk\jre\doc\*

        WriteRegStr HKLM "${REGKEY}\Components" "Java development environment" 1
    SectionEnd
    LangString DESC_SEC0003 ${LANG_ENGLISH} "Files required for developing Java programs."

    Section "Class library source files" SEC0002
        SetOverwrite on

        SetDetailsPrint textOnly
        DetailPrint "Installing source files..."
        SetDetailsPrint listonly

        SetOutPath $INSTDIR\jdk\jre\lib\boot
        File harmony\jdk\jre\lib\boot\*-src.jar

        WriteRegStr HKLM "${REGKEY}\Components" "Runtime environment source files" 1
    SectionEnd
    LangString DESC_SEC0002 ${LANG_ENGLISH} "Source files for runtime class libraries."


###################
##   HDK definition
###################
    Section /o "Apache Harmony development environment" SEC0004
        SetOverwrite on
        
        SetDetailsPrint textOnly
        DetailPrint "Installing Apache Harmony development environment..."
        SetDetailsPrint listonly

        #  Al files for HDK dirs
        SetOutPath $INSTDIR\build
        File /r harmony\build\*

        SetOutPath $INSTDIR\include
        File /r harmony\include\*

        SetOutPath $INSTDIR\lib
        File /r harmony\lib\*

        WriteRegStr HKLM "${REGKEY}\Components" "Apache Harmony development environment" 1
    SectionEnd
    LangString DESC_SEC0004 ${LANG_ENGLISH} "Files required for developing Apache Harmony itself."

SectionGroupEnd

###################
##   Additional install tasks
###################
Section -post SEC0005
#  Here we install ourselves into the JAVA_HOME slot
    Push ${JAVA_HOME_VAR}
    Push $INSTDIR
    Call WriteEnvStr

# Add our bin to the path
    Push $INSTDIR\jre\bin
    Call AddToPath

# Register our uninstaller with windows
    WriteRegStr HKLM "${REGKEY}" Path $INSTDIR
    WriteUninstaller $INSTDIR\uninstall.exe

    !insertmacro MUI_STARTMENU_WRITE_BEGIN Application
    SetOutPath $SMPROGRAMS\$StartMenuGroup
    CreateShortcut "$SMPROGRAMS\$StartMenuGroup\Uninstall $(^Name).lnk" $INSTDIR\uninstall.exe
    !insertmacro MUI_STARTMENU_WRITE_END

    WriteRegStr HKLM "SOFTWARE\Microsoft\Windows\CurrentVersion\Uninstall\$(^Name)" DisplayName "$(^Name)"
    WriteRegStr HKLM "SOFTWARE\Microsoft\Windows\CurrentVersion\Uninstall\$(^Name)" DisplayVersion "${VERSION}"
    WriteRegStr HKLM "SOFTWARE\Microsoft\Windows\CurrentVersion\Uninstall\$(^Name)" Publisher "${COMPANY}"
    WriteRegStr HKLM "SOFTWARE\Microsoft\Windows\CurrentVersion\Uninstall\$(^Name)" URLInfoAbout "${URL}"
    WriteRegStr HKLM "SOFTWARE\Microsoft\Windows\CurrentVersion\Uninstall\$(^Name)" DisplayIcon $INSTDIR\uninstall.exe
    WriteRegStr HKLM "SOFTWARE\Microsoft\Windows\CurrentVersion\Uninstall\$(^Name)" UninstallString $INSTDIR\uninstall.exe
    WriteRegDWORD HKLM "SOFTWARE\Microsoft\Windows\CurrentVersion\Uninstall\$(^Name)" NoModify 1
    WriteRegDWORD HKLM "SOFTWARE\Microsoft\Windows\CurrentVersion\Uninstall\$(^Name)" NoRepair 1
SectionEnd


!insertmacro MUI_FUNCTION_DESCRIPTION_BEGIN
  !insertmacro MUI_DESCRIPTION_TEXT ${SEC0001} $(DESC_SEC0001)
  !insertmacro MUI_DESCRIPTION_TEXT ${SEC0002} $(DESC_SEC0002)
  !insertmacro MUI_DESCRIPTION_TEXT ${SEC0003} $(DESC_SEC0003)
  !insertmacro MUI_DESCRIPTION_TEXT ${SEC0004} $(DESC_SEC0004)
!insertmacro MUI_FUNCTION_DESCRIPTION_END


# Macro for selecting uninstaller sections
!macro SELECT_UNSECTION SECTION_NAME UNSECTION_ID
    Push $R0
    ReadRegStr $R0 HKLM "${REGKEY}\Components" "${SECTION_NAME}"
    StrCmp $R0 1 0 next${UNSECTION_ID}
    !insertmacro SelectSection "${UNSECTION_ID}"
    GoTo done${UNSECTION_ID}
next${UNSECTION_ID}:
    !insertmacro UnselectSection "${UNSECTION_ID}"
done${UNSECTION_ID}:
    Pop $R0
!macroend

# Uninstaller sections
Section /o "un.Apache Harmony development environment" UNSEC0004
    RmDir /r /REBOOTOK $INSTDIR\build
    RmDir /r /REBOOTOK $INSTDIR\include
    RmDir /r /REBOOTOK $INSTDIR\lib

    DeleteRegValue HKLM "${REGKEY}\Components" "Apache Harmony development environment"
SectionEnd

Section /o "un.Java development environment" UNSEC0003
    Delete /REBOOTOK $INSTDIR\jdk\jre\lib\boot\*-stubs.jar
    RmDir /r /REBOOTOK $INSTDIR\jdk\jre\doc
    RmDir /r /REBOOTOK $INSTDIR\jdk\lib
    RmDir /r /REBOOTOK $INSTDIR\jdk\include

    DeleteRegValue HKLM "${REGKEY}\Components" "Java development environment"
SectionEnd

Section /o "un.Required Runtime Files" UNSEC0002
    Delete /REBOOTOK $INSTDIR\jdk\jre\lib\boot\*-src.jar

    DeleteRegValue HKLM "${REGKEY}\Components" "Runtime environment source files"
SectionEnd

Section /o "un.Required Runtime Files" UNSEC0001
    RmDir /r /REBOOTOK $INSTDIR\jdk\jre\bin
    RmDir /r /REBOOTOK $INSTDIR\jdk\jre\lib

    DeleteRegValue HKLM "${REGKEY}\Components" "Required Runtime Files"
SectionEnd

Section /o "un.Required Files" UNSEC0000
    Delete /REBOOTOK $INSTDIR\COPYRIGHT
    Delete /REBOOTOK $INSTDIR\INCUBATOR_NOTICE.txt
    Delete /REBOOTOK $INSTDIR\LICENSE
    Delete /REBOOTOK $INSTDIR\NOTICE
    Delete /REBOOTOK $INSTDIR\THIRD_PARTY_NOTICES.txt

    DeleteRegValue HKLM "${REGKEY}\Components" "Required Files"
SectionEnd

Section un.post UNSEC0005
    # remove the JAVA_HOME variable slam
    Push ${JAVA_HOME_VAR}
    Call un.DeleteEnvStr

#TODO remove ourselves from PATH

    DeleteRegKey HKLM "SOFTWARE\Microsoft\Windows\CurrentVersion\Uninstall\$(^Name)"
    Delete /REBOOTOK "$SMPROGRAMS\$StartMenuGroup\Uninstall $(^Name).lnk"
    Delete /REBOOTOK $INSTDIR\uninstall.exe
    DeleteRegValue HKLM "${REGKEY}" StartMenuGroup
    DeleteRegValue HKLM "${REGKEY}" Path
    DeleteRegKey /IfEmpty HKLM "${REGKEY}\Components"
    DeleteRegKey /IfEmpty HKLM "${REGKEY}"
    RmDir /REBOOTOK $SMPROGRAMS\$StartMenuGroup
    RmDir /REBOOTOK $INSTDIR
SectionEnd

# Installer functions
Function .onInit
    InitPluginsDir
    Push $R1
    File /oname=$PLUGINSDIR\spltmp.bmp splash.bmp
    advsplash::show 1000 1000 1000 -1 $PLUGINSDIR\spltmp
    Pop $R1
    Pop $R1
FunctionEnd

# Uninstaller functions
Function un.onInit
    ReadRegStr $INSTDIR HKLM "${REGKEY}" Path
    ReadRegStr $StartMenuGroup HKLM "${REGKEY}" StartMenuGroup
    !insertmacro SELECT_UNSECTION "Required Files" ${UNSEC0000}
    !insertmacro SELECT_UNSECTION "Required Runtime Files" ${UNSEC0001}
    !insertmacro SELECT_UNSECTION "Runtime environment source files" ${UNSEC0002}
    !insertmacro SELECT_UNSECTION "Java development environment" ${UNSEC0003}
    !insertmacro SELECT_UNSECTION "Apache Harmony development environment" ${UNSEC0004}
FunctionEnd

