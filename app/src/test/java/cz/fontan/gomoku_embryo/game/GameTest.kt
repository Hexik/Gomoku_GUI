package cz.fontan.gomoku_embryo.game

import org.junit.Test

class GameTest {
    @Test
    fun empty() {
        val game = Game(BOARD_SIZE_MAX)
        assert(game.moveCount() == 0)
        assert(game.playerToMove == EnumMove.Black)
        assert(game.canMakeMove(Move(0, 0)))
        assert(game.canMakeMove(Move(BOARD_SIZE_MAX - 1, BOARD_SIZE_MAX - 1)))
    }

    @Test
    fun makeMove() {
        val game = Game(BOARD_SIZE_MAX).makeMove(Move(1, 1, EnumMove.Black))
        assert(game[0] == Move(1, 1, EnumMove.Black))
        assert(game.moveCount() == 1)
        assert(game.playerToMove == EnumMove.White)
        assert(!game.canMakeMove(Move(1, 1)))
    }

    @Test
    fun undoMove() {
        val game = Game(BOARD_SIZE_MAX).makeMove(Move(1, 1, EnumMove.Black)).undoMove()
        assert(game.moveCount() == 0)
        assert(game.playerToMove == EnumMove.Black)
        assert(game.canMakeMove(Move(1, 1)))
    }

    @Test
    fun newGame() {
        val game = Game(BOARD_SIZE_MAX).makeMove(Move(1, 1, EnumMove.Black)).newGame()
        assert(game.moveCount() == 0)
        assert(game.playerToMove == EnumMove.Black)
        assert(game.canMakeMove(Move(0, 0)))
        assert(game.canMakeMove(Move(1, 1)))
        assert(game.canMakeMove(Move(BOARD_SIZE_MAX - 1, BOARD_SIZE_MAX - 1)))
    }

    @Test
    fun reset() {
        val game = Game(BOARD_SIZE_MAX).makeMove(Move(1, 1, EnumMove.Black)).reset()
        assert(game.moveCount() == 0)
        assert(game.playerToMove == EnumMove.Black)
        assert(game.canMakeMove(Move(0, 0)))
        assert(game.canMakeMove(Move(1, 1)))
        assert(game.canMakeMove(Move(BOARD_SIZE_MAX - 1, BOARD_SIZE_MAX - 1)))
    }

    @Test
    fun redoMove() {
        val game = Game(BOARD_SIZE_MAX).makeMove(Move(1, 1, EnumMove.Black)).undoMove().redoMove()
        assert(game.moveCount() == 1)
        assert(game.playerToMove == EnumMove.White)
        assert(!game.canMakeMove(Move(1, 1)))

        game.makeMove(Move(2, 2, EnumMove.White)).undoMove().redoMove()
        assert(game.moveCount() == 2)
        assert(game.playerToMove == EnumMove.Black)
        assert(!game.canMakeMove(Move(1, 1)))
        assert(!game.canMakeMove(Move(2, 2)))

        game.undoMove().undoMove().redoMove().redoMove()
        assert(game.moveCount() == 2)
        assert(game.playerToMove == EnumMove.Black)
        assert(!game.canMakeMove(Move(1, 1)))
        assert(!game.canMakeMove(Move(2, 2)))
    }

    @Test(expected = IllegalArgumentException::class)
    fun undo2Move() {
        Game(BOARD_SIZE_MAX).makeMove(Move(1, 1, EnumMove.Black)).undoMove().undoMove()
    }

    @Test(expected = IllegalArgumentException::class)
    fun redo2Move() {
        Game(BOARD_SIZE_MAX).makeMove(Move(1, 1, EnumMove.Black)).undoMove().redoMove().redoMove()
    }

    @Test
    fun to_board_empty() {
        assert(Game(BOARD_SIZE_MAX).toBoard(false) == "yxboard\ndone")
    }

    @Test
    fun to_board_B() {
        val game = Game(BOARD_SIZE_MAX).makeMove(Move(1, 1, EnumMove.Black))
        assert(game.toBoard(true) == "board\n1,1,2\ndone")
    }

    @Test
    fun to_board_BW() {
        val game = Game(BOARD_SIZE_MAX).makeMove(Move(1, 1, EnumMove.Black))
            .makeMove(Move(2, 2, EnumMove.White))
        assert(game.toBoard(true) == "board\n1,1,1\n2,2,2\ndone")
    }

