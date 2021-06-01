package cz.fontan.gomoku_gui.game

import org.junit.Test

class GameTest {
    @Test
    fun empty() {
        val game = Game(BOARD_SIZE)
        assert(game.moveCount() == 0)
        assert(game.playerToMove == EnumMove.Black)
        assert(game.canMakeMove(Move(0, 0)))
        assert(game.canMakeMove(Move(BOARD_SIZE - 1, BOARD_SIZE - 1)))
    }

    @Test
    fun makeMove() {
        val game = Game(BOARD_SIZE)
        game.makeMove(Move(1, 1, EnumMove.Black))
        assert(game[0] == Move(1, 1, EnumMove.Black))
        assert(game.moveCount() == 1)
        assert(game.playerToMove == EnumMove.White)
        assert(!game.canMakeMove(Move(1, 1)))
    }

    @Test
    fun undoMove() {
        val game = Game(BOARD_SIZE)
        game.makeMove(Move(1, 1, EnumMove.Black))
        game.undoMove()
        assert(game.moveCount() == 0)
        assert(game.playerToMove == EnumMove.Black)
        assert(game.canMakeMove(Move(1, 1)))
    }

    @Test
    fun newGame() {
        val game = Game(BOARD_SIZE)
        game.makeMove(Move(1, 1, EnumMove.Black))
        game.newGame()
        assert(game.moveCount() == 0)
        assert(game.playerToMove == EnumMove.Black)
        assert(game.canMakeMove(Move(0, 0)))
        assert(game.canMakeMove(Move(BOARD_SIZE - 1, BOARD_SIZE - 1)))
    }

    @Test
    fun reset() {
        val game = Game(BOARD_SIZE)
        game.makeMove(Move(1, 1, EnumMove.Black))
        game.reset()
        assert(game.moveCount() == 0)
        assert(game.playerToMove == EnumMove.Black)
        assert(game.canMakeMove(Move(0, 0)))
        assert(game.canMakeMove(Move(BOARD_SIZE - 1, BOARD_SIZE - 1)))
    }

    @Test
    fun redoMove() {
        val game = Game(BOARD_SIZE)
        game.makeMove(Move(1, 1, EnumMove.Black))
        game.undoMove()
        game.redoMove()
        assert(game.moveCount() == 1)
        assert(game.playerToMove == EnumMove.White)
        assert(!game.canMakeMove(Move(1, 1)))

        game.makeMove(Move(2, 2, EnumMove.White))
        game.undoMove()
        game.redoMove()
        assert(game.moveCount() == 2)
        assert(game.playerToMove == EnumMove.Black)
        assert(!game.canMakeMove(Move(1, 1)))
        assert(!game.canMakeMove(Move(2, 2)))

        game.undoMove()
        game.undoMove()
        game.redoMove()
        game.redoMove()
        assert(game.moveCount() == 2)
        assert(game.playerToMove == EnumMove.Black)
        assert(!game.canMakeMove(Move(1, 1)))
        assert(!game.canMakeMove(Move(2, 2)))
    }

    @Test(expected = IllegalArgumentException::class)
    fun undo2Move() {
        val game = Game(BOARD_SIZE)
        game.makeMove(Move(1, 1, EnumMove.Black))
        game.undoMove()
        game.undoMove()
    }

    @Test(expected = IllegalArgumentException::class)
    fun redo2Move() {
        val game = Game(BOARD_SIZE)
        game.makeMove(Move(1, 1, EnumMove.Black))
        game.undoMove()
        game.redoMove()
        game.redoMove()
    }

    @Test
    fun to_board_empty() {
        val game = Game(BOARD_SIZE)
        assert(game.toBoard(false) == "yxboard\ndone")
    }

    @Test
    fun to_board_B() {
        val game = Game(BOARD_SIZE)
        game.makeMove(Move(1, 1, EnumMove.Black))
        assert(game.toBoard(true) == "board\n1,1,2\ndone")
    }

    @Test
    fun to_board_BW() {
        val game = Game(BOARD_SIZE)
        game.makeMove(Move(1, 1, EnumMove.Black))
        game.makeMove(Move(2, 2, EnumMove.White))
        assert(game.toBoard(true) == "board\n1,1,1\n2,2,2\ndone")
    }

    @Test
    fun to_board_BWB() {
        val game = Game(BOARD_SIZE)
        game.makeMove(Move(1, 1, EnumMove.Black))
        game.makeMove(Move(2, 2, EnumMove.White))
        game.makeMove(Move(3, 3, EnumMove.Black))
        assert(game.toBoard(false) == "yxboard\n1,1,2\n2,2,1\n3,3,2\ndone")
    }

    @Test
    fun to_board_BWBW() {
        val game = Game(BOARD_SIZE)
        game.makeMove(Move(1, 1, EnumMove.Black))
        game.makeMove(Move(2, 2, EnumMove.White))
        game.makeMove(Move(3, 3, EnumMove.Black))
        game.makeMove(Move(4, 4, EnumMove.White))
        assert(game.toBoard(true) == "board\n1,1,1\n2,2,2\n3,3,1\n4,4,2\ndone")
    }

    @Test
    fun to_stream_empty() {
        val game = Game(BOARD_SIZE)
        assert(game.toStream() == "$BOARD_SIZE\n$BOARD_SIZE\n1\n")
    }

    @Test
    fun to_stream_moves() {
        val game = Game(BOARD_SIZE)
        game.makeMove(Move(1, 1, EnumMove.Black))
        game.makeMove(Move(2, 2, EnumMove.White))
        game.makeMove(Move(3, 3, EnumMove.Black))
        game.makeMove(Move(4, 4, EnumMove.White))
        assert(
            game.toStream() == """
            |$BOARD_SIZE
            |$BOARD_SIZE
            |1
            |1 1
            |2 2
            |3 3
            |4 4
            |""".trimMargin()
        )
    }

    @Test
    fun from_stream_moves() {
        val game = Game(BOARD_SIZE)
        game.fromStream(
            """
            |$BOARD_SIZE
            |$BOARD_SIZE
            |1
            |1 1
            |2 2
            |3 3
            |4 4
            |""".trimMargin()
        )
        assert(game.toBoard(true) == "board\n1,1,1\n2,2,2\n3,3,1\n4,4,2\ndone")
    }

    @Test(expected = IllegalArgumentException::class)
    fun from_stream_empty() {
        val game = Game(BOARD_SIZE)
        game.fromStream("")
    }

    @Test(expected = IllegalArgumentException::class)
    fun from_stream_bad_move() {
        val game = Game(BOARD_SIZE)
        game.fromStream(
            """
            |$BOARD_SIZE
            |$BOARD_SIZE
            |1
            |1
            |""".trimMargin()
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun from_stream_bad_number() {
        val game = Game(BOARD_SIZE)
        game.fromStream(
            """
            |$BOARD_SIZE
            |$BOARD_SIZE
            |1
            |1 w
            |""".trimMargin()
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun from_stream_bad_dimension() {
        val game = Game(BOARD_SIZE)
        game.fromStream(
            """
            |$BOARD_SIZE
            |$BOARD_SIZE - 1
            |1
            |""".trimMargin()
        )
    }

}