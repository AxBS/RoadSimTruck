package searchAlgorithms;

/**
 * Factory pattern
 *
 */
public class AlgorithmFactory {

	/**
	 * Default method, returns the shortest path
	 * 
	 * @return Shortest path
	 */
	public Algorithm getAlgorithm() {

		return this.getAlgorithm(Method.SHORTEST);
	}

	/**
	 * Returns the desired path
	 * 
	 * @param method Desired method
	 * @return Desired path
	 */
	public Algorithm getAlgorithm(Method method) {

		Algorithm ret = null;
		
		//I really dislike switch/case
		if (method.equals(Method.SHORTEST)) {
			
			return new ShortestPathAlgorithm();
			
		} else if (method.equals(Method.FASTEST)) {
			
			return new FastestPathAlgorithm();
			
		} else if (method.equals(Method.SMARTEST)) {
			
			return new SmartestPathAlgorithm();
		}
		
		return ret;
	}
}