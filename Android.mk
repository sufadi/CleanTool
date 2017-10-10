LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)
LOCAL_STATIC_JAVA_LIBRARIES := android-support-v4
LOCAL_MODULE_TAGS := optional
LOCAL_JAVA_LIBRARIES := framework

LOCAL_STATIC_JAVA_LIBRARIES := rxjava \
                               rxandroid \
                               android-support-annotations \
                               universal-image-loader \
                               android-support-v13

LOCAL_PROGUARD_FLAG_FILES :=proguard.cfg
LOCAL_PROGUARD_ENABLED := disabled

LOCAL_SRC_FILES := $(call all-subdir-java-files)

LOCAL_PACKAGE_NAME := LavaCleanMaster

LOCAL_CERTIFICATE := platform
LOCAL_PRIVILEGED_MODULE := true
include $(BUILD_PACKAGE)

include $(CLEAR_VARS)

LOCAL_PREBUILT_STATIC_JAVA_LIBRARIES := rxjava:libs/rxjava-1.3.0.jar \
                                        rxandroid:libs/rxandroid-0.24.0.jar \
                                        universal-image-loader:libs/universal-image-loader-1.8.6-with-sources.jar \
                                        android-support-v13:libs/android-support-v13.jar

include $(BUILD_MULTI_PREBUILT)

include $(call all-makefiles-under,$(LOCAL_PATH))