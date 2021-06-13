/**
 * @file test_engine.cpp
 * @brief Game brain tests
 **/

#include "catch.hpp"

#include "../brain/board.h"
#include "../brain/engine.h"

/**
 * @brief Engine About test
 */
TEST_CASE( "Engine, OK", "[All]" ) {
    Engine e( 5 );

    e.CmdAbout();
    CHECK_THAT( e.GetLastPipeOut(), Catch::Matchers::Contains( "Generic" ));
}
/**
 * @brief Engine ReadInputLine and parse line test
 */
TEST_CASE( "Engine, ReadInputLine", "[All]" ) {
    Engine e( 5 );
    CHECK( !e.HasReaderInput());

    e.AddCommandsToInputQueue( "TEST" );
    CHECK( e.ReadInputLine() == "TEST" );
    CHECK( !e.HasReaderInput());

    e.AddCommandsToInputQueue( "TEST1\nTEST2" );
    CHECK( e.ReadInputLine() == "TEST1" );
    CHECK( e.ReadInputLine() == "TEST2" );
    CHECK( !e.HasReaderInput());

    e.AddCommandsToInputQueue( "TEST1\n\nTEST2" );
    CHECK( e.ReadInputLine() == "TEST1" );
    CHECK( e.ReadInputLine() == "TEST2" );
    CHECK( !e.HasReaderInput());
}

/**
 * @brief Engine ParseCmd and parse line test
 */
TEST_CASE( "Engine, ParseCmd", "[All]" ) {
    Engine      e( 20 );
    std::string rest;
    e.AddCommandsToInputQueue( "xxxx" );
    CHECK( e.ParseCmd( e.ReadInputLine(), rest ) == Engine::eCommand::eUnknown );

    e.AddCommandsToInputQueue( "About" );
    CHECK( e.ParseCmd( e.ReadInputLine(), rest ) == Engine::eCommand::eAbout );

    e.AddCommandsToInputQueue( "Start 15" );
    CHECK( e.ParseCmd( e.ReadInputLine(), rest ) == Engine::eCommand::eStart );
    auto        v = Util::ParseNumbers( rest, " " );
    CHECK( v.size() == 1 );
    CHECK( v[0] == 15 );

    e.AddCommandsToInputQueue( "Start 15 20" );
    CHECK( e.ParseCmd( e.ReadInputLine(), rest ) == Engine::eCommand::eStart );
    v = Util::ParseNumbers( rest, " ," );
    CHECK( v.size() == 2 );
    CHECK( v[0] == 15 );

    e.AddCommandsToInputQueue( "Start" );
    CHECK( e.ParseCmd( e.ReadInputLine(), rest ) == Engine::eCommand::eStart );
    v = Util::ParseNumbers( rest, " " );
    CHECK( v.empty());

    e.AddCommandsToInputQueue( "yxboard" );
    CHECK( e.ParseCmd( e.ReadInputLine(), rest ) == Engine::eCommand::eYxBoard );
    v = Util::ParseNumbers( rest, " " );
    CHECK( v.empty());

    e.AddCommandsToInputQueue( "turn       1       ,      1     " );
    CHECK( e.ParseCmd( e.ReadInputLine(), rest ) == Engine::eCommand::eTurn );
    v = Util::ParseNumbers( rest, "," );
    CHECK( v.size() == 2 );
    CHECK( v[0] == 1 );
    CHECK( v[1] == 1 );
}

/**
 * @brief Engine ParseInfo and parse line test
 */
TEST_CASE( "Engine, ParseInfo", "[All]" ) {
    Engine      e( 5 );
    std::string rest;

    e.AddCommandsToInputQueue( "info max_memory 123456" );
    CHECK( e.ParseCmd( e.ReadInputLine(), rest ) == Engine::eCommand::eInfo );
    e.CmdParseInfo( rest );
    CHECK( e.GetInfo().GetMaxMemory() == 123456 );

    e.AddCommandsToInputQueue( "info max_memory 0" );
    CHECK( e.ParseCmd( e.ReadInputLine(), rest ) == Engine::eCommand::eInfo );
    e.CmdParseInfo( rest );
    CHECK( e.GetInfo().GetMaxMemory() == kTTMemorySize );

    e.AddCommandsToInputQueue( "info TIMEOUT_MATCH 20000" );
    CHECK( e.ParseCmd( e.ReadInputLine(), rest ) == Engine::eCommand::eInfo );
    e.CmdParseInfo( rest );
    CHECK( e.GetInfo().GetTimeoutMatch() == 20000 );

    e.AddCommandsToInputQueue( "info TIMEOUT_TURN 20000" );
    CHECK( e.ParseCmd( e.ReadInputLine(), rest ) == Engine::eCommand::eInfo );
    e.CmdParseInfo( rest );
    CHECK( e.GetInfo().GetTimeoutTurn() == 20000 );

    e.AddCommandsToInputQueue( "info TIME_left 20000" );
    CHECK( e.ParseCmd( e.ReadInputLine(), rest ) == Engine::eCommand::eInfo );
    e.CmdParseInfo( rest );
    CHECK( e.GetInfo().GetTimeLeft() == 20000 );

    e.AddCommandsToInputQueue( "info hash_size 1024" );
    CHECK( e.ParseCmd( e.ReadInputLine(), rest ) == Engine::eCommand::eInfo );
    e.CmdParseInfo( rest );

    e.AddCommandsToInputQueue( "info max_node 9223372036854775807" );
    CHECK( e.ParseCmd( e.ReadInputLine(), rest ) == Engine::eCommand::eInfo );
    e.CmdParseInfo( rest );
    CHECK( e.GetInfo().GetLimitNodes() == 9223372036854775807ULL );

    /*
            "GAME_TYPE",
            "RULE",
            "FOLDER",
    */
}

