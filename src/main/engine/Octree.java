package main.engine;
import java.io.Serializable;
import java.util.*;

public class Octree implements Serializable {

   private class Node implements Serializable{
        private double minX,maxX,minY,maxY,minZ,maxZ;
        private Node[] children;
        private ArrayList<Object[]> elements;


        public Node(double minX, double maxX, double minY, double maxY, double minZ, double maxZ) {
            this.minX = minX;
            this.maxX = maxX;
            this.minY = minY;
            this.maxY = maxY;
            this.minZ = minZ;
            this.maxZ = maxZ;
            this.children = new Node[8];
            this.elements = new ArrayList<>();
        }

        @java.lang.Override
        public java.lang.String toString() {
            return "Node{" +
                    "minX=" + minX +
                    ", maxX=" + maxX +
                    ", minY=" + minY +
                    ", maxY=" + maxY +
                    ", minZ=" + minZ +
                    ", maxZ=" + maxZ +
                    ", elements=" + displayElements(elements) +
                    '}';
        }

        public String displayElements(ArrayList<Object[]> list){
            if(list == null)
                return "";
            String str = "[ ";
            for (Object[] arr : list) {
                for (int i = 0; i < arr.length; i++) {
                    if (i == arr.length - 1)
                        str += arr[i];
                    else
                        str += arr[i] + " , ";
                }
                str += " | ";
            }
            str += " ]";

            return str;
        }

        public boolean isLeaf(){
            return this.children[0]==null;
        }



    }

    private Node root;
    private String xName, yName, zName;


