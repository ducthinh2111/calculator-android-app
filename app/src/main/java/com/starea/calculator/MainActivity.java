package com.starea.calculator;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Process;
import android.text.InputType;
import android.util.Log;
import android.view.ActionMode;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.commons.lang3.math.NumberUtils;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Stack;

public class MainActivity extends AppCompatActivity {

    private TextView result;
    private EditText operation;
    private List<String> function;

    public void process(View view) {
        String input = ((Button) view).getText().toString();

        if (function.contains(input)) {
            switch (input) {
                case "DEL":
                    int startCursor = operation.getSelectionStart();
                    int endCursor = operation.getSelectionEnd();
                    if (operation.getText().length() > 0 && startCursor > 0) {
                        StringBuilder currentText = new StringBuilder(operation.getText().toString());
                        if ((endCursor - startCursor) > 0) {
                            currentText.replace(startCursor, endCursor, "");
                            operation.setText(currentText);
                            operation.setSelection(startCursor);
                        } else {
                            currentText.replace(startCursor-1, startCursor, "");
                            operation.setText(currentText);
                            startCursor--;
                            operation.setSelection(startCursor);
                        }
                        operation.requestFocus();
                    }
                    break;
                case "AC":
                    operation.setText("");
                    result.setText("");
                    result.setHint("0");
                    break;
                case "=":
                    //User has not entered anything
                    if (calculate(operation.getText().toString()) == null) {
                        Toast.makeText(MainActivity.this, "Syntax Error", Toast.LENGTH_SHORT).show();
                    } else {
                        //Format decimal
                        String pattern = "#,###.###";
                        DecimalFormat decimalFormat = new DecimalFormat(pattern);
                        double num = Double.parseDouble(Objects.requireNonNull(calculate(operation.getText().toString())));
                        String str = decimalFormat.format(num);
                        if(str.length()<=18){
                            result.setText(str);
                        }
                        else {
                            result.setText("");
                            result.setHint("∞");
                        }
                    }
                    break;
            }
        } else {
            int startCursor = operation.getSelectionStart();
            int endCursor = operation.getSelectionEnd();
            StringBuilder currentText = new StringBuilder(operation.getText().toString());
            if ((endCursor - startCursor) > 0) {
                currentText.replace(startCursor, endCursor, input);
            } else {
                currentText.insert(startCursor, input);
            }
            startCursor++;
            operation.setText(currentText);
            operation.requestFocus();
            operation.setSelection(startCursor);
        }

    }

    private String calculate(String operation) {

        List<String> o = infixToPostfix(splitOperation(operation));
        if(o == null) {
            return null;
        }
        Stack<String> operands = new Stack<>();

        if (o.contains("(") || o.contains(")") || o.contains(".")) {
            return null;
        }

        for (String str : o) {
            if (NumberUtils.isCreatable(str)) {
                operands.push(str);
            } else if (!str.equals("√")) {
                try {
                    double x2 = Double.parseDouble(operands.pop());
                    double x1 = Double.parseDouble(operands.pop());
                    switch (str) {
                        case "+":
                            double sum = x1 + x2;
                            operands.push(String.valueOf(sum));
                            break;
                        case "-":
                            double difference = x1 - x2;
                            operands.push(String.valueOf(difference));
                            break;
                        case "*":
                            double product = x1 * x2;
                            operands.push(String.valueOf(product));
                            break;
                        case "/":
                            double quotient = x1 / x2;
                            operands.push(String.valueOf(quotient));
                            break;
                        case "%":
                            double remainder = x1 % x2;
                            operands.push(String.valueOf(remainder));
                            break;
                        case "^":
                            double exponent = Math.pow(x1, x2);
                            operands.push(String.valueOf(exponent));
                            break;
                    }
                } catch (Exception e) {
                    return null;
                }
            } else {
                double squareRoot = Math.sqrt(Double.parseDouble(operands.pop()));
                operands.push(String.valueOf(squareRoot));
            }
        }

        if (operands.size() != 1) {
            return null;
        }
        return operands.pop();
    }

    private List<String> infixToPostfix(List<String> operation) {
        List<String> result = new ArrayList<>();
        Stack<String> stack = new Stack<>();

        if(operation == null) {
            return null;
        }

        for (String str : operation) {
            if (NumberUtils.isCreatable(str)) {
                result.add(str);
            } else if (isOperator(str)) {
                while (!stack.isEmpty() && !stack.peek().equals("(") && hasHigherPrec(stack.peek(), str)) {
                    result.add(stack.pop());
                }
                stack.push(str);
            } else if (str.equals("(")) {
                stack.push(str);
            } else if (str.equals(")")) {
                while (!stack.isEmpty() && !stack.peek().equals("(")) {
                    result.add(stack.pop());
                }
                if (!stack.isEmpty()) {
                    stack.pop();
                }
            }
        }

        while (!stack.isEmpty()) {
            result.add(stack.pop());
        }

        return result;
    }

    private boolean isOperator(String str) {
        List<String> operators = new ArrayList<>(Arrays.asList("+", "-", "*", "/", "^", "%", "√"));
        return operators.contains(str);
    }

    private boolean hasHigherPrec(String o1, String o2) {
        return precedence(o1) >= precedence(o2);
    }

    private int precedence(String o) {
        if (o.equals("+") || o.equals("-")) {
            return 2;
        }
        if (o.equals("*") || o.equals("/") || o.equals("%")) {
            return 3;
        }

        if (o.equals("^") || o.equals("√")) {
            return 4;
        }
        return 1;
    }

    private List<String> splitOperation(String str) {
        List<String> result = new ArrayList<>();
        StringBuilder number = new StringBuilder();
        boolean remember = false;
        for (int i = 0; i < str.length(); i++) {
            if (Character.getNumericValue(str.charAt(i)) >= 0 && Character.getNumericValue(str.charAt(i)) <= 9 || str.charAt(i) == '.') {
                number.append(str.charAt(i));
                if (i == str.length() - 1) {
                    result.add(number.toString());
                    number = new StringBuilder();
                    if (remember) {
                        result.add(String.valueOf('√'));
                        remember = false;
                    }
                }
            } else {
                if (number.length() > 0) {
                    result.add(number.toString());
                    number = new StringBuilder();
                }
                if (String.valueOf(str.charAt(i)).equals("√")) {
                    remember = true;
                    if(i == str.length() - 1) {
                        return null;
                    }
                } else {
                    if (remember) {
                        result.add(String.valueOf('√'));
                        remember = false;
                    }
                    result.add(String.valueOf(str.charAt(i)));
                }
            }
        }

        return result;
    }

    //User click Exit button
    public void onBackPressed(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder
                .setMessage("Are you sure?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        // Yes-code
                        moveTaskToBack(true);
                        Process.killProcess(Process.myPid());
                        System.exit(1);
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        Toast.makeText(MainActivity.this, "Nice choice", Toast.LENGTH_SHORT).show();
                        dialog.cancel();
                    }
                })
                .show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        result = findViewById(R.id.result);
        operation = findViewById(R.id.operation);
        function = new ArrayList<>(Arrays.asList("DEL", "AC", "="));
    }
}

