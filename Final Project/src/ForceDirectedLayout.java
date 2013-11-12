import java.util.ArrayList;

//Algorithm from paper: Simple Algorithms for Network Visualization: A Tutorial
//                      by: Michael J. McGuffin

public class ForceDirectedLayout {
	
    public double springRestLength = 10; //Preferred length of an edge, without forces
    public double repulsiveForceK = 10; //Between nodes, low: nodes less repulsive
    public double springK = 0.4; //Low: more flexible edges
    public double timeStep = 0.04;   
    public int numIterations = 5; //Number of times to run the force algorithm
    public ArrayList <Node> nodes;
    public ArrayList <Edge> edges;
    public int numNodes, numEdges;
    
    /** Constructor for setting the nodes, using the default constants
     * */
    ForceDirectedLayout(ArrayList<Node> n, ArrayList<Edge> e){
    	this.nodes = n;
    	this.numNodes = n.size();
    	this.edges = e;
    	this.numEdges = e.size();
    }
    
    /** Constructor for setting the constants
     * */
    ForceDirectedLayout(double l, double rK,double sK,double tS,ArrayList<Node> n, ArrayList<Edge> e){
    	this.springRestLength = l;
    	this.repulsiveForceK = rK;
    	this.springK = sK;
    	this.timeStep = tS;    	
    	this.nodes = n;
    	this.numNodes = n.size();
    	this.edges = e;
    	this.numEdges = e.size();
    }
    
    /**Generates the layout by assigning positions of nodes
     * */
    public void makeLayout(){
    	randomizePositions();
    	for (int i=0;i<this.numIterations;i++){
    		calculateForces();
    	}    	
    	for (int i=0;i<this.numNodes-1;i++){
    		
    		System.out.println("After: "+this.nodes.get(i).x+" "+this.nodes.get(i).y);
    	}
    }
    
    /**Sets all nodes to a random position on the screen
     * random number in range: min + Math.random()*((max-min)+1)
     * */
    public void randomizePositions(){
    	for (int i=0;i<this.numNodes-1;i++){
    		this.nodes.get(i).x = (int)(Math.random()*((800-0)+1));
    		this.nodes.get(i).y = (int)(Math.random()*((800-0)+1));    		
    		System.out.println("Before: "+this.nodes.get(i).x+" "+this.nodes.get(i).y);
    	}
    }
    
    /**Main body of the algorithm which computes all forces applied to the nodes and adjacent node pairs
     * */
    public void calculateForces (){
    	//Re-set the net forces
    	for (int i=0;i<this.numNodes;i++){
    		this.nodes.get(i).forceX = 0;
    		this.nodes.get(i).forceY = 0;
    	}
    	Node node1, node2;
    	double dx, dy,distanceSquared,distance,repulsiveForce,fx,fy;
    	
    	//Calculate the repulsive forces between all pairs of nodes
    	for (int i =0; i< this.numNodes;i++){
    	   node1 = this.nodes.get(i);
    		for (int j=i+1;j< this.numNodes-1;j++){
    			node2 = this.nodes.get(j);
    			dx = node2.x - node1.x;
    		    dy = node2.y - node1.y;
    			if (dx!=0 || dy!=0){
    				distanceSquared = dx*dx + dy*dy;
    				distance = Math.sqrt(distanceSquared);
    				repulsiveForce =  this.repulsiveForceK/distanceSquared;
    				fx = repulsiveForce*dx/distance;
    				fy = repulsiveForce*dy/distance;
    				node1.forceX = node1.forceX - fx;
    				node1.forceY = node1.forceY - fy;
    				node2.forceX = node2.forceX + fx;
    				node2.forceY = node2.forceY + fy;
    				this.nodes.set(i, node1);
    				this.nodes.set(j, node2);
    			}
    		}
    	}
    	Edge currentEdge;
    	double springForce;
    	
    	//Calculate the spring force between all adjacent pairs of nodes
    	for (int i=0;i<this.numEdges;i++){
    		currentEdge = this.edges.get(i);
    		node1 = this.nodes.get(currentEdge.node1);
    		node2 = this.nodes.get(currentEdge.node2);
    		dx = node2.x - node1.x;
    		dy = node2.y - node1.y;
    		if (dx!=0 || dy!=0){
    			distance = Math.sqrt(dx*dx + dy*dy);
    			springForce = this.springK*(distance - this.springRestLength);
    			fx = springForce*dx/distance;
    			fy = springForce*dy/distance;
    			node1.forceX = node1.forceX + fx;
    			node1.forceY = node1.forceY + fy;
    			node2.forceX = node2.forceX - fx;
    			node2.forceY = node2.forceY - fy;
    			this.nodes.set(currentEdge.node1,node1);
    			this.nodes.set(currentEdge.node2,node2);
    		}
    	}
    	
    	Node currentNode;
    	//Update positions of nodes
    	for (int i=0;i<this.numNodes;i++){
    		currentNode = this.nodes.get(i);
    		dx = this.timeStep * currentNode.forceX;
    		dy = this.timeStep * currentNode.forceY;
    		distanceSquared = dx*dx + dy*dy;
    		distance = Math.sqrt(distanceSquared);
    		dx = dx*distance;
    		dy = dy*distance;
            currentNode.x = (float) (currentNode.x + dx);
            currentNode.y = (float) (currentNode.y + dy);
            this.nodes.set(i,currentNode);
    	}
    }
}
