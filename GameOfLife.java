import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.io.FileReader;

public class GameOfLife
{
    static int printerDelay = 256;
    static boolean[][] cells;
    static boolean[][] next;
    static int cursorRow;
    static int cursorColumn;
    static boolean cursorVisible = true;
    static int rowCount;
    static int columnCount;
    static boolean showHelp = true;
    static BufferedReader input = null;

    static void
    info()
    {
        System.out.println("--- Debug Info");
        System.out.printf("rows           : %d\n", rowCount);
        System.out.printf("columns        : %d\n", columnCount);
        newLine();
        System.out.printf("cursor row     : %d\n", cursorRow);
        System.out.printf("cursor column  : %d\n", cursorColumn);
        System.out.printf("cursor visible : %b\n", cursorVisible);
        newLine();
        System.out.printf("printer delay  : %d ms\n", printerDelay);
        newLine();
    }

    static void
    initialize(String[] args)
    {
        int height = 10;
        int width = 16;

        int unprocessed = 0;

        for(int i = 0; i < args.length; ++i)
        {
            String arg = args[i];

            if(arg.equals("--nohelp"))
            {
                showHelp = false;
            }
            else
            {

                try
                {
                    FileReader fileReader = new FileReader(arg);
                    input = new BufferedReader(fileReader);
                }
                catch(Exception ex)
                {
                    args[unprocessed] = arg;
                    unprocessed++;
                }
                
            }
        }

        if(unprocessed >= 1)
        {
            try
            {
                width = (height = Integer.parseInt(args[0]));
            }
            catch(NumberFormatException nfex)
            {

            }
        }
        if(unprocessed >= 2)
        {
            try
            {
                width = Integer.parseInt(args[1]);
            }
            catch(NumberFormatException nfex)
            {

            }

        }

        initialize(height, width);
    }


    static void
    initialize(int height, int width)
    {
        rowCount = height;
        columnCount = width;
        cells = new boolean[rowCount][columnCount];
        next = new boolean[rowCount][columnCount];
    }

    static void
    resize(int heightIncrement, int widthIncrement)
    {
        int rows = rowCount;

        rowCount += heightIncrement;
        if(rowCount < 1)
        {
            rowCount = 1;
        }

        if(rowCount < rows)
        {
            rows = rowCount;
        }

        int columns = columnCount;

        columnCount += widthIncrement;
        if(columnCount < 1)
        {
            columnCount = 1;
        }

        if(columnCount < columns)
        {
            columns = columnCount;
        }

        next = new boolean[rowCount][columnCount];

        for(int rowIndex = 0;
            rowIndex < rows;
            ++rowIndex)
        {
            boolean[] cellsRow = cells[rowIndex];
            boolean[] nextRow = next[rowIndex];

            for(int columnIndex = 0;
                columnIndex < columns;
                ++columnIndex)
            {
                nextRow[columnIndex] = cellsRow[columnIndex];
            }
        }

        cells = next;

        next = new boolean[rowCount][columnCount];

        if(cursorRow >= rowCount)
        {
            cursorRow = rowCount - 1;
        }

        if(cursorColumn >= columnCount)
        {
            cursorColumn = columnCount - 1;
        }
    }

    static void
    newLine()
    {
        System.out.println();
    }

    static void
    show()
    {
        StringBuilder sb = new StringBuilder();
        for(int rowIndex = 0;
            rowIndex < rowCount;
            ++rowIndex)
        {
            boolean[] row = cells[rowIndex];
            for(int columnIndex = 0;
                columnIndex < columnCount;
                ++columnIndex)
            {
                boolean cell = row[columnIndex];

                char c;

                if(cursorVisible && rowIndex == cursorRow && columnIndex == cursorColumn)
                {
                    c = cell ? 'X' : '+';
                }
                else
                {
                    c = cell ? 'o' : '.';
                }

                sb.append(c);
            }
            sb.append('\n');
        }

        String s = sb.toString();

        System.out.println(s);
    }

