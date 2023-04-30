package main;
import java.util.*;

public class Octree {
   private class Node {
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
//    public static contains(double x,double y,double z){
//        if(this.)
//
//
//    }



    }

    private Node root;

    public Octree(double minX, double maxX, double minY, double maxY, double minZ, double maxZ) {
        root=new Node(minX,maxX,minY,maxY,minZ,maxZ);

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

    public void insert(Object[] arr){
        insert(arr,root);


    }

    public void insert(Object[] arr,Node node ){
        if(node.isLeaf()){
            if(node.elements.size()<2){
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

    public void splitNode(Node node){
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

    public int getChildIndex(Object[] arr, Node node){
        for (int i = 0 ; i<=7 ; i++){
            Node current = node.children[i];
            if(checkWithinRange(arr,current))
                return i;
        }
        return -1;
    }

    public static boolean checkWithinRange(Object[] arr,Node current){
        String s1 = arr[0]+"";
        String s2 = arr[1]+"";
        String s3 = arr[2]+"";

        if(arr[0] instanceof String)
            s1 = Double.parseDouble(arr[0].toString().hashCode()+"")+"";
        if(arr[1] instanceof String)
            s2 = Double.parseDouble(arr[1].toString().hashCode()+"")+"";
        if(arr[2] instanceof String)
            s3 = Double.parseDouble(arr[2].toString().hashCode()+"")+"";


        if(compareTo(s1,current.minX+"") >= 0 && compareTo(s1,current.maxX+"") < 0){
            if(compareTo(s2,current.minY+"") >= 0 && compareTo(s2,current.maxY+"") < 0){
                if(compareTo(s3,current.minZ+"") >= 0 && compareTo(s3,current.maxZ+"") < 0){
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

    public static void main(String[] args) {
        Octree octree = new Octree(1.0,321456985.0,0.0,5.5,1.0,40.0);
        Object[] o1 = {"arwa",2.1,20.0};
        Object[] o2 = {"ebram",0.3,39.0};
        Object[] o3 = {"maya",1.8,6.0};
        Object[] o4 = {"nour",4.1,2.0};
        Object[] o5 = {"slim",1.9,25.0};
        Object[] o6 = {"ashry",3.1,35.0};
        Object[] o7 = {"arxa",2.2,10.0};

        octree.insert(o1);
        octree.insert(o2);
        octree.insert(o3);
        octree.insert(o4);
        octree.insert(o5);
        octree.insert(o6);
        octree.insert(o7);
        octree.printTree();


    }


}
