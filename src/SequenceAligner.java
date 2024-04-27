import java.util.Random;

/**
 * TODO: Implement the fillCache(), getResult(), and traceback() methods, in
 * that order. This is the biggest part of this project.
 */

public class SequenceAligner {
    private static Random gen = new Random();

    private String x, y;
    private int n, m;
    private String alignedX, alignedY;
    private Result[][] cache;
    private Judge judge;

    /**
     * Generates a pair of random DNA strands, where x is of length n and
     * y has some length between n/2 and 3n/2, and aligns them using the
     * default judge.
     */
    public SequenceAligner(int n) {
        this(randomDNA(n), randomDNA(n - gen.nextInt(n / 2) * (gen.nextInt(2) * 2 - 1)));
    }

    /**
     * Aligns the given strands using the default judge.
     */
    public SequenceAligner(String x, String y) {
        this(x, y, new Judge());
    }

    /**
     * Aligns the given strands using the specified judge.
     */
    public SequenceAligner(String x, String y, Judge judge) {
        this.x = x.toUpperCase();
        this.y = y.toUpperCase();
        this.judge = judge;
        n = x.length();
        m = y.length();
        cache = new Result[n + 1][m + 1];
        fillCache();
        traceback();
    }

    /**
     * Returns the x strand.
     */
    public String getX() {
        return x;
    }

    /**
     * Returns the y strand.
     */
    public String getY() {
        return y;
    }

    /**
     * Returns the judge associated with this pair.
     */
    public Judge getJudge() {
        return judge;
    }

    /**
     * Returns the aligned version of the x strand.
     */
    public String getAlignedX() {
        return alignedX;
    }

    /**
     * Returns the aligned version of the y strand.
     */
    public String getAlignedY() {
        return alignedY;
    }

    /**
     * TODO: Solve the alignment problem using bottom-up dynamic programming
     * algorithm described in lecture. When you're done, cache[i][j] will hold
     * the result of solving the alignment problem for the first i characters
     * in x and the first j characters in y.
     * <p>
     * Your algorithm must run in O(n * m) time, where n is the length of x
     * and m is the length of y.
     * <p>
     * Ordering convention: So that your code will identify the same alignment
     * as is expected in Testing, we establish the following preferred order
     * of operations: M (diag), I (left), D (up). This only applies when you
     * are picking the operation with the biggest payoff and two or more
     * operations have the same max score.
     */
    private void fillCache() {
        /*
        * Base Case Assignments
        */
        cache[0][0] = new Result(0, Direction.NONE);
        /*Top Row*/
        for(int j = 1; j <= y.length(); j++){
            cache[0][j] = new Result((j * -1), Direction.LEFT);
        }
        /*Left-most Column*/
        for(int i = 1; i <= x.length(); i++){
            cache[i][0] = new Result((i * -1), Direction.UP);
        }

        /*
        * Assignment Loop
        */
        for(int i = 1; i < cache.length; i++){
            for(int j = 1; j < cache[0].length; j++){
                int maxScore = 0;
                Direction direction;
                int matchScore = judge.score(x.charAt(i - 1), y.charAt(j - 1)) + cache[i - 1][j - 1].getScore();
                int insertScore = judge.score(Constants.GAP_CHAR, y.charAt(j - 1)) + cache[i][j - 1].getScore();
                int deleteScore = judge.score(x.charAt(i - 1), Constants.GAP_CHAR) + cache[i - 1][j].getScore();
                //compares matchScore with insertScore, giving us the greater, or the score if they are same
                maxScore = Math.max(matchScore, insertScore);
                //then compares maxScore (now the greater of match and insert) with deleteScore
                maxScore = Math.max(maxScore, deleteScore);
                if(maxScore == matchScore){
                    direction = Direction.DIAGONAL;
                }
                else if(maxScore == insertScore){
                    direction = Direction.LEFT;
                }
                else {
                    direction = Direction.UP;
                }
                cache[i][j]  = new Result(maxScore, direction);
            }
        }
    }

    /**
     * TODO: Returns the result of solving the alignment problem for the
     * first i characters in x and the first j characters in y. You can
     * find the result in O(1) time by looking in your cache.
     */
    public Result getResult(int i, int j) {
        return cache[i][j];
    }

    /**
     * TODO: Mark the path by tracing back through parent pointers, starting
     * with the Result in the lower right corner of the cache. Call Result.markPath()
     * on each Result along the path. The GUI will highlight all such marked cells
     * when you check 'Show path'. As you're tracing back along the path, build
     * the aligned strings in alignedX and alignedY (using Constants.GAP_CHAR
     * to denote a gap in the strand).
     * <p>
     * Your algorithm must run in O(n + m) time, where n is the length of x
     * and m is the length of y.
     */
    private void traceback() {
        alignedX = "";
        alignedY = "";
        int currI = n;
        int currJ = m;
        Result currResult = cache[currI][currJ];
        while(currResult != cache[0][0]){
            currResult.markPath();
            if(currResult.getParent() == Direction.DIAGONAL){
                alignedX = Character.toString(x.charAt(currI - 1)).concat(alignedX);
                alignedY = Character.toString(y.charAt(currJ - 1)).concat(alignedY);
                currI--;
                currJ--;
                currResult = cache[currI][currJ];
            }
            else if(currResult.getParent() == Direction.LEFT){
                alignedX = Character.toString(Constants.GAP_CHAR).concat(alignedX);
                alignedY = Character.toString(y.charAt(currJ - 1)).concat(alignedY);
                currJ--;
                currResult = cache[currI][currJ];
            }
            else if(currResult.getParent() == Direction.UP){
                alignedX = Character.toString(x.charAt(currI - 1)).concat(alignedX);
                alignedY = Character.toString(Constants.GAP_CHAR).concat(alignedY);
                currI--;
                currResult = cache[currI][currJ];
            }
        }
        //cache[0][0] *should* in theory always be the final result being marked
        cache[0][0].markPath();
    }

    /**
     * Returns true iff these strands are seemingly aligned.
     */
    public boolean isAligned() {
        return alignedX != null && alignedY != null &&
                alignedX.length() == alignedY.length();
    }

    /**
     * Returns the score associated with the current alignment.
     */
    public int getScore() {
        if (isAligned())
            return judge.score(alignedX, alignedY);
        return 0;
    }

    /**
     * Returns a nice textual version of this alignment.
     */
    public String toString() {
        if (!isAligned())
            return "[X=" + x + ",Y=" + y + "]";
        final char GAP_SYM = '.', MATCH_SYM = '|', MISMATCH_SYM = ':';
        StringBuilder ans = new StringBuilder();
        ans.append(alignedX).append('\n');
        int n = alignedX.length();
        for (int i = 0; i < n; i++)
            if (alignedX.charAt(i) == Constants.GAP_CHAR || alignedY.charAt(i) == Constants.GAP_CHAR)
                ans.append(GAP_SYM);
            else if (alignedX.charAt(i) == alignedY.charAt(i))
                ans.append(MATCH_SYM);
            else
                ans.append(MISMATCH_SYM);
        ans.append('\n').append(alignedY).append('\n').append("score = ").append(getScore());
        return ans.toString();
    }

    /**
     * Returns a DNA strand of length n with randomly selected nucleotides.
     */
    private static String randomDNA(int n) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < n; i++)
            sb.append("ACGT".charAt(gen.nextInt(4)));
        return sb.toString();
    }

}
