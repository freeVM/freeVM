Here's how to edit and publish the website.  Note 
that this assumes you have a working implementation 
of Java and Ant installed.


1) Edit the docs in xdocs/

2) Type "ant" in the root (site/)

3) Look at the changes in the docs/ directory using
   a web browser.

4) Repeat steps 2-4 until happiness and joy achieved.

5) Commit all changes, both docs/ and xdocs/
   
    svn commit

6) ssh to minotaur

7) cd /www/harmony.apache.org

8) execute 
    
    umask 02; svn update

   to update the just-committed docs out of SVN

9) Verify that all went as planned by browsing the live 
    site 

      http://harmony.apache.org

    and look for the changes you made.  Note that it may take up
    to an hour after step (8) is complete before the changes are
    reflected on the live website.


