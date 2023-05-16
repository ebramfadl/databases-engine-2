package main.engine;

public class SQLTerm {
    private String tableName;
    private String colName;
    private String operator;
    private Object value;

    public SQLTerm(String tableName, String colName, String operator, Object value) {
        this.tableName = tableName;
        this.colName = colName;
        this.operator = operator;
        this.value = value;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getColName() {
        return colName;
    }

    public void setColName(String colName) {
        this.colName = colName;
    }

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "Table "+tableName +" "+colName+" "+operator+" "+value;
    }
}



