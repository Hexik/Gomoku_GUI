# define the sources of the self test
# Please keep these ordered alphabetically
set(TEST_SOURCES ${TEST_SOURCES}
        AndroidBuffer.cpp
        test_basic.cpp
        game/test_config.cpp
        game/test_engine.cpp
        test_inputQueue.cpp
        )

CHECK_CXX_COMPILER_FLAG("-Wreserved-identifier" COMPILER_SUPPORTS_RESERVED_IDENTIFIER)
if (COMPILER_SUPPORTS_RESERVED_IDENTIFIER)
    set_property(SOURCE ${TEST_SOURCES} APPEND_STRING PROPERTY COMPILE_FLAGS "-Wno-reserved-identifier ")
endif (COMPILER_SUPPORTS_RESERVED_IDENTIFIER)

add_library(test_main OBJECT ${TEST_SOURCES})
