package searchAlgorithms;

import environment.Intersection;
import environment.Segment;

/**
 * Search for the shortest path. 
 *
 */
public class ShortestPathAlgorithm extends Algorithm {

	@Override
	public double getMetric(Intersection origin, Intersection destination, int maxSpeed){

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
