package server;

import javafx.util.Pair;

import java.security.InvalidParameterException;
import java.util.*;

/**
 * class for parsing string to simple mathematical expressions.
 */
public class MathematicalExpressionParser {

    /**
     * @return Map of supported operators to a pair of (precedence, associativity)
     */
    public static Map<String, Pair<Integer, Boolean>> getOperators() {
        Map<String, Pair<Integer, Boolean>> ops = new HashMap<>();
        ops.put("~", new Pair(3, true));
        ops.put("*", new Pair(2, false));
        ops.put("/", new Pair(2, false));
        ops.put("+", new Pair(1, false));
        ops.put("-", new Pair(1, false));

        return ops;
    }


    /**
     * @param str - a string.
     * @return true if [str] can be parsed to number(Double), false otherwise.
     */
    public static boolean isNumber(String str) {
        try {
            Double.parseDouble(str);
        } catch (NumberFormatException e) {
            return false;
        }
        return true;
    }

    /**
     * @param str - a string.
     * @return null - if 'str' is null.
     * Otherwise - [ArrayList] of permitted tokens in simple mathematical expression.
     * @throws InvalidParameterException - when the given string can't be parsed to simple mathematical expression.
     */
    public static ArrayList<String> exprTokenizer(String str) throws InvalidParameterException {
        if (str == null) {
            return null;
        }

        Map<String, Pair<Integer, Boolean>> ops = getOperators();

        String expr = str.replaceAll("\\s", ""); // remove spaces

        ArrayList<String> tokens = new ArrayList<>();

        StringBuilder tempBuilder = new StringBuilder();
        for (int i = 0; i < expr.length(); i++) {
            char c = expr.charAt(i);

            String cStr = Character.toString(c);
            if (!ops.containsKey(cStr) && !isNumber(cStr) && !cStr.equals(".") && !cStr.equals("(") && !cStr.equals(")")) {
                throw new InvalidParameterException();
            }

            if (c == '.' || Character.isDigit(c)) {
                tempBuilder.append(c);
                continue;
            }

            String temp = tempBuilder.toString();
            if (!temp.equals("")) {
                tokens.add(temp);
                tempBuilder = new StringBuilder();
            }

            tokens.add(String.valueOf(c));
        }

        if (!tempBuilder.toString().equals("")) {
            tokens.add(tempBuilder.toString());
        }

        return tokens;
    }

    /**
     * parsing string with shunting yard algorithm.
     *
     * @param str - a string.
     * @return null - if 'str' is null.
     * Otherwise - [Queue] of parsed string.
     * @throws InvalidParameterException - when the given string can't be parsed to simple mathematical expression.
     */
    // shunting-yard
    public static Queue<String> parseString(String str) throws InvalidParameterException {
        if (str == null) {
            return null;
        }
        ArrayList<String> tokens;
        try {
            tokens = exprTokenizer(str);
        } catch (InvalidParameterException e) {
            throw new InvalidParameterException();
        }

        Queue<String> outQueue = new LinkedList<>();
        Stack<String> opStack = new Stack<>();
        Map<String, Pair<Integer, Boolean>> ops = getOperators();

        for (int i = 0; i < tokens.size(); i++) {
            String token = tokens.get(i);
            if (isNumber(token)) {
                outQueue.add(token);
                continue;
            }

            if (token.equals("-")
                    && ((i == 0)
                    || (tokens.get(i - 1).equals("("))
                    || ops.containsKey(tokens.get(i - 1)))) {
                token = "~";
            }

            if (ops.containsKey(token)) {
                if (opStack.isEmpty() || token.equals("~")) {
                    opStack.push(token);
                    continue;
                }

                Pair<Integer, Boolean> op = ops.get(token);
                String top = opStack.peek();

                while ((!top.equals("(")) &&
                        ((ops.get(top).getKey() > op.getKey())
                                || (ops.get(top).getValue() && (ops.get(top).getKey().equals(op.getKey()))))) {
                    outQueue.add(top);
                    opStack.pop();
                    top = opStack.peek();
                }

                opStack.push(token);
            }

            if (token.equals("(")) {
                opStack.push(token);
            } else if (token.equals(")")) {
                String top = opStack.peek();

                while (!top.equals("(")) {
                    outQueue.add(opStack.peek());
                    opStack.pop();
                    top = opStack.peek();
                }
                opStack.pop();
            }
        }

        while (!opStack.isEmpty()) {
            outQueue.add(opStack.peek());
            opStack.pop();
        }

        return outQueue;
    }


    /**
     * @param str - a string.
     * @return evaluated math expression.
     * @throws InvalidParameterException - when the given string can't be parsed to simple mathematical expression.
     * @throws ArithmeticException       - when trying to divide by zero.
     */
    public static Double evaluate(String str) throws InvalidParameterException, ArithmeticException {
        Queue<String> reversePolish;
        try {
            reversePolish = parseString(str);
        } catch (InvalidParameterException e) {
            throw new InvalidParameterException();
        }

        Stack<Double> stack = new Stack<>();

        while (!reversePolish.isEmpty()) {
            String token = reversePolish.element();
            if (isNumber(token)) {
                stack.push(Double.parseDouble(token));
            } else {
                if (token.equals("~")) {
                    if (stack.isEmpty()) {
                        throw new InvalidParameterException();
                    }
                    stack.push(-stack.pop());

                    reversePolish.remove();
                    continue;
                }

                if (stack.size() < 2) {
                    throw new InvalidParameterException();
                }

                double a = stack.pop();
                double b = stack.pop();

                switch (token) {
                    case "*":
                        stack.push(a * b);
                        break;
                    case "/":
                        if (a == 0) {
                            throw new ArithmeticException();
                        }
                        stack.push(b / a);
                        break;
                    case "+":
                        stack.push((a + b));
                        break;
                    case "-":
                        stack.push(b - a);
                        break;
                    default:
                        throw new InvalidParameterException();
                }
            }
            reversePolish.remove();
        }

        if (stack.isEmpty()) {
            throw new InvalidParameterException();
        }
        return stack.pop();
    }
}