/**
 * @brief Engine Board command test
 */
TEST_CASE( "Engine, ParseBoard", "[All]" ) {
    Engine e( 20 );
    e.AddCommandsToInputQueue( "info TIMEOUT_TURN 200\n"
                               "info TIME_left 20000\n"
                               "board\n"
                               "1,1,1\n"
                               "2,2,2\n"
                               "done\n"
                               "end" );
    e.Loop();
    CHECK( !e.GetLastPipeOut().empty());
    CHECK( e.GetLastPipeOut().empty());
}

/**
 * @brief Engine Board multiple command test
 */
TEST_CASE( "Engine, ParseBoardMultiple", "[All]" ) {
    Engine e( 20 );
    e.AddCommandsToInputQueue( "info TIMEOUT_TURN 200\n"
                               "info TIME_left 20000\n"
                               "yxboard\n"
                               "1,1,1\n"
                               "2,2,2\n"
                               "done\n"
                               "board\n"
                               "1,1,1\n"
                               "2,2,2\n"
                               "done\n"
                               "end" );
    e.Loop();
    CHECK( !e.GetLastPipeOut().empty());
    CHECK( e.GetLastPipeOut().empty());
}

/**
 * @brief Engine YxBoard command test
 */
TEST_CASE( "Engine, ParseYxBoard", "[All]" ) {
    Engine e( 20 );
    e.AddCommandsToInputQueue( "info TIMEOUT_TURN 200\n"
                               "info TIME_left 20000\n"
                               "yxboard\n"
                               "done\n"
                               "end" );
    e.Loop();
    CHECK( e.GetLastPipeOut().empty());
}

/**
 * @brief Engine Execute test
 */
TEST_CASE( "Engine, CmdExecute", "[All]" ) {
    Engine e( 5 );

    CHECK( e.CmdExecute( "about" ));
    CHECK( e.CmdExecute( "start 15" ));
    CHECK_THAT( e.GetLastPipeOut(), Catch::Matchers::Contains( "OK" ));
    CHECK( !e.CmdExecute( "end" ));
}

/**
 * @brief Engine Loop test
 */
TEST_CASE( "Engine, Loop", "[All]" ) {
    Engine e( 5 );
    e.AddCommandsToInputQueue( "start 33\n"
                               "end" );
    e.Loop();
    CHECK_THAT( e.GetLastPipeOut(), Catch::Matchers::Contains( "ERROR size of the board" ));
    e.AddCommandsToInputQueue( "start\n"
                               "end" );
    e.Loop();
    CHECK_THAT( e.GetLastPipeOut(), Catch::Matchers::Contains( "ERROR size of the board" ));

    e.AddCommandsToInputQueue( "start 5\n"
                               "turn\n"
                               "end" );
    e.Loop();
    CHECK_THAT( e.GetLastPipeOut(), Catch::Matchers::Contains( "," ));
}

/**
 * @brief Engine START command test
 */
TEST_CASE( "Engine, CmdStart", "[All]" ) {
    Engine e( 5 );

    e.CmdParseStart( "5" );
    CHECK( e.GetLastPipeOut() == "OK" );

    e.CmdParseStart( "4" );
    CHECK( e.GetLastPipeOut() == "ERROR size of the board" );
    e.CmdParseStart( "33" );
    CHECK( e.GetLastPipeOut() == "ERROR size of the board" );

    CHECK( e.CmdExecute( "start 15" ));
    CHECK_THAT( e.GetLastPipeOut(), Catch::Matchers::Contains( "OK" ));

    CHECK( e.CmdExecute( "start 3" ));
    CHECK( e.GetLastPipeOut() == "ERROR size of the board" );
    CHECK( e.CmdExecute( "start 33" ));
    CHECK( e.GetLastPipeOut() == "ERROR size of the board" );
}

/**
 * @brief Engine Put move test
 */
TEST_CASE( "Engine, CmdPutMove", "[All]" ) {
    Engine e( 5 );

    e.CmdStart( 5 );
    CHECK( e.GetLastPipeOut() == "OK" );

    e.CmdPutMyMove( 0, 0 );
    CHECK( e.GetLastPipeOut().empty());

    e.CmdPutYourMove( 1, 0 );
    CHECK( e.GetLastPipeOut().empty());

    e.CmdPutMyMove( 1, 0 );
    CHECK( e.GetLastPipeOut() == "ERROR my move [1,0]" );

    e.CmdPutYourMove( 0, 0 );
    CHECK( e.GetLastPipeOut() == "ERROR opponent's move [0,0]" );

    e.CmdPutMyMove( 5, 5 );
    CHECK( e.GetLastPipeOut() == "ERROR my move [5,5]" );
}

/**
 * @brief Engine RESTART command test
 */
TEST_CASE( "Engine, CmdRestart", "[All]" ) {
    Engine e( 5 );

    e.CmdStart( 5 );

    CHECK( e.CmdExecute( "restart" ));
    CHECK( e.GetLastPipeOut() == "OK" );
    CHECK( e.GetBoard()->GetGamePly() == 0 );

    e.CmdPutMyMove( 2, 2 );
    CHECK( e.GetBoard()->GetGamePly() == 1 );

    CHECK( e.CmdExecute( "restart" ));
    CHECK( e.GetLastPipeOut() == "OK" );
    CHECK( e.GetBoard()->GetGamePly() == 0 );
}
