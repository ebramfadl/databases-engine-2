package main.engine;

import java.io.BufferedReader;
import java.io.*;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class DBApp {


    public  void init(){
        DBConfig.update(200,16);
    }

    public static int compareTo(String s1, String s2) {

        if (s1.matches("\\d+") && s2.matches("\\d+")) {
            if(Integer.parseInt(s1) > Integer.parseInt(s2))
                return 1;
            else if (Integer.parseInt(s1) == Integer.parseInt(s2))
                return 0;
            else
                return -1;
        }
        else if (s1.matches("[a-zA-Z]+") && s2.matches("[a-zA-Z]+")) {
            return s1.compareTo(s2);
        }
        else if(s1.indexOf(".") != -1 || s2.indexOf(".") != -1) {
            if(Double.parseDouble(s1) > Double.parseDouble(s2))
                return 1;
            else if (Double.parseDouble(s1) == Double.parseDouble(s2))
                return 0;
            else
                return -1;
        }

        System.out.println("Comparison failed between "+s1+" and "+s2);
        return -5;
    }

    public static String getPrimaryKey(String tableName) throws DBAppException {

        try {
            String line = "";
            String path = "src/main/resources/metadata.csv";

            BufferedReader br = new BufferedReader(new FileReader(path));
            while ( (line = br.readLine()) != null ){
                String[] fields = line.split(",");
                if(fields[0].equals(tableName) && fields[3].equals("true"))
                    return fields[1];
            }
            br.close();
        } catch (Exception e) {
            throw new DBAppException(e);
        }
        return null;
    }

    public static void sortPage(Page page, String toSortBy) {

        Comparator<Tuple> compareById = new Comparator<Tuple>() {
            @Override
            public int compare(Tuple t1, Tuple t2) {
                String pk1 = t1.getHtblColNameValue().get(toSortBy).toString();
                String pk2 = t2.getHtblColNameValue().get(toSortBy).toString();
                return compareTo(pk1, pk2);
            }
        };
        // Sort the vector by primary key
        Collections.sort(page.getPageTuples(), compareById);

    }

    public static String displayTablePages(String tableName) throws DBAppException {

        String str = null;
        try {
            str = "";
            Table table = deserializeTable(tableName);

            for(int i = 1 ; i<= table.getNumberOfPages() ; i++){
                Page page = deserializePage(tableName,i);
                str = str + page.toString()+"\n";
            }

        } catch (Exception e) {
            throw new DBAppException(e);
        }
        return  str;
    }

    public static ArrayList<String[]> getAllMetadata(String tableName){
        String line = "";
        String path = "src/main/resources/metadata.csv";
        ArrayList<String[]> allMetadata = new ArrayList<String[]>();

        try (BufferedReader br = new BufferedReader(new FileReader(path)))
        {
            while((line = br.readLine()) !=null ){
                String[] fields = line.split(",");
                if(fields[0].equals(tableName))
                    allMetadata.add(fields);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return allMetadata;
    }

    public static boolean validateDataTypes(String strTableName, Hashtable<String,Object> htblColNameValue) throws DBAppException {


        ArrayList<String[]> allMetadata = getAllMetadata(strTableName);

        for (Map.Entry<String,Object> entry : htblColNameValue.entrySet()) {

            String colName = entry.getKey();
            Object colValue = entry.getValue();
            boolean flag = false;
            for (String[] strings : allMetadata){
                if(strings[1].equals(colName)){
                    flag = true;
                    if(!((colValue.getClass().getName().toString().equals(strings[2]))))
                        throw new DBAppException("Invalid column datatype! "+colValue.getClass().getName().toString());
                    if(compareTo(colValue.toString(),strings[6]) == -1){
                        throw new DBAppException(colValue + " is less than the minimum of column "+colName);
                    }
                    if(compareTo(colValue.toString() , strings[7]) == 1){
                        throw new DBAppException(colValue + " is greater than the maximum of column "+colName);
                    }
                }
            }
            if (flag == false){
                throw  new DBAppException("Column "+colName+" does not exist");
            }

        }

        return true;

    }


    public static boolean serializePage(Page page){
        //write ===> FileOutputStream
        //Read ===> FileInputStream
        try {
            FileOutputStream fileOut = new FileOutputStream("src/main/resources/data/"+page.getTableName()+"/"+page.getPageNumber()+".ser");
            ObjectOutputStream objectOut = new ObjectOutputStream(fileOut);
            objectOut.writeObject(page);
            //System.out.println("page updated " + path + " successfully!");
            objectOut.close();
            fileOut.close();
            return true;
        }
        catch (IOException e){
            e.printStackTrace();
        }
        return false;
    }

    public static Page deserializePage(String tableName, int pageNum) throws IOException, ClassNotFoundException {

            FileInputStream fileInputStream = new FileInputStream("src/main/resources/data/"+tableName+"/"+pageNum+".ser");
            ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
            Page page = (Page) objectInputStream.readObject();

            objectInputStream.close();
            fileInputStream.close();
            return page;

    }

    public static boolean serializeTable(Table table) throws DBAppException {
        try(
                FileOutputStream fileOut = new FileOutputStream("src/main/resources/data/"+table.getTableName()+"/"+table.getTableName()+".ser");
                ObjectOutputStream objectOut = new ObjectOutputStream(fileOut)) {
            objectOut.writeObject(table);
            objectOut.close();
            fileOut.close();
            return true;
        }
        catch (Exception e){
            throw new DBAppException(e.getMessage());
        }
    }

    public static Table deserializeTable(String tableName) throws DBAppException {

        String path = "src/main/resources/data/"+tableName+"/"+tableName+".ser";
        try {
            FileInputStream fileInputStram = new FileInputStream(path);
            ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStram);
            while (true){
                try {
                    Table table = (Table)objectInputStream.readObject();
                    if(table.getTableName().equals(tableName))
                        return table;
                }
                catch (EOFException e){
                    break;
                }
            }
            fileInputStram.close();
            objectInputStream.close();
        }
        catch (IOException e){
            throw new DBAppException("Table does not exist");
        }
        catch (Exception e){
            throw new DBAppException(e.getMessage());
        }
        return null;
    }


    public static boolean checkTableExists(String tableName) throws DBAppException{
        try {
            String line = "";
            String path = "src/main/resources/metadata.csv";
            BufferedReader br = new BufferedReader(new FileReader(path));
            while((line = br.readLine())!=null) {
                String[] fields = line.split(",");
                if (fields[0].equals(tableName))
                    return true;
            }
            br.close();
            return false;
        } catch (Exception e) {
            throw new DBAppException(e);
        }
    }




    // following method creates one table only
    // strClusteringKeyColumn is the name of the column that will be the primary
    // key and the clustering column as well. The data type of that column will
    // be passed in htblColNameType
    // htblColNameValue will have the column name as key and the data
    // type as value
    // htblColNameMin and htblColNameMax for passing minimum and maximum values
    // for data in the column. Key is the name of the column
    public void createTable(String strTableName, String strClusteringKeyColumn, Hashtable<String,String> htblColNameType, Hashtable<String,String> htblColNameMin, Hashtable<String,String> htblColNameMax ) throws DBAppException {


        try {
            if(checkTableExists(strTableName))
                throw  new DBAppException("Table "+strTableName+" already exists!");


            String path = "src/main/resources/metadata.csv";

            for (Map.Entry<String,String> entry : htblColNameType.entrySet()){
                if( !( entry.getValue().equals("java.lang.String") || entry.getValue().equals("java.lang.Integer") ||  entry.getValue().equals("java.lang.Date") || entry.getValue().equals("java.lang.Double")) )
                    throw new DBAppException("Invalid column datatype "+entry.getValue()+" you can only use 'java.lang.Integer/Double/Date/String' ");
            }

            for (Map.Entry<String,String> entry : htblColNameType.entrySet()){
                String isClustering = "false";
                if(entry.getKey().equals(strClusteringKeyColumn))
                    isClustering="true";

                String[] columns= {strTableName,entry.getKey(),entry.getValue(),isClustering,null,null,htblColNameMin.get(entry.getKey()),htblColNameMax.get(entry.getKey())};

                BufferedWriter br = new BufferedWriter( new FileWriter(path,true));
                br.write(String.join(",",columns));
                br.newLine();
                br.close();
                System.out.println("Data inserted successfully into metadata.csv");
            }
            File tableDirectory = new File("src/main/resources/data/"+strTableName);
            tableDirectory.mkdir();
            Table table = new Table(strTableName, DBConfig.getPageMaximum());
            serializeTable(table);
        }
        catch (IOException e) {
            throw new DBAppException(e.getMessage());
        }

    }


    public static  boolean checkRecordExists(String tableName, String pkValue,int pageNum) throws ClassNotFoundException, IOException{
        try {
            getTupleByBinarySearch(tableName,pkValue,pageNum);
        }
        catch (DBAppException e){
            return false;
        }
        return true;
    }

    public static void fillMissingFields(String tableName, Hashtable<String,Object> htblColNameValue){
        ArrayList<String[]> allMetadata = getAllMetadata(tableName);

        for(String[] arr : allMetadata){
            if(htblColNameValue.get(arr[1]) == null){
                htblColNameValue.put(arr[1],"null");
            }
        }
    }

    public static  String[] getIndexToInsert(String tablename, Hashtable<String,Object> htblColNameValue) throws DBAppException {
        Table table = deserializeTable(tablename);
        for(String[] array : table.getAllIndices()){
            if(htblColNameValue.containsKey(array[0]) && htblColNameValue.containsKey(array[1]) && htblColNameValue.containsKey(array[2])){
                 return new String[] {array[0],array[1],array[2]};
            }
        }
        return null;
    }

    // following method inserts one row only.
    // htblColNameValue must include a value for the primary key
    public static void insertIntoTable(String strTableName, Hashtable<String,Object> htblColNameValue) throws DBAppException {
        //check id does not exist
        try {
            if(checkTableExists(strTableName) == false)
                throw new DBAppException("Table "+strTableName+" does not exist, Please create it first");

            validateDataTypes(strTableName,htblColNameValue);

            String pk = getPrimaryKey(strTableName); //"id"

            if(htblColNameValue.get(pk) == null){
                throw new DBAppException("Missing the primary key!");
            }

            fillMissingFields(strTableName,htblColNameValue);

            Table table = deserializeTable(strTableName);

            //what if pk is not determined

            String pkValue =  htblColNameValue.get(pk).toString();

            Integer pageNum = null;
            try {
                pageNum = getPageByBinarySearch(strTableName,pkValue);
            } catch (Exception e) {

            }
            if(pageNum != null){
                if(checkRecordExists(strTableName,pkValue,pageNum)){
                    throw new DBAppException("Duplicate keys for "+pk+ " value "+pkValue);
                }
            }


            if(table.getNumberOfPages() == 0){
                Page page = new Page(1,strTableName);
                Tuple addedTuple = page.addTuple(htblColNameValue);
                String path = "src/main/resources/data/"+strTableName+"/"+page.getPageNumber()+".ser";
                table.setNumberOfPages(1);
                serializeTable(table);
                serializePage(page);

                String [] s = getIndexToInsert(strTableName,htblColNameValue);
                if(s != null){
                    Octree octree = deserializeIndex(s[0]+s[1]+s[2],strTableName);
                    Object x = htblColNameValue.get(s[0]);
                    Object y = htblColNameValue.get(s[1]);
                    Object z = htblColNameValue.get(s[2]);
                    Object[] array = {x,y,z, page.getPageNumber()};
                    octree.insert(array);
                    serializeIndex(octree,strTableName);
                }

                return;

            }


            for(int i = 1 ; i<= table.getNumberOfPages() ; i++){
                Page currentPage = deserializePage(strTableName,i);
                Vector<Tuple> pageVector = currentPage.getPageTuples();
                String pageMax = pageVector.lastElement().getHtblColNameValue().get(pk).toString();
                String pageMin = pageVector.firstElement().getHtblColNameValue().get(pk).toString();
                String key = htblColNameValue.get(pk).toString();



                if(table.getNumberOfPages() == i && compareTo(key, pageMax) > 0 && pageVector.size() == pageVector.capacity()) {

                    int pageNumber = table.getNumberOfPages()+1;
                    Page newPage = new Page(pageNumber, strTableName);
                    Tuple addedTuple = newPage.addTuple(htblColNameValue);
                    table.setNumberOfPages(table.getNumberOfPages()+1);
                    serializeTable(table);
                    serializePage(newPage);

                    String [] s = getIndexToInsert(strTableName,htblColNameValue);
                    if(s!=null){
                        Octree octree = deserializeIndex(s[0]+s[1]+s[2],strTableName);
                        Object x = htblColNameValue.get(s[0]);
                        Object y = htblColNameValue.get(s[1]);
                        Object z = htblColNameValue.get(s[2]);
                        Object[] array = {x,y,z, newPage.getPageNumber()};
                        octree.insert(array);
                        serializeIndex(octree,strTableName);
                    }

                    return;

                }
                else if(pageVector.size() < pageVector.capacity()) {

                    if(i == table.getNumberOfPages()) {
                        Tuple addedTuple = currentPage.addTuple(htblColNameValue);
                        sortPage(currentPage, pk);
                        serializePage(currentPage);

                        String [] s = getIndexToInsert(strTableName,htblColNameValue);
                        if(s!=null){
                            Octree octree = deserializeIndex(s[0]+s[1]+s[2],strTableName);
                            Object x = htblColNameValue.get(s[0]);
                            Object y = htblColNameValue.get(s[1]);
                            Object z = htblColNameValue.get(s[2]);
                            Object[] array = {x,y,z, currentPage.getPageNumber()};
                            octree.insert(array);
                            serializeIndex(octree,strTableName);
                        }

                        return;
                    }
                    else {
                        int nextPageNum = i+1;
                        Page nextPage = deserializePage(table.getTableName(),nextPageNum);
                        if(compareTo(key,nextPage.getPageTuples().firstElement().getHtblColNameValue().get(pk).toString()) < 0) {
                            Tuple addedTuple = currentPage.addTuple(htblColNameValue);
                            sortPage(currentPage, pk);
                            serializePage(currentPage);

                            String [] s = getIndexToInsert(strTableName,htblColNameValue);
                            if(s!=null){
                                Octree octree = deserializeIndex(s[0]+s[1]+s[2],strTableName);
                                Object x = htblColNameValue.get(s[0]);
                                Object y = htblColNameValue.get(s[1]);
                                Object z = htblColNameValue.get(s[2]);
                                Object[] array = {x,y,z, currentPage.getPageNumber()};
                                octree.insert(array);
                                serializeIndex(octree,strTableName);
                            }

                            return;
                        }
                    }

                }
                else if(pageVector.size() == pageVector.capacity() && compareTo(key, pageMax) < 0 ){

                    Tuple popedTuple = (Tuple)pageVector.remove(pageVector.size()-1);
                    Tuple addedTuple = currentPage.addTuple(htblColNameValue);
                    sortPage(currentPage, pk);
                    serializePage(currentPage);

                    String [] s = getIndexToInsert(strTableName,htblColNameValue);
                    if(s!=null){
                        Octree octree = deserializeIndex(s[0]+s[1]+s[2],strTableName);
                        Object x = htblColNameValue.get(s[0]);
                        Object y = htblColNameValue.get(s[1]);
                        Object z = htblColNameValue.get(s[2]);
                        Object[] array = {x,y,z, currentPage.getPageNumber()};
                        Object[] toDelete = {popedTuple.getHtblColNameValue().get(s[0]),popedTuple.getHtblColNameValue().get(s[1]),popedTuple.getHtblColNameValue().get(s[2]),currentPage.getPageNumber()};
                        octree.delete(toDelete);
                        serializeIndex(octree,strTableName);
                        octree = deserializeIndex(s[0]+s[1]+s[2],strTableName);
                        octree.insert(array);
                        serializeIndex(octree,strTableName);
                    }

                    insertIntoTable(strTableName, popedTuple.getHtblColNameValue());
                    return;
                }


            }
        } catch (Exception e) {
            throw new DBAppException(e);
        }

    }
    public void updateTable(String strTableName, String strClusteringKeyValue, Hashtable<String,Object> htblColNameValue) throws DBAppException {
        try {
            if(!checkTableExists(strTableName)){
                throw new DBAppException("Table " +strTableName+ " does not exist.");
            }
            int pageNumber = 0;
            Table table = deserializeTable(strTableName);
            String pk = getPrimaryKey(strTableName);
            boolean indexOnPkFound = false;
            for (String[] index : table.getAllIndices()){
                for (int i = 0 ; i<index.length ; i++){
                    if(index[i].equals(pk)){
                        Octree octree = deserializeIndex(index[0]+index[1]+index[2],strTableName);
                        Object[] toFindByPk = {"null","null","null"};
                        toFindByPk[i] = Integer.valueOf(strClusteringKeyValue);
                        pageNumber = octree.findByPrimaryKey(toFindByPk);
                        indexOnPkFound = true;
                        System.out.println("Update using index");
                    }
                }
            }

            if(! indexOnPkFound){
                pageNumber = getPageByBinarySearch(strTableName, strClusteringKeyValue);//////////////
            }
            Page page = deserializePage(strTableName,pageNumber);
            int tupleIndex = getTupleByBinarySearch(strTableName, strClusteringKeyValue,pageNumber);
            Tuple tuple = page.getPageTuples().get(tupleIndex);
            for(int i = 0; i<table.getAllIndices().size(); i++){
                String[] arr = table.getAllIndices().get(i);
                if(htblColNameValue.containsKey(arr[0]) || htblColNameValue.containsKey(arr[1]) || htblColNameValue.containsKey(arr[2]) ){
                    Object[] oldArr = {tuple.getHtblColNameValue().get((arr[0])),tuple.getHtblColNameValue().get((arr[1])),tuple.getHtblColNameValue().get((arr[2]))};
                    Object[] newArr = new Object[4];
                    for(int j=0 ; j < 3; j++){
                        if(htblColNameValue.containsKey(arr[j])){
                            newArr[j] = htblColNameValue.get(arr[j]);
                        }
                        else{
                            newArr[j]= tuple.getHtblColNameValue().get((arr[j]));
                        }
                    }
                    newArr[3] = pageNumber;
                    Octree octree = deserializeIndex(arr[0]+arr[1]+arr[2],strTableName);
                    octree.delete(oldArr);
                    serializeIndex(octree,strTableName);
                    octree = deserializeIndex(arr[0]+arr[1]+arr[2],strTableName);
                    octree.insert(newArr);
                    serializeIndex(octree,strTableName);

                }
            }

            for (Map.Entry<String, Object> entry : htblColNameValue.entrySet()) {
                if(!tuple.getHtblColNameValue().containsKey(entry.getKey())){
                    throw new DBAppException("Field " +entry.getKey()+  " does not exist.");
                }

                else if(!entry.getValue().equals("null")){
                    if(!tuple.getHtblColNameValue().get(entry.getKey()).getClass().getName().equals(entry.getValue().getClass().getName()) ){
                        throw new DBAppException("Cannot update column " +entry.getKey()+ " to be " + entry.getValue());
                    }
                }


                tuple.getHtblColNameValue().put(entry.getKey(),entry.getValue());

            }
            serializePage(page);
        } catch (Exception e) {
            throw new DBAppException(e.getMessage());
        }
    }
    public static int getTupleByBinarySearch(String strTableName, String value,int pageNum) throws ClassNotFoundException, IOException, DBAppException {
//        int x = getPageByBinarySearch(strTableName,value);
        Page page = deserializePage(strTableName,pageNum);
        String pk = getPrimaryKey(strTableName);    //"ID"
        int low = 0;
        int high = page.getPageTuples().size() - 1;
        while(low <= high){
            int mid = (low + high)/2;
            String key = page.getPageTuples().get(mid).getHtblColNameValue().get(pk).toString();
            if(compareTo(key,value) == 0){
                return mid;
            }
            else if(compareTo(key,value) < 0 ){
                low = mid + 1;
            }
            else{
                high = mid - 1;
            }

        }
        throw new DBAppException("Record with primary key " + value + " is not found");
    }
    public static int getPageByBinarySearch(String strTableName, String value) throws IOException, ClassNotFoundException, DBAppException {
        Table table = deserializeTable(strTableName);
        int low = 1;
        int high = table.getNumberOfPages();
        String pk = getPrimaryKey(strTableName);
        while(low <= high){
            int mid = (low + high)/2;
            Page page = deserializePage(strTableName,mid);
            String min = page.getPageTuples().firstElement().getHtblColNameValue().get(pk).toString();
            String max = page.getPageTuples().lastElement().getHtblColNameValue().get(pk).toString();

            if((compareTo(value, min) >= 0) && (compareTo(value,max) <= 0)){
                return mid;
            }
            else if(compareTo(value,max) > 0){
                low = mid + 1;
            }
            else{
                high = mid - 1;
            }

        }
        throw new DBAppException("Record with primary key " + value + " is not found");
    }

    public static void clearTableContent(Table table) throws DBAppException {
        String path = "src/main/resources/data/"+table.getTableName()+"/";
        for (int i = 1 ; i<= table.getNumberOfPages() ; i++){
            File file = new File(path+i+".ser");
            file.delete();
        }
        table.setNumberOfPages(0);
        serializeTable(table);
    }
    public void deleteFromTable(String strTableName, Hashtable<String,Object> htblColNameValue) throws DBAppException {

        try {
            if(!checkTableExists(strTableName)){
                throw new DBAppException("Table " +strTableName+ " does not exist.");
            }

            Table table = deserializeTable(strTableName);
            if(htblColNameValue.isEmpty()){
                clearTableContent(table);
                return;
            }
            String indexName = " ";
            Boolean indexFound=false;
            String[] array=null;
            for(String[] arr : table.getAllIndices()){
                if(htblColNameValue.containsKey(arr[0]) && htblColNameValue.containsKey(arr[1]) && htblColNameValue.containsKey(arr[2])){
                    indexName=arr[0]+arr[1]+arr[2];
                    indexFound=true;
                    array=arr;
                    break;
                }
            }

            if(indexFound==true){
                Octree octree = deserializeIndex(indexName,strTableName);
                Object[] arr = {htblColNameValue.get(array[0]),htblColNameValue.get(array[1]),htblColNameValue.get(array[2])};
                ArrayList<Object[]> toBeDeleted = octree.search(arr);
                for(Object[] a : toBeDeleted){

                        Page page = deserializePage(strTableName, (int)a[3]);
                        Iterator<Tuple> tupleIterator = page.getPageTuples().iterator();
                        while(tupleIterator.hasNext()){
                            Tuple tuple = tupleIterator.next();
                            Object xTuple = tuple.getHtblColNameValue().get(array[0]);
                            Object yTuple = tuple.getHtblColNameValue().get(array[1]);
                            Object zTuple = tuple.getHtblColNameValue().get(array[2]);
                            Object toDeleteX = htblColNameValue.get(array[0]);
                            Object toDeleteY = htblColNameValue.get(array[1]);
                            Object toDeleteZ = htblColNameValue.get(array[2]);

                            if (xTuple.equals(toDeleteX) && yTuple.equals(toDeleteY) && zTuple.equals(toDeleteZ))
                                tupleIterator.remove();
                        }
                        serializePage(page);
                        if (page.getPageTuples().isEmpty()) {
                            File file = new File("src/main/resources/data/" + strTableName + "/" + page.getPageNumber() + ".ser");
                            file.delete();
                            updatePagesNumber(strTableName, page.getPageNumber() + 1);
                            table.setNumberOfPages(table.getNumberOfPages() - 1);
                            serializeTable(table);
                        }


                }
                octree.delete(arr);
                serializeIndex(octree,strTableName);
                return;
            }


            for (int i = 1; i <= table.getNumberOfPages(); i++) {
                String[] indexNames = null;
                Page page = deserializePage(strTableName,i);
                Vector<Tuple> pageVector = page.getPageTuples();
                Iterator<Tuple> tupleItearator = pageVector.iterator();
                while (tupleItearator.hasNext()) {
                    Tuple tuple = tupleItearator.next();
                    Boolean flag=true;
                    for (Map.Entry<String, Object> entry : htblColNameValue.entrySet()) {
                        String entryVal = entry.getValue().toString();
                        if(!tuple.getHtblColNameValue().containsKey(entry.getKey())){
                            throw new DBAppException("Field " +entry.getKey()+  " does not exist.");
                        }

                        else if(!entry.getValue().equals("null")){
                            if(!tuple.getHtblColNameValue().get(entry.getKey()).getClass().getName().equals(entry.getValue().getClass().getName()) && !tuple.getHtblColNameValue().get(entry.getKey()).equals("null")){
                                throw new DBAppException("Invalid data type.");
                            }
                        }

                        if(!tuple.getHtblColNameValue().get(entry.getKey()).equals(entry.getValue())){
                            flag = false;
                        }

                    }
                    if(flag==true){
                        tupleItearator.remove();
                        for (String[] index : table.getAllIndices()){
                            indexNames = index;
                            String indexToDeleteFrom = index[0]+index[1]+index[2]+"";
                            Object[] toDelete = {tuple.getHtblColNameValue().get(index[0]),tuple.getHtblColNameValue().get(index[1]),tuple.getHtblColNameValue().get(index[2])};
                            Octree octree = deserializeIndex(index[0]+index[1]+index[2],strTableName);
                            octree.delete(toDelete);
                            serializeIndex(octree,strTableName);
                        }
                    }
                }
                serializePage(page);
                if(page.getPageTuples().isEmpty()){
                    File file = new File("src/main/resources/data/"+strTableName+"/"+i+".ser");
                    file.delete();
                    updatePagesNumber(strTableName,i+1);
                    table.setNumberOfPages(table.getNumberOfPages()-1);
                    serializeTable(table);
                    updateIndexPages(strTableName,i,indexNames);

                }
            }
            serializeTable(table);
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    public static void updateIndexPages(String tableName,int start ,String[] indexFields) throws DBAppException, IOException, ClassNotFoundException {
        Table table = deserializeTable(tableName);
        Octree octree = deserializeIndex(indexFields[0]+indexFields[1]+indexFields[2],tableName);

        for (int i = start ; i <= table.getNumberOfPages() ; i++ ){
            Page page = deserializePage(tableName,i);
            for(Tuple tuple : page.getPageTuples()){
                Object[] toDelete = {tuple.getHtblColNameValue().get(indexFields[0]),tuple.getHtblColNameValue().get(indexFields[1]),tuple.getHtblColNameValue().get(indexFields[2])};
                Object[] toInsert = {tuple.getHtblColNameValue().get(indexFields[0]),tuple.getHtblColNameValue().get(indexFields[1]),tuple.getHtblColNameValue().get(indexFields[2]),i};
                octree.delete(toDelete);
                serializeIndex(octree,tableName);
                octree = deserializeIndex(indexFields[0]+indexFields[1]+indexFields[2],tableName);
                octree.insert(toInsert);
                serializeIndex(octree,tableName);
            }
        }
    }

    public void updatePagesNumber(String tablename, int currentpage) throws ClassNotFoundException, IOException {
        Page page;
        try{
            page = deserializePage(tablename,currentpage);
        }
        catch(IOException e){
            return;
        }
        int newPageNumber = currentpage - 1;
        File oldFile = new File("src/main/resources/data/"+tablename+"/"+currentpage+".ser");
        File newFile = new File("src/main/resources/data/"+tablename+"/"+newPageNumber+".ser");
        oldFile.renameTo(newFile);
        page.setPageNumber(newPageNumber);
        serializePage(page);
        updatePagesNumber(tablename,currentpage+1);
    }

    public void createIndex(String strTableName, String[] strarrColName) throws DBAppException{
        if(strarrColName.length != 3){
            throw new DBAppException("An octree can only be created on exactly 3 dimensions.");
        }
        if(!checkTableExists(strTableName)){
            throw new DBAppException("The table " + strTableName + " does not exit.");
        }

        try {
            String[] min = new String[3];
            String[] max = new String[3];
            String[] dataTypes = new String[3];
            Double[] minValues = new Double[3];
            Double[] maxValues = new Double[3];
            ArrayList<String[]> allMetaData = getAllMetadata(strTableName);

            for (int i=0 ; i<=2 ; i++){
                Boolean flag=false;
                for(String[] arr : allMetaData){
                    if(strarrColName[i].equals(arr[1])){
                            flag=true;
                            min[i]=arr[6];
                            max[i]=arr[7];
                            dataTypes[i]=arr[2];
                    }
                }
                if(flag==false){
                    throw new DBAppException("The column " +strarrColName[i] + " does not exist.");
                }
            }

            for(int i=0 ; i<=2; i++){
                if(dataTypes[i].equals("java.lang.String")){
                    minValues[i] = (double) min[i].hashCode();
                    maxValues[i] = (double) max[i].hashCode();
                }
                else if(dataTypes[i].equals("java.lang.Integer") || dataTypes[i].equals("java.lang.Double")){
                    minValues[i] = Double.parseDouble(min[i]);
                    maxValues[i] = Double.parseDouble(max[i]);
                }
                else {
                    DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                    Date minDate = dateFormat.parse(min[i]);
                    Date maxDate = dateFormat.parse(max[i]);
                    minValues[i] = (double) minDate.hashCode();
                    maxValues[i] = (double) maxDate.hashCode();
                }
            }

            Table table = deserializeTable("Student");
            for(int i=0; i<table.getAllIndices().size();i++){
                String[] index = table.getAllIndices().get(i);
                if(index[0].equals(strarrColName[0]) && index[1].equals(strarrColName[1]) && index[2].equals(strarrColName[2])){
                    throw new DBAppException("Index " + index[0]+index[1]+index[2] + " already exists.");
                }
            }
            Octree octree = new Octree(minValues[0],maxValues[0],minValues[1],maxValues[1],minValues[2],maxValues[2], strarrColName[0], strarrColName[1],strarrColName[2]);
            table.getAllIndices().add(strarrColName);
            serializeTable(table);

            serializeIndex(octree,strTableName);

            fillIndex(table.getTableName(),strarrColName[0],strarrColName[1],strarrColName[2]);
        } catch (Exception e) {
            throw new DBAppException(e);
        }

    }


    public static void serializeIndex(Octree tree,String tablename) {
        try(
                FileOutputStream fileOut = new FileOutputStream("src/main/resources/data/" + tablename + "/" +tree.getFullName()+".ser");
                ObjectOutputStream objectOut = new ObjectOutputStream(fileOut)) {
            objectOut.writeObject(tree);
            System.out.println("Index "+tree.getFullName()+" saved to file: "+tree.getFullName()+".ser");
            objectOut.close();
            fileOut.close();

        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Octree deserializeIndex(String name, String tablename) {
        try {
            FileInputStream fileInputStream = new FileInputStream("src/main/resources/data/" + tablename + "/" +name+".ser");
            ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
            Octree tree = (Octree) objectInputStream.readObject();
            objectInputStream.close();
            fileInputStream.close();
            return tree;
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    public static boolean SQLTermHelper(SQLTerm sqlTerm, Tuple tuple) throws DBAppException {
        if(!tuple.getHtblColNameValue().containsKey(sqlTerm.getColName())){
            throw new DBAppException("Column " + sqlTerm.getColName() + " does not exist.");
        }
        switch(sqlTerm.getOperator()){
            case "=": return sqlTerm.getValue().equals(tuple.getHtblColNameValue().get(sqlTerm.getColName()));
            case "!=": return !(sqlTerm.getValue().equals(tuple.getHtblColNameValue().get(sqlTerm.getColName())));
            case "<=": return Double.valueOf(tuple.getHtblColNameValue().get(sqlTerm.getColName()).toString()) <= Double.valueOf(sqlTerm.getValue().toString());
            case "<": return Double.valueOf(tuple.getHtblColNameValue().get(sqlTerm.getColName()).toString()) < Double.valueOf(sqlTerm.getValue().toString());
            case ">=": return Double.valueOf(tuple.getHtblColNameValue().get(sqlTerm.getColName()).toString()) >= Double.valueOf(sqlTerm.getValue().toString());
            case ">": return Double.valueOf(tuple.getHtblColNameValue().get(sqlTerm.getColName()).toString()) > Double.valueOf(sqlTerm.getValue().toString());
        }
        return false;
    }

    public static boolean evaluateSQLTerms(SQLTerm[] arrSQLTerms, String[] strarrOperators, Tuple tuple) throws DBAppException {
        Boolean flag = SQLTermHelper(arrSQLTerms[0],tuple);
        for(int i = 1 ; i<arrSQLTerms.length ; i++){
            switch(strarrOperators[i-1]){
                case "AND" : flag = flag && SQLTermHelper(arrSQLTerms[i],tuple); break;
                case "OR" : flag = flag || SQLTermHelper(arrSQLTerms[i],tuple); break;
                case "XOR" : flag = flag ^ SQLTermHelper(arrSQLTerms[i],tuple); break;
            }
        }
        return flag;
    }

    public Iterator selectFromTable(SQLTerm[] arrSQLTerms, String[] strarrOperators) throws DBAppException {
        try {
            if(!checkTableExists(arrSQLTerms[0].getTableName())){
                throw new DBAppException("Table " + arrSQLTerms[0].getTableName() + " does not exist.");
            }

            String tableName = arrSQLTerms[0].getTableName();
            if( (arrSQLTerms.length == 1) && (arrSQLTerms[0].getColName().equals(getPrimaryKey(tableName)))){
                int pageNumber = getPageByBinarySearch(tableName,arrSQLTerms[0].getValue().toString());
                int tupleIndex = getTupleByBinarySearch(tableName, arrSQLTerms[0].getValue().toString(),pageNumber);
                Page page = deserializePage(tableName,pageNumber);
                Tuple tuple = page.getPageTuples().get(tupleIndex);

                Vector<Tuple> ayhaga = new Vector<Tuple>();
                ayhaga.add(tuple);
                return ayhaga.iterator();
            }

            Table table = deserializeTable(arrSQLTerms[0].getTableName());
            Vector<Tuple> resultTuples = new Vector<>();

            ArrayList<String[]> allIndecies = table.getAllIndices();
            SQLTerm[] terms = new SQLTerm[3];
            String[] operators = new String[2];
            int counter = 0;
            boolean indexExists = false;
            String indexName = "";
            if(arrSQLTerms.length >= 3) {
                for (int i = 0; i < allIndecies.size(); i++) {
                    String[] index = allIndecies.get(i); // [name,age,gpa]
                    boolean[] indexFound = {false, false, false};
                    for (int j = 0; j < index.length; j++) {
                        for (int k = 0; k < arrSQLTerms.length; k++) {
                            if (index[j].equals(arrSQLTerms[k].getColName())) {
                                indexFound[j] = true;
                                terms[j] = arrSQLTerms[k];
                                if (counter < 2) {
                                    operators[counter] = strarrOperators[k];
                                    counter++;
                                }
                                break;
                            }
                        }
                    }
                    if (indexFound[0] && indexFound[1] && indexFound[2]) {
                        indexExists = true;
                        indexName = indexName + index[0] + index[1] + index[2];
                        break;
                    }
                }
            }
            if(indexExists){
                Octree octree = deserializeIndex(indexName,table.getTableName());
                Vector<Tuple> result = new Vector<Tuple>();
                ArrayList<Integer> pages = octree.select(terms,operators);
                for (Integer i : pages){
                    Page page = deserializePage(table.getTableName(),i);
                    for (Tuple tuple : page.getPageTuples()){
                        if(evaluateSQLTerms(arrSQLTerms,strarrOperators,tuple)){
                            result.add(tuple);
                        }
                    }
                }
                return result.iterator();
            }


            for(int i = 1; i<= table.getNumberOfPages();i++){
                Page page = deserializePage(arrSQLTerms[0].getTableName(),i);
                for(Tuple tuple : page.getPageTuples()){
                     Boolean result = evaluateSQLTerms(arrSQLTerms,strarrOperators,tuple);
                     if(result==true){
                         resultTuples.add(tuple);
                     }
                }
            }
            return resultTuples.iterator();
        } catch (Exception e) {
            throw new DBAppException(e);
        }
    }

    public static void test(){
        ///
    }
    public static void test2(){
        ///
    }

    public static void updateMetaDataIndex(String strTableName, String[] colName, String indexName, String indexType) throws IOException {
        File current = new File("src/main/resources/metadata.csv");
        File temp = new File("src/main/resources/metadata.csv.tmp");
        BufferedReader bufferedReader = new BufferedReader(new FileReader(current));
        BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(temp));
        String line = "";
        while((line=bufferedReader.readLine())!=null){
            String[] curLine = line.split(",");
            if(curLine[0].equals(strTableName)){
                if(curLine[1].equals(colName[0]) || curLine[1].equals(colName[1]) || curLine[1].equals(colName[2]) ){
                    String newLine = "";
                    String[] newArr = {curLine[0],curLine[1],curLine[2],curLine[3], indexName, indexType, curLine[6],curLine[7]};
                    int i = 0;
                    while(i<newArr.length){
                        if(i == newArr.length - 1){
                            newLine = newLine + newArr[i];
                        }
                        else{
                            newLine = newLine + newArr[i] + ",";
                        }
                        i++;
                    }
                    bufferedWriter.write(newLine);
                    bufferedWriter.newLine();
                }
                else{
                    bufferedWriter.write(line);
                    bufferedWriter.newLine();
                }
            }
        }
        bufferedReader.close();
        bufferedWriter.close();
        temp.renameTo(current);
        current.delete();
        temp.renameTo(current);
    }
    public static void fillIndex(String strtablename, String x, String y, String z) throws DBAppException, IOException, ClassNotFoundException {
        Table table = deserializeTable(strtablename);
        Octree octree = deserializeIndex(x+y+z,strtablename);
        for(int i=1;i<=table.getNumberOfPages();i++){
           Page page = deserializePage(strtablename,i);
           for(int j=0;j<page.getPageTuples().size();j++){
               Tuple tuple = page.getPageTuples().get(j);
               Object x1 = tuple.getHtblColNameValue().get(x);
               Object y1 = tuple.getHtblColNameValue().get(y);
               Object z1 = tuple.getHtblColNameValue().get(z);
               Object[] toBeInserted = {x1,y1,z1,i};
               octree.insert(toBeInserted);
           }
        }
        serializeIndex(octree,strtablename);
    }

    public Iterator parseSQL( StringBuffer strbufSQL ) throws DBAppException {

        DBApp app = new DBApp();

        SQLParser parser = new SQLParser();

        if(parser.getOperation(strbufSQL).equals("INSERT")){
            Object[] arr = parser.prepareToInsert(strbufSQL);
            app.insertIntoTable(arr[0].toString(),(Hashtable<String, Object>) arr[1]);
        }
        else if(parser.getOperation(strbufSQL).equals("UPDATE")){
            Object[] arr = parser.prepareToUpdate(strbufSQL);
            app.updateTable(arr[0].toString(),arr[1].toString(),(Hashtable<String, Object>) arr[2]);
        }
        else if(parser.getOperation(strbufSQL).equals("DELETE")){
            Object[] arr = parser.prepareToDelete(strbufSQL);
            app.deleteFromTable(arr[0].toString(),(Hashtable<String, Object>) arr[1]);
        }
        else if(parser.getOperation(strbufSQL).equals("SELECT")){
            return app.selectFromTable(SQLParser.convertToSQLTerms(strbufSQL),SQLParser.extractOperators(strbufSQL));
        }
        else if(parser.getOperation(strbufSQL).equals("CREATETABLE")){
            Object[] arr = parser.prepareToCreateTable(strbufSQL);
            app.createTable(arr[0].toString(),arr[1].toString(),(Hashtable<String, String>)arr[2],(Hashtable<String, String>)arr[3],(Hashtable<String, String>)arr[4]);
        }
        else if(parser.getOperation(strbufSQL).equals("CREATEINDEX")){
            Object[] arr = parser.prepareToCreateIndex(strbufSQL);
            app.createIndex(arr[0].toString(),(String[]) arr[1]);
        }

        return  null;
    }

    public static void main(String[] args) throws DBAppException {
        DBApp dbApp = new DBApp();



//		Hashtable htblColNameType = new Hashtable( );
//		htblColNameType.put("id", "java.lang.Integer");
//		htblColNameType.put("name", "java.lang.String");
//		htblColNameType.put("gpa", "java.lang.Double");
//		htblColNameType.put("age", "java.lang.Integer");
//
//		Hashtable htblColNameMin = new Hashtable( );
//		htblColNameMin.put("id", "0");
//		htblColNameMin.put("name", "A");
//		htblColNameMin.put("gpa", "0.0");
//		htblColNameMin.put("age", "1");
//
//		Hashtable htblColNameMax = new Hashtable( );
//		htblColNameMax.put("id", "100");
//		htblColNameMax.put("name", "ZZZZZZZZZZZ");
//		htblColNameMax.put("gpa", "8.0");
//		htblColNameMax.put("age", "61");
//
//		dbApp.createTable( "Student", "id", htblColNameType,htblColNameMin,htblColNameMax) ;



        Hashtable htblColNameValue = new Hashtable( );
//        htblColNameValue.put("id", new Integer( 5 ));
//        htblColNameValue.put("name", new String("Ebram" ) );
//        htblColNameValue.put("gpa", new Double( 1.5 ) );
//        htblColNameValue.put("age", new Integer( 10 ) );
////
//        insertIntoTable( "Student" , htblColNameValue );

//        htblColNameValue.put("id", new Integer( 5 ));
//        htblColNameValue.put("name", new String("Slim" ) );
//        htblColNameValue.put("gpa", new Double( 7.5 ) );
//        htblColNameValue.put("age", new Integer( 5 ) );
//
//        insertIntoTable( "Student" , htblColNameValue );
//
//
//
//
//
//        htblColNameValue = new Hashtable( );
//        htblColNameValue.put("id", new Integer( 27 ));
//        htblColNameValue.put("name", new String("Arwa" ) );
//        htblColNameValue.put("gpa", new Double( 0.3 ) );
//        htblColNameValue.put("age", new Integer( 21 ) );
//        insertIntoTable( "Student" , htblColNameValue );

//        htblColNameValue = new Hashtable( );
//        htblColNameValue.put("id", new Integer( 25 ));
//        htblColNameValue.put("name", new String("Maya" ) );
//        htblColNameValue.put("gpa", new Double( 7.3 ) );
//        htblColNameValue.put("age", new Integer( 33 ) );
//        insertIntoTable( "Student" , htblColNameValue );

//        htblColNameValue = new Hashtable( );
//        htblColNameValue.put("id", new Integer( 30 ));
//        htblColNameValue.put("name", new String("Sergio" ) );
//        htblColNameValue.put("gpa", new Double( 7.5 ) );
//        htblColNameValue.put("age", new Integer( 5 ) );
//
//        insertIntoTable( "Student" , htblColNameValue );
//
//        htblColNameValue = new Hashtable( );
//        htblColNameValue.put("id", new Integer( 35 ));
//        htblColNameValue.put("name", new String("Nour" ) );
//        htblColNameValue.put("gpa", new Double( 2.6 ) );
//        htblColNameValue.put("age", new Integer( 55 ) );
//        insertIntoTable( "Student" , htblColNameValue );
//
//        htblColNameValue = new Hashtable( );
//        htblColNameValue.put("id", new Integer( 40 ));
//        htblColNameValue.put("name", new String("Sergio" ) );
//        htblColNameValue.put("gpa", new Double( 7.5 ) );
//        htblColNameValue.put("age", new Integer( 5 ) );
//
//        insertIntoTable( "Student" , htblColNameValue );
//
//        htblColNameValue = new Hashtable( );
//        htblColNameValue.put("id", new Integer( 41 ));
//        htblColNameValue.put("name", new String("Gerard" ) );
//        htblColNameValue.put("gpa", new Double( 4.8 ) );
//        htblColNameValue.put("age", new Integer( 40 ) );
//
//        insertIntoTable( "Student" , htblColNameValue );
//
//        htblColNameValue = new Hashtable( );
//        htblColNameValue.put("id", new Integer( 45 ));
//        htblColNameValue.put("name", new String("Isco" ) );
//        htblColNameValue.put("gpa", new Double( 0.7 ) );
//        htblColNameValue.put("age", new Integer( 17 ) );
//        insertIntoTable( "Student" , htblColNameValue );
//
//        htblColNameValue = new Hashtable( );
//        htblColNameValue.put("id", new Integer( 30 ));
//        htblColNameValue.put("name", new String("Marcello" ) );
//        htblColNameValue.put("gpa", new Double( 5.9) );
//        htblColNameValue.put("age", new Integer( 49 ) );
//        insertIntoTable( "Student" , htblColNameValue );
//
//        htblColNameValue = new Hashtable( );
//        htblColNameValue.put("id", new Integer( 55 ));
//        htblColNameValue.put("name", new String("Sergio" ) );
//        htblColNameValue.put("gpa", new Double( 7.5 ) );
//        htblColNameValue.put("age", new Integer( 5 ) );
//
//        insertIntoTable( "Student" , htblColNameValue );
//
//
//        htblColNameValue = new Hashtable( );
//        htblColNameValue.put("id", new Integer( 44 ));
//        htblColNameValue.put("name", new String("Toni" ) );
//        htblColNameValue.put("gpa", new Double( 7.2 ) );
//        htblColNameValue.put("age", new Integer( 27 ) );
//        insertIntoTable( "Student" , htblColNameValue );
//
//        htblColNameValue = new Hashtable( );
//        htblColNameValue.put("id", new Integer( 65 ));
//        htblColNameValue.put("name", new String("Nacho" ) );
//        htblColNameValue.put("gpa", new Double( 5.7 ) );
//        htblColNameValue.put("age", new Integer( 59 ) );
//        insertIntoTable( "Student" , htblColNameValue );
//
//        htblColNameValue = new Hashtable( );
//        htblColNameValue.put("id", new Integer( 70 ));
//        htblColNameValue.put("name", new String("Busquets" ) );
//        htblColNameValue.put("gpa", new Double( 6.3 ) );
//        htblColNameValue.put("age", new Integer( 43) );
//
//        insertIntoTable( "Student" , htblColNameValue );
//
//        htblColNameValue = new Hashtable( );
//        htblColNameValue.put("id", new Integer( 75 ));
//        htblColNameValue.put("name", new String("Sergio" ) );
//        htblColNameValue.put("gpa", new Double( 7.5 ) );
//        htblColNameValue.put("age", new Integer( 5 ) );
//
//        insertIntoTable( "Student" , htblColNameValue );

//        Hashtable hashtable = new Hashtable();
//        hashtable.put("name","Ebram");
//        hashtable.put("age",10);
//        hashtable.put("gpa",1.5);
//
//        dbApp.deleteFromTable("Student",hashtable);
//
//        Octree octree = deserializeIndex("namegpaage","Student");
//        Object[] arr = {"Arwa",0.3,21,1,1};
//        octree.insert(arr);
//        octree.printTree();



//        Hashtable hashtable = new Hashtable();
//        hashtable.put("name", "Arwa");
//        hashtable.put("age",21);
//        hashtable.put("gpa",2.0);
//        Tuple tuple = new Tuple(hashtable);
//
//        SQLTerm sqlTerm = new SQLTerm("Student","age", "<=",2);
//        Boolean x = SQLTermHelper(sqlTerm,tuple);
//        System.out.println(x);


        //"name = arwa && age < 30 || gpa = 5.7"
//        SQLTerm sqlTerm1 = new SQLTerm("Student", "name", "=", "Arwa" );
//        SQLTerm sqlTerm2 = new SQLTerm("Student", "gpa", "=", 5.7 );
//        SQLTerm sqlTerm3 = new SQLTerm("Student", "age", "<", 30 );
//        SQLTerm sqlTerm4 = new SQLTerm("Student", "id", "!=", 27 );
//
//
//        SQLTerm[] sqlTerms = {sqlTerm1,sqlTerm2,sqlTerm3,sqlTerm4};


//        Hashtable hashtable = new Hashtable();
//        hashtable.put("name", "Ebram");
//        hashtable.put("age", 10);

//        htblColNameValue.put("id", new Integer( 22 ));
//        htblColNameValue.put("name", new String("Rami" ) );
//        htblColNameValue.put("gpa", new Double( 7.5 ) );
//        htblColNameValue.put("age", new Integer( 5 ) );
//        insertIntoTable( "Student" , htblColNameValue );






//        htblColNameValue = new Hashtable( );
//        htblColNameValue.put("id", new Integer( 44 ));
//        htblColNameValue.put("name", new String("Toni" ) );
//        htblColNameValue.put("gpa", new Double( 7.2 ) );
//        htblColNameValue.put("age", new Integer( 27 ) );
//        insertIntoTable( "Student" , htblColNameValue );
//
//        htblColNameValue = new Hashtable( );
//        htblColNameValue.put("id", new Integer( 30 ));
//        htblColNameValue.put("name", new String("Marcello" ) );
//        htblColNameValue.put("gpa", new Double( 5.9) );
//        htblColNameValue.put("age", new Integer( 49 ) );
//        insertIntoTable( "Student" , htblColNameValue );
//
//        htblColNameValue = new Hashtable( );
//        htblColNameValue.put("id", new Integer( 35 ));
//        htblColNameValue.put("name", new String("Nour" ) );
//        htblColNameValue.put("gpa", new Double( 2.6 ) );
//        htblColNameValue.put("age", new Integer( 55 ) );
//        insertIntoTable( "Student" , htblColNameValue );
//
//        htblColNameValue = new Hashtable( );
//        htblColNameValue.put("id", new Integer( 25 ));
//        htblColNameValue.put("name", new String("Maya" ) );
//        htblColNameValue.put("gpa", new Double( 7.3 ) );
//        htblColNameValue.put("age", new Integer( 33 ) );
//        insertIntoTable( "Student" , htblColNameValue );
//
//        htblColNameValue = new Hashtable( );
//        htblColNameValue.put("id", new Integer( 27 ));
//        htblColNameValue.put("name", new String("Arwa" ) );
//        htblColNameValue.put("gpa", new Double( 0.3 ) );
//        htblColNameValue.put("age", new Integer( 21 ) );
//        insertIntoTable( "Student" , htblColNameValue );

//        htblColNameValue = new Hashtable( );
//        htblColNameValue.put("id", new Integer( 45 ));
//        htblColNameValue.put("name", new String("Isco" ) );
//        htblColNameValue.put("gpa", new Double( 0.7 ) );
//        htblColNameValue.put("age", new Integer( 17 ) );
//        insertIntoTable( "Student" , htblColNameValue );

//        htblColNameValue = new Hashtable( );
//        htblColNameValue.put("id", new Integer( 65 ));
//        htblColNameValue.put("name", new String("Ebram" ) );
//        htblColNameValue.put("gpa", new Double( 5.7 ) );
//        htblColNameValue.put("age", new Integer( 59 ) );
//        insertIntoTable( "Student" , htblColNameValue );

//        Hashtable hashtable = new Hashtable();
//        hashtable.put("name","Maya");
//        dbApp.deleteFromTable("Student",hashtable);
//
//        hashtable = new Hashtable();
//        hashtable.put("name","Arwa");
//        dbApp.deleteFromTable("Student",hashtable);
//
//        hashtable = new Hashtable();
//        hashtable.put("name","Marcello");
//        dbApp.deleteFromTable("Student",hashtable);
//
//        hashtable = new Hashtable();
//        hashtable.put("name","Nour");
//        dbApp.deleteFromTable("Student",hashtable);
//
//        hashtable = new Hashtable();
//        hashtable.put("name","Toni");
//        dbApp.deleteFromTable("Student",hashtable);

//        updateIndexPages("Student",1,new String[]{"name","gpa","age"});
//        String[] arr = {"name","gpa","age"};
//        dbApp.createIndex("Student", arr);


//        Hashtable hashtable = new Hashtable();
//        hashtable.put("age",30);
//        dbApp.updateTable("Student","25",hashtable);

//        System.out.println(displayTablePages("Student"));
//        Table table = deserializeTable("Student");
//        System.out.println(table.getAllIndices().get(0)[0]);
//        Octree octree = deserializeIndex("namegpaage",table.getTableName());
//        octree.printTree();


//        table.getAllIndices().clear();
//        serializeTable(table);
//        Page page = deserializePage("Student",1);
//
//        Tuple tuple = page.getPageTuples().get(0);
//        tuple.getHtblColNameValue().put("name", "Maya");
//        serializePage(page);
//        System.out.println(displayTablePages("Student"));
//


//        String[] arr = {"name","age", "gpa"};
//        updateMetaDataIndex("Student",arr ,"namegpaage", "Octree");

//        SQLTerm sqlTerm = new SQLTerm("Student", "id", "=", 27 );
//        SQLTerm[] sqlTerms = {sqlTerm};
//        String[] operators = {"AND"};
//
//        StringBuffer buffer = new StringBuffer("SELECT * FROM Student WHERE name = 'Arwa' OR age > 30");
//        StringBuffer buffer = new StringBuffer("UPDATE Student SET name = 'Ramos', age = 50 WHERE id = 30");
//        dbApp.parseSQL(buffer);

//            StringBuffer sqlStatement = new StringBuffer("    delete     FROM    Student     WHERE     name    = 'arwa'    AND     age = 21");

//        Iterator iterator = dbApp.parseSQL(sqlStatement);

//        while(iterator.hasNext()){
//            Tuple tuple = (Tuple)iterator.next();
//            System.out.println(tuple);
//        }
//        System.out.println(displayTablePages("Student"));


    }


}
