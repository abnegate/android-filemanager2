# Sets the minimum version of CMake required to build your native library.
# This ensures that a certain set of CMake features is available to
# your build.

cmake_minimum_required(VERSION 3.4.1)

# Specifies a library name, specifies whether the library is STATIC or
# SHARED, and provides relative paths to the source code. You can
# define multiple libraries by adding multiple add.library() commands,
# and CMake builds them for you. When you build your app, Gradle
# automatically packages shared libraries with your APK.

# Specifies the name of the library.
# Sets the library as a shared library.
# Provides a relative path to your source file(s).
add_library( io-lib
             SHARED
             src/main/jni/io-lib.c )

# Specifies a path to native header files.
include_directories(src/main/cpp/include/)


# Defines the name of the path variable that stores the location of the NDK library.
# Specifies the name of the NDK library that CMake needs to locate.
find_library( log-lib
              log )

# Links your native library against one or more other native libraries.
# Specifies the target library.
# Links the log library to the target library.
target_link_libraries( io-lib
                       ${log-lib} )

# The following command tells CMake to build android_native_app_glue.c
# which manages NativeActivity lifecycle events and touch input
# into a static library and links it to native-lib
add_library( app-glue
             STATIC
             ${ANDROID_NDK}/sources/android/native_app_glue/android_native_app_glue.c )

# You need to link static libraries against your shared native library.
target_link_libraries( io-lib app-glue ${log-lib} )