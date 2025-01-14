/*
 * Lisa Mekonnen and Kayla Stuhlmann
 * Markov Decision Processes Final Project
 * 
 * CSCI 3420: Optimization and Uncertainty
 * Professor Stephen Majercik 
 */


import java.io.*;
import java.util.*;
import java.util.Map.*;
import java.util.Collections;
import java.util.HashMap;
import java.util.Scanner;
import java.lang.Math;


public class MDP {

    // number of states and actions in the MDP
    private static final int NUM_STATES = 65;
    private static final int NUM_ACTIONS = 4;
    
    // N = go-north, E = go-east, S = go-south, W = go-west
    private static final int N = 0;
    private static final int E = 1;
    private static final int S = 2;
    private static final int W = 3;

    // how many decimal places to print for the utilities 
    // in the printUtilitiesAndPolicy method
    private static final int PRINT_UTILITY_PRECISION = 2;

    // discount for rewards
    private static double discountFactor;

    // maximum error allowed in the utility of any state
    private static double maxStateUtilityError;

    // probability that agent will lose key in Lose-Key? square
    private static double keyLossProbability;

    // reward for transitions into +1-with-key state
    private static double positiveTerminalReward;

    // reward for transition into -1 square
    private static double negativeTerminalReward;
    
    // reward for all other transitions (step cost)
    private static double stepCost;

    // solution technique
    private static String solutionTechnique = "";

    // arrays for transition and reward functions
    private static double[][][] T = new double[NUM_STATES][NUM_ACTIONS][NUM_STATES];
    private static double[] R = new double[NUM_STATES];

    // arrays for state utilities and the current policy
    private static double[] utility = new double[NUM_STATES];
    private static int[] policy = new int[NUM_STATES];

    /* VALUE ITERATION */

    // Backup for value iteration
    public static double vbackup () {

        // Set the delta (change) to 0
        double delta = 0;

        // For all the states in the grid 
        for (int s = 0; s < NUM_STATES ; ++s) {

            // Iterate through all the actions (1-4) and pick the action that has the highest utility
            int action = ActionfromUtilities(s);

            // Update the policy 
            policy[s] = action;

            // With the best action, calculate the utility
            double getUtility = calculateUtility(s, action, discountFactor);

            // Conditional
            if (Math.abs(utility[s] - getUtility) > delta) {

                // Reset delta
                delta = (Math.abs(utility[s] - getUtility));
            }

            // Update our utilities
            utility[s] = getUtility;
        
        }
        return delta;

    }


    // Iteration count for value iteraiton
    public static int valueIteration() {

        int counter = 0;

        // Use this help with the iteration
        double delta = Double.POSITIVE_INFINITY;

        // Iteration loop (keeping track of the number of iterations)
        while (delta > Math.abs(maxStateUtilityError * (1 - discountFactor) / discountFactor)) {

            delta = vbackup();
            counter++;

        }
        return counter;
    }

    // Get the best action by calculating the action with the highest value 
    public static int ActionfromUtilities(int s) {
        
        int maxAction = 0;

        // Max action value (to compare to find the max action)
        double maxActionValue = Double.NEGATIVE_INFINITY;

        // Iterate through all the direction and find the max value
        for (int a = 0; a < NUM_ACTIONS; ++a) {
            
            // Get the utility for that action 
            double current_utility = calculateUtility(s, a, discountFactor);

            // Check to see if this is the max value
            if (current_utility > maxActionValue) {

                // Reset maxActionValue to be that value, and reset maxAction to be the action
                maxActionValue = current_utility;
                maxAction = a;
            }

        }

        // Return the best action 
        return maxAction; 
    }

    // Calculate the utility for that s and that action
    public static double calculateUtility (int s, int action, double discountFactor) {

        // Set a value for the utility value 
        double val = 0;

        // Iterate through the next states and get the utility

        for (int j = 0; j < NUM_STATES ; j++) {

            // Get the expected probability
            double prob = T[s][action][j];

            // Implement the Bellman update formula to calculate the utility
            val += discountFactor * (prob * utility[j]);
        }

        // Implement the step cost when retrieving the rewards
        val += R[s];
        
        // Return the utility value for the next state
        return val;

        }

/*
    public static double utilityOfAction() {
        // given action, calculate utility of that action
    }
*/

    /* POLICY ITERATION */

    public static int policyIteration() {
    // returns the number of iterations policyEvaluation does before reaching optimal policy
        int iterationCount = 0; // use to keep track of how many iterations it takes to get best Policy

        boolean improving = true; 
        // Initialize a random Policy array
        Random rand = new Random();
        for(int i = 0; i < NUM_STATES; i++){
            int randomNum = rand.nextInt(3); // 0-3 represent the different actions
            policy[i] = randomNum;
        }

        while(improving) { // stop when policy is no longer improving
            iterationCount += 1;
            /* Policy Evaluation */
            utility = policyEvaluation(policy, utility);  // maybe we dont need to pass these in since it is w/in the class?

            
            /* Policy Improvement */
            // the utility fxn implies a policy
            int[] newPolicy = new int[NUM_STATES];
            // for each state, calculate expected utility using Bellman equation and possible actions
            for (int i = 0; i < NUM_STATES; i++) {
                //int oldAction = policy[i];
                int newAction = ActionfromUtilities(i);
                
                //int highestUtility = -999999999;
                //for (int j = 0; j < 4; j++) { // for each possible action
                //    double newUtility = calculateUtility(i, j, discountFactor);
                //    if (newUtility > highestUtility) {
                //        newAction = j;
                //    }
                //}
                newPolicy[i] = newAction;
            }
            // if it stops improving, stop policy iteration
            if (Arrays.equals(policy, newPolicy)) {
                improving = false;
            } 
            policy = newPolicy;
        }
        
        return iterationCount;
    }



