package PQsearch;

public class Tree {
	private Node root;
	
	public Tree(Node root){
		this.root = root;
	}
	
	//constructor
	public void addNode(String name, Node parent, int index){
		Node newNode = new Node(name, parent, index);
		
		//start root of tree
		if(this.root == null){
			root = newNode;
		}else if(parent != root){
			parent.addChildren(newNode);
		}else if(root.getChildren().isEmpty()){
			root.addChildren(newNode);
			//newNode.setParent(root);
		}else if(!root.getChildren().isEmpty()){
			root.addChildren(newNode);
			//newNode.setParent(root);
		}
	}
	
	//tree methods
	public Node getRoot(){
		//System.out.println("The root is: "+root.getName());
		return root;
	}
	
	
}
