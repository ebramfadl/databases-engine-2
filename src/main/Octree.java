package main;

public class Octree {
   private class Node {
        private double minX,maxX,minY,maxY,minZ,maxZ;
        private Node[] children;
        private Arraylist<Object[]> elements;

        public Node(double minX, double maxX, double minY, double maxY, double minZ, double maxZ) {
            this.minX = minX;
            this.maxX = maxX;
            this.minY = minY;
            this.maxY = maxY;
            this.minZ = minZ;
            this.maxZ = maxZ;
            this.children = new Node[8];
            this.elements = new Arraylist<>();
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
                    ", elements=" + elements +
                    '}';
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



    public void insert(Object[] arr){
        insert(arr,root);


    }

    public void insert(Object[] arr,Node node ){
        if(node.isLeaf()){

            if(node.elements.size()<2){
                elements.add(arr);

            }
            else{
                //node full



            }


        }


    }

    public void splitNode(Node node){
        double midX=(node.minX+ node.maxX)/2;
        double midY=(node.minY+ node.maxY)/2;
        double midZ=(node.minZ+ node.maxZ)/2;
        node.children[1] = new Node(node.minX, midX, node.minY, midY, node, node.zMax);
        node.children[2] = new Node(node.xMin, xMid, yMid, node.yMax, node.zMin, zMid);
        node.children[3] = new Node(node.xMin, xMid, yMid, node.yMax, zMid, node.zMax);
        node.children[4] = new Node(xMid, node.xMax, node.yMin, yMid, node.zMin, zMid);
        node.children[5] = new Node(xMid, node.xMax, node.yMin, yMid, zMid, node.zMax);
        node.children[6] = new Node(xMid, node.xMax,yMid,node.yMax,node.zMin,zMid);
        node.children[7] = new Node(xMid, node.xMax, yMid, node.yMax, zMid, node.zMax);






    }






}