    // given policy p, calculte the utility of each state if p were to be executed
    public static double[] policyEvaluation(int[] policy, double[] utility) {

        /* make coefficient matrix */
        double[][] coefficients = new double[NUM_STATES][NUM_STATES];
        // initialize coefficients
        for (int i = 0; i < NUM_STATES; i++) {
            for(int j = 0; j < NUM_STATES; j++) {
                if(i == j) {  
                    coefficients[i][j] = 1;  // if state row and column are the same, coefficient should init as 1
                } else {                    //^^ bc of left hand side of bellman equation --> U(s)
                    coefficients[i][j] = 0;  // otherwise, should init as 0
                }
            }
        }
        // use policy to change coefficients for each state's row
        for (int i = 0; i < NUM_STATES; i++) {
            int currentAction = policy[i];
            double[] end_states = T[i][currentAction];  // possible end states when you take currentAction from state i
            // for each possible end state
            for(int j = 0; j < end_states.length; j++) {
                // find the amount of change --> -(probability*discountFactor)
                double changeAmount = -(discountFactor * (T[i][currentAction][j]));
                // update coefficients array at that spot by the change amount
                double oldCoeff = coefficients[i][j];
                coefficients[i][j] = oldCoeff + changeAmount;
            }
        }
        // init matrix
        Matrix C = new Matrix(coefficients); 

        /* make reward matrix */
        double[][] reward2D = new double[NUM_STATES][1]; // make existing 1-D reward array into 2-D array, with NUM-STATES rows
        for (int i = 0; i < NUM_STATES; i++) {
            reward2D[i][0] = R[i];  // copy over values, each row should have a column containing one item
        }
        Matrix R = new Matrix(reward2D);

        // solve for utility matrix if: [coeff. for each utility][utility]=[rewards]
        Matrix U = C.solve(R);     
        // assume utility vector is first element of array
        

        
        double[] newUtility = new double[NUM_STATES];
        double[][] values = U.getArray();
        for(int i = 0; i < NUM_STATES; i++) {
            double utilityValue = values[i][0];
            newUtility[i] = utilityValue;
        }
        utility = newUtility;

        return utility;

    }


    // print out the current utilities and action choices for all states
    // given the utility function and policy passed in.  the utility function
    // is specified as an array of doubles representing utilities for the
    // states, and the policy is specified as an array of integers representing
    // optimal actions for the states
    //
    public static void printUtilitiesAndPolicy(double[] utility, int[] policy) {
	
        // formateString is a C-style format string to use with Java's printf-wannabe
        // method; the format string specifies what the output should look like, including
        // format specifications for values, and the actual items to be printed are
        // the arguments to printf that come after the format string.  in the following,
        // if PRINT_UTILITY_PRECISION is 2, the format string would be:
        //
        //    "%s%2d%s %.2f %s    "
        //
        // This means that the output will be:
        //    a string,
        //    an integer that should be printed in 2 spaces, 
        //    a string,
        //    a space (spaces in the format string are printed literally),
        //    a floating-point number printed in 5 spaces with 
        //          PRINT_UTILITY_PRECISION digits after the decimal point, 
        //    a space,
        //    a string, and
        //    4 spaces.
        //
        // The arguments that come after specify *what* string, *what* integer, etc.
    
        String formatString = "%s%2d%s %5." + PRINT_UTILITY_PRECISION + "f %s    ";
        for(int s = 58 ; s <= 64 ; s += 2)
            System.out.printf(formatString, "(", s, ")", utility[s], "(" + action(policy[s]) + ")");
        System.out.println();
        
        for(int s = 59 ; s <= 63 ; s += 2)
            System.out.printf(formatString, "(", s, ")", utility[s], "(" + action(policy[s]) + ")");
        System.out.println();
        System.out.println();
        
        
        for(int s = 50 ; s <= 56 ; s += 2)
            System.out.printf(formatString, "(", s, ")", utility[s], "(" + action(policy[s]) + ")");
        System.out.println();
        
        for(int s = 51 ; s <= 57 ; s += 2)
            System.out.printf(formatString, "(", s, ")", utility[s], "(" + action(policy[s]) + ")");
        System.out.println();
        System.out.println();
        
        
        for(int s = 40 ; s <= 48 ; s += 2)
            System.out.printf(formatString, "(", s, ")", utility[s], "(" + action(policy[s]) + ")");
        System.out.println();
        
        for(int s = 41 ; s <= 49 ; s += 2)
            System.out.printf(formatString, "(", s, ")", utility[s], "(" + action(policy[s]) + ")");
        System.out.println();
        System.out.println();
        
        
        for(int s = 30 ; s <= 38 ; s += 2)
            System.out.printf(formatString, "(", s, ")", utility[s], "(" + action(policy[s]) + ")");
        System.out.println();
        
        for(int s = 31 ; s <= 39 ; s += 2)
            System.out.printf(formatString, "(", s, ")", utility[s], "(" + action(policy[s]) + ")");
        System.out.println();
        System.out.println();
        
        
        for(int s = 0 ; s <= 14 ; s += 2) {
            if (s < 10)
            System.out.printf(formatString, "(", s, ")", utility[s], "(" + action(policy[s]) + ")");
            else
            System.out.printf(formatString, "(", s, ")", utility[s], "(" + action(policy[s]) + ")");
        }
        System.out.println();
        
        for(int s = 1 ; s <= 15 ; s += 2) {
            if (s < 10)
            System.out.printf(formatString, "(", s, ")", utility[s], "(" + action(policy[s]) + ")");
            else
            System.out.printf(formatString, "(", s, ")", utility[s], "(" + action(policy[s]) + ")");
        }
        System.out.println();
        System.out.println();
        System.out.println();
        
        System.out.print("    ");
        for(int s = 16 ; s <= 28 ; s += 2)
            System.out.printf(formatString, "(", s, ")", utility[s], "(" + action(policy[s]) + ")");
        System.out.println();
        
        System.out.print("    ");
        for(int s = 17 ; s <= 29 ; s += 2)
            System.out.printf(formatString, "(", s, ")", utility[s], "(" + action(policy[s]) + ")");
        System.out.println();
        System.out.println();
    
        } // printUtilitiesAndPolicy
        
    
    
        
        // return appropriate string given action number
        //
        public static String action(int a) {
        
        switch (a) {
            
           case N: return "N";
            
           case E: return "E";
            
           case S: return "S";
            
           case W: return "W";
    
           default: return "X";
    
        }
        
        } // action
    
    
    
