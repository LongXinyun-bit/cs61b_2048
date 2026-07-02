package game2048;

import java.security.KeyStore;
import java.util.Formatter;
import java.util.Observable;


/** The state of a game of 2048.
 *  @author Longxinyun
 */
public class Model extends Observable {
    private static Object Else;
    /** Current contents of the board. */
    private Board board;
    /** Current score. */
    private int score;
    /** Maximum score so far.  Updated when game ends. */
    private int maxScore;
    /** True iff game is ended. */
    private boolean gameOver;

    /* Coordinate System: column C, row R of the board (where row 0,
     * column 0 is the lower-left corner of the board) will correspond
     * to board.tile(c, r).  Be careful! It works like (x, y) coordinates.
     */

    /** Largest piece value. */
    public static final int MAX_PIECE = 2048;

    /** A new 2048 game on a board of size SIZE with no pieces
     *  and score 0. */
    public Model(int size) {
        board = new Board(size);
        score = maxScore = 0;
        gameOver = false;
    }

    /** A new 2048 game where RAWVALUES contain the values of the tiles
     * (0 if null). VALUES is indexed by (row, col) with (0, 0) corresponding
     * to the bottom-left corner. Used for testing purposes. */
    public Model(int[][] rawValues, int score, int maxScore, boolean gameOver) {
        int size = rawValues.length;
        board = new Board(rawValues, score);
        this.score = score;
        this.maxScore = maxScore;
        this.gameOver = gameOver;
    }

    /** Return the current Tile at (COL, ROW), where 0 <= ROW < size(),
     *  0 <= COL < size(). Returns null if there is no tile there.
     *  Used for testing. Should be deprecated and removed.
     *  */
    public Tile tile(int col, int row) {
        return board.tile(col, row);
    }

    /** Return the number of squares on one side of the board.
     *  Used for testing. Should be deprecated and removed. */
    public int size() {
        return board.size();
    }

    /** Return true iff the game is over (there are no moves, or
     *  there is a tile with value 2048 on the board). */
    public boolean gameOver() {
        checkGameOver();
        if (gameOver) {
            maxScore = Math.max(score, maxScore);
        }
        return gameOver;
    }

    /** Return the current score. */
    public int score() {
        return score;
    }

    /** Return the current maximum game score (updated at end of game). */
    public int maxScore() {
        return maxScore;
    }

    /** Clear the board to empty and reset the score. */
    public void clear() {
        score = 0;
        gameOver = false;
        board.clear();
        setChanged();
    }

    /** Add TILE to the board. There must be no Tile currently at the
     *  same position. */
    public void addTile(Tile tile) {
        board.addTile(tile);
        checkGameOver();
        setChanged();
    }

    /** Tilt the board toward SIDE. Return true iff this changes the board.
     *
     * 1. If two Tile objects are adjacent in the direction of motion and have
     *    the same value, they are merged into one Tile of twice the original
     *    value and that new value is added to the score instance variable
     * 2. A tile that is the result of a merge will not merge again on that
     *    tilt. So each move, every tile will only ever be part of at most one
     *    merge (perhaps zero).
     * 3. When three adjacent tiles in the direction of motion have the same
     *    value, then the leading two tiles in the direction of motion merge,
     *    and the trailing tile does not.
     * */
    public boolean tilt(Side side) {
        boolean changed;//先预设为假的
        changed = false;
        boolean [][] merge = new boolean[board.size()][board.size()];//建一个**“记事本”**，专门记录哪个格子刚刚发生过合并

        board.setViewingPerspective(side);//盘子降要面向的方向， 括号里写目前看的是向什么方向的
        for (int col=0; col< board.size(); col+=1){//负当列不变的时候， 如果列是在边长的数量当中的， 切大于等于0 +1
            for (int row = board.size()-1; row>=0; row-=1){ //当列不变的时候， 从行最大往下看， 每看一个久-1
               if (board.tile(col,row) != null){//判断目前的哥哥里面有没有东西， 如果不是NUll就是有东西，可以往下看
                    Tile T = this.board.tile(col,row);// Java 里，我们不能直接去移动“格子”这个位置，必须移动具体的“物体”。所以我们要先用这行代码，把当前格子里装的那个数字方块本身给取出来，存在变量 T 里（就像你用手把这个小木块拿了起来），等一会儿你才能把这个 T 扔给 board.move 叫它滑过去。
                       int targetRow = row;//把你目前的行的数字付给targetrow这个
                       while (targetRow < board.size() - 1  && board.tile(col,targetRow+1) == null){
                       //如果目前的格子和上一个相比 ，上一个是空的， 说明可以向上走一格，线
                           targetRow+=1;}
                       if (targetRow < board.size() - 1  && board.tile(col,targetRow+1).value() == T.value() && !merge[col][targetRow+1]){
                           targetRow+=1;//如果在范围里并且方格里面的value和我目前站着的地方的value相同，且我目前站在的格子的上面一个格子，从记事本里查发现没有合并过 加一
                       }
                       if (board.move(col, targetRow, T) ){//执行滑动和算分，这个 board.move 就会在原地变身成 true（合并成功）或者 false（没有合并

                           score = score+ board.tile(col, targetRow).value();
                           merge[col][targetRow] = true;
                       }
                   // 只有方块真的挪窝了，才记作发生改变
                   if (targetRow != row)
                       changed = true;
                }
            }
        }
        board.setViewingPerspective(Side.NORTH);
        checkGameOver();//
        if (changed) {
            setChanged();
        }
        return changed;
    }

