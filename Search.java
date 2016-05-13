package a4;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.Scanner;
import java.util.Stack;
import java.io.BufferedReader;
import java.io.FileReader;



/* ACADEMIC INTEGRITY STATEMENT
 * 
 * By submitting this file, we state that all group members associated
 * with the assignment understand the meaning and consequences of cheating, 
 * plagiarism and other academic offenses under the Code of Student Conduct 
 * and Disciplinary Procedures (see www.mcgill.ca/students/srr for more information).
 * 
 * By submitting this assignment, we state that the members of the group
 * associated with this assignment claim exclusive credit as the authors of the
 * content of the file (except for the solution skeleton provided).
 * 
 * In particular, this means that no part of the solution originates from:
 * - anyone not in the assignment group
 * - Internet resources of any kind.
 * 
 * This assignment is subject to inspection by plagiarism detection software.
 * 
 * Evidence of plagiarism will be forwarded to the Faculty of Science's disciplinary
 * officer.
 */



/* A Simple Search Engine exploring subnetwork of McGill University's webpages.
 * 	
 *	Complete the code provided as part of the assignment package. Fill in the \\TODO sections
 *  
 *  Do not change any of the function signatures. However, you can write additional helper functions 
 *  and test functions if you want.
 *  
 *  Do not define any new classes. Do not import any data structures. 
 *  
 *  Make sure your entire solution is in this file.
 *  
 *  We have simplified the task of exploring the network. Instead of doing the search online, we've
 *  saved the result of an hour of real-time graph traversal on the McGill network into two csv files.
 *  The first csv file "vertices.csv" contains the vertices (webpages) on the network and the second csv 
 *  file "edges.csv" contains the links between vertices. Note that the links are directed edges.
 *  
 *  An edge (v1,v2) is link from v1 to v2. It is NOT a link from v2 to v1.
 * 
 */

public class Search {

	private ArrayList<Vertex> graph;
	private ArrayList<Vertex> BFS_inspector;
	private ArrayList<Vertex> DFS_inspector;
	private Comparator<SearchResult> comparator = new WordOccurrenceComparator();
	private PriorityQueue<SearchResult> wordOccurrenceQueue;
	
	private boolean DebugFlag = false;
	
	/**
	 * You don't have to modify the constructor. It only initializes the graph
	 * as an arraylist of Vertex objects
	 */
	public Search(){
		graph = new ArrayList<Vertex>();
	}
	
	/**
     * Used to invoke the command line search interface. You only need to change
     * the 2 filepaths and toggle between "DFS" and "BFS" implementations.
	 */
	public static void main(String[] args) {
		String pathToVertices = "vertices.csv";
		String pathToEdges = "edges.csv";
		
		Search mcgill_network = new Search();
		mcgill_network.loadGraph(pathToVertices, pathToEdges);
		
		Scanner scan = new Scanner(System.in);
		String keyword;
		
		do{
			System.out.println("\nEnter a keyword to search: ");
			keyword = scan.nextLine();
			keyword = keyword.toLowerCase();
			
			if(keyword.compareToIgnoreCase("EXIT") != 0){
				mcgill_network.search(keyword, "DFS");		//You should be able to change between "BFS" and "DFS"
				mcgill_network.displaySearchResults();
			}

		} while(keyword.compareToIgnoreCase("EXIT") != 0);
		
		System.out.println("\n\nExiting Search...");
		scan.close();
	}
	
	/**
	 * Do not change this method. You don't have to do anything here.
	 * @return
	 */
	public int getGraphSize(){
		return this.graph.size();
	}
	
