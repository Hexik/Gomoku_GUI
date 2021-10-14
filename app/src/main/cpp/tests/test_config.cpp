/**
 * @file test_config.cpp
 * @brief Configuration set
 **/

#include "catch2/catch.hpp"

#include "../game/config.h"

/**
 * @brief Config defaults test
 */
TEST_CASE( "Config, Default", "[All]" ) {
    Config c;
    CHECK( c.GetContinuous() == 0 );
    CHECK( c.GetGameType() == 0 );
    CHECK( c.GetMaxMemory() == kTTMemorySize );
    CHECK( c.GetHeight() == kPlaySize );
    CHECK( c.GetWidth() == kPlaySize );
    CHECK( c.GetTimeLeft() == 0 );
    CHECK( c.GetTimeInc() == 0 );
    CHECK( c.GetTimeoutMatch() == 0 );
    CHECK( c.GetTimeoutTurn() == 0 );
//    CHECK( c.GetRule() == static_cast<int>( BuildOptions::GameRule ) );
}

/**
 * @brief Config setters/getters test
 */
TEST_CASE( "Config, SetGet", "[All]" ) {
    Config c;
    CHECK( c.SetContinuous( 1 ).GetContinuous() == 1 );
    CHECK( c.SetRule( 1 ).GetRule() == 1 );
    CHECK( c.SetGameType( 2 ).GetGameType() == 2 );
    CHECK( c.SetMaxMemory( 10000000 ).GetMaxMemory() == 10000000 );
    CHECK( c.SetMaxMemory( 0 ).GetMaxMemory() == kTTMemorySize );
    CHECK( c.SetHeight( 15 ).GetHeight() == 15 );
    CHECK( c.SetWidth( 15 ).GetWidth() == 15 );
    CHECK( c.SetTimeLeft( 1000 ).GetTimeLeft() == 1000 );
    CHECK( c.SetTimeInc( 1000 ).GetTimeInc() == 1000 );
    CHECK( c.SetTimeoutMatch( 10000 ).GetTimeoutMatch() == 10000 );
    CHECK( c.SetTimeoutTurn( 5000 ).GetTimeoutTurn() == 5000 );
    CHECK( c.SetLimitNodes( 9223372036854775807ULL ).GetLimitNodes() == 9223372036854775807ULL );
}
