package cz.fontan.gomoku_gui.game

import org.junit.Test

class MoveListTest {

    @Test
    fun empty() {
        val list = MoveList()
        assert(list.isValid())
        assert(list.getCurrentMove() == Move())
        assert(list.getIndex() == -1)
        assert(list.getLastIndex() == -1)
        assert(list.size() == 0)
    }

    @Test
    fun add() {
        val list = MoveList()
        list.add(Move(1, 1, EnumMove.Black))
        assert(list.getCurrentMove() == Move(1, 1, EnumMove.Black))
        assert(list.getIndex() == 0)
        assert(list.getLastIndex() == 0)
        list.add(Move(2, 2, EnumMove.White))
        assert(list.getCurrentMove() == Move(2, 2, EnumMove.White))
        assert(list.getIndex() == 1)
        assert(list.getLastIndex() == 1)
        assert(list.size() == 2)
    }

    @Test(expected = IllegalArgumentException::class)
    fun add_empty_move() {
        val list = MoveList()
        list.add(Move())
    }

    @Test(expected = IllegalArgumentException::class)
    fun add_bad_W_move() {
        val list = MoveList()
        list.add(Move(1, 1, EnumMove.White))
    }

    @Test(expected = IllegalArgumentException::class)
    fun add_bad_BB_move() {
        val list = MoveList()
        list.add(Move(1, 1, EnumMove.Black))
        list.add(Move(2, 2, EnumMove.Black))
    }

    @Test(expected = IllegalArgumentException::class)
    fun add_bad_BWW_move() {
        val list = MoveList()
        list.add(Move(1, 1, EnumMove.Black))
        list.add(Move(2, 2, EnumMove.White))
        list.add(Move(3, 3, EnumMove.White))
    }

    @Test(expected = IllegalArgumentException::class)
    fun undo_empty() {
        val list = MoveList()
        list.undo()
    }

    @Test(expected = IllegalArgumentException::class)
    fun undo2_empty() {
        val list = MoveList()
        list.add(Move(1, 1, EnumMove.Black))
        list.undo()
        list.undo()
    }

    @Test(expected = IllegalArgumentException::class)
    fun redo2_empty() {
        val list = MoveList()
        list.add(Move(1, 1, EnumMove.Black))
        list.undo()
        list.redo()
        list.redo()
    }

    @Test
    fun reset() {
        val list = MoveList()
        list.add(Move(1, 1, EnumMove.Black))
        list.add(Move(2, 2, EnumMove.White))
        list.reset()
        assert(list.isValid())
        assert(list.getCurrentMove() == Move())
        assert(list.getIndex() == -1)
        assert(list.getLastIndex() == -1)
        assert(list.size() == 0)
    }

    @Test
    fun undo() {
        val list = MoveList()
        list.add(Move(1, 1, EnumMove.Black))
        list.add(Move(2, 2, EnumMove.White))

        list.undo()
        assert(list.getCurrentMove() == Move(1, 1, EnumMove.Black))
        assert(list.getIndex() == 0)
        assert(list.getLastIndex() == 1)
        list.undo()
        assert(list.getCurrentMove() == Move())
        assert(list.getIndex() == -1)
        assert(list.getLastIndex() == 1)
        assert(list.size() == 2)
    }

    @Test
    fun redo() {
        val list = MoveList()
        list.add(Move(1, 1, EnumMove.Black))
        list.add(Move(2, 2, EnumMove.White))
        list.undo()
        list.undo()

        list.redo()
        assert(list.getCurrentMove() == Move(1, 1, EnumMove.Black))
        assert(list.getIndex() == 0)
        assert(list.getLastIndex() == 1)
        list.redo()
        assert(list.getCurrentMove() == Move(2, 2, EnumMove.White))
        assert(list.getIndex() == 1)
        assert(list.getLastIndex() == 1)
    }

    @Test
    fun undo_add() {
        val list = MoveList()
        list.add(Move(1, 1, EnumMove.Black))
        list.add(Move(2, 2, EnumMove.White))
        list.add(Move(3, 3, EnumMove.Black))
        assert(list.size() == 3)
        list.undo()
        list.undo()
        assert(list.size() == 3)
        list.add(Move(4, 4, EnumMove.White))

        assert(list.getCurrentMove() == Move(4, 4, EnumMove.White))
        assert(list.getIndex() == 1)
        assert(list.getLastIndex() == 1)

        list.add(Move(5, 5, EnumMove.Black))
        assert(list.getCurrentMove() == Move(5, 5, EnumMove.Black))
        assert(list.getIndex() == 2)
        assert(list.getLastIndex() == 2)
        assert(list.size() == 3)
    }

    @Test
    fun iterate() {
        val list = MoveList()
        list.add(Move(1, 1, EnumMove.Black))
        list.add(Move(2, 2, EnumMove.White))
        list.add(Move(3, 3, EnumMove.Black))
        list.add(Move(4, 4, EnumMove.White))
        list.add(Move(5, 5, EnumMove.Black))
        assert(list.getCurrentMove() == Move(5, 5, EnumMove.Black))
        assert(list.getIndex() == 4)
        assert(list.getLastIndex() == 4)
        assert(list.size() == 5)
        for (it in list) {
            assert(it != Move())
        }
        list.rewind()
        var counter = 0
        for (it in list) {
            assert(it != Move())
            ++counter
        }
        assert(counter == list.size())
    }

    @Test
    fun to_board_empty() {
        val list = MoveList()
        assert(list.toBoard(false) == "yxboard\ndone\n")
    }

    @Test
    fun to_board_B() {
        val list = MoveList()
        list.add(Move(1, 1, EnumMove.Black))
        assert(list.toBoard(true) == "board\n1,1,2\ndone\n")
    }

    @Test
    fun to_board_BW() {
        val list = MoveList()
        list.add(Move(1, 1, EnumMove.Black))
        list.add(Move(2, 2, EnumMove.White))
        assert(list.toBoard(true) == "board\n1,1,1\n2,2,2\ndone\n")
    }

    @Test
    fun to_board_BWB() {
        val list = MoveList()
        list.add(Move(1, 1, EnumMove.Black))
        list.add(Move(2, 2, EnumMove.White))
        list.add(Move(3, 3, EnumMove.Black))
        assert(list.toBoard(false) == "yxboard\n1,1,2\n2,2,1\n3,3,2\ndone\n")
    }

    @Test
    fun to_board_BWBW() {
        val list = MoveList()
        list.add(Move(1, 1, EnumMove.Black))
        list.add(Move(2, 2, EnumMove.White))
        list.add(Move(3, 3, EnumMove.Black))
        list.add(Move(4, 4, EnumMove.White))
        assert(list.toBoard(true) == "board\n1,1,1\n2,2,2\n3,3,1\n4,4,2\ndone\n")
    }

    @Test
    fun to_board_BWBW_undo() {
        val list = MoveList()
        list.add(Move(1, 1, EnumMove.Black))
        list.add(Move(2, 2, EnumMove.White))
        list.add(Move(3, 3, EnumMove.Black))
        list.add(Move(4, 4, EnumMove.White))
        list.undo()
        assert(list.toBoard(true) == "board\n1,1,2\n2,2,1\n3,3,2\ndone\n")
        list.redo()
        assert(list.toBoard(true) == "board\n1,1,1\n2,2,2\n3,3,1\n4,4,2\ndone\n")
    }

}