    static boolean
    getCell(int rowIndex, int columnIndex)
    {
        while(rowIndex < 0)
        {
            rowIndex += rowCount;
        }

        while(columnIndex < 0)
        {
            columnIndex += columnCount;
        }

        while(rowIndex >= rowCount)
        {
            rowIndex -= rowCount;
        }

        boolean[] row = cells[rowIndex];

        while(columnIndex >= columnCount)
        {
            columnIndex -= columnCount;
        }

        return row[columnIndex];
    }

    static int
    neighborCount(int rowIndex, int columnIndex)
    {
        int result = 0;

        for(int row = rowIndex - 1;
            row <= rowIndex + 1;
            ++row)
        {
            for(int column = columnIndex - 1;
                column <= columnIndex + 1;
                ++column)
            {
                if((row == rowIndex) && (column == columnIndex))
                {
                    continue;
                }

                boolean cell = getCell(row, column);

                if(cell)
                {
                    ++result;
                }
            }
        }

        return(result);
    }

    static void
    step()
    {
        for(int rowIndex = 0;
            rowIndex < rowCount;
            ++rowIndex)
        {            
            boolean[] cellRow = cells[rowIndex];
            boolean[] nextRow = next[rowIndex];

            for(int columnIndex = 0;
                columnIndex < columnCount;
                ++columnIndex)
            {
                boolean cell = cellRow[columnIndex];

                int neighbors = neighborCount(rowIndex, columnIndex);

                if(((cell) && (neighbors < 2 || neighbors > 3)) ||
                    ((!cell) && (neighbors == 3)))
                {

                    cell = !cell;
                } 

                nextRow[columnIndex] = cell;
            }
        }

        boolean[][] tmp = cells;
        cells = next;
        next = tmp;
        
    }

    static void 
    moveCursor(int rowIncrement, int columnIncrement)
    {
        cursorRow += rowIncrement;

        while(cursorRow < 0)
        {
            cursorRow += rowCount;
        }

        while(cursorRow >= rowCount)
        {
            cursorRow -= rowCount;
        }

        cursorColumn += columnIncrement;

        while(cursorColumn < 0)
        {
            cursorColumn += columnCount;
        }

        if(cursorColumn >= columnCount)
        {
            cursorColumn -= columnCount;
        }
    }

    static boolean runningFromFile = false;

    static void
    run()
    {
        PrinterThread printer = new PrinterThread();

        printer.start();

        try
        {
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            if(!runningFromFile)
            {
                br.readLine();
            }
            
            br.readLine();    
        }
        catch(IOException ioex)
        {
            ioex.printStackTrace();
        }
        
        printer.stopPrinting();
    }

    static void
    processCharacter(int i)
    {
        char c = (char) i;
        
        switch(c)
        {
            case ' ':
            {
                // do nothing
            } break;
            case 'w':
            {
                moveCursor(-1, 0);
            } break;
            case 'a':
            {
                moveCursor(0, -1);
            } break;
            case 's':
            {
                moveCursor(1, 0);
            } break;
            case 'd':
            {
                moveCursor(0, 1);
            } break;
            case 'x':
            {
                boolean cell = cells[cursorRow][cursorColumn];
                cells[cursorRow][cursorColumn] = !cell;
            } break;                        
            case 'f':
            {
                step();
            } break;
            case 'v':
            {
                toggleCursor();
            } break;
            case 'c':
            {
                clear();
            } break;
            case 'r':
            {
                run();
            } break;
            case 'h':
            {
                help();
            } break;
            case 'z':
            {
                info();
            } break;
            case 'i':
            {
                resize(-1, 0);
            } break;
            case 'k':
            {
                resize(1, 0);
            } break;
            case 'j':
            {
                resize(0, -1);
            } break;
            case 'l':
            {
                resize(0, 1);
            } break;
            case '.':
            {
                if(printerDelay / 2 > 0)
                {
                    printerDelay /= 2;
                }
            } break;
            case ',':
            {
                if(printerDelay < (printerDelay * 2))
                {
                    printerDelay *= 2;
                }
            } break;
            case ((char) 13):
            {
                show();
            } break;
            case ((char) 10):
            {

            } break;
            default:
            {
                System.out.printf("Unrecognized character: %c #%d\n", c, i);

            } break;
        }
    }

