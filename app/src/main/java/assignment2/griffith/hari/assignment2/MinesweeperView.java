package assignment2.griffith.hari.assignment2;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

/**
 * Created by aahuyarakshakaharil on 09/11/17.
 */

public class MinesweeperView extends View {

    private Paint black, white, silver, red, yellow; //Colors for background and line colors.

    private IOnMineCountChangeEventListener iOnMineCountChangeEventListener;
    private int minesMarkedCount, unCoveredCount = 0;
    private float multiplier;
    private final float paddingConstant = 2f;
    CellObject[][] cellObjectTable;
    private boolean uncoverModeSet = true; // Default to uncover Mode
    private boolean gameOverFlag = false;
    private Canvas canvas;


    //Constructors Block Start..!!!
    public MinesweeperView(Context context) {
        super(context);
        init();
    }

    public MinesweeperView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public MinesweeperView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }
    //Constructors Block Ends..!!!


    //Common Init Method..!!
    private void init() { //Common init method.
        //Initialize Paints
        black = new Paint();
        white = new Paint();
        silver = new Paint();
        red = new Paint();
        yellow = new Paint();
        //Setting respective Colors
        black.setColor(getResources().getColor(R.color.black));
        white.setColor(getResources().getColor(R.color.white));
        silver.setColor(getResources().getColor(R.color.silver));
        red.setColor(getResources().getColor(R.color.red));
        yellow.setColor(getResources().getColor(R.color.yellow));
        //Initialize textPaints Array --> Index indicates mineCount, each mine count is assigned a different color as per requirement

        //Setting Anti Aliasing Flags..!!
        white.setAntiAlias(true);
        silver.setAntiAlias(true);
        black.setAntiAlias(true);
        red.setAntiAlias(true);
        yellow.setAntiAlias(true);
        //Setting Anti Aliasing Flag Ends..!!
    }

    @Override
    protected void onDraw(Canvas canvas) { //Called on invalidate()
        super.onDraw(canvas); //Calling super..!!
        this.canvas = canvas;
        float width = getMeasuredWidth(); //Get Width
        float height = getMeasuredHeight(); //Get Height
        if (height < width) //Checking least values of height and weight
        {
            multiplier = height / 10;//Setting with least of the values
        } else {
            multiplier = width / 10; // Set Multiplier to adjust to make it perfect square
        }
        drawInitialSquares(); // Draw Background
        drawTextSquares(); //This method is responsible for drawing Texts
        if ((!gameOverFlag) && getUnCoveredCount() == 40) //Half way there.. If 40 squares are opened., player wins..!! Check for game over flag.
        {
            Toast.makeText(getContext(), "Great..!!! You're Half way there..!! Keep rocking..", Toast.LENGTH_LONG).show();//Toast showing game ended
            unCoverAllMines(); //Uncover mines to show winner all mines..
        }
        if (getUnCoveredCount() >= 80) //If 80 squares are opened., player wins..!! C.
        {
            Toast.makeText(getContext(), "Awesome..!!! You Win.. Click on Reset to try again", Toast.LENGTH_LONG).show();//Toast showing game ended
            unCoverAllMines(); //Uncover mines to show winner all mines..
        }
    }

    private void drawInitialSquares() {
        for (int index = 0; index <= 10; index++) {
            canvas.drawLine(paddingConstant, index * multiplier, 10 * multiplier, index * multiplier, white); // Horizontal Lines
            canvas.drawLine(index * multiplier, paddingConstant, index * multiplier, 10 * multiplier, white); // Vertical Lines
        }
        for (int row = 0; row < cellObjectTable.length; row++) { //Iterate through rows
            for (int col = 0; col < cellObjectTable[row].length; col++) { // Iterate Columns
                if (!cellObjectTable[row][col].isFlagged())
                    canvas.drawRect(row * multiplier + paddingConstant, col * multiplier + paddingConstant, (row + 1) * multiplier - paddingConstant, (col + 1) * multiplier - paddingConstant, black); //Render Rectangles
                else
                    canvas.drawRect(row * multiplier + paddingConstant, col * multiplier + paddingConstant, (row + 1) * multiplier - paddingConstant, (col + 1) * multiplier - paddingConstant, yellow); // Yellow Rectangles for flagged
                if (isGameOverFlag()) {
                    unCoverAllMines();  //Display Users All Mines..!!
                }
            }

        }
    }

    private void drawTextSquares() {
        for (int row = 0; row < cellObjectTable.length; row++) { //Iterate through rows
            for (int col = 0; col < cellObjectTable[row].length; col++) { // Iterate Columns
                if (!cellObjectTable[row][col].isCovered())//Check if cell is uncovered...
                {
                    if (!(cellObjectTable[row][col].isFlagged()) || (cellObjectTable[row][col].isCovered())) { //Cell shouldn't be flagged or uncovered
                        drawMineText(row, col); //Print the data
                    }
                }
            }
        }
    }

    private void drawMineText(int row, int col) { // Used to draw Text
        //Calculate dimensions to draw Text, best way is to use Rect
        float left =  (row * multiplier) + paddingConstant;
        float top = (col * multiplier) + paddingConstant;
        float right =  ((row + 1) * multiplier) - paddingConstant;
        float bottom =  ((col + 1) * multiplier) - paddingConstant;
        canvas.drawRect(left, top, right, bottom, cellObjectTable[row][col].getCellBackground()); //Draw backgroud rectangle to fill cell background
        Paint textPaint = cellObjectTable[row][col].getCellTextColor(); //Set text color
        String input = cellObjectTable[row][col].getMineString(); //Set input text to be printed
        float textWidth = textPaint.measureText(input, 0, 1); //Get Text Width, to calcuate center or where actually text printing starts
        float textBottom = textPaint.descent() - textPaint.ascent(); // Get the distance to calculate final bottom value of text.. It just works.. :P  https://stackoverflow.com/questions/4909367/how-to-align-text-vertically
        left = left + ((right - left)- textWidth) / 2; // Calulate the difference and divide by 2, since we do not need complete length but just half upto center
        top = top + ((bottom - top) - textBottom) / 2;// Similarly Calulate the difference and divide by 2, since we do not need complete length but just half upto center
        canvas.drawText(input, left, top - textPaint.ascent(), textPaint); //Finally draw the text.. Phew.. Lot of math..!!
    }




    private void unCoverAllMines() {
        for (CellObject[] rowCellObjectTable : cellObjectTable) { // Iterate through rows
            for (CellObject columnCellObjectTable : rowCellObjectTable) { // Iterate through columns
                if (columnCellObjectTable.isMine()) {
                    columnCellObjectTable.setFlagged(false); //Unflag all mines to enable it to be displayed
                    columnCellObjectTable.setCovered(false); // Uncover all mines to enable it to be displayed
                }
            }
        }
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        //Setting square dimension.. Requirement and followed logic as per pdf
        float width = getMeasuredWidth();
        float height = getMeasuredHeight();
        if (height < width) {
            setMeasuredDimension((int) height, (int) height); // Responsible for rendering square mines.
        } else {
            setMeasuredDimension((int) width, (int) width); // Responsible for rendering square mines.
        }

    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //Get user touch input co-ordinates
        float x = event.getX();
        float y = event.getY();
        //Convert user touch co-ordinates to out cell map
        int row = (int) (x / multiplier);
        int column = (int) (y / multiplier);

        if (!isGameOverFlag()) //Invalidate all clicks after game over
        {
            if (!isUncoverModeSet()) {// Check Mode Goes, inside loop if
                if (cellObjectTable[row][column].isCovered()) { //Checking if cell is covered
                    if (cellObjectTable[row][column].isFlagged()) { //Checking if cell is flagged, unflag if flagged and vice versa
                        cellObjectTable[row][column].setFlagged(false); //Unset Flag and reduce Marked count
                        setMinesMarkedCount(getMinesMarkedCount() - 1); //Reduce marked count
                    } else { //This will be called in marking mode if flag was not set already
                        cellObjectTable[row][column].setFlagged(true); //Set Flag
                        setMinesMarkedCount(getMinesMarkedCount() + 1); //Increase marking count
                    }
                }
            } else {
                if (!cellObjectTable[row][column].isFlagged())  // Check if flagged.
                {
                    if (!cellObjectTable[row][column].isMine()) { //Check if user stepped on mine..!! Oops
                        if (cellObjectTable[row][column].getMineCount() == 0) //If not mine check for count., if zero, we've to appreciate user
                            uncoverCells(row, column); // Appreciating by opening adjacent 0 tiles, till it reach a non zero tile
                        else {
                            cellObjectTable[row][column].setCovered(false); //If not mine, uncover the cell
                            setUnCoveredCount(getUnCoveredCount() + 1); //Increase the uncovered count to track the game progress
                        }
                    } else { //If User Clicked on Mine
                        setGameOverFlag(true);//Sets game is over..!!
                        Toast.makeText(getContext(), "OOPS..!! Better Luck Next Time..!!! Click Reset for new game..", Toast.LENGTH_SHORT).show();
                        //Uncover Mines
                    }
                }
            }
        }
        invalidate(); //Calls onDraw Again forcefully... Why Android not do this automatically...?????
        return super.onTouchEvent(event);
    }

    private void uncoverCells(int row, int col) {
        for (int rowIndex = -1; rowIndex < 2; rowIndex++) //Horizontal Neighbours
        {
            for (int columnIndex = -1; columnIndex < 2; columnIndex++) //Vertical Neighbors
            {
                if (row + rowIndex < 0 || row + rowIndex >= 10 || col + columnIndex < 0 || col + columnIndex >= 10) // Checking corner case for IndexOutOfBounds
                {
                    continue;
                }
                if (cellObjectTable[row + rowIndex][col + columnIndex].isCovered()) { //Check if cell is already covered..

                    if (cellObjectTable[row + rowIndex][col + columnIndex].isFlagged()) { //Remove flagged cells and reduce flag count
                        cellObjectTable[row + rowIndex][col + columnIndex].setFlagged(false);
                        minesMarkedCount--;
                    }
                    cellObjectTable[row + rowIndex][col + columnIndex].setCovered(false); //Uncover mine
                    setUnCoveredCount(getUnCoveredCount() + 1); // Increase uncovered count.. Must not miss track of this
                    if (cellObjectTable[row + rowIndex][col + columnIndex].getMineCount() == 0) { //Check if neighbor cell also have zero
                        uncoverCells(row + rowIndex, col + columnIndex); // Recursion.. Helps here..!! Man.. I used to hate this..
                    }
                }
            }
        }
    }

    // Getters and Setters Start.....!!
    public int getUnCoveredCount() {
        return unCoveredCount;
    }

    public void setUnCoveredCount(int unCoveredCount) {
        this.unCoveredCount = unCoveredCount;
    }

    public boolean isGameOverFlag() {
        return gameOverFlag;
    }

    public void setGameOverFlag(boolean gameOverFlag) {
        this.gameOverFlag = gameOverFlag;
    }

    public boolean isUncoverModeSet() {
        return uncoverModeSet;
    }

    public void setUncoverModeSet(boolean uncoverModeSet) {
        this.uncoverModeSet = uncoverModeSet;
    }

    public int getMinesMarkedCount() {
        return minesMarkedCount;
    }

    public void setMinesMarkedCount(int minesMarkedCount) {
        IOnMineCountChangeEventListener iOnMineCountChangeEventListener = this.iOnMineCountChangeEventListener;
        this.minesMarkedCount = minesMarkedCount;
        iOnMineCountChangeEventListener.onMinesMarkedCountChange(minesMarkedCount); //Call this to notify user of a change..!!!
    }

    public void setCellObjectTable(CellObject[][] cellObjectTable) {
        this.cellObjectTable = cellObjectTable;
    }
    // Getters and Setters End.....!!


    //Custom listener will be implemented in MainActivity so it gets called every time there is change in count
    public interface IOnMineCountChangeEventListener {
        void onMinesMarkedCountChange(int count);
    }

    //Method to be called to get listener event
    public void setIOnMineCountChangeEventListener(IOnMineCountChangeEventListener iOnMineCountChangeEventListener) {
        this.iOnMineCountChangeEventListener = iOnMineCountChangeEventListener;
    }
}
