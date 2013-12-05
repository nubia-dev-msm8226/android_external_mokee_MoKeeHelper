#
# Copyright (C) 2014 The MoKee OpenSource Project
#
# This program is free software: you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation, either version 3 of the License, or
# (at your option) any later version.
#
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License
# along with this program.  If not, see <http://www.gnu.org/licenses/>.
#

my_path := $(call my-dir)

ifdef MOKEEHELPER_EMBEDDED
MOKEEHELPER_PACKAGE := com.android.settings
else
ifeq ($(MOKEEHELPER_PACKAGE),)
MOKEEHELPER_PACKAGE := com.mokee.helper
endif
include $(my_path)/MoKeeHelper/Android.mk
endif


LOCAL_PATH := $(my_path)
include $(CLEAR_VARS)

ifdef MOKEEHELPER_PACKAGE_PREFIX
  LOCAL_CFLAGS += -DREQUESTOR_PREFIX=\"$(MOKEEHELPER_PACKAGE_PREFIX)\"
endif

ifdef MOKEEHELPER_EMBEDDED
  LOCAL_CFLAGS += -DMOKEEHELPER_EMBEDDED
endif
