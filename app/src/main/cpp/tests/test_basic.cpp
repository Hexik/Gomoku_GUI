/**
 * @file test_basic.cpp
 * @brief Basic simple tests
 **/

#include "catch2/catch.hpp"

/* ================================================================= */
/**  First test in every test run. This simple builtin test is
 *  included everytime to have non-empty test list and check
 *  the Unit test framework, it never fails
 */
TEST_CASE( "Basic, Generic", "[All]" )
{
    CHECK( true );
    CHECK_FALSE( false );

    CHECK_THAT( "", Catch::Matchers::Equals( "" ) );
    CHECK_THAT( "Strings are objects that represent sequences of characters.",
                Catch::Matchers::Equals( "Strings are objects that represent sequences of characters." ) );
    CHECK_THAT( "String", !Catch::Matchers::Equals( "Memory" ) );

    CHECK( 0 == 0 );
    CHECK( 0xff == 0xff );
    CHECK( 0xffff == 0xffff );
    CHECK( 0xffffffff == 0xffffffff );
    CHECK( 0x7fffffffffffffffLL == 0x7fffffffffffffffLL );
    CHECK( 0xffffffffffffffffLL == 0xffffffffffffffffLL );
    CHECK( 0 != 1 );
    CHECK( 1 != -1 );
    CHECK( 1 > -1 );
    CHECK( -1 < 1 );
}
