package searchAlgorithms;

import environment.Intersection;
import environment.Segment;

/**
 * Search for the fastest path, the one that can be done in minimum time
 * without traffic knowledge. 
 *
 */
public class FastestPathAlgorithm extends Algorithm {

	public double getMetric(Intersection origin, Intersection destination, int maxSpeed){

		double ret = Double.MAX_VALUE;

		for(Segment segment: origin.getOutSegments()){

			if(segment.getDestination().equals(destination)){

				ret = segment.getLength()/Math.min(segment.getMaxSpeed(), maxSpeed);
				
				break;
			}
		}

		return ret;
	}
}
