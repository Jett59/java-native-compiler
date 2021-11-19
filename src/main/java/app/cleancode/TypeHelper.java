package app.cleancode;

public class TypeHelper {
    /*
     * Returns the actual type of a type descriptor. The actual type is the type, without any
     * leading '[' or 'L', and no trailing ';'.
     */
    public static String getActualType(String descriptor) {
        String result = descriptor.replace("[", "").replace(";", "");
        if (result.startsWith("L")) {
            result = result.substring(1);
        }
        return result;
    }

    public static boolean isArrayType(String descriptor) {
        return descriptor.startsWith("[");
    }

    public static boolean isReferenceType(String descriptor) {
        return descriptor.charAt(isArrayType(descriptor) ? 1 : 0) == 'L';
    }

    public static String getCStyleTypeName(String descriptor) {
        boolean isArray = isArrayType(descriptor);
        boolean isReference = isReferenceType(descriptor);
        String actualTypeName = getActualType(descriptor);
        StringBuilder result = new StringBuilder();
        if (isReference) {
            result.append(NameMangler.mangle(actualTypeName));
            result.append("*");
        } else {
            switch (actualTypeName) {
                case "V": {
                    result.append("void");
                    break;
                }
                case "B": {
                    result.append("int8_t");
                    break;
                }
                case "C": {
                    result.append("char");
                    break;
                }
                case "D": {
                    result.append("double");
                    break;
                }
                case "F": {
                    result.append("float");
                    break;
                }
                case "I": {
                    result.append("int32_t");
                    break;
                }
                case "J": {
                    result.append("int64_t");
                    break;
                }
                case "S": {
                    result.append("int16_t");
                    break;
                }
                case "Z": {
                    result.append("bool");
                    break;
                }
            }
        }
        if (isArray) {
            result.append("[]");
        }
        return result.toString();
    }
}
