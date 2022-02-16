package app.cleancode.compiler;

import java.util.ArrayList;
import java.util.EmptyStackException;
import java.util.List;
import java.util.Stack;

public class OperandStack {
    private final Stack<List<String>> outputs;
    private final List<OperandStack> nextPossibilities;

    private OperandStack(Stack<List<String>> outputs, List<OperandStack> nextPossibilities) {
        this.outputs = outputs;
        this.nextPossibilities = nextPossibilities;
    }

    public OperandStack() {
        this(new Stack<>(), new ArrayList<>());
    }

    public void push(List<String> output) {
        outputs.push(output);
    }

    public void pop(String destination) {
        if (outputs.size() > 0) {
            outputs.pop().add(destination);
        } else {
            if (nextPossibilities.size() < 1) {
                throw new EmptyStackException();
            } else {
                for (OperandStack stack : nextPossibilities) {
                    stack.pop(destination);
                }
            }
        }
    }

    public void addPossibility(OperandStack possibility) {
        nextPossibilities.add(possibility);
    }

    public boolean isEmpty() {
        if (!outputs.isEmpty()) {
            return false;
        } else {
            boolean empty = true;
            for (OperandStack stack : nextPossibilities) {
                if (!stack.isEmpty()) {
                    empty = false;
                    break;
                }
            }
            return empty;
        }
    }

    public OperandStack possibilityCopy() {
        Stack<List<String>> newOutputs = new Stack<>();
        newOutputs.addAll(outputs);
        OperandStack newOperandStack =
                new OperandStack(newOutputs, new ArrayList<>(nextPossibilities));
        return newOperandStack;
    }
}
