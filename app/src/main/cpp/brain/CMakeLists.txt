cmake_minimum_required(VERSION 3.4.1)
set(CMAKE_VERBOSE_MAKEFILE on)

project(brain)
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -g -Wall -Wextra -pedantic -Werror ")

find_library( # Sets the name of the path variable.
        log-lib

        # Specifies the name of the NDK library that
        # you want CMake to locate.
        log)

add_library(brain board.cpp config.cpp engine.cpp safecast.cpp)

# Specifies libraries CMake should link to your target library. You
# can link multiple libraries, such as libraries you define in this
# build script, prebuilt third-party libraries, or system libraries.

#target_compile_options(brain PUBLIC -fsanitize=address -fno-omit-frame-pointer)
#set_target_properties(brain PROPERTIES LINK_FLAGS -fsanitize=address)

target_link_libraries( # Specifies the target library.
        brain

        # Links the target library to the log library
        # included in the NDK.
        ${log-lib})

message("   Game type : ${GAME_TYPE}")