	/**
	 * This method will either call the BFS or DFS algorithms to explore your graph and search for the
	 * keyword specified. You do not have to implement anything here. Do not change the code.
	 * @param pKeyword
	 * @param pType
	 */
	public void search(String pKeyword, String pType){
			resetVertexVisits();
			wordOccurrenceQueue = new PriorityQueue<SearchResult>(1000, comparator);
			BFS_inspector = new ArrayList<Vertex>();
			DFS_inspector = new ArrayList<Vertex>();
			
			if(pType.compareToIgnoreCase("BFS") == 0){
				Iterative_BFS(pKeyword);
			}
			else{
				Iterative_DFS(pKeyword);
			}
	}
	
	/**
	 * This method is called when a new search will be performed. It resets the visited attribute
	 * of all vertices in your graph. You do not need to do anything here.
	 */
	public void resetVertexVisits(){
		for(Vertex k : graph){
			k.resetVisited();
		}
	}
	
	/**
	 * Do not change the code of this method. This is used for testing purposes. It follows the 
	 * your graph search traversal track to ensure a BFS implementation is performed.
	 * @return
	 */
	public String getBFSInspector(){
		String result = "";
		for(Vertex k : BFS_inspector){
			result = result + "," + k.getURL();
		}
		
		return result;
	}
	
	/**
	 * Do not change the code of this method. This is used for testing purposes. It follows the 
	 * your graph search traversal track to ensure a DFS implementation is performed.
	 * @return
	 */
	public String getDFSInspector(){
		String result = "";
		for(Vertex k : DFS_inspector){
			result = result + "," + k.getURL();
		}
		return result;
	}
	
	/**
	 * This method prints the search results in order of most occurrences. It utilizes
	 * a priority queue (wordOccurrenceQueue). You do not need to change the code.
	 * @return
	 */
	public int displaySearchResults(){
		
		int count = 0;
		while(this.wordOccurrenceQueue.size() > 0){
			SearchResult r = this.wordOccurrenceQueue.remove();
			
			if(r.getOccurrence() > 0){
				System.out.println("Count: " + r.getOccurrence() + ", Page: " + r.getUrl());
				count++;
			}
		}
		
		if(count == 0) System.out.println("No results found for your search query");
		
		return count;
		
	}
	
	/**
	 * This method returns the graph instance. You do not need to change the code.
	 * @return
	 */
	public ArrayList<Vertex> getGraph(){
		return this.graph;
	}
	
