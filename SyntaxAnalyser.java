import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.*;

/**
 * SyntaxAnalyser methods which extends AbstractSyntaxAnalyser.
 * Implement a syntax analyser (SA) for the language using recursive descent recogniser
 *
 * @Author: Andreea Buzatu
 * Student ID: 34683089
 *
 **/

public class SyntaxAnalyser extends AbstractSyntaxAnalyser {

    private String file;
    private ArrayList<String> list = new ArrayList<String>();

    public SyntaxAnalyser (String file){
        try {
            lex = new LexicalAnalyser(file);  // Initalise lexical analyser
            this.file = file;

        } catch (IOException e) {
            System.out.println("Failed to load lexical analyser. " + file);
            System.exit(-1);
        }


    }


    /**
     * Parse the Statement List
     * @throws IOException
     * @throws CompilationException
     */
    private void statementList() throws IOException, CompilationException {
        myGenerate.commenceNonterminal("StatementList"); // Start reading Statement List

        statement(); //Enter statement

        // If nextToken symbol is semicolon then accept semicolon and enter statementList
        while (nextToken.symbol == Token.semicolonSymbol) {
            acceptTerminal(Token.semicolonSymbol);  //accept the semicolon

            statementList();  //parse the statement
        }

        myGenerate.finishNonterminal("StatementList");  // Finish reading Statement list
    }


    /**
     * Parse Statement
     * @throws IOException
     * @throws CompilationException
     */
    private void statement() throws IOException, CompilationException {
        myGenerate.commenceNonterminal("Statement");

        switch (nextToken.symbol) {
            case Token.identifier:
                assignment();                           //Assignment Statement
                break;
            case Token.ifSymbol:
                if_stmt();                              //Is Statement
                break;
            case Token.whileSymbol:
                while_stmt();                           //While Statement
                break;
            case Token.callSymbol:
                procedure();                            //Procedure Statemrnt
                break;
            case Token.untilSymbol:
                until_stmt();                           //Until Statement
                break;
            case Token.forSymbol:
                for_stmt();                             //For Statement
                break;
            default:                                    //Error Handling
                myGenerate.reportError(nextToken,
                        "Error occurred in File: [ " + file + " ] on line [ " + nextToken.lineNumber + " ] " +
                                "\n\t- Expected: [ identifier, if, while , call, until or for ]" +

                                "\n\t- Found : [ " + nextToken.text + " ] Token type [ " + Token.getName(nextToken.symbol) + " ]");

        }

        myGenerate.finishNonterminal("Statement");  //Finish reading Statement
    }


    /**
     * Parse Assignment Statement
     * @throws IOException
     * @throws CompilationException
     */
    private void assignment() throws IOException, CompilationException {
        myGenerate.commenceNonterminal("AssignmetStatement");   // Start reading assignment


        acceptTerminal(Token.identifier);   // accept identifier
        acceptTerminal(Token.becomesSymbol);    //accept the becomes token

        //if the next token is a string accept it, if not, parse the expression
        if (nextToken.symbol == Token.stringConstant){
            acceptTerminal(Token.stringConstant);
            myGenerate.finishNonterminal("AssignmentStatement");
            return;
        }
        else {
            expression();
        }

        myGenerate.finishNonterminal("AssignmentStatement");    //Finish reading assignment
    }


    /**
     * Parse an If Statement
     * @throws IOException
     * @throws CompilationException
     */
    private void if_stmt() throws IOException, CompilationException{
        myGenerate.commenceNonterminal("IfStatement");
        acceptTerminal(Token.ifSymbol); //accept the if token

        condition();    //parse the condition

        acceptTerminal(Token.thenSymbol);   //accept the the then token

        statementList();    //parse the statement list

        //if the next token is an else token then parse the extra components
        if (nextToken.symbol == Token.elseSymbol) {
            acceptTerminal(Token.elseSymbol);   //accept the else token
            statementList();    //parse the statement list
        }

        acceptTerminal(Token.endSymbol);    //accept the end token
        acceptTerminal(Token.ifSymbol);     //accept the if token
        myGenerate.finishNonterminal("IfStatement");
    }


    /**
     * Parse While Statement
     * @throws IOException
     * @throws CompilationException
     */
    private void while_stmt() throws IOException, CompilationException{
        myGenerate.commenceNonterminal("WhileStatement");

        acceptTerminal(Token.whileSymbol);  //accept the while token
        condition();    //parse the condition
        acceptTerminal(Token.loopSymbol);   //accept the loop token
        statementList();    //parse the statement list
        acceptTerminal(Token.endSymbol);    //accept the end token
        acceptTerminal(Token.loopSymbol);   //accept the loop token

        myGenerate.finishNonterminal("WhileStatement");
    }


