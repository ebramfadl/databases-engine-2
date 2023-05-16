package main.engine;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class SQLParser {

    public static Object[] prepareToInsert(StringBuffer sqlStatement){
        String tableName = null;

        // Extract table name
        Pattern pattern = Pattern.compile("INSERT INTO (\\w+)");
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
        Pattern pattern = Pattern.compile("\\((.*?)\\)");
        Matcher matcher = pattern.matcher(sqlStatement);

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
        Pattern pattern = Pattern.compile("VALUES\\((.*?)\\)");
        Matcher matcher = pattern.matcher(sqlStatement);

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
        String pattern = "\\bSET\\b\\s+([\\w,\\s=']+)";
        Pattern regex = Pattern.compile(pattern);
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

        Pattern regex = Pattern.compile(pattern);
        Matcher matcher = regex.matcher(sqlStatement);

        while (matcher.find()) {
            String value = matcher.group(1);
            values.add(value);
        }
        return  values;
    }

    public static Object[] prepareToUpdate(StringBuffer sqlStatement) {
        String tableName = null;

        // Extract table name
        Pattern pattern = Pattern.compile("UPDATE (\\w+)");
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
        Pattern regex = Pattern.compile(pattern);
        Matcher matcher = regex.matcher(sqlStatement);

        if (matcher.find()) {
            String columnClause = matcher.group(1);
            String[] columnArray = columnClause.split("\\s+AND\\s+");

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

    public static Object[] prepareToDelete(StringBuffer sqlStatement) {
        String tableName = null;

        // Extract table name
        Pattern pattern = Pattern.compile("DELETE FROM (\\w+)");
        Matcher matcher = pattern.matcher(sqlStatement.toString());

        if (matcher.find()) {
            tableName = matcher.group(1);
        }

        ArrayList<String> names = extractColumnsToDelete(sqlStatement);
        ArrayList<String> values = extractValuesToDelete(sqlStatement);
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

        return new Object[]{tableName,htblColNameVal};

    }

    public static String[] extractOperators(StringBuffer sqlStatement) {
        ArrayList<String> operators = new ArrayList<>();

        // Regular expression pattern to match operators
        String pattern = "\\b(AND|OR|XOR)\\b";
        Pattern regex = Pattern.compile(pattern);
        Matcher matcher = regex.matcher(sqlStatement);

        while (matcher.find()) {
            String operator = matcher.group();
            operators.add(operator);
        }

        return operators.toArray(new String[0]);
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



    public static String getFirstWord(StringBuffer sqlStatement) {
        String statement = sqlStatement.toString().trim();
        String[] words = statement.split("\\s+");
        if (words.length > 0) {
            return words[0];
        }
        return null;
    }

        public static void main(String[] args) {
//        StringBuffer sqlStatement = new StringBuffer("INSERT INTO Student(id,name,age,gpa) VALUES(10,'arwa',21,0.3)");
//        ArrayList<String> extractedColumns = extractColumnNames(sqlStatement.toString());
//        ArrayList<String> extractedValues = extractValues(sqlStatement.toString());
//
//        System.out.println(extractedColumns);
//        System.out.println(extractedValues);
//        System.out.println(prepareToInsert(sqlStatement));

//        StringBuffer sqlStatement = new StringBuffer("UPDATE Student SET name = 'arwa', age = 30, gpa = 3 WHERE id = 5");
//        System.out.println(extractColumnsToUpdate(sqlStatement));
//        System.out.println(extractValuesToUpdate(sqlStatement));
//        System.out.println(prepareToUpdate(sqlStatement)[2]);

//            StringBuffer sqlStatement = new StringBuffer("DELETE FROM Student WHERE name = 'arwa' AND age = 30");
//            System.out.println(extractColumnsToDelete(sqlStatement));
//            System.out.println(extractValuesToDelete(sqlStatement));
//            System.out.println(prepareToDelete(sqlStatement)[1]);

            StringBuffer sqlStatement = new StringBuffer("SELECT * FROM Student WHERE name = 'arwa' AND age > 30 OR gpa < 1.6");
            System.out.println(extractOperators(sqlStatement));
            System.out.println(convertToSQLTerms(sqlStatement)[2]);
            System.out.println(getFirstWord(sqlStatement));
        }
}