        // initialize T, the 3-dimensional array representing the transition 
        // function, and R, the 1-dimensional array representing the reward
        // function
        //
        public static void initializeMDP(double[][][] T, double[] R) {
    
        // set up reward function
            // format = R[state]
        // specifies reward you get given the state you're 
            // starting out in BEFORE a transition
    
        for(int s = 0 ; s < NUM_STATES ; ++s)
            R[s] = stepCost;
    
            // reset the rewards for terminal states
         R[44] = negativeTerminalReward;
         R[45] = negativeTerminalReward;
         R[48] = negativeTerminalReward;
         R[49] = negativeTerminalReward;
    
         R[28] = positiveTerminalReward;
    
    
        // set up transition function
        // format = T[state][action][state']
    
        // initialize all transition probabilities to 0.0
        for(int s1 = 0 ; s1 < NUM_STATES ; ++s1)
            for(int a = 0 ; a < NUM_ACTIONS ; ++a)
            for(int s2 = 0 ; s2 < NUM_STATES ; ++s2)
                T[s1][a][s2] = 0.0;
    
        // reset those transition probabilities that are NOT 0
        T[0][N][0] = 0.1;
        T[0][N][30] = 0.8;
        T[0][N][2] = 0.1;
    
        T[0][E][30] = 0.1;
        T[0][E][2] = 0.8;
        T[0][E][0] = 0.1;
    
        T[0][S][2] = 0.1;
        T[0][S][0] = 0.9;
    
        T[0][W][0] = 0.9;
        T[0][W][30] = 0.1;
    
    
        T[1][N][1] = 0.1;
        T[1][N][31] = 0.8;
        T[1][N][3] = 0.1;
    
        T[1][E][31] = 0.1;
        T[1][E][3] = 0.8;
        T[1][E][1] = 0.1;
    
         T[1][S][3] = 0.1;
         T[1][S][1] = 0.9;
    
        T[1][W][1] = 0.9;
        T[1][W][31] = 0.1;
    
    
        T[2][N][0] = 0.1;
        T[2][N][32] = 0.8;
        T[2][N][4] = 0.1;
    
        T[2][E][32] = 0.1;
        T[2][E][4] = 0.8;
        T[2][E][2] = 0.1;
    
        T[2][S][4] = 0.1;
        T[2][S][2] = 0.8;
        T[2][S][0] = 0.1;
    
        T[2][W][2] = 0.1;
        T[2][W][0] = 0.8;
        T[2][W][32] = 0.1;
    
    
        T[3][N][1] = 0.1;
        T[3][N][33] = 0.8;
        T[3][N][5] = 0.1;
    
        T[3][E][33] = 0.1;
        T[3][E][5] = 0.8;
        T[3][E][3] = 0.1;
    
        T[3][S][5] = 0.1;
        T[3][S][3] = 0.8;
        T[3][S][1] = 0.1;
    
        T[3][W][3] = 0.1;
        T[3][W][1] = 0.8;
        T[3][W][33] = 0.1;
    
    
        T[4][N][2] = 0.1;
        T[4][N][34] = 0.8;
        T[4][N][6] = 0.1;
    
        T[4][E][34] = 0.1;
        T[4][E][6] = 0.8;
        T[4][E][4] = 0.1;
    
        T[4][S][6] = 0.1;
        T[4][S][4] = 0.8;
        T[4][S][2] = 0.1;
    
        T[4][W][4] = 0.1;
        T[4][W][2] = 0.8;
        T[4][W][34] = 0.1;
    
    
        T[5][N][3] = 0.1;
        T[5][N][35] = 0.8;
        T[5][N][7] = 0.1;
    
        T[5][E][35] = 0.1;
        T[5][E][7] = 0.8;
        T[5][E][5] = 0.1;
    
        T[5][S][7] = 0.1;
        T[5][S][5] = 0.8;
        T[5][S][3] = 0.1;
    
        T[5][W][5] = 0.1;
        T[5][W][3] = 0.8;
        T[5][W][35] = 0.1;
    
    
        T[6][N][4] = 0.1;
        T[6][N][36] = 0.8;
        T[6][N][8] = 0.1;
    
        T[6][E][36] = 0.1;
        T[6][E][8] = 0.8;
        T[6][E][6] = 0.1;
    
        T[6][S][8] = 0.1;
        T[6][S][6] = 0.8;
        T[6][S][4] = 0.1;
    
        T[6][W][6] = 0.1;
        T[6][W][4] = 0.8;
        T[6][W][36] = 0.1;
    
    
        T[7][N][5] = 0.1;
        T[7][N][37] = 0.8;
        T[7][N][9] = 0.1;
    
        T[7][E][37] = 0.1;
        T[7][E][9] = 0.8;
        T[7][E][7] = 0.1;
    
        T[7][S][9] = 0.1;
        T[7][S][7] = 0.8;
        T[7][S][5] = 0.1;
    
        T[7][W][7] = 0.1;
        T[7][W][5] = 0.8;
        T[7][W][37] = 0.1;
    
    
        T[8][N][6] = 0.1;
        T[8][N][38] = 0.8;
        T[8][N][10] = 0.1;
    
        T[8][E][38] = 0.1;
        T[8][E][10] = 0.8;
        T[8][E][8] = 0.1;
    
        T[8][S][10] = 0.1;
        T[8][S][8] = 0.8;
        T[8][S][6] = 0.1;
    
        T[8][W][8] = 0.1;
        T[8][W][6] = 0.8;
        T[8][W][38] = 0.1;
    
    
        T[9][N][7] = 0.1;
        T[9][N][39] = 0.8;
        T[9][N][11] = 0.1;
    
        T[9][E][39] = 0.1;
        T[9][E][11] = 0.8;
        T[9][E][9] = 0.1;
    
        T[9][S][11] = 0.1;
        T[9][S][9] = 0.8;
        T[9][S][7] = 0.1;
    
        T[9][W][9] = 0.1;
        T[9][W][7] = 0.8;
        T[9][W][39] = 0.1;
    
    
        T[10][N][8] = 0.1;
        T[10][N][10] = 0.8;
        T[10][N][12] = 0.1;
    
        T[10][E][10] = 0.2;
        T[10][E][12] = 0.8;
    
        T[10][S][12] = 0.1;
        T[10][S][10] = 0.8;
        T[10][S][8] = 0.1;
    
        T[10][W][10] = 0.2;
        T[10][W][8] = 0.8;
    
    
        T[11][N][9] = 0.1;
        T[11][N][11] = 0.8;
        T[11][N][13] = 0.1;
    
        T[11][E][11] = 0.2;
        T[11][E][13] = 0.8;
    
        T[11][S][13] = 0.1;
        T[11][S][11] = 0.8;
        T[11][S][9] = 0.1;
    
        T[11][W][11] = 0.2;
        T[11][W][9] = 0.8;
    
    
        T[12][N][10] = 0.1;
        T[12][N][12] = 0.8;
        T[12][N][14] = 0.1;
    
        T[12][E][12] = 0.2;
        T[12][E][14] = 0.8;
    
        T[12][S][14] = 0.1;
        T[12][S][12] = 0.8;
        T[12][S][10] = 0.1;
    
        T[12][W][12] = 0.2;
        T[12][W][10] = 0.8;
    
    
        T[13][N][11] = 0.1;
        T[13][N][13] = 0.8;
        T[13][N][15] = 0.1;
    
        T[13][E][13] = 0.2;
        T[13][E][15] = 0.8;
    
        T[13][S][15] = 0.1;
        T[13][S][13] = 0.8;
        T[13][S][11] = 0.1;
    
        T[13][W][13] = 0.2;
        T[13][W][11] = 0.8;
    
    
        T[14][N][12] = 0.1;
        T[14][N][14] = 0.8;
        T[14][N][16] = 0.1;
    
        T[14][E][14] = 0.2;
        T[14][E][16] = 0.8;
    
        T[14][S][16] = 0.1;
        T[14][S][14] = 0.8;
        T[14][S][12] = 0.1;
    
        T[14][W][14] = 0.2;
        T[14][W][12] = 0.8;
    
    
        T[15][N][13] = 0.1;
        T[15][N][15] = 0.8;
        T[15][N][17] = 0.1;
    
        T[15][E][15] = 0.2;
        T[15][E][17] = 0.8;
    
        T[15][S][17] = 0.1;
        T[15][S][15] = 0.8;
        T[15][S][13] = 0.1;
    
        T[15][W][15] = 0.2;
        T[15][W][13] = 0.8;
    
    
        T[16][N][14] = 0.1;
        T[16][N][16] = 0.8;
        T[16][N][18] = 0.1;
    
        T[16][E][16] = 0.2;
        T[16][E][18] = 0.8;
    
        T[16][S][18] = 0.1;
        T[16][S][16] = 0.8;
        T[16][S][14] = 0.1;
    
        T[16][W][16] = 0.2;
        T[16][W][14] = 0.8;
    
    
        T[17][N][15] = 0.1;
        T[17][N][17] = 0.8;
        T[17][N][19] = 0.1;
    
        T[17][E][17] = 0.2;
        T[17][E][19] = 0.8;
    
        T[17][S][19] = 0.1;
        T[17][S][17] = 0.8;
        T[17][S][15] = 0.1;
    
        T[17][W][17] = 0.2;
        T[17][W][15] = 0.8;
    
    
        T[18][N][16] = 0.1;
        T[18][N][18] = 0.8;
        T[18][N][20] = 0.1;
    
        T[18][E][18] = 0.2;
        T[18][E][20] = 0.8;
    
        T[18][S][20] = 0.1;
        T[18][S][18] = 0.8;
        T[18][S][16] = 0.1;
    
        T[18][W][18] = 0.2;
        T[18][W][16] = 0.8;
    
    
        T[19][N][17] = 0.1;
        T[19][N][19] = 0.8;
        T[19][N][21] = 0.1;
    
        T[19][E][19] = 0.2;
        T[19][E][21] = 0.8;
    
        T[19][S][21] = 0.1;
        T[19][S][19] = 0.8;
        T[19][S][17] = 0.1;
    
        T[19][W][19] = 0.2;
        T[19][W][17] = 0.8;
    
    
    
        T[20][N][18] = 0.1;
        T[20][N][20] = 0.8;
        T[20][N][22] = 0.1;
    
        T[20][E][20] = 0.2;
        T[20][E][22] = 0.8;
    
        T[20][S][22] = 0.1;
        T[20][S][20] = 0.8;
        T[20][S][18] = 0.1;
    
        T[20][W][20] = 0.2;
        T[20][W][18] = 0.8;
    
    
        T[21][N][19] = 0.1;
        T[21][N][21] = 0.8;
        T[21][N][23] = 0.1;
    
        T[21][E][21] = 0.2;
        T[21][E][23] = 0.8;
    
        T[21][S][23] = 0.1;
        T[21][S][21] = 0.8;
        T[21][S][19] = 0.1;
    
        T[21][W][21] = 0.2;
        T[21][W][19] = 0.8;
    
    
        T[22][N][20] = 0.1;
        T[22][N][22] = 0.8;
        T[22][N][24] = 0.1;
    
        T[22][E][22] = 0.2;
        T[22][E][24] = 0.8;
    
        T[22][S][24] = 0.1;
        T[22][S][22] = 0.8;
        T[22][S][20] = 0.1;
    
        T[22][W][22] = 0.2;
        T[22][W][20] = 0.8;
    
    
        T[23][N][21] = 0.1;
        T[23][N][23] = 0.8;
        T[23][N][25] = 0.1;
    
        T[23][E][23] = 0.2;
        T[23][E][25] = 0.8;
    
        T[23][S][25] = 0.1;
        T[23][S][23] = 0.8;
        T[23][S][21] = 0.1;
    
        T[23][W][23] = 0.2;
        T[23][W][21] = 0.8;
    
    
        T[24][N][22] = 0.1;
        T[24][N][24] = 0.8;
        T[24][N][26] = 0.1;
    
        T[24][E][24] = 0.2;
        T[24][E][26] = 0.8;
    
        T[24][S][26] = 0.1;
        T[24][S][24] = 0.8;
        T[24][S][22] = 0.1;
    
        T[24][W][24] = 0.2;
        T[24][W][22] = 0.8;
    
    
        T[25][N][23] = 0.1;
        T[25][N][25] = 0.8;
        T[25][N][27] = 0.1;
    
        T[25][E][25] = 0.2;
        T[25][E][27] = 0.8;
    
        T[25][S][27] = 0.1;
        T[25][S][25] = 0.8;
        T[25][S][23] = 0.1;
    
        T[25][W][25] = 0.2;
        T[25][W][23] = 0.8;
    
    
        T[26][N][24] = 0.1;
        T[26][N][26] = 0.8;
        T[26][N][28] = 0.1;
    
        T[26][E][26] = 0.2;
        T[26][E][28] = 0.8;
    
        T[26][S][28] = 0.1;
        T[26][S][26] = 0.8;
        T[26][S][24] = 0.1;
    
        T[26][W][26] = 0.2;
        T[26][W][24] = 0.8;
    
    
        T[27][N][25] = 0.1;
        T[27][N][27] = 0.8;
        T[27][N][29] = 0.1;
    
        T[27][E][27] = 0.2;
        T[27][E][29] = 0.8;
    
        T[27][S][29] = 0.1;
        T[27][S][27] = 0.8;
        T[27][S][25] = 0.1;
    
        T[27][W][27] = 0.2;
        T[27][W][25] = 0.8;
    
    
        // no transitions from states 28 and 29
    
    
        T[30][N][30] = 0.1;
        T[30][N][40] = 0.8 * (1.0 - keyLossProbability);
        T[30][N][41] = 0.8 * keyLossProbability;
        T[30][N][32] = 0.1;
    
        T[30][E][40] = 0.1 * (1.0 - keyLossProbability);
        T[30][E][41] = 0.1 * keyLossProbability;
        T[30][E][32] = 0.8;
        T[30][E][0] = 0.1;
    
        T[30][S][32] = 0.1;
        T[30][S][0] = 0.8;
        T[30][S][30] = 0.1;
    
        T[30][W][0] = 0.1;
        T[30][W][30] = 0.8;
        T[30][W][40] = 0.1 * (1.0 - keyLossProbability);
        T[30][W][41] = 0.1 * keyLossProbability;
    
    
        T[31][N][31] = 0.1;
        T[31][N][41] = 0.8;
        T[31][N][33] = 0.1;
    
        T[31][E][41] = 0.1;
        T[31][E][33] = 0.8;
        T[31][E][1] = 0.1;
    
        T[31][S][33] = 0.1;
        T[31][S][1] = 0.8;
        T[31][S][31] = 0.1;
    
        T[31][W][1] = 0.1;
        T[31][W][31] = 0.8;
        T[31][W][41] = 0.1;
    
    
        T[32][N][30] = 0.1;
        T[32][N][42] = 0.8;
        T[32][N][34] = 0.1;
    
        T[32][E][42] = 0.1;
        T[32][E][34] = 0.8;
        T[32][E][2] = 0.1;
    
        T[32][S][34] = 0.1;
        T[32][S][2] = 0.8;
        T[32][S][30] = 0.1;
    
        T[32][W][2] = 0.1;
        T[32][W][30] = 0.8;
        T[32][W][42] = 0.1;
    
    
        T[33][N][31] = 0.1;
        T[33][N][43] = 0.8;
        T[33][N][35] = 0.1;
    
        T[33][E][43] = 0.1;
        T[33][E][35] = 0.8;
        T[33][E][3] = 0.1;
    
        T[33][S][35] = 0.1;
        T[33][S][3] = 0.8;
        T[33][S][31] = 0.1;
    
        T[33][W][3] = 0.1;
        T[33][W][31] = 0.8;
        T[33][W][43] = 0.1;
    
    
        T[34][N][32] = 0.1;
        T[34][N][44] = 0.8;
        T[34][N][36] = 0.1;
    
        T[34][E][44] = 0.1;
        T[34][E][36] = 0.8;
        T[34][E][4] = 0.1;
    
        T[34][S][36] = 0.1;
        T[34][S][4] = 0.8;
        T[34][S][32] = 0.1;
    
        T[34][W][4] = 0.1;
        T[34][W][32] = 0.8;
        T[34][W][44] = 0.1;
    
    
        T[35][N][33] = 0.1;
        T[35][N][45] = 0.8;
        T[35][N][37] = 0.1;
    
        T[35][E][45] = 0.1;
        T[35][E][37] = 0.8;
        T[35][E][5] = 0.1;
    
        T[35][S][37] = 0.1;
        T[35][S][5] = 0.8;
        T[35][S][33] = 0.1;
    
        T[35][W][5] = 0.1;
        T[35][W][33] = 0.8;
        T[35][W][45] = 0.1;
    
    
        T[36][N][34] = 0.1;
        T[36][N][46] = 0.8;
        T[36][N][38] = 0.1;
    
        T[36][E][46] = 0.1;
        T[36][E][38] = 0.8;
        T[36][E][6] = 0.1;
    
        T[36][S][38] = 0.1;
        T[36][S][6] = 0.8;
        T[36][S][34] = 0.1;
    
        T[36][W][6] = 0.1;
        T[36][W][34] = 0.8;
        T[36][W][46] = 0.1;
    
    
        T[37][N][35] = 0.1;
        T[37][N][47] = 0.8;
        T[37][N][39] = 0.1;
    
        T[37][E][47] = 0.1;
        T[37][E][39] = 0.8;
        T[37][E][7] = 0.1;
    
        T[37][S][39] = 0.1;
        T[37][S][7] = 0.8;
        T[37][S][35] = 0.1;
    
        T[37][W][7] = 0.1;
        T[37][W][35] = 0.8;
        T[37][W][47] = 0.1;
    
    
        T[38][N][36] = 0.1;
        T[38][N][48] = 0.8;
        T[38][N][38] = 0.1;
    
        T[38][E][48] = 0.1;
        T[38][E][38] = 0.8;
        T[38][E][8] = 0.1;
    
        T[38][S][38] = 0.1;
        T[38][S][8] = 0.8;
        T[38][S][36] = 0.1;
    
        T[38][W][8] = 0.1;
        T[38][W][36] = 0.8;
        T[38][W][48] = 0.1;
    
    
        T[39][N][37] = 0.1;
        T[39][N][49] = 0.8;
        T[39][N][39] = 0.1;
    
        T[39][E][49] = 0.1;
        T[39][E][39] = 0.8;
        T[39][E][9] = 0.1;
    
        T[39][S][39] = 0.1;
        T[39][S][9] = 0.8;
        T[39][S][37] = 0.1;
    
        T[39][W][9] = 0.1;
        T[39][W][37] = 0.8;
        T[39][W][49] = 0.1;
    
    
        T[40][N][40] = 0.1 * (1.0 - keyLossProbability);
        T[40][N][41] = 0.1 * keyLossProbability;
        T[40][N][50] = 0.8;
        T[40][N][42] = 0.1;
    
        T[40][E][50] = 0.1;
        T[40][E][42] = 0.8;
        T[40][E][30] = 0.1;
    
        T[40][S][42] = 0.1;
        T[40][S][30] = 0.8;
        T[40][S][40] = 0.1 * (1.0 - keyLossProbability);
        T[40][S][41] = 0.1 * keyLossProbability;
    
        T[40][W][30] = 0.1;
        T[40][W][40] = 0.8 * (1.0 - keyLossProbability);
        T[40][W][41] = 0.8 * keyLossProbability;
        T[40][W][50] = 0.1;
    
    
        T[41][N][41] = 0.1;
        T[41][N][51] = 0.8;
        T[41][N][43] = 0.1;
    
        T[41][E][51] = 0.1;
        T[41][E][43] = 0.8;
        T[41][E][31] = 0.1;
    
        T[41][S][43] = 0.1;
        T[41][S][31] = 0.8;
        T[41][S][41] = 0.1;
    
        T[41][W][31] = 0.1;
        T[41][W][41] = 0.8;
        T[41][W][51] = 0.1;
    
    
        T[42][N][40] = 0.1 * (1.0 - keyLossProbability);
        T[42][N][41] = 0.1 * keyLossProbability;
        T[42][N][52] = 0.8;
        T[42][N][44] = 0.1;
    
        T[42][E][52] = 0.1;
        T[42][E][44] = 0.8;
        T[42][E][32] = 0.1;
    
         T[42][S][44] = 0.1;
        T[42][S][32] = 0.8;
          T[42][S][40] = 0.1 * (1.0 - keyLossProbability);
          T[42][S][41] = 0.1 * keyLossProbability;
        
        T[42][W][32] = 0.1;
        T[42][W][40] = 0.8 * (1.0 - keyLossProbability);
        T[42][W][41] = 0.8 * keyLossProbability;
        T[42][W][52] = 0.1;
    
    
        T[43][N][41] = 0.1;
        T[43][N][53] = 0.8;
        T[43][N][45] = 0.1;
    
        T[43][E][53] = 0.1;
        T[43][E][45] = 0.8;
        T[43][E][33] = 0.1;
    
        T[43][S][45] = 0.1;
        T[43][S][33] = 0.8;
        T[43][S][41] = 0.1;
    
        T[43][W][33] = 0.1;
        T[43][W][41] = 0.8;
        T[43][W][53] = 0.1;
    
        
        // no transitions from states 44 and 45
    
    
        T[46][N][44] = 0.1;
        T[46][N][56] = 0.8;
        T[46][N][48] = 0.1;
    
        T[46][E][56] = 0.1;
        T[46][E][48] = 0.8;
        T[46][E][36] = 0.1;
    
        T[46][S][48] = 0.1;
        T[46][S][36] = 0.8;
        T[46][S][44] = 0.1;
    
        T[46][W][36] = 0.1;
        T[46][W][44] = 0.8;
        T[46][W][56] = 0.1;
    
    
        T[47][N][45] = 0.1;
        T[47][N][57] = 0.8;
        T[47][N][49] = 0.1;
    
        T[47][E][57] = 0.1;
        T[47][E][49] = 0.8;
        T[47][E][37] = 0.1;
    
        T[47][S][49] = 0.1;
        T[47][S][37] = 0.8;
        T[47][S][45] = 0.1;
    
        T[47][W][37] = 0.1;
        T[47][W][45] = 0.8;
        T[47][W][57] = 0.1;
    
    
        // no transitions from states 48 and 49
    
    
        T[50][N][50] = 0.1;
        T[50][N][58] = 0.8;
        T[50][N][52] = 0.1;
    
        T[50][E][58] = 0.1;
        T[50][E][52] = 0.8;
        T[50][E][40] = 0.1 * (1.0 - keyLossProbability);
        T[50][E][41] = 0.1 * keyLossProbability;
    
        T[50][S][52] = 0.1;
        T[50][S][40] = 0.8 * (1.0 - keyLossProbability);
        T[50][S][41] = 0.8 * keyLossProbability;
        T[50][S][50] = 0.1;
    
        T[50][W][40] = 0.1 * (1.0 - keyLossProbability);
        T[50][W][41] = 0.1 * keyLossProbability;
        T[50][W][50] = 0.8;
        T[50][W][58] = 0.1;
    
    
        T[51][N][51] = 0.1;
        T[51][N][59] = 0.8;
        T[51][N][53] = 0.1;
    
        T[51][E][59] = 0.1;
        T[51][E][53] = 0.8;
        T[51][E][41] = 0.1;
    
        T[51][S][53] = 0.1;
        T[51][S][41] = 0.8;
        T[51][S][51] = 0.1;
    
        T[51][W][41] = 0.1;
        T[51][W][51] = 0.8;
        T[51][W][59] = 0.1;
    
    
        T[52][N][50] = 0.1;
        T[52][N][60] = 0.8;
        T[52][N][54] = 0.1;
    
        T[52][E][60] = 0.1;
        T[52][E][54] = 0.8;
        T[52][E][42] = 0.1;
    
        T[52][S][54] = 0.1;
        T[52][S][42] = 0.8;
        T[52][S][50] = 0.1;
    
        T[52][W][42] = 0.1;
        T[52][W][50] = 0.8;
        T[52][W][60] = 0.1;
    
    
        T[53][N][51] = 0.1;
        T[53][N][61] = 0.8;
        T[53][N][55] = 0.1;
    
        T[53][E][61] = 0.1;
        T[53][E][55] = 0.8;
        T[53][E][43] = 0.1;
    
        T[53][S][55] = 0.1;
        T[53][S][43] = 0.8;
        T[53][S][51] = 0.1;
    
        T[53][W][43] = 0.1;
        T[53][W][51] = 0.8;
        T[53][W][61] = 0.1;
    
    
        T[54][N][52] = 0.1;
        T[54][N][62] = 0.8;
        T[54][N][56] = 0.1;
    
        T[54][E][62] = 0.1;
        T[54][E][56] = 0.8;
        T[54][E][44] = 0.1;
    
        T[54][S][56] = 0.1;
        T[54][S][44] = 0.8;
        T[54][S][52] = 0.1;
    
        T[54][W][44] = 0.1;
        T[54][W][52] = 0.8;
        T[54][W][62] = 0.1;
    
    
        T[55][N][53] = 0.1;
        T[55][N][63] = 0.8;
        T[55][N][57] = 0.1;
    
        T[55][E][63] = 0.1;
        T[55][E][57] = 0.8;
        T[55][E][45] = 0.1;
    
        T[55][S][57] = 0.1;
        T[55][S][45] = 0.8;
        T[55][S][53] = 0.1;
    
        T[55][W][45] = 0.1;
        T[55][W][53] = 0.8;
        T[55][W][63] = 0.1;
    
    
        T[56][N][54] = 0.1;
        T[56][N][64] = 0.8;
        T[56][N][56] = 0.1;
    
        T[56][E][64] = 0.1;
        T[56][E][56] = 0.8;
        T[56][E][46] = 0.1;
    
        T[56][S][56] = 0.1;
        T[56][S][46] = 0.8;
        T[56][S][54] = 0.1;
    
        T[56][W][46] = 0.1;
        T[56][W][54] = 0.8;
        T[56][W][64] = 0.1;
    
    
         T[57][N][55] = 0.1;
         T[57][N][64] = 0.8;
         T[57][N][57] = 0.1;
    
        T[57][E][64] = 0.1;
        T[57][E][57] = 0.8;
        T[57][E][47] = 0.1;
    
        T[57][S][57] = 0.1;
        T[57][S][47] = 0.8;
        T[57][S][55] = 0.1;
    
        T[57][W][47] = 0.1;
        T[57][W][55] = 0.8;
        T[57][W][64] = 0.1;
    
    
        T[58][N][58] = 0.9;
        T[58][N][60] = 0.1;
    
        T[58][E][58] = 0.1;
        T[58][E][60] = 0.8;
        T[58][E][50] = 0.1;
    
        T[58][S][60] = 0.1;
        T[58][S][50] = 0.8;
        T[58][S][58] = 0.1;
    
        T[58][W][50] = 0.1;
        T[58][W][58] = 0.9;
    
    
        T[59][N][59] = 0.9;
        T[59][N][61] = 0.1;
    
        T[59][E][59] = 0.1;
        T[59][E][61] = 0.8;
        T[59][E][51] = 0.1;
    
        T[59][S][61] = 0.1;
        T[59][S][51] = 0.8;
        T[59][S][59] = 0.1;
    
        T[59][W][51] = 0.1;
        T[59][W][59] = 0.9;
    
    
    
        T[60][N][58] = 0.1;
        T[60][N][60] = 0.8;
        T[60][N][62] = 0.1;
    
        T[60][E][60] = 0.1;
        T[60][E][62] = 0.8;
        T[60][E][52] = 0.1;
    
        T[60][S][62] = 0.1;
        T[60][S][52] = 0.8;
        T[60][S][58] = 0.1;
    
        T[60][W][52] = 0.1;
        T[60][W][58] = 0.8;
        T[60][W][60] = 0.1;
    
    
        T[61][N][59] = 0.1;
        T[61][N][61] = 0.8;
        T[61][N][63] = 0.1;
    
        T[61][E][61] = 0.1;
        T[61][E][63] = 0.8;
        T[61][E][53] = 0.1;
    
        T[61][S][63] = 0.1;
        T[61][S][53] = 0.8;
        T[61][S][59] = 0.1;
    
        T[61][W][53] = 0.1;
        T[61][W][59] = 0.8;
        T[61][W][61] = 0.1;
    
    
        T[62][N][60] = 0.1;
        T[62][N][62] = 0.8;
        T[62][N][64] = 0.1;
    
        T[62][E][62] = 0.1;
        T[62][E][64] = 0.8;
        T[62][E][54] = 0.1;
    
        T[62][S][64] = 0.1;
        T[62][S][54] = 0.8;
        T[62][S][60] = 0.1;
    
        T[62][W][54] = 0.1;
        T[62][W][60] = 0.8;
        T[62][W][62] = 0.1;
    
    
        T[63][N][61] = 0.1;
        T[63][N][63] = 0.8;
        T[63][N][64] = 0.1;
    
        T[63][E][63] = 0.1;
        T[63][E][64] = 0.8;
        T[63][E][55] = 0.1;
    
        T[63][S][64] = 0.1;
        T[63][S][55] = 0.8;
        T[63][S][61] = 0.1;
    
        T[63][W][55] = 0.1;
        T[63][W][61] = 0.8;
        T[63][W][63] = 0.1;
    
    
        T[64][N][62] = 0.1;
        T[64][N][64] = 0.9;
    
        T[64][E][64] = 0.9;
        T[64][E][56] = 0.1;
    
        T[64][S][64] = 0.1;
        T[64][S][56] = 0.8;
        T[64][S][62] = 0.1;
    
        T[64][W][56] = 0.1;
        T[64][W][62] = 0.8;
        T[64][W][64] = 0.1;

        } // initMDP