    /**
     * Parse a Procedure Statement
     * @throws IOException
     * @throws CompilationException
     */
    private void procedure() throws IOException, CompilationException {
        myGenerate.commenceNonterminal("CallStatement");

        acceptTerminal(Token.callSymbol);   //accept the call token
        acceptTerminal(Token.identifier);   //accept the identifier
        acceptTerminal(Token.leftParenthesis);  //accept the left parenthesis

        argumentList(); //parse the argument list

        acceptTerminal(Token.rightParenthesis);  //accept the right parenthesis

        myGenerate.finishNonterminal("CallStatement");
    }


    /**
     * Parse Until Statement
     * @throws IOException
     * @throws CompilationException
     */
    private void until_stmt() throws IOException, CompilationException {
        myGenerate.commenceNonterminal("UntilStatement");

        acceptTerminal(Token.doSymbol); //accept the do token

        statementList();    //parse the statement list

        acceptTerminal(Token.untilSymbol);  //accept the until token

        condition();    //parse the condition

        myGenerate.finishNonterminal("UntilStatement");
    }


    /**
     * Parse For Statement
     * @throws IOException
     * @throws CompilationException
     */
    private void for_stmt() throws IOException, CompilationException {
        myGenerate.commenceNonterminal("ForStatement");

        acceptTerminal(Token.forSymbol);    //accept for token
        acceptTerminal(Token.leftParenthesis);  //accept left paranthesis
        assignment();   //parse assignment

        acceptTerminal(Token.semicolonSymbol);  //accept semicolon token
        condition();    //parse condition

        acceptTerminal(Token.semicolonSymbol);  //accept semicolon token
        assignment();   //parse assignment

        acceptTerminal(Token.rightParenthesis); //accept right parenthesis
        acceptTerminal(Token.doSymbol); //accept do token
        statementList(); //parse statement

        acceptTerminal(Token.endSymbol);    //accept end token
        acceptTerminal(Token.loopSymbol);   //accept loop token

        myGenerate.finishNonterminal("ForStatement");
    }


    /**
     * Parse an Argument List
     * @throws IOException
     * @throws CompilationException
     */
    private void argumentList() throws IOException, CompilationException {
        myGenerate.commenceNonterminal("ArgumentList");
        acceptTerminal(Token.identifier);   //accept the identifier token

        while (nextToken.symbol == Token.commaSymbol) {

            acceptTerminal(Token.commaSymbol);  //accept comma token
            acceptTerminal(Token.identifier);   //accept next identifier token
        }

        myGenerate.finishNonterminal("ArgumentList");
    }


    private void condition() throws IOException, CompilationException {
        myGenerate.commenceNonterminal("Condition");
        acceptTerminal(Token.identifier);   //accept the identifier token

        conditionalOperator();   //parse the conditional operator

        //check the next token from the lexical analyser and call the appropriate accept method
        switch (nextToken.symbol) {
            case Token.identifier:
                acceptTerminal(Token.identifier);
                break;
            case Token.numberConstant:
                acceptTerminal(Token.numberConstant);
                break;
            case Token.stringConstant:
                acceptTerminal(Token.stringConstant);
                break;
            default:                    //error handling
                myGenerate.reportError(nextToken,
                        "Erron occurred on File: [ " + file + " ] on line [ " + nextToken.lineNumber + " ]" +
                                "\n\t- Expected: Condition " +
                                " (" + Token.getName(Token.numberConstant) +
                                " ," + Token.getName(Token.stringConstant) +")" +
                                "\n\t- Found: [ " + nextToken.text + " ] Token type [ " + Token.getName(nextToken.symbol) + " ]"
                );
        }

        myGenerate.finishNonterminal("Condition");
    }


    /**
     * Parse a condition operator
     * @throws IOException
     * @throws CompilationException
     */
    private void conditionalOperator() throws IOException, CompilationException {
        myGenerate.commenceNonterminal("ConditionalOperator");

        //check the next token from the lexical analyser and call the appropriate accept method
        switch (nextToken.symbol) {
            case Token.lessThanSymbol:
                acceptTerminal(Token.lessThanSymbol);
                break;
            case Token.greaterThanSymbol:
                acceptTerminal(Token.greaterThanSymbol);
                break;
            case Token.lessEqualSymbol:
                acceptTerminal(Token.lessEqualSymbol);
                break;
            case Token.greaterEqualSymbol:
                acceptTerminal(Token.greaterEqualSymbol);
                break;
            case Token.equalSymbol:
                acceptTerminal(Token.equalSymbol);
                break;
            case Token.notEqualSymbol:
                acceptTerminal(Token.notEqualSymbol);
                break;
            default:                    //error handling
                myGenerate.reportError(nextToken,
                        "Error occurred in File: [ " + file + " ] on line [ " + nextToken.lineNumber + " ] " +
                                "\n\t- Expected: ConditionalOperator (FIRST) " +
                                " (" + Token.getName(Token.greaterEqualSymbol) +
                                " ," + Token.getName(Token.equalSymbol) +
                                " ," + Token.getName(Token.notEqualSymbol) +
                                " ," + Token.getName(Token.lessThanSymbol) +
                                " ," + Token.getName(Token.lessEqualSymbol) + ")" +
                                "\n\t-Found: [ " + nextToken.text + " ] Token type [ " + Token.getName(nextToken.symbol) +" ] "
                );
        }

        myGenerate.finishNonterminal("ConditionalOperator");
    }


