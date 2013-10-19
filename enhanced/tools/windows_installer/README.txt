Main script is "setup.nsi".

It assumes that you have got a full HDK located in "./harmony"
within the same directory as the project, and it pulls the
files from there.

It uses helper functions WriteEnvString and AddToPath that are
defined here (http://nsis.sourceforge.net/Path_Manipulation)
You should also put them in the project directory as "WriteEnvStr.nsh"
and "AddToPath.nsh" respectively.


TPE.
