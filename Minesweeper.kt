package minesweeper

import kotlin.random.Random

class Minesweeper(private val fieldSize: Int) {
    private val field = createEmptyGrid(fieldSize)
    private var gameState = GameState.RUNNING
    private var firstTurn = true
    private var numberOfMines = 0

    fun play() {
        print("How many mines do you want on the field? ")
        numberOfMines = readLine()!!.trim().toInt()

        printField()
        getAndProcessInput()

        println(when (gameState){
            GameState.LOST -> "You stepped on a mine and failed!"
            else -> "You stepped on a mine and failed!"
        })
    }

    private fun createEmptyGrid(fieldSize: Int): Array<Array<Cell>> {
        val field = Array(fieldSize) { Array(fieldSize) { Cell(0, 0) } }

        for (row in 0 until fieldSize)
            for (column in 0 until fieldSize) {
                field[row][column] = Cell(row + 1, column + 1)
            }

        return field
    }

    private fun getAndProcessInput() {
        while (gameState == GameState.RUNNING) {
            print("Set/unset mine marks or claim a cell as free: ")
            val (y, x, command) = readLine()!!.trim().split("\\s+".toRegex())
            processInput(x.toInt() - 1, y.toInt() - 1, command[0])
            printField()
            if (gameState != GameState.LOST) updateGameState()
        }
    }

    private fun processInput(row: Int, column: Int, command: Char) {
        val cell = field[row][column]

        if (firstTurn) {
            setupField(cell)
            firstTurn = false
        }

        when (command) {
            'm'  -> cell.toggleMark()
            else -> explore(cell)
        }
    }

    private fun explore(cell: Cell) {
        if (cell.isExplored)
            return

        if (cell.isMined) {
            cell.displayChar = 'X'
            gameState = GameState.LOST
            return
        }

        cell.setExplored()

        if (cell.minesVisible == 0)
            checkAdjacents(cell)
    }

    private fun checkAdjacents(cell: Cell) {
        for (rowOffset in -1..1) {
            if (rowOffset == -1 && cell.row == 1) continue
            if (rowOffset ==  1 && cell.row == field.size) continue
            for (columnOffset in -1..1) {
                if (columnOffset == -1 && cell.column == 1) continue
                if (columnOffset ==  1 && cell.column == field[0].size) continue
                val adjacentCell = field[cell.row - 1 + rowOffset][cell.column - 1 + columnOffset]
                if (adjacentCell.minesVisible > 0) {
                    adjacentCell.setExplored()
                } else
                    explore(adjacentCell)
            }
        }
    }

    private fun updateGameState() {
        gameState = if (allAndOnlyMinesMarked() || onlyMinesUnexplored())
            GameState.WON
        else
            GameState.RUNNING
    }

    private fun onlyMinesUnexplored(): Boolean {
        var unexploredMines = 0
        var unexploredCells = 0

        for (row in field)
            for (column in field.indices) {
                val cell = row[column]
                if (!cell.isExplored && cell.isMined)
                    unexploredMines++
                if (!cell.isExplored)
                    unexploredCells++
            }

        return unexploredMines == unexploredCells
    }

    private fun allAndOnlyMinesMarked(): Boolean {
        var marks = 0
        var markedMines = 0

        for (row in field)
            for (column in field.indices) {
                val cell = row[column]
                if (cell.isMarked)
                    marks++
                if (cell.isMarked && cell.isMined)
                    markedMines++
            }

        return markedMines == numberOfMines && marks == numberOfMines
    }

    private fun setupField(firstExploredCell: Cell) {
        for (i in 1..numberOfMines) {
            var rowIndex = Random.nextInt(0, field.size - 1)
            var columnIndex = Random.nextInt(0, field[0].size - 1)

            while (field[rowIndex][columnIndex].isMined || (rowIndex + 1 == firstExploredCell.row && columnIndex + 1 == firstExploredCell.column)) {
                rowIndex = Random.nextInt(0, field.size)
                columnIndex = Random.nextInt(0, field[0].size)
            }

            field[rowIndex][columnIndex].isMined = true

            for(rowOffset in -1..1) {
                if ((rowIndex == 0 && rowOffset == -1) || (rowIndex == field.size - 1 && rowOffset == 1)) continue
                for (columnOffset in -1..1) {
                    if ((columnIndex == 0 && columnOffset == -1) || (columnIndex == field.size - 1 && columnOffset == 1)) continue
                    val cell = field[rowIndex + rowOffset][columnIndex + columnOffset]
                    if (cell.isMined)
                        continue
                    else
                        cell.minesVisible++
                }
            }
        }
    }

    private fun printField() {
        val dashes = "-".repeat(field.size)

        for (rowIndex in 0..fieldSize - 1) {
            if (rowIndex == 0)
                printTableHeader(dashes)

            print("" + (rowIndex + 1) + "│")

            for (columnIndex in 0..fieldSize - 1) {
                val cell = field[rowIndex][columnIndex]
                if (gameState == GameState.LOST && cell.isMined)
                    print('X')
                else
                    print(cell.displayChar)
            }
            println("│")
        }
        println("—│$dashes│")
    }

    private fun printTableHeader(dashes: String) {
        print(" │")
        for (n in 1..fieldSize)
            print("$n")
        println("│\n—│$dashes│")
    }

    private class Cell(val row: Int, val column: Int) {
        var isMined = false
        var isMarked = false
        var isExplored = false
        var displayChar = '.'
        var minesVisible = 0

        fun setExplored() {
            isExplored = true
            displayChar = when (minesVisible) {
                0    -> '/'
                else -> '0' + minesVisible
            }
        }

        fun toggleMark() {
            if (isMarked) {
                if (isExplored)
                    displayChar = '/'
                else
                    displayChar = '.'
                isMarked = false
            }
            else {
                displayChar = '*'
                isMarked = true
            }
        }
    }

    enum class GameState {
        RUNNING, WON, LOST
    }
}
