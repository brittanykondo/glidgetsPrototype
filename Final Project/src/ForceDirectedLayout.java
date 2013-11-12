//Algorithm from paper: Simple Algorithms for Network Visualization: A Tutorial
//                      by: Michael J. McGuffin

public class ForceDirectedLayout {
	
    public double springRestLength = 100; //Preferred length of an edge, without forces
    public double repulsiveForceK = 0.3; //Between nodes, low: nodes less repulsive
    public double springK = 0.4; //Low: more flexible edges
    public int timeStep = 10;   
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
    ForceDirectedLayout(double l, double rK,double sK,int tS,Node [] n,Edge[] e){
    	this.springRestLength = l;
    	this.repulsiveForceK = rK;
    	this.springK = sK;
    	this.timeStep = tS;    	
    	this.nodes = n;
    	this.numNodes = n.length;
    	this.edges = e;
    	this.numEdges = e.length;
    }
    
    /**Sets all nodes to a random position on the screen
     * random number in range: min + Math.random()*((max-min)+1)
     * */
    public void randomizePositions(){
    	for (int i=0;i<this.numNodes-1;i++){
    		this.nodes[i].x = (int)(Math.random()*((100-0)+1));
    		this.nodes[i].y = (int)(Math.random()*((100-0)+1));
    	}
    }
    
    /**Main body of the algorithm which computes all forces applied to the nodes and adjacent node pairs
     * */
    public void calculateForces (){
    	//Re-set the net forces
    	for (int i=0;i<this.numNodes-1;i++){
    		this.nodes[i].forceX = 0;
    		this.nodes[i].forceY = 0;
    	}
    	Node node1, node2;
    	double dx, dy,distanceSquared,distance,repulsiveForce,fx,fy;
    	
    	//Calculate the repulsive forces between all pairs of nodes
    	for (int i =0; i< this.numNodes-2;i++){
    	   node1 = this.nodes[i];
    		for (int j=i+1;j< this.numNodes-1;j++){
    			node2 = this.nodes[j];
    			dx = node2.x - node1.x;
    		    dy = node2.y - node1.y;
    			if (dx!=0 && dy!=0){
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
    	
    	//Calculate the spring force between all adjacent pairs of nodes
    	for (int i=0;i<this.numEdges-1;i++){
    		
    	}
    }
}
