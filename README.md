Detlef Gpodderson
=================

An Android podcast application for gpodder.net.

How To Build
============

To build detlef, you will need ant and a working installation
of the Android 16 SDK.

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

How To Test
===========

    $ cd test-detlef
    $ ant clean debug install test
    $ cd test-detlef-ui
    $ ant clean debug install test

License
=======

Detlef is licensed under the terms of the GPLv2.

Contributing
============

In case you'd like to contribute patches (or if you're just curious
about Detlef's innards), the source code is available from

https://github.com/gpodder/detlef <br/>
git://github.com/gpodder/detlef.git

The coding style should follow the conventions of the surrounding
code. Enable the provided astyle pre-commit hook:

    $ cp etc/pre-commit .git/hooks/pre-commit

Please submit patches as github pull requests.

Contact
=======

We can be reached on the [gpodder mailing list](http://wiki.gpodder.org/wiki/Mailing_List).
Please report any bugs at our [github issue tracker](https://github.com/gpodder/detlef/issues).
