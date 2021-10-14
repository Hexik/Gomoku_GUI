/**
 * @file config.cpp
 * @brief Configuration info
 */

#include "config.h"
#include <cassert>

Config::Config() {
    assert( SetTimeoutTurn( 0U ).GetTimeoutTurn() == 0U );
}