    static void
    help()
    {
        String[] text = new String[] {
            "******************************************",
            "********* Conway's Game of Life **********",
            "*** Implemented by Oliver Phillip Roer ***",
            "******************************************",
            "",
            "--- Commands",
            " w - Moves the cursor up.",
            " a - Moves the cursor left.",
            " s - Moves the cursor down.",
            " d - Moves the cursor right.",
            " x - Toggles a cell.",
            " c - Clear all cells.",
            " f - Progress the game one frame.",
            " r - Runs the game until you hit 'Enter'.",
            " . - Make the game run twice as fast.",
            " , - Make the game run twice as slow.",
            " q - Exits the game.",
            " h - Displays this help menu.",
            " v - Toggles cursor visibility.",
            " z - Show debug info.",
            "",
            "--- Legend",
            " . - A dead cell.",
            " o - A live cell.",
            " + - The cursor, resting on a dead cell.",
            " X - The cursor, resting on a live cell.",
            ""
        };

        for(String line : text)
        {
            System.out.println(line);
        }
    }

    static void
    toggleCursor()
    {
        cursorVisible = !cursorVisible;
    }

    static void 
    clear()
    {
        for(int rowIndex = 0;
            rowIndex < rowCount;
            ++rowIndex)
        {
            boolean[] row = cells[rowIndex];
            for(int columnIndex = 0;
                columnIndex < columnCount;
                ++columnIndex)
            {
                row[columnIndex] = false;
            }
        }
    }

    private static void processInput() throws IOException
    {
        runningFromFile = true;

        String s = input.readLine();
        
        if(s.equals("c"))
        {
            try
            {
                int width = Integer.parseInt(input.readLine());
                int height = Integer.parseInt(input.readLine());
                initialize(height, width);

                for(int rowIndex = 0; 
                    rowIndex < height; 
                    ++rowIndex)
                {
                    String line = input.readLine();
                    if(line == null)
                    {
                        break;
                    }

                    boolean[] row = cells[rowIndex];

                    for(int columnIndex = 0; 
                        columnIndex < width && columnIndex < line.length();
                        ++columnIndex)
                    {
                        char c = line.charAt(columnIndex);
                        row[columnIndex] = (c != ' ' && c != '.');
                    }
                }
            }
            catch(NumberFormatException nfex)
            {
                nfex.printStackTrace();
            }

        }
        else if(s.equals("i"))
        {
            while((s = input.readLine()) != null)
            {
                for(int i = 0;
                    i < s.length();
                    ++i)
                {
                    char c = s.charAt(i);

                    if(c == '#')
                    {
                        break;
                    }

                    processCharacter(c);
                }
            }


        }

        runningFromFile = false;
    }

    public static void
    main(String[] args) 
    {
        initialize(args);

        if(input != null)
        {
            try
            {
                processInput();
            }
            catch(IOException ioex)
            {
                ioex.printStackTrace();
            }
        }

        if(showHelp)
        {
            help();
        }

        show();


        while(true)
        {

            try 
            {
                int i = System.in.read();

                if(i == 'q')
                {
                    return;
                }
                else
                {
                    processCharacter(i);
                }
            } 
            catch(IOException ioex)
            {
                continue;
            }
        }
    }
}

class PrinterThread extends Thread
{
    private boolean printing = true;

    @Override
    public void 
    run()
    {
        while(printing)
        {
            GameOfLife.step();
            GameOfLife.newLine();
            GameOfLife.show();
            try
            {
                Thread.sleep(GameOfLife.printerDelay);
            }
            catch(InterruptedException iex)
            {
                iex.printStackTrace();
            }
        }
    }

    public void stopPrinting()
    {
        printing = false;
    }
}