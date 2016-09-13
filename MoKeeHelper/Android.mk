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

LOCAL_PATH:= $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE_TAGS := optional

LOCAL_SRC_FILES := $(call all-java-files-under, src)

LOCAL_JAVA_LIBRARIES := org.apache.http.legacy
LOCAL_STATIC_JAVA_LIBRARIES := android-support-v13 appoffer jpush play volley mokee-support-widget

LOCAL_SRC_FILES := $(call all-subdir-java-files) \

LOCAL_RESOURCE_DIR := $(LOCAL_PATH)/res \
        ../google/google_play_services/libproject/google-play-services_lib/res

LOCAL_PACKAGE_NAME := MoKeeHelper
LOCAL_CERTIFICATE := platform

LOCAL_PROGUARD_FLAG_FILES := proguard.flags

include $(BUILD_PACKAGE)