	/**
	 * This method takes in the 2 file paths and creates your graph. Each Vertex must be 
	 * added to the graph arraylist. To implement an edge (v1, v2), add v2 to v1.neighbors list
	 * by calling v1.addNeighbor(v2)
	 * @param pVerticesPathFile
	 * @param pEdgesFilePath
	 */
	public void loadGraph(String pVerticesFilePath, String pEdgesFilePath){
		
		// **** LOADING VERTICES ***///
		
//TODO: Load the vertices from the pVerticesFilePath into this.graph. A Vertex needs a url and the words on the page. The 
//		first column of the vertices.csv file contains the urls. The other columns contain the words on the pages, one word per column.
//		Each row is 1 page.
		
		
		int Vertex_Count = Load_Vertices(pVerticesFilePath);
		//tests if vertices loaded. Assisted in debugging.
		if (Vertex_Count <= 0) {
			System.err.println("Load Vertices failed");
			return;
		}
		if(DebugFlag) {
			System.out.printf("\nVertex loaded %d", Vertex_Count);
		}

		// **** END LOADING VERTICES ***///
		
		
		// **** LOADING EDGES ***///
		
//TODO: Load the edges from edges.csv. The file contains 2 columns. An edge is a link from column 1 to column 2.
//		Each row is an edge. Read the edges.csv file line by line. For every line, find the two Vertices that 
//		contain the urls in columns 1 and 2. Add an edge from Vertex v1 to Vertex v2 by calling v1.addNeighbor(v2); 

		int Edge_Count = Load_Edges(pEdgesFilePath);
		//tests if edges loaded. Assisted in debugging.
		if (Edge_Count <= 0) {
			System.err.println("Load Edges failed");
			return;
		}
		if(DebugFlag) {
			System.out.printf("\nEdges Connected %d", Edge_Count);
		}
		
		// **** END LOADING EDGES ***///
		
	}
	
	
	public int Load_Vertices(String pFilePath) {
		//try-catch block to check if vertices upload properly
		try {
			//buffered reader to go through lines of csv file
			BufferedReader br = new BufferedReader(new FileReader(pFilePath));
			String vertex_line;
			int line_num = 0;
			//loops through until line is null(end of document)
		    while ((vertex_line = br.readLine()) != null) {
		    	line_num++;
	    		if (DebugFlag) {
	    			System.out.println(vertex_line);
	    		}
	    		//creates string array from words split by ","
	    		//these are the vertex words
		    	String[] vertices_line_split_array = vertex_line.split(",");
		    	//if the array is empty
		    	if (vertices_line_split_array.length == 0) {
		    		System.err.format("\nBlank line in vertices file at line number %d", line_num);
		    	}
//		    	if (vertices_line_split_array.length == 1) {
//		    		System.err.format("\nNo words in vertices file at line number %d", line_num);
//		    	}
		    	
		    	//first value in array is url. We call this string vertice_url
		    	String vertice_url = vertices_line_split_array[0];
		    	//vertex object curVertex is a new vertex using the first url
		    	Vertex curVertex = new Vertex(vertice_url);
		    	
		    	int word_count = vertices_line_split_array.length;
		    	//loops through length of array
		    	for (int w=1; w < word_count; w++) {
		    		//prints each word
		    		String curWord = vertices_line_split_array[w].toLowerCase();
		    		if (DebugFlag) {
		    			System.out.println(curWord); 
		    		}
		    		//adds to end of curVertex
		    		curVertex.addWord(curWord);
		    	}
		    	//adds to graph
		    	this.graph.add(curVertex);

		    }
		    //close buffered reader
		    br.close();
		}
		//catch exception
		catch (Exception e)
		  {
		    System.err.format("\nException occurred trying to read vertices file '%s'.", pFilePath);
		    e.printStackTrace();
		    return -1;
		  }
		
		return this.graph.size();
	}
	
	//similar to Load_Vertices method
	public int Load_Edges(String pFilePath) {
		//starts at 0. This is a counter 
		int edges_connected = 0;
		//try-catch block to check if edges.csv upload properly
		try {
			//buffered reader to go through lines
			BufferedReader br = new BufferedReader(new FileReader(pFilePath));
			String edge_line;
			int line_num = 0;
			//loops through until null. Basically till last line
		    while ((edge_line = br.readLine()) != null) {
		    	line_num++;

		    	if (DebugFlag) {
		    		System.out.println(edge_line);
		    	}
		    	//splits string into array at points of ","
		    	String[] edges_line_split_array = edge_line.split(",");
		    	//empty array
		    	if (edges_line_split_array.length == 0) {
		    		System.err.format("\nBlank line in edges file at line number %d", line_num);
		    	}
		    	//only one column
		    	if (edges_line_split_array.length == 1) {
		    		System.err.format("\nOnly one column in edges file at line number %d", line_num);
		    	}
		    	//More than 2
		    	if (edges_line_split_array.length > 2) {
		    		System.err.format("\nMore than two columns in edges file at line number %d", line_num);
		    	}
		    	//saves first value to string. This is the url FROM. Similar to v1
		    	String edge_url_from = edges_line_split_array[0];
		    	//saves first value to string. This is the url TO. Similar to v2
		    	String edge_url_to = edges_line_split_array[1];
		    	
	    		if (DebugFlag) {
	    			System.out.println(edge_url_from + " -> " + edge_url_to );
	    		}
	    		//if values for from and to are identical
		    	if (edge_url_from.equals(edge_url_to)) {
		    		System.err.format("\nSame from and to url in edges file at line number %d", line_num);
		    		return 0;
		    	}
		    	//checks if values in arraylist are the from url or to url. Switched to true when identified
		    	boolean found_from = false;
		    	boolean found_to = false;
		    	//for all the from vertices in the graph
		    	for(Vertex curVertex_from : this.graph) {
		    		//if url is equal to the vertex url
		    		if (curVertex_from.getURL().equals(edge_url_from)) {
		    			found_from = true;
		    			//loops from beginng to check TO url
		    			for(Vertex curVertex_to : this.graph) {
				    		if (curVertex_to.getURL().equals(edge_url_to)) {
				    			found_to = true;
				    			edges_connected++;
				    			//runs addNeighbor method on curVertex_from to the corresponding curVertex_to. We have verified it through the previous code.
				    			curVertex_from.addNeighbor(curVertex_to);
				    			if (DebugFlag) {
					    			System.out.printf("\nNeighbor Added %s %s", edge_url_from, edge_url_to);
					    		}
				    			break;
				    		}
		    			}
		    		}
		    	}

		    	// If it ends up here, it means either from or to url was not found
		    	if (found_from == false) {
		    		System.err.format("\nFrom url %s not found at line number %d", edge_url_from, edge_line);
		    	}
		    	if (found_to == false) {
		    		System.err.format("\nTo url %s not found at line number %d", edge_url_to, edge_line);
		    	}
		    }
		    //closes buffer. Done reading.
		    br.close();
		}
		catch (Exception e)
		  {
		    System.err.format("\nException occurred trying to read edges file '%s'.", pFilePath);
		    e.printStackTrace();
		    return -1;
		  }
		
		return edges_connected;
	}
	