    public static void main(String[] args) {
        
        // java MDP 0.99 1e-6 0.5 1 -1 -0.04 v
        // Allow the user to specify the following parameters on the command line
        // Numerical input
        discountFactor = Double.parseDouble(args[0]);
        maxStateUtilityError = Double.parseDouble(args[1]);
        keyLossProbability = Double.parseDouble(args[2]);
        positiveTerminalReward = Double.parseDouble(args[3]);
        negativeTerminalReward = Double.parseDouble(args[4]);
        stepCost = Double.parseDouble(args[5]);
        // String input
        solutionTechnique = args[6];
        
        // FOR TESTING
        /*
        discountFactor = 0.99;
        maxStateUtilityError = 1e-6;
        keyLossProbability = 0.5;
        positiveTerminalReward = 1;
        negativeTerminalReward = -1;
        stepCost = -0.04;

        solutionTechnique = "p";
        */

        // Initalize the MDP
        initializeMDP(T, R);

        // Check which solution technique they use
        if (args[6].charAt(0) == 'v') {

            // Starting the timer for value iteration
            long startTime = System.currentTimeMillis();

            // Call the function that is running value iteration with its helper functions
            int iterations = valueIteration();

            // End the time after the iteration is finished
            long endTime = System.currentTimeMillis();

            // Change the variable to use for output
            solutionTechnique = "Value Iteration";
    
            // Print out the following lines
            System.out.println("Solution Technique: " + solutionTechnique);
            
            System.out.println("Discount Factor: " + discountFactor);

            System.out.println("Maximum State Error: " + maxStateUtilityError);

            System.out.println("Key Loss Probability: " + keyLossProbability);

            System.out.println("Positive Terminal State Reward: " + positiveTerminalReward);

            System.out.println("Negative Terminal State Reward: " + negativeTerminalReward);

            System.out.println("Step cost: " + stepCost);

            System.out.println("The number of iterations in that run: " + String.valueOf(iterations));

            System.out.println("The time taken to run this solution technique: " + (endTime - startTime) + " milliseconds");

            // Print out the utility and policy
            printUtilitiesAndPolicy(utility, policy);

        }
        else if (args[6].charAt(0) == 'p') {

            // Starting the timer for value iteration
            long startTime = System.currentTimeMillis();

            // Call the function that is running value iteration with its helper functions
            int numIterations = policyIteration();

            // End the time after the iteration is finished
            long endTime = System.currentTimeMillis();

            // Change the variable to use for output
            // solutionTechnique = "Policy Iteration";
    
            // Print out the following lines
            System.out.println("Solution Technique: " + solutionTechnique);
            
            System.out.println("Discount Factor: " + discountFactor);

            System.out.println("Maximum State Error: " + maxStateUtilityError);

            System.out.println("Key Loss Probability: " + keyLossProbability);

            System.out.println("Positive Terminal State Reward: " + positiveTerminalReward);

            System.out.println("Negative Terminal State Reward: " + negativeTerminalReward);

            System.out.println("Step cost: " + stepCost);

            System.out.println("The number of iterations in that run: " + String.valueOf(numIterations));

            System.out.println("The time taken to run this solution technique: " + (endTime - startTime) + " milliseconds");

            // Print out the utility and policy
            printUtilitiesAndPolicy(utility, policy);

        }
        else {

            // Get the new solution technique
            System.out.println("Try again. Enter the solution technique (v for Value Iteration and p for Policy Iteration): ");

        }
    
    }

    }

    