    /**
     * Parse an expression
     * @throws IOException
     * @throws CompilationException
     */
    private void expression() throws IOException, CompilationException {
        myGenerate.commenceNonterminal("Expression");

        term(); //parse the term

        //if there is a plus/minus token check which and accept the token
        //then parse the term
        while (nextToken.symbol == Token.plusSymbol || nextToken.symbol == Token.minusSymbol) {
            if (nextToken.symbol == Token.plusSymbol) {
                acceptTerminal(Token.plusSymbol);
                term();
            }
            if (nextToken.symbol == Token.minusSymbol) {
                acceptTerminal(Token.minusSymbol);
                term();
            }
        }

        myGenerate.finishNonterminal("Expression");
    }


    /**
     * Parse a term
     * @throws IOException
     * @throws CompilationException
     */
    private void term() throws IOException, CompilationException {
        myGenerate.commenceNonterminal("Term");

        factor();//parse the factor

        //if there if a times/divide symbol check which and accept the token
        //then parse the factor
        while (nextToken.symbol == Token.timesSymbol || nextToken.symbol == Token.divideSymbol) {
            if (nextToken.symbol == Token.timesSymbol) {
                acceptTerminal(Token.timesSymbol);
                factor();
            }
            if (nextToken.symbol == Token.divideSymbol) {
                acceptTerminal(Token.divideSymbol);
                factor();
            }
        }

        myGenerate.finishNonterminal("Term");
    }


    /**
     * Parse a factor
     * @throws IOException
     * @throws CompilationException
     */
    private void factor() throws IOException, CompilationException {
        myGenerate.commenceNonterminal("Factor");

        //check the next token from the lexical analyser and call the appropriate accept method
        //in the case of the left parenthesis accept, parse the expression and accept the right parenthesis
        switch (nextToken.symbol) {
            case Token.identifier:
                acceptTerminal(Token.identifier);
                break;
            case Token.numberConstant:
                acceptTerminal(Token.numberConstant);
                break;
            case Token.leftParenthesis:
                acceptTerminal(Token.leftParenthesis);
                expression();
                acceptTerminal(Token.rightParenthesis);
            default:                //error handling
                myGenerate.reportError(nextToken,
                        "Error occurred in File: [ " + file + " ] on line [ " + nextToken.lineNumber + " ] " +
                                "\n\t- Expected: Factor (FIRST) " +
                                " (" + Token.getName(Token.identifier) +
                                " ," + Token.getName(Token.numberConstant) +
                                " ," + Token.getName(Token.leftParenthesis) + ")" +
                                "\n\t- Found [ : " + nextToken.text + " ] Token type [ " + Token.getName(nextToken.symbol) + " ]"
                );
        }

        myGenerate.finishNonterminal("Factor");
    }


    /**
     * Accept the current symbol from the Lexical Analyser
     * @param symbol - is the symbol that is expected
     * @throws IOException
     * @throws CompilationException
     */
    @Override
    public void acceptTerminal (int symbol) throws IOException, CompilationException{

        if (nextToken.symbol == symbol) {
            //insert the symbol into and get the next token
            myGenerate.insertTerminal(nextToken);
            nextToken = lex.getNextToken();
            return;
        }
        else{
            //error handling
            myGenerate.reportError(nextToken,
                    "Error occurred in File: [ " + file + " ] on line [ " + nextToken.lineNumber + " ] " +
                            "\n\t- Expected: " +
                            Token.getName(symbol) +
                            "\n\t- Found: [ " + nextToken.text + " ] Token type [ " + Token.getName(nextToken.symbol) +" ]"

            );
        }
    }


    /**
     * Function to processes the distinguished symbol for the language
     * @throws IOException
     * @throws CompilationException
     */
    @Override
    public void _statementPart_() throws IOException, CompilationException {
        myGenerate.commenceNonterminal("StatementPart");    // Begin parsing

        acceptTerminal(Token.beginSymbol);    // Find 'begin'
        statementList();    // Enters statementList
        acceptTerminal(Token.endSymbol);    // End parsing

        myGenerate.finishNonterminal("StatementPart");
    }
}