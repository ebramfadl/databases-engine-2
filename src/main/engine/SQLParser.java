package main.engine;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class SQLParser {

    public  Object[] prepareToInsert(StringBuffer sqlStatement){
        String tableName = null;

        // Extract table name
        Pattern pattern = Pattern.compile("\\s*INSERT\\s+INTO\\s+(\\w+)", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(sqlStatement.toString());

        if (matcher.find()) {
            tableName = matcher.group(1);
        }

        ArrayList<String> extractedColumns = extractColumnNames(sqlStatement.toString());
        ArrayList<String> extractedValues = extractValues(sqlStatement.toString());
        Hashtable<String,Object> htblColNameVal = new Hashtable<>();

        for(int i = 0 ; i < extractedColumns.size() ; i++){
            if(extractedValues.get(i).matches("[a-zA-Z]+")){ //String
                htblColNameVal.put(extractedColumns.get(i),extractedValues.get(i));
            }
            else if(extractedValues.get(i).matches("\\d+")){ //String
                htblColNameVal.put(extractedColumns.get(i),Integer.parseInt(extractedValues.get(i)));
            }
            else{
                try {
                    Double.parseDouble(extractedValues.get(i));
                    htblColNameVal.put(extractedColumns.get(i),Double.parseDouble(extractedValues.get(i)));

                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
            }
        }

        return new Object[]{tableName,htblColNameVal};
    }

    public static ArrayList<String> extractColumnNames(String sqlStatement) {
        ArrayList<String> columnNames = new ArrayList<>();

        // Extract column names
        String statement = sqlStatement.toString().replaceAll("\\s+", "");
        Pattern pattern = Pattern.compile("\\((.*?)\\)",Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(statement);

        if (matcher.find()) {
            String columnList = matcher.group(1);
            String[] columns = columnList.split(",");

            for (String column : columns) {
                String columnName = column.trim();
                columnNames.add(columnName);
            }
        }

        return columnNames;
    }
    public static ArrayList<String> extractValues(String sqlStatement) {
        ArrayList<String> values = new ArrayList<>();

        // Extract values
        String statement = sqlStatement.toString().replaceAll("\\s+", "");
        Pattern pattern = Pattern.compile("VALUES\\((.*?)\\)",Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(statement);

        if (matcher.find()) {
            String valuesList = matcher.group(1);
            String[] valueTokens = valuesList.split(",");

            for (String token : valueTokens) {
                String value = token.trim().replaceAll("'", "");
                values.add(value);
            }
        }

        return values;
    }

    public static ArrayList<String> extractColumnsToUpdate(StringBuffer sqlStatement) {
        ArrayList<String> columns = new ArrayList<>();


        // Regular expression pattern to match column names
//        String statement = sqlStatement.toString().replaceAll("\\s+", "");
        String pattern = "\\bSET\\b\\s+([\\w,\\s=']+)";
        Pattern regex = Pattern.compile(pattern,Pattern.CASE_INSENSITIVE);
        Matcher matcher = regex.matcher(sqlStatement);

        if (matcher.find()) {
            String columnClause = matcher.group(1);
            String[] columnArray = columnClause.split(",");

            for (String column : columnArray) {
                String trimmedColumn = column.trim().split("\\s*=\\s*")[0];
                if (!trimmedColumn.isEmpty())
                    columns.add(trimmedColumn);
            }
        }

        return columns;
    }

    public static ArrayList<String> extractValuesToUpdate(StringBuffer sqlStatement) {
        ArrayList<String> values = new ArrayList<>();

        // Regular expression pattern to match values
        String pattern = "=\\s*([^,\\s]+)";
        Pattern regex = Pattern.compile(pattern,Pattern.CASE_INSENSITIVE);
        Matcher matcher = regex.matcher(sqlStatement);

        while (matcher.find()) {
            String value = matcher.group(1);
            values.add(value);
        }
        return  values;
    }

    public  Object[] prepareToUpdate(StringBuffer sqlStatement) {
        String tableName = null;

        // Extract table name

        Pattern pattern = Pattern.compile("\\s*UPDATE\\s+(\\w+)", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(sqlStatement.toString());

        if (matcher.find()) {
            tableName = matcher.group(1);
        }

        ArrayList<String> names = extractColumnsToUpdate(sqlStatement);
        ArrayList<String> values = extractValuesToUpdate(sqlStatement);
        Hashtable<String,Object> htblColNameVal = new Hashtable<>();

        for(int i = 0 ; i < names.size() ; i++){
            if(values.get(i).matches("'?[a-zA-Z]+'?")){ //String
                htblColNameVal.put(names.get(i),values.get(i).substring(1,values.get(i).length()-1));
            }
            else if(values.get(i).matches("\\d+")){ //String
                htblColNameVal.put(names.get(i),Integer.parseInt(values.get(i)));
            }
            else{
                try {
                    Double.parseDouble(values.get(i));
                    htblColNameVal.put(names.get(i),Double.parseDouble(values.get(i)));

                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
            }
        }

        String pk = values.get(values.size()-1);
        values.remove(values.size()-1);
        return new Object[]{tableName,pk,htblColNameVal};

    }

    public static ArrayList<String> extractColumnsToDelete(StringBuffer sqlStatement) {
        ArrayList<String> columns = new ArrayList<>();

        // Regular expression pattern to match column names
        String pattern = "\\bWHERE\\b\\s+([\\w,\\s=']+)";
        Pattern regex = Pattern.compile(pattern,Pattern.CASE_INSENSITIVE);
        Matcher matcher = regex.matcher(sqlStatement);

        if (matcher.find()) {
            String columnClause = matcher.group(1);
            String[] columnArray = columnClause.split("\\s+AND\\s+",Pattern.CASE_INSENSITIVE);

            for (String column : columnArray) {
                String trimmedColumn = column.trim().split("\\s*=\\s*")[0];
                if (!trimmedColumn.isEmpty())
                    columns.add(trimmedColumn);
            }
        }

        return columns;
    }

    public static ArrayList<String> extractValuesToDelete(StringBuffer sqlStatement) {
        ArrayList<String> values = new ArrayList<>();

        // Regular expression pattern to match values
        String pattern = "=\\s*([^,\\s]+)";
        Pattern regex = Pattern.compile(pattern);
        Matcher matcher = regex.matcher(sqlStatement);

        while (matcher.find()) {
            String value = matcher.group(1);
            values.add(value);
        }

        return values;
    }

    public Object[] prepareToDelete(StringBuffer sqlStatement) {
        String tableName = null;

        // Extract table name
        Pattern pattern = Pattern.compile("\\s*DELETE\\s*FROM\\s* (\\w+)",Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(sqlStatement.toString());

        if (matcher.find()) {
            tableName = matcher.group(1);
        }

        ArrayList<String> names = extractColumnsToDelete(sqlStatement);
        ArrayList<String> values = extractValuesToDelete(sqlStatement);
        Hashtable<String,Object> htblColNameVal = new Hashtable<>();
        System.out.println(names);
        System.out.println(values);
        for(int i = 0 ; i < names.size() ; i++){
            if(values.get(i).matches("'?[a-zA-Z]+'?")){ //String
                htblColNameVal.put(names.get(i),values.get(i).substring(1,values.get(i).length()-1));
            }
            else if(values.get(i).matches("\\d+")){ //String
                htblColNameVal.put(names.get(i),Integer.parseInt(values.get(i)));
            }
            else{
                try {
                    Double.parseDouble(values.get(i));
                    htblColNameVal.put(names.get(i),Double.parseDouble(values.get(i)));

                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
            }
        }

        return new Object[]{tableName,htblColNameVal};

    }

    public static String[] extractOperators(StringBuffer sqlStatement) {
        ArrayList<String> operators = new ArrayList<>();

        // Regular expression pattern to match operators
        String pattern = "\\b(AND|OR|XOR)\\b";
        Pattern regex = Pattern.compile(pattern,Pattern.CASE_INSENSITIVE);
        Matcher matcher = regex.matcher(sqlStatement);

        while (matcher.find()) {
            String operator = matcher.group();
            operators.add(operator);
        }

        ArrayList<String> uppercasedStrings = new ArrayList<>();
        for (String str : operators) {
            uppercasedStrings.add(str.toUpperCase());
        }

        return uppercasedStrings.toArray(new String[0]);
    }

    public static SQLTerm[] convertToSQLTerms(StringBuffer sqlStatement) {
        String tableNamePattern = "\\bFROM\\s+(\\w+)\\b";
        Pattern tableNameRegex = Pattern.compile(tableNamePattern, Pattern.CASE_INSENSITIVE);
        Matcher tableNameMatcher = tableNameRegex.matcher(sqlStatement);

        String table = null;
        if (tableNameMatcher.find()) {
            table = tableNameMatcher.group(1);
        }

        List<SQLTerm> sqlTerms = new ArrayList<>();

        // Regular expression pattern to match conditions
        String pattern = "\\b(\\w+)\\s*([=<>]+)\\s*([^\\s']+|'[^']*')";
        Pattern regex = Pattern.compile(pattern);
        Matcher matcher = regex.matcher(sqlStatement);

        while (matcher.find()) {
            String colName = matcher.group(1);
            String operator = matcher.group(2);
            String value = matcher.group(3).replaceAll("'", "");

            SQLTerm sqlTerm = new SQLTerm(table, colName, operator, value);
            sqlTerms.add(sqlTerm);
        }

        return sqlTerms.toArray(new SQLTerm[0]);
    }


    public Object[] prepareToCreateTable(StringBuffer sqlStatement) throws DBAppException {
        String tableName = null;

        // Extract table name
        Pattern pattern = Pattern.compile("\\s*CREATE\\s*TABLE\\s* (\\w+)",Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(sqlStatement.toString());

        if (matcher.find()) {
            tableName = matcher.group(1);
        }

        String pk = getPrimaryKeyName(sqlStatement);

        Hashtable<String,String> htblColNameType = new Hashtable<>();
        Hashtable<String,String> htblColNameMin = new Hashtable<>();
        Hashtable<String,String> htblColNameMax = new Hashtable<>();

        ArrayList<String> names = extractColumnNamesToCreateTable(sqlStatement);
        ArrayList<String> types = extractDataTypes(sqlStatement);
        ArrayList<String> min = extractMinValues(sqlStatement);
        ArrayList<String> max = extractMaxValues(sqlStatement);

        for (int i = 0; i < names.size() ; i++){
            if(types.get(i).equalsIgnoreCase("varchar")){
                htblColNameType.put(names.get(i),"java.lang.String");
            }
            else if(types.get(i).equalsIgnoreCase("int")){
                htblColNameType.put(names.get(i),"java.lang.Integer");
            }
            else if(types.get(i).equalsIgnoreCase("double")){
                htblColNameType.put(names.get(i),"java.lang.Double");
            }
            else if(types.get(i).equalsIgnoreCase("datetime")){
                htblColNameType.put(names.get(i),"java.lang.Date");
            }

            htblColNameMin.put(names.get(i),min.get(i));
            htblColNameMax.put(names.get(i),max.get(i));
        }

        return new Object[]{tableName,pk,htblColNameType,htblColNameMin,htblColNameMax};
    }

    public static String getPrimaryKeyName(StringBuffer sqlStatement) throws DBAppException {
        String patternString = "\\s(\\w+)\\s+\\w+\\s+primarykey";
        Pattern pattern = Pattern.compile(patternString,Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(sqlStatement);

        if (matcher.find()) {
            return matcher.group(1);
        }

        return null;
    }

    private static ArrayList<String> extractColumnNamesToCreateTable(StringBuffer sqlStatement) {
        ArrayList<String> columnNames = new ArrayList<>();

        String statement = sqlStatement.toString();
        int startIndex = statement.indexOf("(");
        int endIndex = statement.indexOf(")");
        if (startIndex == -1 || endIndex == -1) {
            return columnNames;  // Return empty list if no column definitions found
        }
        String columnDefinitions = statement.substring(startIndex + 1, endIndex);
        String[] columns = columnDefinitions.split(",");
        for (String column : columns) {
            column = column.trim();
            if (column.contains("primarykey")) {
                String[] primaryKeyParts = column.split("\\s+");
                if (primaryKeyParts.length >= 2) {
                    columnNames.add(primaryKeyParts[0]);
                }
            } else {
                String[] columnParts = column.split("\\s+");
                if (columnParts.length >= 1) {
                    columnNames.add(columnParts[0]);
                }
            }
        }
        columnNames.remove(columnNames.size()-1);
        return columnNames;
    }


    private static ArrayList<String> extractDataTypes(StringBuffer sqlStatement) {
        ArrayList<String> dataTypes = new ArrayList<>();

        Pattern pattern = Pattern.compile("\\((.*?)\\)");
        Matcher matcher = pattern.matcher(sqlStatement.toString());

        if (matcher.find()) {
            String columnDefinitions = matcher.group(1);

            // Split the column definitions by comma
            String[] columns = columnDefinitions.split(",");

            // Extract the data types from each column definition
            for (String column : columns) {
                String dataType = column.trim().split("\\s+")[1];
                dataTypes.add(dataType);
            }
        }
        dataTypes.remove(dataTypes.size()-1);
        return dataTypes;
    }

    private static ArrayList<String> extractMinValues(StringBuffer sqlStatement) {
        ArrayList<String> datatypes = new ArrayList<>();
        ArrayList<String> minValues = new ArrayList<>();

        Pattern datatypePattern = Pattern.compile("(int|varchar|double|datetime)",Pattern.CASE_INSENSITIVE);
        Matcher datatypeMatcher = datatypePattern.matcher(sqlStatement);
        while (datatypeMatcher.find()) {
            datatypes.add(datatypeMatcher.group());
        }

        Pattern minValuePattern = Pattern.compile("CHECK \\([^<>=]*>=\\s*([^,\\s]+)");
        Matcher minValueMatcher = minValuePattern.matcher(sqlStatement);
        while (minValueMatcher.find()) {
            minValues.add(minValueMatcher.group(1));
        }
        return minValues;
    }

    private static ArrayList<String> extractMaxValues(StringBuffer sqlStatement) {
        ArrayList<String> datatypes = new ArrayList<>();
        ArrayList<String> minValues = new ArrayList<>();
        ArrayList<String> maxValues = new ArrayList<>();

        Pattern datatypePattern = Pattern.compile("(int|varchar|double|datetime)",Pattern.CASE_INSENSITIVE);
        Matcher datatypeMatcher = datatypePattern.matcher(sqlStatement);
        while (datatypeMatcher.find()) {
            datatypes.add(datatypeMatcher.group());
        }

        Pattern minValuePattern = Pattern.compile("CHECK \\([^<>=]*>=\\s*([^,\\s]+)",Pattern.CASE_INSENSITIVE);
        Matcher minValueMatcher = minValuePattern.matcher(sqlStatement);
        while (minValueMatcher.find()) {
            minValues.add(minValueMatcher.group(1));
        }

        Pattern maxValuePattern = Pattern.compile("<=\\s*([^,\\s)]+)");
        Matcher maxValueMatcher = maxValuePattern.matcher(sqlStatement);
        while (maxValueMatcher.find()) {
            maxValues.add(maxValueMatcher.group(1));
        }
        return maxValues;
    }

    public  Object[] prepareToCreateIndex(StringBuffer sqlStatement){
        Pattern pattern = Pattern.compile("\\s*CREATE\\s*INDEX\\s*ON\\s*(\\w+)",Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(sqlStatement.toString());
        String tableName = "";
        if (matcher.find()) {
            tableName = matcher.group(1);
            System.out.println("Table Name: " + tableName);
        }

        return new Object[]{tableName,extractDimensions(sqlStatement).toArray(new String[0])};

    }

    private static ArrayList<String> extractDimensions(StringBuffer sqlStatement) {
        ArrayList<String> dimensionList = new ArrayList<>();
        Pattern pattern = Pattern.compile("\\s*CREATE\\s*INDEX\\s*ON\\s*\\w+ \\(([^)]+)\\)",Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(sqlStatement);

        if (matcher.find()) {
            String dimensions = matcher.group(1);
            String[] parts = dimensions.split("\\s*,\\s*");
            for (String part : parts) {
                dimensionList.add(part.trim());
            }
        }

        return dimensionList;
    }

    public String getOperation(StringBuffer sqlStatement) throws DBAppException {
//        String statement = sqlStatement.toString().replaceAll("\\s+", "");
//        String[] words = statement.split("\\b");
//        if (words.length > 0) {
//            return words[0];
//        }
//        return null;
        boolean containsDeleteFrom = sqlStatement.toString().matches("(?i).*delete\\s+from.*");
        boolean containsInsertInto = sqlStatement.toString().matches("(?i).*insert\\s+into.*");
        boolean containsUpdate = sqlStatement.toString().matches("(?i).*update.*");
        boolean containsSelect = sqlStatement.toString().matches("(?i).*select.*");
        boolean containsCreateIndex = sqlStatement.toString().matches("(?i).*create\\s+index.*");
        boolean containsCreateTable = sqlStatement.toString().matches("(?i).*create\\s+table.*");

        if (containsDeleteFrom)
            return "DELETE";
        else if(containsInsertInto)
            return "INSERT";
        else if(containsUpdate)
            return "UPDATE";
        else if(containsSelect)
            return "SELECT";
        else if(containsCreateIndex)
            return "CREATEINDEX";
        else if(containsCreateTable)
            return "CREATETABLE";

        String sqlGrammar = "Here are some correct examples"+"\n";
        sqlGrammar += "   UPDATE    Student    SET    name =    'arwa'   ,    age = 30, gpa =    3 WHERE    id    = 5"+"\n";
        sqlGrammar += "    delete     FROM    Student     WHERE     name    = 'arwa'    AND     age = 30"+"\n";
        sqlGrammar += "    INSERT    INTO     Student  (  id  ,  name  ,age  ,gpa )   VALUES   (  10  ,'arwa' ,  21,  0.3)  "+"\n";
        sqlGrammar += "   SELECT    *    FROM     Student    where        name= 'arwa'    and       age >     30   OR     gpa < 1.6"+"\n";
        sqlGrammar += "CREATE TABLE Student(    id int    primarykey    , name varchar , gpa double ,CHECK (id >= 1 AND id <= 100),CHECK (name >= Z        AND       name <=    ZZZZZZ),CHECK (gpa >= 0.3 AND gpa <= 5.2)); "+"\n";
        sqlGrammar += "CREATE INDEX ON Student (name, gpa, age)"+"\n";

        throw new DBAppException("Unsupported SQL statement"+"\n"+sqlGrammar);
    }
        public static void main(String[] args) throws DBAppException {
        SQLParser parser = new SQLParser();
//        StringBuffer sqlStatement = new StringBuffer("    INSERT    INTO     Student  (  id  ,  name  ,age  ,gpa )   VALUES   (  10  ,'arwa' ,  21,  0.3)  ");
//        ArrayList<String> extractedColumns = extractColumnNames(sqlStatement.toString());
//        ArrayList<String> extractedValues = extractValues(sqlStatement.toString());

//        System.out.println(extractedColumns);
//        System.out.println(extractedValues);
//        System.out.println(prepareToInsert(sqlStatement)[0]);

//        StringBuffer sqlStatement = new StringBuffer("   UPDATE    Student    SET    name =    'arwa'   ,    age = 30, gpa =    3 WHERE    id    = 5");
//        System.out.println(extractColumnsToUpdate(sqlStatement));
//        System.out.println(extractValuesToUpdate(sqlStatement));
//        System.out.println(prepareToUpdate(sqlStatement)[0]);

//            StringBuffer sqlStatement = new StringBuffer("    delete     FROM    Student     WHERE     name    = 'arwa'    AND     age = 30");
//            System.out.println(extractColumnsToDelete(sqlStatement));
//            System.out.println(extractValuesToDelete(sqlStatement));
//            System.out.println(prepareToDelete(sqlStatement)[0]);

//            StringBuffer sqlStatement = new StringBuffer("   SELECT    *    FROM     Student    where        name= 'arwa'    and       age >     30   OR     gpa < 1.6");
//            System.out.println(extractOperators(sqlStatement)[0]);
//            System.out.println(convertToSQLTerms(sqlStatement)[0]);
//            System.out.println(getFirstWord(sqlStatement));


//            StringBuffer sqlStatement = new StringBuffer("CREATE    constraint Student(    id int    primarykey    , name varchar , gpa double ,CHECK (id >= 1 AND id <= 100),CHECK (name >= Z        AND       name <=    ZZZZZZ),CHECK (gpa >= 0.3 AND gpa <= 5.2)); ");
//            System.out.println(getPrimaryKeyName(sqlStatement));
//            System.out.println(extractColumnNamesToCreateTable(sqlStatement));
//            System.out.println(extractDataTypes(sqlStatement));
//            System.out.println(extractMinValues(sqlStatement));
//            System.out.println(extractMaxValues(sqlStatement));
//            Object[] result = prepareToCreateTable(sqlStatement);

//            StringBuffer sqlStatement = new StringBuffer("CREATE INDEX ON Student (x, y, z)");
            //CREATE INDEX octree_index ON Student (x, y, z) USING octree;
//            System.out.println(prepareToCreateIndex(sqlStatement)[0]);

//            System.out.println(parser.getOperation(sqlStatement));

    }
}
