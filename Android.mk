# Copyright (C) 2013 The MoKee OpenSource Project
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
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
