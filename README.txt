Here's how to edit and publish the website.  Note 
that this assumes you have a working implementation 
of Java and Ant installed.


1) **Temporarily** update the link to the CSS so you can
    preview the site in a local browser:
    1) Go to line 250 in file xdocs/stylesheets/site.vsl
    2) Edit the path to site.css.  Specify an absolute path. 
       e.g. from

       <link rel="Stylesheet" type="text/css" href="/harmony/site.css"/>

       to

       <link rel="Stylesheet" type="text/css" href="/home/fred/site/docs/site.css"/>

2) Edit the docs in xdocs/

3) Type "ant" in the root (site/)

4) Look at the changes in the docs/ directory using
   a web browser.

5) Repeat steps 2-4 until happiness and joy achieved.

6) Revert the temporary CSS changes made in step (1) above.

7) Commit all changes, both docs/ and xdocs/
   
    svn commit

8) ssh to minotaur

9) cd /www/incubator.apache.org/harmony

10) execute 
    
     `cat UPDATE` 

   to update the just-committed docs out of SVN

11) Verify that all went as planned by browsing the live 
    site 

      http://incubator.apache.org/harmony/

    and look for the changes you made.  Note that it may take up
    to an hour after step (10) is complete before the changes are
    reflected on the live website.