    /** Checks if the game is over and sets the gameOver variable
     *  appropriately.
     */
    private void checkGameOver() {
        gameOver = checkGameOver(board);
    }

    /** Determine whether game is over. */
    private static boolean checkGameOver(Board b) {
        return maxTileExists(b) || !atLeastOneMoveExists(b);
    }

    /** Returns true if at least one space on the Board is empty.
     *  Empty spaces are stored as null.
     * */
    public static boolean emptySpaceExists(Board b) {//这里开始写！！看是否这个位置是空的,
        // 是的就报True， 否就报F
        int size= b.size();
        for ( int col =0; col<b.size();col+=1){;
        for ( int row=0;row<b.size();row+=1){;
        if (b.tile(col,row) == null){
            return true;
        }
        }
        }
        return false;
    }

     //Returns true if any tile is equal to the maximum valid value.
    //     * Maximum valid value is given by MAX_PIECE. Note that
    //     * given a Tile object t, we get its value with t.value().
    //     * 去检查整个棋盘 16 个格子里，有没有任何一个方块的数字达到了 2048。
    //     * 只要发现任何一个格子有 2048，就立刻返回 true 代表有人通关了；
    //     * 如果把 16 个格子全部看完了都没有 2048，就返回 false。
    //     * 它的逻辑和上一个找空位非常像，也是让“检查员”用双重循环一格一格去看，
    //     * 只不过这一次我们不是看格子是不是 null，而是要看格子的数值。
    //     * 在动手写之前，有两点最简单的规则你必须要先搞清楚：
    //     * 棋盘上有些格子是空的（null），空的格子是没有数字的。所以检查员在看格子的数值之前，必须先拍一拍它，
    //     * 确定它不是空的 null，否则直接读取数值程序会崩溃。
    //     * 我们不要把 2048 这个数字写死在代码里（也就是不要写 == 2048），
    //     * 而是要用学校给的变量 MAX_PIECE，所以我们要比对的是 == MAX_PIECE。

    public static boolean maxTileExists(Board b) {
     int size= b.size();
     for ( int col =0; col<b.size();col+=1){
     for ( int row=0;row<b.size();row+=1){
     if (b.tile(col,row)!= null){
     if (b.tile(col,row).value() == MAX_PIECE){
       return true;
                                              }
                                  }
                                         }
     }
       return false;
    }

    /**
     * Returns true if there are any valid moves on the board.返回图惹如果这里有任何能移动的方块
     * There are two ways that there can be valid moves:
     * 1. There is at least one empty space on the board.至少一个空格
     * 2. There are two adjacent tiles with the same value.两个可以移动的、相同数字的方块
     */
    public static boolean atLeastOneMoveExists(Board b) {
        if (emptySpaceExists(b)) {//if (emptySpaceExists(b) == true)
            return true;
        }
            for (int col=0; col < b.size(); col += 1){
            for (int row=0;row < b.size();row += 1){
                if (col < b.size() - 1) {
                if (b.tile(col, row).value() == b.tile(col + 1, row).value()) {
                    return true;
                }
                }
                if (row < b.size() - 1) {
                if (b.tile(col, row).value() == b.tile(col, row+1).value()) {
                   return true;
            }
            }
        }
            }
                return false;
    }//做的事情是： 判断能不能下， 当还有至少一个是null的时候可，或者当它这个tile在3x3移动的时候， 往右往下去进行比较一个空格， 相同的 可
//返回ture, 否则返回false

    @Override
     /** Returns the model as a string, used for debugging. */
    public String toString() {
        Formatter out = new Formatter();
        out.format("%n[%n");
        for (int row = size() - 1; row >= 0; row -= 1) {
            for (int col = 0; col < size(); col += 1) {
                if (tile(col, row) == null) {
                    out.format("|    ");
                } else {
                    out.format("|%4d", tile(col, row).value());
                }
            }
            out.format("|%n");
        }
        String over = gameOver() ? "over" : "not over";
        out.format("] %d (max: %d) (game is %s) %n", score(), maxScore(), over);
        return out.toString();
    }

    @Override
    /** Returns whether two models are equal. */
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        } else if (getClass() != o.getClass()) {
            return false;
        } else {
            return toString().equals(o.toString());
        }
    }

    @Override
    /** Returns hash code of Model’s string. */
    public int hashCode() {
        return toString().hashCode();
    }
}
