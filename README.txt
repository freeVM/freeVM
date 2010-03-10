Here's how to edit and publish the website.  Note 
that this assumes you have a working implementation 
of Java and Ant installed.

1) Checkout a working copy of the site

   svn co https://svn.apache.org/repos/asf/harmony/standard/site/trunk
   cd trunk

2) Edit the docs in xdocs/

3) Type "ant" in the root (trunk/)

4) Look at the changes in the docs/ directory using
   a web browser.

5) Commit all changes, both docs/ and xdocs/
   
   svn commit

6) Review your changes on the staging server

   http://harmony.staging.apache.org

7) Repeat steps 1-6 until happiness and joy achieved.

8) Merge changes into the live website

   svn co https://svn.apache.org/repos/asf/harmony/standard/site/branches/live
   cd live
   svn merge https://svn.apache.org/repos/asf/harmony/standard/site/trunk
   # review changes
   svn ci -m "Merging from trunk."

9) Verify that all went as planned by browsing the live 
    site 

      http://harmony.apache.org

    and look for the changes you made.
