# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html

# Sets the minimum version of CMake required to build the native library.

cmake_minimum_required(VERSION 3.10.2)

# Declares and names the project.

project("cpp_native" CXX)

SET(CMAKE_CXX_FLAGS_SPEEDY "-Ofast")
SET(CMAKE_CXX_FLAGS_RELEASE "-Ofast")
SET(CMAKE_CXX_FLAGS_DEBUG "-O0")

include(CheckCXXCompilerFlag)

macro(add_cxx_compiler_flag FLAG)
    string(REGEX REPLACE "-" "" SFLAG ${FLAG})
    check_cxx_compiler_flag(${FLAG} COMPILER_SUPPORT_${SFLAG})
    if (COMPILER_SUPPORT_${SFLAG})
        set(CMAKE_CXX_FLAGS_TMP "${CMAKE_CXX_FLAGS_TMP} ${FLAG}")
    endif ()
endmacro(add_cxx_compiler_flag)

macro(add_compiler_flag FLAG)
    add_cxx_compiler_flag(${FLAG})
endmacro(add_compiler_flag)

check_cxx_compiler_flag("-Werror" COMPILER_SUPPORT_WERROR)

add_compiler_flag("-fdata-sections")
add_compiler_flag("-ffunction-sections")
add_compiler_flag("-minline-all-stringops")
add_compiler_flag("-fno-check-pointer-bounds")
add_compiler_flag("-fno-common")
add_compiler_flag("-fno-exceptions")
add_compiler_flag("-fno-lto")
add_compiler_flag("-fno-rtti")
add_compiler_flag("-fno-openmp")
add_compiler_flag("-fsigned-char")
add_compiler_flag("-m64")
add_compiler_flag("-pedantic")
add_compiler_flag("-fcomment-block-commands=cond")
add_compiler_flag("-fcomment-block-commands=endcond")

add_compiler_flag("-Weverything")
#add_compiler_flag("-Wno-documentation")
add_compiler_flag("-Wno-c++98-compat")
add_compiler_flag("-Wno-c++98-compat-pedantic")
#add_compiler_flag("-Wno-comma")
#add_compiler_flag("-Wcovered-switch-default")
add_compiler_flag("-Wno-exit-time-destructors")
#add_compiler_flag("-Wno-format-nonliteral")
#add_compiler_flag("-Wno-global-constructors")
add_compiler_flag("-Wno-padded")
#add_compiler_flag("-Wno-undefined-func-template")
#add_compiler_flag("-Wno-unknown-warning-option")

set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS_TMP} ${CMAKE_CXX_FLAGS}")

# The -ansi flag must be added last, otherwise it is also used as a linker flag by check_cxx_compiler_flag making it fails
# Moreover we should not set both -strict-ansi and -ansi
check_cxx_compiler_flag("-strict-ansi" COMPILER_SUPPORT_STRICTANSI)
add_compiler_flag("-Qunused-arguments")        # disable clang warning: argument unused during compilation: '-ansi'

if (COMPILER_SUPPORT_STRICTANSI)
    set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -strict-ansi")
else ()
    add_cxx_compiler_flag("-ansi")
endif ()

if ("${CMAKE_C_COMPILER_ID}" STREQUAL "Clang")
    set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -ferror-limit=0")
    set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS}   -ferror-limit=0")
    add_compiler_flag("-Waggregate-return")
endif ()

# adding -Werror turns such warnings into errors
if (COMPILER_SUPPORT_WERROR)
    set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -Werror")
    set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS}   -Werror")
endif ()

add_subdirectory(brain)
add_subdirectory(test)

CHECK_CXX_COMPILER_FLAG("-Wmissing-prototypes" COMPILER_SUPPORTS_MISSING_PROTOTYPES)
if (COMPILER_SUPPORTS_MISSING_PROTOTYPES)
    set_property(SOURCE native-lib.cpp APPEND_STRING PROPERTY COMPILE_FLAGS "-Wno-missing-prototypes ")
endif (COMPILER_SUPPORTS_MISSING_PROTOTYPES)

# Creates and names a library, sets it as either STATIC
# or SHARED, and provides the relative paths to its source code.
# You can define multiple libraries, and CMake builds them for you.
# Gradle automatically packages shared libraries with your APK.

add_library( # Sets the name of the library.
        native-lib

        # Sets the library as a shared library.
        SHARED

        # Provides a relative path to your source file(s).
        native-lib.cpp)

#target_compile_options(native-lib PUBLIC -g -O0 -fsanitize=address -fno-omit-frame-pointer)
#set_target_properties(native-lib PROPERTIES LINK_FLAGS -fsanitize=address)

# Searches for a specified prebuilt library and stores the path as a
# variable. Because CMake includes system libraries in the search path by
# default, you only need to specify the name of the public NDK library
# you want to add. CMake verifies that the library exists before
# completing its build.

find_library( # Sets the name of the path variable.
        log-lib

        # Specifies the name of the NDK library that
        # you want CMake to locate.
        log)

# Specifies libraries CMake should link to your target library. You
# can link multiple libraries, such as libraries you define in this
# build script, prebuilt third-party libraries, or system libraries.

target_link_libraries( # Specifies the target library.
        native-lib
        brain
        $<TARGET_OBJECTS:test_main>
        # Links the target library to the log library
        # included in the NDK.
        ${log-lib})