    public Octree(double minX, double maxX, double minY, double maxY, double minZ, double maxZ, String xName, String yName, String zName) {
        root=new Node(minX,maxX,minY,maxY,minZ,maxZ);
        this.xName=xName;
        this.yName=yName;
        this.zName=zName;
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

    public void insert(Object[] arr) throws DBAppException {
        insert(arr,root);


    }

    public void insert(Object[] arr,Node node ) throws DBAppException {

        if(node.isLeaf()){
            checkWithinRange(arr,node);
            if(node.elements.size()<DBConfig.getOctreeNodeEntries()){
                node.elements.add(arr);
            }
            else{
                splitNode(node);
                insert(arr,node);
            }
        }
        else {
            int childIndex = getChildIndex(arr,node);
            insert(arr,node.children[childIndex]);
        }
    }

    public void splitNode(Node node) throws DBAppException {
        double midX=(node.minX+ node.maxX)/2;
        double midY=(node.minY+ node.maxY)/2;
        double midZ=(node.minZ+ node.maxZ)/2;

        node.children[0] = new Node(node.minX, midX, node.minY, midY, node.minZ, midZ);
        node.children[1] = new Node(node.minX, midX, node.minY, midY, midZ, node.maxZ);
        node.children[2] = new Node(node.minX, midX, midY, node.maxY, node.minZ, midZ);
        node.children[3] = new Node(node.minX, midX, midY, node.maxY, midZ, node.maxZ);
        node.children[4] = new Node(midX, node.maxX, node.minY, midY, node.minZ, midZ);
        node.children[5] = new Node(midX, node.maxX, node.minY, midY, midZ, node.maxZ);
        node.children[6] = new Node(midX, node.maxX,midY,node.maxY,node.minZ,midZ);
        node.children[7] = new Node(midX, node.maxX, midY, node.maxY, midZ, node.maxZ);

        for (Object[] arr : node.elements){
            int childIndex = getChildIndex(arr,node);
            insert(arr,node.children[childIndex]);
        }
        node.elements = null;
    }

    public int getChildIndex(Object[] arr, Node node) throws DBAppException {
        for (int i = 0 ; i<=7 ; i++){
            Node current = node.children[i];
            if(checkWithinRange(arr,current))
                return i;
        }
        throw new DBAppException("Cannot insert : "+arr[0]+" , "+arr[1]+" , "+arr[2]);
    }

    public static boolean checkWithinRange(Object[] arr,Node current){
        String s1 = arr[0]+"";
        String s2 = arr[1]+"";
        String s3 = arr[2]+"";

//        if(arr[0] instanceof String){
//            if(Double.parseDouble(arr[0].toString().hashCode()+"") < 0)
//                s1 = Double.parseDouble((arr[0].toString().hashCode()+"")+"";
//            else
//                s1 = Double.parseDouble(arr[0].toString().hashCode()+"")+"";
//        }
//        if(arr[1] instanceof String){
//            if(Double.parseDouble(arr[1].toString().hashCode()+"") < 0)
//                s2 = Double.parseDouble((arr[1]+" ").toString().hashCode()+"")+"";
//            else
//                s2 = Double.parseDouble(arr[1].toString().hashCode()+"")+"";
//        }
//        if(arr[2] instanceof String){
//            if(Double.parseDouble(arr[2].toString().hashCode()+"") < 0)
//                s3 = Double.parseDouble((arr[2]+" ").toString().hashCode()+"")+"";
//            else
//                s3 = Double.parseDouble(arr[2].toString().hashCode()+"")+"";
//        }

//        if(arr[0] instanceof String){
//            Integer h1 = s1.hashCode();
//            Integer h2 = (int)current.minX;
//            Long l1 = h1.longValue();
//            Long l2 = h2.longValue();
//            int result =  l1.compareTo(l2);
//
//        }
//        boolean xWithin = false;
//        boolean yWithin = false;
//        boolean zWithin = false;
//
//        if(arr[0] instanceof String) {
//            Double hash = (double)arr[0].hashCode();
//            Double minHash = current.minX;
//            Double maxHash = current.maxX;
//            if(hash.compareTo(maxHash) < 0 && hash.compareTo(minHash) > 0)
//                xWithin = true;
//        }
//        if(arr[1] instanceof String) {
//            Double hash = (double)arr[1].hashCode();
//            Double minHash = current.minY;
//            Double maxHash = current.maxY;
//            if(hash.compareTo(maxHash) < 0 && hash.compareTo(minHash) > 0)
//                yWithin = true;
//        }
//        if(arr[2] instanceof String) {
//            Double hash = (double)arr[2].hashCode();
//            Double minHash = current.minZ;
//            Double maxHash = current.maxZ;
//            if(hash.compareTo(maxHash) < 0 && hash.compareTo(minHash) > 0)
//                zWithin = true;
//        }
        if(arr[0] instanceof String){
            Double hashCode = Double.parseDouble((arr[0].toString().hashCode()+""));
            if(hashCode < 0)
                s1 = Math.abs(hashCode)+"";
            else
                s1 = hashCode+"";
        }
        if(arr[1] instanceof String){
            Double hashCode = Double.parseDouble((arr[1].toString().hashCode()+""));
            if(hashCode < 0)
                s2 = Math.abs(hashCode)+"";
            else
                s2 = hashCode+"";
        }
        if(arr[2] instanceof String){
            Double hashCode = Double.parseDouble((arr[2].toString().hashCode()+""));
            if(hashCode < 0)
                s3 = Math.abs(hashCode)+"";
            else
                s3 = hashCode+"";
        }


        if(compareTo(s1,current.minX+"") >= 0 && compareTo(s1,current.maxX+"") < 0){
            if( compareTo(s2,current.minY+"") >= 0 && compareTo(s2,current.maxY+"") < 0){
                if( compareTo(s3,current.minZ+"") >= 0 && compareTo(s3,current.maxZ+"") < 0){
                    return true;
                }
            }
        }

        return false;
    }

    public void printTree(){
        printTree(root,"");
    }

    public void printTree(Node node, String prefix){
        if(node!=null){
            System.out.println(prefix+"------"+node.toString());
            if(!node.isLeaf()){
                prefix += "    ";
                for (int i = 0 ; i <= 7 ; i++){
                    printTree(node.children[i], prefix+ ((i == node.children.length -1) ? "    " : "|   "));
                }
            }
        }
    }

    public void delete(Object []arr){
        delete(arr,root);
    }

    public void delete(Object[] arr,Node node){
        if(node.isLeaf()){
            Iterator<Object[]> iterator=node.elements.iterator();
            while(iterator.hasNext()){
                Object[] cur= iterator.next();
                if(compareArr(arr,cur)){
                    iterator.remove();
                }
            }
        }
        else{
            for(Node n: node.children){
                if(checkWithinRange(arr,n))
                    delete(arr,n);
            }
        }
    }

    public static boolean compareArr(Object[] arr1,Object[] arr2){

        return arr1[0].equals(arr2[0]) && arr1[1].equals(arr2[1]) && arr1[2].equals(arr2[2]);

    }

    public void update(Object[] oldObject,Object[] newObject) throws DBAppException {
        delete(oldObject);
        insert(newObject);

    }

    public String getFullName(){
        return xName + yName + zName;
    }

    public ArrayList<Object[]> search(Object[] arr){
        ArrayList<Object[]> result = new ArrayList<Object[]>();
        searchHelper(arr,root,result);
        return result;
    }

    public void searchHelper(Object[] arr,Node node,ArrayList<Object[]> result){
        if(checkWithinRange(arr,node)){
            if(node.isLeaf()){
                for(int i=0;i<node.elements.size();i++){
                    if(compareArr(arr,node.elements.get(i))){
                        result.add(node.elements.get(i));
                    }
                }
            }
            else{
                for(Node n : node.children){
                    searchHelper(arr,n,result);
                }
            }
        }
    }
    public static boolean evaluateSql(SQLTerm term,double min,double max){
        switch(term.getOperator()){
            case "=": if(term.getValue() instanceof String ){
                return term.getValue().hashCode() >= min && term.getValue().hashCode() <= max;
            }
            else{
                return Double.valueOf(term.getValue().toString()) >= min && Double.valueOf(term.getValue().toString()) <= max;
            }
            case"!=":if(term.getValue() instanceof String ){
                return !(term.getValue().hashCode() >= min && term.getValue().hashCode() <= max);
            }
            else{
                return !(Double.valueOf(term.getValue().toString()) >= min && Double.valueOf(term.getValue().toString()) <= max);
            }
            case ">" : return (Double.valueOf(term.getValue().toString()) < min) ||((Double.valueOf(term.getValue().toString()) >= min && Double.valueOf(term.getValue().toString()) <= max));
            case">=":  return (Double.valueOf(term.getValue().toString()) <= min) || (Double.valueOf(term.getValue().toString()) >= min && Double.valueOf(term.getValue().toString()) <= max) ;
            case"<":   return (Double.valueOf(term.getValue().toString()) > max) || (Double.valueOf(term.getValue().toString()) >= min && Double.valueOf(term.getValue().toString()) <= max);
            case"<=":  return (Double.valueOf(term.getValue().toString()) >= max) || (Double.valueOf(term.getValue().toString()) >= min && Double.valueOf(term.getValue().toString()) <= max);
        }

     return false;

    }

    public static boolean evaluateAllTerms(SQLTerm[] arrSQLTerms,String[] strarrOperators,Node node){
        boolean firstTerm= evaluateSql(arrSQLTerms[0], node.minX, node.maxX);
        boolean secondTerm= evaluateSql(arrSQLTerms[1], node.minY, node.maxY);
        boolean thirdTerm= evaluateSql(arrSQLTerms[2], node.minZ, node.maxZ);
        boolean result1=false;
        switch (strarrOperators[0]){
            case "AND": result1 = firstTerm && secondTerm;break;
            case "XOR": result1 = firstTerm ^ secondTerm;break;
            case "OR" : result1 = firstTerm || secondTerm;break;
        }
       switch (strarrOperators[1]){
           case "AND": return result1 && thirdTerm;
           case "XOR": return result1 ^ thirdTerm;
           case "OR" : return result1 || thirdTerm;
       }
     return false;
    }

    public ArrayList<Integer> select(SQLTerm[] arrSQLTerms,String[] strarrOperators){
        ArrayList<Integer> result = new ArrayList<Integer>();
        selectHelper(root,arrSQLTerms,strarrOperators,result);

        HashSet<Integer> set = new HashSet<>(result);
        ArrayList<Integer> resultSet = new ArrayList<>(set);
        return resultSet;
    }

    public void selectHelper(Node node,SQLTerm[] arrSQLTerms,String[] strarrOperators, ArrayList<Integer> result){
        if(node.isLeaf()){
            if(evaluateAllTerms(arrSQLTerms,strarrOperators,node)){
                for(Object[] array: node.elements){
                    if(evaluateObjectSatisfies(arrSQLTerms,strarrOperators,array) ){
                        result.add((Integer) array[3]);
                    }
                }
            }
        }
        else{
            for(Node n : node.children){
                selectHelper(n,arrSQLTerms,strarrOperators,result);
            }
        }

    }

    public static boolean evaluateObjectSatisfies(SQLTerm[] arr,String[] starOperators,Object[] objectArr){
        boolean result1 = false;
        boolean result2 = false;
        boolean expression = false;
        boolean result3 = false;

        switch(arr[0].getOperator()){
            case "=": if(arr[0].getValue() instanceof String ){
                result1= arr[0].getValue().equals(objectArr[0].toString()) ;
            }
            else{
                result1= Double.valueOf(arr[0].getValue().toString()) == Double.valueOf(objectArr[0].toString()) ;
            }break;
            case"!=":if(arr[0].getValue() instanceof String ){
                result1= Double.parseDouble(arr[0].getValue().hashCode()+"") != Double.parseDouble(arr[0].getValue().hashCode()+"") ;
            }
            else{
                result1 =Double.valueOf(arr[0].getValue().toString()) != Double.valueOf(objectArr[0].toString()) ;
            }break;
            case ">" : result1 = Double.valueOf(objectArr[0].toString()) > Double.valueOf(arr[0].getValue().toString());break;
            case "<" : result1 = Double.valueOf(objectArr[0].toString()) < Double.valueOf(arr[0].getValue().toString());break;
            case "<=" : result1 = Double.valueOf(objectArr[0].toString()) <= Double.valueOf(arr[0].getValue().toString());break;
            case ">=" : result1 = Double.valueOf(objectArr[0].toString()) >= Double.valueOf(arr[0].getValue().toString());break;
        }
        switch(arr[1].getOperator()){
            case "=": if(arr[1].getValue() instanceof String ){
                result2= arr[1].getValue().equals(objectArr[1].toString()) ;
            }
            else{
                result2= Double.valueOf(arr[1].getValue().toString()) == Double.valueOf(objectArr[1].toString()) ;
            }break;
            case"!=":if(arr[1].getValue() instanceof String ){
                result2= Double.parseDouble(arr[1].getValue().hashCode()+"") != Double.parseDouble(arr[1].getValue().hashCode()+"") ;
            }
            else{
                result2 =Double.valueOf(arr[1].getValue().toString()) != Double.valueOf(objectArr[1].toString()) ;
            }break;
            case ">" : result2 = Double.valueOf(objectArr[1].toString()) > Double.valueOf(arr[1].getValue().toString());break;
            case "<" : result2 = Double.valueOf(objectArr[1].toString()) < Double.valueOf(arr[1].getValue().toString());break;
            case "<=" : result2 = Double.valueOf(objectArr[1].toString()) <= Double.valueOf(arr[1].getValue().toString());break;
            case ">=" : result2 = Double.valueOf(objectArr[1].toString()) >= Double.valueOf(arr[1].getValue().toString());break;
        }
        switch(arr[2].getOperator()){
            case "=": if(arr[2].getValue() instanceof String ){
                result3= arr[2].getValue().equals(objectArr[2].toString()) ;
            }
            else{
                result3= Double.valueOf(arr[2].getValue().toString()) == Double.valueOf(objectArr[2].toString()) ;
            }break;
            case"!=":if(arr[2].getValue() instanceof String ){
                result3= Double.parseDouble(arr[2].getValue().hashCode()+"") != Double.parseDouble(arr[2].getValue().hashCode()+"") ;
            }
            else{
                result3 =Double.valueOf(arr[2].getValue().toString()) != Double.valueOf(objectArr[2].toString()) ;
            }break;
            case ">" : result3 = Double.valueOf(objectArr[2].toString()) > Double.valueOf(arr[2].getValue().toString());break;
            case "<" : result3 = Double.valueOf(objectArr[2].toString()) < Double.valueOf(arr[2].getValue().toString());break;
            case "<=" : result3 = Double.valueOf(objectArr[2].toString()) <= Double.valueOf(arr[2].getValue().toString());break;
            case ">=" : result3 = Double.valueOf(objectArr[2].toString()) >= Double.valueOf(arr[2].getValue().toString());break;
        }
        switch (starOperators[0]){
            case "AND" : expression = result1 && result2;break;
            case "OR" : expression = result1 || result2;break;
            case  "XOR" : expression = result1 ^ result2;break;
        }
        switch (starOperators[1]){
            case "AND" : return expression && result3;
            case "OR" : return expression || result3;
            case  "XOR" : return expression ^ result3;
        }
        return false;
    }









    public static void main(String[] args) throws DBAppException {
        Octree octree = new Octree(1.0,9.33463706E8,0.0,5.5,1.0,40.0,"","","");
        Object[] o1 = {"Arwa",2.1,20.0,1};//
        Object[] o2 = {"ebram",0.3,39.0,1};//
        Object[] o3 = {"maya",1.8,6.0,3};//
        Object[] o4 = {"nour",4.1,2.0,4};//
        Object[] o5 = {"slim",1.9,25.0,5};//
        Object[] o6 = {"ashry",3.1,35.0,6};
        Object[] o7 = {"arxa",2.2,10.0,7};//

        octree.insert(o1);
        octree.insert(o2);
        octree.insert(o3);
        octree.insert(o4);
        octree.insert(o5);
        octree.insert(o6);
        octree.insert(o7);
//        Object[] old = {"nour",4.1,2.0};
//        Object[] newVersion = {"bour",3.2,2.0};


//        octree.update(old,newVersion);
        octree.printTree();

        SQLTerm sqlTerm1 = new SQLTerm("Student", "name", "=", "Arwa" );
        SQLTerm sqlTerm2 = new SQLTerm("Student", "gpa", ">", 1.0 );
        SQLTerm sqlTerm3 = new SQLTerm("Student", "age", ">", 30.0 );

        SQLTerm[] sqlTerms = {sqlTerm1,sqlTerm2,sqlTerm3};
        String[] operators = {"AND", "OR"};

        ArrayList<Integer> result = octree.select(sqlTerms,operators);

        System.out.println(result);


    }


}
