package app.cleancode.compiler;

import java.util.ArrayList;
import java.util.EmptyStackException;
import java.util.List;
import java.util.Stack;
import app.cleancode.Pair;

public class OperandStack {
    private final Stack<Pair<String, List<String>>> outputs;
    private final List<OperandStack> nextPossibilities;

    private OperandStack(Stack<Pair<String, List<String>>> outputs,
            List<OperandStack> nextPossibilities) {
        this.outputs = outputs;
        this.nextPossibilities = nextPossibilities;
    }

    public OperandStack() {
        this(new Stack<>(), new ArrayList<>());
    }

    public void push(List<String> output, String type) {
        outputs.push(new Pair<String, List<String>>(type, output));
    }

    /*
     * @return the type of the element just popped
     */
    public String pop(String destination) {
        if (outputs.size() > 0) {
            Pair<String, List<String>> element = outputs.pop();
            element.b().add(destination);
            return element.a();
        } else {
            if (nextPossibilities.size() < 1) {
                throw new EmptyStackException();
            } else {
                String type = null;
                for (OperandStack stack : nextPossibilities) {
                    String newType = stack.pop(destination);
                    if (type == null) {
                        type = newType;
                    } else if (!type.equals(newType)) {
                        throw new IllegalStateException("Disagreement on type of stack element");
                    }
                }
                return type;
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
        Stack<Pair<String, List<String>>> newOutputs = new Stack<>();
        newOutputs.addAll(outputs);
        OperandStack newOperandStack =
                new OperandStack(newOutputs, new ArrayList<>(nextPossibilities));
        return newOperandStack;
    }
}
