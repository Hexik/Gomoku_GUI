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

}