package searchAlgorithms;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;

import environment.Intersection;
import environment.Map;
import environment.Path;
import environment.Segment;
import environment.Step;

/**
 * Abstract class for the search algorithms
 *
 */
public abstract class Algorithm {

	//This is the metric that will decide the type of path
	public abstract double getMetric(Intersection origin, Intersection destination, int maxSpeed);
	
	public static int MAXSPEED = 120;

	/**
	 * Dijkstra https://en.wikipedia.org/?title=Dijkstra%27s_algorithm#Pseudocode.
	 * 
	 * @param origin
	 * @return
	 */
	public HashMap<Intersection, Intersection> shortestPathsFrom(Map map, String originID, int maxSpeed){

		HashMap<Intersection, Double> time = new HashMap<Intersection, Double>();
		HashMap<Intersection, Intersection> prev = new HashMap<Intersection, Intersection>();
		Queue<Intersection> q = new LinkedList<Intersection>();

		Intersection origin = map.getIntersectionByID(originID);

		time.put(origin, 0.0);
		prev.put(origin, null);

		for(Intersection intersection: map.getIntersections()){

			if(!intersection.equals(origin)){

				time.put(intersection, Double.MAX_VALUE);
				prev.put(intersection, null);
			}

			q.add(intersection);
		}

		while(!q.isEmpty()){

			Intersection u = getMinimum(time, q);

			q.remove(u);

			for(Intersection v: getNeighbours(u)){ //Each neighbour

				if(v == null){

					break;
				}

				double alt = time.get(u) + getMetric(u, v, maxSpeed);

				if(alt < time.get(v)){

					time.put(v, alt);
					prev.put(v, u);
				}
			}
		}

		return prev;	
	}


	/**
	 * Given the Dijkstra result, this returns the distance to a destination.
	 * 
	 * @param prev
	 * @param destination
	 * @return
	 */
	public double getDistance(Map map, HashMap<Intersection, Intersection> prev, String destination){

		return this.getDistance(prev, map.getIntersectionByID(destination));
	}

	/**
	 * Same as above using Intersections instead of ID.
	 * 
	 * @param dijks
	 * @param destination
	 * @return
	 */
	public double getDistance(HashMap<Intersection, Intersection> dijks, Intersection destination){

		//Get the path
		double ret = 0;

		Intersection u = destination;

		while(dijks.get(u) != null){

			ret += this.getLenghtToNeighbour(u, dijks.get(u));
			u = dijks.get(u);
		}

		return ret;
	}

	/**
	 * Returns the shortest intersection path to a destination.
	 * 
	 * @param dijks
	 * @param destination
	 * @return
	 */
	public List<String> getIntersectionPath(HashMap<Intersection, Intersection> dijks, Intersection destination){

		ArrayList<String> ret = new ArrayList<String>();

		Intersection u = destination;

		while(dijks.get(u) != null){

			ret.add(0, dijks.get(u).getId());
			u = dijks.get(u);
		}

		ret.add(ret.size(), destination.getId());

		return ret;
	}

	/**
	 * Returns the shortest graphical path to a destination.
	 * 
	 * @param dijks
	 * @param destination
	 * @return
	 */
	public List<Step> getGraphicalPath(Map map, HashMap<Intersection, Intersection> dijks, Intersection destination){

		List<String> path = this.getIntersectionPath(dijks, destination);

		List<Step> graphicalPath = new LinkedList<Step>();

		for( int i=0; i<path.size()-1; i++){

			Intersection in = map.getIntersectionByID(path.get(i));

			for(Segment seg: in.getOutSegments()){

				if (seg.getDestination().getId().equals(path.get(i+1))){

					graphicalPath.addAll(seg.getSteps());
				}
			}
		}

		return graphicalPath;
	}

	/**
	 * Returns the shortest segment path to a destination.
	 * 
	 * @param dijks
	 * @param destination
	 * @return
	 */
	public List<Segment> getSegmentPath(Map map, HashMap<Intersection, Intersection> dijks, Intersection destination){

		List<String> path = this.getIntersectionPath(dijks, destination);

		List<Segment> segmentPath = new LinkedList<Segment>();

		for( int i=0; i<path.size()-1; i++){

			Intersection in = map.getIntersectionByID(path.get(i));

			for(Segment seg: in.getOutSegments()){

				if (seg.getDestination().getId().equals(path.get(i+1))){

					segmentPath.add(seg);
				}
			}
		}

		return segmentPath;
	}