    @Test
    fun to_board_BWB() {
        val game = Game(BOARD_SIZE_MAX).makeMove(Move(1, 1, EnumMove.Black))
            .makeMove(Move(2, 2, EnumMove.White)).makeMove(Move(3, 3, EnumMove.Black))
        assert(game.toBoard(false) == "yxboard\n1,1,2\n2,2,1\n3,3,2\ndone")
    }

    @Test
    fun to_board_BWBW() {
        val game = Game(BOARD_SIZE_MAX).makeMove(Move(1, 1, EnumMove.Black))
            .makeMove(Move(2, 2, EnumMove.White)).makeMove(Move(3, 3, EnumMove.Black))
            .makeMove(Move(4, 4, EnumMove.White))
        assert(game.toBoard(true) == "board\n1,1,1\n2,2,2\n3,3,1\n4,4,2\ndone")
    }

    @Test
    fun to_stream_empty() {
        assert(Game(BOARD_SIZE_MAX).toStream() == "$BOARD_SIZE_MAX\n$BOARD_SIZE_MAX\n1\n")
    }

    @Test
    fun to_stream_moves() {
        val game = Game(BOARD_SIZE_MAX).makeMove(Move(1, 1, EnumMove.Black))
            .makeMove(Move(2, 2, EnumMove.White))
            .makeMove(Move(3, 3, EnumMove.Black)).makeMove(Move(4, 4, EnumMove.White))
        assert(
            game.toStream() == """
            |$BOARD_SIZE_MAX
            |$BOARD_SIZE_MAX
            |1
            |1 1 0
            |2 2 0
            |3 3 0
            |4 4 0
            |""".trimMargin()
        )
    }

    @Test
    fun from_stream_moves() {
        val s =
            """
            |$BOARD_SIZE_MAX
            |$BOARD_SIZE_MAX
            |1
            |1 1 0
            |2 2 0
            |3 3 0
            |4 4 0
            |""".trimMargin()
        assert(
            Game(BOARD_SIZE_MAX).fromStream(s)
                .toBoard(true) == "board\n1,1,1\n2,2,2\n3,3,1\n4,4,2\ndone"
        )
    }

    @Test
    fun to_stream_moves_and_block() {
        val game = Game(BOARD_SIZE_MAX).makeMove(Move(1, 1, EnumMove.Black))
            .makeMove(Move(2, 2, EnumMove.White)).makeMove(Move(5, 5, EnumMove.Wall))
            .makeMove(Move(3, 3, EnumMove.Black)).makeMove(Move(4, 4, EnumMove.White))
        assert(
            game.toStream() == """
            |$BOARD_SIZE_MAX
            |$BOARD_SIZE_MAX
            |1
            |5 5 1
            |1 1 0
            |2 2 0
            |3 3 0
            |4 4 0
            |""".trimMargin()
        )
    }

    @Test
    fun from_stream_moves_and_block() {
        val s =
            """
            |$BOARD_SIZE_MAX
            |$BOARD_SIZE_MAX
            |1
            |5 5 1
            |1 1 0
            |2 2 0
            |3 3 0
            |4 4 0
            |""".trimMargin()
        assert(
            Game(BOARD_SIZE_MAX).fromStream(s)
                .toBoard(true) == "board\n5,5,3\n1,1,1\n2,2,2\n3,3,1\n4,4,2\ndone"
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun from_stream_empty() {
        Game(BOARD_SIZE_MAX).fromStream("")
    }

    @Test(expected = IllegalArgumentException::class)
    fun from_stream_bad_move() {
        Game(BOARD_SIZE_MAX)
            .fromStream(
                """
            |$BOARD_SIZE_MAX
            |$BOARD_SIZE_MAX
            |1
            |1
            |""".trimMargin()
            )
    }

    @Test(expected = IllegalArgumentException::class)
    fun from_stream_bad_number() {
        Game(BOARD_SIZE_MAX)
            .fromStream(
                """
            |$BOARD_SIZE_MAX
            |$BOARD_SIZE_MAX
            |1
            |1 w
            |""".trimMargin()
            )
    }

    @Test(expected = IllegalArgumentException::class)
    fun from_stream_bad_dimension() {
        Game(BOARD_SIZE_MAX)
            .fromStream(
                """
            |$BOARD_SIZE_MAX
            |$BOARD_SIZE_MAX - 1
            |1
            |""".trimMargin()
            )
    }
}