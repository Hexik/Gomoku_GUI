/**
 * @file test_inputQueue.cpp
 * @brief Unit tests for LockedQueue
 **/
#include "catch2/catch.hpp"

#include "../game/lockedQueue.h"
#include <future>

/**
 * @brief LockedQueue basic test
 */
TEST_CASE( "LockedQueue, Basic", "[All]" ) {
    LockedQueue<int> q;

    CHECK( q.count() == 0 );
    CHECK( q.is_empty());

    q.push( 2 );

    CHECK( q.count() == 1 );
    CHECK( !q.is_empty());

    CHECK( q.front( 0 ) == 2 );
    CHECK( q.pop( 0 ) == 2 );
    CHECK( q.is_empty());

    q.push( 3 );
    auto f1 = std::async( std::launch::async, [&q]() { return q.pop( 0 ); } );
    auto f2 = std::async( std::launch::async, [&q]() { return q.pop( 0 ); } );
    q.push( 5 );

    CHECK( f1.get() + f2.get() == 8 );
    CHECK( q.is_empty());
}