	/**
	 * Returns the shortest graphical path to a destination.
	 * 
	 * @param dijks
	 * @param destination
	 * @param path
	 * @return
	 */
	public List<Step> getGraphicalPath(Map map, HashMap<Intersection, Intersection> dijks, Intersection destination, List<String> path){

		path = this.getIntersectionPath(dijks, destination);

		List<Step> graphicalPath = new LinkedList<Step>();

		for( int i=0; i<path.size()-1; i++){

			Intersection in = map.getIntersectionByID(path.get(i));

			for(Segment seg: in.getOutSegments()){

				if (seg.getDestination().getId().equals(path.get(i+1))){

					graphicalPath.addAll(seg.getSteps());
				}
			}
		}

		return graphicalPath;
	}

	/**
	 * Returns the shortest segment path to a destination.
	 * 
	 * @param dijks
	 * @param destination
	 * @param path
	 * @return
	 */
	public List<Segment> getSegmentPath(Map map, HashMap<Intersection, Intersection> dijks, Intersection destination, List<String> path){

		path = this.getIntersectionPath(dijks, destination);

		List<Segment> segmentPath = new LinkedList<Segment>();

		for( int i=0; i<path.size()-1; i++){

			Intersection in = map.getIntersectionByID(path.get(i));

			for(Segment seg: in.getOutSegments()){

				if (seg.getDestination().getId().equals(path.get(i+1))){

					segmentPath.add(seg);
				}
			}
		}

		return segmentPath;
	}

	/**
	 * This method calculates all the different ways of representing a path.
	 * 
	 * @param initialIntersection
	 * @param finalIntersection
	 * @return
	 */
	
	public Path getPath(Map map, String initialIntersection, String finalIntersection, int maxSpeed) {

		//Calculate dijkstra
		HashMap<Intersection, Intersection> dijks = this.shortestPathsFrom(map, initialIntersection, maxSpeed);

		//Calculate the intersection path
		List<String> intersectionPath = this.getIntersectionPath(dijks, map.getIntersectionByID(finalIntersection));

		//Calculate the graphical path
		List<Step> graphicalPath = this.getGraphicalPath(map, dijks, map.getIntersectionByID(finalIntersection), intersectionPath);

		//Calculate the segment path
		List<Segment> segmentPath = this.getSegmentPath(map, dijks, map.getIntersectionByID(finalIntersection), intersectionPath);

		return new Path(intersectionPath, graphicalPath, segmentPath);
	}

	/**
	 * Gets the minimum
	 * 
	 * @param dict
	 * @param queue
	 * @return
	 */
	private Intersection getMinimum(HashMap<Intersection, Double> dict, Queue<Intersection> queue){

		double min = Double.MAX_VALUE;
		Intersection ret = queue.peek();

		Set<Intersection> keys = dict.keySet();

		for(Intersection intersection: keys){

			if(queue.contains(intersection) && dict.get(intersection) < min){

				min = dict.get(intersection);
				ret = intersection;
			}
		}

		return ret;
	}

	/**
	 * Gets the neighbours of an intersection 
	 * 
	 * @param intersection
	 * @return
	 */
	private List<Intersection> getNeighbours(Intersection intersection){

		List<Intersection> ret = new ArrayList<Intersection>();

		for(Segment segment: intersection.getOutSegments()){

			ret.add(segment.getDestination());
		}

		return ret;
	}

	/**
	 * Gets the distance to a neighbour
	 * 
	 * @param origin
	 * @param destination
	 * @return
	 */
	private double getLenghtToNeighbour(Intersection origin, Intersection destination){

		double ret = Double.MAX_VALUE;

		for(Segment segment: origin.getOutSegments()){

			if(segment.getDestination().equals(destination)){

				ret = segment.getLength();
				break;
			}
		}

		return ret;
	}
}
