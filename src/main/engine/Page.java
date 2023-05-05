package main.engine;

import java.io.Serializable;
import java.util.Hashtable;
import java.util.Vector;

public class Page implements Serializable {

    private String tableName;
    private int pageNumber;
    private int n;
    private String pagePath;

    private Vector<Tuple> pageTuples;

    public Page(int pageNumber, String tableName) {
        this.tableName = tableName;
        this.pageNumber = pageNumber;
        this.n = DBConfig.getPageMaximum();
        pageTuples = new Vector<>(n);

    }

    public Tuple addTuple(Hashtable<String , Object> htblColNameValue){

        Tuple tuple = new Tuple(htblColNameValue);
        pageTuples.add(tuple);
        return tuple;
    }


    public Vector<Tuple> getPageTuples() {
        return pageTuples;
    }

    public int getPageNumber() {
        return pageNumber;
    }

    public void setPageNumber(int pageNumber) {
        this.pageNumber = pageNumber;
    }

    public String getPagePath(){
        return "src/main/resources/data/"+tableName+"/"+pageNumber+".ser";
    }

    public String getTableName() {
        return tableName;
    }

    @Override
    public String toString() {
        String str = "";

        for (Tuple tuple : pageTuples){
            str = str + "-"+tuple.toString()+"\n";
        }

        return "Page number " +pageNumber+"\n"+
        "Maximum tuples "+n+"\n"+
        "Tuples" + "\n"+str+"\n";
    }
}
