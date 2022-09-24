package server;


import java.security.InvalidParameterException;


/**
 * A processor of simple calculator for server's requests.
 */
public class SimpleCalculatorProcessor implements RequestProcessor {

    @Override
    public String process(String input) throws InvalidParameterException{
        double res;
        try{
           res = MathematicalExpressionParser.evaluate(input);
        }catch (InvalidParameterException | ArithmeticException e)
        {
            throw new InvalidParameterException();
        }

        if((res % 1) > 0)
        {
            return Double.toString(res);
        }
        return Integer.toString((int)res);
    }

}
