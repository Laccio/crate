.. highlight:: psql
.. _ref-alter-user:

==============
``ALTER USER``
==============

Alter an existing database user.

.. rubric:: Table of contents

.. contents::
   :local:

Synopsis
========

::

    ALTER USER username
      SET ( user_parameter = value [, ...] )


Description
===========

``ALTER USER`` applies a change to existing database users. Only existing
superusers or the user itself have the privilege to alter an existing database
user.


Arguments
=========

``USERNAME``
------------

The name by which the user is identified inside the database.

``SET``
-------

Changes a user parameter to a different value. The following ``user_parameter``
are supported to alter an existing user account:

:password:
  The password as cleartext entered as string literal.

  ``NULL`` removes the password from the user.

.. CAUTION::

    Passwords cannot be set for the ``crate`` superuser.

    For security reasons it is recommended to authenticate as ``crate`` using a
    client certificate.