	/**
	 * This method must implement the Iterative Breadth-First Search algorithm. Refer to the lecture
	 * notes for the exact implementation. Fill in the //TODO lines
	 * @param pKeyword
	 */
	public void Iterative_BFS(String pKeyword){
		ArrayList<Vertex> BFSQ = new ArrayList<Vertex>();	//This is your breadth-first search queue.
		Vertex start = graph.get(0);						//We will always start with this vertex in a graph search
				
		start.setVisited();
		Search_for_Occurances_and_record_in_SearchResults(start, pKeyword);
		BFSQ.add(start);
		BFS_inspector.add(start);
		
//TODO: Complete the Code. Please add the line BFS_inspector.add(vertex); immediately after any insertion to your Queue BFSQ.add(vertex); This
//		is used for testing the validity of your code. See the above lines.
		
		//while the arraylist is not empty
		while(!BFSQ.isEmpty()) {
			//curVertex is the top vertex
			Vertex curVertex = BFSQ.remove(0);
			//creates arraylist of type vertex called curNeighbors. This is a result of running getNeighbors on the curVertex
			ArrayList<Vertex> curNeighbors = curVertex.getNeighbors();
			//visits all neighbors and switches visited boolean to true once "visited"
			for(Vertex curNeigbor : curNeighbors) {
				if(curNeigbor.visited == false) {
					curNeigbor.visited = true;
					BFSQ.add(curNeigbor);					
					BFS_inspector.add(curNeigbor);
					//runs below method on curNeighbor and pKeyword parameters
					Search_for_Occurances_and_record_in_SearchResults(curNeigbor, pKeyword);
				}
			}
		}
		
//TODO: When you explore a page, count the number of occurrences of the pKeyword on that page. You can use the String.contains() method to count.
//		Save your results into a SearchResult object "SearchResult r = new SearchResult(vertex.getURL(), occurrence);"
//		Also, add the SearchResult into this.wordOccurrenceQueue queue.
		
	}
	
