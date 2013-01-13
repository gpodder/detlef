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


License
=======

Detlef is licensed under the terms of the GPLv2.


Contributing
============

In case you'd like to contribute patches (or if you're just curious
about Detlef's innards), the source code is available from

https://github.com/gpodder/detlef
git://github.com/gpodder/detlef.git

Please submit patches as github pull requests.


Contact
=======

We can be best reached on the gpodder mailing list:

http://wiki.gpodder.org/wiki/Mailing_List
