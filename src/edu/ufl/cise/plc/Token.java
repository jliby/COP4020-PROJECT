package edu.ufl.cise.plc;

class Token implements IToken {

    public Kind type;
    public Object literal;
    int line;
    int column;
    String lexeme;

    boolean isException;
    public Token(Kind type, String lexeme, Object literal, int line, int column, boolean isException) {
        this.type = type;
        this.lexeme = lexeme;
        this.literal = literal;
        this.line = line;
        this.column = column;
        this.isException = isException;
    }

    @Override
    public Kind getKind() {
        return type;
    }

    @Override
    public String getText() {
        return literal.toString();
    }

    @Override
    public SourceLocation getSourceLocation() {
        return new SourceLocation(line, column);
    }

    @Override
    public int getIntValue() {
        int lit = Integer.parseInt(literal.toString());
        return lit;
    }

    @Override
    public float getFloatValue() {
        float lit = Float.parseFloat(literal.toString());
        return lit;
    }

    @Override
    public boolean getBooleanValue() {
        if (literal == "false") {
            return false;
        } else {
            return true;
        }
    }

    @Override
    public String getStringValue() {
        String rawStr = literal.toString();
        String returnStr = "";
        for (int i = 1; i < rawStr.length() - 1; i++) {
            if (rawStr.charAt(i) == '\\') {
                if (rawStr.charAt(i + 1) == 'b') {
                    returnStr += '\b';
                } else if (rawStr.charAt(i + 1) == 't') {
                    returnStr += '\t';
                } else if (rawStr.charAt(i + 1) == 'n') {
                    returnStr += '\n';
                } else if (rawStr.charAt(i + 1) == 'f') {
                    returnStr += '\f';
                } else if (rawStr.charAt(i + 1) == 'r') {
                    returnStr += '\r';
                } else if (rawStr.charAt(i + 1) == '"') {
                    returnStr += '\"';
                } else if (rawStr.charAt(i + 1) == '\'') {
                    returnStr += '\'';
                } else if (rawStr.charAt(i + 1) == '\\') {
                    returnStr += '\\';
                }
                i++;
            } else {
                returnStr += rawStr.charAt(i);
            }
        }
        return returnStr;
    }
}