Detlef Gpodderson
=================

An Android podcast application for gpodder.net.


How To Build
============

    $ git clone git://github.com/gpodder/detlef.git
    $ cd detlef
    $ git submodule init
    $ git submodule update
    $ cd drag-sort-listview
    $ android update project -t "android-16" -p . --subprojects
    $ cd ../detlef
    $ android update project -t "android-16" -p . --subprojects
    $ ant debug
    $ ant installd