	public void Search_for_Occurances_and_record_in_SearchResults(Vertex curNode, String pKeyword) {
		// search for keyword and record in searchResults
		int word_occurances = 0;
		for (String curWord : curNode.getWords()) {
			if(curWord.contains(pKeyword)) {
				word_occurances++;
			}
		}
		if(word_occurances > 0) {
			SearchResult r = new SearchResult(curNode.getURL(), word_occurances);
			this.wordOccurrenceQueue.add(r);
		}
	}

	
	/**
	 * This method must implement the Iterative Depth-First Search algorithm. Refer to the lecture
	 * notes for the exact implementation. Fill in the //TODO lines
	 * @param pKeyword
	 */
	public void Iterative_DFS(String pKeyword){
		Stack<Vertex> DFSS = new Stack<Vertex>();	//This is your depth-first search stack.
		Vertex start = graph.get(0);				//We will always start with this vertex in a graph search
		
//TODO: Complete the code. Follow the same instructions that are outlined in the Iterative_BFS() method.		
		//starts at beginning(0 index of graph arraylist) and runs setVisited
		start.setVisited();
		Search_for_Occurances_and_record_in_SearchResults(start, pKeyword);
		DFSS.push(start);
		DFS_inspector.add(start);
		//while not empty
		while(!DFSS.isEmpty()) {
			Vertex curVertex = DFSS.pop();
			ArrayList<Vertex> curNeighbors = curVertex.getNeighbors();
			for(Vertex curNeigbor : curNeighbors) {
				if(curNeigbor.visited == false) {
					curNeigbor.visited = true;
					DFSS.push(curNeigbor);
					DFS_inspector.add(curNeigbor);
					
					Search_for_Occurances_and_record_in_SearchResults(curNeigbor, pKeyword);
				}
			}
		}
		
	}
	
	
	/**
	 * This simple class just keeps the information about a Vertex together. 
	 * You do not need to modify this class. You only need to understand how it works.
	 */
	public class Vertex{
		private String aUrl;
		private boolean visited;
		private ArrayList<String> aWords;
		private ArrayList<Vertex> neighbors;
		
		public Vertex(String pUrl){
			this.aUrl = pUrl;
			this.visited = false;
			this.neighbors = new ArrayList<Vertex>();
			this.aWords = new ArrayList<String>();
		}
		
		public String getURL(){
			return this.aUrl;
		}
		
		public void setVisited(){
			this.visited = true;
		}
		
		public void resetVisited(){
			this.visited = false;
		}
		
		public boolean getVisited(){
			return this.visited;
		}
			
		public void addWord(String pWord){
			this.aWords.add(pWord);
		}

		public ArrayList<String> getWords(){
			return this.aWords;
		}
		
		public ArrayList<Vertex> getNeighbors(){
			return this.neighbors;
		}
		
		public void addNeighbor(Vertex pVertex){
			this.neighbors.add(pVertex);
		}

	}

	/**
	 * This simple class just keeps the information about a Search Result. It stores
	 * the occurrences of your keyword in a specific page in the graph. You do not need to modify this class. 
	 * You only need to understand how it works.
	 */
	public class SearchResult{
		private String aUrl;
		private int aWordCount;
		
		public SearchResult(String pUrl, int pWordCount){
			this.aUrl = pUrl;
			this.aWordCount = pWordCount;
		}
		
		public int getOccurrence(){
			return this.aWordCount;
		}
		
		public String getUrl(){
			return this.aUrl;
		}
	}
	
	/**
	 * This class enables us to use the PriorityQueue type. The PriorityQueue needs to know how to 
	 * prioritize its elements. This class instructs the PriorityQueue to compare the SearchResult 
	 * elements based on their word occurrence values.
	 * You do not need to modify this class. You only need to understand how it works.
	 */
	public class WordOccurrenceComparator implements Comparator<SearchResult>{
	    @Override
	    public int compare(SearchResult o1, SearchResult o2){
	    	int x = o1.getOccurrence();
	    	int y = o2.getOccurrence();
	    	
	        if (x > y)
	        {
	            return -1;
	        }
	        if (x < y)
	        {
	            return 1;
	        }
	        return 0;
	    }
	}
}
