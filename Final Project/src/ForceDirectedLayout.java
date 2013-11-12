//Algorithm from paper: Simple Algorithms for Network Visualization: A Tutorial
//                      by: Michael J. McGuffin

public class ForceDirectedLayout {
	
    public double springRestLength = 50; //Preferred length of an edge, without forces
    public double repulsiveForceK = 6250; //Between nodes, low: nodes less repulsive
    public double springK =1; //Low: more flexible edges
    public double timeStep = 0.04;   
    public Node [] nodes;
    public Edge [] edges;
    public int numNodes, numEdges;
    
    /** Constructor for setting the nodes, using the default constants
     * */
    ForceDirectedLayout(Node [] n, Edge [] e){
    	this.nodes = n;
    	this.numNodes = n.length;
    	this.edges = e;
    	this.numEdges = e.length;
    }
    
    /** Constructor for setting the constants
     * */
    ForceDirectedLayout(double l, double rK,double sK,double tS,Node [] n,Edge[] e){
    	this.springRestLength = l;
    	this.repulsiveForceK = rK;
    	this.springK = sK;
    	this.timeStep = tS;    	
    	this.nodes = n;
    	this.numNodes = n.length;
    	this.edges = e;
    	this.numEdges = e.length;
    }
    
    /**Generates the layout by assigning positions of nodes
     * */
    public void makeLayout(){
    	randomizePositions();
    	calculateForces();
    	for (int i=0;i<this.numNodes-1;i++){
    		
    		System.out.println("After: "+this.nodes[i].x+" "+this.nodes[i].y);
    	}
    }
    
    /**Sets all nodes to a random position on the screen
     * random number in range: min + Math.random()*((max-min)+1)
     * */
    public void randomizePositions(){
    	for (int i=0;i<this.numNodes-1;i++){
    		this.nodes[i].x = (int)(Math.random()*((500-0)+1));
    		this.nodes[i].y = (int)(Math.random()*((500-0)+1));
    		System.out.println("Before: "+this.nodes[i].x+" "+this.nodes[i].y);
    	}
    }
    
    /**Main body of the algorithm which computes all forces applied to the nodes and adjacent node pairs
     * */
    public void calculateForces (){
    	//Re-set the net forces
    	for (int i=0;i<this.numNodes;i++){
    		this.nodes[i].forceX = 0;
    		this.nodes[i].forceY = 0;
    	}
    	Node node1, node2;
    	double dx, dy,distanceSquared,distance,repulsiveForce,fx,fy;
    	
    	//Calculate the repulsive forces between all pairs of nodes
    	for (int i =0; i< this.numNodes;i++){
    	   node1 = this.nodes[i];
    		for (int j=i+1;j< this.numNodes-1;j++){
    			node2 = this.nodes[j];
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
    				this.nodes[i] = node1;
    				this.nodes[j] = node2;
    			}
    		}
    	}
    	Edge currentEdge;
    	double springForce;
    	
    	//Calculate the spring force between all adjacent pairs of nodes
    	for (int i=0;i<this.numEdges;i++){
    		currentEdge = this.edges[i];
    		node1 = this.nodes[currentEdge.node1];
    		node2 = this.nodes[currentEdge.node2];
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
    			this.nodes[currentEdge.node1] = node1;
    			this.nodes[currentEdge.node2] = node2;
    		}
    	}
    	
    	Node currentNode;
    	//Update positions of nodes
    	for (int i=0;i<this.numNodes;i++){
    		currentNode = this.nodes[i];
    		dx = this.timeStep * currentNode.forceX;
    		dy = this.timeStep * currentNode.forceY;
    		distanceSquared = dx*dx + dy*dy;
    		distance = Math.sqrt(distanceSquared);
    		dx = dx*distance;
    		dy = dy*distance;
            currentNode.x = (float) (currentNode.x + dx);
            currentNode.y = (float) (currentNode.y + dy);
            this.nodes[i] = currentNode;
    	}
    }
}
